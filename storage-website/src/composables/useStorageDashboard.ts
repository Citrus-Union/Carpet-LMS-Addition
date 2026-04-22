import { computed, onMounted, ref, watch } from "vue";
import { useI18n } from "vue-i18n";

import {
  fetchStorageData,
  requestGetItem,
  type GetItemResponse,
  login,
  StorageApiError,
  type StorageCredentials,
} from "@/api/storage";
import { STORAGE_WEBSITE_LOCALE_KEY, type AppLocale } from "@/i18n";
import type {
  PositionCount,
  StorageItem,
  StorageResponse,
} from "@/types/storage";

const STORAGE_WEBSITE_AUTH_TOKEN_KEY = "storageWebsite.authToken";
const STORAGE_WEBSITE_AUTH_USERNAME_KEY = "storageWebsite.authUsername";

type DashboardErrorCode =
  | "NETWORK_ERROR"
  | "HTTP_ERROR"
  | "INVALID_PAYLOAD"
  | "UNAUTHORIZED"
  | "FORBIDDEN"
  | "TOKEN_EXPIRED"
  | "CREDENTIALS_REQUIRED"
  | "UNKNOWN";

interface DashboardErrorState {
  code: DashboardErrorCode;
  status?: number;
  detail?: string;
}

export interface FlattenedPosition {
  dimension: string;
  pos: PositionCount["pos"];
  count: number;
}

export function useStorageDashboard() {
  const { t, locale, te } = useI18n();

  const storages = ref<StorageResponse[]>([]);
  const loading = ref(false);
  const refreshedAt = ref<Date | null>(null);
  const mojangLoaded = ref(false);
  const mojangMap = ref<Record<string, string>>({});
  const credentials = ref<StorageCredentials>({
    username: "",
    password: "",
  });
  const authToken = ref<string | null>(
    window.localStorage.getItem(STORAGE_WEBSITE_AUTH_TOKEN_KEY),
  );
  const authUsername = ref<string | null>(
    window.localStorage.getItem(STORAGE_WEBSITE_AUTH_USERNAME_KEY),
  );
  const requiresLogin = ref(false);
  const errorState = ref<DashboardErrorState | null>(null);
  const getItemModalVisible = ref(false);
  const getItemLoading = ref(false);
  const getItemResult = ref<GetItemResponse | null>(null);
  const getItemErrorMessage = ref("");
  const getItemCopyMessage = ref("");
  const getItemManualCopyBotName = ref("");
  const getItemManualCopyText = ref("");
  let copyMessageTimer: number | null = null;

  const currentLocale = computed<AppLocale>(() =>
    locale.value === "zh-CN" ? "zh-CN" : "en-US",
  );
  const isAuthenticated = computed<boolean>(() => Boolean(authToken.value));
  const isAnonymousAccess = computed<boolean>(
    () => !requiresLogin.value && !isAuthenticated.value,
  );
  const showData = computed<boolean>(
    () => isAuthenticated.value || isAnonymousAccess.value,
  );
  const mojangCache = new Map<AppLocale, Record<string, string>>();
  let mojangLoadToken = 0;

  const storageCount = computed(() => storages.value.length);
  const totalItemKinds = computed(() =>
    storages.value.reduce(
      (sum, storage) => sum + Object.keys(storage.data.items).length,
      0,
    ),
  );
  const totalErrors = computed(() =>
    storages.value.reduce(
      (sum, storage) => sum + storage.data.errors.length,
      0,
    ),
  );

  const errorMessage = computed(() => {
    if (!errorState.value) {
      return "";
    }

    if (errorState.value.code === "HTTP_ERROR") {
      return t("errors.http", { status: errorState.value.status ?? "-" });
    }

    if (errorState.value.code === "NETWORK_ERROR") {
      return t("errors.network");
    }

    if (errorState.value.code === "INVALID_PAYLOAD") {
      return t("errors.invalidPayload");
    }

    if (errorState.value.code === "TOKEN_EXPIRED") {
      return t("errors.tokenExpired");
    }

    if (errorState.value.code === "UNAUTHORIZED") {
      const detail = errorState.value.detail;
      if (detail === "Invalid token") {
        return t("errors.invalidToken");
      }
      if (detail === "Username is empty") {
        return t("errors.unauthorizedUsernameEmpty");
      }
      if (detail === "Password is empty") {
        return t("errors.unauthorizedPasswordEmpty");
      }
      if (detail === "Invalid username or password") {
        return t("errors.unauthorized");
      }
      if (detail === "Database connection error") {
        return t("errors.unauthorizedDatabaseConnection");
      }
      if (detail === "Database data error") {
        return t("errors.unauthorizedDatabaseData");
      }
      return t("errors.unauthorized");
    }

    if (errorState.value.code === "FORBIDDEN") {
      return t("errors.forbidden");
    }

    if (errorState.value.code === "CREDENTIALS_REQUIRED") {
      return t("errors.credentialsRequired");
    }

    return t("errors.unknown");
  });

  function setLocale(nextLocale: AppLocale): void {
    locale.value = nextLocale;
    window.localStorage.setItem(STORAGE_WEBSITE_LOCALE_KEY, nextLocale);
  }

  function formatRefreshedAt(value: Date | null): string {
    if (!value) {
      return t("status.neverRefreshed");
    }

    return value.toLocaleString(currentLocale.value, { hour12: false });
  }

  async function importMojang(
    localeToLoad: AppLocale,
  ): Promise<Record<string, string>> {
    if (localeToLoad === "zh-CN") {
      const mod = await import("@/i18n/mojang/zh_cn.json");
      return mod.default as Record<string, string>;
    }

    const mod = await import("@/i18n/mojang/en_us.json");
    return mod.default as Record<string, string>;
  }

  async function loadMojangByLocale(localeToLoad: AppLocale): Promise<void> {
    const currentToken = ++mojangLoadToken;
    mojangLoaded.value = false;

    const cached = mojangCache.get(localeToLoad);
    if (cached) {
      mojangMap.value = cached;
      mojangLoaded.value = true;
      return;
    }

    try {
      const loaded = await importMojang(localeToLoad);
      mojangCache.set(localeToLoad, loaded);
      if (currentToken !== mojangLoadToken) {
        return;
      }
      mojangMap.value = loaded;
      mojangLoaded.value = true;
    } catch {
      if (currentToken !== mojangLoadToken) {
        return;
      }
      mojangMap.value = {};
      mojangLoaded.value = true;
    }
  }

  function flattenPositions(item: StorageItem): FlattenedPosition[] {
    const rows: FlattenedPosition[] = [];

    Object.entries(item.positionsByDimension).forEach(([dimension, list]) => {
      list.forEach((point) => {
        rows.push({
          dimension,
          pos: point.pos,
          count: point.count,
        });
      });
    });

    return rows;
  }

  function getDimensionLabel(dimension: string): string {
    const key = `dimension.${dimension}`;
    if (te(key)) {
      return t(key);
    }
    return `${t("dimension.unknown")} (${dimension})`;
  }

  function getItemDisplayName(itemId: string): string {
    if (!mojangLoaded.value) {
      return itemId;
    }

    const normalized = itemId.replace(":", ".");
    const translationCandidates = [`item.${normalized}`, `block.${normalized}`];
    for (const key of translationCandidates) {
      const translatedName = mojangMap.value[key];
      if (translatedName) {
        return translatedName;
      }
    }
    return itemId;
  }

  function persistAuth(token: string, username: string): void {
    authToken.value = token;
    authUsername.value = username;
    window.localStorage.setItem(STORAGE_WEBSITE_AUTH_TOKEN_KEY, token);
    window.localStorage.setItem(STORAGE_WEBSITE_AUTH_USERNAME_KEY, username);
  }

  function clearAuthSession(): void {
    authToken.value = null;
    authUsername.value = null;
    window.localStorage.removeItem(STORAGE_WEBSITE_AUTH_TOKEN_KEY);
    window.localStorage.removeItem(STORAGE_WEBSITE_AUTH_USERNAME_KEY);
  }

  function clearStorageData(): void {
    storages.value = [];
    refreshedAt.value = null;
  }

  function clearAuthenticatedState(): void {
    clearAuthSession();
    clearStorageData();
  }

  function clearGetItemCopyMessage(): void {
    getItemCopyMessage.value = "";
    if (copyMessageTimer !== null) {
      window.clearTimeout(copyMessageTimer);
      copyMessageTimer = null;
    }
  }

  function openGetItemModal(): void {
    getItemModalVisible.value = true;
    getItemResult.value = null;
    getItemErrorMessage.value = "";
    getItemManualCopyBotName.value = "";
    getItemManualCopyText.value = "";
    clearGetItemCopyMessage();
  }

  function closeGetItemModal(): void {
    getItemModalVisible.value = false;
    getItemLoading.value = false;
    getItemResult.value = null;
    getItemErrorMessage.value = "";
    getItemManualCopyBotName.value = "";
    getItemManualCopyText.value = "";
    clearGetItemCopyMessage();
  }

  function getGetItemErrorMessage(error: StorageApiError): string {
    if (error.code === "FORBIDDEN") {
      if (error.detail === "Website getItem is disabled") {
        return t("errors.websiteGetItemDisabled");
      }
      return t("errors.forbidden");
    }

    if (error.code === "UNAUTHORIZED") {
      if (error.detail === "Token expired") {
        return t("errors.tokenExpired");
      }
      if (error.detail === "Invalid token") {
        return t("errors.invalidToken");
      }
      return t("errors.unauthorized");
    }

    if (error.code === "NETWORK_ERROR") {
      return t("errors.network");
    }

    if (error.code === "HTTP_ERROR") {
      if (error.detail) {
        return error.detail;
      }
      return t("errors.http", { status: error.status ?? "-" });
    }

    if (error.code === "INVALID_PAYLOAD") {
      return t("errors.invalidPayload");
    }

    if (error.code === "TOKEN_EXPIRED") {
      return t("errors.tokenExpired");
    }

    return t("errors.unknown");
  }

  async function copyGetItemCommand(
    botName: string,
    command: string,
  ): Promise<void> {
    getItemManualCopyBotName.value = botName;
    getItemManualCopyText.value = command;

    try {
      if (!navigator.clipboard?.writeText) {
        throw new Error("Clipboard API unavailable");
      }
      await navigator.clipboard.writeText(command);
      getItemCopyMessage.value = t("status.copied");
    } catch {
      getItemCopyMessage.value = t("status.copyFailed");
    }

    if (copyMessageTimer !== null) {
      window.clearTimeout(copyMessageTimer);
    }
    copyMessageTimer = window.setTimeout(() => {
      getItemCopyMessage.value = "";
      copyMessageTimer = null;
    }, 1500);
  }

  async function handleGetItem(itemId: string): Promise<void> {
    const raw = window.prompt(t("getItem.inputPrompt", { itemId }), "1");
    if (raw === null) {
      return;
    }

    const count = Number.parseInt(raw.trim(), 10);
    if (!Number.isInteger(count) || count < 1) {
      window.alert(t("getItem.invalidCount"));
      return;
    }

    openGetItemModal();
    getItemLoading.value = true;
    try {
      const result = await requestGetItem(itemId, count, authToken.value);
      getItemResult.value = result;
      refreshData().catch(() => {
        // ignore refresh failure in getItem flow
      });
    } catch (error) {
      if (error instanceof StorageApiError) {
        if (error.code === "TOKEN_EXPIRED") {
          handleTokenExpired();
        } else if (error.code === "UNAUTHORIZED" && authToken.value) {
          clearAuthSession();
          requiresLogin.value = true;
        }
        getItemErrorMessage.value = getGetItemErrorMessage(error);
      } else {
        getItemErrorMessage.value = t("errors.unknown");
      }
    } finally {
      getItemLoading.value = false;
    }
  }

  function handleTokenExpired(): void {
    clearAuthenticatedState();
    requiresLogin.value = true;
    errorState.value = { code: "TOKEN_EXPIRED" };
  }

  async function handleManualLogout(): Promise<void> {
    clearAuthenticatedState();
    errorState.value = null;
    requiresLogin.value = false;
    await refreshData();
  }

  async function refreshData(): Promise<void> {
    loading.value = true;
    errorState.value = null;

    try {
      storages.value = await fetchStorageData(authToken.value);
      refreshedAt.value = new Date();
      requiresLogin.value = false;
    } catch (error) {
      if (error instanceof StorageApiError) {
        if (error.code === "TOKEN_EXPIRED") {
          handleTokenExpired();
          return;
        }
        if (error.code === "UNAUTHORIZED") {
          if (authToken.value) {
            clearAuthSession();
          }
          clearStorageData();
          requiresLogin.value = true;
        }
        errorState.value = {
          code: error.code,
          status: error.status,
          detail: error.detail,
        };
      } else {
        errorState.value = { code: "UNKNOWN" };
      }
    } finally {
      loading.value = false;
    }
  }

  async function handleLogin(): Promise<void> {
    if (!credentials.value.username.trim() || !credentials.value.password) {
      errorState.value = { code: "CREDENTIALS_REQUIRED" };
      return;
    }

    loading.value = true;
    errorState.value = null;

    try {
      const result = await login(credentials.value);
      persistAuth(result.token, result.username);
      requiresLogin.value = false;

      try {
        storages.value = await fetchStorageData(result.token);
        refreshedAt.value = new Date();
        requiresLogin.value = false;
      } catch (error) {
        if (error instanceof StorageApiError) {
          if (error.code === "TOKEN_EXPIRED") {
            handleTokenExpired();
            return;
          }
          if (error.code === "UNAUTHORIZED") {
            clearAuthenticatedState();
            requiresLogin.value = true;
          }
        }
        throw error;
      }
    } catch (error) {
      if (error instanceof StorageApiError) {
        errorState.value = {
          code: error.code,
          status: error.status,
          detail: error.detail,
        };
      } else {
        errorState.value = { code: "UNKNOWN" };
      }
    } finally {
      loading.value = false;
    }
  }

  onMounted(async () => {
    await refreshData();
  });

  watch(
    () => currentLocale.value,
    async (next) => {
      await loadMojangByLocale(next);
    },
    { immediate: true },
  );

  return {
    storages,
    loading,
    refreshedAt,
    credentials,
    authUsername,
    currentLocale,
    isAuthenticated,
    isAnonymousAccess,
    showData,
    requiresLogin,
    storageCount,
    totalItemKinds,
    totalErrors,
    errorMessage,
    setLocale,
    formatRefreshedAt,
    flattenPositions,
    getDimensionLabel,
    getItemDisplayName,
    refreshData,
    handleLogin,
    handleManualLogout,
    getItemModalVisible,
    getItemLoading,
    getItemResult,
    getItemErrorMessage,
    getItemCopyMessage,
    getItemManualCopyBotName,
    getItemManualCopyText,
    handleGetItem,
    closeGetItemModal,
    copyGetItemCommand,
  };
}
