package com.ritchie.middleware.redis.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

/**
 * 使用Redis实现简单限流.控制用户行为: 某个操作在指定时间内,只能发生N次
 *
 * @author created by Ritchie at 2020/5/11 15:05
 */
@RestController
@Slf4j
@RequestMapping("simpleLimiter")
public class SimpleLimiterController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final int MAX_COUNT = 5;
    private static final int PERIOD = 60;

    @GetMapping("sendMsg")
    public String sendMsg(String phone) {
        return canSend(phone)
                ? UUID.randomUUID().toString().replaceAll("-","")
                : phone + "操作过于频繁,请待会再试";
    }

    private boolean canSend(String phone) {
        String key = String.format("simple_limiter:%s:%s", phone, "send_message");
        List<Object> result = stringRedisTemplate.executePipelined((RedisCallback<Long>) connection -> {
            //1. 记录用户该次行为, value 和 score 都使用当前毫秒戳
            long nowTs = System.currentTimeMillis();
            connection.zAdd(key.getBytes(), nowTs, String.valueOf(nowTs).getBytes());
            // 2. 移除滑动窗口之前的数据
            connection.zRemRangeByScore(key.getBytes(), 0, nowTs - PERIOD * 1000);
            // 3. 计算窗口内的数据
            Long size = connection.zCard(key.getBytes());
            // 4. 设置过期时间, 避免冷用户占据内存空间, 预留1s的宽限时间
            connection.expire(key.getBytes(), PERIOD + 1);
            return null;
        });
        if (CollectionUtils.isEmpty(result)) {
            throw new RuntimeException("redis管道命令执行失败,未获取到结果");
        }
        long count  = (long) result.get(2);
        log.info(String.format("手机%s在%d秒内进行了%d次操作", phone, PERIOD, count));
        return count <= MAX_COUNT;
    }


}
