<script setup lang="ts">
import { useI18n } from "vue-i18n";

import type { AppLocale } from "@/i18n";

interface Props {
  currentLocale: AppLocale;
  isAuthenticated: boolean;
  isAnonymousAccess: boolean;
  requiresLogin: boolean;
  loading: boolean;
  authUsername: string | null;
  username: string;
  password: string;
  refreshedAtLabel: string;
  loginErrorMessage: string;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  "update:username": [value: string];
  "update:password": [value: string];
  "locale-change": [value: AppLocale];
  login: [];
  refresh: [];
  logout: [];
}>();

const { t } = useI18n();

function onUsernameInput(event: Event): void {
  const value = (event.target as HTMLInputElement).value;
  emit("update:username", value);
}

function onPasswordInput(event: Event): void {
  const value = (event.target as HTMLInputElement).value;
  emit("update:password", value);
}
</script>

<template>
  <section
    class="rounded-2xl border border-slate-800 bg-slate-900/80 p-5 md:p-6"
  >
    <div
      class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between"
    >
      <div>
        <h1 class="text-2xl font-semibold tracking-tight md:text-3xl">
          {{ t("app.title") }}
        </h1>
        <p class="mt-1 text-sm text-slate-300">{{ t("app.subtitle") }}</p>
      </div>
      <div class="flex flex-col items-start gap-2 md:items-end">
        <div class="inline-flex rounded-lg border border-slate-700 p-1">
          <button
            class="rounded-md px-2 py-1 text-xs"
            :class="
              props.currentLocale === 'zh-CN'
                ? 'bg-cyan-500 text-slate-950'
                : 'text-slate-300'
            "
            @click="emit('locale-change', 'zh-CN')"
          >
            {{ t("locale.zh") }}
          </button>
          <button
            class="rounded-md px-2 py-1 text-xs"
            :class="
              props.currentLocale === 'en-US'
                ? 'bg-cyan-500 text-slate-950'
                : 'text-slate-300'
            "
            @click="emit('locale-change', 'en-US')"
          >
            {{ t("locale.en") }}
          </button>
        </div>

        <form
          v-if="props.requiresLogin"
          class="flex w-full flex-col gap-2 md:w-auto md:flex-row"
          @submit.prevent="emit('login')"
        >
          <input
            :value="props.username"
            :placeholder="t('auth.username')"
            autocomplete="username"
            class="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-slate-100 outline-none ring-cyan-500 placeholder:text-slate-500 focus:ring-2 md:w-40"
            type="text"
            @input="onUsernameInput"
          />
          <input
            :value="props.password"
            :placeholder="t('auth.password')"
            autocomplete="current-password"
            class="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-slate-100 outline-none ring-cyan-500 placeholder:text-slate-500 focus:ring-2 md:w-40"
            type="password"
            @input="onPasswordInput"
          />
          <button
            class="inline-flex items-center justify-center rounded-lg bg-cyan-500 px-4 py-2 text-sm font-medium text-slate-950 transition hover:bg-cyan-400 disabled:cursor-not-allowed disabled:bg-slate-700 disabled:text-slate-300"
            :disabled="props.loading"
            type="submit"
          >
            {{ props.loading ? t("actions.refreshing") : t("actions.login") }}
          </button>
        </form>

        <div v-else class="flex w-full flex-col items-start gap-2 md:items-end">
          <p
            v-if="props.isAuthenticated && props.authUsername"
            class="text-xs text-slate-300"
          >
            {{ t("labels.loggedInAs") }}: {{ props.authUsername }}
          </p>
          <p v-if="props.isAnonymousAccess" class="text-xs text-slate-300">
            {{ t("labels.anonymousAccess") }}
          </p>
          <div class="flex gap-2">
            <button
              class="inline-flex items-center justify-center rounded-lg bg-cyan-500 px-4 py-2 text-sm font-medium text-slate-950 transition hover:bg-cyan-400 disabled:cursor-not-allowed disabled:bg-slate-700 disabled:text-slate-300"
              :disabled="props.loading"
              @click="emit('refresh')"
            >
              {{
                props.loading ? t("actions.refreshing") : t("actions.refresh")
              }}
            </button>
            <button
              v-if="props.isAuthenticated"
              class="inline-flex items-center justify-center rounded-lg border border-slate-600 px-4 py-2 text-sm font-medium text-slate-200 transition hover:border-rose-500 hover:text-rose-300"
              :disabled="props.loading"
              @click="emit('logout')"
            >
              {{ t("actions.logout") }}
            </button>
          </div>
          <p class="text-xs text-slate-400">
            {{ t("labels.lastRefreshed") }}: {{ props.refreshedAtLabel }}
          </p>
        </div>

        <p
          v-if="props.loginErrorMessage"
          class="max-w-sm text-xs text-rose-300 md:text-right"
        >
          {{ props.loginErrorMessage }}
        </p>
      </div>
    </div>
  </section>
</template>
