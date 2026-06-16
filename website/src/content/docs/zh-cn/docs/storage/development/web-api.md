---
title: Web API
description: 仓储网站 API 参考
---

## 基础

- 服务地址：`http://<server-ip>:<port>`
- 端口来自 `checkStorageConfig.json` 的 `port` 字段。

## 认证

- 请求头：`Authorization: Bearer <token>`
- `/api/login` 不使用 token 认证。
- 如果 `noPassword: true`
  - `/api/storage/getData` 可以不带 token 调用。
  - `/api/storage/getItem` 可以不带 token 调用。
  - `/api/storage/sendGetItemResult` 仍然需要有效 token。
- 如果向 `/api/storage/getData` 或 `/api/storage/getItem` 发送了 token，后端会先校验 token。
- 当 `noPassword: true` 且 token 校验失败时，`/api/storage/getData` 与 `/api/storage/getItem` 会回退为匿名访问。
- `/api/storage/sendGetItemResult` 不支持匿名回退。

## POST /api/login

- 请求体：

```json
{
  "username": "admin",
  "password": "your-password"
}
```

- 成功响应：

```json
{
  "username": "admin",
  "token": "jwt-or-token-string"
}
```

行为说明：

- `websiteLoginCooldownSeconds` 会按请求中的 `username` 限制重复登录尝试。
- 登录成功和失败都会计入冷却。
- 被该规则限制的请求会返回 `429`。

登录错误消息：

- `401` 可能返回以下 `message`
  - `Invalid username or password`
  - `Username cannot be empty`
  - `Password cannot be empty`
  - `User data error`
- `500` 可能返回
  - `Unknown error`

## GET /api/storage/getData

- 方法：`GET`
- 返回数据会在处理请求时，根据当前的 `checkStorageConfig.json` 与 `checkStorageList` 文件生成。
- `getStorageDataCooldownSeconds` 会按 token 用户名限制重复查询。
- 如果 `noPassword: true` 且未使用有效 token，所有匿名查询共享空字符串这个冷却键。
- 被该规则限制的请求会返回 `429`。
- 响应使用紧凑字段名，并且只包含每种物品的总数量。
- 错误位置仍使用维度键：
- `"0"`：主世界
- `"-1"`：下界
- `"1"`：末地

示例响应：

```json
[
  {
    "n": "main",
    "d": [
      {
        "i": "barrel",
        "c": 1152
      }
    ],
    "e": {
      "0": [[0, 100, 0]]
    }
  }
]
```

字段说明：

- `n`：仓储名称。
- `d`：物品列表。
- `d[].i`：物品 ID。
- 对于 `minecraft:*` 物品，键中会省略 `minecraft:` 前缀（例如 `barrel`）。
- `d[].c`：物品总数量。
- `e["0" | "-1" | "1"]`：某个维度中的错误坐标列表，每个条目都是 `[x, y, z]`。

## POST /api/storage/getItem

- 方法：`POST`
- 认证
  - `noPassword: false` 时必须带认证
  - `noPassword: true` 时认证可选
- 请求体：

```json
{
  "i": "minecraft:stone",
  "c": 64
}
```

字段说明：

- `i`：物品 ID。
- `c`：请求数量，必须是大于等于 `1` 的整数。

成功响应：

```json
[
  {
    "n": "bot_getitem_1",
    "i": "stone",
    "c": 64
  }
]
```

字段说明：

- `n`：假人名称。
- `i`：物品 ID。
- 对于 `minecraft:*` 物品，`i` 会省略 `minecraft:` 前缀（例如 `stone`）。
- `c`：该假人取回的数量。
- 前端会根据 `n/i/c` 生成命令与日志文本。
- 后端会优先选择“总取出量不小于请求值且尽可能小”的槽位组合；若总量相同，再尽量减少使用槽位数。
- 如果 token 校验成功，后端会使用 token 用户名作为 getItem 的玩家名参与限流统计。
- 如果 `noPassword: true` 且 token 校验失败或未发送 token，后端会将该请求按匿名请求处理。

## POST /api/storage/sendGetItemResult

- 方法：`POST`
- 认证：始终需要 `Authorization: Bearer <token>`
- 用途：将一条或多条 getItem 结果消息发送给与 token 用户名对应的在线玩家。
- 请求体：

```json
[
  {
    "i": "minecraft:stone",
    "c": 64,
    "n": "bot_getitem_1"
  }
]
```

字段说明：

- 请求体必须是数组。
- `i`：物品 ID。
- `c`：该假人结果行里显示的物品数量。
- `n`：假人名称。
- 数组中的多个条目会按请求顺序发送。

成功响应：

```json
{
  "success": true,
  "message": "Sent to player"
}
```

行为说明：

- token 用户名会被用作目标 Minecraft 玩家名。
- 如果该玩家不在线，后端会返回错误且不会发送任何消息。
- 即使 `noPassword: true`，这个 API 也不会回退为匿名访问。

## 错误响应

通用格式：

```json
{
  "status": "400",
  "message": "error message"
}
```

后端已知会返回的 `message` 值：

- 认证相关
  - `Invalid username or password`
  - `Username cannot be empty`
  - `Password cannot be empty`
  - `User data error`
  - `Invalid token`
  - `Token expired`
- 限流相关
  - `Rate limited, wait <seconds> seconds`
- 仓储网站相关
  - `Website getItem is disabled`
  - `Target player is not online`
  - `Minecraft server is not initialized`
- 通用
  - `Unknown error`
