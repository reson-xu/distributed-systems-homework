package io.github.resonxu.seckill.auth.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求数据传输对象。
 */
@Data
public class AuthLoginDTO {

    @NotBlank(message = "username can not be blank")
    private String username;

    @NotBlank(message = "password can not be blank")
    private String password;
}
