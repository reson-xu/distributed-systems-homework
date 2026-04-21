package io.github.resonxu.seckill.payment.interfaces.controller;

import io.github.resonxu.seckill.common.response.Result;
import io.github.resonxu.seckill.payment.application.PaymentQueryAppService;
import io.github.resonxu.seckill.payment.application.PaymentSubmitAppService;
import io.github.resonxu.seckill.payment.interfaces.dto.PaymentNotifyDTO;
import io.github.resonxu.seckill.payment.interfaces.dto.PaymentSubmitDTO;
import io.github.resonxu.seckill.payment.interfaces.vo.PaymentDetailVO;
import io.github.resonxu.seckill.payment.interfaces.vo.PaymentSubmitVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付接口控制器。
 */
@Tag(name = "支付接口", description = "订单支付与支付结果投递")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final PaymentSubmitAppService paymentSubmitAppService;
    private final PaymentQueryAppService paymentQueryAppService;

    /**
     * 提交支付请求。
     *
     * @param userIdHeader 用户ID请求头
     * @param request 支付请求
     * @return 支付结果
     */
    @Operation(summary = "提交订单支付", description = "校验订单可支付后创建支付单并异步发送支付结果事件")
    @PostMapping
    public Result<PaymentSubmitVO> submit(
            @RequestHeader(USER_ID_HEADER) Long userIdHeader,
            @Valid @RequestBody PaymentSubmitDTO request
    ) {
        return Result.success(paymentSubmitAppService.submit(userIdHeader, request));
    }

    /**
     * 查询支付单详情。
     *
     * @param userIdHeader 用户ID请求头
     * @param paymentId 支付单ID
     * @return 支付单详情
     */
    @Operation(summary = "查询支付单详情", description = "按支付单ID查询当前用户支付单")
    @GetMapping("/{paymentId}")
    public Result<PaymentDetailVO> getPaymentDetail(
            @RequestHeader(USER_ID_HEADER) Long userIdHeader,
            @PathVariable Long paymentId
    ) {
        return Result.success(paymentQueryAppService.getPaymentDetail(userIdHeader, paymentId));
    }

    /**
     * 按订单ID查询支付单。
     *
     * @param userIdHeader 用户ID请求头
     * @param orderId 订单ID
     * @return 支付单详情
     */
    @Operation(summary = "按订单查询支付单", description = "按订单ID查询当前用户支付单")
    @GetMapping("/order/{orderId}")
    public Result<PaymentDetailVO> getPaymentByOrderId(
            @RequestHeader(USER_ID_HEADER) Long userIdHeader,
            @PathVariable Long orderId
    ) {
        return Result.success(paymentQueryAppService.getPaymentByOrderId(userIdHeader, orderId));
    }

    /**
     * 处理支付结果回调。
     *
     * @param request 支付回调请求
     * @return 最新支付状态
     */
    @Operation(summary = "支付结果回调", description = "接收支付渠道结果并更新支付单状态")
    @PostMapping("/notify")
    public Result<PaymentDetailVO> notifyPayment(@Valid @RequestBody PaymentNotifyDTO request) {
        return Result.success(paymentQueryAppService.notifyPayment(request));
    }
}
