package com.zhibiao.platform.shared.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Set;

/**
 * 登录用户主体（写入 SecurityContext 与 Redis 会话）。
 */
@Getter
public class LoginUser implements UserDetails {
    private final String userId;
    private final String username;
    private final String passwordHash;
    private final boolean superUser;
    private final boolean enabled;
    private final Set<String> roleCodes;

    public LoginUser(String userId, String username, String passwordHash, boolean superUser,
                     boolean enabled, Set<String> roleCodes) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.superUser = superUser;
        this.enabled = enabled;
        this.roleCodes = roleCodes;
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return roleCodes.stream().map(SimpleGrantedAuthority::new).map(a -> (GrantedAuthority) a).toList();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
