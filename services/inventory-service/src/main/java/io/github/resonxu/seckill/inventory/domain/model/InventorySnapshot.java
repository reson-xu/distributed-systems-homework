package io.github.resonxu.seckill.inventory.domain.model;

import lombok.Data;

/**
 * 库存快照领域模型。
 */
@Data
public class InventorySnapshot {

    private Long productId;

    private Integer totalStock;

    private Integer availableStock;

    private Integer lockedStock;
}
