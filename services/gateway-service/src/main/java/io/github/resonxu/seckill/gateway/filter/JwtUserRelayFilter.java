package io.github.resonxu.seckill.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resonxu.seckill.common.response.Result;
import io.github.resonxu.seckill.common.response.ResultCode;
import io.github.resonxu.seckill.gateway.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关用户透传过滤器。
 */
@Component
@RequiredArgsConstructor
public class JwtUserRelayFilter implements GlobalFilter, Ordered {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final JwtTokenService jwtTokenService;
    private final ObjectMapper objectMapper;

    /**
     * 对需要鉴权的请求解析 JWT 并透传用户ID。
     *
     * @param exchange 请求交换对象
     * @param chain 过滤器链
     * @return 过滤结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (!requiresAuthentication(path)) {
            return chain.filter(exchange);
        }

        String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        Long userId;
        try {
            userId = jwtTokenService.parseUserIdFromAuthorizationHeader(authorizationHeader);
        } catch (IllegalArgumentException exception) {
            return writeUnauthorized(exchange);
        }

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(USER_ID_HEADER, String.valueOf(userId))
                .build();
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    /**
     * 过滤器执行顺序。
     *
     * @return 顺序值
     */
    @Override
    public int getOrder() {
        return -100;
    }

    private boolean requiresAuthentication(String path) {
        return path.startsWith("/api/v1/seckill/orders")
                || path.startsWith("/api/v1/orders")
                || path.startsWith("/api/v1/payments");
    }

    private Mono<Void> writeUnauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = toJsonBytes(Result.fail(ResultCode.UNAUTHORIZED));
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory()
                .wrap(body)));
    }

    private byte[] toJsonBytes(Object body) {
        try {
            return objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException exception) {
            return "{\"code\":\"401\",\"message\":\"unauthorized\",\"data\":null}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}
