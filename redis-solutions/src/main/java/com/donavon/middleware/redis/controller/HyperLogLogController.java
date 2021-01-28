package com.donavon.middleware.redis.controller;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * HyperLogLog: 不精准的去重统计
 *
 * @author created by donavon.xie at 2020/5/6 16:14
 */
@RestController
@RequestMapping("hyper")
public class HyperLogLogController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("total")
    public String userViewCount(Integer size) {
        String key = "UV";
        size = size == null || size <= 0 ? 100 : size;
        int count = 0;

        // 添加
        for (int i = 1; i <= size; i++) {
            if (i % 11 == 0) {
                stringRedisTemplate.opsForHyperLogLog().add(key, "user:" + (i - 1));    // 制造重复数据
                continue;
            }
            stringRedisTemplate.opsForHyperLogLog().add(key, "user:" + i);
            count++;
        }

        // 统计
        Long total = stringRedisTemplate.opsForHyperLogLog().size(key);

        return String.format("设计人数: %d, 实际人数: %d, Hyper统计数量: %d", size, count, total);
    }
}
