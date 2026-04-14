package io.github.resonxu.seckill.payment.infrastructure.client.dto;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 订单详情 DTO。
 */
@Data
public class OrderDetailDTO {

    private Long orderId;

    private Long userId;

    private Long productId;

    private String requestId;

    private BigDecimal orderAmount;

    private Integer orderStatus;
}
