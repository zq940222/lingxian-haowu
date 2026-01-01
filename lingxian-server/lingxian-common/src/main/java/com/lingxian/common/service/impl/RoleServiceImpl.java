package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.Role;
import com.lingxian.common.entity.RolePermission;
import com.lingxian.common.mapper.RoleMapper;
import com.lingxian.common.mapper.RolePermissionMapper;
import com.lingxian.common.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色服务实现
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public Role getByCode(String code) {
        return baseMapper.selectByCode(code);
    }

    @Override
    public List<Role> getAllRoles() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getStatus, 1)
                .orderByAsc(Role::getSort);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRolePermissions(Long roleId, List<Long> permissionIds) {
        // 先删除原有权限
        rolePermissionMapper.deleteByRoleId(roleId);

        // 批量插入新权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                rp.setCreateTime(LocalDateTime.now());
                rolePermissionMapper.insert(rp);
            }
        }
    }
}
