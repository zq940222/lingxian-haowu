package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商户配送小区关联实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_merchant_community")
public class MerchantCommunity extends BaseEntity {

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 小区ID
     */
    private Long communityId;

    /**
     * 是否开放配送 0-关闭 1-开放
     */
    private Integer enabled;

    /**
     * 排序
     */
    private Integer sort;

    // 非数据库字段
    @TableField(exist = false)
    private String communityName;

    @TableField(exist = false)
    private String communityAddress;
}
