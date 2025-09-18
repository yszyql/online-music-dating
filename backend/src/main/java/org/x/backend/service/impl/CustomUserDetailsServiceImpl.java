package org.x.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.x.backend.mapper.UserDetailsMapper;
import org.x.backend.mapper.OmdRolePermissionMapper;
import org.x.backend.mapper.OmdUserRoleMapper;
import org.x.backend.pojo.OmdUser;
import org.x.backend.pojo.OmdRole;
import org.x.backend.pojo.OmdPermission;
import org.x.backend.service.CustomUserDetailsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {

    private final UserDetailsMapper userDetailsMapper;
    private final OmdUserRoleMapper omdUserRoleMapper;
    private final OmdRolePermissionMapper omdRolePermissionMapper;

    /**
     * 加载用户信息
     * @param id 用户ID
     * @return 用户信息
     * @throws UsernameNotFoundException 用户不存在异常
     */
    @Override
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        Optional<OmdUser> userOptional = userDetailsMapper.findById(id);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("用户不存在，ID: " + id);
        }
        OmdUser user = userOptional.get();
        return buildUserDetails(user);
    }

    /**
     * 加载用户信息
     * @param username 用户名
     * @return 用户信息
     * @throws UsernameNotFoundException 用户不存在异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<OmdUser> userOptional = userDetailsMapper.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("用户不存在，用户名: " + username);
        }
        OmdUser user = userOptional.get();
        return buildUserDetails(user);
    }

    /**
     * 构建UserDetails对象
     * @param user 用户对象
     * @return UserDetails对象
     */
    private UserDetails buildUserDetails(OmdUser user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 获取用户的所有角色
        List<OmdRole> omdRoles = omdUserRoleMapper.findRolesByUserId(user.getOmdUserId());
        if (omdRoles != null) {
            omdRoles.forEach(omdRole -> {
                // 确保角色以ROLE_为前缀
                String roleCode = omdRole.getOmdRoleCode();
                if (!roleCode.startsWith("ROLE_")) {
                    roleCode = "ROLE_" + roleCode;
                }
                authorities.add(new SimpleGrantedAuthority(roleCode));
                log.info("用户[{}]的角色为：{}", user.getOmdUserName(), roleCode);

                // 获取角色对应的权限（建议权限以permission:为前缀）
                List<OmdPermission> omdPermissions = omdRolePermissionMapper.findPermissionsByRoleCode(omdRole.getOmdRoleCode());
                if (omdPermissions != null) {
                    omdPermissions.forEach(perm -> {
                        // 权限建议格式：permission:资源:操作
                        String permissionCode = "permission:" + perm.getOmdPermissionCode();
                        authorities.add(new SimpleGrantedAuthority(permissionCode));
                    });
                }
            });
        }

        return new User(
                user.getOmdUserName(),
                user.getOmdUserPassword(),
                user.isEnabled(),           // 使用实体类的isEnabled()方法
                true,                        // 账户未过期
                true,                        // 密码未过期
                true,                        // 账户未锁定
                authorities                  // 权限集合
        );
    }
}