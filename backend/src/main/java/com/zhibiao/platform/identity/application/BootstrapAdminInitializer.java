package com.zhibiao.platform.identity.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhibiao.platform.identity.domain.SysRole;
import com.zhibiao.platform.identity.domain.SysUser;
import com.zhibiao.platform.identity.domain.SysUserRole;
import com.zhibiao.platform.identity.infrastructure.SysRoleMapper;
import com.zhibiao.platform.identity.infrastructure.SysUserMapper;
import com.zhibiao.platform.identity.infrastructure.SysUserRoleMapper;
import com.zhibiao.platform.shared.util.Ids;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 启动期创建初始管理员：凭证仅由环境注入（ZB_BOOTSTRAP_ADMIN_USERNAME / ZB_BOOTSTRAP_ADMIN_PASSWORD），
 * 不存在硬编码默认密码；用户已存在或未配置时跳过。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BootstrapAdminInitializer implements ApplicationRunner {

    private static final String SUPER_ROLE_CODE = "R_SUPER";

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${zhibiao.security.bootstrap-admin-username:}")
    private String bootstrapUsername;

    @Value("${zhibiao.security.bootstrap-admin-password:}")
    private String bootstrapPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (isBlank(bootstrapUsername) && isBlank(bootstrapPassword)) {
            log.warn("未配置初始管理员（ZB_BOOTSTRAP_ADMIN_USERNAME/ZB_BOOTSTRAP_ADMIN_PASSWORD），跳过创建；"
                    + "首次使用请先通过环境变量提供管理员凭证");
            return;
        }
        if (isBlank(bootstrapUsername) || isBlank(bootstrapPassword)) {
            throw new IllegalStateException("初始管理员配置不完整：ZB_BOOTSTRAP_ADMIN_USERNAME 与 "
                    + "ZB_BOOTSTRAP_ADMIN_PASSWORD 必须同时提供");
        }
        String username = bootstrapUsername.trim();
        if (bootstrapPassword.length() < 8) {
            throw new IllegalStateException("初始管理员密码长度不得少于 8 位");
        }
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (exists != null && exists > 0) {
            log.info("初始管理员已存在（username={}），跳过创建", username);
            return;
        }
        SysRole superRole = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, SUPER_ROLE_CODE));
        if (superRole == null) {
            throw new IllegalStateException("角色 " + SUPER_ROLE_CODE + " 不存在，请确认 Flyway 迁移已执行");
        }

        SysUser user = new SysUser();
        user.setId(Ids.uuid());
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(bootstrapPassword));
        user.setDisplayName("系统管理员");
        user.setStatus("ENABLED");
        user.setIsSuper(Boolean.TRUE);
        user.setDifyUserId("dify-" + user.getId());
        user.setVersion(0);
        userMapper.insert(user);
        userRoleMapper.insertIgnore(user.getId(), superRole.getId());
        log.info("已创建初始管理员 username={}（凭证来自环境变量，不再打印）", username);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}