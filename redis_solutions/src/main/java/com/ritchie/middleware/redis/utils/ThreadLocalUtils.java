package com.ritchie.middleware.redis.utils;

import com.google.common.base.Preconditions;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author created by Ritchie at 2020-05-08 17:48:50
 */
public class ThreadLocalUtils {

    private static final ThreadLocal<Map<Object, Object>> RESOURCES = new ThreadLocal<>();

    public static Map<Object, Object> getResources() {
        return  null != RESOURCES.get() ? new HashMap<>(RESOURCES.get()) : null;
    }

    public static void setResources(Map<Object, Object> newResources) {
        if (CollectionUtils.isEmpty(newResources)) {
            return;
        }
        RESOURCES.set(newResources);
    }

    public static Object getValue(Object key) {
        return RESOURCES.get().get(key);
    }

    public static Object remove(Object key) {
        return RESOURCES.get().remove(key);
    }

    public static void put(Object key, Object value) {
        Preconditions.checkNotNull(key, "key cannot be null");
        RESOURCES.get().put(key, value);
    }

    public static void remove() {
        RESOURCES.remove();
    }

}