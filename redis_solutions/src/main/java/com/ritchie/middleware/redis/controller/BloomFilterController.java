package com.ritchie.middleware.redis.controller;

import com.ritchie.middleware.redis.utils.BloomFilterByGuava;
import com.ritchie.middleware.redis.utils.BloomFilterByLua;
import com.ritchie.middleware.redis.utils.BloomFilterHelper;
import com.google.common.base.Charsets;
import com.google.common.hash.Funnel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 使用布隆过滤器进行
 * 1. 重复判断(如爬虫URL去重)
 * 2. 防止缓存击穿
 *
 * @author created by Ritchie at 2020/5/6 16:38
 */
@RestController
@RequestMapping("bloom")
@Slf4j
public class BloomFilterController {

    @Resource
    private BloomFilterByLua bloomFilterByLua;
    @Resource
    private BloomFilterByGuava bloomFilterByGuava;

    /**
     * 使用lua脚本操作redis的bloom filter 插件
     *
     * @param name 布隆过滤器名称
     * @param rate 误判率(假阳性概率 fpp)
     * @param size 布隆过滤器大小
     * @return 测试结果
     */
    @PostMapping("lua")
    public String testLuaBloom(String name, Double rate, Long size) {
        rate = rate == null || rate <= 0 || rate >= 1 ? 0.01 : rate;
        size = size == null || size <= 0 ? 100 : size;

        // 创建布隆过滤器, 如果不创建, 可以通过add自动创建,error_rate默认 1%, initial_size默认100
        String bloomKey = "bloom:test:lua";

        List<String> doubleNames = new ArrayList<>();
        List<String> singleNames = new ArrayList<>();
        assignNames(name, size, doubleNames, singleNames);

        // 在布隆中添加 doubleNames
        for (String s : doubleNames) {
            bloomFilterByLua.add(bloomKey, s, rate, size);
        }

        int count = 0;
        int errorCount = 0;

        // 检查singleNames的元素是否存在
        // 不能再去检查doubleNames,看是否不存在! 因为布隆对于见过的元素,一定不会误判. 只会误判没有见过的元素
        for (String s : singleNames) {
            boolean exist = bloomFilterByLua.exist(bloomKey, s);
            if (exist) {
                count++;
                if (!doubleNames.contains(s)) {
                    errorCount ++;
                    log.info(s + "出现了误判, 布隆认为存在, 实际不存在");
                }
            }
        }

        return String.format("通过布隆判断存在的数量是: %d, 布隆的误判数量: %d", count, errorCount);
    }

    private void assignNames(String name, Long size, List<String> doubleNames, List<String> singleNames) {
        for (int i = 1; i <= size; i++) {
            String n = name + "_" + i;
            if (i % 2 == 0) {
                doubleNames.add(n);
            } else {
                singleNames.add(n);
            }
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @PostMapping("guava")
    public String testGuavaBloom(String name, Double rate, Long size) {
        rate = rate == null || rate <= 0 || rate >= 1 ? 0.01 : rate;
        size = size == null || size <= 0 ? 100 : size;

        String bloomKey = "bloom:test:guava";
        List<String> doubleNames = new ArrayList<>();
        List<String> singleNames = new ArrayList<>();
        assignNames(name, size, doubleNames, singleNames);

        // 在布隆中添加 doubleNames
        BloomFilterHelper<String> helper = new BloomFilterHelper<>(
                (Funnel<String>) (from, into) -> into.putString(from, Charsets.UTF_8), size.intValue(), rate
        );
        for (String s : doubleNames) {
            bloomFilterByGuava.addByBloomFilter(helper, bloomKey, s);
        }

        // 测试是否存在
        int count = 0;
        int errorCount = 0;

        // 检查singleNames的元素是否存在
        // 不能再去检查doubleNames,看是否不存在! 因为布隆对于见过的元素,一定不会误判. 只会误判没有见过的元素
        for (String s : singleNames) {
            boolean existed = bloomFilterByGuava.includeByBloomFilter(helper, bloomKey, s);
            if (existed) {
                count++;
                if (!doubleNames.contains(s)) {
                    errorCount ++;
                    log.info(s + "出现了误判, 布隆认为存在, 实际不存在");
                }
            }
        }

        return String.format("通过布隆判断存在的数量是: %d, 布隆的误判数量: %d", count, errorCount);
    }

}
