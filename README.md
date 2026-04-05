# 分布式商品库存与秒杀系统

## 1. 项目简介

这是一个基于 Spring Boot 3、MyBatis、MySQL、Redis 和 RocketMQ 的分布式商品库存与秒杀系统练手项目。

当前已经落地的能力：

- 用户注册
- 用户登录
- JWT 令牌签发
- MyBatis 用户持久化
- Docker Compose 一键启动后端、数据库、RocketMQ、Nginx

## 2. 技术栈

- Java 17
- Spring Boot 3.3.5
- MyBatis
- MySQL
- Redis
- RocketMQ
- Nginx
- Docker / Docker Compose

## 3. 目录说明

```text
.
├── Dockerfile
├── docker-compose.yml
├── nginx/
│   └── default.conf
├── resources/
│   └── database/
│       └── init_schema.sql
├── src/
│   ├── main/
│   └── test/
└── README.md
```

## 4. 启动方式

### 4.1 环境要求

- 已安装 Docker
- 已安装 Docker Compose Plugin

### 4.2 一键启动

在项目根目录执行：

```bash
docker compose up -d --build
```

启动后会拉起以下服务：

- `backend`：Spring Boot 后端服务，容器内端口 `8080`
- `db`：MySQL 数据库，宿主机映射端口 `3306`
- `rmqnamesrv`：RocketMQ NameServer，宿主机映射端口 `9876`
- `rmqbroker`：RocketMQ Broker，宿主机映射端口 `10909/10911/10912`
- `nginx`：反向代理入口，宿主机映射端口 `80`

### 4.3 停止服务

```bash
docker compose down
```

如果希望连同数据库数据卷一起清理：

```bash
docker compose down -v
```

## 5. 访问地址

启动完成后可通过以下地址访问：

- 应用入口：`http://localhost`
- Swagger UI：`http://localhost/swagger-ui.html`
- OpenAPI 文档：`http://localhost/v3/api-docs`

## 6. 接口说明

### 6.1 用户注册

- 方法：`POST`
- 路径：`/api/v1/auth/register`

请求示例：

```json
{
  "username": "alice",
  "password": "123456"
}
```

### 6.2 用户登录

- 方法：`POST`
- 路径：`/api/v1/auth/login`

请求示例：

```json
{
  "username": "alice",
  "password": "123456"
}
```

## 7. 容器配置说明

### 7.1 数据库初始化

MySQL 首次启动时会自动执行：

`resources/database/init_schema.sql`

用于初始化用户、商品、库存、订单相关表结构。

### 7.2 Nginx 代理

Nginx 配置文件位于：

`nginx/default.conf`

默认会将所有请求转发到后端服务 `backend:8080`。

### 7.3 默认数据库参数

- 数据库名：`seckill`
- 用户名：`seckill`
- 密码：`seckill123`
- root 密码：`root123`
- 宿主机端口：`3306`

如需调整，可以直接修改 `docker-compose.yml` 中对应环境变量。

## 8. 本地开发运行

如果不使用 Docker，也可以本地直接启动：

```bash
mvn spring-boot:run
```

默认情况下，应用配置支持通过环境变量覆盖数据库和 JWT 参数，例如：

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `ROCKETMQ_NAME_SERVER`
- `SECURITY_JWT_SECRET`

## 9. 常用命令

构建项目：

```bash
mvn clean package -DskipTests
```

运行测试：

```bash
mvn test
```

查看日志：

```bash
docker compose logs -f backend
docker compose logs -f db
docker compose logs -f rmqnamesrv
docker compose logs -f rmqbroker
docker compose logs -f nginx
```

## 10. 说明

当前 Docker Compose 已包含后端、MySQL、RocketMQ 和 Nginx。

## 11. MySQL 替代说明

当前仓库已将原先的 MariaDB 假设替换为 MySQL，主要调整如下：

- JDBC 驱动切换为 `com.mysql:mysql-connector-j`
- 默认数据源地址切换为 `jdbc:mysql://127.0.0.1:3306/seckill`
- Docker Compose 数据库镜像切换为 `mysql:8.4`
- 初始化脚本保持 MySQL 兼容语法，并显式指定 `InnoDB`、`utf8mb4` 与 `utf8mb4_unicode_ci`
- 容器健康检查改为 `mysqladmin ping`

## 12. RocketMQ 配置说明

项目已引入官方 `rocketmq-spring-boot-starter`，并预留了以下配置：

- `rocketmq.name-server`
- `rocketmq.producer.group`
- `seckill.rocketmq.topics.order-create`
- `seckill.rocketmq.topics.order-cancel`
- `seckill.rocketmq.topics.inventory-compensate`

Docker Compose 中已新增：

- `rmqnamesrv`：`apache/rocketmq:5.3.2`
- `rmqbroker`：`apache/rocketmq:5.3.2`

默认 broker 配置文件位于：

`deploy/rocketmq/broker.conf`

注意：

- 当前 `broker.conf` 的 `brokerIP1=rmqbroker` 适用于 Docker Compose 内部服务互联
- 如果你要在宿主机直接启动 Spring Boot 并连接同一个 Docker RocketMQ，需要按宿主机可访问地址调整 `brokerIP1`
