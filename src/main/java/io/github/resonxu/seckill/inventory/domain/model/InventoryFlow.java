package io.github.resonxu.seckill.inventory.domain.model;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 库存流水领域模型。
 */
@Data
public class InventoryFlow {

    private Long id;

    private Long bizId;

    private Long productId;

    private String flowType;

    private Integer changeCount;

    private Integer beforeAvailableStock;

    private Integer afterAvailableStock;

    private String sourceEvent;

    private LocalDateTime createdAt;
}
