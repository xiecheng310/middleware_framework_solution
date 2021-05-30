package com.ritchie.mp.test;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ritchie.mp.entity.PageResult;
import com.ritchie.mp.entity.User;
import com.ritchie.mp.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ritchie create on 2021-05-03
 */
@SpringBootTest
@Slf4j
public class UserTest {
    @Resource
    private UserMapper userMapper;
    // ----------------------------------- 基本操作 --------------------------------------

    /**
     * 查询所有
     */
    @Test
    public void findAll() {
        List<User> users = userMapper.selectList(null);
        System.out.println(JSON.toJSONString(users));
    }

    /**
     * 添加操作
     */
    @Test
    public void addUser() {
        User user = User.builder()
                .name("Lucy")
                .age(22)
                .email("Lucy@163.com").build();
        int rows = userMapper.insert(user);
        log.info("insert {} user", rows);
    }

    /**
     * 修改操作
     */
    @Test
    public void updateUser() {
        User user = User.builder()
                .id(1389223572441993218L)
                .name("kuangzhan")
                .age(32)
                .email("kuangzhan@163.com").build();
        int rows = userMapper.updateById(user);
        log.info("update {} user", rows);
    }

    /**
     * 乐观锁测试
     * 1. 添加字段和注解 @Version
     * 2. 配置乐观锁Bean. @see com.ritchie.mp.config.MybatisPlusConfig
     */
    @Test
    public void testLock() {
        User user = userMapper.selectById(1);
        user.setAge(80);
        int rows = userMapper.updateById(user);
        log.info("update {} user", rows);
    }

    // ---------------------------------- 查询 ---------------------------------

    /**
     * 多id批量查询
     */
    @Test
    public void testSelectByIds() {
        List<User> users = userMapper.selectBatchIds(Arrays.asList(1, 2, 3, 4));
        users.forEach(u -> log.info("user: {}", u));
    }

    /**
     * 简单条件查询, 通过map
     */
    @Test
    public void testSelectByCondition() {
        Map<String, Object> condition = new HashMap<>();
        condition.put("name", "ritchie");
        condition.put("age", 32);
        List<User> users = userMapper.selectByMap(condition);
        users.forEach(u -> log.info("user: {}", u));
    }

    /**
     * 分页查询
     * 1. 配置分页插件Bean. @see com.ritchie.mp.config.MybatisPlusConfig
     * 2. 构建分页查询类: Page
     */
    @Test
    public void testSelectPage() {
        long offset = 1;
        long limit = 10;
        Page<User> page = new Page<>(offset, limit);
        PageResult<User> result = new PageResult<>();
        Page<User> pageData = userMapper.selectPage(page, null);
        result.setTotal(pageData.getTotal());
        result.setData(pageData.getRecords());
        log.info("page result is: {}", JSON.toJSONString(result));
    }

    // ------------------------- 删除测试 -------------------------

    /**
     * 根据id删除
     * 如果是逻辑删除.
     *  1. 添加逻辑删除字段,属性
     *  2. 添加注解:@TableLogic
     */
    @Test
    public void testDeleteById() {
        int rows = userMapper.deleteById(1389227997059362818L);
        log.info("delete {} rows", rows);
    }

    /**
     * 根据id批量删除
     */
    @Test
    public void testDeleteByIds() {
        int rows = userMapper.deleteBatchIds(Arrays.asList(1389223572441993218L, 1389231899347587074L));
        log.info("delete {} rows", rows);
    }

    /**
     * 根据条件删除
     */
    @Test
    public void testDeleteByCondition() {
        Map<String, Object> condition = new HashMap<>();
        condition.put("name", "Lucy");
        condition.put("age", 22);
        int rows = userMapper.deleteByMap(condition);
        log.info("delete {} rows", rows);
    }

    // ----------------------------------------- 复杂条件查询 --------------------------------

    /**
     * QueryWrapper基本用法
     */
    @Test
    public void testWrapper() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.ge("age", 22);
        List<User> users = userMapper.selectList(wrapper);
        log.info("result: {}", JSON.toJSONString(users));
    }


    /**
     * 通常使用lambda,来避免"age"这样的硬编码
     */
    @Test
    public void testLambdaWrapper3() {
        List<User> users = userMapper.selectList(Wrappers.lambdaQuery(User.class).ge(User::getAge, 22));
        log.info("result: {}", JSON.toJSONString(users));

    }

    /**
     * 链式查询写法, 用于炫技
     */
    @Test
    public void testLambdaChainWrapper() {
        List<User> users = new LambdaQueryChainWrapper<>(userMapper).ge(User::getAge, 30).list();
        log.info("result: {}", JSON.toJSONString(users));
    }


}
