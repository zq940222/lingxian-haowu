package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.AdminUser;
import com.lingxian.common.mapper.AdminUserMapper;
import com.lingxian.common.service.AdminUserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 管理员用户服务实现
 */
@Service
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser> implements AdminUserService {

    @Override
    public AdminUser getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    @Override
    public Integer countByRoleId(Long roleId) {
        return baseMapper.countByRoleId(roleId);
    }

    @Override
    public void updateLoginInfo(Long id, String ip) {
        AdminUser adminUser = new AdminUser();
        adminUser.setId(id);
        adminUser.setLastLoginTime(LocalDateTime.now());
        adminUser.setLastLoginIp(ip);
        updateById(adminUser);
    }
}
