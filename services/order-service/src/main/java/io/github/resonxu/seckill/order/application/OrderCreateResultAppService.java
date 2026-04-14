package io.github.resonxu.seckill.order.application;

import io.github.resonxu.seckill.order.domain.enums.OrderStatus;
import io.github.resonxu.seckill.order.domain.model.MqConsumeRecord;
import io.github.resonxu.seckill.order.domain.model.SeckillOrder;
import io.github.resonxu.seckill.order.domain.repository.MqConsumeRecordRepository;
import io.github.resonxu.seckill.order.domain.repository.OrderRepository;
import io.github.resonxu.seckill.order.infrastructure.mq.message.SeckillOrderCreateResultMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单创建结果处理应用服务。
 */
@Service
@RequiredArgsConstructor
public class OrderCreateResultAppService {

    private static final String BIZ_TYPE_ORDER_CREATE_RESULT = "SECKILL_ORDER_CREATE_RESULT";
    private static final Integer CONSUME_STATUS_PROCESSING = 0;
    private static final Integer CONSUME_STATUS_SUCCESS = 1;
    private static final String CREATE_SOURCE_SECKILL = "SECKILL";

    private final OrderRepository orderRepository;
    private final MqConsumeRecordRepository mqConsumeRecordRepository;

    /**
     * 消费库存结果事件并创建订单。
     *
     * @param message 库存结果事件
     */
    @Transactional(rollbackFor = Exception.class)
    public void consumeOrderCreateResult(SeckillOrderCreateResultMessage message) {
        String messageKey = String.valueOf(message.getOrderId());
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

        if (orderRepository.findById(message.getOrderId()) == null) {
            orderRepository.insert(buildSeckillOrder(message));
        }
        mqConsumeRecordRepository.updateConsumeStatus(BIZ_TYPE_ORDER_CREATE_RESULT, messageKey, CONSUME_STATUS_SUCCESS);
    }

    private boolean isAlreadyConsumed(String messageKey) {
        MqConsumeRecord consumeRecord =
                mqConsumeRecordRepository.findByBizTypeAndMessageKey(BIZ_TYPE_ORDER_CREATE_RESULT, messageKey);
        return consumeRecord != null && CONSUME_STATUS_SUCCESS.equals(consumeRecord.getConsumeStatus());
    }

    private MqConsumeRecord buildConsumeRecord(String messageKey) {
        MqConsumeRecord consumeRecord = new MqConsumeRecord();
        consumeRecord.setBizType(BIZ_TYPE_ORDER_CREATE_RESULT);
        consumeRecord.setMessageKey(messageKey);
        consumeRecord.setConsumeStatus(CONSUME_STATUS_PROCESSING);
        return consumeRecord;
    }

    private SeckillOrder buildSeckillOrder(SeckillOrderCreateResultMessage message) {
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setId(message.getOrderId());
        seckillOrder.setUserId(message.getUserId());
        seckillOrder.setProductId(message.getProductId());
        seckillOrder.setRequestId(message.getRequestId());
        seckillOrder.setOrderAmount(message.getOrderAmount());
        seckillOrder.setOrderStatus(Boolean.TRUE.equals(message.getSuccess())
                ? OrderStatus.CREATED.getCode()
                : OrderStatus.FAILED.getCode());
        seckillOrder.setFailReason(message.getFailReason());
        seckillOrder.setCreateSource(CREATE_SOURCE_SECKILL);
        return seckillOrder;
    }
}
