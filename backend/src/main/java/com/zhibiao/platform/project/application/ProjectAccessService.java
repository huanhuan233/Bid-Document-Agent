package com.zhibiao.platform.project.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhibiao.platform.project.domain.BizProjectMember;
import com.zhibiao.platform.project.infrastructure.BizProjectMemberMapper;
import com.zhibiao.platform.shared.exception.BizException;
import com.zhibiao.platform.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 项目数据范围与角色校验。后端检查每一次对象访问。
 */
@Service
@RequiredArgsConstructor
public class ProjectAccessService {

    public static final String OWNER = "OWNER";
    public static final String EDITOR = "EDITOR";
    public static final String VIEWER = "VIEWER";
    private static final Set<String> RANK = Set.of(VIEWER, EDITOR, OWNER);

    private final BizProjectMemberMapper memberMapper;

    /** 校验用户在项目上至少具备 minRole；超级管理员放行 */
    public void assertRole(String projectId, String userId, String minRole) {
        if (SecurityUtils.isSuper()) {
            return;
        }
        String role = roleOf(projectId, userId);
        if (role == null) {
            throw BizException.forbidden("无该项目访问权限");
        }
        if (RANK.contains(minRole) && RANK.contains(role)) {
            List<String> order = List.of(VIEWER, EDITOR, OWNER);
            if (order.indexOf(role) >= order.indexOf(minRole)) {
                return;
            }
        }
        throw BizException.forbidden("项目权限不足，需要 " + minRole);
    }

    public void assertMember(String projectId, String userId) {
        if (SecurityUtils.isSuper()) {
            return;
        }
        if (roleOf(projectId, userId) == null) {
            throw BizException.forbidden("无该项目访问权限");
        }
    }

    public String roleOf(String projectId, String userId) {
        BizProjectMember m = memberMapper.selectOne(new LambdaQueryWrapper<BizProjectMember>()
                .eq(BizProjectMember::getProjectId, projectId)
                .eq(BizProjectMember::getUserId, userId));
        return m == null ? null : m.getRole();
    }

    /** 用户可见项目集合（超级管理员为空表示不限制，由调用方判断） */
    public List<String> visibleProjectIds(String userId) {
        return memberMapper.selectList(new LambdaQueryWrapper<BizProjectMember>()
                        .eq(BizProjectMember::getUserId, userId))
                .stream().map(BizProjectMember::getProjectId).toList();
    }
}
