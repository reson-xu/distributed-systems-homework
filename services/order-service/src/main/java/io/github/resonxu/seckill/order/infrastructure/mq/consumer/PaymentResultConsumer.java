package io.github.resonxu.seckill.order.infrastructure.mq.consumer;

import io.github.resonxu.seckill.order.application.OrderPaymentResultAppService;
import io.github.resonxu.seckill.order.infrastructure.mq.config.SeckillRocketMqProperties;
import io.github.resonxu.seckill.order.infrastructure.mq.message.PaymentResultMessage;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 支付结果消费者。
 */
@Component
@RocketMQMessageListener(
        topic = "${seckill.rocketmq.topics.payment-result}",
        consumerGroup = "${seckill.rocketmq.consumer-groups.payment-result}"
)
public class PaymentResultConsumer implements RocketMQListener<PaymentResultMessage> {

    private final OrderPaymentResultAppService orderPaymentResultAppService;

    /**
     * 创建支付结果消费者。
     *
     * @param orderPaymentResultAppService 支付结果处理服务
     * @param rocketMqProperties RocketMQ 配置
     */
    public PaymentResultConsumer(
            OrderPaymentResultAppService orderPaymentResultAppService,
            SeckillRocketMqProperties rocketMqProperties
    ) {
        this.orderPaymentResultAppService = orderPaymentResultAppService;
    }

    @Override
    public void onMessage(PaymentResultMessage message) {
        orderPaymentResultAppService.consumePaymentResult(message);
    }
}
