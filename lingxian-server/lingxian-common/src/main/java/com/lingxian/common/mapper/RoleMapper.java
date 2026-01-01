package com.lingxian.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingxian.common.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 角色Mapper
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据角色编码查询角色
     */
    @Select("SELECT * FROM t_role WHERE code = #{code} AND deleted = 0")
    Role selectByCode(@Param("code") String code);
}
