<script setup lang="ts">
import { computed } from "vue";

import AppHeader from "@/components/AppHeader.vue";
import GetItemResultModal from "@/components/GetItemResultModal.vue";
import StatsOverview from "@/components/StatsOverview.vue";
import StorageList from "@/components/StorageList.vue";
import { useStorageDashboard } from "@/composables/useStorageDashboard";

const {
  storagesWithAggregate,
  loading,
  refreshedAt,
  credentials,
  authUsername,
  currentLocale,
  isAuthenticated,
  canSendGetItemResult,
  isAnonymousAccess,
  showData,
  requiresLogin,
  storageCount,
  totalItemKinds,
  totalErrors,
  errorMessage,
  setLocale,
  formatRefreshedAt,
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
  getItemSendingBotName,
  getItemManualCopyBotName,
  getItemManualCopyText,
  handleGetItem,
  sendGetItemResultToPlayer,
  sendAllGetItemResultsToPlayer,
  closeGetItemModal,
  copyGetItemCommand,
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
          :storages="storagesWithAggregate"
          :loading="loading"
          :error-message="errorMessage"
          :get-item-display-name="getItemDisplayName"
          :get-dimension-label="getDimensionLabel"
          :on-get-item="handleGetItem"
        />
      </template>
    </div>

    <GetItemResultModal
      :visible="getItemModalVisible"
      :loading="getItemLoading"
      :error-message="getItemErrorMessage"
      :copy-message="getItemCopyMessage"
      :sending-bot-name="getItemSendingBotName"
      :can-send-to-player="canSendGetItemResult"
      :manual-copy-bot-name="getItemManualCopyBotName"
      :manual-copy-text="getItemManualCopyText"
      :result="getItemResult"
      :get-item-display-name="getItemDisplayName"
      @close="closeGetItemModal"
      @copy="copyGetItemCommand"
      @send="sendGetItemResultToPlayer"
      @send-all="sendAllGetItemResultsToPlayer"
    />
  </main>
</template>
