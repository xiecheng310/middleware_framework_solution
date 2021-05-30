package com.ritchie.middleware.redis.support;

/**
 * @author created by Ritchie at 2020/4/30 13:45
 */
public interface Constant {

    String SUCCESS_MSG = "操作成功";
    String FAILED_MSG = "操作失败";

    String MSG_QUEUE_KEY = "middleware:redis:queue";
    String DELAY_QUEUE_KEY = "middleware:redis:delay_queue";

    int DEFAULT_FUNNEL_QUOTA = 1;
}
