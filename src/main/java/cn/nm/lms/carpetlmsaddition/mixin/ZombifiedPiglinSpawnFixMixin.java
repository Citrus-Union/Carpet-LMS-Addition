package cn.nm.lms.carpetlmsaddition.mixin;

import static cn.nm.lms.carpetlmsaddition.rules.ZombifiedPiglinSpawnFix.zombifiedPiglinSpawnFix;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NetherPortalBlock.class)
public abstract class ZombifiedPiglinSpawnFixMixin {
  @Unique
  private static boolean carpetlmsaddition$passesNaturalCollisionChecks(
      EntityType<?> type, ServerWorld world, BlockPos spawnPos, SpawnReason reason) {
    double x = spawnPos.getX() + 0.5D;
    double y = spawnPos.getY();
    double z = spawnPos.getZ() + 0.5D;
    Box spawnBox = type.getSpawnBox(x, y, z);
    if (!world.isSpaceEmpty(spawnBox)) {
      return false;
    }
    Entity entity = type.create(world, reason);
    if (!(entity instanceof MobEntity mob)) {
      return true;
    }
    mob.refreshPositionAndAngles(x, y, z, 0.0F, 0.0F);
    return mob.canSpawn(world);
  }

  @WrapOperation(
      method = "randomTick",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/entity/EntityType;spawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/SpawnReason;)Lnet/minecraft/entity/Entity;"))
  private Entity carpetlmsaddition$onlyIfClear(
      EntityType<?> type,
      ServerWorld world,
      BlockPos spawnPos,
      SpawnReason reason,
      Operation<Entity> origin) {
    if (zombifiedPiglinSpawnFix
        && !carpetlmsaddition$passesNaturalCollisionChecks(type, world, spawnPos, reason)) {
      return null;
    }
    return origin.call(type, world, spawnPos, reason);
  }
}
