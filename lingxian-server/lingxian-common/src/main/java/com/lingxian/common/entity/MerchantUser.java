package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 商户端用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_merchant_user")
public class MerchantUser extends BaseEntity {

    /**
     * 微信openid
     */
    private String openid;

    /**
     * 微信unionid
     */
    private String unionId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 关联商户ID
     */
    private Long merchantId;

    /**
     * 角色: owner-店主, manager-管理员, staff-员工
     */
    private String role;

    /**
     * 状态: 0-禁用, 1-正常
     */
    private Integer status;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 非数据库字段 - 关联的商户信息
     */
    @TableField(exist = false)
    private Merchant merchant;
}
