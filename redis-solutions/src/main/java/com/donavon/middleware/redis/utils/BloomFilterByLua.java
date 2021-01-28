package com.donavon.middleware.redis.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * @author created by donavon.xie at 2020/5/7 14:52
 */
@Service
@Slf4j
public class BloomFilterByLua {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisUtils redisUtils;


    /**
     * 创建布隆过滤器
     *
     * @param key         布隆过滤器名称
     * @param errorRate   错误率
     * @param initialSize 初始大小
     */
    public void createBloomFilter(String key, Double errorRate, Long initialSize) {
        String cmd = "return redis.call('bf.reserve', KEYS[1], ARGV[1], ARGV[2])";
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>(cmd, Boolean.class);
        Boolean success = stringRedisTemplate.execute(script, Collections.singletonList(key), errorRate.toString(), initialSize.toString());
        if (success == null || !success) {
            throw new RuntimeException("创建布隆过滤器失败");
        }
    }

    /**
     * 向布隆过滤器添加元素
     *
     * @param key         布隆过滤器名称
     * @param value       元素
     * @param errorRate   错误率
     * @param initialSize 过滤器大小
     */
    public void add(String key, String value, Double errorRate, Long initialSize) {
        // 先手动创建布隆过滤器
        if (! redisUtils.existsKey(key)) {
            createBloomFilter(key, errorRate, initialSize);
        }
        String cmd = "return redis.call('bf.add', KEYS[1], ARGV[1])";
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>(cmd, Boolean.class);
        stringRedisTemplate.execute(script, Collections.singletonList(key), value);
    }


    /**
     * 判断元素是否存在
     *
     * @param key   布隆过滤器名称
     * @param value 判断的元素
     * @return 是否存在
     */
    public boolean exist(String key, String value) {
        String cmd = "return redis.call('bf.exists', KEYS[1], ARGV[1])";
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>(cmd, Boolean.class);
        Boolean exists = stringRedisTemplate.execute(script, Collections.singletonList(key), value);
        if (null == exists) {
            throw new RuntimeException(String.format("判断元素[%s]是否存在失败", value));
        }
        return exists;
    }

}
