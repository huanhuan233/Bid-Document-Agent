package com.zhibiao.platform.identity.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhibiao.platform.identity.application.dto.AuthDtos.CreateUserRequest;
import com.zhibiao.platform.identity.application.dto.AuthDtos.UpdateUserRequest;
import com.zhibiao.platform.identity.application.dto.AuthDtos.UserVO;
import com.zhibiao.platform.identity.domain.SysRole;
import com.zhibiao.platform.identity.domain.SysUser;
import com.zhibiao.platform.identity.domain.SysUserRole;
import com.zhibiao.platform.identity.infrastructure.SysRoleMapper;
import com.zhibiao.platform.identity.infrastructure.SysUserMapper;
import com.zhibiao.platform.identity.infrastructure.SysUserRoleMapper;
import com.zhibiao.platform.shared.exception.BizException;
import com.zhibiao.platform.shared.util.Ids;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 管理员用户管理：创建、重置密码、禁用/启用、角色分配。
 */
@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PermissionService permissionService;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    public Page<UserVO> page(long page, long pageSize, String keyword) {
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(SysUser::getUsername, keyword).or().like(SysUser::getDisplayName, keyword));
        }
        qw.orderByDesc(SysUser::getCreatedAt);
        Page<SysUser> p = userMapper.selectPage(new Page<>(page, pageSize), qw);
        List<UserVO> items = p.getRecords().stream().map(this::toVO).toList();
        Page<UserVO> result = new Page<>(page, pageSize, p.getTotal());
        result.setRecords(items);
        return result;
    }

    @Transactional
    public String create(CreateUserRequest req) {
        if (!StringUtils.hasText(req.username()) || !StringUtils.hasText(req.password())
                || req.password().length() < 8) {
            throw BizException.badRequest("用户名必填，密码至少 8 位");
        }
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, req.username()));
        if (exists > 0) {
            throw BizException.badRequest("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setId(Ids.uuid());
        user.setUsername(req.username().trim());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setDisplayName(StringUtils.hasText(req.displayName()) ? req.displayName() : req.username());
        user.setEmail(req.email());
        user.setPhone(req.phone());
        user.setStatus("ENABLED");
        user.setIsSuper(false);
        user.setDifyUserId("u-" + Ids.uuid());
        user.setVersion(0);
        userMapper.insert(user);
        assignRoles(user.getId(), req.roleCodes());
        return user.getId();
    }

    public void update(String userId, UpdateUserRequest req) {
        SysUser user = mustFind(userId);
        if (StringUtils.hasText(req.displayName())) {
            user.setDisplayName(req.displayName());
        }
        user.setEmail(req.email());
        user.setPhone(req.phone());
        userMapper.updateById(user);
    }

    @Transactional
    public void resetPassword(String userId, String newPassword) {
        if (!StringUtils.hasText(newPassword) || newPassword.length() < 8) {
            throw BizException.badRequest("新密码至少 8 位");
        }
        SysUser user = mustFind(userId);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        authService.invalidateUserSessions(userId);
    }

    /** 禁用/启用：条件更新；禁用后既有会话失效 */
    @Transactional
    public void changeStatus(String userId, String status) {
        if (!"ENABLED".equals(status) && !"DISABLED".equals(status)) {
            throw BizException.badRequest("非法状态");
        }
        SysUser user = mustFind(userId);
        if (Boolean.TRUE.equals(user.getIsSuper()) && "DISABLED".equals(status)) {
            throw BizException.badRequest("不能禁用超级管理员");
        }
        int updated = userMapper.updateStatus(userId, status);
        if (updated == 0) {
            throw BizException.conflict("状态更新冲突，请刷新后重试");
        }
        permissionService.evict(userId);
        if ("DISABLED".equals(status)) {
            authService.invalidateUserSessions(userId);
        }
    }

    @Transactional
    public void assignRoles(String userId, List<String> roleCodes) {
        mustFind(userId);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleCodes != null) {
            for (String code : roleCodes) {
                SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, code));
                if (role == null) {
                    throw BizException.badRequest("角色不存在: " + code);
                }
                userRoleMapper.insertIgnore(userId, role.getId());
            }
        }
        permissionService.evict(userId);
    }

    private SysUser mustFind(String id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw BizException.notFound("用户不存在");
        }
        return user;
    }

    private UserVO toVO(SysUser u) {
        List<String> roles = roleMapper.findRoleCodesByUserId(u.getId());
        return new UserVO(u.getId(), u.getUsername(), u.getDisplayName(), u.getEmail(), u.getPhone(),
                u.getStatus(), Boolean.TRUE.equals(u.getIsSuper()), roles,
                u.getCreatedAt() == null ? null : u.getCreatedAt().toString());
    }
}
