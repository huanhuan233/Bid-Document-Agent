package com.zhibiao.platform.identity.infrastructure;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhibiao.platform.identity.domain.SysUser;
import com.zhibiao.platform.identity.infrastructure.SysUserMapper;
import com.zhibiao.platform.shared.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;

/**
 * 用户加载：登录认证与密码校验入口。
 */
@Service
@RequiredArgsConstructor
public class LoginUserDetailsService implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final com.zhibiao.platform.identity.infrastructure.SysRoleMapper roleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return new LoginUser(user.getId(), user.getUsername(), user.getPasswordHash(),
                Boolean.TRUE.equals(user.getIsSuper()), "ENABLED".equals(user.getStatus()),
                new HashSet<>(roleMapper.findRoleCodesByUserId(user.getId())));
    }
}
