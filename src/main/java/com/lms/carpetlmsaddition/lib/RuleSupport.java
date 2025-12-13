package com.lms.carpetlmsaddition.lib;

import carpet.utils.CommandHelper;
import net.minecraft.commands.CommandSourceStack;

public final class RuleSupport {
  public static final String LMS = "lms";

  private RuleSupport() {}

  public static boolean canUseCommand(CommandSourceStack source, Object rule) {
    return CommandHelper.canUseCommand(source, rule);
  }
}
