package com.donavon.middleware.redis.controller;

import com.donavon.middleware.redis.utils.DistributeFunnelRateLimiter;
import com.donavon.middleware.redis.utils.LocalFunnelRateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 漏斗限流算法
 * 限制一个用户(手机号码),发送验证码的请求频率为 60S 只能进行5次
 *
 * @author created by donavon.xie at 2020/5/12 12:48
 */
@RestController
@Slf4j
@RequestMapping("funnel")
public class FunnelLimiterController {

    private static final String ACTION_KEY = "sendMsg";
    private static final int PERIOD = 60;
    private static final int MAX_COUNT = 5;

    @Resource
    private LocalFunnelRateLimiter localFunnelRateLimiter;
    @Resource
    private DistributeFunnelRateLimiter distributeFunnelRateLimiter;

    /**
     * 使用单机版自定义的漏斗实现限流
     *
     * @param phone 用户手机号码
     * @return 请求结果
     */
    @PostMapping("single")
    public String singleFunnelRateLimit(String phone) {
        /*
         * 初始容量capacity: 多少次请求之后进行限流.比如初始值为10,那么就在10次请求之后会进行限流
         * 定额quota: 一次请求占用的容量
         */
        double rate = MAX_COUNT / (PERIOD * 1000.0); // 单位是ms
        boolean allowed = localFunnelRateLimiter.isActionAllowed(phone, ACTION_KEY, MAX_COUNT, rate, 1);
        return String.format("%s的发送消息请求%s", phone, allowed);
    }

    /**
     * 使用Redis-Cell插件提供的漏斗限流功能
     *
     * @param phone 用户手机
     * @return 请求结果
     */
    @PostMapping("redis")
    public String redisFunnelRateLimiter(String phone) {
        boolean allowed = distributeFunnelRateLimiter.throttle(phone, ACTION_KEY, PERIOD, MAX_COUNT, 5);
        return String.format("%s的发送消息请求%s", phone, allowed);
    }


}
