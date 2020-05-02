package com.cgg.middleware.redis.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author created by donavon.xie at 2020/4/30 16:21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskItem<T> {
    private String id;
    private T msg;
}
