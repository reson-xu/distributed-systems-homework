package io.github.resonxu.seckill.order.application;

import io.github.resonxu.seckill.common.exception.BusinessException;
import io.github.resonxu.seckill.common.response.ResultCode;
import io.github.resonxu.seckill.order.domain.enums.OrderStatus;
import io.github.resonxu.seckill.order.domain.model.SeckillOrder;
import io.github.resonxu.seckill.order.domain.repository.OrderRepository;
import io.github.resonxu.seckill.order.interfaces.vo.OrderDetailVO;
import io.github.resonxu.seckill.order.interfaces.vo.OrderStatusVO;
import io.github.resonxu.seckill.order.interfaces.vo.OrderSummaryVO;
import io.github.resonxu.seckill.order.interfaces.vo.OrderTimelineEventVO;
import io.github.resonxu.seckill.order.interfaces.vo.OrderTimelineVO;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 订单查询应用服务。
 */
@Service
@RequiredArgsConstructor
public class OrderQueryAppService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

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

    /**
     * 查询当前用户订单列表。
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 页大小
     * @return 订单列表
     */
    public List<OrderSummaryVO> listUserOrders(Long userId, Integer page, Integer size) {
        return orderRepository.listByUserId(userId, buildOffset(page, size), normalizeSize(size))
                .stream()
                .map(this::toOrderSummary)
                .toList();
    }

    /**
     * 查询当前用户订单状态。
     *
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 订单状态
     */
    public OrderStatusVO getOrderStatus(Long userId, Long orderId) {
        SeckillOrder seckillOrder = loadUserOrder(userId, orderId);
        return OrderStatusVO.builder()
                .orderId(seckillOrder.getId())
                .orderStatus(seckillOrder.getOrderStatus())
                .orderStatusDesc(resolveStatusDesc(seckillOrder.getOrderStatus()))
                .failReason(seckillOrder.getFailReason())
                .build();
    }

    /**
     * 查询订单时间线。
     *
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 订单时间线
     */
    public OrderTimelineVO getOrderTimeline(Long userId, Long orderId) {
        SeckillOrder seckillOrder = loadUserOrder(userId, orderId);
        List<OrderTimelineEventVO> events = new ArrayList<>();
        OrderStatus status = resolveStatus(seckillOrder.getOrderStatus());

        switch (status) {
            case FAILED -> events.add(OrderTimelineEventVO.builder()
                    .eventCode("ORDER_FAILED")
                    .eventName("订单创建失败")
                    .eventTime(seckillOrder.getCreatedAt())
                    .message(defaultMessage(seckillOrder.getFailReason(), "库存扣减失败或订单创建失败"))
                    .build());
            case PAY_FAILED -> {
                events.add(buildCreatedEvent(seckillOrder));
                events.add(OrderTimelineEventVO.builder()
                        .eventCode("PAYMENT_FAILED")
                        .eventName("支付失败")
                        .eventTime(seckillOrder.getUpdatedAt())
                        .message(defaultMessage(seckillOrder.getFailReason(), "支付失败"))
                        .build());
            }
            case PAID -> {
                events.add(buildCreatedEvent(seckillOrder));
                events.add(OrderTimelineEventVO.builder()
                        .eventCode("ORDER_PAID")
                        .eventName("订单已支付")
                        .eventTime(seckillOrder.getUpdatedAt())
                        .message("支付成功")
                        .build());
            }
            case CANCELLED -> {
                events.add(buildCreatedEvent(seckillOrder));
                events.add(OrderTimelineEventVO.builder()
                        .eventCode("ORDER_CANCELLED")
                        .eventName("订单已取消")
                        .eventTime(seckillOrder.getUpdatedAt())
                        .message(defaultMessage(seckillOrder.getFailReason(), "订单已取消"))
                        .build());
            }
            default -> events.add(buildCreatedEvent(seckillOrder));
        }

        return OrderTimelineVO.builder()
                .orderId(seckillOrder.getId())
                .currentStatus(seckillOrder.getOrderStatus())
                .currentStatusDesc(resolveStatusDesc(seckillOrder.getOrderStatus()))
                .events(events)
                .build();
    }

    /**
     * 取消订单。
     *
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 最新订单状态
     */
    public OrderStatusVO cancelOrder(Long userId, Long orderId) {
        SeckillOrder seckillOrder = loadUserOrder(userId, orderId);
        OrderStatus currentStatus = resolveStatus(seckillOrder.getOrderStatus());
        if (currentStatus == OrderStatus.CANCELLED) {
            return getOrderStatus(userId, orderId);
        }
        if (currentStatus == OrderStatus.PAID) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "paid order cannot be cancelled");
        }
        if (currentStatus == OrderStatus.FAILED) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "failed order cannot be cancelled");
        }

        int affectedRows = orderRepository.updateStatus(
                orderId,
                OrderStatus.CREATED.getCode(),
                OrderStatus.CANCELLED.getCode(),
                "cancelled by user"
        );
        if (affectedRows == 0) {
            orderRepository.updateStatus(
                    orderId,
                    OrderStatus.PAY_FAILED.getCode(),
                    OrderStatus.CANCELLED.getCode(),
                    "cancelled after payment failure"
            );
        }
        return getOrderStatus(userId, orderId);
    }

    private SeckillOrder loadUserOrder(Long userId, Long orderId) {
        SeckillOrder seckillOrder = orderRepository.findByIdAndUserId(orderId, userId);
        if (seckillOrder == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "order not found");
        }
        return seckillOrder;
    }

    private OrderSummaryVO toOrderSummary(SeckillOrder seckillOrder) {
        return OrderSummaryVO.builder()
                .orderId(seckillOrder.getId())
                .productId(seckillOrder.getProductId())
                .orderAmount(seckillOrder.getOrderAmount())
                .orderStatus(seckillOrder.getOrderStatus())
                .orderStatusDesc(resolveStatusDesc(seckillOrder.getOrderStatus()))
                .failReason(seckillOrder.getFailReason())
                .build();
    }

    private OrderTimelineEventVO buildCreatedEvent(SeckillOrder seckillOrder) {
        return OrderTimelineEventVO.builder()
                .eventCode("ORDER_CREATED")
                .eventName("订单已创建")
                .eventTime(seckillOrder.getCreatedAt())
                .message("订单创建成功")
                .build();
    }

    private String resolveStatusDesc(Integer statusCode) {
        return resolveStatus(statusCode).getDescription();
    }

    private OrderStatus resolveStatus(Integer statusCode) {
        return java.util.Arrays.stream(OrderStatus.values())
                .filter(status -> status.getCode() == statusCode)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ResultCode.SYSTEM_ERROR, "unknown order status"));
    }

    private int buildOffset(Integer page, Integer size) {
        int normalizedPage = page == null || page < 1 ? DEFAULT_PAGE : page;
        return (normalizedPage - 1) * normalizeSize(size);
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private String defaultMessage(String source, String fallback) {
        return source == null || source.isBlank() ? fallback : source;
    }
}
