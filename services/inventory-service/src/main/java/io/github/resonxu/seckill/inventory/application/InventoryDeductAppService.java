package io.github.resonxu.seckill.inventory.application;

import io.github.resonxu.seckill.inventory.domain.model.InventoryFlow;
import io.github.resonxu.seckill.inventory.domain.model.MqConsumeRecord;
import io.github.resonxu.seckill.inventory.domain.repository.InventoryFlowRepository;
import io.github.resonxu.seckill.inventory.domain.repository.InventoryRepository;
import io.github.resonxu.seckill.inventory.domain.repository.MqConsumeRecordRepository;
import io.github.resonxu.seckill.inventory.infrastructure.mq.SeckillOrderCreateResultProducer;
import io.github.resonxu.seckill.inventory.infrastructure.mq.message.SeckillOrderCreateMessage;
import io.github.resonxu.seckill.inventory.infrastructure.mq.message.SeckillOrderCreateResultMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 库存扣减应用服务。
 */
@Service
@RequiredArgsConstructor
public class InventoryDeductAppService {

    private static final String BIZ_TYPE_ORDER_CREATE = "SECKILL_ORDER_CREATE";
    private static final Integer CONSUME_STATUS_PROCESSING = 0;
    private static final Integer CONSUME_STATUS_SUCCESS = 1;
    private static final String FLOW_TYPE_DEDUCT = "DEDUCT";
    private static final String SOURCE_EVENT_ORDER_CREATE = "SECKILL_ORDER_CREATE";
    private static final int SECKILL_ORDER_QUANTITY = 1;

    private final InventoryRepository inventoryRepository;
    private final InventoryFlowRepository inventoryFlowRepository;
    private final MqConsumeRecordRepository mqConsumeRecordRepository;
    private final SeckillOrderCreateResultProducer seckillOrderCreateResultProducer;

    /**
     * 消费下单事件并执行库存扣减。
     *
     * @param message 下单事件
     */
    @Transactional(rollbackFor = Exception.class)
    public void consumeOrderCreateMessage(SeckillOrderCreateMessage message) {
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

        Integer beforeAvailableStock = inventoryRepository.findAvailableStockByProductId(message.getProductId());
        if (beforeAvailableStock == null || beforeAvailableStock < SECKILL_ORDER_QUANTITY) {
            publishResult(message, false, "stock not enough");
            mqConsumeRecordRepository.updateConsumeStatus(BIZ_TYPE_ORDER_CREATE, messageKey, CONSUME_STATUS_SUCCESS);
            return;
        }

        int affectedRows = inventoryRepository.deductAvailableStock(message.getProductId(), SECKILL_ORDER_QUANTITY);
        if (affectedRows != 1) {
            publishResult(message, false, "stock not enough");
            mqConsumeRecordRepository.updateConsumeStatus(BIZ_TYPE_ORDER_CREATE, messageKey, CONSUME_STATUS_SUCCESS);
            return;
        }

        inventoryFlowRepository.insert(buildInventoryFlow(message, beforeAvailableStock));
        publishResult(message, true, null);
        mqConsumeRecordRepository.updateConsumeStatus(BIZ_TYPE_ORDER_CREATE, messageKey, CONSUME_STATUS_SUCCESS);
    }

    private boolean isAlreadyConsumed(String messageKey) {
        MqConsumeRecord consumeRecord = mqConsumeRecordRepository.findByBizTypeAndMessageKey(BIZ_TYPE_ORDER_CREATE, messageKey);
        return consumeRecord != null && CONSUME_STATUS_SUCCESS.equals(consumeRecord.getConsumeStatus());
    }

    private MqConsumeRecord buildConsumeRecord(String messageKey) {
        MqConsumeRecord consumeRecord = new MqConsumeRecord();
        consumeRecord.setBizType(BIZ_TYPE_ORDER_CREATE);
        consumeRecord.setMessageKey(messageKey);
        consumeRecord.setConsumeStatus(CONSUME_STATUS_PROCESSING);
        return consumeRecord;
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

    private void publishResult(SeckillOrderCreateMessage message, boolean success, String failReason) {
        seckillOrderCreateResultProducer.send(SeckillOrderCreateResultMessage.builder()
                .orderId(message.getOrderId())
                .userId(message.getUserId())
                .productId(message.getProductId())
                .requestId(message.getRequestId())
                .orderAmount(message.getOrderAmount())
                .success(success)
                .failReason(failReason)
                .build());
    }
}
