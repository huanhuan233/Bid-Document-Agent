package com.zhibiao.platform.project.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhibiao.platform.project.domain.BizProject;
import com.zhibiao.platform.project.domain.BizProjectMember;
import com.zhibiao.platform.project.infrastructure.BizProjectMapper;
import com.zhibiao.platform.project.infrastructure.BizProjectMemberMapper;
import com.zhibiao.platform.shared.exception.BizException;
import com.zhibiao.platform.shared.security.SecurityUtils;
import com.zhibiao.platform.shared.util.Ids;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 项目与成员管理。
 */
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final BizProjectMapper projectMapper;
    private final BizProjectMemberMapper memberMapper;
    private final ProjectAccessService accessService;

    @Transactional
    public BizProject create(String name, String code, String description) {
        if (!StringUtils.hasText(name)) {
            throw BizException.badRequest("项目名称必填");
        }
        String finalCode = StringUtils.hasText(code) ? code : "P" + Ids.shortId().toUpperCase();
        Long exists = projectMapper.selectCount(new LambdaQueryWrapper<BizProject>().eq(BizProject::getCode, finalCode));
        if (exists > 0) {
            throw BizException.badRequest("项目编码已存在");
        }
        String userId = SecurityUtils.currentUserId();
        BizProject project = new BizProject();
        project.setName(name.trim());
        project.setCode(finalCode);
        project.setDescription(description);
        project.setStatus("ACTIVE");
        project.setOwnerUserId(userId);
        project.setVersion(0);
        projectMapper.insert(project);

        BizProjectMember member = new BizProjectMember();
        member.setProjectId(project.getId());
        member.setUserId(userId);
        member.setRole(ProjectAccessService.OWNER);
        memberMapper.insert(member);
        return project;
    }

    public BizProject get(String projectId) {
        BizProject p = projectMapper.selectById(projectId);
        if (p == null) {
            throw BizException.notFound("项目不存在");
        }
        return p;
    }

    public Page<BizProject> page(long page, long pageSize, String userId) {
        LambdaQueryWrapper<BizProject> qw = new LambdaQueryWrapper<>();
        if (!SecurityUtils.isSuper()) {
            List<String> ids = accessService.visibleProjectIds(userId);
            if (ids.isEmpty()) {
                return new Page<>(page, pageSize);
            }
            qw.in(BizProject::getId, ids);
        }
        qw.orderByDesc(BizProject::getCreatedAt);
        return projectMapper.selectPage(new Page<>(page, pageSize), qw);
    }

    @Transactional
    public void archive(String projectId) {
        accessService.assertRole(projectId, SecurityUtils.currentUserId(), ProjectAccessService.OWNER);
        BizProject p = get(projectId);
        p.setStatus("ARCHIVED");
        int rows = projectMapper.updateById(p);
        if (rows == 0) {
            throw BizException.conflict("项目状态更新冲突");
        }
    }

    @Transactional
    public void addMember(String projectId, String targetUserId, String role) {
        accessService.assertRole(projectId, SecurityUtils.currentUserId(), ProjectAccessService.OWNER);
        if (!List.of(ProjectAccessService.OWNER, ProjectAccessService.EDITOR, ProjectAccessService.VIEWER).contains(role)) {
            throw BizException.badRequest("非法项目角色");
        }
        Long exists = memberMapper.selectCount(new LambdaQueryWrapper<BizProjectMember>()
                .eq(BizProjectMember::getProjectId, projectId)
                .eq(BizProjectMember::getUserId, targetUserId));
        if (exists > 0) {
            memberMapper.updateRole(projectId, targetUserId, role);
            return;
        }
        BizProjectMember m = new BizProjectMember();
        m.setProjectId(projectId);
        m.setUserId(targetUserId);
        m.setRole(role);
        memberMapper.insert(m);
    }

    public Map<String, String> memberRoles(String projectId) {
        accessService.assertMember(projectId, SecurityUtils.currentUserId());
        return memberMapper.selectList(new LambdaQueryWrapper<BizProjectMember>()
                        .eq(BizProjectMember::getProjectId, projectId)).stream()
                .collect(Collectors.toMap(BizProjectMember::getUserId, BizProjectMember::getRole, (a, b) -> a));
    }

    @Transactional
    public void removeMember(String projectId, String targetUserId) {
        accessService.assertRole(projectId, SecurityUtils.currentUserId(), ProjectAccessService.OWNER);
        memberMapper.removeMember(projectId, targetUserId);
    }
}
