package org.x.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.x.backend.filter.JwtAuthenticationFilter;
import org.x.backend.exception.JwtExceptionHandler;
import org.x.backend.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    private final JwtExceptionHandler jwtExceptionHandler;
    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 构造函数注入依赖
     */
    public SecurityConfig(JwtExceptionHandler jwtExceptionHandler,
                          CustomUserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtExceptionHandler = jwtExceptionHandler;
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 配置安全过滤器链
     * 通过定义哪些请求需要认证和授权，以及如何响应安全威胁，可以保护应用程序免受各种攻击。
     * 它还可以用于控制哪些请求可以访问特定的资源，以及如何处理未授权的请求。
     * @param http http安全配置
     * @return 安全过滤器链
     * @throws Exception 安全异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("SecurityFilterChain配置已生效，放行路径: /public/**");
        http
                // 处理未授权请求
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(jwtExceptionHandler)
                )
                // 无状态会话（适用于JWT）
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .csrf(csrf -> csrf.disable())
                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource())
                )
                .authorizeHttpRequests(auth -> auth
                        // 公开访问的URL
                        .requestMatchers("/public/**","/chat-websocket/**").permitAll()
                        // 管理员专属URL
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 歌手专属URL
                        .requestMatchers("/singer/**").hasRole("SINGER")
                        // 普通用户可访问的URL
                        .requestMatchers("/user/**","/music/**","/friend/**","/ai/**").hasAnyRole("USER","SINGER", "ADMIN")
                        // 其他URL需要认证
                        .anyRequest().authenticated()
                );

        // JWT过滤器放在UsernamePassword过滤器之前
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 密码编码器
     * @return 密码编码
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // strength: 4-31，默认10，数值越高安全性越好但性能越低
        return new BCryptPasswordEncoder(12);
    }

    /**
     * 认证管理器
     * 来确定用户是否提供了有效的凭据（如用户名和密码）。
     * 这通常是通过检查数据库或其他存储中的用户凭据来实现的。
     * 认证管理器会返回一个Authentication对象，该对象包含用户的身份信息和权限。
     * 如果认证失败，认证管理器会抛出一个AuthenticationException异常。
     * 认证管理器还可以与其他安全组件（如授权管理器）结合使用，以确保用户具有访问特定资源的权限。
     * @param authConfig 认证配置
     * @return 认证管理器
     * @throws Exception 认证异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * CORS配置
     * @return CORS配置
     * 它允许你控制哪些外部域可以访问你的资源，从而在保持安全性的同时实现跨域通信
     * CORS（跨域资源共享）是一种机制，它允许浏览器和服务器进行跨域通信，以解决跨域资源共享的问题
     * 它通过在服务器端设置响应头来控制哪些域可以访问资源，从而实现跨域通信
     * 它的工作原理是，当浏览器向服务器发送跨域请求时，服务器会检查请求头中的Origin字段，如果Origin字段的值在服务器端被允许，那么服务器会在响应头中添加Access-Control-Allow-Origin字段，告诉浏览器可以访问资源
     * 这样，浏览器就可以安全地进行跨域通信，而不会被浏览器的同源策略所限制
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*"); // 开发环境临时放开
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        // 允许所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}