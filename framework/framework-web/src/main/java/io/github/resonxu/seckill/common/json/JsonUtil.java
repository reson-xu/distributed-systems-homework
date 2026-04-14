package io.github.resonxu.seckill.common.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * JSON 序列化与反序列化工具。
 */
@Component
@RequiredArgsConstructor
public class JsonUtil {

    private final ObjectMapper objectMapper;

    /**
     * 将对象序列化为 JSON 字符串。
     *
     * @param value 待序列化对象
     * @return JSON 字符串
     */
    public String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize object to json", exception);
        }
    }

    /**
     * 将 JSON 字符串反序列化为目标对象。
     *
     * @param value JSON 字符串
     * @param targetType 目标类型
     * @param <T> 目标泛型
     * @return 反序列化对象
     */
    public <T> T fromJson(String value, Class<T> targetType) {
        try {
            return objectMapper.readValue(value, targetType);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to deserialize json to object", exception);
        }
    }
}
