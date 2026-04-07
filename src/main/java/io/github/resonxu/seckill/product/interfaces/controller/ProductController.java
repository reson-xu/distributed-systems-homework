package io.github.resonxu.seckill.product.interfaces.controller;

import io.github.resonxu.seckill.common.response.Result;
import io.github.resonxu.seckill.product.application.ProductCacheService;
import io.github.resonxu.seckill.product.interfaces.vo.ProductDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品接口控制器。
 */
@Tag(name = "商品接口", description = "商品详情查询")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductCacheService productCacheService;

    /**
     * 查询商品详情
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    @Operation(summary = "商品详情", description = "优先从 Redis 读取商品详情缓存，未命中时回源数据库")
    @GetMapping("/{productId}")
    public Result<ProductDetailVO> getDetail(@PathVariable Long productId) {
        return Result.success(productCacheService.getProductDetail(productId));
    }
}
