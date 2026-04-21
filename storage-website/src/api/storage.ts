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

function isPosition(value: unknown): boolean {
  if (!isRecord(value)) {
    return false;
  }

  return (
    typeof value.x === "number" &&
    typeof value.y === "number" &&
    typeof value.z === "number"
  );
}

function isErrorPosition(value: unknown): value is StorageErrorPosition {
  if (!isRecord(value)) {
    return false;
  }

  return typeof value.dimension === "string" && isPosition(value.pos);
}

function isStorageResponse(value: unknown): value is StorageResponse {
  if (!isRecord(value) || typeof value.name !== "string") {
    return false;
  }

  const data = value.data;
  if (!isRecord(data) || !isRecord(data.items) || !Array.isArray(data.errors)) {
    return false;
  }

  return data.errors.every(isErrorPosition);
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
  | "UNAUTHORIZED";

export interface StorageCredentials {
  username: string;
  password: string;
}

export interface LoginResponse {
  username: string;
  token: string;
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
  if (!Array.isArray(payload) || !payload.every(isStorageResponse)) {
    throw new StorageApiError("INVALID_PAYLOAD");
  }

  return payload;
}
