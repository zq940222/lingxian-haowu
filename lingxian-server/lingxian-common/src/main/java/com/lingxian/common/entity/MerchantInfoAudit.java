package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商户信息变更审核实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_merchant_info_audit")
public class MerchantInfoAudit extends BaseEntity {

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 变更类型: shop_info-店铺信息变更
     */
    private String changeType;

    /**
     * 变更前的店铺名称
     */
    private String oldShopName;

    /**
     * 变更后的店铺名称
     */
    private String newShopName;

    /**
     * 变更前的Logo
     */
    private String oldLogo;

    /**
     * 变更后的Logo
     */
    private String newLogo;

    /**
     * 变更前的联系电话
     */
    private String oldPhone;

    /**
     * 变更后的联系电话
     */
    private String newPhone;

    /**
     * 变更前的地址
     */
    private String oldAddress;

    /**
     * 变更后的地址
     */
    private String newAddress;

    /**
     * 变更前的经度
     */
    private BigDecimal oldLongitude;

    /**
     * 变更后的经度
     */
    private BigDecimal newLongitude;

    /**
     * 变更前的纬度
     */
    private BigDecimal oldLatitude;

    /**
     * 变更后的纬度
     */
    private BigDecimal newLatitude;

    /**
     * 审核状态: 0-待审核 1-审核通过 2-审核拒绝
     */
    private Integer status;

    /**
     * 审核备注/拒绝原因
     */
    private String auditRemark;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核人ID
     */
    private Long auditBy;
}
