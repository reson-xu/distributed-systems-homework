package io.github.resonxu.seckill.product.interfaces.controller;

import io.github.resonxu.seckill.common.response.Result;
import io.github.resonxu.seckill.product.application.ProductQueryAppService;
import io.github.resonxu.seckill.product.interfaces.vo.ProductListItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 秒杀商品接口控制器。
 */
@Tag(name = "秒杀商品接口", description = "秒杀商品列表查询")
@RestController
@RequestMapping("/api/v1/seckill/products")
@RequiredArgsConstructor
public class SeckillProductController {

    private final ProductQueryAppService productQueryAppService;

    /**
     * 查询可秒杀商品列表。
     *
     * @param page 页码
     * @param size 页大小
     * @return 可秒杀商品列表
     */
    @Operation(summary = "秒杀商品列表", description = "分页查询当前可参与秒杀的商品列表")
    @GetMapping
    public Result<List<ProductListItemVO>> listSeckillProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return Result.success(productQueryAppService.listSeckillProducts(page, size));
    }
}
