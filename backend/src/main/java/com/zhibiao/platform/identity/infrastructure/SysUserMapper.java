package com.zhibiao.platform.identity.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhibiao.platform.identity.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /** 条件更新禁用状态（乐观并发，拒绝静默覆盖） */
    @Update("UPDATE sys_user SET status = #{status}, version = version + 1, updated_at = UTC_TIMESTAMP(6) " +
            "WHERE id = #{id} AND deleted = 0")
    int updateStatus(@Param("id") String id, @Param("status") String status);
}
