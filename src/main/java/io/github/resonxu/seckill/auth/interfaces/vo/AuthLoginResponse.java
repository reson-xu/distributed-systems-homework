package io.github.resonxu.seckill.auth.interfaces.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "用户登录响应")
public class AuthLoginResponse {

    @Schema(description = "用户ID", example = "1")
    Long userId;

    @Schema(description = "用户名", example = "alice")
    String username;

    @Schema(description = "JWT 访问令牌")
    String token;
}
