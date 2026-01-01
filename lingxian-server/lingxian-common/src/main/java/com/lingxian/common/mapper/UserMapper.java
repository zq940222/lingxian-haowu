package com.lingxian.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingxian.common.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
