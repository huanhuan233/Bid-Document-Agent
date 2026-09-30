package com.zhibiao.platform.identity.application;

import com.zhibiao.platform.identity.application.dto.AuthDtos.LoginRequest;
import com.zhibiao.platform.identity.application.dto.AuthDtos.LoginResponse;
import com.zhibiao.platform.identity.application.dto.AuthDtos.UserInfoVO;
import com.zhibiao.platform.identity.domain.SysUser;
import com.zhibiao.platform.identity.infrastructure.SysUserMapper;
import com.zhibiao.platform.shared.exception.BizException;
import com.zhibiao.platform.shared.security.LoginUser;
import com.zhibiao.platform.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.List;

/**
 * 登录/退出/当前用户/修改密码。服务端会话（Spring Session + Redis），浏览器以 HttpOnly Cookie 携带，
 * 同时兼容 Authorization: Bearer 携带同一会话标识（供 Soybean 请求封装）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String LOGIN_LIMIT_PREFIX = "bid:limit:login:";
    private static final int LOGIN_LIMIT = 10;
    private static final Duration LOGIN_LIMIT_TTL = Duration.ofMinutes(10);

    private final AuthenticationManager authenticationManager;
    private final SysUserMapper userMapper;
    private final PermissionService permissionService;
    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;
    private final StringRedisTemplate redis;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest req, String clientIp) {
        if (req == null || isBlank(req.userName()) || isBlank(req.password())) {
            throw BizException.badRequest("用户名与密码不能为空");
        }
        String account = req.userName().trim();
        // 登录限流：bid:limit:login:{account}:{ip}
        String limitKey = LOGIN_LIMIT_PREFIX + account + ":" + (clientIp == null ? "-" : clientIp);
        Long count = redis.opsForValue().increment(limitKey);
        if (count != null && count == 1) {
            redis.expire(limitKey, LOGIN_LIMIT_TTL);
        }
        if (count != null && count > LOGIN_LIMIT) {
            throw new BizException("B1429", "登录尝试过于频繁，请稍后再试", 429);
        }
        return doLogin(account, req.password(), limitKey);
    }

    private LoginResponse doLogin(String account, String password, String limitKey) {
        LoginUser principal;
        try {
            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(account, password));
            principal = (LoginUser) auth.getPrincipal();
        } catch (AuthenticationException e) {
            log.info("login failed for account={}", account);
            throw new BizException("B1401", "用户名或密码错误", 401);
        }
        if (!principal.isEnabled()) {
            throw new BizException("B1403", "账号已被禁用", 403);
        }
        // 建立服务端会话并写入 SecurityContext。
        // 注意：Spring Security 6 的 SecurityContextHolderFilter 不再在请求结束时自动持久化上下文，
        // 手动登录必须显式把上下文写入会话属性，后续请求才能从会话恢复认证状态。
        var attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        var session = attrs.getRequest().getSession(true);
        var context = new SecurityContextImpl(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        session.setAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, principal.getUserId());
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        SecurityContextHolder.setContext(context);
        List<String> roles = List.copyOf(principal.getRoleCodes());
        List<String> permissions = List.copyOf(permissionService.permissionCodesOf(principal.getUserId()));
        redis.delete(limitKey);
        log.info("login success user={} session={}", principal.getUsername(), session.getId());
        SysUser u = userMapper.selectById(principal.getUserId());
        return new LoginResponse(session.getId(), null, principal.getUserId(),
                principal.getUsername(), u == null ? "" : u.getDisplayName(), roles, permissions);
    }

    public void logout() {
        var attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        var session = attrs.getRequest().getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }

    public UserInfoVO currentUser() {
        LoginUser lu = SecurityUtils.current();
        SysUser u = userMapper.selectById(lu.getUserId());
        return new UserInfoVO(lu.getUserId(), lu.getUsername(), u == null ? "" : u.getDisplayName(),
                List.copyOf(lu.getRoleCodes()), List.copyOf(permissionService.permissionCodesOf(lu.getUserId())));
    }

    public void changePassword(String userId, String oldPassword, String newPassword) {
        if (isBlank(newPassword) || newPassword.length() < 8) {
            throw BizException.badRequest("新密码长度至少 8 位");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw BizException.notFound("用户不存在");
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), oldPassword));
        } catch (AuthenticationException e) {
            throw BizException.badRequest("原密码不正确");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        // 修改密码后既有会话全部失效
        invalidateUserSessions(userId);
    }

    public void invalidateUserSessions(String userId) {
        sessionRepository.findByPrincipalName(userId).keySet()
                .forEach(sessionRepository::deleteById);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
