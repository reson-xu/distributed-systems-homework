package io.github.resonxu.seckill.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResultCode {

    SUCCESS("0", "success"),
    BAD_REQUEST("400", "bad request"),
    USERNAME_ALREADY_EXISTS("1001", "username already exists"),
    USER_NOT_FOUND("1002", "user not found"),
    INVALID_CREDENTIALS("1003", "invalid credentials"),
    USER_DISABLED("1004", "user is disabled"),
    SYSTEM_ERROR("500", "system error");

    private final String code;
    private final String message;
}
