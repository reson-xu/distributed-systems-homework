package io.github.resonxu.seckill.inventory.infrastructure.mq.consumer;

import io.github.resonxu.seckill.inventory.application.InventoryDeductAppService;
import io.github.resonxu.seckill.inventory.infrastructure.mq.message.SeckillOrderCreateMessage;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 秒杀下单消息消费者。
 */
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "${seckill.rocketmq.topics.order-create}",
        consumerGroup = "${seckill.rocketmq.consumer-groups.order-create}"
)
public class SeckillOrderCreateConsumer implements RocketMQListener<SeckillOrderCreateMessage> {

    private final InventoryDeductAppService inventoryDeductAppService;

    /**
     * 处理秒杀下单消息。
     *
     * @param message 秒杀下单消息
     */
    @Override
    public void onMessage(SeckillOrderCreateMessage message) {
        inventoryDeductAppService.consumeOrderCreateMessage(message);
    }
}
