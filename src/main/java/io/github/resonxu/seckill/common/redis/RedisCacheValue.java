package io.github.resonxu.seckill.common.redis;

/**
 * Redis 缓存查询结果。
 *
 * @param <T> 缓存值类型
 */
public record RedisCacheValue<T>(T value, boolean exists, boolean emptyMarker) {

    /**
     * 创建缓存未命中的结果。
     *
     * @param <T> 缓存值类型
     * @return 未命中结果
     */
    public static <T> RedisCacheValue<T> miss() {
        return new RedisCacheValue<>(null, false, false);
    }

    /**
     * 创建空值标记结果。
     *
     * @param <T> 缓存值类型
     * @return 空值标记结果
     */
    public static <T> RedisCacheValue<T> emptyMarker() {
        return new RedisCacheValue<>(null, true, true);
    }

    /**
     * 创建正常缓存结果。
     *
     * @param value 缓存值
     * @param <T> 缓存值类型
     * @return 正常缓存结果
     */
    public static <T> RedisCacheValue<T> hit(T value) {
        return new RedisCacheValue<>(value, true, false);
    }

    /**
     * 是否命中正常缓存值。
     *
     * @return true 表示命中正常值
     */
    public boolean hasValue() {
        return exists && !emptyMarker && value != null;
    }

    /**
     * 是否缓存未命中。
     *
     * @return true 表示缓存不存在
     */
    public boolean isMiss() {
        return !exists;
    }
}
