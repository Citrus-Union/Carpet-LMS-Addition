export interface StorageItem {
  count: number;
}

export interface BlockPos {
  x: number;
  y: number;
  z: number;
}

export interface StorageErrorPosition {
  dimension: string;
  pos: BlockPos;
}

export interface StorageData {
  items: Record<string, StorageItem>;
  errors: StorageErrorPosition[];
}

export interface StorageResponse {
  name: string;
  data: StorageData;
}
