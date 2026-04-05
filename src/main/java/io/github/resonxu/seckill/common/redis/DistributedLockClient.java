package io.github.resonxu.seckill.common.redis;

import java.time.Duration;

/**
 * 分布式锁访问接口。
 */
public interface DistributedLockClient {

    /**
     * 尝试获取分布式锁。
     *
     * @param key 锁 Key
     * @param waitTime 等待时间
     * @param leaseTime 自动释放时间
     * @return true 表示获取成功
     */
    boolean tryLock(String key, Duration waitTime, Duration leaseTime);

    /**
     * 释放当前线程持有的锁。
     *
     * @param key 锁 Key
     */
    void unlock(String key);
}
