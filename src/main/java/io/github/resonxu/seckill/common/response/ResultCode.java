package io.github.resonxu.seckill.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResultCode {

    SUCCESS("0", "success"),
    UNAUTHORIZED("401", "unauthorized"),
    BAD_REQUEST("400", "bad request"),
    USERNAME_ALREADY_EXISTS("1001", "username already exists"),
    USER_NOT_FOUND("1002", "user not found"),
    INVALID_CREDENTIALS("1003", "invalid credentials"),
    USER_DISABLED("1004", "user is disabled"),
    PRODUCT_NOT_FOUND("2001", "product not found"),
    SECKILL_REPEAT_ORDER("3001", "repeat seckill order"),
    SECKILL_OUT_OF_STOCK("3002", "stock not enough"),
    SECKILL_PRODUCT_NOT_AVAILABLE("3003", "product not available for seckill"),
    DUPLICATE_REQUEST("3004", "duplicate request"),
    SECKILL_SUBMIT_FAILED("3005", "failed to submit seckill order"),
    SYSTEM_ERROR("500", "system error");

    private final String code;
    private final String message;
}
