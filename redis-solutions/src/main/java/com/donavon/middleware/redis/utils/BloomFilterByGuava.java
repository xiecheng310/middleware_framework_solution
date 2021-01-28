package com.donavon.middleware.redis.utils;

import com.google.common.base.Preconditions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;

/**
 * @author created by donavon.xie at 2020/5/9 10:51
 */
@Service
public class BloomFilterByGuava {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 根据给定的布隆过滤器添加值
     */
    public <T> void addByBloomFilter(BloomFilterHelper<T> bloomFilterHelper, String key, T value) {
        Preconditions.checkArgument(bloomFilterHelper != null, "bloomFilterHelper不能为空");
        int[] offset = bloomFilterHelper.murmurHashOffset(value);
        for (int i : offset) {
            stringRedisTemplate.opsForValue().setBit(key, i, true);
        }
    }

    /**
     * 根据给定的布隆过滤器判断值是否存在
     */
    public <T> boolean includeByBloomFilter(BloomFilterHelper<T> bloomFilterHelper, String key, T value) {
        Preconditions.checkArgument(bloomFilterHelper != null, "bloomFilterHelper不能为空");
        int[] offset = bloomFilterHelper.murmurHashOffset(value);
        for (int i : offset) {
            Boolean existed = stringRedisTemplate.opsForValue().getBit(key, i);
            Assert.notNull(existed, String.format("bitMap获取索引%d位置的数据为null", i));
            if (!existed) return false;
        }
        return true;
    }

}
