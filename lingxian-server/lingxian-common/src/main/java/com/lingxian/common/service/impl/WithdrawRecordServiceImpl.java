package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.WithdrawRecord;
import com.lingxian.common.mapper.WithdrawRecordMapper;
import com.lingxian.common.service.WithdrawRecordService;
import org.springframework.stereotype.Service;

@Service
public class WithdrawRecordServiceImpl extends ServiceImpl<WithdrawRecordMapper, WithdrawRecord> implements WithdrawRecordService {

    @Override
    public Page<WithdrawRecord> getByMerchantId(Long merchantId, int page, int pageSize) {
        return page(new Page<>(page, pageSize),
                new LambdaQueryWrapper<WithdrawRecord>()
                        .eq(WithdrawRecord::getMerchantId, merchantId)
                        .orderByDesc(WithdrawRecord::getCreateTime));
    }
}
