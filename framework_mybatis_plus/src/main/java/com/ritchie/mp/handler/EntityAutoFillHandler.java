package com.ritchie.mp.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author Ritchie create on 2021-05-03
 */
@Component
public class EntityAutoFillHandler implements MetaObjectHandler {
    private static final String CREATE_TIME_KEY = "gmtCreate";
    private static final String UPDATE_TIME_KEY = "gmtModify";

    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName(CREATE_TIME_KEY, new Date(), metaObject);
        this.setFieldValByName(UPDATE_TIME_KEY, new Date(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName(UPDATE_TIME_KEY, new Date(), metaObject);
    }

}
