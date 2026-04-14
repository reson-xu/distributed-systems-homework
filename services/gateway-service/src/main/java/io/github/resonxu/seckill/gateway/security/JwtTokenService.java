package io.github.resonxu.seckill.gateway.security;

import io.github.resonxu.seckill.gateway.config.JwtProperties;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 网关 JWT 解析服务。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtProperties jwtProperties;

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

    private SecretKey buildSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
