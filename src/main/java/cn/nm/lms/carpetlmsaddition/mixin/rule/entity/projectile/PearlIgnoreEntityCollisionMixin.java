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
package cn.nm.lms.carpetlmsaddition.mixin.rule.entity.projectile;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.phys.HitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import cn.nm.lms.carpetlmsaddition.rule.entity.projectile.PearlIgnoreEntityCollisionRule;

@Mixin(
    Projectile.class
)
public abstract class PearlIgnoreEntityCollisionMixin
{
    @WrapWithCondition(
            method = "hitTargetOrDeflectSelf",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/Projectile;onHit" + "(Lnet/minecraft/world/phys/HitResult;)V"
            )
    )
    private boolean skipCollisionIfEntityHit$LMS(Projectile self, HitResult hitResult)
    {
        return !shouldSkip(self, hitResult);
    }

    @Unique
    private boolean shouldSkip(Projectile self, HitResult hitResult)
    {
        if (!PearlIgnoreEntityCollisionRule.pearlIgnoreEntityCollision) return false;
        if (!(self instanceof ThrownEnderpearl)) return false;
        return hitResult.getType() == HitResult.Type.ENTITY;
    }
}
