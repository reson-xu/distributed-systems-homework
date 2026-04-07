package io.github.resonxu.seckill.auth.interfaces.vo;

import lombok.Builder;
import lombok.Value;

/**
 * 用户注册结果视图对象。
 */
@Value
@Builder
public class AuthRegisterVO {

    Long userId;

    String username;
}
