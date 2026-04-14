package io.github.resonxu.seckill.payment.infrastructure.mq.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付 RocketMQ 主题配置。
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "seckill.rocketmq")
public class PaymentRocketMqProperties {

    private Topics topics = new Topics();

    /**
     * Topic 配置。
     */
    @Getter
    @Setter
    public static class Topics {

        private String paymentResult;
    }
}
