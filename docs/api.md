# API 设计文档

## 1. 设计范围

当前阶段仅设计用户注册与登录接口，其它模块接口暂不展开。

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

## 5. DTO 建议

### 5.1 AuthRegisterRequest

- `username`
- `password`

### 5.2 AuthRegisterResponse

- `userId`
- `username`

### 5.3 AuthLoginRequest

- `username`
- `password`

### 5.4 AuthLoginResponse

- `userId`
- `username`
- `token`

## 6. 后续预留

后续可继续补充以下接口，但当前阶段不纳入设计范围：

- 商品查询
- 库存查询
- 订单创建

## 7. 数据可见性约定

- 面向业务侧的接口默认只返回 `is_deleted = 0` 的数据
- 逻辑删除的数据不应通过普通查询接口返回
- 后续新增删除类接口时，默认语义应为更新 `is_deleted = 1`
- 当前登录接口默认通过响应体返回 JWT，后续如切换到 Cookie 鉴权，需要同步更新接口契约
