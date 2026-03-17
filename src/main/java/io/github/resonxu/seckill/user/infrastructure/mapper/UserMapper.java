package io.github.resonxu.seckill.user.infrastructure.mapper;

import io.github.resonxu.seckill.user.domain.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User selectActiveByUsername(@Param("username") String username);

    int insert(User user);
}
