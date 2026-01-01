package com.lingxian.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lingxian.common.entity.MerchantWallet;

public interface MerchantWalletService extends IService<MerchantWallet> {

    /**
     * 获取商户钱包，不存在则创建
     */
    MerchantWallet getOrCreateByMerchantId(Long merchantId);

    /**
     * 增加余额
     */
    boolean addBalance(Long merchantId, java.math.BigDecimal amount, String title, String refType, Long refId);

    /**
     * 扣减余额
     */
    boolean subtractBalance(Long merchantId, java.math.BigDecimal amount, String title, String refType, Long refId);
}
