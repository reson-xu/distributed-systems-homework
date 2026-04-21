package io.github.resonxu.seckill.order.interfaces.vo;

import lombok.Builder;
import lombok.Value;

/**
 * 订单状态视图对象。
 */
@Value
@Builder
public class OrderStatusVO {

    Long orderId;

    Integer orderStatus;

    String orderStatusDesc;

    String failReason;
}
