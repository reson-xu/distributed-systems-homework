package io.github.resonxu.seckill.payment.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 支付提交请求。
 */
@Data
@Schema(description = "支付提交请求")
public class PaymentSubmitDTO {

    @NotNull
    @Schema(description = "订单ID", example = "1911111111111111111")
    private Long orderId;

    @NotBlank
    @Schema(description = "支付请求幂等ID", example = "pay-20260414-0001")
    private String requestId;

    @Schema(description = "是否模拟支付成功，默认 true", example = "true")
    private Boolean success;
}
