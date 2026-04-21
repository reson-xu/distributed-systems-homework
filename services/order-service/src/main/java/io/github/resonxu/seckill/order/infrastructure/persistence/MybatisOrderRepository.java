package io.github.resonxu.seckill.order.infrastructure.persistence;

import io.github.resonxu.seckill.order.domain.model.SeckillOrder;
import io.github.resonxu.seckill.order.domain.repository.OrderRepository;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 基于 MyBatis 的订单仓储实现。
 */
@Mapper
public interface MybatisOrderRepository extends OrderRepository {

    @Override
    SeckillOrder findById(@Param("orderId") Long orderId);

    @Override
    SeckillOrder findByIdAndUserId(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId
    );

    @Override
    java.util.List<SeckillOrder> listByUserId(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    @Override
    int insert(SeckillOrder seckillOrder);

    @Override
    int updateStatus(
            @Param("orderId") Long orderId,
            @Param("expectedStatus") Integer expectedStatus,
            @Param("targetStatus") Integer targetStatus,
            @Param("failReason") String failReason
    );
}
