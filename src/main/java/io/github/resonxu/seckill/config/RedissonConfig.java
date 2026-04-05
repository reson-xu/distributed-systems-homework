package io.github.resonxu.seckill.config;

import java.time.Duration;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Redisson 客户端配置。
 */
@Configuration
public class RedissonConfig {

    /**
     * 创建 RedissonClient。
     * 当前默认按单机 Redis 方式构建，具体连接参数由业务方通过 redis 配置补齐。
     *
     * @param redisProperties Redis 配置
     * @return Redisson 客户端
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(buildRedisAddress(redisProperties))
                .setDatabase(redisProperties.getDatabase());
        Duration timeout = redisProperties.getTimeout();
        if (timeout != null) {
            singleServerConfig.setTimeout((int) timeout.toMillis());
        }
        if (StringUtils.hasText(redisProperties.getUsername())) {
            singleServerConfig.setUsername(redisProperties.getUsername());
        }
        if (StringUtils.hasText(redisProperties.getPassword())) {
            singleServerConfig.setPassword(redisProperties.getPassword());
        }
        return Redisson.create(config);
    }

    private String buildRedisAddress(RedisProperties redisProperties) {
        return "redis://" + redisProperties.getHost() + ":" + redisProperties.getPort();
    }
}
