package io.github.resonxu.seckill.payment.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 支付回调请求对象。
 */
@Data
@Schema(description = "支付结果回调请求")
public class PaymentNotifyDTO {

    @NotNull
    @Schema(description = "支付单ID", example = "1912222222222222222")
    private Long paymentId;

    @NotNull
    @Schema(description = "支付是否成功", example = "true")
    private Boolean success;

    @Schema(description = "失败原因", example = "bank rejected")
    private String failReason;
}
