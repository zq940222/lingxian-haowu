package com.lingxian.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lingxian.common.entity.MerchantInfoAudit;

public interface MerchantInfoAuditService extends IService<MerchantInfoAudit> {

    /**
     * 获取商户待审核的信息变更记录
     */
    MerchantInfoAudit getPendingAudit(Long merchantId);
}
