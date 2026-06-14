---
title: 规则
description: Carpet LMS Addition 规则总览
---

## 悦灵回血间隔 (allayHealInterval)

控制悦灵的回血间隔（单位：gt）

设置为0时不回血

<0时不修改原版行为

- 类型: `int`
- 默认值: `-1`
- 分类: `LMS`, `SURVIVAL`

## 管理自己的LMS设置 (commandLMSSelf)

查看或修改自己的 /lms 设置

- 类型: `String`
- 默认值: `true`
- 参考选项: `false`, `true`, `ops`, `0`, `1`, `2`, `3`, `4`
- 分类: `LMS`, `SURVIVAL`, `COMMAND`

## 管理他人LMS设置 (commandLMSOthers)

查看或修改其他玩家的 /lms 设置

- 类型: `String`
- 默认值: `ops`
- 参考选项: `false`, `true`, `ops`, `0`, `1`, `2`, `3`, `4`
- 分类: `LMS`, `SURVIVAL`, `COMMAND`

## 管理假人LMS设置 (commandLMSBot)

查看或修改假人的 /lms 设置

- 类型: `String`
- 默认值: `true`
- 参考选项: `false`, `true`, `ops`, `0`, `1`, `2`, `3`, `4`
- 分类: `LMS`, `SURVIVAL`, `COMMAND`

## 玩家破坏限制 (breakingRestriction)

启用通过 /lms 配置的玩家方块破坏限制

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `false`, `true`
- 分类: `LMS`, `SURVIVAL`, `COMMAND`

## 发射器猪灵交易 (dispenserBartering)

发射金物品会触发猪灵交易

设置为ingot时，金锭会触发交易

设置为block时，金锭和金块会触发交易

设置为shulkerBox时，有且仅有金锭或金块的潜影盒会触发交易，且潜影盒以物品形式投出

- 类型: `enum`
- 默认值: `false`
- 参考选项: `false`, `ingot`, `block`, `shulkerBox`
- 分类: `LMS`, `SURVIVAL`, `CREATIVE`

## 发射器猪灵交易名称 (dispenserBarteringName)

设置触发发射器猪灵交易所需的发射器名称

如果设置为 false，则禁用该规则

设置为 true 时，命名为“bartering”的发射器会触发猪灵交易

如果设置为别的字符串，则以该字符串命名的发射器会触发猪灵交易

- 类型: `String`
- 默认值: `false`
- 参考选项: `true`, `false`
- 分类: `LMS`, `SURVIVAL`, `CREATIVE`

## 可再生鞘翅 (elytraRecipe)

添加鞘翅的制作配方

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `false`, `true`
- 分类: `LMS`, `SURVIVAL`

## 可再生附魔金苹果 (enchantedGoldenAppleRecipe)

添加附魔金苹果的制作配方

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `false`, `true`
- 分类: `LMS`, `SURVIVAL`

## 防爆紫晶芽 (explosionProofBuddingAmethyst)

将紫晶芽的爆炸抗性设置为与基岩相同

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `false`, `true`
- 分类: `LMS`, `SURVIVAL`

## 脆弱试炼刷怪笼 (fragileTrialSpawner)

将试炼刷怪笼的爆炸抗性设置为与信标相同

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `false`, `true`
- 分类: `LMS`, `SURVIVAL`

## 脆弱宝库 (fragileVault)

将宝库的爆炸抗性设置为与信标相同

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `false`, `true`
- 分类: `LMS`, `SURVIVAL`

## 全局史莱姆区块 (globalSlimeChunk)

将所有区块视为史莱姆区块

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `false`, `true`
- 分类: `LMS`, `SURVIVAL`, `CREATIVE`

## 低血量自动切换旁观 (lowHealthSpectator)

当玩家生命值在受到伤害后低于设定的阈值时，将玩家切换到旁观者模式

- 类型: `enum`
- 默认值: `false`
- 参考选项: `false`, `true`, `custom`
- 分类: `LMS`, `SURVIVAL`, `COMMAND`

## 低血量切换旁观方式 (lowHealthSpectatorMethod)

设置低生命时将玩家切换到旁观模式的方法

- 类型: `enum`
- 默认值: `vanilla`
- 参考选项: `vanilla`, `mcdreforged`, `carpetOrgAddition`, `kick`
- 分类: `LMS`, `SURVIVAL`

## 低血量切换旁观冷却 (lowHealthSpectatorCooldown)

设置玩家再次切换到旁观模式前的冷却时间

- 类型: `long`
- 默认值: `200`
- 分类: `LMS`, `SURVIVAL`

## 低血量切换旁观阈值 (lowHealthSpectatorThreshold)

玩家生命值低于此阈值时，将被切换到旁观模式

- 类型: `float`
- 默认值: `5`
- 参考选项: `5`, `10`, `15`, `20`
- 分类: `LMS`, `SURVIVAL`

## 更好的防止op作弊 (opPlayerNoCheatExtra)

禁用更多命令以防止作弊

禁用命令列表：

/clear /damage /fillbiome /forceload /item /place /spawnpoint

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `false`, `true`
- 分类: `LMS`, `SURVIVAL`, `COMMAND`

## 珍珠忽略实体碰撞 (pearlIgnoreEntityCollision)

末影珍珠忽略与实体的碰撞

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `false`, `true`
- 分类: `LMS`, `SURVIVAL`

## 命名珍珠不传送 (pearlNoTp)

阻止具有特定自定义命名的末影珍珠传送

如果设置为 false，则禁用该规则

设置为 true 时，命名为“noTp”的末影珍珠不会传送玩家

如果设置为别的字符串，则以该字符串命名的末影珍珠在撞击时不会传送玩家

- 类型: `String`
- 默认值: `false`
- 参考选项: `true`, `false`
- 分类: `LMS`, `CREATIVE`

## 玩家丢出全部物品 (playerCommandDropall)

用“dropall”替代“dropstack all”

- 类型: `String`
- 默认值: `false`
- 参考选项: `false`, `true`, `ops`, `0`, `1`, `2`, `3`, `4`
- 分类: `LMS`, `SURVIVAL`, `COMMAND`

## 潜影盒熔炉 (shulkerBoxFurnace)

按模式处理潜影盒烧制

false - 原版逻辑

force - 盒内可烧制物品在原槽位烧制完成，不可烧制物品保留原槽位，10秒后返回潜影盒

strict - 仅当盒内非空且全部可烧制时才可烧制，否则该潜影盒不可烧制

- 类型: `enum`
- 默认值: `false`
- 参考选项: `false`, `force`, `strict`
- 分类: `LMS`, `SURVIVAL`, `CREATIVE`

## 低血量潜影贝复制失败概率 (shulkerDupLowHealthFailChance)

设置潜影贝生命值低于一半时复制失败的概率

失败概率 = 1 / value

设置为0时不会失败

<0时不修改原版行为

- 类型: `int`
- 默认值: `-1`
- 分类: `LMS`, `SURVIVAL`

## 潜影贝复制附近数量限制 (shulkerDupNearbyLimit)

设置根据附近数量减少潜影贝复制几率的除数

失败概率 = （附近潜影贝数量包括自身 - 1） / value

<0时不修改原版行为

- 类型: `float`
- 默认值: `-1.0`
- 分类: `LMS`, `SURVIVAL`

## 易碎试炼刷怪笼 (softTrialSpawner)

将试炼刷怪笼的硬度设置为与信标相同

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `false`, `true`
- 分类: `LMS`, `SURVIVAL`

## 易碎宝库 (softVault)

将宝库的硬度设置为与信标相同

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `false`, `true`
- 分类: `LMS`, `SURVIVAL`

## 可再生海绵 (spongeRecipe)

添加海绵的制作配方

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `false`, `true`
- 分类: `LMS`, `SURVIVAL`

## 不可破坏紫水晶母岩 (unbreakableBuddingAmethyst)

将紫水晶母岩的硬度设置为与基岩相同

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `false`, `true`
- 分类: `LMS`, `SURVIVAL`

## 宝库黑名单上限 (vaultMaxBlacklistSize)

设置宝库黑名单中存储的最大玩家数量

<0时不修改原版行为

- 类型: `int`
- 默认值: `-1`
- 分类: `LMS`, `SURVIVAL`

## 僵尸猪灵生成修复 (zombifiedPiglinSpawnFix)

当僵尸猪人在地狱门上受随机刻而生成时，将会检测他的碰撞箱来判断是否可以生成

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `false`, `true`
- 分类: `LMS`, `SURVIVAL`, `BUGFIX`

## 仓库网页命令 (commandStorageWebsite)

启用 "/storageWebsite status", "/storageWebsite start" 和 "/storageWebsite stop" 命令

- 类型: `String`
- 默认值: `false`
- 参考选项: `false`, `true`, `ops`, `0`, `1`, `2`, `3`, `4`
- 分类: `LMS`, `COMMAND`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## 设置密码命令 (commandSetPassword)

启用 "/setPassword" 命令

- 类型: `String`
- 默认值: `false`
- 参考选项: `false`, `true`, `ops`, `0`, `1`, `2`, `3`, `4`
- 分类: `LMS`, `COMMAND`, `SURVIVAL`, `CREATIVE`

## 取物命令 (commandGetItem)

启用 "/getItem <item> <count>" 命令

- 类型: `String`
- 默认值: `false`
- 参考选项: `false`, `true`, `ops`, `0`, `1`, `2`, `3`, `4`
- 分类: `LMS`, `COMMAND`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## 仓库数据查询命令 (commandGetStorageData)

启用 "/getStorageData" 和 "/getStorageData <id>" 命令

- 类型: `String`
- 默认值: `false`
- 参考选项: `false`, `true`, `ops`, `0`, `1`, `2`, `3`, `4`
- 分类: `LMS`, `COMMAND`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## 取物假人检查命令 (commandCleanGetItemBot)

启用 "/cleanGetItemBot" 命令

- 类型: `String`
- 默认值: `false`
- 参考选项: `false`, `true`, `ops`, `0`, `1`, `2`, `3`, `4`
- 分类: `LMS`, `COMMAND`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## 网页取物 API (websiteGetItem)

启用储存网页的 getItem API

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `false`, `true`
- 分类: `LMS`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## 取物假人前缀 (getItemBotPrefix)

设置自动取物假人的命名前缀

假人名称格式为 <prefix><index>

示例：前缀为 "bot*getitem*" 时，名称会是 "bot_getitem_1"、"bot_getitem_2"……

- 类型: `String`
- 默认值: `bot_getitem_`
- 分类: `LMS`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## 取物容器调用间隔 (getItemDelayMs)

设置 getItem 中每次调用容器取物之间的等待时间（单位：毫秒）

- 类型: `int`
- 默认值: `0`
- 分类: `LMS`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## 取物限速 (getItemCooldownSeconds)

设置同一玩家名两次 getItem 请求之间的冷却时间（单位：秒，0为关闭）

- 类型: `int`
- 默认值: `0`
- 分类: `LMS`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## 仓库查询限速 (getStorageDataCooldownSeconds)

设置同一玩家名两次仓库查询之间的冷却时间（单位：秒，0为关闭）

- 类型: `int`
- 默认值: `0`
- 分类: `LMS`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## 网页登录限速 (websiteLoginCooldownSeconds)

设置同一用户名两次网页登录请求之间的冷却时间（单位：秒，0为关闭）

- 类型: `int`
- 默认值: `0`
- 分类: `LMS`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## 取物单次最大数量 (getItemMaxCount)

设置 getItem 单次请求允许的最大物品数量（0为不限制）

- 类型: `int`
- 默认值: `0`
- 分类: `LMS`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## 取物单次假人上限 (getItemMaxBots)

设置 getItem 单次请求最多可召唤的假人数量（0为不限制）

- 类型: `int`
- 默认值: `0`
- 分类: `LMS`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## 跨维度传送实体 (entityTeleportCrossDimension)

跨维度传送实体动量处理方案

设为origin时，不进行改变

设为1.21.9+时，启用1.21.9pre1+的方案

设为1.21.8-时，启用25w37a-的方案

- 类型: `String`
- 默认值: `origin`
- 参考选项: `origin`, `1.21.9+`, `1.21.8-`
- 分类: `LMS`, `SURVIVAL`, `CREATIVE`
