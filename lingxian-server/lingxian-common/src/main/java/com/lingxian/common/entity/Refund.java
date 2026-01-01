package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 退款实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_refund")
public class Refund extends BaseEntity {

    private String refundNo;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long merchantId;
    private BigDecimal refundAmount;
    private Integer refundType;
    private String reason;
    private String description;
    private String images;
    private Integer status;
    private LocalDateTime auditTime;
    private String auditRemark;
    private LocalDateTime refundTime;
    private String refundTradeNo;

    // 非数据库字段
    @TableField(exist = false)
    private String userNickname;
    @TableField(exist = false)
    private String userPhone;
    @TableField(exist = false)
    private String merchantName;
    @TableField(exist = false)
    private String statusName;
    @TableField(exist = false)
    private String refundTypeName;
    @TableField(exist = false)
    private List<RefundItem> items;
}
