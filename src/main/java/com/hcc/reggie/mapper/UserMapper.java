package com.hcc.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hcc.reggie.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
