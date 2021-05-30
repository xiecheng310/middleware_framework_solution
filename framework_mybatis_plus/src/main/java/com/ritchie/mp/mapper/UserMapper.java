package com.ritchie.mp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ritchie.mp.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Ritchie create on 2021-05-03
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
