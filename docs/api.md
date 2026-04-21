# API 设计文档

## 1. 设计范围

当前文档覆盖仓库中已经实现的基础业务接口：

- 用户认证与用户资料
- 商品详情、商品列表、秒杀商品列表
- 秒杀订单提交与订单查询
- 库存查询
- 支付提交、支付查询、支付回调

统一前缀：

`/api/v1`

## 2. 接口风格

- 接口采用 RESTful 风格
- 返回结构统一使用 `Result`
- 需要鉴权的接口统一通过 `Authorization: Bearer <JWT>` 访问网关
- 支付回调接口当前不要求 JWT

## 3. 通用返回结构

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
- `data`：响应数据

## 4. 用户接口

### 4.1 用户注册

- Method: `POST`
- Path: `/api/v1/auth/register`

请求体：

```json
{
  "username": "alice",
  "password": "123456"
}
```

### 4.2 用户登录

- Method: `POST`
- Path: `/api/v1/auth/login`

请求体：

```json
{
  "username": "alice",
  "password": "123456"
}
```

响应体：

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

### 4.3 查询当前用户

- Method: `GET`
- Path: `/api/v1/auth/me`
- Header: `Authorization: Bearer <JWT>`

### 4.4 退出登录

- Method: `POST`
- Path: `/api/v1/auth/logout`
- Header: `Authorization: Bearer <JWT>`
- Description: 当前实现为无状态 JWT，服务端只校验令牌有效性，客户端自行丢弃令牌

### 4.5 查询用户资料

- Method: `GET`
- Path: `/api/v1/users/{userId}`

响应字段：

- `userId`
- `username`
- `status`

### 4.6 查询秒杀资格

- Method: `GET`
- Path: `/api/v1/users/{userId}/eligibility/seckill`

响应字段：

- `userId`
- `eligible`
- `reason`

## 5. 商品接口

### 5.1 商品列表

- Method: `GET`
- Path: `/api/v1/products`
- Query: `page`、`size`

### 5.2 商品详情

- Method: `GET`
- Path: `/api/v1/products/{productId}`
- Description: 服务端优先读取 Redis 缓存，未命中时回源数据库

响应字段：

- `productId`
- `productName`
- `price`
- `status`
- `availableStock`

### 5.3 商品可售状态

- Method: `GET`
- Path: `/api/v1/products/{productId}/availability`

响应字段：

- `productId`
- `status`
- `availableStock`
- `available`

### 5.4 秒杀商品列表

- Method: `GET`
- Path: `/api/v1/seckill/products`
- Query: `page`、`size`
- Description: 返回当前上架且库存大于 0 的商品

## 6. 秒杀订单接口

### 6.1 提交秒杀订单

- Method: `POST`
- Path: `/api/v1/seckill/orders`
- Header: `Authorization: Bearer <JWT>`

请求体：

```json
{
  "productId": 1,
  "requestId": "req-20260405-0001"
}
```

响应体：

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

### 6.2 当前用户订单列表

- Method: `GET`
- Path: `/api/v1/orders`
- Header: `Authorization: Bearer <JWT>`
- Query: `page`、`size`

### 6.3 订单详情

- Method: `GET`
- Path: `/api/v1/orders/{orderId}`
- Header: `Authorization: Bearer <JWT>`
- Description: 当前接口仍保留服务间查询用途

### 6.4 订单状态

- Method: `GET`
- Path: `/api/v1/orders/{orderId}/status`
- Header: `Authorization: Bearer <JWT>`

响应字段：

- `orderId`
- `orderStatus`
- `orderStatusDesc`
- `failReason`

### 6.5 取消订单

- Method: `POST`
- Path: `/api/v1/orders/{orderId}/cancel`
- Header: `Authorization: Bearer <JWT>`
- Description: 当前仅允许取消未支付订单

### 6.6 订单时间线

- Method: `GET`
- Path: `/api/v1/orders/{orderId}/timeline`
- Header: `Authorization: Bearer <JWT>`

响应字段：

- `orderId`
- `currentStatus`
- `currentStatusDesc`
- `events`

### 6.7 业务失败码

- `3001`：重复秒杀同一商品
- `3002`：库存不足
- `3003`：商品不处于可秒杀状态
- `3004`：重复请求
- `3005`：秒杀请求提交失败

## 7. 库存接口

### 7.1 查询库存详情

- Method: `GET`
- Path: `/api/v1/inventories/{productId}`

响应字段：

- `productId`
- `totalStock`
- `availableStock`
- `lockedStock`

### 7.2 查询可用库存

- Method: `GET`
- Path: `/api/v1/inventories/{productId}/available-stock`

## 8. 支付接口

### 8.1 提交订单支付

- Method: `POST`
- Path: `/api/v1/payments`
- Header: `Authorization: Bearer <JWT>`

请求体：

```json
{
  "orderId": 1911111111111111111,
  "requestId": "pay-20260414-0001",
  "success": true
}
```

### 8.2 查询支付单详情

- Method: `GET`
- Path: `/api/v1/payments/{paymentId}`
- Header: `Authorization: Bearer <JWT>`

### 8.3 按订单查询支付单

- Method: `GET`
- Path: `/api/v1/payments/order/{orderId}`
- Header: `Authorization: Bearer <JWT>`

### 8.4 支付结果回调

- Method: `POST`
- Path: `/api/v1/payments/notify`

请求体：

```json
{
  "paymentId": 1912222222222222222,
  "success": true,
  "failReason": null
}
```

### 8.5 支付单响应字段

- `paymentId`
- `orderId`
- `userId`
- `requestId`
- `paymentAmount`
- `paymentStatus`
- `paymentStatusDesc`
- `failReason`

### 8.6 业务失败码

- `4001`：订单不存在或支付单不存在
- `4002`：订单当前不可支付
- `4003`：支付请求提交失败
- `4004`：支付用户与订单用户不匹配

## 9. 数据可见性约定

- 面向业务侧的接口默认只返回 `is_deleted = 0` 的数据
- 逻辑删除的数据不应通过普通查询接口返回
- 登录接口当前通过响应体返回 JWT
- 商品详情接口采用 Cache Aside 模式，Redis Key 使用统一规范 `seckill:业务域:数据类型:业务主键`
