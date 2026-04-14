package io.github.resonxu.seckill.order.infrastructure.mq.message;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 支付结果事件。
 */
@Data
public class PaymentResultMessage {

    private Long paymentId;

    private Long orderId;

    private Long userId;

    private BigDecimal paymentAmount;

    private Boolean success;

    private String failReason;
}
