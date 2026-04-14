package io.github.resonxu.seckill.payment.domain.repository;

import io.github.resonxu.seckill.payment.domain.model.PaymentOrder;
import org.apache.ibatis.annotations.Param;

/**
 * 支付单仓储。
 */
public interface PaymentOrderRepository {

    /**
     * 按支付请求ID查询支付单。
     *
     * @param requestId 支付请求ID
     * @return 支付单
     */
    PaymentOrder findByRequestId(String requestId);

    /**
     * 按订单ID查询支付单。
     *
     * @param orderId 订单ID
     * @return 支付单
     */
    PaymentOrder findByOrderId(Long orderId);

    /**
     * 新增支付单。
     *
     * @param paymentOrder 支付单
     * @return 影响行数
     */
    int insert(PaymentOrder paymentOrder);

    /**
     * 更新支付状态。
     *
     * @param paymentId 支付单ID
     * @param paymentStatus 支付状态
     * @param failReason 失败原因
     * @return 影响行数
     */
    int updateStatus(
            @Param("paymentId") Long paymentId,
            @Param("paymentStatus") Integer paymentStatus,
            @Param("failReason") String failReason
    );
}
