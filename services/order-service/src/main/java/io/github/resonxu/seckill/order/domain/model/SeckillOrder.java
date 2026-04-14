package io.github.resonxu.seckill.order.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 秒杀订单领域模型。
 */
@Data
public class SeckillOrder {

    private Long id;

    private Long userId;

    private Long productId;

    private String requestId;

    private BigDecimal orderAmount;

    private Integer orderStatus;

    private String failReason;

    private String createSource;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
