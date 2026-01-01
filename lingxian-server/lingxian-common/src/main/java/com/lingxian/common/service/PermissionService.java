package com.lingxian.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lingxian.common.entity.Permission;

import java.util.List;

/**
 * 权限Service
 */
public interface PermissionService extends IService<Permission> {

    /**
     * 根据角色ID查询权限列表
     */
    List<Permission> getByRoleId(Long roleId);

    /**
     * 获取权限树
     */
    List<Permission> getPermissionTree();

    /**
     * 根据角色ID获取权限ID列表
     */
    List<Long> getPermissionIdsByRoleId(Long roleId);
}
