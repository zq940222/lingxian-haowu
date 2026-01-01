package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_product")
public class Product extends BaseEntity {

    private Long merchantId;
    private Long categoryId;
    private String name;
    private String image;
    private String images;
    private String video;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal costPrice;
    private Integer stock;
    private Integer salesCount;
    private String unit;
    private Integer weight;
    private String description;
    private String detail;
    private Integer status;
    private Integer isRecommend;
    private Integer isNew;
    private Integer isHot;
    private Integer sort;

    // 非数据库字段
    @TableField(exist = false)
    private String categoryName;
    @TableField(exist = false)
    private String merchantName;
    @TableField(exist = false)
    private List<ProductSku> skus;
}
