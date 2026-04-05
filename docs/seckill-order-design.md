# 秒杀下单方案设计

## 1. 目标

本文档用于定义秒杀下单功能的第一版技术方案，目标如下：

- 支持高并发秒杀请求接入
- 使用 Redis 承担秒杀热路径流量
- 使用 RocketMQ 异步创建订单，实现削峰填谷
- 使用雪花算法生成全局订单 ID
- 支持按订单 ID 或用户 ID 查询订单
- 防止重复下单，满足同一用户同一商品只能秒杀一次
- 保证最终库存不超卖，订单数据完整

当前方案采用：

`Redis 预扣库存 + RocketMQ 异步创建订单 + MySQL 最终一致性`

## 2. 设计原则

- 正确性优先于吞吐
- Redis 负责高并发前置拦截，不作为最终真相
- MySQL 负责订单和库存的最终一致性
- 消息队列负责削峰和异步解耦
- 所有关键写路径都必须考虑幂等
- 所有失败路径都必须具备补偿能力

## 3. 总体架构

### 3.1 关键组件

- `OrderController`：接收秒杀下单请求
- `OrderApplicationService`：编排秒杀下单流程
- `Redis`：秒杀热路径校验、库存预扣、一人一单占位
- `RocketMQ`：异步创建订单、补偿和后续取消事件
- `OrderConsumer`：消费下单消息并落库
- `InventoryService`：执行数据库库存确认或扣减
- `OrderService`：创建订单、维护订单状态
- `MySQL`：订单与库存最终状态存储

### 3.2 高层时序

1. 用户发起秒杀请求
2. 服务端执行参数校验、活动状态校验
3. Redis Lua 脚本原子执行资格校验和库存预扣
4. 生成雪花订单 ID
5. 投递 RocketMQ 下单消息
6. Consumer 异步消费并创建订单
7. 数据库库存与订单落地成功后更新订单状态
8. 用户通过订单查询接口获取最终状态

## 4. 秒杀下单主链路

### 4.1 同步请求阶段

接口建议：

- `POST /api/v1/seckill/orders`

请求体建议包含：

- `productId`
- `activityId`
- `requestId`

同步阶段建议执行以下逻辑：

1. 校验用户登录态
2. 校验参数完整性
3. 校验商品和秒杀活动基础信息
4. 调用 Redis Lua 脚本完成原子预扣
5. 若 Redis 预扣成功，则生成订单 ID
6. 发送下单消息到 RocketMQ
7. 返回“已受理”结果和订单 ID

同步接口不建议等待数据库订单落库完成，否则会丢失异步削峰的意义。

### 4.2 异步消费阶段

下单消息 Consumer 建议执行以下逻辑：

1. 解析消息体
2. 校验消息幂等
3. 校验订单是否已存在
4. 执行数据库层库存确认或扣减
5. 创建订单记录
6. 更新订单状态为 `CREATED`
7. 记录消费成功结果

若任一步骤失败，则进入重试或补偿流程。

## 5. Redis 设计

### 5.1 Redis 在秒杀中的职责

- 承接高并发请求入口
- 原子校验库存
- 原子校验一人一单
- 预扣库存
- 写入用户抢购占位
- 可选记录异步处理状态

### 5.2 推荐 Key 设计

- `seckill:activity:{activityId}`
  - 秒杀活动元信息
- `seckill:stock:{productId}`
  - 商品秒杀库存
- `seckill:order:user:{activityId}:{productId}:{userId}`
  - 一人一单占位标记
- `seckill:order:result:{orderId}`
  - 秒杀请求处理结果，可选

### 5.3 Lua 原子脚本

Lua 脚本建议一次完成以下动作：

1. 判断活动是否存在并有效
2. 判断库存是否足够
3. 判断用户是否已下单
4. 扣减库存
5. 写入用户下单占位标记

推荐返回码：

- `0`：成功
- `1`：库存不足
- `2`：重复下单
- `3`：活动不存在或不在有效时间内
- `4`：系统繁忙或 Redis 执行异常

这样业务层只需要解释返回码，不在 Java 侧拼并发判断逻辑。

## 6. RocketMQ 设计

### 6.1 选型说明

虽然题目示例中提到了 Kafka，但当前项目已经引入 RocketMQ，因此本方案统一采用 RocketMQ。

RocketMQ 适用于当前场景的原因：

- 适合订单类异步业务
- 便于后续补偿和延迟取消扩展
- 与业务型消息流更契合

### 6.2 Topic 规划

第一版建议预留以下 Topic：

- `seckill-order-create`
  - 秒杀下单主消息
- `seckill-order-cancel`
  - 超时取消或失败回滚事件
- `seckill-inventory-compensate`
  - 库存补偿事件

### 6.3 Consumer Group 规划

- `seckill-order-create-consumer-group`
- `seckill-order-cancel-consumer-group`
- `seckill-inventory-compensate-consumer-group`

### 6.4 消息体字段建议

下单消息体建议包含：

- `messageId`
- `orderId`
- `requestId`
- `userId`
- `productId`
- `activityId`
- `requestTime`
- `traceId`

## 7. 订单 ID 方案

建议使用雪花算法生成业务订单号。

原因如下：

- 不依赖数据库自增
- 可在高并发下本地快速生成
- 全局唯一
- 大致有序，方便查询与排障
- 适合异步消息链路

建议提供独立组件：

- `OrderIdGenerator`

可通过配置注入：

- `workerId`
- `datacenterId`

## 8. 数据库设计建议

### 8.1 订单表

`t_order` 建议在现有基础上补充或明确以下字段：

- `order_id`
- `user_id`
- `product_id`
- `activity_id`
- `order_amount`
- `order_status`
- `request_id`
- `create_source`
- `is_deleted`
- `created_at`
- `updated_at`

关键索引建议：

- 唯一索引：`uk_order_id(order_id)`
- 唯一索引：`uk_user_activity_product(user_id, activity_id, product_id)`

如果当前阶段不引入 `activity_id`，可退化为：

- 唯一索引：`uk_user_product(user_id, product_id)`

### 8.2 库存表

`t_inventory` 建议明确以下字段：

- `product_id`
- `total_stock`
- `available_stock`
- `locked_stock`
- `updated_at`

说明：

- `available_stock`：当前可抢库存
- `locked_stock`：预留给异步订单确认或支付确认的冻结库存

### 8.3 库存流水表

建议新增库存流水表 `t_inventory_flow`，记录：

- 业务单号
- 商品 ID
- 操作类型
- 操作数量
- 操作前后库存
- 来源事件
- 创建时间

用途：

- 便于排查超卖问题
- 支撑补偿逻辑追踪
- 作为库存审计依据

## 9. 订单状态设计

建议订单状态至少包含：

- `PENDING_CREATE`
- `CREATED`
- `PAID`
- `CANCELLED`
- `FAILED`

推荐流转：

1. 秒杀请求受理后形成 `PENDING_CREATE`
2. MQ 消费成功后转为 `CREATED`
3. 支付成功后转为 `PAID`
4. 超时取消或补偿回滚后转为 `CANCELLED`
5. 无法恢复的失败转为 `FAILED`

## 10. 幂等设计

幂等必须覆盖三层。

### 10.1 接口层幂等

- 请求携带 `requestId`
- Redis 或数据库记录请求幂等标记
- 重试请求不能重复提交新订单

### 10.2 业务层幂等

- Redis 占位保证一人一单
- 数据库唯一索引兜底

### 10.3 消费层幂等

- 记录 `messageId` 或消费记录
- 重复消息直接忽略
- Consumer 重试不允许重复创建订单

## 11. 一致性设计

### 11.1 一致性原则

- Redis 不是最终真相
- MySQL 是订单和库存最终真相
- RocketMQ 用于异步缓冲和最终一致性驱动

### 11.2 防超卖关键点

必须同时满足以下条件：

- Redis Lua 原子预扣库存
- Redis Lua 原子校验一人一单
- 数据库库存更新必须带条件约束
- 数据库唯一索引防止重复订单
- MQ 消费逻辑具备幂等性
- 失败时有库存补偿和占位清理

只做 Redis 预扣不足以保证最终一致性，只做数据库扣减又无法承载秒杀流量。

## 12. 补偿设计

### 12.1 需要补偿的场景

- RocketMQ 下单消息消费失败
- 订单落库失败
- 数据库库存确认失败
- 订单创建成功但后续流程回滚

### 12.2 补偿动作

补偿建议通过事件触发：

1. 发送库存补偿消息
2. 回补 Redis 预扣库存
3. 清理用户一人一单占位
4. 更新订单状态为 `FAILED` 或 `CANCELLED`
5. 记录补偿日志和失败原因

## 13. 查询设计

### 13.1 按订单 ID 查询

建议接口：

- `GET /api/v1/orders/{orderId}`

查询来源：

- 直接查 MySQL 订单表

若异步订单尚未落库，可返回：

- `PENDING_CREATE`

### 13.2 按用户 ID 查询

建议接口：

- `GET /api/v1/users/{userId}/orders`

查询方式：

- 基于 `user_id` 索引分页查询

第一版不需要额外引入 ES 或二级检索系统。

## 14. 推荐落地顺序

建议分三步实施。

### 第一步：基础设施

- 接入 RocketMQ
- 接入 Redis Lua 执行能力
- 实现雪花订单号生成器

### 第二步：主链路打通

- 实现秒杀下单接口
- 实现 Redis 预扣和一人一单校验
- 实现下单消息投递
- 实现 Consumer 创建订单
- 实现按订单 ID 查询

### 第三步：可靠性增强

- 补全消费幂等
- 引入补偿消息
- 增加库存流水
- 增加订单超时取消
- 增加监控和告警

## 15. 第一版范围建议

第一版建议包含：

- 秒杀下单接口
- 雪花订单号生成
- Redis Lua 原子预扣
- RocketMQ 下单消息发送
- 下单 Consumer 创建订单
- 订单查询接口
- 失败补偿基础链路

第一版暂不包含：

- 支付能力
- 延迟取消订单
- 分库分表
- 复杂活动运营能力
- 布隆过滤器
- 多级缓存

## 16. 风险与注意事项

- Redis 预扣成功但 MQ 发送失败时，必须处理库存回滚
- Consumer 重复消费必须保证幂等
- 宿主机直连 Docker RocketMQ 时要注意 broker 对外地址配置
- 一人一单约束要同时由 Redis 和数据库保证
- 不应让控制器直接承担 Lua 或 MQ 细节，必须通过应用服务编排

## 17. 结论

当前项目秒杀下单推荐方案为：

`Redis 预扣库存 + RocketMQ 异步创建订单 + MySQL 最终一致性`

这是兼顾当前项目复杂度、可落地性和后续扩展性的方案。后续如需支持支付超时取消、库存补偿增强和更多活动能力，可在本方案基础上继续扩展。
