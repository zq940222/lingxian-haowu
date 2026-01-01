package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 轮播图实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_banner")
public class Banner extends BaseEntity {

    /**
     * 标题
     */
    private String title;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 跳转类型 1-商品 2-商户 3-活动 4-外链
     */
    private Integer linkType;

    /**
     * 跳转目标ID
     */
    private Long linkId;

    /**
     * 跳转URL
     */
    private String linkUrl;

    /**
     * 位置 1-首页 2-分类页 3-个人中心
     */
    private Integer position;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;
}
