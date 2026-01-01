package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拼团活动实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_group_activity")
public class GroupActivity extends BaseEntity {

    private String name;
    private Long productId;
    private Long merchantId;
    private BigDecimal originalPrice;
    private BigDecimal groupPrice;
    private Integer groupSize;
    private Integer limitPerUser;
    private Integer stock;
    private Integer soldCount;
    private Integer groupCount;
    private Integer expireHours;
    private Integer autoCancel;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;

    // 非数据库字段
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String productImage;
    @TableField(exist = false)
    private String merchantName;
    @TableField(exist = false)
    private String statusName;
    @TableField(exist = false)
    private Integer successGroupCount;
}
