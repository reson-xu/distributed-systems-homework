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
     * 按订单ID和用户ID查询订单。
     *
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 订单
     */
    SeckillOrder findByIdAndUserId(Long orderId, Long userId);

    /**
     * 分页查询用户订单列表。
     *
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 查询条数
     * @return 订单列表
     */
    java.util.List<SeckillOrder> listByUserId(Long userId, int offset, int limit);

    /**
     * 新增订单记录。
     *
     * @param seckillOrder 秒杀订单
     * @return 影响行数
     */
    int insert(SeckillOrder seckillOrder);

    /**
     * 条件更新订单状态。
     *
     * @param orderId 订单ID
     * @param expectedStatus 期望旧状态
     * @param targetStatus 目标状态
     * @param failReason 失败原因
     * @return 影响行数
     */
    int updateStatus(Long orderId, Integer expectedStatus, Integer targetStatus, String failReason);
}
