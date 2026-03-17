package io.github.resonxu.seckill.auth.interfaces.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "用户注册响应")
public class AuthRegisterResponse {

    @Schema(description = "用户ID", example = "1")
    Long userId;

    @Schema(description = "用户名", example = "alice")
    String username;
}
