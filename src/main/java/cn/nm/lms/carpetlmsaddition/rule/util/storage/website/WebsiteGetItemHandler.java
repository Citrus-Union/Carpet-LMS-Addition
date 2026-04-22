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
package cn.nm.lms.carpetlmsaddition.rule.util.storage.website;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cn.nm.lms.carpetlmsaddition.bot.GetItem;
import cn.nm.lms.carpetlmsaddition.lib.ItemRegistryCompat;
import cn.nm.lms.carpetlmsaddition.rule.Settings;

final class WebsiteGetItemHandler {
    private WebsiteGetItemHandler() {}

    static GetItemResponse process(String body, String playerName) {
        GetItemRequest request = parseRequest(body);
        if (request.count < 1) {
            throw new WebsiteApiException(400, "Count must be at least 1");
        }

        int maxCount = Settings.getItemMaxCount;
        if (maxCount > 0 && request.count > maxCount) {
            throw new WebsiteApiException(400, String.format("Count must be between 1 and %d", maxCount));
        }

        Identifier itemId = parseItemId(request.itemId);
        if (itemId == null) {
            throw new WebsiteApiException(400, "Invalid itemId");
        }

        Item item = ItemRegistryCompat.getItem(itemId);
        if (item == null) {
            throw new WebsiteApiException(400, "Invalid itemId");
        }
        Map<String, Map<Item, Integer>> result;
        try {
            result = GetItem.getItem(item, request.count, playerName);
        } catch (IllegalStateException e) {
            String message = e.getMessage();
            if (message != null && message.contains("rate limited")) {
                throw new WebsiteApiException(429, message);
            }
            throw e;
        }
        return toResponse(itemId.toString(), item, result);
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
        JsonElement itemIdElement = object.get("itemId");
        JsonElement countElement = object.get("count");
        if (itemIdElement == null || !itemIdElement.isJsonPrimitive()
            || !itemIdElement.getAsJsonPrimitive().isString()) {
            throw new WebsiteApiException(400, "itemId is required");
        }
        if (countElement == null || !countElement.isJsonPrimitive() || !countElement.getAsJsonPrimitive().isNumber()) {
            throw new WebsiteApiException(400, "count is required");
        }

        String itemId = itemIdElement.getAsString().trim();
        if (itemId.isEmpty()) {
            throw new WebsiteApiException(400, "itemId is required");
        }
        int count;
        try {
            count = countElement.getAsInt();
        } catch (Exception e) {
            throw new WebsiteApiException(400, "count must be a valid integer");
        }
        return new GetItemRequest(itemId, count);
    }

    private static Identifier parseItemId(String rawItemId) {
        String[] parts = rawItemId.split(":", 2);
        if (parts.length != 2) {
            return null;
        }
        try {
            return Identifier.fromNamespaceAndPath(parts[0], parts[1]);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static GetItemResponse toResponse(String itemId, Item item, Map<String, Map<Item, Integer>> result) {
        List<GetItemBotResult> bots = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        int total = 0;

        for (Map.Entry<String, Map<Item, Integer>> entry : result.entrySet()) {
            String botName = entry.getKey();
            int got = entry.getValue().getOrDefault(item, 0);
            total += got;
            String spawnCommand = "/player " + botName + " spawn";
            String killCommand = "/player " + botName + " kill";
            String inventoryCommand = "/player " + botName + " inventory";
            bots.add(new GetItemBotResult(botName, got, spawnCommand, killCommand, inventoryCommand));
            lines.add(botName + ": " + itemId + " x" + got);
        }
        lines.add("getItem done: " + itemId + " x" + total);
        return new GetItemResponse(itemId, total, bots, lines);
    }

    static final class GetItemResponse {
        String itemId;
        int total;
        List<GetItemBotResult> bots;
        List<String> lines;

        GetItemResponse(String itemId, int total, List<GetItemBotResult> bots, List<String> lines) {
            this.itemId = itemId;
            this.total = total;
            this.bots = bots;
            this.lines = lines;
        }
    }

    private static final class GetItemRequest {
        final String itemId;
        final int count;

        GetItemRequest(String itemId, int count) {
            this.itemId = itemId;
            this.count = count;
        }
    }

    private static final class GetItemBotResult {
        String botName;
        int count;
        String spawnCommand;
        String killCommand;
        String inventoryCommand;

        GetItemBotResult(String botName, int count, String spawnCommand, String killCommand, String inventoryCommand) {
            this.botName = botName;
            this.count = count;
            this.spawnCommand = spawnCommand;
            this.killCommand = killCommand;
            this.inventoryCommand = inventoryCommand;
        }
    }
}
