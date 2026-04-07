package io.github.resonxu.seckill.order.domain.model;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * MQ 消费幂等记录领域模型。
 */
@Data
public class MqConsumeRecord {

    private Long id;

    private String bizType;

    private String messageKey;

    private Integer consumeStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
