package io.github.resonxu.seckill.order.infrastructure.client.dto;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 商品详情传输对象。
 */
@Data
public class ProductDetailDTO {

    private Long productId;

    private String productName;

    private BigDecimal price;

    private Integer status;

    private Integer availableStock;
}
