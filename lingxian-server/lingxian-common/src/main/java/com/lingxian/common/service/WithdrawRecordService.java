package com.lingxian.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lingxian.common.entity.WithdrawRecord;

public interface WithdrawRecordService extends IService<WithdrawRecord> {

    /**
     * 分页获取商户的提现记录
     */
    Page<WithdrawRecord> getByMerchantId(Long merchantId, int page, int pageSize);
}
