package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.Refund;
import com.lingxian.common.mapper.RefundMapper;
import com.lingxian.common.service.RefundService;
import org.springframework.stereotype.Service;

@Service
public class RefundServiceImpl extends ServiceImpl<RefundMapper, Refund> implements RefundService {
}
