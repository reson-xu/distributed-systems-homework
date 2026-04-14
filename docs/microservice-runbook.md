# 微服务联调说明

## 1. 当前服务集合

当前仓库已经具备以下微服务模块：

- `gateway-service`
- `user-service`
- `product-service`
- `order-service`
- `inventory-service`
- `payment-service`

## 2. 当前已迁出的能力

### 2.1 gateway-service

- 网关路由
- JWT 解析
- 用户 ID 透传到订单服务

### 2.2 user-service

- 用户注册
- 用户登录
- JWT 签发

### 2.3 product-service

- 商品详情查询
- Redis 缓存读取
- Redisson 热点重建锁

### 2.4 order-service

- 秒杀下单受理
- 订单查询
- 订单创建结果消费
- 调用 `product-service` 的 Feign 契约

### 2.5 inventory-service

- 商品库存查询
- 下单消息消费
- 库存扣减
- 库存结果消息回传

### 2.6 payment-service

- 订单支付受理
- 支付单持久化
- 支付结果事件投递

## 3. 当前推荐联调顺序

建议按以下顺序启动：

1. `Nacos`
2. `MySQL`
3. `Redis`
4. `RocketMQ`
5. `user-service`
6. `product-service`
7. `inventory-service`
8. `order-service`
9. `payment-service`
10. `gateway-service`

## 4. 本地启动命令示例

在仓库根目录分别启动：

```bash
mvn -pl services/user-service spring-boot:run
mvn -pl services/product-service spring-boot:run
mvn -pl services/inventory-service spring-boot:run
mvn -pl services/order-service spring-boot:run
mvn -pl services/payment-service spring-boot:run
mvn -pl services/gateway-service spring-boot:run
```

## 5. 当前网关路由

- `/api/v1/auth/**` -> `user-service`
- `/api/v1/products/**` -> `product-service`
- `/api/v1/seckill/orders/**` -> `order-service`
- `/api/v1/orders/**` -> `order-service`
- `/api/v1/inventories/**` -> `inventory-service`
- `/api/v1/payments/**` -> `payment-service`

## 6. 当前关键调用链

### 7.1 登录链路

`Gateway -> user-service`

### 7.2 商品详情链路

`Gateway -> product-service`

### 7.3 秒杀下单链路

`Gateway -> order-service -> Feign -> product-service`

然后：

`order-service -> RocketMQ -> inventory-service -> RocketMQ -> order-service`

### 7.4 支付链路

`Gateway -> payment-service -> Feign -> order-service`

然后：

`payment-service -> RocketMQ -> order-service`

## 7. 当前未完成项

- `Gateway` 目前只对订单相关路径做 JWT 透传
- `user-service` 还没有单独提供用户资格、限购、用户查询接口
- `product-service` 目前仍直接读取 `t_inventory`，正式拆库后要调整
- `order-service`、`inventory-service`、`payment-service` 还没有完整集成测试
## 8. 下一阶段建议

优先建议：

1. 给各服务补独立 `README` 或 `nacos` 配置样例
2. 引入统一的本地启动脚本或新的多服务 compose 文件
