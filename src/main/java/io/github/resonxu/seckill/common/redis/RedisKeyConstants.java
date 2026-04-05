package io.github.resonxu.seckill.common.redis;

import java.time.Duration;

public final class RedisKeyConstants {

    private static final String KEY_PREFIX = "seckill";
    private static final String SEPARATOR = ":";
    private static final String PRODUCT = "product";
    private static final String DETAIL = "detail";
    private static final String LOCK = "lock";

    public static final String EMPTY_CACHE_VALUE = "EMPTY";
    public static final Duration PRODUCT_DETAIL_TTL = Duration.ofMinutes(10);
    public static final Duration PRODUCT_DETAIL_TTL_JITTER = Duration.ofMinutes(5);
    public static final Duration PRODUCT_DETAIL_EMPTY_TTL = Duration.ofMinutes(2);
    public static final Duration PRODUCT_DETAIL_CACHE_REBUILD_MAX_WAIT = Duration.ofMillis(200);
    public static final Duration PRODUCT_DETAIL_LOCK_LEASE_TIME = Duration.ofSeconds(5);
    public static final Duration PRODUCT_DETAIL_CACHE_REBUILD_POLL_INTERVAL = Duration.ofMillis(50);

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
}
