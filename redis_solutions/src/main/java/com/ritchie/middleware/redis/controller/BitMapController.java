package com.ritchie.middleware.redis.controller;

import com.ritchie.middleware.redis.entity.UserSignVO;
import com.ritchie.middleware.redis.support.Constant;
import com.ritchie.middleware.redis.utils.BitMapUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 使用位图bitmap进行用户签到统计
 * bitmap的bitcount 没有直接的api,使用自定义扩展的工具
 *
 * @author created by Ritchie at 2020/5/6 15:01
 */
@RestController
@RequestMapping("bitmap")
public class BitMapController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private BitMapUtils bitMapUtils;

    @PostMapping("sign")
    public String userSign(@RequestBody UserSignVO userSignVO) {
        String username = userSignVO.getUsername();
        LocalDateTime dateTime = LocalDateTime.from(userSignVO.getDate().toInstant().atZone(ZoneId.systemDefault()));
        stringRedisTemplate.opsForValue().setBit(username, dateTime.getDayOfYear(), true);
        return Constant.SUCCESS_MSG;
    }

    /**
     * 统计用户签到次数
     *
     * @param username 用户名
     * @return 总共签到次数
     */
    @GetMapping("count/{username}")
    public Long userSignCount(@PathVariable String username) {
        Long count;
        return (count = bitMapUtils.bitCount(username)) == null ? 0 : count;
    }

}
