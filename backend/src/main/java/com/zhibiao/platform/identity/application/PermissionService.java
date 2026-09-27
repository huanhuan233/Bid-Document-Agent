package com.zhibiao.platform.identity.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhibiao.platform.identity.infrastructure.SysPermissionMapper;
import com.zhibiao.platform.identity.infrastructure.SysRoleMapper;
import com.zhibiao.platform.identity.infrastructure.SysUserMapper;
import com.zhibiao.platform.identity.domain.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * 权限读取（带短期缓存 bid:cache:perms:{userId}，TTL 5 分钟，变更后失效）。
 * 缓存绑定用户，不跨用户复用，不能绕过权限校验。
 */
@Service
@RequiredArgsConstructor
public class PermissionService {

    private static final String CACHE_PREFIX = "bid:cache:perms:";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public record PermCache(boolean superUser, Set<String> roles, Set<String> codes) {
    }

    public PermCache permCacheOf(String userId) {
        String key = CACHE_PREFIX + userId;
        try {
            String cached = redis.opsForValue().get(key);
            if (cached != null) {
                return objectMapper.readValue(cached, PermCache.class);
            }
        } catch (Exception ignored) {
            // 缓存读取失败时回源数据库
        }
        SysUser user = userMapper.selectById(userId);
        boolean superUser = user != null && Boolean.TRUE.equals(user.getIsSuper());
        Set<String> roles = new HashSet<>(roleMapper.findRoleCodesByUserId(userId));
        Set<String> codes = new HashSet<>(permissionMapper.findPermissionCodesByUserId(userId));
        PermCache pc = new PermCache(superUser, roles, codes);
        try {
            redis.opsForValue().set(key, objectMapper.writeValueAsString(pc), TTL);
        } catch (Exception ignored) {
        }
        return pc;
    }

    public Set<String> permissionCodesOf(String userId) {
        return permCacheOf(userId).codes();
    }

    public boolean hasPermission(String userId, String code) {
        PermCache pc = permCacheOf(userId);
        return pc.superUser() || pc.codes().contains(code);
    }

    public void evict(String userId) {
        redis.delete(CACHE_PREFIX + userId);
    }
}
