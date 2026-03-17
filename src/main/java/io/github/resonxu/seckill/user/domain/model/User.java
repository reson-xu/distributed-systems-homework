package io.github.resonxu.seckill.user.domain.model;

import java.time.LocalDateTime;
import lombok.Data;

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
