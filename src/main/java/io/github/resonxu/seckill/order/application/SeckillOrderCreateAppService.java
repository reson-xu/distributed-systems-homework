package io.github.resonxu.seckill.order.application;

import io.github.resonxu.seckill.inventory.domain.model.InventoryFlow;
import io.github.resonxu.seckill.inventory.domain.repository.InventoryFlowRepository;
import io.github.resonxu.seckill.inventory.domain.repository.InventoryRepository;
import io.github.resonxu.seckill.order.domain.enums.OrderStatus;
import io.github.resonxu.seckill.order.domain.model.MqConsumeRecord;
import io.github.resonxu.seckill.order.domain.model.SeckillOrder;
import io.github.resonxu.seckill.order.domain.repository.MqConsumeRecordRepository;
import io.github.resonxu.seckill.order.domain.repository.OrderRepository;
import io.github.resonxu.seckill.order.infrastructure.mq.message.SeckillOrderCreateMessage;
import io.github.resonxu.seckill.order.infrastructure.persistence.RedisStockReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 秒杀下单消息消费应用服务。
 */
@Service
@RequiredArgsConstructor
public class SeckillOrderCreateAppService {

    private static final String CREATE_SOURCE_SECKILL = "SECKILL";
    private static final String FLOW_TYPE_DEDUCT = "DEDUCT";
    private static final String SOURCE_EVENT_ORDER_CREATE = "SECKILL_ORDER_CREATE";
    private static final String BIZ_TYPE_ORDER_CREATE = "SECKILL_ORDER_CREATE";
    private static final Integer CONSUME_STATUS_PROCESSING = 0;
    private static final Integer CONSUME_STATUS_SUCCESS = 1;
    private static final Integer CONSUME_STATUS_FAILED = 2;
    private static final int SECKILL_ORDER_QUANTITY = 1;

    private final OrderRepository orderRepository;
    private final MqConsumeRecordRepository mqConsumeRecordRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryFlowRepository inventoryFlowRepository;
    private final RedisStockReservationRepository redisStockReservationRepository;

    /**
     * 消费秒杀下单消息并完成订单落库。
     *
     * @param message 秒杀下单消息
     */
    @Transactional(rollbackFor = Exception.class)
    public void consumeCreateOrderMessage(SeckillOrderCreateMessage message) {
        String messageKey = String.valueOf(message.getOrderId());
        if (isAlreadyConsumed(messageKey)) {
            return;
        }

        try {
            mqConsumeRecordRepository.insert(buildConsumeRecord(messageKey, CONSUME_STATUS_PROCESSING));
        } catch (DuplicateKeyException exception) {
            if (isAlreadyConsumed(messageKey)) {
                return;
            }
            throw exception;
        }

        Integer beforeAvailableStock = inventoryRepository.findAvailableStockByProductId(message.getProductId());
        if (beforeAvailableStock == null || beforeAvailableStock < SECKILL_ORDER_QUANTITY) {
            handleTerminalFailure(message, messageKey);
            return;
        }

        int affectedRows = inventoryRepository.deductAvailableStock(message.getProductId(), SECKILL_ORDER_QUANTITY);
        if (affectedRows != 1) {
            handleTerminalFailure(message, messageKey);
            return;
        }

        orderRepository.insert(buildSeckillOrder(message));
        inventoryFlowRepository.insert(buildInventoryFlow(message, beforeAvailableStock));
        mqConsumeRecordRepository.updateConsumeStatus(BIZ_TYPE_ORDER_CREATE, messageKey, CONSUME_STATUS_SUCCESS);
    }

    private boolean isAlreadyConsumed(String messageKey) {
        MqConsumeRecord consumeRecord = mqConsumeRecordRepository.findByBizTypeAndMessageKey(BIZ_TYPE_ORDER_CREATE, messageKey);
        return consumeRecord != null && CONSUME_STATUS_SUCCESS.equals(consumeRecord.getConsumeStatus());
    }

    private void handleTerminalFailure(SeckillOrderCreateMessage message, String messageKey) {
        redisStockReservationRepository.rollback(message.getProductId(), message.getUserId());
        mqConsumeRecordRepository.updateConsumeStatus(BIZ_TYPE_ORDER_CREATE, messageKey, CONSUME_STATUS_FAILED);
    }

    private MqConsumeRecord buildConsumeRecord(String messageKey, Integer consumeStatus) {
        MqConsumeRecord consumeRecord = new MqConsumeRecord();
        consumeRecord.setBizType(BIZ_TYPE_ORDER_CREATE);
        consumeRecord.setMessageKey(messageKey);
        consumeRecord.setConsumeStatus(consumeStatus);
        return consumeRecord;
    }

    private SeckillOrder buildSeckillOrder(SeckillOrderCreateMessage message) {
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setId(message.getOrderId());
        seckillOrder.setUserId(message.getUserId());
        seckillOrder.setProductId(message.getProductId());
        seckillOrder.setRequestId(message.getRequestId());
        seckillOrder.setOrderAmount(message.getOrderAmount());
        seckillOrder.setOrderStatus(OrderStatus.CREATED.getCode());
        seckillOrder.setCreateSource(CREATE_SOURCE_SECKILL);
        return seckillOrder;
    }

    private InventoryFlow buildInventoryFlow(SeckillOrderCreateMessage message, Integer beforeAvailableStock) {
        InventoryFlow inventoryFlow = new InventoryFlow();
        inventoryFlow.setBizId(message.getOrderId());
        inventoryFlow.setProductId(message.getProductId());
        inventoryFlow.setFlowType(FLOW_TYPE_DEDUCT);
        inventoryFlow.setChangeCount(SECKILL_ORDER_QUANTITY);
        inventoryFlow.setBeforeAvailableStock(beforeAvailableStock);
        inventoryFlow.setAfterAvailableStock(beforeAvailableStock - SECKILL_ORDER_QUANTITY);
        inventoryFlow.setSourceEvent(SOURCE_EVENT_ORDER_CREATE);
        return inventoryFlow;
    }
}
