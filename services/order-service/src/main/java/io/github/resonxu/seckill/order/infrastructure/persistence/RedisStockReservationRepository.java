package io.github.resonxu.seckill.order.infrastructure.persistence;

import io.github.resonxu.seckill.order.config.OrderRedisKeys;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

/**
 * 秒杀下单 Redis 热路径服务。
 */
@Component
@RequiredArgsConstructor
public class RedisStockReservationRepository {

    private static final DefaultRedisScript<Long> RESERVE_STOCK_SCRIPT =
            buildScript("lua/seckill_reserve_stock.lua");
    private static final DefaultRedisScript<Long> ROLLBACK_STOCK_SCRIPT =
            buildScript("lua/seckill_rollback_stock.lua");

    private final StringRedisTemplate stringRedisTemplate;

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
        stringRedisTemplate.execute(
                ROLLBACK_STOCK_SCRIPT,
                List.of(
                        OrderRedisKeys.buildSeckillStockKey(productId),
                        OrderRedisKeys.buildSeckillUserOrderKey(productId, userId)
                )
        );
    }

    private ReserveResult executeReserveScript(Long productId, Long userId) {
        Long executeResult = stringRedisTemplate.execute(
                RESERVE_STOCK_SCRIPT,
                List.of(
                        OrderRedisKeys.buildSeckillStockKey(productId),
                        OrderRedisKeys.buildSeckillUserOrderKey(productId, userId)
                ),
                String.valueOf(userId),
                String.valueOf(OrderRedisKeys.SECKILL_USER_ORDER_TTL.toMillis())
        );
        return ReserveResult.fromCode(executeResult == null ? -1 : executeResult.intValue());
    }

    private void initializeStockIfAbsent(Long productId, int initialStock) {
        if (initialStock <= 0) {
            return;
        }
        stringRedisTemplate.opsForValue().setIfAbsent(
                OrderRedisKeys.buildSeckillStockKey(productId),
                String.valueOf(initialStock),
                OrderRedisKeys.SECKILL_STOCK_TTL
        );
    }

    private static DefaultRedisScript<Long> buildScript(String path) {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setLocation(new ClassPathResource(path));
        redisScript.setResultType(Long.class);
        return redisScript;
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

        /**
         * 根据返回码解析预扣结果。
         *
         * @param code 返回码
         * @return 预扣结果
         */
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
