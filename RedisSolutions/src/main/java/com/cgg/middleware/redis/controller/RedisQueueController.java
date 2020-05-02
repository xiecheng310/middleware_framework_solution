package com.cgg.middleware.redis.controller;

import com.cgg.middleware.redis.support.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author created by donavon.xie at 2020/4/30 13:43
 * 使用Redis List作为异步消息队列
 */
@RequestMapping("queue")
@RestController
@Slf4j
public class RedisQueueController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("push")
    public String pushMsgToRedisQueue(String msg) {
        log.info("收到消息:" + msg);
        Long count = stringRedisTemplate.opsForList().rightPush(Constant.MSG_QUEUE_KEY, msg);
        return "往redis消息队列中发送了" + count + "条消息";
    }

}
