package io.github.resonxu.seckill.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 统一业务结果码定义。
 */
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
    PAYMENT_ORDER_NOT_FOUND("4001", "payment order not found"),
    ORDER_NOT_PAYABLE("4002", "order is not payable"),
    PAYMENT_SUBMIT_FAILED("4003", "failed to submit payment"),
    PAYMENT_USER_MISMATCH("4004", "payment user mismatch"),
    SYSTEM_ERROR("500", "system error");

    private final String code;
    private final String message;
}
