package io.github.resonxu.seckill.common.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Redis 缓存值序列化组件。
 */
@Component
@RequiredArgsConstructor
public class RedisCacheSerializer {

    private final ObjectMapper objectMapper;

    /**
     * 将对象序列化为 JSON 字符串。
     *
     * @param value 待序列化对象
     * @return JSON 字符串
     */
    public String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize redis cache value", exception);
        }
    }

    /**
     * 将 JSON 字符串反序列化为目标对象。
     *
     * @param value Redis 中保存的 JSON 字符串
     * @param targetType 目标类型
     * @param <T> 目标泛型
     * @return 反序列化后的对象
     */
    public <T> T deserialize(String value, Class<T> targetType) {
        try {
            return objectMapper.readValue(value, targetType);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to deserialize redis cache value", exception);
        }
    }
}
