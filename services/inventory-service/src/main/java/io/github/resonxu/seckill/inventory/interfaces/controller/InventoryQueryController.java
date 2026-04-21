package io.github.resonxu.seckill.inventory.interfaces.controller;

import io.github.resonxu.seckill.common.response.Result;
import io.github.resonxu.seckill.inventory.application.InventoryQueryAppService;
import io.github.resonxu.seckill.inventory.interfaces.vo.InventoryDetailVO;
import io.github.resonxu.seckill.inventory.interfaces.vo.InventoryStockVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存查询接口控制器。
 */
@Tag(name = "库存查询接口", description = "库存基础查询能力")
@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public class InventoryQueryController {

    private final InventoryQueryAppService inventoryQueryAppService;

    /**
     * 查询商品库存详情。
     *
     * @param productId 商品ID
     * @return 库存详情
     */
    @Operation(summary = "查询商品库存详情", description = "返回总库存、可用库存和锁定库存")
    @GetMapping("/{productId}")
    public Result<InventoryDetailVO> getInventoryDetail(@PathVariable Long productId) {
        return Result.success(inventoryQueryAppService.getInventoryDetail(productId));
    }

    /**
     * 查询商品当前可用库存。
     *
     * @param productId 商品ID
     * @return 可用库存信息
     */
    @Operation(summary = "查询商品可用库存", description = "返回商品当前可用库存")
    @GetMapping("/{productId}/available-stock")
    public Result<InventoryStockVO> getAvailableStock(@PathVariable Long productId) {
        return Result.success(inventoryQueryAppService.getAvailableStock(productId));
    }
}
