package com.zhibiao.platform.project.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhibiao.platform.project.domain.BizProjectMember;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BizProjectMemberMapper extends BaseMapper<BizProjectMember> {

    @Update("UPDATE biz_project_member SET role = #{role}, updated_at = UTC_TIMESTAMP(6) " +
            "WHERE project_id = #{projectId} AND user_id = #{userId} AND deleted = 0")
    int updateRole(@Param("projectId") String projectId,
                   @Param("userId") String userId,
                   @Param("role") String role);

    @Delete("DELETE FROM biz_project_member WHERE project_id = #{projectId} AND user_id = #{userId}")
    int removeMember(@Param("projectId") String projectId, @Param("userId") String userId);
}
