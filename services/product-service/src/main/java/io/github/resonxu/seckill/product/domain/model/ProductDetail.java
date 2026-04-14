package io.github.resonxu.seckill.product.domain.model;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 商品详情领域模型。
 */
@Data
public class ProductDetail {

    private Long id;

    private String productName;

    private BigDecimal price;

    private Integer status;

    private Integer availableStock;
}
