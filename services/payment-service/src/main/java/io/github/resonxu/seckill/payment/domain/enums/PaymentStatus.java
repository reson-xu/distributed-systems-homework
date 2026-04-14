package io.github.resonxu.seckill.payment.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 支付状态枚举。
 */
@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

    PENDING(0, "PENDING"),
    SUCCESS(1, "SUCCESS"),
    FAILED(2, "FAILED");

    private final int code;
    private final String description;
}
