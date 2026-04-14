# 订单与库存微服务拆分设计方案

## 1. 文档目标

本文档用于指导当前项目从“模块化单体”演进到“以订单服务和库存服务为核心的微服务架构”。

本次设计明确约束如下：

- 当前阶段不考虑支付域
- 优先拆分 `order-service` 与 `inventory-service`
- 引入 `Nacos`、`Spring Cloud Alibaba`、`Spring Cloud Gateway`
- 保留当前仓库的秒杀业务方向
- 设计优先，不在本文档内直接落代码

## 2. 当前现状判断

当前仓库虽然已经出现 `user`、`product`、`order`、`inventory` 四个业务模块，但整体仍属于：

`模块化单体`

主要特征：

- 所有模块运行在同一个 Spring Boot 进程内
- 默认共享一套应用配置
- 默认共享一套数据库连接
- 模块间通过本地方法直接调用
- 事务边界本质上仍是单体事务

这意味着当前项目还没有真正进入微服务阶段。

## 3. 本次演进范围

### 3.1 本次要做的事情

- 设计目标微服务边界
- 设计服务职责
- 设计服务间调用方式
- 设计数据库拆分方式
- 设计基于 RocketMQ 的最终一致性方案
- 设计 Nacos、Gateway、Spring Cloud Alibaba 的接入位置
- 设计当前仓库是否需要重构 DDD

### 3.2 本次不做的事情

- 不设计支付服务
- 不引入 TCC
- 不引入 Seata
- 不直接展开 Kubernetes、Service Mesh 等平台级方案
- 不在这一阶段追求完整企业级治理体系

## 4. 目标架构总览

在不考虑支付的前提下，建议收敛为如下目标结构：

```text
client
  -> gateway-service
       -> user-service
       -> product-service
       -> order-service
       -> inventory-service

基础设施:
  nacos
  rocketmq
  redis
  mysql
```

系统定位：

- `Gateway` 负责统一入口
- `Nacos` 负责注册发现和配置管理
- `RocketMQ` 负责异步削峰和最终一致性推进
- `Redis` 负责秒杀热路径
- `MySQL` 负责最终数据真相

## 5. 推荐服务拆分

### 5.1 gateway-service

职责：

- 统一路由
- JWT 透传
- 请求限流
- 黑名单拦截
- 统一 TraceId 注入

推荐技术：

- `Spring Cloud Gateway`
- `Spring Cloud Alibaba Nacos Discovery`

### 5.2 user-service

职责：

- 用户注册与登录
- 用户身份校验
- 用户秒杀资格校验
- 用户限购策略查询

数据库：

- `user_db`

### 5.3 product-service

职责：

- 商品基础信息
- 商品状态管理
- 秒杀商品元信息查询
- 商品详情缓存管理

数据库：

- `product_db`

### 5.4 order-service

职责：

- 受理秒杀下单请求
- 维护订单状态机
- 查询订单
- 发布下单事件
- 处理库存结果事件

数据库：

- `order_db`

### 5.5 inventory-service

职责：

- 库存真相维护
- 库存扣减
- 库存释放
- 库存流水审计
- 消费下单事件并回传库存结果

数据库：

- `inventory_db`

## 6. 为什么优先拆 order 和 inventory

当前项目最核心的业务矛盾是：

- 秒杀高并发接入
- 不能超卖
- 一人一单
- 订单与库存跨边界一致性

这些问题主要集中在 `order` 与 `inventory` 两个域之间。

因此优先拆这两个服务的收益最高：

- 可以最早建立服务边界
- 可以最早暴露一致性问题
- 可以最早验证 RocketMQ 事件驱动链路
- 可以避免先拆边缘服务导致收益很低

## 7. DDD 是否需要重构

结论：

`需要做轻量重构，但不建议演进成重型 DDD。`

### 7.1 当前项目适合什么样的 DDD

推荐采用：

`轻量 DDD + 清晰分层 + 明确边界上下文`

不推荐采用：

- 过多聚合根
- 复杂领域工厂
- 大量领域事件对象泛滥
- 过度抽象的防腐层

### 7.2 为什么需要重构

因为从单体走向微服务后，原本很多“包内可见的直接调用”会失效，必须把边界拉清楚。

需要重点重构的不是“写法更花哨”，而是：

- 边界上下文明确
- 领域对象不跨服务滥用
- 接口模型、事件模型、持久化模型分离
- 应用服务只编排，不偷写别的领域仓储

### 7.3 推荐保留的分层

每个服务内部仍建议保留：

```text
interfaces
application
domain
infrastructure
```

说明：

- `interfaces`：Controller、DTO、VO、Feign 入参等
- `application`：用例编排、事务边界、事件发布
- `domain`：核心规则、状态机、领域模型、仓储接口
- `infrastructure`：MyBatis、RocketMQ、Redis、Nacos 配置等

### 7.4 推荐新增的模型边界

建议在微服务化后明确区分：

- `DTO`
- `VO`
- `Entity / DO`
- `Event`
- `Command / Query`

至少不要再让：

- Controller 请求对象直接下沉到仓储层
- MQ 消息对象替代领域对象
- 一个服务的 DO 直接传到另一个服务

## 8. Spring Cloud Alibaba 技术选型建议

当前范围内建议使用：

- `Spring Cloud Gateway`
- `Spring Cloud Alibaba Nacos Discovery`
- `Spring Cloud Alibaba Nacos Config`
- `OpenFeign`
- `RocketMQ Spring Boot Starter`

说明：

- 服务注册发现依赖 `Nacos Discovery`
- 动态配置依赖 `Nacos Config`
- HTTP 同步调用优先用 `OpenFeign`
- 异步一致性和削峰仍由 `RocketMQ` 承担

## 9. Nacos 设计

### 9.1 Nacos 的职责

本项目中 `Nacos` 主要负责：

- 服务注册与发现
- 多环境配置管理
- 灰度或环境隔离配置
- 网关动态路由配置基础能力

### 9.2 推荐服务注册名

- `gateway-service`
- `user-service`
- `product-service`
- `order-service`
- `inventory-service`

### 9.3 推荐配置划分

每个服务至少拆分为：

- `bootstrap` 或等效启动配置
- `application` 基础配置
- `datasource` 配置
- `redis` 配置
- `rocketmq` 配置
- 业务配置

建议在 `Nacos` 中按环境区分：

- `dev`
- `test`
- `prod`

## 10. Gateway 设计

### 10.1 Gateway 的职责边界

`Gateway` 负责：

- 路由转发
- JWT 解析与透传
- 限流
- 统一异常响应包装
- 跨域处理
- TraceId 注入

`Gateway` 不负责：

- 业务规则判断
- 秒杀库存校验
- 订单创建编排

### 10.2 推荐路由前缀

- `/api/v1/auth/**` -> `user-service`
- `/api/v1/products/**` -> `product-service`
- `/api/v1/seckill/orders/**` -> `order-service`
- `/api/v1/inventories/**` -> `inventory-service`

### 10.3 推荐网关过滤器

- `TraceIdFilter`
- `JwtAuthFilter`
- `RateLimitFilter`
- `AccessLogFilter`

## 11. 服务间调用原则

### 11.1 同步调用

同步调用只用于：

- 用户身份校验
- 商品基础信息查询
- 必要的资格检查

推荐方式：

- `OpenFeign`

### 11.2 异步调用

异步调用用于：

- 下单和库存一致性推进
- 秒杀请求削峰
- 失败补偿

推荐方式：

- `RocketMQ`

### 11.3 关键原则

- 查模型可以同步
- 写模型尽量事件驱动
- 不在关键链路上滥用同步级联调用

## 12. 订单与库存一致性方案

不考虑支付时，系统核心只需要保证：

- 秒杀请求受理正确
- 不超卖
- 一人一单
- 订单与库存最终一致

推荐方案：

`Redis 预扣 + RocketMQ 事件驱动 + MySQL 本地事务 + 补偿`

### 12.1 同步阶段

由 `order-service` 完成：

1. 校验登录态
2. 校验商品是否可秒杀
3. 通过 Redis Lua 完成库存预扣与一人一单校验
4. 生成订单 ID
5. 发送下单事件到 RocketMQ
6. 返回 `PENDING`

### 12.2 异步阶段

由 `inventory-service` 完成：

1. 消费下单事件
2. 执行数据库条件扣减库存
3. 写库存流水
4. 发布库存结果事件

再由 `order-service` 完成：

1. 消费库存结果事件
2. 成功则创建订单
3. 失败则标记订单失败
4. 必要时回滚 Redis 占位或写补偿记录

## 13. 为什么当前不推荐 TCC

当前阶段不考虑支付，且系统主要目标是课程级别的秒杀系统演进。

如果此时直接引入 TCC，会带来以下额外复杂度：

- Try/Confirm/Cancel 三阶段接口设计
- 反悬挂、空回滚、幂等等边界处理
- 开发和测试复杂度大幅上升
- 对当前仓库代码基础不够友好

因此当前最务实的选择仍然是：

`基于 RocketMQ 的最终一致性`

## 14. 目标状态机设计

### 14.1 订单状态

建议至少包含：

- `PENDING`
- `INVENTORY_PROCESSING`
- `CREATED`
- `FAILED`
- `CANCELLED`

### 14.2 库存事件状态

建议至少包含：

- `INIT`
- `SUCCESS`
- `FAILED`
- `COMPENSATED`

## 15. 数据库拆分设计

### 15.1 order_db

建议保留：

- `t_order`
- `t_order_event_log`
- `t_mq_consume_record`

说明：

- `t_order` 保存订单主数据
- `t_mq_consume_record` 用于消费者幂等
- `t_order_event_log` 用于关键事件审计

### 15.2 inventory_db

建议保留：

- `t_inventory`
- `t_inventory_flow`
- `t_inventory_reservation`
- `t_mq_consume_record`

说明：

- `t_inventory` 是库存真相
- `t_inventory_flow` 用于库存审计
- `t_inventory_reservation` 可用于未来扩展预留库存模型

## 16. 推荐 Maven 多模块结构

如果正式开始拆分，建议从单体工程切换到 Maven 多模块结构：

```text
pom.xml
services/
  gateway-service/
  user-service/
  product-service/
  order-service/
  inventory-service/
libraries/
  common-core/
  common-web/
  common-security/
  common-redis/
  common-mq/
docs/
```

### 16.1 common 模块建议

`common-core`

- 通用响应模型
- 错误码
- 基础异常
- TraceId 工具

`common-security`

- JWT 解析
- 鉴权上下文

`common-mq`

- RocketMQ 公共配置
- 事件基类

`common-redis`

- Redis Key 规范
- Lua 脚本装载支持

## 17. 服务目录建议

以 `order-service` 为例，建议目录如下：

```text
order-service/
  src/main/java/.../order
    interfaces/
    application/
    domain/
    infrastructure/
```

`inventory-service` 同理。

不要在微服务拆分后再保留“一个服务里同时放 order 和 inventory 代码”的结构，否则边界会持续模糊。

## 18. 演进步骤建议

### 18.1 第一步：先补边界，不先拆服务

在当前单体仓库内先做到：

- `order` 不直接依赖 `inventory` 的持久化实现
- 服务间只通过应用层接口或事件对象交互
- 订单和库存对象模型边界明确

这一步本质上是“先做逻辑拆分，再做物理拆分”。

### 18.2 第二步：抽公共库

抽出：

- 响应模型
- JWT 能力
- MQ 通用配置
- Redis 通用能力

### 18.3 第三步：拆 order-service

优先让 `order-service` 独立出来，负责：

- 接口受理
- Redis 热路径
- 下单事件发布
- 订单状态推进

### 18.4 第四步：拆 inventory-service

然后让 `inventory-service` 独立出来，负责：

- 消费下单事件
- 库存扣减
- 库存流水
- 库存结果回传

### 18.5 第五步：引入 Gateway 和 Nacos

这一步完成后，系统才真正具备：

- 服务发现
- 统一入口
- 配置集中化

### 18.6 第六步：补服务治理

包括：

- 日志聚合
- 链路追踪
- 指标监控
- 限流熔断

## 19. 风险与难点

本次重构最大的难点不是“建几个模块”，而是：

- 服务边界容易反复穿透
- Redis 预扣与数据库库存可能漂移
- MQ 重复消费会导致重复订单或重复扣库存
- 事件顺序和失败补偿需要明确状态机
- 旧单体代码改造时容易出现共享模型污染

因此必须坚持以下原则：

- 一切关键消费都要幂等
- 一切关键写操作都要可追踪
- 一切跨服务一致性都要有失败路径设计

## 20. 结论

在当前“不考虑支付”的范围内，最合理的架构演进方案是：

- 保留轻量 DDD 分层
- 优先拆 `order-service` 和 `inventory-service`
- 引入 `Nacos + Spring Cloud Alibaba + Spring Cloud Gateway`
- 使用 `Redis + RocketMQ + MySQL` 实现最终一致性

架构目标不是一步走到“完整企业级微服务”，而是先构建一个边界清晰、可演进、能证明秒杀核心能力的微服务骨架。

这也是当前仓库最适合的重构方向。
