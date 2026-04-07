package io.github.resonxu.seckill.order.infrastructure.mq;

import io.github.resonxu.seckill.config.SeckillRocketMqProperties;
import io.github.resonxu.seckill.order.infrastructure.mq.message.SeckillOrderCreateMessage;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

/**
 * 秒杀下单消息生产者。
 */
@Component
@RequiredArgsConstructor
public class SeckillOrderProducer {

    private final RocketMQTemplate rocketMQTemplate;
    private final SeckillRocketMqProperties seckillRocketMqProperties;

    /**
     * 发送秒杀下单消息。
     *
     * @param message 秒杀下单消息
     */
    public void sendCreateOrderMessage(SeckillOrderCreateMessage message) {
        rocketMQTemplate.syncSend(seckillRocketMqProperties.getTopics().getOrderCreate(), message);
    }
}
