package io.github.resonxu.seckill.inventory.infrastructure.mq;

import io.github.resonxu.seckill.inventory.infrastructure.mq.message.SeckillOrderCreateResultMessage;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 秒杀下单库存处理结果消息生产者。
 */
@Component
public class SeckillOrderCreateResultProducer {

    private final RocketMQTemplate rocketMQTemplate;
    private final String orderCreateResultTopic;

    /**
     * 创建库存处理结果消息生产者。
     *
     * @param rocketMQTemplate RocketMQ 模板
     * @param orderCreateResultTopic 库存处理结果 Topic
     */
    public SeckillOrderCreateResultProducer(
            RocketMQTemplate rocketMQTemplate,
            @Value("${seckill.rocketmq.topics.order-create-result}") String orderCreateResultTopic
    ) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.orderCreateResultTopic = orderCreateResultTopic;
    }

    /**
     * 发送库存处理结果消息。
     *
     * @param message 库存处理结果消息
     */
    public void send(SeckillOrderCreateResultMessage message) {
        rocketMQTemplate.syncSend(orderCreateResultTopic, message);
    }
}
