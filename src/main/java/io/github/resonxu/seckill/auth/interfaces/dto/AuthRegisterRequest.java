package io.github.resonxu.seckill.auth.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "用户注册请求")
public class AuthRegisterRequest {

    @NotBlank(message = "username can not be blank")
    @Size(min = 4, max = 64, message = "username length must be between 4 and 64")
    @Schema(description = "用户名", example = "alice")
    private String username;

    @NotBlank(message = "password can not be blank")
    @Size(min = 6, max = 32, message = "password length must be between 6 and 32")
    @Schema(description = "密码", example = "123456")
    private String password;
}
