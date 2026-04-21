package io.github.resonxu.seckill.inventory.interfaces.vo;

import lombok.Builder;
import lombok.Value;

/**
 * 库存详情视图对象。
 */
@Value
@Builder
public class InventoryDetailVO {

    Long productId;

    Integer totalStock;

    Integer availableStock;

    Integer lockedStock;
}
