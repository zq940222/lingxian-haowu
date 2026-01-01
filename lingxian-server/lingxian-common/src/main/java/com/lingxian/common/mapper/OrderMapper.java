package com.lingxian.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingxian.common.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
