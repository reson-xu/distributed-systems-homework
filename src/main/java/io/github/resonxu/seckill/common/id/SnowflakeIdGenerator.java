package io.github.resonxu.seckill.common.id;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 * 雪花算法订单号生成器。
 */
@Component
public class SnowflakeIdGenerator {

    private static final long EPOCH = 1704067200000L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    private final long workerId = 1L;
    private final long datacenterId = 1L;
    private final AtomicLong sequence = new AtomicLong(0L);

    private long lastTimestamp = -1L;

    /**
     * 生成全局订单号。
     *
     * @return 全局唯一订单号
     */
    public synchronized long nextId() {
        validateNodeId();
        long currentTimestamp = currentTimestamp();
        if (currentTimestamp < lastTimestamp) {
            throw new IllegalStateException("clock moved backwards");
        }

        if (currentTimestamp == lastTimestamp) {
            long currentSequence = (sequence.incrementAndGet()) & SEQUENCE_MASK;
            if (currentSequence == 0L) {
                currentTimestamp = waitNextMillis(currentTimestamp);
            }
        } else {
            sequence.set(0L);
        }

        lastTimestamp = currentTimestamp;
        return ((currentTimestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence.get();
    }

    private void validateNodeId() {
        if (workerId > MAX_WORKER_ID || datacenterId > MAX_DATACENTER_ID) {
            throw new IllegalStateException("snowflake node id out of range");
        }
    }

    private long waitNextMillis(long currentTimestamp) {
        long nextTimestamp = currentTimestamp();
        while (nextTimestamp <= currentTimestamp) {
            nextTimestamp = currentTimestamp();
        }
        sequence.set(0L);
        return nextTimestamp;
    }

    private long currentTimestamp() {
        return Instant.now().toEpochMilli();
    }
}
