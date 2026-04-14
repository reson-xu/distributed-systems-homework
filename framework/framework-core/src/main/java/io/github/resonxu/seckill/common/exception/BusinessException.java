package io.github.resonxu.seckill.common.exception;

import io.github.resonxu.seckill.common.response.ResultCode;
import lombok.Getter;

/**
 * 统一业务异常。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String code;

    /**
     * 创建业务异常。
     *
     * @param resultCode 结果码
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 创建带自定义消息的业务异常。
     *
     * @param resultCode 结果码
     * @param message 异常消息
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
}
