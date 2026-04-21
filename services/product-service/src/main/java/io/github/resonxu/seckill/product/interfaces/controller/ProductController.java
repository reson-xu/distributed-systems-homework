package io.github.resonxu.seckill.product.interfaces.controller;

import io.github.resonxu.seckill.common.response.Result;
import io.github.resonxu.seckill.product.application.ProductDetailQueryAppService;
import io.github.resonxu.seckill.product.application.ProductQueryAppService;
import io.github.resonxu.seckill.product.interfaces.vo.ProductAvailabilityVO;
import io.github.resonxu.seckill.product.interfaces.vo.ProductDetailVO;
import io.github.resonxu.seckill.product.interfaces.vo.ProductListItemVO;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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

    private final ProductDetailQueryAppService productDetailQueryAppService;
    private final ProductQueryAppService productQueryAppService;

    /**
     * 查询商品列表。
     *
     * @param page 页码
     * @param size 页大小
     * @return 商品列表
     */
    @Operation(summary = "商品列表", description = "分页查询在售商品列表")
    @GetMapping
    public Result<List<ProductListItemVO>> listProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return Result.success(productQueryAppService.listProducts(page, size));
    }

    /**
     * 查询商品详情。
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    @Operation(summary = "商品详情", description = "优先从 Redis 读取商品详情缓存，未命中时回源数据库")
    @GetMapping("/{productId}")
    public Result<ProductDetailVO> getDetail(@PathVariable Long productId) {
        return Result.success(productDetailQueryAppService.getProductDetail(productId));
    }

    /**
     * 查询商品可售状态。
     *
     * @param productId 商品ID
     * @return 可售状态
     */
    @Operation(summary = "商品可售状态", description = "返回商品是否可下单以及当前可用库存")
    @GetMapping("/{productId}/availability")
    public Result<ProductAvailabilityVO> getAvailability(@PathVariable Long productId) {
        return Result.success(productQueryAppService.getAvailability(productId));
    }

}
