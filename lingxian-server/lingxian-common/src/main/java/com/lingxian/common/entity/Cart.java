package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 购物车实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_cart")
public class Cart extends BaseEntity {

    private Long userId;
    private Long merchantId;
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
    private Integer selected;

    // 非数据库字段
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String productImage;
    @TableField(exist = false)
    private BigDecimal currentPrice;
    @TableField(exist = false)
    private Integer stock;
    @TableField(exist = false)
    private Integer productStatus;
}
