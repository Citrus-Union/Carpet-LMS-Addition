---
title: 命令 API
description: 面向自动化的命令输出格式
---

## getItem

```text
/getItem <item> <count> nbt
```

命令规则：

- `commandGetItem` 必须允许命令源使用 `/getItem`。

字段说明：

- `item`：Minecraft 物品参数，例如 `minecraft:stone`。
- `count`：请求的物品数量，必须是大于等于 `1` 的整数。
- `nbt`：启用机器可读的命令输出。

行为说明：

- 命令会在后台执行取物流程，并在完成后发送最终结果。
- 当 `getItemMaxCount` 大于 `0` 时，会限制 `count` 的最大值。
- 限流行为仍遵循 `getItemCooldownSeconds`，并以命令源玩家名进行匹配。

## getItem 的 NBT 结果

成功输出为一个 NBT 列表。每个条目表示一个假人取回的一组物品。

示例：

```snbt
[
  {
    name: "bot_getitem_1",
    id: "minecraft:stone",
    count: 64
  }
]
```

字段说明：

- `name`：假人名称。
- `id`：物品注册表 ID。
- `count`：该假人取回的物品数量。

如果没有取到任何物品，结果为空列表：

```snbt
[]
```

## getItem 的 NBT 错误

当 `count` 无效或超过 `getItemMaxCount` 时，NBT 模式会返回一个包含已配置上限的失败响应：

```snbt
{
  maxCount: 256
}
```

字段说明：

- `maxCount`：`getItemMaxCount` 规则的值。`0` 表示不限制。

其他运行时错误仍使用普通命令失败文本：

- 消息会按服务器语言本地化。

```text
[Carpet LMS Addition] Unknown error
```

或

```text
[Carpet LMS Addition] 未知错误
```

当 `getItemCooldownSeconds` 对 NBT 模式触发限流时，命令会返回一个包含剩余等待时间的失败响应：

```snbt
{
  waitSecond: 5
}
```

## getStorageData

```text
/getStorageData
/getStorageData <id>
```

命令规则：

- `commandGetStorageData` 必须允许命令源使用 `/getStorageData`。

字段说明：

- `id`：Minecraft 物品参数，例如 `minecraft:diamond`。

行为说明：

- 命令会扫描所有已配置的仓储文件，并合并其中匹配物品的总数。
- 潜影盒内容使用与仓储网站数据相同的递归计数逻辑。
- 已配置仓储中的无效条目会被现有仓储扫描器记录日志并跳过。
- 限流行为遵循 `getStorageDataCooldownSeconds`，并以命令源玩家名进行匹配。

## getStorageData 的 NBT 结果

`/getStorageData` 会返回一个 NBT 列表。每个条目表示所有已配置仓储文件中某种物品的累计数量。

示例：

```snbt
[
  {
    id: "minecraft:diamond",
    count: 100
  }
]
```

`/getStorageData <id>` 会为请求的物品返回一个单独的 NBT 复合标签：

```snbt
{
  id: "minecraft:diamond",
  count: 100
}
```

当 `getStorageDataCooldownSeconds` 对命令触发限流时，会返回一个包含剩余等待时间的失败响应：

```snbt
{
  waitSecond: 5
}
```

字段说明：

- `id`：物品注册表 ID。
- `count`：所有已配置仓储文件中的累计物品数量。如果请求的物品是有效物品但仓储中不存在，单物品结果会返回 `0`。
