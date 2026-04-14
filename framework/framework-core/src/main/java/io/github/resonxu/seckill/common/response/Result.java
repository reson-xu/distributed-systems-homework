package io.github.resonxu.seckill.common.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应包装对象。
 *
 * @param <T> 响应数据类型
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Result<T> {

    private final String code;
    private final String message;
    private final T data;

    /**
     * 创建成功结果。
     *
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 创建无数据成功结果。
     *
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 创建预定义失败结果。
     *
     * @param resultCode 结果码
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 创建自定义失败结果。
     *
     * @param code 业务错误码
     * @param message 业务错误信息
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> Result<T> fail(String code, String message) {
        return new Result<>(code, message, null);
    }
}
