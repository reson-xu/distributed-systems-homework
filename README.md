# 分布式商品库存与秒杀系统

## 项目简介

这是一个基于 Spring Boot、Spring Cloud Alibaba、MyBatis、Redis、MySQL 和 RocketMQ 的分布式秒杀系统练手项目。

当前仓库已经演进为多模块微服务工程，目标是：

- 支持秒杀场景下的高并发下单
- 使用 Redis 做库存预扣、防重复下单和热点保护
- 使用 RocketMQ 驱动下单、库存和支付相关的最终一致性链路
- 保持订单、库存、支付三个核心领域的服务边界清晰

## 当前模块

```text
.
├── framework/
│   ├── framework-core
│   └── framework-web
├── services/
│   ├── gateway-service
│   ├── user-service
│   ├── product-service
│   ├── order-service
│   ├── inventory-service
│   └── payment-service
├── deploy/
├── docs/
├── resources/
└── pom.xml
```

各服务职责如下：

- `gateway-service`：统一入口、路由转发、JWT 解析、用户 ID 透传
- `user-service`：注册、登录、JWT 签发
- `product-service`：商品详情查询、缓存读取、热点重建保护
- `order-service`：秒杀下单、订单查询、订单状态更新
- `inventory-service`：库存扣减、库存流水、库存结果消息回传
- `payment-service`：支付受理、支付单持久化、支付结果消息发送

## 技术栈

- Java 17
- Spring Boot 3.3.5
- Spring Cloud 2023
- Spring Cloud Alibaba
- Spring Cloud Gateway
- OpenFeign
- MyBatis
- MySQL 8
- Redis 7
- RocketMQ 5
- Nacos
- Maven

## 当前能力

- 用户注册与登录
- 网关 JWT 鉴权和 `X-User-Id` 透传
- 商品详情缓存查询
- Redis Lua 秒杀库存预扣与一人一单限制
- 下单与库存扣减的消息最终一致性
- 支付受理与订单状态更新的消息最终一致性
- Docker Compose 微服务联调编排

## 本地构建

在项目根目录执行：

```bash
mvn clean package -DskipTests
```

## 本地启动

### 方式一：逐服务启动

```bash
mvn -pl services/user-service spring-boot:run
mvn -pl services/product-service spring-boot:run
mvn -pl services/inventory-service spring-boot:run
mvn -pl services/order-service spring-boot:run
mvn -pl services/payment-service spring-boot:run
mvn -pl services/gateway-service spring-boot:run
```

### 方式二：Docker Compose 联调

```bash
docker-compose -f deploy/docker-compose.microservices.yml up -d --build
```

会拉起：

- `nginx`
- `nacos`
- `db`
- `redis`
- `rmqnamesrv`
- `rmqbroker`
- `user-service`
- `product-service`
- `inventory-service`
- `order-service`
- `payment-service`
- `gateway-service`

对外入口：

- `http://localhost`
- `http://localhost/api/v1/...`

## 关键接口

- 用户登录：`POST /api/v1/auth/login`
- 商品详情：`GET /api/v1/products/{productId}`
- 秒杀下单：`POST /api/v1/seckill/orders`
- 订单查询：`GET /api/v1/orders/{orderId}`
- 订单支付：`POST /api/v1/payments`

更完整的接口说明见 [docs/api.md](docs/api.md)。

## 关键文档

- [docs/architecture.md](docs/architecture.md)
- [docs/api.md](docs/api.md)
- [docs/microservice-runbook.md](docs/microservice-runbook.md)
- [docs/microservice-evolution-architecture.md](docs/microservice-evolution-architecture.md)
- [docs/order-inventory-microservice-refactor-plan.md](docs/order-inventory-microservice-refactor-plan.md)

## 说明

当前仓库应以 `framework/` 和 `services/` 下的微服务模块为主进行开发与扩展。

当前默认通过 `nginx -> gateway-service` 对外暴露接口，`gateway-service` 不再直接映射宿主机端口。
