package io.github.resonxu.seckill.inventory.domain.repository;

import io.github.resonxu.seckill.inventory.domain.model.InventoryFlow;

/**
 * 库存流水领域仓储。
 */
public interface InventoryFlowRepository {

    /**
     * 新增库存流水。
     *
     * @param inventoryFlow 库存流水
     * @return 影响行数
     */
    int insert(InventoryFlow inventoryFlow);
}
