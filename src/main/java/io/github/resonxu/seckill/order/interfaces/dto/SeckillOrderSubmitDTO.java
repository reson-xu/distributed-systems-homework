package io.github.resonxu.seckill.order.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 秒杀下单请求对象。
 */
@Data
public class SeckillOrderSubmitDTO {

    @NotNull(message = "productId can not be null")
    private Long productId;

    @NotBlank(message = "requestId can not be blank")
    private String requestId;
}
