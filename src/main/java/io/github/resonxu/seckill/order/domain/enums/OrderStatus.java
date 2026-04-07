package io.github.resonxu.seckill.order.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 订单状态枚举。
 */
@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    PENDING_CREATE(0, "PENDING_CREATE"),
    CREATED(1, "CREATED"),
    PAID(2, "PAID"),
    CANCELLED(3, "CANCELLED"),
    FAILED(4, "FAILED");

    private final int code;
    private final String description;
}
