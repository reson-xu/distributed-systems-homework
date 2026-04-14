package io.github.resonxu.seckill.order.infrastructure.client;

import io.github.resonxu.seckill.common.response.Result;
import io.github.resonxu.seckill.order.infrastructure.client.dto.ProductDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 商品服务客户端。
 */
@FeignClient(name = "product-service")
public interface ProductClient {

    /**
     * 查询商品详情。
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    @GetMapping("/api/v1/products/{productId}")
    Result<ProductDetailDTO> getProductDetail(@PathVariable("productId") Long productId);
}
