package io.github.resonxu.seckill.order.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.resonxu.seckill.inventory.domain.model.InventoryFlow;
import io.github.resonxu.seckill.inventory.domain.repository.InventoryFlowRepository;
import io.github.resonxu.seckill.inventory.domain.repository.InventoryRepository;
import io.github.resonxu.seckill.order.domain.model.MqConsumeRecord;
import io.github.resonxu.seckill.order.domain.model.SeckillOrder;
import io.github.resonxu.seckill.order.domain.repository.MqConsumeRecordRepository;
import io.github.resonxu.seckill.order.domain.repository.OrderRepository;
import io.github.resonxu.seckill.order.infrastructure.mq.message.SeckillOrderCreateMessage;
import io.github.resonxu.seckill.order.infrastructure.persistence.RedisStockReservationRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

@ExtendWith(MockitoExtension.class)
class SeckillOrderCreateAppServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MqConsumeRecordRepository mqConsumeRecordRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryFlowRepository inventoryFlowRepository;

    @Mock
    private RedisStockReservationRepository redisStockReservationRepository;

    @InjectMocks
    private SeckillOrderCreateAppService seckillOrderCreateAppService;

    @Test
    void shouldCreateOrderWhenMessageConsumedSuccessfully() {
        SeckillOrderCreateMessage message = buildMessage();

        when(mqConsumeRecordRepository.findByBizTypeAndMessageKey("SECKILL_ORDER_CREATE", "10001"))
                .thenReturn(null);
        when(inventoryRepository.findAvailableStockByProductId(10L)).thenReturn(5);
        when(inventoryRepository.deductAvailableStock(10L, 1)).thenReturn(1);

        seckillOrderCreateAppService.consumeCreateOrderMessage(message);

        ArgumentCaptor<SeckillOrder> orderCaptor = ArgumentCaptor.forClass(SeckillOrder.class);
        verify(orderRepository).insert(orderCaptor.capture());
        assertEquals(10001L, orderCaptor.getValue().getId());
        assertEquals(88L, orderCaptor.getValue().getUserId());
        assertEquals(10L, orderCaptor.getValue().getProductId());
        assertEquals("req-1", orderCaptor.getValue().getRequestId());
        assertEquals("SECKILL", orderCaptor.getValue().getCreateSource());

        ArgumentCaptor<InventoryFlow> flowCaptor = ArgumentCaptor.forClass(InventoryFlow.class);
        verify(inventoryFlowRepository).insert(flowCaptor.capture());
        assertEquals(5, flowCaptor.getValue().getBeforeAvailableStock());
        assertEquals(4, flowCaptor.getValue().getAfterAvailableStock());
        verify(mqConsumeRecordRepository).updateConsumeStatus("SECKILL_ORDER_CREATE", "10001", 1);
        verify(redisStockReservationRepository, never()).rollback(any(), any());
    }

    @Test
    void shouldSkipWhenMessageAlreadyConsumed() {
        SeckillOrderCreateMessage message = buildMessage();
        MqConsumeRecord consumeRecord = new MqConsumeRecord();
        consumeRecord.setConsumeStatus(1);

        when(mqConsumeRecordRepository.findByBizTypeAndMessageKey("SECKILL_ORDER_CREATE", "10001"))
                .thenReturn(consumeRecord);

        seckillOrderCreateAppService.consumeCreateOrderMessage(message);

        verify(mqConsumeRecordRepository, never()).insert(any(MqConsumeRecord.class));
        verify(orderRepository, never()).insert(any(SeckillOrder.class));
    }

    @Test
    void shouldSkipWhenConcurrentConsumerAlreadyFinishedMessage() {
        SeckillOrderCreateMessage message = buildMessage();
        MqConsumeRecord consumeRecord = new MqConsumeRecord();
        consumeRecord.setConsumeStatus(1);

        when(mqConsumeRecordRepository.findByBizTypeAndMessageKey("SECKILL_ORDER_CREATE", "10001"))
                .thenReturn(null, consumeRecord);
        when(mqConsumeRecordRepository.insert(any(MqConsumeRecord.class)))
                .thenThrow(new DuplicateKeyException("duplicate"));

        seckillOrderCreateAppService.consumeCreateOrderMessage(message);

        verify(orderRepository, never()).insert(any(SeckillOrder.class));
        verify(redisStockReservationRepository, never()).rollback(any(), any());
    }

    @Test
    void shouldRollbackRedisReservationWhenDatabaseStockInsufficient() {
        SeckillOrderCreateMessage message = buildMessage();

        when(mqConsumeRecordRepository.findByBizTypeAndMessageKey("SECKILL_ORDER_CREATE", "10001"))
                .thenReturn(null);
        when(inventoryRepository.findAvailableStockByProductId(10L)).thenReturn(0);

        seckillOrderCreateAppService.consumeCreateOrderMessage(message);

        verify(redisStockReservationRepository).rollback(10L, 88L);
        verify(mqConsumeRecordRepository).updateConsumeStatus("SECKILL_ORDER_CREATE", "10001", 2);
        verify(orderRepository, never()).insert(any(SeckillOrder.class));
        verify(inventoryFlowRepository, never()).insert(any(InventoryFlow.class));
    }

    private SeckillOrderCreateMessage buildMessage() {
        return SeckillOrderCreateMessage.builder()
                .orderId(10001L)
                .userId(88L)
                .productId(10L)
                .requestId("req-1")
                .orderAmount(new BigDecimal("99.90"))
                .build();
    }
}
