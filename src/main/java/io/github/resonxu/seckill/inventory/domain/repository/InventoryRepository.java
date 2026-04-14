package io.github.resonxu.seckill.inventory.domain.repository;

/**
 * 库存领域仓储。
 */
public interface InventoryRepository {

    /**
     * 查询商品当前可用库存。
     *
     * @param productId 商品ID
     * @return 可用库存，不存在时返回 null
     */
    Integer findAvailableStockByProductId(Long productId);

    /**
     * 条件扣减可用库存。
     *
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @return 影响行数
     */
    int deductAvailableStock(Long productId, Integer quantity);
}
