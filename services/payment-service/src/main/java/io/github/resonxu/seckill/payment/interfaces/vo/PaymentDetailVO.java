package io.github.resonxu.seckill.payment.interfaces.vo;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

/**
 * 支付单详情视图对象。
 */
@Value
@Builder
public class PaymentDetailVO {

    Long paymentId;

    Long orderId;

    Long userId;

    String requestId;

    BigDecimal paymentAmount;

    Integer paymentStatus;

    String paymentStatusDesc;

    String failReason;
}
