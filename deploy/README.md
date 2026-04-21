# Deploy 说明

## 1. 单体版

根目录的 `docker-compose.yml` 仍然保留，适用于当前单体后端部署。

## 2. 微服务版

微服务联调部署文件位于：

`deploy/docker-compose.microservices.yml`

该文件会启动：

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
- `gateway-service`

## 3. 启动命令

在仓库根目录执行：

```bash
docker compose -f deploy/docker-compose.microservices.yml up -d --build
```

## 4. 停止命令

```bash
docker compose -f deploy/docker-compose.microservices.yml down
```

如果需要连同卷一起清理：

```bash
docker compose -f deploy/docker-compose.microservices.yml down -v
```

## 5. 访问入口

- Nginx 入口：`http://localhost`
- API 入口：`http://localhost/api/v1/...`
- Nacos 控制台：`http://localhost:8848/nacos`

## 6. 说明

- 该 compose 文件面向当前微服务骨架联调，不替代生产部署方案
- 当前所有服务仍默认共享同一套 `seckill` 数据库
- `nginx` 只代理到 `gateway-service`，不直接路由到业务服务
- 后续如果继续拆库，需要再调整各服务数据源和初始化脚本
