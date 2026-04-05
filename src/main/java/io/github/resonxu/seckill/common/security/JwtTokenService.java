package io.github.resonxu.seckill.common.security;

import io.github.resonxu.seckill.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtProperties jwtProperties;

    /**
     * 生成 JWT 令牌
     *
     * @param userId 用户ID
     * @param username 用户名
     * @return JWT 令牌
     */
    public String generateToken(Long userId, String username) {
        Instant now = Instant.now();
        Instant expirationAt = now.plusSeconds(jwtProperties.getExpiration());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expirationAt))
                .signWith(buildSigningKey())
                .compact();
    }

    /**
     * 构建 JWT 签名密钥
     *
     * @return 签名密钥
     */
    private SecretKey buildSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 从 Authorization 请求头中解析用户ID。
     *
     * @param authorizationHeader Authorization 请求头
     * @return 用户ID
     */
    public Long parseUserIdFromAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("invalid authorization header");
        }
        return parseUserId(authorizationHeader.substring("Bearer ".length()).trim());
    }

    /**
     * 从 JWT 令牌中解析用户ID。
     *
     * @param token JWT 令牌
     * @return 用户ID
     */
    public Long parseUserId(String token) {
        try {
            String subject = Jwts.parser()
                    .verifyWith(buildSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Long.valueOf(subject);
        } catch (JwtException | IllegalArgumentException exception) {
            log.warn("failed to parse jwt token", exception);
            throw new IllegalArgumentException("invalid jwt token", exception);
        }
    }
}
