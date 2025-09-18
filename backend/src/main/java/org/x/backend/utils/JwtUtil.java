package org.x.backend.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret; // 建议这里的 secret 是 Base64 编码后的安全密钥

    @Value("${jwt.expiration}")
    private int expiration; // 过期时间，单位：秒

    /**
     * 获取用于签名的密钥（处理 Base64 解码）
     * @return 签名密钥
     */
    private Key getSigningKey() {
        // 如果 secret 是 Base64 编码的，先解码
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 JWT Token
     * @param claims 载荷数据（自定义的用户信息等）
     * @return 生成的 JWT Token
     */
    public String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000)) // 转换为毫秒
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证 JWT Token 是否有效
     * @param authToken 要验证的 Token
     * @return 有效返回 true，否则 false
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("无效的 JWT 令牌，Token 格式错误: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("JWT 令牌已过期: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("不支持的 JWT 令牌类型: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT 令牌为空或参数错误: {}", ex.getMessage());
        } catch (SignatureException ex) {
            log.error("JWT 签名不匹配，可能密钥错误或 Token 被篡改: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * 从 JWT Token 中获取指定声明（通用方法）
     * @param token JWT Token
     * @param claimsResolver 声明解析函数
     * @return 解析后的声明值
     * @param <T> 声明值的类型
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从 JWT Token 中获取所有声明
     * @param token JWT Token
     * @return 所有声明
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从 JWT Token 中获取用户名（主题）
     * @param token JWT Token
     * @return 用户名（主题）
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从JWT令牌中获取用户ID
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        // 从自定义声明omdUserId获取
        Object userIdObj = claims.get("omdUserId");
        if (userIdObj == null) {
            log.error("JWT中缺少omdUserId声明");
            throw new IllegalArgumentException("JWT中缺少用户ID");
        }
        return Long.parseLong(userIdObj.toString());
    }

    /**
     * 生成安全的 JWT 密钥（Base64 编码，仅供开发时生成密钥用，实际项目中密钥应配置在 application.yml）
     * 可以直接运行 main 方法生成，然后把输出的密钥配置到配置文件中
     */
    public static void main(String[] args) {
        // 生成 256 位 HMAC-SHA256 密钥
        byte[] key = Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded();
        String base64Key = Base64.getEncoder().encodeToString(key);
        System.out.println("安全 JWT 密钥（Base64 编码）:");
        System.out.println(base64Key);
    }
}