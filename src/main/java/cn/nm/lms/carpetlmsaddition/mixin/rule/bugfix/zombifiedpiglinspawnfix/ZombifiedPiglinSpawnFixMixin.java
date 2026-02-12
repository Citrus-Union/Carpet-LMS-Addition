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
package cn.nm.lms.carpetlmsaddition.mixin.rule.bugfix.zombifiedpiglinspawnfix;

import static cn.nm.lms.carpetlmsaddition.rule.bugfix.zombifiedpiglinspawnfix.ZombifiedPiglinSpawnFixRule.zombifiedPiglinSpawnFix;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.phys.AABB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(
    NetherPortalBlock.class
)
public abstract class ZombifiedPiglinSpawnFixMixin
{
    @Unique
    private static boolean passesNaturalCollisionChecks$LMS(
            EntityType<?> type,
            ServerLevel world,
            BlockPos spawnPos,
            EntitySpawnReason reason
    )
    {
        double x = spawnPos.getX() + 0.5D;
        double y = spawnPos.getY();
        double z = spawnPos.getZ() + 0.5D;
        AABB spawnBox = type.getSpawnAABB(x, y, z);
        if (!world.noCollision(spawnBox))
        {
            return false;
        }
        Entity entity = type.create(world, reason);
        if (!(entity instanceof Mob mob))
        {
            return true;
        }
        mob.snapTo(x, y, z, 0.0F, 0.0F);
        return mob.checkSpawnObstruction(world);
    }

    @WrapOperation(
            method = "randomTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/EntityType;spawn" + "(Lnet/minecraft/server/level/ServerLevel;" + "Lnet/minecraft/core/BlockPos;" + "Lnet/minecraft/world/entity/EntitySpawnReason;)" + "Lnet/minecraft/world/entity/Entity;"
            )
    )
    private Entity onlyIfClear$LMS(
            EntityType<?> type,
            ServerLevel world,
            BlockPos spawnPos,
            EntitySpawnReason reason,
            Operation<Entity> origin
    )
    {
        if (zombifiedPiglinSpawnFix && !passesNaturalCollisionChecks$LMS(
                type,
                world,
                spawnPos,
                reason
        ))
        {
            return null;
        }
        return origin.call(type, world, spawnPos, reason);
    }
}
