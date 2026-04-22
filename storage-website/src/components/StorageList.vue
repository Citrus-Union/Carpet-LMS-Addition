<script setup lang="ts">
import { ref, watch, type PropType } from "vue";
import { useI18n } from "vue-i18n";

import type { FlattenedPosition } from "@/composables/useStorageDashboard";
import type { StorageItem, StorageResponse } from "@/types/storage";

interface Props {
  storages: StorageResponse[];
  loading: boolean;
  errorMessage: string;
  getItemDisplayName: (itemId: string) => string;
  flattenPositions: (item: StorageItem) => FlattenedPosition[];
  getDimensionLabel: (dimension: string) => string;
  onGetItem: (itemId: string) => void | Promise<void>;
}

type SortMode = "itemIdAsc" | "itemIdDesc" | "countAsc" | "countDesc";

interface ItemRow {
  itemId: string;
  item: StorageItem;
}

const props = defineProps({
  storages: {
    type: Array as PropType<StorageResponse[]>,
    required: true,
  },
  loading: {
    type: Boolean,
    required: true,
  },
  errorMessage: {
    type: String,
    required: true,
  },
  getItemDisplayName: {
    type: Function as PropType<Props["getItemDisplayName"]>,
    required: true,
  },
  flattenPositions: {
    type: Function as PropType<Props["flattenPositions"]>,
    required: true,
  },
  getDimensionLabel: {
    type: Function as PropType<Props["getDimensionLabel"]>,
    required: true,
  },
  onGetItem: {
    type: Function as PropType<Props["onGetItem"]>,
    required: true,
  },
});

const { t } = useI18n();

const expandedItemKeys = ref<Set<string>>(new Set());
const expandedErrorKeys = ref<Set<string>>(new Set());
const sortModeByStorage = ref<Record<string, SortMode>>({});
const searchQueryByStorage = ref<Record<string, string>>({});

watch(
  () => props.storages,
  (storages) => {
    expandedItemKeys.value = new Set();
    expandedErrorKeys.value = new Set();

    const nextSortModeByStorage: Record<string, SortMode> = {};
    const nextSearchQueryByStorage: Record<string, string> = {};
    storages.forEach((storage) => {
      nextSortModeByStorage[storage.name] =
        sortModeByStorage.value[storage.name] ?? "itemIdAsc";
      nextSearchQueryByStorage[storage.name] =
        searchQueryByStorage.value[storage.name] ?? "";
    });
    sortModeByStorage.value = nextSortModeByStorage;
    searchQueryByStorage.value = nextSearchQueryByStorage;
  },
  { deep: true },
);

function getItemKey(storageName: string, itemId: string): string {
  return `${storageName}::${itemId}`;
}

function isExpanded(storageName: string, itemId: string): boolean {
  return expandedItemKeys.value.has(getItemKey(storageName, itemId));
}

function toggleExpanded(storageName: string, itemId: string): void {
  const key = getItemKey(storageName, itemId);
  const next = new Set(expandedItemKeys.value);

  if (next.has(key)) {
    next.delete(key);
  } else {
    next.add(key);
  }

  expandedItemKeys.value = next;
}

function isErrorExpanded(storageName: string): boolean {
  return expandedErrorKeys.value.has(storageName);
}

function toggleErrorExpanded(storageName: string): void {
  const next = new Set(expandedErrorKeys.value);

  if (next.has(storageName)) {
    next.delete(storageName);
  } else {
    next.add(storageName);
  }

  expandedErrorKeys.value = next;
}

function setSortMode(storageName: string, mode: SortMode): void {
  sortModeByStorage.value = {
    ...sortModeByStorage.value,
    [storageName]: mode,
  };
}

function getSortMode(storageName: string): SortMode {
  return sortModeByStorage.value[storageName] ?? "itemIdAsc";
}

function isSortModeActive(storageName: string, mode: SortMode): boolean {
  return getSortMode(storageName) === mode;
}

function getSearchQuery(storageName: string): string {
  return searchQueryByStorage.value[storageName] ?? "";
}

function setSearchQuery(storageName: string, value: string): void {
  searchQueryByStorage.value = {
    ...searchQueryByStorage.value,
    [storageName]: value,
  };
}

function onSearchInput(storageName: string, event: Event): void {
  const value = (event.target as HTMLInputElement).value;
  setSearchQuery(storageName, value);
}

function normalizeSearchText(value: string): string {
  return value.replace(/\s+/g, "").toLowerCase();
}

function sortItemRows(rows: ItemRow[], mode: SortMode): ItemRow[] {
  const sorted = [...rows];

  sorted.sort((left, right) => {
    if (mode === "itemIdAsc") {
      return left.itemId.localeCompare(right.itemId);
    }
    if (mode === "itemIdDesc") {
      return right.itemId.localeCompare(left.itemId);
    }
    if (mode === "countAsc") {
      const byCount = left.item.count - right.item.count;
      if (byCount !== 0) {
        return byCount;
      }
      return left.itemId.localeCompare(right.itemId);
    }

    const byCount = right.item.count - left.item.count;
    if (byCount !== 0) {
      return byCount;
    }
    return left.itemId.localeCompare(right.itemId);
  });

  return sorted;
}

function getSortedItemRows(storage: StorageResponse): ItemRow[] {
  const rows: ItemRow[] = Object.entries(storage.data.items).map(
    ([itemId, item]) => ({
      itemId,
      item,
    }),
  );

  return sortItemRows(rows, getSortMode(storage.name));
}

function getVisibleItemRows(storage: StorageResponse): ItemRow[] {
  const sortedRows = getSortedItemRows(storage);
  const normalizedQuery = normalizeSearchText(getSearchQuery(storage.name));

  if (!normalizedQuery) {
    return sortedRows;
  }

  return sortedRows.filter((row) => {
    const normalizedName = normalizeSearchText(
      props.getItemDisplayName(row.itemId),
    );
    const normalizedId = normalizeSearchText(row.itemId);

    return (
      normalizedName.includes(normalizedQuery) ||
      normalizedId.includes(normalizedQuery)
    );
  });
}
</script>

<template>
  <section
    v-if="loading"
    class="rounded-2xl border border-slate-800 bg-slate-900/60 p-8 text-center text-slate-300"
  >
    {{ t("status.loading") }}
  </section>

  <section
    v-else-if="errorMessage"
    class="rounded-2xl border border-rose-700/60 bg-rose-950/30 p-6 text-sm text-rose-200"
  >
    {{ t("status.loadingFailed") }}: {{ errorMessage }}
  </section>

  <section
    v-else-if="storages.length === 0"
    class="rounded-2xl border border-slate-800 bg-slate-900/60 p-8 text-center text-slate-300"
  >
    {{ t("status.noData") }}
  </section>

  <section v-else class="space-y-4">
    <article
      v-for="storage in storages"
      :key="storage.name"
      class="overflow-hidden rounded-2xl border border-slate-800 bg-slate-900/70"
    >
      <header class="border-b border-slate-800 bg-slate-900/90 px-5 py-4">
        <div
          class="flex flex-col gap-2 md:flex-row md:items-center md:justify-between"
        >
          <h2 class="text-lg font-semibold text-slate-100">
            {{ storage.name }}
          </h2>
          <input
            :value="getSearchQuery(storage.name)"
            class="w-full rounded-md border border-slate-700 bg-slate-950 px-3 py-1.5 text-sm text-slate-100 outline-none ring-cyan-500 placeholder:text-slate-500 focus:ring-2 md:w-72"
            :placeholder="t('storage.searchPlaceholder')"
            type="text"
            @input="onSearchInput(storage.name, $event)"
          />
        </div>
      </header>

      <div class="space-y-5 p-5">
        <section class="space-y-3">
          <h3 class="text-sm font-semibold tracking-wide text-slate-300">
            {{ t("section.items") }}
          </h3>
          <div
            v-if="Object.keys(storage.data.items).length === 0"
            class="text-sm text-slate-400"
          >
            {{ t("storage.noItems") }}
          </div>
          <div
            v-else
            class="overflow-x-auto rounded-lg border border-slate-800"
          >
            <table
              class="min-w-full divide-y divide-slate-800 text-left text-sm"
            >
              <thead class="bg-slate-900/95 text-slate-300">
                <tr>
                  <th class="px-3 py-2 font-medium">
                    {{ t("table.itemName") }}
                  </th>
                  <th class="px-3 py-2 font-medium">
                    <span class="inline-flex items-center gap-2">
                      {{ t("table.itemId") }}
                      <span class="inline-flex gap-1">
                        <button
                          class="text-xs transition hover:text-cyan-300"
                          :class="
                            isSortModeActive(storage.name, 'itemIdAsc')
                              ? 'text-cyan-300'
                              : 'text-slate-500'
                          "
                          @click="setSortMode(storage.name, 'itemIdAsc')"
                        >
                          ↑
                        </button>
                        <button
                          class="text-xs transition hover:text-cyan-300"
                          :class="
                            isSortModeActive(storage.name, 'itemIdDesc')
                              ? 'text-cyan-300'
                              : 'text-slate-500'
                          "
                          @click="setSortMode(storage.name, 'itemIdDesc')"
                        >
                          ↓
                        </button>
                      </span>
                    </span>
                  </th>
                  <th class="px-3 py-2 font-medium">
                    <span class="inline-flex items-center gap-2">
                      {{ t("table.totalCount") }}
                      <span class="inline-flex gap-1">
                        <button
                          class="text-xs transition hover:text-cyan-300"
                          :class="
                            isSortModeActive(storage.name, 'countAsc')
                              ? 'text-cyan-300'
                              : 'text-slate-500'
                          "
                          @click="setSortMode(storage.name, 'countAsc')"
                        >
                          ↑
                        </button>
                        <button
                          class="text-xs transition hover:text-cyan-300"
                          :class="
                            isSortModeActive(storage.name, 'countDesc')
                              ? 'text-cyan-300'
                              : 'text-slate-500'
                          "
                          @click="setSortMode(storage.name, 'countDesc')"
                        >
                          ↓
                        </button>
                      </span>
                    </span>
                  </th>
                  <th class="px-3 py-2 font-medium">
                    {{ t("table.details") }}
                  </th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-800">
                <template
                  v-for="row in getVisibleItemRows(storage)"
                  :key="row.itemId"
                >
                  <tr class="bg-slate-900/40">
                    <td class="px-3 py-2 text-slate-100">
                      {{ getItemDisplayName(row.itemId) }}
                    </td>
                    <td class="px-3 py-2 font-mono text-xs text-slate-200">
                      <div class="flex items-center justify-between gap-2">
                        <span class="truncate">{{ row.itemId }}</span>
                        <button
                          class="shrink-0 rounded-md border border-cyan-700/70 px-2 py-1 text-[11px] text-cyan-300 transition hover:border-cyan-400 hover:text-cyan-200"
                          @click="props.onGetItem(row.itemId)"
                        >
                          {{ t("actions.getItem") }}
                        </button>
                      </div>
                    </td>
                    <td class="px-3 py-2 text-slate-100">
                      {{ row.item.count }}
                    </td>
                    <td class="px-3 py-2 text-slate-300">
                      <button
                        class="rounded-md border border-slate-600 px-2 py-1 text-xs transition hover:border-cyan-400 hover:text-cyan-300"
                        @click="toggleExpanded(storage.name, row.itemId)"
                      >
                        {{
                          isExpanded(storage.name, row.itemId)
                            ? t("actions.collapse")
                            : t("actions.expand")
                        }}
                      </button>
                    </td>
                  </tr>
                  <tr
                    v-if="isExpanded(storage.name, row.itemId)"
                    class="bg-slate-900/20"
                  >
                    <td colspan="4" class="px-3 py-3">
                      <div
                        v-if="flattenPositions(row.item).length === 0"
                        class="text-xs text-slate-400"
                      >
                        {{ t("storage.noPositionDetails") }}
                      </div>
                      <ul v-else class="grid gap-2 md:grid-cols-2">
                        <li
                          v-for="(point, pointIndex) in flattenPositions(
                            row.item,
                          )"
                          :key="`${storage.name}-${row.itemId}-${pointIndex}`"
                          class="rounded-md border border-slate-700 bg-slate-900/50 px-3 py-2"
                        >
                          <p class="text-xs text-slate-300">
                            {{ t("table.dimension") }}:
                            {{ getDimensionLabel(point.dimension) }}
                          </p>
                          <p class="font-mono text-xs text-slate-300">
                            {{ t("table.coordinate") }}: ({{ point.pos.x }}
                            {{ point.pos.y }} {{ point.pos.z }})
                          </p>
                          <p class="text-xs text-cyan-300">
                            {{ t("table.positionCount") }}:
                            {{ point.count }}
                          </p>
                        </li>
                      </ul>
                    </td>
                  </tr>
                </template>
              </tbody>
            </table>
          </div>
        </section>

        <section class="space-y-3">
          <div class="flex items-center justify-between">
            <h3 class="text-sm font-semibold tracking-wide text-slate-300">
              {{ t("section.errors") }}
            </h3>
            <button
              v-if="storage.data.errors.length > 0"
              class="rounded-md border border-slate-600 px-2 py-1 text-xs transition hover:border-cyan-400 hover:text-cyan-300"
              @click="toggleErrorExpanded(storage.name)"
            >
              {{
                isErrorExpanded(storage.name)
                  ? t("actions.collapse")
                  : t("actions.expand")
              }}
            </button>
          </div>
          <div
            v-if="storage.data.errors.length === 0"
            class="text-sm text-slate-400"
          >
            {{ t("storage.noErrors") }}
          </div>
          <ul
            v-else-if="isErrorExpanded(storage.name)"
            class="grid gap-2 md:grid-cols-2"
          >
            <li
              v-for="(error, index) in storage.data.errors"
              :key="`${storage.name}-${index}`"
              class="rounded-lg border border-amber-700/40 bg-amber-950/20 px-3 py-2 text-sm text-amber-200"
            >
              {{ getDimensionLabel(error.dimension) }} ({{ error.pos.x }}
              {{ error.pos.y }} {{ error.pos.z }})
            </li>
          </ul>
          <p v-else class="text-sm text-slate-400">
            {{ t("actions.expand") }} {{ t("section.errors") }}
          </p>
        </section>
      </div>
    </article>
  </section>
</template>
