package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品SKU实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_product_sku")
public class ProductSku extends BaseEntity {

    private Long productId;
    private String name;
    private String image;
    private BigDecimal price;
    private Integer stock;
    private Integer salesCount;
    private Integer sort;
}
