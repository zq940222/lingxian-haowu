package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.Permission;
import com.lingxian.common.mapper.PermissionMapper;
import com.lingxian.common.mapper.RolePermissionMapper;
import com.lingxian.common.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限服务实现
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public List<Permission> getByRoleId(Long roleId) {
        return baseMapper.selectByRoleId(roleId);
    }

    @Override
    public List<Permission> getPermissionTree() {
        // 获取所有权限
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getStatus, 1)
                .orderByAsc(Permission::getSort);
        List<Permission> allPermissions = list(wrapper);

        // 构建树形结构
        return buildTree(allPermissions, 0L);
    }

    @Override
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        return rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    }

    /**
     * 构建权限树
     */
    private List<Permission> buildTree(List<Permission> allPermissions, Long parentId) {
        List<Permission> tree = new ArrayList<>();

        // 按父级ID分组
        Map<Long, List<Permission>> groupedByParent = allPermissions.stream()
                .collect(Collectors.groupingBy(Permission::getParentId));

        // 获取当前层级的权限
        List<Permission> currentLevel = groupedByParent.getOrDefault(parentId, new ArrayList<>());

        for (Permission permission : currentLevel) {
            // 递归获取子权限
            List<Permission> children = buildTree(allPermissions, permission.getId());
            if (!children.isEmpty()) {
                permission.setChildren(children);
            }
            tree.add(permission);
        }

        return tree;
    }
}
