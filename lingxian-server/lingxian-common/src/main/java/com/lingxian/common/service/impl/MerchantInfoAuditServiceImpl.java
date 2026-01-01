package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.MerchantInfoAudit;
import com.lingxian.common.mapper.MerchantInfoAuditMapper;
import com.lingxian.common.service.MerchantInfoAuditService;
import org.springframework.stereotype.Service;

@Service
public class MerchantInfoAuditServiceImpl extends ServiceImpl<MerchantInfoAuditMapper, MerchantInfoAudit> implements MerchantInfoAuditService {

    @Override
    public MerchantInfoAudit getPendingAudit(Long merchantId) {
        return this.getOne(new LambdaQueryWrapper<MerchantInfoAudit>()
                .eq(MerchantInfoAudit::getMerchantId, merchantId)
                .eq(MerchantInfoAudit::getStatus, 0)  // 待审核
                .orderByDesc(MerchantInfoAudit::getCreateTime)
                .last("LIMIT 1"));
    }
}
