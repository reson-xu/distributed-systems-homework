package io.github.resonxu.seckill.payment.infrastructure.persistence;

import io.github.resonxu.seckill.payment.domain.model.PaymentOrder;
import io.github.resonxu.seckill.payment.domain.repository.PaymentOrderRepository;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * MyBatis 支付单仓储实现。
 */
@Mapper
public interface MybatisPaymentOrderRepository extends PaymentOrderRepository {

    @Override
    PaymentOrder findById(@Param("paymentId") Long paymentId);

    @Override
    PaymentOrder findByRequestId(@Param("requestId") String requestId);

    @Override
    PaymentOrder findByOrderId(@Param("orderId") Long orderId);

    @Override
    int insert(PaymentOrder paymentOrder);

    @Override
    int updateStatus(
            @Param("paymentId") Long paymentId,
            @Param("paymentStatus") Integer paymentStatus,
            @Param("failReason") String failReason
    );
}
