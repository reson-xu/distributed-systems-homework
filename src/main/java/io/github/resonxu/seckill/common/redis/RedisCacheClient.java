package io.github.resonxu.seckill.common.redis;

import java.time.Duration;

/**
 * Redis 缓存访问接口。
 */
public interface RedisCacheClient {

    /**
     * 读取缓存值，并识别空值标记。
     *
     * @param key 缓存 Key
     * @param targetType 目标类型
     * @param emptyMarker 空值标记
     * @param <T> 缓存值类型
     * @return 缓存查询结果
     */
    <T> RedisCacheValue<T> get(String key, Class<T> targetType, String emptyMarker);

    /**
     * 写入对象缓存。
     *
     * @param key 缓存 Key
     * @param value 缓存值
     * @param ttl 过期时间
     */
    void set(String key, Object value, Duration ttl);

    /**
     * 写入字符串缓存。
     *
     * @param key 缓存 Key
     * @param value 字符串值
     * @param ttl 过期时间
     */
    void setString(String key, String value, Duration ttl);

    /**
     * 仅在 Key 不存在时写入字符串缓存。
     *
     * @param key 缓存 Key
     * @param value 字符串值
     * @param ttl 过期时间
     * @return true 表示写入成功
     */
    boolean setIfAbsentString(String key, String value, Duration ttl);

    /**
     * 删除缓存。
     *
     * @param key 缓存 Key
     * @return true 表示删除成功
     */
    boolean delete(String key);
}
