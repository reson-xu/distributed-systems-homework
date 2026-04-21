package io.github.resonxu.seckill.payment.application;

import io.github.resonxu.seckill.common.exception.BusinessException;
import io.github.resonxu.seckill.common.response.ResultCode;
import io.github.resonxu.seckill.payment.domain.enums.PaymentStatus;
import io.github.resonxu.seckill.payment.domain.model.PaymentOrder;
import io.github.resonxu.seckill.payment.domain.repository.PaymentOrderRepository;
import io.github.resonxu.seckill.payment.infrastructure.mq.PaymentResultProducer;
import io.github.resonxu.seckill.payment.infrastructure.mq.message.PaymentResultMessage;
import io.github.resonxu.seckill.payment.interfaces.dto.PaymentNotifyDTO;
import io.github.resonxu.seckill.payment.interfaces.vo.PaymentDetailVO;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 支付查询与回调应用服务。
 */
@Service
@RequiredArgsConstructor
public class PaymentQueryAppService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentResultProducer paymentResultProducer;

    /**
     * 查询支付单详情。
     *
     * @param userId 用户ID
     * @param paymentId 支付单ID
     * @return 支付单详情
     */
    public PaymentDetailVO getPaymentDetail(Long userId, Long paymentId) {
        return toDetailVO(loadUserPayment(paymentId, userId));
    }

    /**
     * 根据订单查询支付单。
     *
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 支付单详情
     */
    public PaymentDetailVO getPaymentByOrderId(Long userId, Long orderId) {
        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderId(orderId);
        if (paymentOrder == null || !userId.equals(paymentOrder.getUserId())) {
            throw new BusinessException(ResultCode.PAYMENT_ORDER_NOT_FOUND);
        }
        return toDetailVO(paymentOrder);
    }

    /**
     * 处理支付回调。
     *
     * @param request 回调请求
     * @return 最新支付状态
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentDetailVO notifyPayment(PaymentNotifyDTO request) {
        PaymentOrder paymentOrder = paymentOrderRepository.findById(request.getPaymentId());
        if (paymentOrder == null) {
            throw new BusinessException(ResultCode.PAYMENT_ORDER_NOT_FOUND);
        }

        PaymentStatus targetStatus = Boolean.TRUE.equals(request.getSuccess())
                ? PaymentStatus.SUCCESS
                : PaymentStatus.FAILED;
        if (Integer.valueOf(targetStatus.getCode()).equals(paymentOrder.getPaymentStatus())) {
            return toDetailVO(paymentOrder);
        }

        String failReason = targetStatus == PaymentStatus.SUCCESS ? null : normalizeFailReason(request.getFailReason());
        paymentOrderRepository.updateStatus(paymentOrder.getId(), targetStatus.getCode(), failReason);
        paymentResultProducer.send(PaymentResultMessage.builder()
                .paymentId(paymentOrder.getId())
                .orderId(paymentOrder.getOrderId())
                .userId(paymentOrder.getUserId())
                .paymentAmount(paymentOrder.getPaymentAmount())
                .success(targetStatus == PaymentStatus.SUCCESS)
                .failReason(failReason)
                .build());

        paymentOrder.setPaymentStatus(targetStatus.getCode());
        paymentOrder.setFailReason(failReason);
        return toDetailVO(paymentOrder);
    }

    private PaymentOrder loadUserPayment(Long paymentId, Long userId) {
        PaymentOrder paymentOrder = paymentOrderRepository.findById(paymentId);
        if (paymentOrder == null || !userId.equals(paymentOrder.getUserId())) {
            throw new BusinessException(ResultCode.PAYMENT_ORDER_NOT_FOUND);
        }
        return paymentOrder;
    }

    private PaymentDetailVO toDetailVO(PaymentOrder paymentOrder) {
        PaymentStatus paymentStatus = Arrays.stream(PaymentStatus.values())
                .filter(status -> status.getCode() == paymentOrder.getPaymentStatus())
                .findFirst()
                .orElseThrow(() -> new BusinessException(ResultCode.SYSTEM_ERROR, "unknown payment status"));
        return PaymentDetailVO.builder()
                .paymentId(paymentOrder.getId())
                .orderId(paymentOrder.getOrderId())
                .userId(paymentOrder.getUserId())
                .requestId(paymentOrder.getRequestId())
                .paymentAmount(paymentOrder.getPaymentAmount())
                .paymentStatus(paymentOrder.getPaymentStatus())
                .paymentStatusDesc(paymentStatus.getDescription())
                .failReason(paymentOrder.getFailReason())
                .build();
    }

    private String normalizeFailReason(String failReason) {
        return failReason == null || failReason.isBlank() ? "payment callback failed" : failReason;
    }
}
