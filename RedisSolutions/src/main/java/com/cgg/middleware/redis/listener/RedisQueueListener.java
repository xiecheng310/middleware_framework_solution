package com.cgg.middleware.redis.listener;

import com.cgg.middleware.redis.support.Constant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author created by donavon.xie at 2020/4/30 14:11
 * 用于RedisQueue的消费
 */
@Component
@Slf4j
public class RedisQueueListener implements ApplicationRunner {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * Callback used to run the bean.
     *
     * @param args incoming application arguments
     * @throws Exception on error
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("===> Redis消息队列消费者线程启动!");
        for (; ; ) {
            try {
                String msg = (String) stringRedisTemplate.opsForList()
                        .leftPop(Constant.MSG_QUEUE_KEY, 1, TimeUnit.SECONDS);
                if (StringUtils.isNotBlank(msg)) {
                    log.info("===> 从Redis消息队列中消费消息: " + msg);
                }
            } catch (Exception e) {
                log.error("===> 消费消息时发生异常: " + e.getCause().getMessage(), e);
                break;
            }
        }
        log.warn("===> Redis消息队列消费者线程停止 !");
    }
}
