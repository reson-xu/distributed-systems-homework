package io.github.resonxu.seckill.order.config;

import java.time.Duration;

/**
 * 订单服务 Redis Key 定义。
 */
public final class OrderRedisKeys {

    private static final String KEY_PREFIX = "seckill";
    private static final String SEPARATOR = ":";
    private static final String SECKILL = "seckill";
    private static final String STOCK = "stock";
    private static final String ORDER = "order";
    private static final String USER = "user";

    public static final Duration SECKILL_STOCK_TTL = Duration.ofDays(7);
    public static final Duration SECKILL_USER_ORDER_TTL = Duration.ofDays(1);

    private OrderRedisKeys() {
    }

    /**
     * 构造秒杀库存 Key。
     *
     * @param productId 商品ID
     * @return 秒杀库存 Key
     */
    public static String buildSeckillStockKey(Long productId) {
        return String.join(SEPARATOR, KEY_PREFIX, SECKILL, STOCK, String.valueOf(productId));
    }

    /**
     * 构造用户秒杀占位 Key。
     *
     * @param productId 商品ID
     * @param userId 用户ID
     * @return 用户秒杀占位 Key
     */
    public static String buildSeckillUserOrderKey(Long productId, Long userId) {
        return String.join(SEPARATOR, KEY_PREFIX, SECKILL, ORDER, USER,
                String.valueOf(productId), String.valueOf(userId));
    }
}
