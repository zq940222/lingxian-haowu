package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.Order;
import com.lingxian.common.mapper.OrderMapper;
import com.lingxian.common.service.OrderService;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
}
