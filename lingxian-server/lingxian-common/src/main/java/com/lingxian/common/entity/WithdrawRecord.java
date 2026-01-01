package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提现记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_withdraw_record")
public class WithdrawRecord extends BaseEntity {

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 提现账户ID
     */
    private Long accountId;

    /**
     * 账户类型: wechat/alipay/bank
     */
    private String accountType;

    /**
     * 账户名称（显示用）
     */
    private String accountName;

    /**
     * 账号
     */
    private String accountNo;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 提现金额
     */
    private BigDecimal amount;

    /**
     * 手续费
     */
    private BigDecimal fee;

    /**
     * 实际到账金额
     */
    private BigDecimal actualAmount;

    /**
     * 状态: 0-待审核 1-处理中 2-已到账 3-已拒绝 4-已取消
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核人ID
     */
    private Long auditBy;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;
}
