package io.github.resonxu.seckill.user.domain.repository;

import io.github.resonxu.seckill.user.domain.model.User;

/**
 * 用户领域仓储。
 */
public interface UserRepository {

    /**
     * 根据用户名查询有效用户。
     *
     * @param username 用户名
     * @return 用户实体，不存在时返回 null
     */
    User findActiveByUsername(String username);

    /**
     * 保存用户。
     *
     * @param user 用户实体
     * @return 保存后的用户实体
     */
    User save(User user);
}
