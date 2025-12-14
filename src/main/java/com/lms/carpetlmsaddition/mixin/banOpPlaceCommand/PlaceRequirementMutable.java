package com.lms.carpetlmsaddition.mixin.banOpPlaceCommand;

import com.mojang.brigadier.tree.CommandNode;
import java.util.function.Predicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CommandNode.class, remap = false)
public interface PlaceRequirementMutable<S> {
  @Accessor("requirement")
  @Mutable
  void lms$setRequirement(Predicate<S> requirement);
}
