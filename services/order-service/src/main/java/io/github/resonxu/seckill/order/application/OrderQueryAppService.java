package io.github.resonxu.seckill.order.application;

import io.github.resonxu.seckill.common.exception.BusinessException;
import io.github.resonxu.seckill.common.response.ResultCode;
import io.github.resonxu.seckill.order.domain.model.SeckillOrder;
import io.github.resonxu.seckill.order.domain.repository.OrderRepository;
import io.github.resonxu.seckill.order.interfaces.vo.OrderDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 订单查询应用服务。
 */
@Service
@RequiredArgsConstructor
public class OrderQueryAppService {

    private final OrderRepository orderRepository;

    /**
     * 按订单ID查询订单详情。
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    public OrderDetailVO getOrderDetail(Long orderId) {
        SeckillOrder seckillOrder = orderRepository.findById(orderId);
        if (seckillOrder == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "order not found");
        }
        return OrderDetailVO.builder()
                .orderId(seckillOrder.getId())
                .userId(seckillOrder.getUserId())
                .productId(seckillOrder.getProductId())
                .requestId(seckillOrder.getRequestId())
                .orderAmount(seckillOrder.getOrderAmount())
                .orderStatus(seckillOrder.getOrderStatus())
                .build();
    }
}
