package com.lms.carpetlmsaddition.mixin.fakePlayerDropAll;

import carpet.helpers.EntityPlayerActionPack;
import com.lms.carpetlmsaddition.rules.playerCommandDropall.DropAllActionExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityPlayerActionPack.Action.class)
public class EntityPlayerActionPackActionMixin implements DropAllActionExtension {
  @Unique private boolean lms$dropAll;

  @Override
  public void lms$setDropAll(boolean dropAll) {
    lms$dropAll = dropAll;
  }

  @Override
  public boolean lms$isDropAll() {
    return lms$dropAll;
  }
}
