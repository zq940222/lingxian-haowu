package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户收货地址实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_address")
public class UserAddress extends BaseEntity {

    private Long userId;
    private String name;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private Integer isDefault;
}
