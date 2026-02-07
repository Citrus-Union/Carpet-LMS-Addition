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
package cn.nm.lms.carpetlmsaddition.mixin;

import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.nm.lms.carpetlmsaddition.rules.PearlNoTp;

@Mixin(
    ThrownEnderpearl.class
)
public abstract class PearlNoTpMixin
{
    @Inject(
            method = "onHit(Lnet/minecraft/world/phys/HitResult;)V",
            at = @At(
                "HEAD"
            ),
            cancellable = true
    )
    private void disableTeleportWhenNamed$LMS(HitResult _unusedResult, CallbackInfo ci)
    {
        String noTpName = "true".equals(PearlNoTp.pearlNoTp) ? "noTp" : PearlNoTp.pearlNoTp;
        ThrownEnderpearl pearl = (ThrownEnderpearl) (Object) this;
        if (pearl.level().isClientSide() || noTpName.equals("false")) return;
        ItemStack stack = pearl.getItem();
        String name = stack.getHoverName().getString();
        if (!noTpName.equalsIgnoreCase(name)) return;
        ci.cancel();
        pearl.discard();
    }
}
