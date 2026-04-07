package io.github.resonxu.seckill.order.interfaces.vo;

import lombok.Builder;
import lombok.Value;

/**
 * 秒杀下单受理结果对象。
 */
@Value
@Builder
public class SeckillOrderSubmitVO {

    Long orderId;

    String status;
}
