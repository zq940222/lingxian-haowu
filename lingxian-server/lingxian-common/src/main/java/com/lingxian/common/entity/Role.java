package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 角色实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_role")
public class Role extends BaseEntity {

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 关联的管理员数量（非数据库字段）
     */
    @TableField(exist = false)
    private Integer adminCount;

    /**
     * 权限ID列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Long> permissionIds;
}
