package io.github.resonxu.seckill.inventory.interfaces.vo;

import lombok.Builder;
import lombok.Value;

/**
 * 商品库存视图对象。
 */
@Value
@Builder
public class InventoryStockVO {

    Long productId;

    Integer availableStock;
}
