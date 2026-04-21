package io.github.resonxu.seckill.order.interfaces.vo;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * 订单时间线视图对象。
 */
@Value
@Builder
public class OrderTimelineVO {

    Long orderId;

    Integer currentStatus;

    String currentStatusDesc;

    List<OrderTimelineEventVO> events;
}
