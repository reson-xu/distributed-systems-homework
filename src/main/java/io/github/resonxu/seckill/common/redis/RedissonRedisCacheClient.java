package io.github.resonxu.seckill.common.redis;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;

/**
 * 基于 Redisson 的 Redis 缓存访问实现。
 */
@Component
@RequiredArgsConstructor
public class RedissonRedisCacheClient implements RedisCacheClient {

    private final RedissonClient redissonClient;
    private final RedisCacheSerializer redisCacheSerializer;

    @Override
    public <T> RedisCacheValue<T> get(String key, Class<T> targetType, String emptyMarker) {
        RBucket<String> bucket = redissonClient.getBucket(key, StringCodec.INSTANCE);
        String cachedValue = bucket.get();

        if (cachedValue == null) {
            return RedisCacheValue.miss();
        }
        if (emptyMarker.equals(cachedValue)) {
            return RedisCacheValue.emptyMarker();
        }

        try {
            return RedisCacheValue.hit(redisCacheSerializer.deserialize(cachedValue, targetType));
        } catch (IllegalStateException exception) {
            // 非法缓存视为脏数据，直接删除并按未命中处理。
            bucket.delete();
            return RedisCacheValue.miss();
        }
    }

    @Override
    public void set(String key, Object value, Duration ttl) {
        String serializedValue = redisCacheSerializer.serialize(value);
        redissonClient.getBucket(key, StringCodec.INSTANCE)
                .set(serializedValue, ttl.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void setString(String key, String value, Duration ttl) {
        redissonClient.getBucket(key, StringCodec.INSTANCE)
                .set(value, ttl.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean delete(String key) {
        return redissonClient.getBucket(key, StringCodec.INSTANCE).delete();
    }
}
