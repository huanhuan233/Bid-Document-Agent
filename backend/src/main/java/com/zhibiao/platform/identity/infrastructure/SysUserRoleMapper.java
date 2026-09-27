package com.zhibiao.platform.identity.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhibiao.platform.identity.domain.SysUserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    @Insert("INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    int insertIgnore(@Param("userId") String userId, @Param("roleId") String roleId);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    int deleteOne(@Param("userId") String userId, @Param("roleId") String roleId);
}
