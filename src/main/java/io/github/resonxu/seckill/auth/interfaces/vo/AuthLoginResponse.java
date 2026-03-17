package io.github.resonxu.seckill.auth.interfaces.vo;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthLoginResponse {

    Long userId;

    String username;

    String token;
}
