package com.ritchie.mp.entity;

import lombok.Data;

import java.util.List;

/**
 * @author Ritchie create on 2021-05-03
 */
@Data
public class PageResult<T> {
    private long total;
    private List<T> data;
}
