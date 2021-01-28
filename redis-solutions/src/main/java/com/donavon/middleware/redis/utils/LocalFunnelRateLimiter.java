package com.donavon.middleware.redis.utils;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.donavon.middleware.redis.support.Constant.DEFAULT_FUNNEL_QUOTA;

/**
 * @author created by donavon.xie at 2020/5/12 12:55
 */
@Component
public class LocalFunnelRateLimiter {


    private Map<String, Funnel> funnels = new HashMap<>();

    public boolean isActionAllowed(String phone, String actionKey, int capacity, double leakingRate, int quota) {
        String key = String.format("%s:%s", phone, actionKey);
        Funnel funnel = funnels.get(key);
        if (funnel == null) {
            funnel = new Funnel(capacity, leakingRate);
            funnels.put(key, funnel);
        }
        quota = quota == 0 ? DEFAULT_FUNNEL_QUOTA : quota;
        return funnel.watering(quota);
    }


    @Data
    static class Funnel {
        /**
         * 容量
         */
        private int capacity;
        /**
         * 流水速率
         */
        private double leakingRate;
        /**
         * 剩余空间
         */
        private int leftQuota;
        /**
         * 上次加水时间
         */
        private long leakingTs;

        public Funnel(int capacity, double leakingRate) {
            this.capacity = capacity;
            this.leakingRate = leakingRate;
            this.leftQuota = capacity;
            this.leakingTs = System.currentTimeMillis();
        }

        /**
         * 修正剩余空间
         */
        void mackSpace() {
            // 1. 计算出漏掉的空间(通过漏水,腾出的空间)
            long now = System.currentTimeMillis();
            int deltaQuota = (int) Math.floor((now - leakingTs) * leakingRate);
            // 2. 如果时间间隔太长, 超过int的最大值
            if (deltaQuota < 0) {
                this.leftQuota = capacity;
                leakingTs = now;
                return;
            }
            // 3. 如果腾出的空间太小
            if (deltaQuota == 0) {
                return;
            }

            // 4. 修正剩余空间
            this.leftQuota += deltaQuota;
            this.leakingTs = now;
            if (this.leftQuota > capacity) {
                this.leftQuota = capacity;
            }
        }

        /**
         * 加水
         *
         * @param quota 加水量
         */
        boolean watering(int quota) {
            // 首先修正剩余空间
            mackSpace();
            if (this.leftQuota >= quota) {
                this.leftQuota -= quota;
                return true;
            }
            return false;
        }

    }


}
