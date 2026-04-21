package io.github.resonxu.seckill.user.interfaces.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀资格视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillEligibilityVO {

    private Long userId;

    private Boolean eligible;

    private String reason;
}
