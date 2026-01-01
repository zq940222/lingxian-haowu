package com.lingxian.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lingxian.common.entity.WalletRecord;

public interface WalletRecordService extends IService<WalletRecord> {

    /**
     * 分页获取商户的钱包流水
     */
    Page<WalletRecord> getByMerchantId(Long merchantId, Integer type, int page, int pageSize);
}
