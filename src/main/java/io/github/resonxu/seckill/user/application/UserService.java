package io.github.resonxu.seckill.user.application;

import io.github.resonxu.seckill.user.domain.model.User;
import io.github.resonxu.seckill.user.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    /**
     * 根据用户名查询有效用户
     *
     * @param username 用户名
     * @return 用户实体，不存在时返回 null
     */
    public User findActiveByUsername(String username) {
        return userMapper.selectActiveByUsername(username);
    }

    /**
     * 创建用户
     *
     * @param user 用户实体
     * @return 创建后的用户实体
     */
    public User create(User user) {
        userMapper.insert(user);
        return user;
    }
}
