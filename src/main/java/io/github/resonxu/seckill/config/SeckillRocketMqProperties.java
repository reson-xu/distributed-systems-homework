package io.github.resonxu.seckill.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 秒杀 RocketMQ 主题配置。
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "seckill.rocketmq")
public class SeckillRocketMqProperties {

    private Topics topics = new Topics();

    /**
     * RocketMQ Topic 配置。
     */
    @Getter
    @Setter
    public static class Topics {

        private String orderCreate;
        private String orderCancel;
        private String inventoryCompensate;
    }
}
