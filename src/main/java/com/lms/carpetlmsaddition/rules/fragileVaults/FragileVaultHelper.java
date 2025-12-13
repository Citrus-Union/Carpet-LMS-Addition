package com.lms.carpetlmsaddition.rules.fragileVaults;

import net.minecraft.world.level.block.Block;

public final class FragileVaultHelper {
  // Beacon blast resistance in vanilla: 3.0F.
  public static final float BEACON_BLAST_RESISTANCE = 3.0F;

  private FragileVaultHelper() {}

  public static boolean isVault(Block block) {
    return block instanceof net.minecraft.world.level.block.VaultBlock;
  }
}
