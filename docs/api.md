# API 设计文档

## 1. 设计范围

当前阶段设计用户认证接口与商品详情接口。

统一前缀建议：

`/api/v1`

## 2. 接口风格

- 接口采用 RESTful 风格
- 当前仅保留最小可说明接口
- 返回结构保持统一
- 控制器对外方法需要在函数上方补充 `/** ... */` 风格的 JavaDoc 注释

## 3. 通用返回结构

统一返回模型命名为 `Result`。

```json
{
  "code": "0",
  "message": "success",
  "data": {}
}
```

字段说明：

- `code`：业务状态码，`0` 表示成功
- `message`：结果说明
- `data`：返回数据

## 4. 用户认证接口

认证相关能力统一归属 `auth`，用户资料与用户领域对象归属 `user`。

### 4.1 用户注册

- Method: `POST`
- Path: `/api/v1/auth/register`
- Description: 注册新用户账号

#### 请求体

```json
{
  "username": "alice",
  "password": "123456"
}
```

#### 响应体

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "userId": 1,
    "username": "alice"
  }
}
```

### 4.2 用户登录

- Method: `POST`
- Path: `/api/v1/auth/login`
- Description: 用户登录

#### 请求体

```json
{
  "username": "alice",
  "password": "123456"
}
```

#### 响应体

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "userId": 1,
    "username": "alice",
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

### 4.3 字段说明

- `username`：用户名
- `password`：密码
- `userId`：用户唯一标识
- `token`：登录成功后签发的 JWT 令牌

## 5. DTO / VO 建议

### 5.1 AuthRegisterDTO

- `username`
- `password`

### 5.2 AuthRegisterVO

- `userId`
- `username`

### 5.3 AuthLoginDTO

- `username`
- `password`

### 5.4 AuthLoginVO

- `userId`
- `username`
- `token`

## 6. 商品接口

### 6.1 商品详情

- Method: `GET`
- Path: `/api/v1/products/{productId}`
- Description: 查询商品详情，服务端优先读取 Redis 缓存，未命中时回源数据库

#### 响应体

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "productId": 1,
    "productName": "iPhone 16",
    "price": 6999.00,
    "status": 1,
    "availableStock": 100
  }
}
```

### 6.2 字段说明

- `productId`：商品ID
- `productName`：商品名称
- `price`：商品价格
- `status`：商品状态
- `availableStock`：可用库存

## 7. 秒杀订单接口

### 7.1 提交秒杀订单

- Method: `POST`
- Path: `/api/v1/seckill/orders`
- Header: `Authorization: Bearer <JWT>`
- Description: 受理秒杀下单请求，完成幂等校验、Redis 预扣库存，并异步发送 RocketMQ 下单消息

#### 请求体

```json
{
  "productId": 1,
  "requestId": "req-20260405-0001"
}
```

#### 响应体

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "orderId": 1911111111111111111,
    "status": "PENDING_CREATE"
  }
}
```

### 7.2 业务失败码

- `3001`：重复秒杀同一商品
- `3002`：库存不足
- `3003`：商品不处于可秒杀状态
- `3004`：重复请求
- `3005`：秒杀请求提交失败

## 8. 支付接口

### 8.1 提交订单支付

- Method: `POST`
- Path: `/api/v1/payments`
- Header: `Authorization: Bearer <JWT>`
- Description: 校验订单可支付后创建支付单，并异步发送支付结果事件驱动订单状态更新

#### 请求体

```json
{
  "orderId": 1911111111111111111,
  "requestId": "pay-20260414-0001",
  "success": true
}
```

#### 响应体

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "paymentId": 1912222222222222222,
    "status": "SUCCESS"
  }
}
```

### 8.2 业务失败码

- `4001`：订单不存在
- `4002`：订单当前不可支付
- `4003`：支付请求提交失败
- `4004`：支付用户与订单用户不匹配

## 9. 后续预留

后续可继续补充以下接口，但当前阶段不纳入设计范围：

- 库存查询
- 订单取消
- 订单超时关单

## 10. 数据可见性约定

- 面向业务侧的接口默认只返回 `is_deleted = 0` 的数据
- 逻辑删除的数据不应通过普通查询接口返回
- 后续新增删除类接口时，默认语义应为更新 `is_deleted = 1`
- 当前登录接口默认通过响应体返回 JWT，后续如切换到 Cookie 鉴权，需要同步更新接口契约
- 商品详情接口采用 Cache Aside 模式，Redis Key 使用统一规范 `seckill:业务域:数据类型:业务主键`
