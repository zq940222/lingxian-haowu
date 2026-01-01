package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 提现账户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_withdraw_account")
public class WithdrawAccount extends BaseEntity {

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 账户类型: wechat/alipay/bank
     */
    private String type;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 账号
     */
    private String accountNo;

    /**
     * 开户银行（银行卡类型使用）
     */
    private String bankName;

    /**
     * 开户支行（银行卡类型使用）
     */
    private String bankBranch;

    /**
     * 是否默认: 0-否 1-是
     */
    private Integer isDefault;
}
