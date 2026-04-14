# 秒杀最终一致性实现文档

## 1. 文档目标

本文档用于指导当前仓库补齐秒杀下单链路中的最终一致性实现，重点保证以下两件事：

- 不超卖
- 订单数据完整

这里的“最终一致性”指：

- 秒杀请求可以先在 Redis 热路径快速受理
- 订单和库存的最终结果必须以 MySQL 为准
- 异步消息、重试、失败补偿都不能破坏库存正确性和订单完整性

## 2. 当前实现现状

当前仓库已经具备以下能力：

- 秒杀下单入口：`POST /api/v1/seckill/orders`
- 接口层请求幂等：基于 `requestId`
- Redis Lua 原子预扣库存
- Redis 一人一单占位
- 雪花算法生成订单 ID
- RocketMQ 下单消息发送

当前缺失的关键闭环：

- 下单消息 Consumer
- MySQL 库存确认扣减
- 订单表真正落库
- 消费幂等记录
- 失败补偿链路
- 订单查询接口
- 数据库层唯一约束兜底

结论：

- 当前实现只能做到“请求受理”
- 还不能证明“最终一致性已经实现”

## 3. 实现目标

本次实现需要满足以下业务目标：

1. 任意并发下都不能超卖
2. 同一用户对同一商品只能成功下单一次
3. RocketMQ 重复投递或消费重试不能生成重复订单
4. Redis 预扣成功但后续处理失败时，必须可补偿
5. 用户可以查询订单最终状态
6. 所有关键失败路径都能追踪和恢复

## 4. 一致性边界

需要明确系统中各组件的职责边界：

- Redis
  - 承担高并发前置拦截
  - 做库存预扣和一人一单占位
  - 不作为最终库存真相
- RocketMQ
  - 承担异步削峰
  - 驱动订单创建和补偿流程
- MySQL
  - 作为订单与库存的最终真相
  - 用唯一约束、条件更新和事务兜底正确性

设计原则：

- Redis 负责快
- MySQL 负责准
- MQ 负责把快路径结果可靠地推进到准路径

## 5. 核心实现思路

系统采用以下模型：

`Redis 预扣 + RocketMQ 异步创建 + MySQL 条件扣减 + 失败补偿`

### 5.1 为什么不能只靠 Redis

只靠 Redis 预扣无法解决以下问题：

- MQ 消息发送成功但消费者失败
- Redis 占位过期后重复请求再次进入
- 服务重启后 Redis 与数据库状态不一致
- 异步重试导致重复落订单

因此数据库层必须承担最终兜底责任。

### 5.2 为什么不能只靠 MySQL

只靠 MySQL 扣减库存虽然理论上能保证正确性，但在秒杀场景下会直接把高并发压力打到数据库，不符合当前系统目标。

因此必须保留 Redis 热路径作为前置削峰。

## 6. 数据模型调整

### 6.1 订单表 `t_order`

建议基于现有表补充以下字段：

- `id`
  - 直接存业务订单 ID，不再依赖自增主键表达业务含义
- `request_id`
  - 接口幂等请求号
- `order_status`
  - 订单状态
- `fail_reason`
  - 失败原因
- `create_source`
  - 创建来源，例如 `SECKILL`

建议增加以下约束：

- 主键或唯一索引：`uk_order_id(id)`
- 唯一索引：`uk_request_id(request_id)`
- 唯一索引：`uk_user_product(user_id, product_id, is_deleted)`

说明：

- `uk_request_id` 用于接口请求幂等兜底
- `uk_user_product` 用于一人一单数据库兜底

### 6.2 库存表 `t_inventory`

建议补充以下字段：

- `locked_stock`
  - 预留给异步处理中间态的冻结库存
- `version`
  - 可选，用于乐观锁扩展

第一版如果不引入 `locked_stock`，也必须具备数据库条件扣减能力：

- `UPDATE t_inventory SET available_stock = available_stock - 1 WHERE product_id = ? AND available_stock > 0 AND is_deleted = 0`

这样数据库层才能真正防超卖。

### 6.3 消费记录表 `t_mq_consume_record`

建议新增消费幂等表：

- `id`
- `biz_type`
- `message_key`
- `consume_status`
- `created_at`
- `updated_at`

唯一索引建议：

- `uk_biz_type_message_key(biz_type, message_key)`

用途：

- 防止 MQ 重复消费重复创建订单
- 记录消费处理结果

### 6.4 库存流水表 `t_inventory_flow`

建议新增库存流水表记录每次库存变化：

- `biz_id`
- `product_id`
- `flow_type`
- `change_count`
- `before_available_stock`
- `after_available_stock`
- `source_event`
- `created_at`

用途：

- 审计库存变化
- 排查超卖
- 支撑补偿排障

## 7. 订单状态机

订单状态建议使用当前枚举并补齐状态流转规则：

- `PENDING_CREATE`
- `CREATED`
- `PAID`
- `CANCELLED`
- `FAILED`

状态流转规则：

1. 秒杀请求受理成功后返回 `PENDING_CREATE`
2. MQ 消费并成功落库后变为 `CREATED`
3. 后续支付成功变为 `PAID`
4. 无法创建订单但已完成回滚补偿时变为 `FAILED`
5. 已创建订单但后续取消时变为 `CANCELLED`

当前阶段先实现：

- `PENDING_CREATE -> CREATED`
- `PENDING_CREATE -> FAILED`

## 8. 主链路实现

### 8.1 同步请求阶段

当前 `SeckillOrderSubmissionAppService` 保留以下职责：

1. 校验商品是否存在且可秒杀
2. 调用 Redis Lua 完成原子预扣和一人一单占位
3. 生成业务订单 ID
4. 发送下单消息到 RocketMQ
5. 返回 `orderId + PENDING_CREATE`

同步阶段仍然不直接访问订单表，不等待数据库完成。

### 8.2 异步消费阶段

需要新增 `OrderCreateConsumer`，职责如下：

1. 接收 `seckill-order-create` 消息
2. 基于 `orderId` 或消息唯一键做消费幂等
3. 查询订单是否已存在
4. 执行数据库事务
5. 数据库条件扣减库存
6. 创建订单记录
7. 写入库存流水
8. 标记消费成功

数据库事务要求：

- 库存扣减和订单创建必须在同一事务内完成
- 任一步骤失败都必须回滚事务

### 8.3 数据库扣减策略

库存扣减必须是条件更新，不能先查后改。

推荐 SQL：

```sql
UPDATE t_inventory
SET available_stock = available_stock - 1
WHERE product_id = #{productId}
  AND available_stock > 0
  AND is_deleted = 0
```

判断受影响行数：

- `1`：扣减成功
- `0`：库存不足或已被并发抢完

只有数据库扣减成功，才允许创建订单。

### 8.4 订单创建策略

订单创建需要保证以下约束：

- 订单 ID 全局唯一
- `requestId` 不重复
- 用户对同一商品不重复下单

推荐顺序：

1. 先扣减数据库库存
2. 再插入订单
3. 若插入订单失败，则回滚整个事务

这样可以避免“库存成功扣减但订单未写入”的脏状态。

## 9. 补偿链路实现

### 9.1 需要补偿的场景

以下场景必须进入补偿：

- MQ 消费多次重试后仍失败
- 数据库库存扣减失败
- 订单插入失败
- 消费业务出现不可恢复异常

### 9.2 补偿动作

第一版补偿建议最小化实现：

1. 发送库存补偿消息，或直接调用补偿服务
2. 回补 Redis 库存
3. 清理 Redis 一人一单占位
4. 将订单状态记为 `FAILED`，如果订单尚未入库，则至少写补偿日志

当前仓库已有 Redis 回滚 Lua：

- 回补 `seckill:stock:{productId}`
- 删除 `seckill:order:user:{productId}:{userId}`

这部分应抽象成统一补偿服务，不仅在发送 MQ 失败时调用，也要在消费失败后的终态补偿时调用。

### 9.3 补偿触发方式

第一版推荐方案：

- 下单 Consumer 在确定最终失败后发送 `seckill-inventory-compensate` 消息
- 补偿 Consumer 执行 Redis 回滚

这样可以把主消费链路和补偿链路解耦。

如果暂时不增加第二个 Consumer，也可以先在主消费失败终态时直接回滚 Redis，但后续仍建议改成独立补偿事件。

## 10. 幂等设计

幂等要覆盖三层。

### 10.1 接口层幂等

当前已存在：

- 基于 `requestId` 的 `@Idempotent`

但这只是短期防重，不能替代数据库兜底。

因此数据库必须补充：

- `request_id` 唯一约束

### 10.2 业务层幂等

业务层需要 Redis 和数据库双重保证：

- Redis：抢占热路径，拦截大部分重复请求
- MySQL：唯一约束兜底，处理最终正确性

### 10.3 消费层幂等

Consumer 必须能正确处理重复消息：

- 第一次消费成功，后续重复消息直接忽略
- 第一次处理中途失败，允许 MQ 重试
- 不允许重复扣减库存
- 不允许重复创建订单

建议以 `orderId` 作为消息业务唯一键。

## 11. 查询实现

第一版至少补一个订单查询接口：

- `GET /api/v1/orders/{orderId}`

查询语义：

- 订单已落库，返回真实订单状态
- 订单未落库但仍在处理中，可返回 `PENDING_CREATE`
- 订单处理失败，则返回 `FAILED`

为了支持这个语义，建议增加轻量结果缓存：

- `seckill:order:result:{orderId}`

缓存内容可为：

- `PENDING_CREATE`
- `CREATED`
- `FAILED`

这不是最终真相，只是为了改善异步可见性。

## 12. 日志与可观测性

需要在以下关键节点打印结构化日志：

- 秒杀请求受理成功
- Redis 预扣成功或失败
- MQ 发送成功或失败
- Consumer 开始处理
- 数据库扣减成功或失败
- 订单创建成功或失败
- 补偿开始和补偿完成

日志至少携带：

- `orderId`
- `requestId`
- `userId`
- `productId`
- `messageKey`

后续如接入链路追踪，可继续补 `traceId`。

## 13. 实施顺序

建议按以下顺序落地。

### 第一步：数据库结构补齐

- 调整 `t_order`
- 调整 `t_inventory`
- 新增 `t_mq_consume_record`
- 新增 `t_inventory_flow`

### 第二步：持久化层补齐

- 新增订单 Repository
- 新增库存 Repository
- 新增消费记录 Repository
- 新增库存流水 Repository

### 第三步：主消费链路

- 新增 `OrderCreateConsumer`
- 新增事务型订单创建服务
- 打通库存扣减 + 订单创建

### 第四步：补偿链路

- 新增库存补偿消息体
- 新增补偿 Consumer
- 把 Redis 回滚接入异步补偿

### 第五步：查询与可见性

- 新增订单查询接口
- 增加订单处理中间结果缓存

### 第六步：测试

- 单元测试：幂等、状态流转、异常分支
- 集成测试：订单创建事务、库存扣减
- 并发测试：验证不超卖
- 重复消费测试：验证不会重复扣库存和重复建单

## 14. 第一版验收标准

第一版实现完成后，至少满足以下验收条件：

1. 并发秒杀下数据库最终订单数不超过库存数
2. `t_inventory.available_stock` 不会变成负数
3. 同一用户同一商品只能有一笔有效订单
4. RocketMQ 重复消费不会产生重复订单
5. MQ 消费失败后，Redis 预扣库存和占位可以补偿恢复
6. 用户可按 `orderId` 查询最终订单状态

## 15. 风险说明

第一版仍有以下已知权衡：

- Redis 与 MySQL 之间仍是异步最终一致，不是强一致
- 在短时间窗口内，用户可能看到 `PENDING_CREATE`
- 如果补偿消息自身失败，还需要后续增加重试和死信处理
- 若未来引入支付与超时取消，状态机会进一步扩展

这些权衡是可接受的，但前提是：

- 不超卖必须成立
- 重复订单必须被兜住
- 失败补偿必须能闭环

## 16. 对当前代码的直接改造清单

基于当前仓库，后续代码实现建议直接落在以下位置：

- `order/infrastructure/mq`
  - 新增下单 Consumer
  - 新增补偿 Consumer
- `order/infrastructure/persistence`
  - 新增订单持久化实现
  - 新增消费记录持久化实现
- `inventory/infrastructure/persistence`
  - 新增库存扣减实现
  - 新增库存流水持久化实现
- `order/application`
  - 新增事务型订单创建应用服务
  - 新增补偿应用服务
- `order/interfaces/controller`
  - 新增订单查询接口
- `resources/database/init_schema.sql`
  - 补齐表结构和索引

## 17. 结论

当前仓库已经完成秒杀热路径入口，但还没有真正完成最终一致性闭环。

后续实现必须以“数据库条件扣减 + 订单事务落库 + 消费幂等 + 失败补偿”为核心，只有这样才能真正保证：

- 不超卖
- 订单数据完整
- 异步重试可恢复
