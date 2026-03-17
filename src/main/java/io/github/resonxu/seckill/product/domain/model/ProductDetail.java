package io.github.resonxu.seckill.product.domain.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductDetail {

    private Long id;

    private String productName;

    private BigDecimal price;

    private Integer status;

    private Integer availableStock;
}
