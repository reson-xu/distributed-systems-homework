package io.github.resonxu.seckill.common.redis;

import io.github.resonxu.seckill.common.exception.BusinessException;
import io.github.resonxu.seckill.common.response.ResultCode;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 * 基于 Redisson 的分布式锁实现。
 */
@Component
@RequiredArgsConstructor
public class RedissonDistributedLockClient implements DistributedLockClient {

    private final RedissonClient redissonClient;

    @Override
    public boolean tryLock(String key, Duration waitTime, Duration leaseTime) {
        RLock lock = redissonClient.getLock(key);
        try {
            return lock.tryLock(waitTime.toMillis(), leaseTime.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "interrupted while acquiring distributed lock");
        }
    }

    @Override
    public void unlock(String key) {
        RLock lock = redissonClient.getLock(key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
