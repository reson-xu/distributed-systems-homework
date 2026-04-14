package io.github.resonxu.seckill.user.interfaces.vo;

import lombok.Builder;
import lombok.Value;

/**
 * 用户登录结果视图对象。
 */
@Value
@Builder
public class AuthLoginVO {

    Long userId;

    String username;

    String token;
}
