package io.github.resonxu.seckill.auth.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "用户登录请求")
public class AuthLoginRequest {

    @NotBlank(message = "username can not be blank")
    @Size(max = 64, message = "username length must be less than or equal to 64")
    @Schema(description = "用户名", example = "alice")
    private String username;

    @NotBlank(message = "password can not be blank")
    @Size(min = 6, max = 32, message = "password length must be between 6 and 32")
    @Schema(description = "密码", example = "123456")
    private String password;
}
