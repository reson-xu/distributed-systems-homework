# API 设计文档

## 1. 设计范围

当前阶段仅设计用户登录接口，其它模块接口暂不展开。

统一前缀建议：

`/api/v1`

## 2. 接口风格

- 接口采用 RESTful 风格
- 当前仅保留最小可说明接口
- 返回结构保持统一

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
- `data`：返回数据

## 4. 用户登录接口

### 4.1 基本信息

- Method: `POST`
- Path: `/api/v1/users/login`
- Description: 用户登录

### 4.2 请求体

```json
{
  "username": "alice",
  "password": "123456"
}
```

### 4.3 响应体

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "userId": 1,
    "username": "alice",
    "token": "mock-token"
  }
}
```

### 4.4 字段说明

- `username`：用户名
- `password`：密码
- `userId`：用户唯一标识
- `token`：登录成功后的令牌占位字段

## 5. DTO 建议

### 5.1 UserLoginRequest

- `username`
- `password`

### 5.2 UserLoginResponse

- `userId`
- `username`
- `token`

## 6. 后续预留

后续可继续补充以下接口，但当前阶段不纳入设计范围：

- 用户注册
- 商品查询
- 库存查询
- 订单创建
