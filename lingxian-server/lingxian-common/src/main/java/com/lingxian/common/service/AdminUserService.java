package com.lingxian.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lingxian.common.entity.AdminUser;

/**
 * 管理员用户Service
 */
public interface AdminUserService extends IService<AdminUser> {

    /**
     * 根据用户名查询管理员
     */
    AdminUser getByUsername(String username);

    /**
     * 统计指定角色的管理员数量
     */
    Integer countByRoleId(Long roleId);

    /**
     * 更新最后登录信息
     */
    void updateLoginInfo(Long id, String ip);
}
