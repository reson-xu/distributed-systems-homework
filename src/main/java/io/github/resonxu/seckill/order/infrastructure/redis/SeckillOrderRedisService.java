package io.github.resonxu.seckill.order.infrastructure.redis;

import io.github.resonxu.seckill.common.redis.RedisCacheClient;
import io.github.resonxu.seckill.common.redis.RedisKeyConstants;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;

/**
 * 秒杀下单 Redis 热路径服务。
 */
@Component
@RequiredArgsConstructor
public class SeckillOrderRedisService {

    private static final String RESERVE_STOCK_LUA = """
            local stock = redis.call('GET', KEYS[1])
            if not stock then
                return 3
            end
            if redis.call('EXISTS', KEYS[2]) == 1 then
                return 2
            end
            if tonumber(stock) <= 0 then
                return 1
            end
            redis.call('DECR', KEYS[1])
            redis.call('SET', KEYS[2], ARGV[1], 'PX', ARGV[2])
            return 0
            """;

    private static final String ROLLBACK_STOCK_LUA = """
            if redis.call('EXISTS', KEYS[2]) == 0 then
                return 0
            end
            redis.call('INCR', KEYS[1])
            redis.call('DEL', KEYS[2])
            return 1
            """;

    private final RedissonClient redissonClient;
    private final RedisCacheClient redisCacheClient;

    /**
     * 秒杀预扣库存并写入用户占位。
     *
     * @param productId 商品ID
     * @param userId 用户ID
     * @param initialStock 初始库存
     * @return 预扣结果
     */
    public ReserveResult reserve(Long productId, Long userId, int initialStock) {
        ReserveResult reserveResult = executeReserveScript(productId, userId);
        if (reserveResult != ReserveResult.STOCK_NOT_INITIALIZED) {
            return reserveResult;
        }
        initializeStockIfAbsent(productId, initialStock);
        return executeReserveScript(productId, userId);
    }

    /**
     * 回滚 Redis 预扣库存和用户占位。
     *
     * @param productId 商品ID
     * @param userId 用户ID
     */
    public void rollback(Long productId, Long userId) {
        redissonClient.getScript(StringCodec.INSTANCE).eval(
                RScript.Mode.READ_WRITE,
                ROLLBACK_STOCK_LUA,
                RScript.ReturnType.INTEGER,
                List.of(
                        RedisKeyConstants.buildSeckillStockKey(productId),
                        RedisKeyConstants.buildSeckillUserOrderKey(productId, userId)
                )
        );
    }

    private ReserveResult executeReserveScript(Long productId, Long userId) {
        Number executeResult = redissonClient.getScript(StringCodec.INSTANCE).eval(
                RScript.Mode.READ_WRITE,
                RESERVE_STOCK_LUA,
                RScript.ReturnType.INTEGER,
                List.of(
                        RedisKeyConstants.buildSeckillStockKey(productId),
                        RedisKeyConstants.buildSeckillUserOrderKey(productId, userId)
                ),
                String.valueOf(userId),
                String.valueOf(RedisKeyConstants.SECKILL_USER_ORDER_TTL.toMillis())
        );
        return ReserveResult.fromCode(executeResult == null ? -1 : executeResult.intValue());
    }

    private void initializeStockIfAbsent(Long productId, int initialStock) {
        if (initialStock <= 0) {
            return;
        }
        redisCacheClient.setIfAbsentString(
                RedisKeyConstants.buildSeckillStockKey(productId),
                String.valueOf(initialStock),
                RedisKeyConstants.SECKILL_STOCK_TTL
        );
    }

    /**
     * 秒杀预扣结果。
     */
    public enum ReserveResult {
        SUCCESS(0),
        OUT_OF_STOCK(1),
        REPEAT_ORDER(2),
        STOCK_NOT_INITIALIZED(3),
        UNKNOWN(-1);

        private final int code;

        ReserveResult(int code) {
            this.code = code;
        }

        public static ReserveResult fromCode(int code) {
            for (ReserveResult value : values()) {
                if (value.code == code) {
                    return value;
                }
            }
            return UNKNOWN;
        }
    }
}
