package io.github.resonxu.seckill.payment.application;

import io.github.resonxu.seckill.common.annotation.Idempotent;
import io.github.resonxu.seckill.common.exception.BusinessException;
import io.github.resonxu.seckill.common.id.SnowflakeIdGenerator;
import io.github.resonxu.seckill.common.response.ResultCode;
import io.github.resonxu.seckill.payment.domain.enums.PaymentStatus;
import io.github.resonxu.seckill.payment.domain.model.PaymentOrder;
import io.github.resonxu.seckill.payment.domain.repository.PaymentOrderRepository;
import io.github.resonxu.seckill.payment.infrastructure.client.OrderClient;
import io.github.resonxu.seckill.payment.infrastructure.client.dto.OrderDetailDTO;
import io.github.resonxu.seckill.payment.infrastructure.mq.PaymentResultProducer;
import io.github.resonxu.seckill.payment.infrastructure.mq.message.PaymentResultMessage;
import io.github.resonxu.seckill.payment.interfaces.dto.PaymentSubmitDTO;
import io.github.resonxu.seckill.payment.interfaces.vo.PaymentSubmitVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 支付提交应用服务。
 */
@Service
@RequiredArgsConstructor
public class PaymentSubmitAppService {

    private static final int ORDER_STATUS_CREATED = 1;
    private static final int ORDER_STATUS_PAY_FAILED = 6;

    private final OrderClient orderClient;
    private final PaymentOrderRepository paymentOrderRepository;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final PaymentResultProducer paymentResultProducer;

    /**
     * 提交支付请求。
     *
     * @param userId 用户ID
     * @param request 支付请求
     * @return 支付受理结果
     */
    @Idempotent(
            scene = "payment-submit",
            key = "#userId + ':' + #request.requestId",
            ttlSeconds = 10,
            releaseOnException = true
    )
    public PaymentSubmitVO submit(Long userId, PaymentSubmitDTO request) {
        PaymentOrder existingPayment = paymentOrderRepository.findByRequestId(request.getRequestId());
        if (existingPayment != null) {
            return buildSubmitVO(existingPayment);
        }

        OrderDetailDTO orderDetail = queryOrderDetail(request.getOrderId());
        validateOrder(orderDetail, userId);

        PaymentOrder duplicatedOrderPayment = paymentOrderRepository.findByOrderId(request.getOrderId());
        if (duplicatedOrderPayment != null) {
            return buildSubmitVO(duplicatedOrderPayment);
        }

        PaymentOrder paymentOrder = createPendingPaymentOrder(userId, request, orderDetail);
        boolean success = !Boolean.FALSE.equals(request.getSuccess());
        String failReason = success ? null : "payment failed";
        try {
            paymentResultProducer.send(PaymentResultMessage.builder()
                    .paymentId(paymentOrder.getId())
                    .orderId(paymentOrder.getOrderId())
                    .userId(paymentOrder.getUserId())
                    .paymentAmount(paymentOrder.getPaymentAmount())
                    .success(success)
                    .failReason(failReason)
                    .build());
        } catch (Exception exception) {
            throw new BusinessException(ResultCode.PAYMENT_SUBMIT_FAILED);
        }

        updatePaymentResult(paymentOrder.getId(), success, failReason);
        paymentOrder.setPaymentStatus(success ? PaymentStatus.SUCCESS.getCode() : PaymentStatus.FAILED.getCode());
        paymentOrder.setFailReason(failReason);
        return buildSubmitVO(paymentOrder);
    }

    private OrderDetailDTO queryOrderDetail(Long orderId) {
        OrderDetailDTO orderDetail = orderClient.getOrderDetail(orderId).getData();
        if (orderDetail == null) {
            throw new BusinessException(ResultCode.PAYMENT_ORDER_NOT_FOUND);
        }
        return orderDetail;
    }

    private void validateOrder(OrderDetailDTO orderDetail, Long userId) {
        if (!userId.equals(orderDetail.getUserId())) {
            throw new BusinessException(ResultCode.PAYMENT_USER_MISMATCH);
        }
        Integer orderStatus = orderDetail.getOrderStatus();
        if (!Integer.valueOf(ORDER_STATUS_CREATED).equals(orderStatus)
                && !Integer.valueOf(ORDER_STATUS_PAY_FAILED).equals(orderStatus)) {
            throw new BusinessException(ResultCode.ORDER_NOT_PAYABLE);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    protected PaymentOrder createPendingPaymentOrder(Long userId, PaymentSubmitDTO request, OrderDetailDTO orderDetail) {
        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setId(snowflakeIdGenerator.nextId());
        paymentOrder.setOrderId(orderDetail.getOrderId());
        paymentOrder.setUserId(userId);
        paymentOrder.setRequestId(request.getRequestId());
        paymentOrder.setPaymentAmount(orderDetail.getOrderAmount());
        paymentOrder.setPaymentStatus(PaymentStatus.PENDING.getCode());
        paymentOrderRepository.insert(paymentOrder);
        return paymentOrder;
    }

    @Transactional(rollbackFor = Exception.class)
    protected void updatePaymentResult(Long paymentId, boolean success, String failReason) {
        paymentOrderRepository.updateStatus(
                paymentId,
                success ? PaymentStatus.SUCCESS.getCode() : PaymentStatus.FAILED.getCode(),
                failReason
        );
    }

    private PaymentSubmitVO buildSubmitVO(PaymentOrder paymentOrder) {
        String status = PaymentStatus.PENDING.getDescription();
        if (Integer.valueOf(PaymentStatus.SUCCESS.getCode()).equals(paymentOrder.getPaymentStatus())) {
            status = PaymentStatus.SUCCESS.getDescription();
        } else if (Integer.valueOf(PaymentStatus.FAILED.getCode()).equals(paymentOrder.getPaymentStatus())) {
            status = PaymentStatus.FAILED.getDescription();
        }
        return PaymentSubmitVO.builder()
                .paymentId(paymentOrder.getId())
                .status(status)
                .build();
    }
}
