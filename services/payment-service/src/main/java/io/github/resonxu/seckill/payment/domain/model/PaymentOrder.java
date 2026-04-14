package io.github.resonxu.seckill.payment.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 支付单领域模型。
 */
@Data
public class PaymentOrder {

    private Long id;

    private Long orderId;

    private Long userId;

    private String requestId;

    private BigDecimal paymentAmount;

    private Integer paymentStatus;

    private String failReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
