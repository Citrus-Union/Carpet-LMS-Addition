/*
 * Copyright (C) 2025  Carpet-LMS-Addition contributors
 * https://github.com/Citrus-Union/Carpet-LMS-Addition

 * Carpet LMS Addition is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.

 * Carpet LMS Addition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Carpet LMS Addition.  If not, see <https://www.gnu.org/licenses/>.
 */
package cn.nm.lms.carpetlmsaddition.storage.getitem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cn.nm.lms.carpetlmsaddition.lib.IdentifierCompat;
import cn.nm.lms.carpetlmsaddition.lib.ItemRegistryCompat;
import cn.nm.lms.carpetlmsaddition.lib.NameRateLimiter;
import cn.nm.lms.carpetlmsaddition.lib.Utils;
import cn.nm.lms.carpetlmsaddition.rule.Settings;
import cn.nm.lms.carpetlmsaddition.storage.StorageSlotCounter;
import cn.nm.lms.carpetlmsaddition.storage.website.WebsiteApiException;

public final class WebsiteGetItemHandler {
    private WebsiteGetItemHandler() {}

    public static List<GetItemBotResult> process(String body, String playerName) {
        GetItemRequest request = parseRequest(body);
        if (request.count < 1) {
            throw new WebsiteApiException(400, "Count must be at least 1");
        }

        int maxCount = Settings.getItemMaxCount;
        if (maxCount > 0 && request.count > maxCount) {
            throw new WebsiteApiException(400, String.format("Count must be between 1 and %d", maxCount));
        }

        Identifier itemId = IdentifierCompat.parseNamespacedId(request.itemId);
        if (itemId == null) {
            throw new WebsiteApiException(400, "Invalid itemId");
        }

        Item item = ItemRegistryCompat.getItem(itemId);
        if (item == null) {
            throw new WebsiteApiException(400, "Invalid itemId");
        }
        item = StorageSlotCounter.normalize(item);
        Map<String, Map<Item, Integer>> result;
        try {
            result = GetItem.getItem(item, request.count, playerName);
        } catch (NameRateLimiter.RateLimitException e) {
            throw new WebsiteApiException(429, e.getMessage());
        }
        return toResponse(item, result);
    }

    public static SendGetItemResultResponse sendResultLine(String body, String playerName) {
        if (playerName == null || playerName.isBlank()) {
            throw new WebsiteApiException(401, "Invalid token");
        }

        List<SendGetItemResultRequest> requests = parseSendRequests(body);
        if (requests.isEmpty()) {
            throw new WebsiteApiException(400, "At least one send entry is required");
        }

        try {
            Utils.getServer();
        } catch (IllegalStateException e) {
            throw new WebsiteApiException(503, "Minecraft server is not initialized");
        }

        Boolean sent = Utils.runOnServerThread(() -> {
            ServerPlayer player = Utils.getServer().getPlayerList().getPlayerByName(playerName);
            if (player == null) {
                return Boolean.FALSE;
            }
            for (SendGetItemResultRequest request : requests) {
                player.sendSystemMessage(GetItem.buildBotResultLine(request.botName, request.item, request.count));
            }
            return Boolean.TRUE;
        });
        if (!sent.booleanValue()) {
            throw new WebsiteApiException(404, "Target player is not online");
        }
        return new SendGetItemResultResponse(true, "Sent to player");
    }

    private static GetItemRequest parseRequest(String body) {
        JsonElement root;
        try {
            root = JsonParser.parseString(body);
        } catch (Exception e) {
            throw new WebsiteApiException(400, "Invalid request body");
        }
        if (!root.isJsonObject()) {
            throw new WebsiteApiException(400, "Invalid request body");
        }
        JsonObject object = root.getAsJsonObject();
        JsonElement itemIdElement = object.get("i");
        JsonElement countElement = object.get("c");
        if (itemIdElement == null || !itemIdElement.isJsonPrimitive()
            || !itemIdElement.getAsJsonPrimitive().isString()) {
            throw new WebsiteApiException(400, "i is required");
        }
        if (countElement == null || !countElement.isJsonPrimitive() || !countElement.getAsJsonPrimitive().isNumber()) {
            throw new WebsiteApiException(400, "c is required");
        }

        String itemId = itemIdElement.getAsString().trim();
        if (itemId.isEmpty()) {
            throw new WebsiteApiException(400, "i is required");
        }
        int count;
        try {
            count = countElement.getAsInt();
        } catch (Exception e) {
            throw new WebsiteApiException(400, "count must be a valid integer");
        }
        return new GetItemRequest(itemId, count);
    }

    private static List<SendGetItemResultRequest> parseSendRequests(String body) {
        JsonElement root;
        try {
            root = JsonParser.parseString(body);
        } catch (Exception e) {
            throw new WebsiteApiException(400, "Invalid request body");
        }
        if (!root.isJsonArray()) {
            throw new WebsiteApiException(400, "Invalid request body");
        }
        List<SendGetItemResultRequest> requests = new ArrayList<>();
        for (JsonElement element : root.getAsJsonArray()) {
            if (!element.isJsonObject()) {
                throw new WebsiteApiException(400, "Invalid request body");
            }
            JsonObject object = element.getAsJsonObject();
            JsonElement itemIdElement = object.get("i");
            JsonElement countElement = object.get("c");
            JsonElement botNameElement = object.get("n");
            if (itemIdElement == null || !itemIdElement.isJsonPrimitive()
                || !itemIdElement.getAsJsonPrimitive().isString()) {
                throw new WebsiteApiException(400, "i is required");
            }
            if (countElement == null || !countElement.isJsonPrimitive()
                || !countElement.getAsJsonPrimitive().isNumber()) {
                throw new WebsiteApiException(400, "c is required");
            }
            if (botNameElement == null || !botNameElement.isJsonPrimitive()
                || !botNameElement.getAsJsonPrimitive().isString()) {
                throw new WebsiteApiException(400, "n is required");
            }

            String itemId = itemIdElement.getAsString().trim();
            String botName = botNameElement.getAsString().trim();
            if (itemId.isEmpty()) {
                throw new WebsiteApiException(400, "i is required");
            }
            if (botName.isEmpty()) {
                throw new WebsiteApiException(400, "n is required");
            }
            int count;
            try {
                count = countElement.getAsInt();
            } catch (Exception e) {
                throw new WebsiteApiException(400, "count must be a valid integer");
            }
            if (count < 1) {
                throw new WebsiteApiException(400, "Count must be at least 1");
            }
            Identifier itemIdValue = IdentifierCompat.parseNamespacedId(itemId);
            if (itemIdValue == null) {
                throw new WebsiteApiException(400, "Invalid itemId");
            }
            Item item = ItemRegistryCompat.getItem(itemIdValue);
            if (item == null) {
                throw new WebsiteApiException(400, "Invalid itemId");
            }
            item = StorageSlotCounter.normalize(item);
            requests.add(new SendGetItemResultRequest(item, count, botName));
        }
        return requests;
    }

    private static List<GetItemBotResult> toResponse(Item item, Map<String, Map<Item, Integer>> result) {
        List<GetItemBotResult> bots = new ArrayList<>();
        String compactItemId = ItemRegistryCompat.compactItemId(BuiltInRegistries.ITEM.getKey(item).toString());

        for (Map.Entry<String, Map<Item, Integer>> entry : result.entrySet()) {
            String botName = entry.getKey();
            int got = entry.getValue().getOrDefault(item, 0);
            bots.add(new GetItemBotResult(botName, compactItemId, got));
        }
        return bots;
    }

    private static final class GetItemRequest {
        final String itemId;
        final int count;

        GetItemRequest(String itemId, int count) {
            this.itemId = itemId;
            this.count = count;
        }
    }

    private static final class SendGetItemResultRequest {
        final Item item;
        final int count;
        final String botName;

        SendGetItemResultRequest(Item item, int count, String botName) {
            this.item = item;
            this.count = count;
            this.botName = botName;
        }
    }

    public static final class GetItemBotResult {
        String n;
        String i;
        int c;

        GetItemBotResult(String botName, String itemId, int count) {
            this.n = botName;
            this.i = itemId;
            this.c = count;
        }
    }

    public static final class SendGetItemResultResponse {
        boolean success;
        String message;

        SendGetItemResultResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
