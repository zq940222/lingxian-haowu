package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 管理员用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_admin_user")
public class AdminUser extends BaseEntity {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码(加密)
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 角色名称（非数据库字段）
     */
    @TableField(exist = false)
    private String roleName;
}
