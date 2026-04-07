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
    int insert(SeckillOrder seckillOrder);
}
