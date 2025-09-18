package org.x.backend.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

// 扩展标准接口
public interface CustomUserDetailsService extends UserDetailsService {
    UserDetails loadUserById(Long id) throws UsernameNotFoundException;
}