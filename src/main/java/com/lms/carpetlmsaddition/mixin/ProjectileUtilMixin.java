package com.lms.carpetlmsaddition.mixin;

import com.lms.carpetlmsaddition.CarpetLmsSettings;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProjectileUtil.class)
public abstract class ProjectileUtilMixin {
  @Inject(
      method =
          "getEntityHitResult(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;F)Lnet/minecraft/world/phys/EntityHitResult;",
      at = @At("HEAD"),
      cancellable = true)
  private static void lms$skipPearlEntityHitsWithMargin(
      Level level,
      Entity projectile,
      Vec3 start,
      Vec3 end,
      AABB bounds,
      Predicate<Entity> predicate,
      float margin,
      CallbackInfoReturnable<EntityHitResult> cir) {
    if (CarpetLmsSettings.pearlIgnoreEntityCollision && projectile instanceof ThrownEnderpearl) {
      cir.setReturnValue(null);
    }
  }

  @Inject(
      method =
          "getEntityHitResult(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Lnet/minecraft/world/phys/EntityHitResult;",
      at = @At("HEAD"),
      cancellable = true)
  private static void lms$skipPearlEntityHitsProjectile(
      Level level,
      net.minecraft.world.entity.projectile.Projectile projectile,
      Vec3 start,
      Vec3 end,
      AABB bounds,
      Predicate<Entity> predicate,
      CallbackInfoReturnable<EntityHitResult> cir) {
    if (CarpetLmsSettings.pearlIgnoreEntityCollision && projectile instanceof ThrownEnderpearl) {
      cir.setReturnValue(null);
    }
  }
}
