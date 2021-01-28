package com.donavon.middleware.redis.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 使用redis4.0之后的Redis-Cell模块进行漏斗限流
 * 需要单独安装该插件
 * cmd: cl.throttle
 *
 * @author created by donavon.xie at 2020/5/14 19:51
 */
@Component
public class DistributeFunnelRateLimiter {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final Integer DEFAULT_QUOTA = 1;

    /**
     * 使用Lua脚本完成Redis-Cell模块的漏斗限流
     *
     * @param phone     用户标识,这里用手机号码
     * @param actionKey 需要限制的用户行为key
     * @param period    限制的时间段
     * @param count     允许的最大次数
     */
    @SuppressWarnings("rawtypes")
    public boolean throttle(String phone, String actionKey, Integer period, Integer count, Integer capacity) {
        String cmd = "return redis.call('cl.throttle', KEYS[1], ARGV[1], ARGV[2], ARGV[3], ARGV[4])";
        DefaultRedisScript<List> script = new DefaultRedisScript<>(cmd, List.class);
        List result = stringRedisTemplate.execute(
                script,
                Collections.singletonList(String.format("%s:%s", phone, actionKey)),
                capacity.toString(),
                count.toString(),
                period.toString(),
                DEFAULT_QUOTA.toString());
        if (CollectionUtils.isEmpty(result)) {
            throw new RuntimeException("redis-cell限流命令执行失败");
        }
        Integer allowed = (Integer) result.get(0);
        return allowed == 0;
    }
}
