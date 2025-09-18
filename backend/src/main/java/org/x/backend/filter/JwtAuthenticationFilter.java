package org.x.backend.filter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.x.backend.service.CustomUserDetailsService;
import org.x.backend.utils.JwtUtil;
import org.x.backend.utils.RedisUtil;
import org.x.backend.utils.ThreadLocalUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT认证过滤器
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.token-header}")
    private String tokenHeader;
    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    private final JwtUtil tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final RedisUtil redisUtil;

    public JwtAuthenticationFilter(JwtUtil tokenProvider,
                                   CustomUserDetailsService userDetailsService,
                                   RedisUtil redisUtil) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.redisUtil = redisUtil;
    }


    /**
     * 每次请求都会执行的过滤器方法，用于进行JWT认证
     * @param request HTTP请求对象，包含客户端请求的信息
     * @param response HTTP响应对象，用于向客户端返回响应信息
     * @param filterChain 过滤器链，用于调用后续的过滤器
     * @throws ServletException Servlet异常，当Servlet处理请求出错时抛出
     * @throws IOException IO异常，当输入输出操作出错时抛出
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        // 1. 放行公共接口（/public/ 开头）
        if (requestURI.startsWith("/public/")) {
            log.info("放行公共接口: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        if (requestURI.contains("/chat-websocket")) {
            log.info("开始处理 WebSocket 握手请求: {}", requestURI);
            try {
                // 原有的过滤逻辑...
                log.info("WebSocket 握手请求过滤完成，继续执行过滤器链");
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                log.error("处理 WebSocket 握手请求时发生异常", e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }finally {
                // WebSocket握手完成后立即清除ThreadLocal
                ThreadLocalUtil.remove();
            }
            return;
        }
        try {
            // 从请求头中获取JWT令牌
            String jwt = getJwtFromRequest(request);
            // 记录请求路径和获取到的JWT令牌信息
            log.info("请求路径: {}, Token: {}", request.getRequestURI(), jwt);
            // 检查JWT令牌是否存在且不为空
            if (StringUtils.hasText(jwt)) {
                // 验证JWT令牌的有效性
                if (tokenProvider.validateToken(jwt)) {
                    // 从JWT令牌中解析出用户ID
                    Long userId = tokenProvider.getUserIdFromJWT(jwt);
                    // 记录解析出的用户ID信息
                    log.info("解析出的用户ID: {}", userId);
                    // Redis 校验
                    // 生成Redis中存储用户Token的键
                    String redisKey = "user:token:" + userId;
                    // 从Redis中获取该用户对应的Token
                    String redisToken = redisUtil.get(redisKey);
                    // 检查Redis中的Token是否存在或与请求中的JWT是否匹配
                    if (redisToken == null || !redisToken.equals(jwt)) {
                        // 若不匹配，记录警告日志并返回401未授权状态码
                        log.info("Redis中Token:{}, Token:{}", redisToken, jwt);
                        log.warn("Redis中Token不匹配，用户ID: {}", userId);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                    // 加载用户信息，设置到 SecurityContextHolder
                    // 根据用户ID从数据库加载用户详细信息
                    UserDetails userDetails = userDetailsService.loadUserById(userId);
                    // 构建用户信息Map，用于存储用户的各种信息
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("omdUserId", userId);
                    userInfo.put("omdUserName", userDetails.getUsername());
                    userInfo.put("authorities", userDetails.getAuthorities());
                    // 创建一个UsernamePasswordAuthenticationToken对象，用于表示用户的认证信息
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    // 设置认证信息的详细信息，包含请求的相关信息
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // 将认证信息设置到SecurityContextHolder中，以便后续的安全检查使用
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    // 记录认证成功日志，包含用户名和用户权限信息
                    log.info("认证成功，用户： {}，权限：{}", userDetails.getUsername(),userDetails.getAuthorities());
                    // 3. 如果需要，也可以存到 ThreadLocal（根据你的业务需求）
                    // 将用户信息存储到ThreadLocal中，方便在当前线程中共享使用
                    ThreadLocalUtil.set(userInfo);
                } else {
                    // 若JWT令牌无效，记录警告日志并返回401未授权状态码
                    log.warn("无效的JWT令牌：{}",jwt);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }

            // 调用过滤器链中的下一个过滤器
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException ex) {
            // 处理JWT令牌过期异常，返回401未授权状态码
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (Exception ex) {
            // 处理其他异常，记录错误日志并返回500内部服务器错误状态码
            log.error("JWT认证失败", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            // 在finally块中清除ThreadLocal
            ThreadLocalUtil.remove();
            log.info("ThreadLocal已清除");
        }
    }

    /**
     * 从请求头中获取JWT令牌
     * @param request HTTP请求
     * @return JWT令牌
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(tokenHeader);
        if (StringUtils.isEmpty(bearerToken)) {
            return null;
        }

        // 1. 清理首尾空格，避免前端传递多余空格
        String cleanedToken = StringUtils.trimWhitespace(bearerToken);

        // 2. 校验前缀（tokenPrefix 建议配置为 "Bearer "，这里兼容 trim 后匹配）
        String prefix = StringUtils.trimWhitespace(tokenPrefix);
        if (cleanedToken.startsWith(prefix)) {
            // 3. 截取 Token（再次清理，确保无残留空格）
            String token = cleanedToken.substring(prefix.length()).trim();
            log.info("JWT过滤器 请求头中获取到的Token: {}", token);
            return token;
        }

        log.warn("JWT过滤器 Token 前缀不匹配，原始 Token: {}", bearerToken);
        return null;
    }
}