package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 订单评价实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_review")
public class Review extends BaseEntity {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单项ID
     */
    private Long orderItemId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 评分(1-5)
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评价图片(多个逗号分隔)
     */
    private String images;

    /**
     * 是否匿名(0否1是)
     */
    private Integer isAnonymous;

    /**
     * 商家回复
     */
    private String reply;

    /**
     * 回复时间
     */
    private LocalDateTime replyTime;

    /**
     * 状态(0隐藏1显示)
     */
    private Integer status;

    // ========== 非数据库字段 ==========

    /**
     * 用户昵称
     */
    @TableField(exist = false)
    private String userNickname;

    /**
     * 用户头像
     */
    @TableField(exist = false)
    private String userAvatar;

    /**
     * 商品名称
     */
    @TableField(exist = false)
    private String productName;

    /**
     * 商品图片
     */
    @TableField(exist = false)
    private String productImage;

    /**
     * 商户名称
     */
    @TableField(exist = false)
    private String merchantName;

    /**
     * 订单编号
     */
    @TableField(exist = false)
    private String orderNo;
}
