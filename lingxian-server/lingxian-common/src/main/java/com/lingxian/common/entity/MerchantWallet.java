package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商户钱包实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_merchant_wallet")
public class MerchantWallet extends BaseEntity {

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 可用余额
     */
    private BigDecimal balance;

    /**
     * 累计收入
     */
    private BigDecimal totalIncome;

    /**
     * 累计提现
     */
    private BigDecimal totalWithdraw;

    /**
     * 待结算金额
     */
    private BigDecimal pendingSettle;

    /**
     * 冻结金额
     */
    private BigDecimal frozenAmount;
}
