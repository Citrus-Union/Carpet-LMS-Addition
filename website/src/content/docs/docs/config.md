---
title: Config
description: Runtime JSON files under config/ and world/
---

## checkStorageConfig

- Path: `world/carpetlmsaddition/checkStorageConfig.json`
- Purpose: Read by the storage data API and the built-in website feature. Includes port, auto-start flag, whether password is required, token expiry days, and the storage list filenames to process.
- Generated during world load if missing.
- Example:

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

- `noPassword: false`:
  - `/api/storage/getData` requires `Authorization: Bearer <token>`.
  - `/api/storage/getItem` requires `Authorization: Bearer <token>`.
  - `/api/storage/sendGetItemResult` requires `Authorization: Bearer <token>`.
- `noPassword: true`:
  - `/api/storage/getData` can be accessed without token.
  - `/api/storage/getItem` can be accessed without token.
  - `/api/storage/sendGetItemResult` still requires `Authorization: Bearer <token>`.
- If a token is provided:
  - `/api/storage/getData` and `/api/storage/getItem` try to validate it first.
  - When `noPassword: true` and token validation fails, these two APIs fall back to anonymous access.
  - `/api/storage/sendGetItemResult` never falls back to anonymous access.
- `customWebsite: false`: use the built-in storage website.
- `customWebsite: true`: load files from `world/carpetlmsaddition/customStorageWebsite/` (the mod ensures `index.html` exists automatically).

## checkStorageList

- Path: `world/carpetlmsaddition/checkStorageList/*.json`
- Purpose: Coordinate list read by the storage data API. Which files are read is controlled by `storageList` in `checkStorageConfig.json`.
- Example file: `world/carpetlmsaddition/checkStorageList/example.json`
- If `storageList` contains `example.json`, the mod generates this example file during world load when it is missing.
- Example:

```json
{
  "overworld": [[0, 0, 0]],
  "end": [],
  "nether": []
}
```
