package com.lingxian.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingxian.common.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据角色ID查询权限列表
     */
    @Select("SELECT p.* FROM t_permission p " +
            "INNER JOIN t_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.deleted = 0 " +
            "ORDER BY p.sort")
    List<Permission> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据父级ID查询权限列表
     */
    @Select("SELECT * FROM t_permission WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort")
    List<Permission> selectByParentId(@Param("parentId") Long parentId);
}
