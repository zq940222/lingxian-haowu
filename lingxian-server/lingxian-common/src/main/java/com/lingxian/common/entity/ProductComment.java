package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 商品评价实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_product_comment")
public class ProductComment extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单商品ID
     */
    private Long orderItemId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评分 1-5星
     */
    private Integer rating;

    /**
     * 评价图片JSON数组
     */
    private String images;

    /**
     * 是否匿名 0-否 1-是
     */
    private Integer isAnonymous;

    /**
     * 商家回复内容
     */
    private String replyContent;

    /**
     * 商家回复时间
     */
    private LocalDateTime replyTime;

    /**
     * 状态 0-隐藏 1-显示
     */
    private Integer status;

    // 非数据库字段
    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String userAvatar;

    @TableField(exist = false)
    private String productName;

    @TableField(exist = false)
    private String productImage;
}
