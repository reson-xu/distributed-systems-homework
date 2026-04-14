package io.github.resonxu.seckill.order.application;

import io.github.resonxu.seckill.order.domain.enums.OrderStatus;
import io.github.resonxu.seckill.order.domain.model.MqConsumeRecord;
import io.github.resonxu.seckill.order.domain.model.SeckillOrder;
import io.github.resonxu.seckill.order.domain.repository.MqConsumeRecordRepository;
import io.github.resonxu.seckill.order.domain.repository.OrderRepository;
import io.github.resonxu.seckill.order.infrastructure.mq.message.PaymentResultMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 支付结果处理应用服务。
 */
@Service
@RequiredArgsConstructor
public class OrderPaymentResultAppService {

    private static final String BIZ_TYPE_PAYMENT_RESULT = "PAYMENT_RESULT";
    private static final Integer CONSUME_STATUS_PROCESSING = 0;
    private static final Integer CONSUME_STATUS_SUCCESS = 1;

    private final OrderRepository orderRepository;
    private final MqConsumeRecordRepository mqConsumeRecordRepository;

    /**
     * 消费支付结果并更新订单状态。
     *
     * @param message 支付结果事件
     */
    @Transactional(rollbackFor = Exception.class)
    public void consumePaymentResult(PaymentResultMessage message) {
        String messageKey = String.valueOf(message.getPaymentId());
        if (isAlreadyConsumed(messageKey)) {
            return;
        }

        try {
            mqConsumeRecordRepository.insert(buildConsumeRecord(messageKey));
        } catch (DuplicateKeyException exception) {
            if (isAlreadyConsumed(messageKey)) {
                return;
            }
            throw exception;
        }

        SeckillOrder seckillOrder = orderRepository.findByIdAndUserId(message.getOrderId(), message.getUserId());
        if (seckillOrder == null) {
            throw new IllegalStateException("order not found for payment result");
        }
        if (Integer.valueOf(OrderStatus.PAID.getCode()).equals(seckillOrder.getOrderStatus())) {
            mqConsumeRecordRepository.updateConsumeStatus(BIZ_TYPE_PAYMENT_RESULT, messageKey, CONSUME_STATUS_SUCCESS);
            return;
        }

        Integer targetStatus = Boolean.TRUE.equals(message.getSuccess())
                ? OrderStatus.PAID.getCode()
                : OrderStatus.PAY_FAILED.getCode();
        int affectedRows = orderRepository.updateStatus(
                message.getOrderId(),
                OrderStatus.CREATED.getCode(),
                targetStatus,
                message.getFailReason()
        );
        if (affectedRows == 0 && Boolean.TRUE.equals(message.getSuccess())) {
            orderRepository.updateStatus(
                    message.getOrderId(),
                    OrderStatus.PAY_FAILED.getCode(),
                    OrderStatus.PAID.getCode(),
                    null
            );
        }
        mqConsumeRecordRepository.updateConsumeStatus(BIZ_TYPE_PAYMENT_RESULT, messageKey, CONSUME_STATUS_SUCCESS);
    }

    private boolean isAlreadyConsumed(String messageKey) {
        MqConsumeRecord consumeRecord =
                mqConsumeRecordRepository.findByBizTypeAndMessageKey(BIZ_TYPE_PAYMENT_RESULT, messageKey);
        return consumeRecord != null && CONSUME_STATUS_SUCCESS.equals(consumeRecord.getConsumeStatus());
    }

    private MqConsumeRecord buildConsumeRecord(String messageKey) {
        MqConsumeRecord consumeRecord = new MqConsumeRecord();
        consumeRecord.setBizType(BIZ_TYPE_PAYMENT_RESULT);
        consumeRecord.setMessageKey(messageKey);
        consumeRecord.setConsumeStatus(CONSUME_STATUS_PROCESSING);
        return consumeRecord;
    }
}
