package io.github.resonxu.seckill.product.interfaces.vo;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {

    private Long productId;

    private String productName;

    private BigDecimal price;

    private Integer status;

    private Integer availableStock;
}
