---
title: Command API
description: Command output formats intended for automation
---

## getItem

```mcfunction
/getItem <item> <count> nbt
```

Command rule:

- `commandGetItem` must allow the command source to use `/getItem`.

Field notes:

- `item`: Minecraft item argument, for example `minecraft:stone`.
- `count`: requested item count. Must be an integer greater than or equal to `1`.
- `nbt`: enables machine-readable command output.

Behavior notes:

- The command runs the item fetch in the background and sends the final result when it is done.
- `getItemMaxCount` limits `count` when it is greater than `0`.
- Rate limit behavior still follows `getItemCooldownSeconds` and is matched by the command source player name.

## getItem NBT result

Success output is an NBT list. Each entry describes one item stack fetched by one bot.

Example:

```snbt
[
  {
    name: "bot_getitem_1",
    id: "minecraft:stone",
    count: 64
  }
]
```

Field notes:

- `name`: bot name.
- `id`: item registry id.
- `count`: item count fetched by this bot.

If no item is fetched, the result is an empty list:

```snbt
[]
```

## getItem NBT error

When `count` is invalid or exceeds `getItemMaxCount`, NBT mode sends a failure response with the configured maximum:

```snbt
{
  maxCount: 256
}
```

Field notes:

- `maxCount`: value of the `getItemMaxCount` rule. `0` means unlimited.

Other runtime failures still use the normal command failure text:

```text
getItem failed: <message>
```

## getStorageData

```mcfunction
/getStorageData
/getStorageData <id>
```

Command rule:

- `commandGetStorageData` must allow the command source to use `/getStorageData`.

Field notes:

- `id`: Minecraft item argument, for example `minecraft:diamond`.

Behavior notes:

- The command scans all configured storage files and merges matching item counts across them.
- Shulker box contents follow the same recursive counting behavior as the storage website data.
- Invalid configured storage entries are logged and skipped by the existing storage scanner.

## getStorageData NBT result

`/getStorageData` sends an NBT list. Each entry describes the cumulative count of one item across all configured storage files.

Example:

```snbt
[
  {
    id: "minecraft:diamond",
    count: 100
  }
]
```

`/getStorageData <id>` sends one NBT compound for the requested item:

```snbt
{
  id: "minecraft:diamond",
  count: 100
}
```

Field notes:

- `id`: item registry id.
- `count`: cumulative item count across all configured storage files. If the requested item exists but is not present in storage, the single-item result uses `0`.
