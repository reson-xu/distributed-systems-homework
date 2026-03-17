package io.github.resonxu.seckill.auth.interfaces.vo;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthRegisterResponse {

    Long userId;

    String username;
}
