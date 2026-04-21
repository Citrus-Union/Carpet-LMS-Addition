<script setup lang="ts">
import { computed } from "vue";

import AppHeader from "@/components/AppHeader.vue";
import StatsOverview from "@/components/StatsOverview.vue";
import StorageList from "@/components/StorageList.vue";
import { useStorageDashboard } from "@/composables/useStorageDashboard";

const {
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
} = useStorageDashboard();

const refreshedAtLabel = computed(() => formatRefreshedAt(refreshedAt.value));
const loginErrorMessage = computed(() =>
  requiresLogin.value ? errorMessage.value : "",
);
</script>

<template>
  <main class="min-h-screen bg-slate-950 text-slate-100">
    <div class="mx-auto w-full max-w-7xl space-y-6 px-4 py-8 md:px-6">
      <AppHeader
        :current-locale="currentLocale"
        :is-authenticated="isAuthenticated"
        :is-anonymous-access="isAnonymousAccess"
        :requires-login="requiresLogin"
        :loading="loading"
        :auth-username="authUsername"
        :username="credentials.username"
        :password="credentials.password"
        :refreshed-at-label="refreshedAtLabel"
        :login-error-message="loginErrorMessage"
        @update:username="(value) => (credentials.username = value)"
        @update:password="(value) => (credentials.password = value)"
        @locale-change="setLocale"
        @login="handleLogin"
        @refresh="refreshData"
        @logout="handleManualLogout"
      />

      <template v-if="showData">
        <StatsOverview
          :storage-count="storageCount"
          :total-item-kinds="totalItemKinds"
          :total-errors="totalErrors"
        />

        <StorageList
          :storages="storages"
          :loading="loading"
          :error-message="errorMessage"
          :get-item-display-name="getItemDisplayName"
          :flatten-positions="flattenPositions"
          :get-dimension-label="getDimensionLabel"
        />
      </template>
    </div>
  </main>
</template>
