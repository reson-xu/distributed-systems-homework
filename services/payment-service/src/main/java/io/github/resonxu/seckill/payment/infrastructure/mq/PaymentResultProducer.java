package io.github.resonxu.seckill.payment.infrastructure.mq;

import io.github.resonxu.seckill.payment.infrastructure.mq.config.PaymentRocketMqProperties;
import io.github.resonxu.seckill.payment.infrastructure.mq.message.PaymentResultMessage;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

/**
 * 支付结果消息生产者。
 */
@Component
@RequiredArgsConstructor
public class PaymentResultProducer {

    private final RocketMQTemplate rocketMQTemplate;
    private final PaymentRocketMqProperties paymentRocketMqProperties;

    /**
     * 发送支付结果事件。
     *
     * @param message 支付结果
     */
    public void send(PaymentResultMessage message) {
        rocketMQTemplate.convertAndSend(paymentRocketMqProperties.getTopics().getPaymentResult(), message);
    }
}
