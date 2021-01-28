package com.donavon.middleware.redis.controller;

import com.donavon.middleware.redis.entity.TaskItem;
import com.donavon.middleware.redis.support.Constant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * 使用redis zset实现延迟队列
 *
 * @author created by donavon.xie at 2020/4/30 15:49
 */
@RequestMapping("delay")
@RestController
@Slf4j
public class DelayQueueController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;

    @PostMapping("push")
    public String pushMsgToRedisDelayQueue(String msg, Integer delay) {
        delay = delay == null ? 5 : delay;
        TaskItem<String> item = new TaskItem<>(UUID.randomUUID().toString(), msg);
        log.info("===> 接受到消息: " + msg + ", 即将发送到延迟队列, " + delay + "秒后进行消费.");
        // 序列化后,发送到延迟队列
        try {
            String message = objectMapper.writeValueAsString(item);
            Boolean added = stringRedisTemplate.opsForZSet().add(Constant.DELAY_QUEUE_KEY, message, System.currentTimeMillis() + delay * 1000.0);
            if (added == null || !added) {
                log.error("发送消息到延迟队列失败");
                return Constant.FAILED_MSG;
            }
        } catch (JsonProcessingException e) {
            log.error("序列化TaskItem对象失败");
            return Constant.FAILED_MSG;
        }
        return Constant.SUCCESS_MSG;

    }


}
