package io.github.resonxu.seckill.payment.interfaces.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * 支付受理结果。
 */
@Value
@Builder
@Schema(description = "支付受理结果")
public class PaymentSubmitVO {

    @Schema(description = "支付单ID", example = "1912222222222222222")
    Long paymentId;

    @Schema(description = "支付状态", example = "SUCCESS")
    String status;
}
