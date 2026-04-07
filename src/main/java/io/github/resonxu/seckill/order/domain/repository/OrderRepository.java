package io.github.resonxu.seckill.order.domain.repository;

import io.github.resonxu.seckill.order.domain.model.SeckillOrder;

/**
 * 订单领域仓储。
 */
public interface OrderRepository {

    /**
     * 根据订单ID查询订单。
     *
     * @param orderId 订单ID
     * @return 订单，不存在时返回 null
     */
    SeckillOrder findById(Long orderId);

    /**
     * 新增订单记录。
     *
     * @param seckillOrder 秒杀订单
     * @return 影响行数
     */
    int insert(SeckillOrder seckillOrder);
}
