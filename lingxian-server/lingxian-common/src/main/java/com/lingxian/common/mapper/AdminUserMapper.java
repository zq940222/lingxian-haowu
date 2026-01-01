package com.lingxian.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingxian.common.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 管理员用户Mapper
 */
@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {

    /**
     * 根据用户名查询管理员
     */
    @Select("SELECT * FROM t_admin_user WHERE username = #{username} AND deleted = 0")
    AdminUser selectByUsername(@Param("username") String username);

    /**
     * 统计指定角色的管理员数量
     */
    @Select("SELECT COUNT(*) FROM t_admin_user WHERE role_id = #{roleId} AND deleted = 0")
    Integer countByRoleId(@Param("roleId") Long roleId);
}
