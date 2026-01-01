package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.RefundItem;
import com.lingxian.common.mapper.RefundItemMapper;
import com.lingxian.common.service.RefundItemService;
import org.springframework.stereotype.Service;

@Service
public class RefundItemServiceImpl extends ServiceImpl<RefundItemMapper, RefundItem> implements RefundItemService {
}
