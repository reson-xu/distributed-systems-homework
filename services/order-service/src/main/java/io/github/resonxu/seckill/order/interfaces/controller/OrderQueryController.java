package io.github.resonxu.seckill.order.interfaces.controller;

import io.github.resonxu.seckill.common.response.Result;
import io.github.resonxu.seckill.order.application.OrderQueryAppService;
import io.github.resonxu.seckill.order.interfaces.vo.OrderDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单查询接口控制器。
 */
@Tag(name = "订单查询接口", description = "订单基础查询能力")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderQueryController {

    private final OrderQueryAppService orderQueryAppService;

    /**
     * 根据订单ID查询订单详情。
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    @Operation(summary = "按订单ID查询订单", description = "返回订单的基础信息")
    @GetMapping("/{orderId}")
    public Result<OrderDetailVO> getOrderDetail(@PathVariable Long orderId) {
        return Result.success(orderQueryAppService.getOrderDetail(orderId));
    }
}
