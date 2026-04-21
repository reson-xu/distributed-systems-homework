package io.github.resonxu.seckill.order.interfaces.controller;

import io.github.resonxu.seckill.common.response.Result;
import io.github.resonxu.seckill.order.application.OrderQueryAppService;
import io.github.resonxu.seckill.order.interfaces.vo.OrderDetailVO;
import io.github.resonxu.seckill.order.interfaces.vo.OrderStatusVO;
import io.github.resonxu.seckill.order.interfaces.vo.OrderSummaryVO;
import io.github.resonxu.seckill.order.interfaces.vo.OrderTimelineVO;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
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

    private static final String USER_ID_HEADER = "X-User-Id";

    private final OrderQueryAppService orderQueryAppService;

    /**
     * 查询当前用户订单列表。
     *
     * @param userIdHeader 网关透传的用户ID
     * @param page 页码
     * @param size 页大小
     * @return 订单列表
     */
    @Operation(summary = "当前用户订单列表", description = "分页查询当前登录用户的订单列表")
    @GetMapping
    public Result<List<OrderSummaryVO>> listOrders(
            @RequestHeader(USER_ID_HEADER) Long userIdHeader,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return Result.success(orderQueryAppService.listUserOrders(userIdHeader, page, size));
    }

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

    /**
     * 查询订单状态。
     *
     * @param userIdHeader 网关透传的用户ID
     * @param orderId 订单ID
     * @return 订单状态
     */
    @Operation(summary = "查询订单状态", description = "返回当前用户订单的最新状态")
    @GetMapping("/{orderId}/status")
    public Result<OrderStatusVO> getOrderStatus(
            @RequestHeader(USER_ID_HEADER) Long userIdHeader,
            @PathVariable Long orderId
    ) {
        return Result.success(orderQueryAppService.getOrderStatus(userIdHeader, orderId));
    }

    /**
     * 取消订单。
     *
     * @param userIdHeader 网关透传的用户ID
     * @param orderId 订单ID
     * @return 最新状态
     */
    @Operation(summary = "取消订单", description = "取消当前用户未完成支付的订单")
    @PostMapping("/{orderId}/cancel")
    public Result<OrderStatusVO> cancelOrder(
            @RequestHeader(USER_ID_HEADER) Long userIdHeader,
            @PathVariable Long orderId
    ) {
        return Result.success(orderQueryAppService.cancelOrder(userIdHeader, orderId));
    }

    /**
     * 查询订单时间线。
     *
     * @param userIdHeader 网关透传的用户ID
     * @param orderId 订单ID
     * @return 订单时间线
     */
    @Operation(summary = "查询订单时间线", description = "返回订单的关键状态流转事件")
    @GetMapping("/{orderId}/timeline")
    public Result<OrderTimelineVO> getOrderTimeline(
            @RequestHeader(USER_ID_HEADER) Long userIdHeader,
            @PathVariable Long orderId
    ) {
        return Result.success(orderQueryAppService.getOrderTimeline(userIdHeader, orderId));
    }
}
