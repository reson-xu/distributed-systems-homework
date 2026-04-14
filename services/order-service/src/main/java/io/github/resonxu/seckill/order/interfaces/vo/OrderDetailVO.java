package io.github.resonxu.seckill.order.interfaces.vo;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

/**
 * 订单详情视图对象。
 */
@Value
@Builder
public class OrderDetailVO {

    Long orderId;

    Long userId;

    Long productId;

    String requestId;

    BigDecimal orderAmount;

    Integer orderStatus;
}
