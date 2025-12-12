package com.lms.carpetlmsaddition;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrialSpawnerBlock;

public final class FragileTrialSpawnerHelper {
  // Requested value for trial spawner blast resistance.
  public static final float SPAWNER_BLAST_RESISTANCE = 5.0F;

  private FragileTrialSpawnerHelper() {}

  public static boolean isTrialSpawner(Block block) {
    return block instanceof TrialSpawnerBlock;
  }
}
