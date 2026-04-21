package io.github.resonxu.seckill.order.interfaces.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

/**
 * 订单时间线事件视图对象。
 */
@Value
@Builder
public class OrderTimelineEventVO {

    String eventCode;

    String eventName;

    LocalDateTime eventTime;

    String message;
}
