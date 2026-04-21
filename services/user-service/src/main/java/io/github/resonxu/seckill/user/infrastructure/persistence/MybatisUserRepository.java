package io.github.resonxu.seckill.user.infrastructure.persistence;

import io.github.resonxu.seckill.user.domain.model.User;
import io.github.resonxu.seckill.user.domain.repository.UserRepository;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 基于 MyBatis 的用户仓储实现。
 */
@Mapper
public interface MybatisUserRepository extends UserRepository {

    @Override
    User findById(@Param("userId") Long userId);

    @Override
    User findActiveByUsername(@Param("username") String username);

    /**
     * 插入用户记录。
     *
     * @param user 用户实体
     * @return 影响行数
     */
    int insert(User user);

    @Override
    default User save(User user) {
        insert(user);
        return user;
    }
}
