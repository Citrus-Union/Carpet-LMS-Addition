package com.lms.carpetlmsaddition;

import static carpet.api.settings.RuleCategory.SURVIVAL;

import carpet.api.settings.Rule;

public class CarpetLmsSettings {
  public static final String LMS = "lms";

  @Rule(categories = {LMS, SURVIVAL})
  public static boolean pearlIgnoreEntityCollision = false;

  @Rule(
      categories = {LMS, SURVIVAL},
      validators = DropAllRuleValidator.class)
  public static boolean fakePlayerDropAll = false;

  @Rule(categories = {LMS, SURVIVAL})
  public static boolean fragileVaults = false;

  @Rule(categories = {LMS, SURVIVAL})
  public static boolean fragileTrialSpawners = false;

  @Rule(
      categories = {LMS, SURVIVAL},
      validators = PositiveIntValidator.class)
  public static int allayHealInterval = 10;
}
