package io.github.resonxu.seckill.payment.infrastructure.client;

import io.github.resonxu.seckill.common.response.Result;
import io.github.resonxu.seckill.payment.infrastructure.client.dto.OrderDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 订单服务 Feign 客户端。
 */
@FeignClient(name = "order-service")
public interface OrderClient {

    /**
     * 查询订单详情。
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    @GetMapping("/api/v1/orders/{orderId}")
    Result<OrderDetailDTO> getOrderDetail(@PathVariable("orderId") Long orderId);
}
