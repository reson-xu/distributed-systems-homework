package io.github.resonxu.seckill.common.redis;

import java.time.Duration;

public final class RedisKeyConstants {

    private static final String KEY_PREFIX = "seckill";
    private static final String SEPARATOR = ":";
    private static final String PRODUCT = "product";
    private static final String DETAIL = "detail";
    private static final String LOCK = "lock";
    private static final String SECKILL = "seckill";
    private static final String STOCK = "stock";
    private static final String ORDER = "order";
    private static final String USER = "user";
    private static final String REQUEST = "request";

    public static final String EMPTY_CACHE_VALUE = "EMPTY";
    public static final Duration PRODUCT_DETAIL_TTL = Duration.ofMinutes(10);
    public static final Duration PRODUCT_DETAIL_TTL_JITTER = Duration.ofMinutes(5);
    public static final Duration PRODUCT_DETAIL_EMPTY_TTL = Duration.ofMinutes(2);
    public static final Duration PRODUCT_DETAIL_CACHE_REBUILD_MAX_WAIT = Duration.ofMillis(200);
    public static final Duration PRODUCT_DETAIL_LOCK_LEASE_TIME = Duration.ofSeconds(5);
    public static final Duration PRODUCT_DETAIL_CACHE_REBUILD_POLL_INTERVAL = Duration.ofMillis(50);
    public static final Duration SECKILL_STOCK_TTL = Duration.ofDays(7);
    public static final Duration SECKILL_USER_ORDER_TTL = Duration.ofDays(1);
    public static final Duration IDEMPOTENT_REQUEST_TTL = Duration.ofSeconds(10);

    private RedisKeyConstants() {
    }

    /**
     * Redis Key 统一规范：seckill:业务域:数据类型:业务主键
     *
     * @param productId 商品ID
     * @return 商品详情缓存 Key
     */
    public static String buildProductDetailKey(Long productId) {
        return String.join(SEPARATOR, KEY_PREFIX, PRODUCT, DETAIL, String.valueOf(productId));
    }

    /**
     * Redis Key 统一规范：seckill:业务域:数据类型:lock:业务主键
     *
     * @param productId 商品ID
     * @return 商品详情缓存重建锁 Key
     */
    public static String buildProductDetailLockKey(Long productId) {
        return String.join(SEPARATOR, KEY_PREFIX, PRODUCT, DETAIL, LOCK, String.valueOf(productId));
    }

    /**
     * 秒杀库存 Key。
     *
     * @param productId 商品ID
     * @return 秒杀库存 Key
     */
    public static String buildSeckillStockKey(Long productId) {
        return String.join(SEPARATOR, KEY_PREFIX, SECKILL, STOCK, String.valueOf(productId));
    }

    /**
     * 用户秒杀占位 Key。
     *
     * @param productId 商品ID
     * @param userId 用户ID
     * @return 用户秒杀占位 Key
     */
    public static String buildSeckillUserOrderKey(Long productId, Long userId) {
        return String.join(SEPARATOR, KEY_PREFIX, SECKILL, ORDER, USER,
                String.valueOf(productId), String.valueOf(userId));
    }

    /**
     * 幂等请求 Key。
     *
     * @param scene 业务场景
     * @param uniqueKey 唯一键
     * @return 幂等请求 Key
     */
    public static String buildIdempotentRequestKey(String scene, String uniqueKey) {
        return String.join(SEPARATOR, KEY_PREFIX, REQUEST, scene, uniqueKey);
    }
}
