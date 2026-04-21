package io.github.resonxu.seckill.product.interfaces.vo;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品列表项视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListItemVO {

    private Long productId;

    private String productName;

    private BigDecimal price;

    private Integer status;

    private Integer availableStock;
}
