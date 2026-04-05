package io.github.resonxu.seckill.auth.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户注册请求数据传输对象。
 */
@Data
public class AuthRegisterDTO {

    @NotBlank(message = "username can not be blank")
    private String username;

    @NotBlank(message = "password can not be blank")
    private String password;
}
