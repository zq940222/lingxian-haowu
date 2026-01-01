package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 权限(菜单)实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_permission")
public class Permission extends BaseEntity {

    /**
     * 父级ID，0为顶级
     */
    private Long parentId;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限编码
     */
    private String code;

    /**
     * 类型：1-目录 2-菜单 3-按钮
     */
    private Integer type;

    /**
     * 图标
     */
    private String icon;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 子权限列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Permission> children;
}
