package io.github.resonxu.seckill.user.domain.model;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户领域模型。
 */
@Data
public class User {

    private Long id;

    private String username;

    private String password;

    private Integer status;

    private Integer isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
