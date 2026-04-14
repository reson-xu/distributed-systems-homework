package io.github.resonxu.seckill.inventory.infrastructure.persistence;

import io.github.resonxu.seckill.inventory.domain.model.InventoryFlow;
import io.github.resonxu.seckill.inventory.domain.repository.InventoryFlowRepository;
import org.apache.ibatis.annotations.Mapper;

/**
 * 基于 MyBatis 的库存流水仓储实现。
 */
@Mapper
public interface MybatisInventoryFlowRepository extends InventoryFlowRepository {

    @Override
    int insert(InventoryFlow inventoryFlow);
}
