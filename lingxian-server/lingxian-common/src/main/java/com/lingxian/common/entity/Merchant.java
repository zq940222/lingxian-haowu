package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_merchant")
public class Merchant extends BaseEntity {

    private String name;
    private String logo;
    private String banner;
    private String description;
    private String contactName;
    private String contactPhone;
    private String category;
    private String province;
    private String city;
    private String district;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String businessLicense;
    private String idCardFront;
    private String idCardBack;
    private String licenseImage;
    private Integer status;
    private Integer verifyStatus;
    private LocalDateTime verifyTime;
    private String verifyRemark;
    private BigDecimal rating;
    private BigDecimal commissionRate;
    private BigDecimal balance;
    private BigDecimal frozenBalance;

    // 非数据库字段
    @TableField(exist = false)
    private Integer productCount;
    @TableField(exist = false)
    private Integer orderCount;
    @TableField(exist = false)
    private BigDecimal totalSales;
}
