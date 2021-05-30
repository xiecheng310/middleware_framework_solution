package com.ritchie.middleware.redis.utils;

import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Component
public class BitMapUtils {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public Long bitCount(final String key) {
        return stringRedisTemplate.execute((RedisCallback<Long>) conn -> conn.bitCount(key.getBytes()));
    }

    public Long bitCount(String key, int start, int end) {
        return stringRedisTemplate.execute((RedisCallback<Long>) con -> con.bitCount(key.getBytes(), start, end));
    }

    public Long bitOp(RedisStringCommands.BitOperation op, String saveKey, String... desKey) {
        byte[][] bytes = new byte[desKey.length][];
        for (int i = 0; i < desKey.length; i++) {
            bytes[i] = desKey[i].getBytes();
        }
        return stringRedisTemplate.execute((RedisCallback<Long>) con -> con.bitOp(op, saveKey.getBytes(), bytes));
    }

}