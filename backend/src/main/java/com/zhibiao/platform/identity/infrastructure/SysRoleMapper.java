package com.zhibiao.platform.identity.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhibiao.platform.identity.domain.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Select("SELECT r.code FROM sys_role r " +
            "JOIN sys_user_role ur ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<String> findRoleCodesByUserId(@Param("userId") String userId);
}
