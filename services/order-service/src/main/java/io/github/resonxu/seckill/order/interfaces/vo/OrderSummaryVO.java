package io.github.resonxu.seckill.order.interfaces.vo;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

/**
 * 订单列表项视图对象。
 */
@Value
@Builder
public class OrderSummaryVO {

    Long orderId;

    Long productId;

    BigDecimal orderAmount;

    Integer orderStatus;

    String orderStatusDesc;

    String failReason;
}
