---
title: Rules
description: Overview of the rules for Carpet LMS Addition
---

## allayHealInterval

Control the interval of allay healing (unit: gt)

0 represents no healing

<0 keeps vanilla behavior

- Type: `int`
- Default value: `-1`
- Categories: `LMS`, `SURVIVAL`

## commandLMSSelf

View or modify your own /lms settings

- Type: `String`
- Default value: `true`
- Suggested options: `false`, `true`, `ops`, `0`, `1`, `2`, `3`, `4`
- Categories: `LMS`, `SURVIVAL`, `COMMAND`

## commandLMSOthers

View or modify other players' /lms settings

- Type: `String`
- Default value: `ops`
- Suggested options: `false`, `true`, `ops`, `0`, `1`, `2`, `3`, `4`
- Categories: `LMS`, `SURVIVAL`, `COMMAND`

## commandLMSBot

View or modify bot's /lms settings

- Type: `String`
- Default value: `true`
- Suggested options: `false`, `true`, `ops`, `0`, `1`, `2`, `3`, `4`
- Categories: `LMS`, `SURVIVAL`, `COMMAND`

## breakingRestriction

Enable per-player block breaking restriction configured by /lms

- Type: `boolean`
- Default value: `false`
- Suggested options: `false`, `true`
- Categories: `LMS`, `SURVIVAL`, `COMMAND`

## dispenserBartering

Trigger piglin bartering by dispensing gold items

When set to ingot, dispensing a gold ingot triggers bartering

When set to block, dispensing a gold ingot or gold block triggers bartering

When set to shulkerBox, dispensing a shulker box that contains only gold ingots or gold blocks triggers bartering, and the shulker box is dispensed as an item

- Type: `enum`
- Default value: `false`
- Suggested options: `false`, `ingot`, `block`, `shulkerBox`
- Categories: `LMS`, `SURVIVAL`, `CREATIVE`

## dispenserBarteringName

Set the dispenser name required to trigger dispenser bartering

If set to false, the rule is disabled

When set to true, only a dispenser named "bartering" will trigger piglin bartering

If set to a different string, only a dispenser named that string will trigger piglin bartering

- Type: `String`
- Default value: `false`
- Suggested options: `false`, `true`
- Categories: `LMS`, `SURVIVAL`, `CREATIVE`

## elytraRecipe

Add a crafting recipe to make elytra renewable

- Type: `boolean`
- Default value: `false`
- Suggested options: `false`, `true`
- Categories: `LMS`, `SURVIVAL`

## enchantedGoldenAppleRecipe

Add a crafting recipe to make enchanted golden apples renewable

- Type: `boolean`
- Default value: `false`
- Suggested options: `false`, `true`
- Categories: `LMS`, `SURVIVAL`

## explosionProofBuddingAmethyst

Set the blast resistance of budding amethyst to be the same as bedrock

- Type: `boolean`
- Default value: `false`
- Suggested options: `false`, `true`
- Categories: `LMS`, `SURVIVAL`

## fragileTrialSpawner

Set the blast resistance of trial spawner to be the same as beacon

- Type: `boolean`
- Default value: `false`
- Suggested options: `false`, `true`
- Categories: `LMS`, `SURVIVAL`

## fragileVault

Set the blast resistance of vault to be the same as beacon

- Type: `boolean`
- Default value: `false`
- Suggested options: `false`, `true`
- Categories: `LMS`, `SURVIVAL`

## globalSlimeChunk

Treat every chunk as a slime chunk

- Type: `boolean`
- Default value: `false`
- Suggested options: `false`, `true`
- Categories: `LMS`, `SURVIVAL`, `CREATIVE`

## lowHealthSpectator

Switches the player to spectator mode when their health falls below the configured threshold after taking damage

- Type: `enum`
- Default value: `false`
- Suggested options: `false`, `true`, `custom`
- Categories: `LMS`, `SURVIVAL`, `COMMAND`

## lowHealthSpectatorMethod

Defines the method used to switch the player into spectator mode

- Type: `enum`
- Default value: `vanilla`
- Suggested options: `vanilla`, `mcdreforged`, `carpetOrgAddition`, `kick`
- Categories: `LMS`, `SURVIVAL`

## lowHealthSpectatorCooldown

Sets a cooldown before the player can be switched to spectator mode again

- Type: `long`
- Default value: `200`
- Categories: `LMS`, `SURVIVAL`

## lowHealthSpectatorThreshold

The health threshold below which the player will be switched to spectator mode

- Type: `float`
- Default value: `5`
- Suggested options: `5`, `10`, `15`, `20`
- Categories: `LMS`, `SURVIVAL`

## opPlayerNoCheatExtra

Disable some more command to prevent cheating

Affects command list:

/clear /damage /fillbiome /forceload /item /place /spawnpoint

- Type: `boolean`
- Default value: `false`
- Suggested options: `false`, `true`
- Categories: `LMS`, `SURVIVAL`, `COMMAND`

## pearlIgnoreEntityCollision

Ender pearls ignore collisions with entities

- Type: `boolean`
- Default value: `false`
- Suggested options: `false`, `true`
- Categories: `LMS`, `SURVIVAL`

## pearlNoTp

Prevent teleportation when an ender pearl with a specific custom name impacts

If set to false, the rule is disabled

When set to true, an ender pearl named "noTp" will not teleport the player on impact

If set to a different string, an ender pearl named that string will not teleport the player on impact

- Type: `String`
- Default value: `false`
- Suggested options: `false`, `true`
- Categories: `LMS`, `CREATIVE`

## playerCommandDropall

Use "dropall" instead of "dropstack all"

- Type: `String`
- Default value: `false`
- Suggested options: `false`, `true`, `ops`, `0`, `1`, `2`, `3`, `4`
- Categories: `LMS`, `SURVIVAL`, `COMMAND`

## shulkerBoxFurnace

Smelt shulker boxes with mode-based behavior

false - Vanilla behavior

force - Smelt all smeltable items in place, keep non-smeltable items in place, return the box after 10s

strict - Only works for non-empty boxes where all items are smeltable; otherwise the box cannot be smelted

- Type: `enum`
- Default value: `false`
- Suggested options: `false`, `force`, `strict`
- Categories: `LMS`, `SURVIVAL`, `CREATIVE`

## shulkerDupLowHealthFailChance

Sets the failure chance of shulker duplication when below half health

Failure chance = 1 / value

0 disables failure

<0 keeps vanilla behavior

- Type: `int`
- Default value: `-1`
- Categories: `LMS`, `SURVIVAL`

## shulkerDupNearbyLimit

Sets the divisor used to reduce shulker duplication chance based on nearby count

Failure chance = (nearbyShulkersIncludingSelf - 1) / value

<0 keeps vanilla behavior

- Type: `float`
- Default value: `-1.0`
- Categories: `LMS`, `SURVIVAL`

## softTrialSpawner

Set the hardness of trial spawner to be the same as beacon

- Type: `boolean`
- Default value: `false`
- Suggested options: `false`, `true`
- Categories: `LMS`, `SURVIVAL`

## softVault

Set the hardness of vault to be the same as beacon

- Type: `boolean`
- Default value: `false`
- Suggested options: `false`, `true`
- Categories: `LMS`, `SURVIVAL`

## spongeRecipe

Add a crafting recipe to make sponges renewable

- Type: `boolean`
- Default value: `false`
- Suggested options: `false`, `true`
- Categories: `LMS`, `SURVIVAL`

## unbreakableBuddingAmethyst

Set the hardness of budding amethyst to be the same as bedrock

- Type: `boolean`
- Default value: `false`
- Suggested options: `false`, `true`
- Categories: `LMS`, `SURVIVAL`

## vaultMaxBlacklistSize

Set the maximum number of players stored in the vault's blacklist

<0 keeps vanilla behavior

- Type: `int`
- Default value: `-1`
- Categories: `LMS`, `SURVIVAL`

## zombifiedPiglinSpawnFix

When spawning zombified piglins in nether gates during random ticks, game will check for its hitbox

- Type: `boolean`
- Default value: `false`
- Suggested options: `false`, `true`
- Categories: `LMS`, `SURVIVAL`, `BUGFIX`

## commandStorageWebsite

Enable "/storageWebsite status", "/storageWebsite start" and "/storageWebsite stop" command

- Type: `String`
- Default value: `false`
- Suggested options: `false`, `true`, `ops`, `0`, `1`, `2`, `3`, `4`
- Categories: `LMS`, `COMMAND`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## commandSetPassword

Enable "/setPassword" command

- Type: `String`
- Default value: `false`
- Suggested options: `false`, `true`, `ops`, `0`, `1`, `2`, `3`, `4`
- Categories: `LMS`, `COMMAND`, `SURVIVAL`, `CREATIVE`

## commandGetItem

Enable "/getItem <item> <count>" command

- Type: `String`
- Default value: `false`
- Suggested options: `false`, `true`, `ops`, `0`, `1`, `2`, `3`, `4`
- Categories: `LMS`, `COMMAND`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## commandGetStorageData

Enable "/getStorageData" and "/getStorageData <id>" command

- Type: `String`
- Default value: `false`
- Suggested options: `false`, `true`, `ops`, `0`, `1`, `2`, `3`, `4`
- Categories: `LMS`, `COMMAND`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## commandCleanGetItemBot

Enable "/cleanGetItemBot" command

- Type: `String`
- Default value: `false`
- Suggested options: `false`, `true`, `ops`, `0`, `1`, `2`, `3`, `4`
- Categories: `LMS`, `COMMAND`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## websiteGetItem

Enable getItem API for storage website

- Type: `boolean`
- Default value: `false`
- Suggested options: `false`, `true`
- Categories: `LMS`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## getItemBotPrefix

Set the naming prefix for auto item-fetch bots

Bot name format is <prefix><index>

Example: if prefix is "bot*getitem*", names are "bot_getitem_1", "bot_getitem_2", ...

- Type: `String`
- Default value: `bot_getitem_`
- Categories: `LMS`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## getItemDelayMs

Set the wait time between each container-fetch call in getItem (milliseconds)

- Type: `int`
- Default value: `0`
- Categories: `LMS`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## getItemCooldownSeconds

Set per-player cooldown between getItem requests (seconds, 0 = disabled)

- Type: `int`
- Default value: `0`
- Categories: `LMS`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## getStorageDataCooldownSeconds

Set per-player cooldown between storage data queries (seconds, 0 = disabled)

- Type: `int`
- Default value: `0`
- Categories: `LMS`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## websiteLoginCooldownSeconds

Set per-username cooldown between website login requests (seconds, 0 = disabled)

- Type: `int`
- Default value: `0`
- Categories: `LMS`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## getItemMaxCount

Set the maximum item count accepted by getItem in a single request (0 = unlimited)

- Type: `int`
- Default value: `0`
- Categories: `LMS`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## getItemMaxBots

Set the maximum number of fake players that getItem may summon per request (0 = unlimited)

- Type: `int`
- Default value: `0`
- Categories: `LMS`, `SURVIVAL`, `CREATIVE`, `STORAGE`

## entityTeleportCrossDimension

Set how entity motion is handled during cross-dimension teleportation

When set to origin, keeps vanilla behavior

When set to 1.21.9+, uses the 1.21.9-pre1+ behavior

When set to 1.21.8-, uses the 25w37a-and-earlier behavior

- Type: `String`
- Default value: `origin`
- Suggested options: `origin`, `1.21.9+`, `1.21.8-`
- Categories: `LMS`, `SURVIVAL`, `CREATIVE`
