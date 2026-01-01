package com.lingxian.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingxian.common.entity.RolePermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色权限关联Mapper
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /**
     * 根据角色ID查询权限ID列表
     */
    @Select("SELECT permission_id FROM t_role_permission WHERE role_id = #{roleId}")
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色ID删除关联
     */
    @Delete("DELETE FROM t_role_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);
}
