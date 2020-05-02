package com.cgg.middleware.redis.listener;

import com.cgg.middleware.redis.entity.TaskItem;
import com.cgg.middleware.redis.support.Constant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author created by donavon.xie at 2020/4/30 14:11
 * 用于RedisQueue的消费
 */
@Component
@Slf4j
public class RedisDelayQueueListener implements ApplicationRunner {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Value("${middleware.redis.delay.consumers}")
    private int consumers;

    ThreadPoolExecutor pool = new ThreadPoolExecutor(2, 4, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    /**
     * Callback used to run the bean.
     *
     * @param args incoming application arguments
     * @throws Exception on error
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("===> 延迟队列消费者线程池启动");
        for (int i = 0; i < consumers; i++) {
            pool.execute(this::ListenDelayQueue);
        }
    }

    private void ListenDelayQueue() {
        for (; ; ) {
            /*
             * key : redis key
             * min : 最小分数
             * max : 最大分数.如果element的延迟到了, 当前时间会 > (放入队列时的时间 + 延时)
             * offset : 从哪个开始
             * count : 取几个
             */
            Set<String> tasks = stringRedisTemplate.opsForZSet().rangeByScore(Constant.DELAY_QUEUE_KEY, 0, System.currentTimeMillis(), 0, 1);
            if (tasks == null || tasks.isEmpty()) { // 没有成员超时, 休息片刻
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                continue;
            }
            String s = tasks.iterator().next();
            Long count = stringRedisTemplate.opsForZSet().remove(Constant.DELAY_QUEUE_KEY, s);
            if (count == null || count == 0) {   // 没有抢到
                continue;
            }

            // 进行反序列化
            try {
                TaskItem<String> taskItem = objectMapper.readValue(s, new TypeReference<TaskItem<String>>() {
                });
                log.info("===> 消费到延迟任务: " + taskItem.toString());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                break;
            }

        }
    }
}
