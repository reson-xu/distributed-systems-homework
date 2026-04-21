package io.github.resonxu.seckill.user.interfaces.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthMeVO {

    private Long userId;

    private String username;

    private Integer status;
}
