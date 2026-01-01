package com.lingxian.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lingxian.common.entity.WithdrawAccount;

import java.util.List;

public interface WithdrawAccountService extends IService<WithdrawAccount> {

    /**
     * 获取商户的提现账户列表
     */
    List<WithdrawAccount> getByMerchantId(Long merchantId);

    /**
     * 设置默认账户
     */
    boolean setDefault(Long merchantId, Long accountId);
}
