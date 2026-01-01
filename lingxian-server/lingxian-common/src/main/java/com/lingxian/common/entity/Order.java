package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_order")
public class Order extends BaseEntity {

    private String orderNo;
    private Long userId;
    private Long merchantId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal freightAmount;
    private BigDecimal payAmount;
    private Integer status;
    private Integer payType;
    private String payTradeNo;
    private LocalDateTime payTime;
    private Integer orderType;
    private Long groupId;
    private String receiverName;
    private String receiverPhone;
    private String receiverProvince;
    private String receiverCity;
    private String receiverDistrict;
    private String receiverAddress;
    private String deliveryCompany;
    private String deliveryNo;
    private LocalDateTime deliveryTime;
    private LocalDateTime receiveTime;
    private LocalDateTime completeTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private String remark;

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
    private String payTypeName;
    @TableField(exist = false)
    private String orderTypeName;
    @TableField(exist = false)
    private List<OrderItem> items;
    @TableField(exist = false)
    private Integer productCount;
}
