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
package cn.nm.lms.carpetlmsaddition.lib;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import cn.nm.lms.carpetlmsaddition.translations.Translations;

public final class MessageComponent {
    private static final String SEND_PREFIX = "[Carpet LMS Addition] ";
    private static final String MESSAGE_KEY_PREFIX = "carpetlmsaddition.message.";

    private final MutableComponent component;
    private final boolean prefixed;

    public MessageComponent(Component component) {
        this(component.copy(), true);
    }

    public MessageComponent(Tag tag) {
        this(Component.literal(tag.toString()), false);
    }

    public MessageComponent(String key, Object... args) {
        this(Component.literal(Translations.tr(messageKey(key), args)), true);
    }

    private MessageComponent(Component component, boolean prefixed) {
        this.component = Component.empty().append(component.copy());
        this.prefixed = prefixed;
    }

    public static Component withPrefix(Component component) {
        return Component.literal(SEND_PREFIX).append(component.copy());
    }

    private static String messageKey(String key) {
        return MESSAGE_KEY_PREFIX + key;
    }

    public MessageComponent append(Component component) {
        this.component.append(component.copy());
        return this;
    }

    public MessageComponent append(String text) {
        this.component.append(text);
        return this;
    }

    public Component component() {
        return this.prefixed ? withPrefix(this.component) : this.component.copy();
    }

    /**
     * @deprecated Use {@link #send(CommandSourceStack, boolean)} instead
     */
    @Deprecated
    public void sendSuccess(CommandSourceStack source) {
        source.sendSuccess(this::component, false);
    }

    /**
     * @deprecated Use {@link #send(CommandSourceStack, boolean)} instead
     */
    @Deprecated
    public void sendFailure(CommandSourceStack source) {
        source.sendFailure(component());
    }

    public void send(CommandSourceStack source, boolean success) {
        if (success) {
            sendSuccess(source);
        } else {
            sendFailure(source);
        }
    }

    public void sendSystemMessage(ServerPlayer player) {
        player.sendSystemMessage(component());
    }
}
