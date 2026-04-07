package io.github.resonxu.seckill.order.infrastructure.mq.message;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

/**
 * 秒杀下单消息体。
 */
@Value
@Builder
public class SeckillOrderCreateMessage {

    Long orderId;

    Long userId;

    Long productId;

    String requestId;

    BigDecimal orderAmount;
}
