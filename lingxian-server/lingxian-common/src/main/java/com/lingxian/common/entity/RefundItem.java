package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款商品实体
 */
@Data
@TableName("t_refund_item")
public class RefundItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long refundId;
    private Long productId;
    private String productName;
    private String productImage;
    private String skuName;
    private BigDecimal price;
    private Integer quantity;
    private LocalDateTime createTime;
}
