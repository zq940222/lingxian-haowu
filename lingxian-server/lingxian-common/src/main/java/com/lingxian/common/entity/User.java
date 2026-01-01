package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class User extends BaseEntity {

    private String openid;
    private String unionId;
    private String nickname;
    private String avatar;
    private String phone;
    private Integer gender;
    private LocalDate birthday;
    private Integer points;
    private BigDecimal balance;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;

    // 非数据库字段
    @TableField(exist = false)
    private Integer orderCount;
    @TableField(exist = false)
    private BigDecimal totalAmount;
}
