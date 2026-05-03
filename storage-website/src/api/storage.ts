import type { StorageErrorPosition, StorageResponse } from "@/types/storage";

const DEV_MOCK_DATA: StorageResponse[] = [
  {
    name: "test1",
    data: {
      items: {
        "minecraft:gold_ingot": {
          count: 2,
          positionsByDimension: {
            overworld: [{ pos: { x: -1, y: 100, z: 0 }, count: 2 }],
          },
        },
        "minecraft:white_shulker_box": {
          count: 1,
          positionsByDimension: {
            overworld: [{ pos: { x: -1, y: 100, z: 0 }, count: 1 }],
          },
        },
        "minecraft:shulker_box": {
          count: 2,
          positionsByDimension: {
            overworld: [{ pos: { x: -1, y: 100, z: 0 }, count: 2 }],
          },
        },
        "minecraft:command_block": {
          count: 64,
          positionsByDimension: {
            overworld: [
              {
                pos: { x: 1012, y: 112, z: 930 },
                count: 64,
              },
            ],
          },
        },
        "minecraft:barrel": {
          count: 1152,
          positionsByDimension: {
            overworld: [
              {
                pos: { x: 1, y: 100, z: 0 },
                count: 576,
              },
              { pos: { x: -1, y: 100, z: 0 }, count: 576 },
            ],
          },
        },
        "minecraft:orange_stained_glass_pane": {
          count: 39744,
          positionsByDimension: {
            overworld: [
              {
                pos: { x: 0, y: 100, z: 1 },
                count: 27648,
              },
              { pos: { x: -1, y: 100, z: 0 }, count: 12096 },
            ],
          },
        },
      },
      errors: [{ dimension: "overworld", pos: { x: 0, y: 100, z: 0 } }],
    },
  },
  {
    name: "test2",
    data: {
      items: {},
      errors: [
        { dimension: "end", pos: { x: 0, y: 0, z: 0 } },
        {
          dimension: "end",
          pos: { x: 1, y: 1, z: 1 },
        },
      ],
    },
  },
];

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

function isNumberArray(value: unknown, length: number): value is number[] {
  return (
    Array.isArray(value) &&
    value.length === length &&
    value.every((item) => typeof item === "number")
  );
}

function isPointWithCount(
  value: unknown,
): value is [number, number, number, number] {
  return isNumberArray(value, 4);
}

function isPointOnly(value: unknown): value is [number, number, number] {
  return isNumberArray(value, 3);
}

function decodeDimensionKey(key: string): string | null {
  if (key === "0") {
    return "overworld";
  }
  if (key === "-1") {
    return "nether";
  }
  if (key === "1") {
    return "end";
  }
  return null;
}

function normalizeItemId(itemId: string): string {
  if (itemId.includes(":")) {
    return itemId;
  }
  return `minecraft:${itemId}`;
}

function isLoginResponse(payload: unknown): payload is LoginResponse {
  if (!isRecord(payload)) {
    return false;
  }

  return (
    typeof payload.username === "string" && typeof payload.token === "string"
  );
}

export type StorageApiErrorCode =
  | "NETWORK_ERROR"
  | "HTTP_ERROR"
  | "INVALID_PAYLOAD"
  | "TOKEN_EXPIRED"
  | "UNAUTHORIZED"
  | "FORBIDDEN";

export interface StorageCredentials {
  username: string;
  password: string;
}

export interface LoginResponse {
  username: string;
  token: string;
}

export interface GetItemBotResult {
  botName: string;
  count: number;
  spawnCommand: string;
  killCommand: string;
  inventoryCommand: string;
}

export interface GetItemResponse {
  itemId: string;
  total: number;
  bots: GetItemBotResult[];
  lines: string[];
}

export interface SendGetItemResultResponse {
  success: boolean;
  message: string;
}

export interface SendGetItemEntry {
  itemId: string;
  count: number;
  botName: string;
}

export class StorageApiError extends Error {
  code: StorageApiErrorCode;
  status?: number;
  detail?: string;

  constructor(code: StorageApiErrorCode, status?: number, detail?: string) {
    super(code);
    this.code = code;
    this.status = status;
    this.detail = detail;
  }
}

function parseErrorMessage(payload: unknown): string | undefined {
  if (!isRecord(payload)) {
    return undefined;
  }
  if (typeof payload.message === "string") {
    return payload.message;
  }
  return undefined;
}

function isGetItemBotResult(payload: unknown): payload is GetItemBotResult {
  if (!isRecord(payload)) {
    return false;
  }

  return (
    typeof payload.botName === "string" &&
    typeof payload.count === "number" &&
    typeof payload.spawnCommand === "string" &&
    typeof payload.killCommand === "string" &&
    typeof payload.inventoryCommand === "string"
  );
}

function isSendGetItemResultResponse(
  payload: unknown,
): payload is SendGetItemResultResponse {
  return (
    isRecord(payload) &&
    typeof payload.success === "boolean" &&
    typeof payload.message === "string"
  );
}

function isCompactBotResult(
  payload: unknown,
): payload is { n: string; i: string; c: number } {
  if (!isRecord(payload)) {
    return false;
  }

  return (
    typeof payload.n === "string" &&
    typeof payload.i === "string" &&
    typeof payload.c === "number"
  );
}

function isCompactGetItemResponse(
  payload: unknown,
): payload is Array<{ n: string; i: string; c: number }> {
  return Array.isArray(payload) && payload.every(isCompactBotResult);
}

function decodeCompactStorage(payload: unknown): StorageResponse[] | null {
  if (!Array.isArray(payload)) {
    return null;
  }

  const storages: StorageResponse[] = [];
  for (const oneStorage of payload) {
    if (!isRecord(oneStorage) || typeof oneStorage.n !== "string") {
      return null;
    }
    if (
      !isRecord(oneStorage.d) ||
      !isRecord(oneStorage.d.i) ||
      !isRecord(oneStorage.d.e)
    ) {
      return null;
    }

    const items: StorageResponse["data"]["items"] = {};
    for (const [rawItemId, rawItem] of Object.entries(oneStorage.d.i)) {
      if (!isRecord(rawItem) || typeof rawItem.c !== "number") {
        return null;
      }
      const itemId = normalizeItemId(rawItemId);

      const positionsByDimension: Record<
        string,
        { pos: { x: number; y: number; z: number }; count: number }[]
      > = {};
      for (const dimKey of ["0", "-1", "1"] as const) {
        const rawPoints = rawItem[dimKey];
        const dimName = decodeDimensionKey(dimKey);
        if (dimName == null) {
          return null;
        }
        if (rawPoints == null) {
          positionsByDimension[dimName] = [];
          continue;
        }
        if (!Array.isArray(rawPoints)) {
          return null;
        }
        const decodedPoints = [];
        for (const onePoint of rawPoints) {
          if (!isPointWithCount(onePoint)) {
            return null;
          }
          const [x, y, z, c] = onePoint;
          decodedPoints.push({
            pos: { x, y, z },
            count: c,
          });
        }
        positionsByDimension[dimName] = decodedPoints;
      }

      items[itemId] = {
        count: rawItem.c,
        positionsByDimension,
      };
    }

    const errors: StorageErrorPosition[] = [];
    for (const dimKey of ["0", "-1", "1"] as const) {
      const rawErrors = oneStorage.d.e[dimKey];
      const dimName = decodeDimensionKey(dimKey);
      if (dimName == null) {
        return null;
      }
      if (rawErrors == null) {
        continue;
      }
      if (!Array.isArray(rawErrors)) {
        return null;
      }
      for (const oneError of rawErrors) {
        if (!isPointOnly(oneError)) {
          return null;
        }
        const [x, y, z] = oneError;
        errors.push({
          dimension: dimName,
          pos: { x, y, z },
        });
      }
    }

    storages.push({
      name: oneStorage.n,
      data: {
        items,
        errors,
      },
    });
  }

  return storages;
}

function decodeCompactGetItem(
  payload: unknown,
  requestedItemId: string,
): GetItemResponse | null {
  if (!isCompactGetItemResponse(payload)) {
    return null;
  }

  const total = payload.reduce((sum, one) => sum + one.c, 0);
  return {
    itemId: requestedItemId,
    total,
    bots: payload.map((bot) => ({
      botName: bot.n,
      count: bot.c,
      spawnCommand: `/player ${bot.n} spawn`,
      killCommand: `/player ${bot.n} kill`,
      inventoryCommand: `/player ${bot.n} inventory`,
    })),
    lines: [
      ...payload.map((bot) => `${bot.n}: ${normalizeItemId(bot.i)} x${bot.c}`),
      `getItem done: ${requestedItemId} x${total}`,
    ],
  };
}

export async function login(
  credentials: StorageCredentials,
): Promise<LoginResponse> {
  if (import.meta.env.DEV) {
    return Promise.resolve({
      username: credentials.username.trim() || "dev",
      token: "dev-token",
    });
  }

  let response: Response;
  try {
    response = await fetch("/api/login", {
      method: "POST",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
      body: JSON.stringify(credentials),
    });
  } catch {
    throw new StorageApiError("NETWORK_ERROR");
  }

  if (!response.ok) {
    if (response.status === 401) {
      let detail: string | undefined;
      try {
        const payload: unknown = await response.json();
        detail = parseErrorMessage(payload);
      } catch {
        // ignore parsing errors and fallback to generic unauthorized text.
      }
      if (detail === "Token expired") {
        throw new StorageApiError("TOKEN_EXPIRED", response.status, detail);
      }
      throw new StorageApiError("UNAUTHORIZED", response.status, detail);
    }
    throw new StorageApiError("HTTP_ERROR", response.status);
  }

  const payload: unknown = await response.json();
  if (!isLoginResponse(payload)) {
    throw new StorageApiError("INVALID_PAYLOAD");
  }

  return payload;
}

export async function fetchStorageData(
  token?: string | null,
): Promise<StorageResponse[]> {
  if (import.meta.env.DEV) {
    return Promise.resolve(DEV_MOCK_DATA);
  }

  let response: Response;
  const headers: Record<string, string> = {
    Accept: "application/json",
  };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  try {
    response = await fetch("/api/storage/getData", {
      method: "GET",
      headers,
    });
  } catch {
    throw new StorageApiError("NETWORK_ERROR");
  }

  if (!response.ok) {
    if (response.status === 401) {
      let detail: string | undefined;
      try {
        const payload: unknown = await response.json();
        detail = parseErrorMessage(payload);
      } catch {
        // ignore parsing errors and fallback to generic unauthorized text.
      }
      throw new StorageApiError("UNAUTHORIZED", response.status, detail);
    }
    throw new StorageApiError("HTTP_ERROR", response.status);
  }

  const payload: unknown = await response.json();
  const decoded = decodeCompactStorage(payload);
  if (decoded == null) {
    throw new StorageApiError("INVALID_PAYLOAD");
  }

  return decoded;
}

export async function requestGetItem(
  itemId: string,
  count: number,
  token?: string | null,
): Promise<GetItemResponse> {
  if (import.meta.env.DEV) {
    return Promise.resolve({
      itemId,
      total: count,
      bots: [
        {
          botName: "bot_getitem_1",
          count,
          spawnCommand: "/player bot_getitem_1 spawn",
          killCommand: "/player bot_getitem_1 kill",
          inventoryCommand: "/player bot_getitem_1 inventory",
        },
      ],
      lines: [
        `bot_getitem_1: ${itemId} x${count}`,
        `getItem done: ${itemId} x${count}`,
      ],
    });
  }

  const headers: Record<string, string> = {
    Accept: "application/json",
    "Content-Type": "application/json",
  };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  let response: Response;
  try {
    response = await fetch("/api/storage/getItem", {
      method: "POST",
      headers,
      body: JSON.stringify({ i: itemId, c: count }),
    });
  } catch {
    throw new StorageApiError("NETWORK_ERROR");
  }

  if (!response.ok) {
    let detail: string | undefined;
    try {
      const payload: unknown = await response.json();
      detail = parseErrorMessage(payload);
    } catch {
      // ignore parsing errors
    }
    if (response.status === 401) {
      throw new StorageApiError("UNAUTHORIZED", response.status, detail);
    }
    if (response.status === 403) {
      throw new StorageApiError("FORBIDDEN", response.status, detail);
    }
    throw new StorageApiError("HTTP_ERROR", response.status, detail);
  }

  const payload: unknown = await response.json();
  const decoded = decodeCompactGetItem(payload, itemId);
  if (decoded == null || !decoded.bots.every(isGetItemBotResult)) {
    throw new StorageApiError("INVALID_PAYLOAD");
  }
  return decoded;
}

export async function requestSendGetItemResult(
  entries: SendGetItemEntry[],
  token: string,
): Promise<SendGetItemResultResponse> {
  if (import.meta.env.DEV) {
    return Promise.resolve({
      success: true,
      message: "Sent to player",
    });
  }

  let response: Response;
  try {
    response = await fetch("/api/storage/sendGetItemResult", {
      method: "POST",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(
        entries.map((entry) => ({
          i: entry.itemId,
          c: entry.count,
          n: entry.botName,
        })),
      ),
    });
  } catch {
    throw new StorageApiError("NETWORK_ERROR");
  }

  if (!response.ok) {
    let detail: string | undefined;
    try {
      const payload: unknown = await response.json();
      detail = parseErrorMessage(payload);
    } catch {
      // ignore parsing errors
    }
    if (response.status === 401) {
      throw new StorageApiError("UNAUTHORIZED", response.status, detail);
    }
    if (response.status === 403) {
      throw new StorageApiError("FORBIDDEN", response.status, detail);
    }
    throw new StorageApiError("HTTP_ERROR", response.status, detail);
  }

  const payload: unknown = await response.json();
  if (!isSendGetItemResultResponse(payload)) {
    throw new StorageApiError("INVALID_PAYLOAD");
  }
  return payload;
}
