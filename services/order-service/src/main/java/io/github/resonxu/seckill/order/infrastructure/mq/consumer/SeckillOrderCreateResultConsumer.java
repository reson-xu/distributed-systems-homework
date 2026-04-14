package io.github.resonxu.seckill.order.infrastructure.mq.consumer;

import io.github.resonxu.seckill.order.application.OrderCreateResultAppService;
import io.github.resonxu.seckill.order.infrastructure.mq.message.SeckillOrderCreateResultMessage;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 秒杀下单库存结果消息消费者。
 */
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "${seckill.rocketmq.topics.order-create-result}",
        consumerGroup = "${seckill.rocketmq.consumer-groups.order-create-result}"
)
public class SeckillOrderCreateResultConsumer implements RocketMQListener<SeckillOrderCreateResultMessage> {

    private final OrderCreateResultAppService orderCreateResultAppService;

    /**
     * 处理库存结果消息。
     *
     * @param message 库存结果消息
     */
    @Override
    public void onMessage(SeckillOrderCreateResultMessage message) {
        orderCreateResultAppService.consumeOrderCreateResult(message);
    }
}
