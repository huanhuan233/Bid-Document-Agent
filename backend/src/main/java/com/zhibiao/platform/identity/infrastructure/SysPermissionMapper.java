package com.zhibiao.platform.identity.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhibiao.platform.identity.domain.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    @Select("SELECT DISTINCT p.code FROM sys_permission p " +
            "JOIN sys_role_permission rp ON rp.permission_id = p.id " +
            "JOIN sys_user_role ur ON ur.role_id = rp.role_id " +
            "WHERE ur.user_id = #{userId} AND p.deleted = 0")
    List<String> findPermissionCodesByUserId(@Param("userId") String userId);
}
