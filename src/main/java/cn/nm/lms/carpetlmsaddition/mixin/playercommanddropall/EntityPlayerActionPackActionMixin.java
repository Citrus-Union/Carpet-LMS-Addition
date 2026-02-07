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
package cn.nm.lms.carpetlmsaddition.mixin.playercommanddropall;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import carpet.utils.CommandHelper;

import cn.nm.lms.carpetlmsaddition.rules.playercommanddropall.DropAllActionExtension;
import cn.nm.lms.carpetlmsaddition.rules.playercommanddropall.PlayerCommandDropall;

@Mixin(
    EntityPlayerActionPack.Action.class
)
public class EntityPlayerActionPackActionMixin implements DropAllActionExtension
{
    @Unique
    private boolean lms$dropAll;

    @Override
    @Unique
    public void setDropAll$LMS(boolean v)
    {
        this.lms$dropAll = v;
    }

    @Override
    @Unique
    public boolean isDropAll$LMS()
    {
        return this.lms$dropAll;
    }

    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcarpet/helpers/EntityPlayerActionPack$ActionType;execute" + "(Lnet/minecraft/server/level/ServerPlayer;" + "Lcarpet/helpers/EntityPlayerActionPack$Action;)Z"
            ),
            remap = false
    )
    private boolean wrapExecute$LMS(
            EntityPlayerActionPack.ActionType type,
            net.minecraft.server.level.ServerPlayer player,
            EntityPlayerActionPack.Action action,
            Operation<Boolean> original,
            EntityPlayerActionPack pack
    )
    {
        if (type == EntityPlayerActionPack.ActionType.DROP_STACK && isDropAll$LMS())
        {
            if (!CommandHelper.canUseCommand(
                    player.createCommandSourceStack(),
                    PlayerCommandDropall.playerCommandDropall
            ))
            {
                return false;
            }
            EntityPlayerActionPack actionPack = ((ServerPlayerInterface) player).getActionPack();
            actionPack.drop(-2, true);
            return false;
        }
        return original.call(type, player, action);
    }
}
