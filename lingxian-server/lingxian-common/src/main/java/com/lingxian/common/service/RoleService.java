package com.lingxian.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lingxian.common.entity.Role;

import java.util.List;

/**
 * 角色Service
 */
public interface RoleService extends IService<Role> {

    /**
     * 根据角色编码查询角色
     */
    Role getByCode(String code);

    /**
     * 获取所有有效角色
     */
    List<Role> getAllRoles();

    /**
     * 更新角色权限
     */
    void updateRolePermissions(Long roleId, List<Long> permissionIds);
}
