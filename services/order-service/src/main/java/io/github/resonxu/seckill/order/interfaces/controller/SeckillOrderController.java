package io.github.resonxu.seckill.order.interfaces.controller;

import io.github.resonxu.seckill.common.annotation.OperationLog;
import io.github.resonxu.seckill.common.exception.BusinessException;
import io.github.resonxu.seckill.common.response.Result;
import io.github.resonxu.seckill.common.response.ResultCode;
import io.github.resonxu.seckill.order.application.SeckillOrderSubmissionAppService;
import io.github.resonxu.seckill.order.interfaces.dto.SeckillOrderSubmitDTO;
import io.github.resonxu.seckill.order.interfaces.vo.SeckillOrderSubmitVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 秒杀订单接口控制器。
 */
@Tag(name = "秒杀订单接口", description = "秒杀下单请求受理")
@RestController
@RequestMapping("/api/v1/seckill/orders")
@RequiredArgsConstructor
public class SeckillOrderController {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final SeckillOrderSubmissionAppService seckillOrderSubmissionAppService;

    /**
     * 提交秒杀订单请求。
     *
     * @param userIdHeader 网关透传的用户ID
     * @param request 秒杀下单请求
     * @return 秒杀请求受理结果
     */
    @Operation(summary = "提交秒杀订单", description = "校验秒杀资格并异步投递订单创建消息")
    @PostMapping
    @OperationLog(description = "提交秒杀订单")
    public Result<SeckillOrderSubmitVO> submit(
            @RequestHeader(USER_ID_HEADER) String userIdHeader,
            @Valid @RequestBody SeckillOrderSubmitDTO request
    ) {
        return Result.success(seckillOrderSubmissionAppService.submit(parseUserId(userIdHeader), request));
    }

    private Long parseUserId(String userIdHeader) {
        try {
            return Long.valueOf(userIdHeader);
        } catch (NumberFormatException exception) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
    }
}
