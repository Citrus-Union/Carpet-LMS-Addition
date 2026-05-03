<script setup lang="ts">
import { useI18n } from "vue-i18n";

import type { GetItemResponse } from "@/api/storage";

interface Props {
  visible: boolean;
  loading: boolean;
  errorMessage: string;
  copyMessage: string;
  sendingBotName: string;
  canSendToPlayer: boolean;
  manualCopyBotName: string;
  manualCopyText: string;
  result: GetItemResponse | null;
  getItemDisplayName: (itemId: string) => string;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  close: [];
  copy: [botName: string, command: string];
  send: [botName: string, itemId: string, count: number];
  sendAll: [];
}>();

const { t } = useI18n();
</script>

<template>
  <div
    v-if="props.visible"
    class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/70 px-4"
  >
    <div
      class="w-full max-w-2xl rounded-2xl border border-slate-700 bg-slate-900 p-5 shadow-2xl"
    >
      <div class="mb-4 flex items-start justify-between gap-3">
        <h2 class="text-lg font-semibold text-slate-100">
          {{ t("getItem.resultTitle") }}
        </h2>
        <button
          class="rounded-md border border-slate-600 px-2 py-1 text-xs text-slate-300 transition hover:border-slate-400 hover:text-slate-100"
          @click="emit('close')"
        >
          {{ t("actions.close") }}
        </button>
      </div>

      <p
        v-if="props.copyMessage"
        class="mb-3 rounded-md border border-cyan-700/70 bg-cyan-950/30 px-3 py-2 text-sm text-cyan-200"
      >
        {{ props.copyMessage }}
      </p>

      <div
        v-if="props.loading"
        class="rounded-md border border-slate-700 bg-slate-950/40 px-3 py-4 text-sm text-slate-300"
      >
        {{ t("getItem.requesting") }}
      </div>

      <div
        v-else-if="props.errorMessage"
        class="rounded-md border border-rose-700/70 bg-rose-950/30 px-3 py-4 text-sm text-rose-200"
      >
        {{ props.errorMessage }}
      </div>

      <div v-else-if="props.result" class="space-y-3">
        <div class="flex items-center justify-between gap-3">
          <p class="text-sm text-slate-200">
            {{ t("getItem.resultTotal", { total: props.result.total }) }}
          </p>
          <button
            v-if="props.canSendToPlayer"
            class="rounded-md border border-slate-600 px-2 py-1 text-xs text-slate-200 transition hover:border-emerald-400 hover:text-emerald-300 disabled:cursor-not-allowed disabled:opacity-60"
            :disabled="props.sendingBotName === '__all__'"
            @click="emit('sendAll')"
          >
            {{
              props.sendingBotName === "__all__"
                ? t("actions.sendingAllToPlayer")
                : t("actions.sendAllToPlayer")
            }}
          </button>
        </div>

        <ul class="max-h-[50vh] space-y-2 overflow-y-auto">
          <li
            v-for="bot in props.result.bots"
            :key="bot.botName"
            class="rounded-lg border border-slate-700 bg-slate-950/50 p-3"
          >
            <p class="mb-2 text-sm text-slate-100">
              {{ bot.botName }}:
              {{ props.getItemDisplayName(props.result.itemId) }} x{{
                bot.count
              }}
            </p>
            <div class="flex flex-wrap gap-2">
              <button
                class="rounded-md border border-slate-600 px-2 py-1 text-xs text-slate-200 transition hover:border-cyan-400 hover:text-cyan-300"
                @click="emit('copy', bot.botName, bot.spawnCommand)"
              >
                {{ t("actions.copySpawn") }}
              </button>
              <button
                class="rounded-md border border-slate-600 px-2 py-1 text-xs text-slate-200 transition hover:border-cyan-400 hover:text-cyan-300"
                @click="emit('copy', bot.botName, bot.killCommand)"
              >
                {{ t("actions.copyKill") }}
              </button>
              <button
                class="rounded-md border border-slate-600 px-2 py-1 text-xs text-slate-200 transition hover:border-cyan-400 hover:text-cyan-300"
                @click="emit('copy', bot.botName, bot.inventoryCommand)"
              >
                {{ t("actions.copyInventory") }}
              </button>
              <button
                v-if="props.canSendToPlayer"
                class="rounded-md border border-slate-600 px-2 py-1 text-xs text-slate-200 transition hover:border-emerald-400 hover:text-emerald-300 disabled:cursor-not-allowed disabled:opacity-60"
                :disabled="props.sendingBotName === bot.botName"
                @click="
                  emit('send', bot.botName, props.result.itemId, bot.count)
                "
              >
                {{
                  props.sendingBotName === bot.botName
                    ? t("actions.sendingToPlayer")
                    : t("actions.sendToPlayer")
                }}
              </button>
            </div>
            <div
              v-if="
                props.manualCopyText && props.manualCopyBotName === bot.botName
              "
              class="mt-2"
            >
              <textarea
                readonly
                :value="props.manualCopyText"
                class="h-20 w-full resize-y rounded-md border border-slate-600 bg-slate-900 px-2 py-2 font-mono text-xs text-slate-100 outline-none"
              />
            </div>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>
