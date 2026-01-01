package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.WalletRecord;
import com.lingxian.common.mapper.WalletRecordMapper;
import com.lingxian.common.service.WalletRecordService;
import org.springframework.stereotype.Service;

@Service
public class WalletRecordServiceImpl extends ServiceImpl<WalletRecordMapper, WalletRecord> implements WalletRecordService {

    @Override
    public Page<WalletRecord> getByMerchantId(Long merchantId, Integer type, int page, int pageSize) {
        LambdaQueryWrapper<WalletRecord> wrapper = new LambdaQueryWrapper<WalletRecord>()
                .eq(WalletRecord::getMerchantId, merchantId)
                .orderByDesc(WalletRecord::getCreateTime);

        if (type != null) {
            wrapper.eq(WalletRecord::getType, type);
        }

        return page(new Page<>(page, pageSize), wrapper);
    }
}
