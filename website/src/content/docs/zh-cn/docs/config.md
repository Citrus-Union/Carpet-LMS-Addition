---
title: 配置
description: "`config/` 与 `world/` 下的运行时 JSON 文件"
---

## checkStorageConfig

- 路径：`world/carpetlmsaddition/checkStorageConfig.json`
- 用途：被仓储数据 API 与内置网站功能读取。包含端口、是否自动启动网站、是否需要密码、令牌过期天数，以及要处理的仓储列表文件名。
- 若文件不存在，会在世界加载时自动生成。
- 示例：

```json
{
  "port": 7000,
  "autoStartWebsite": false,
  "customWebsite": false,
  "noPassword": false,
  "expireDay": 0,
  "storageList": ["example.json"]
}
```

- `noPassword: false`
  - `/api/storage/getData` 需要 `Authorization: Bearer <token>`。
  - `/api/storage/getItem` 需要 `Authorization: Bearer <token>`。
  - `/api/storage/sendGetItemResult` 需要 `Authorization: Bearer <token>`。
- `noPassword: true`
  - `/api/storage/getData` 可不带 token 访问。
  - `/api/storage/getItem` 可不带 token 访问。
  - `/api/storage/sendGetItemResult` 仍然需要 `Authorization: Bearer <token>`。
- 如果请求里带了 token
  - `/api/storage/getData` 与 `/api/storage/getItem` 会先尝试校验 token。
  - 当 `noPassword: true` 且 token 校验失败时，这两个 API 会回退为匿名访问。
  - `/api/storage/sendGetItemResult` 不会回退为匿名访问。
- `customWebsite: false`：使用内置仓储网站。
- `customWebsite: true`：从 `world/carpetlmsaddition/customStorageWebsite/` 加载文件（模组会自动确保 `index.html` 存在）。

## checkStorageList

- 路径：`world/carpetlmsaddition/checkStorageList/*.json`
- 用途：仓储数据 API 读取的坐标列表。具体读取哪些文件由 `checkStorageConfig.json` 中的 `storageList` 控制。
- 示例文件：`world/carpetlmsaddition/checkStorageList/example.json`
- 如果 `storageList` 包含 `example.json`，且该文件不存在，模组会在世界加载时自动生成这个示例文件。
- 示例：

```json
{
  "overworld": [[0, 0, 0]],
  "end": [],
  "nether": []
}
```
