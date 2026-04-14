package io.github.resonxu.seckill.inventory.infrastructure.persistence;

import io.github.resonxu.seckill.inventory.domain.repository.InventoryRepository;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 基于 MyBatis 的库存仓储实现。
 */
@Mapper
public interface MybatisInventoryRepository extends InventoryRepository {

    @Override
    Integer findAvailableStockByProductId(@Param("productId") Long productId);

    @Override
    int deductAvailableStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
