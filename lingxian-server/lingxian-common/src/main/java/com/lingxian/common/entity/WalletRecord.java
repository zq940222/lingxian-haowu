package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 钱包流水实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_wallet_record")
public class WalletRecord extends BaseEntity {

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 类型: 1-收入 2-支出
     */
    private Integer type;

    /**
     * 标题
     */
    private String title;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 变动前余额
     */
    private BigDecimal balanceBefore;

    /**
     * 变动后余额
     */
    private BigDecimal balanceAfter;

    /**
     * 关联类型: order/withdraw/refund
     */
    private String refType;

    /**
     * 关联ID
     */
    private Long refId;

    /**
     * 备注
     */
    private String remark;
}
