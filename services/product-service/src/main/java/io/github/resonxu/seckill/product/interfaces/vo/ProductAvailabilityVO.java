package io.github.resonxu.seckill.product.interfaces.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品可售状态视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAvailabilityVO {

    private Long productId;

    private Integer status;

    private Integer availableStock;

    private Boolean available;
}
