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
package cn.nm.lms.carpetlmsaddition.rule.recipe.smelting;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import cn.nm.lms.carpetlmsaddition.lib.Utils;
import cn.nm.lms.carpetlmsaddition.rule.Settings;

public final class ShulkerBoxFurnaceService {
    private static final int SLOT_INPUT = 0;
    private static final int SLOT_FUEL = 1;
    private static final int SLOT_RESULT = 2;
    private static final int SHULKER_COOK_TIME = 200;

    private ShulkerBoxFurnaceService() {}

    public static boolean tryTick(ServerLevel level, BlockPos pos, BlockState state,
        AbstractFurnaceBlockEntity entity) {
        if ("false".equals(Settings.shulkerBoxFurnace)) {
            return false;
        }

        ItemStack input = entity.getItem(SLOT_INPUT);
        if (!Utils.isShulkerBox(input)) {
            return false;
        }

        boolean strict = "strict".equals(Settings.shulkerBoxFurnace);
        boolean stateChanged = false;
        boolean wasLit = entity.litTimeRemaining > 0;

        if (entity.litTimeRemaining > 0) {
            entity.litTimeRemaining--;
        }

        boolean isLit = entity.litTimeRemaining > 0;
        ItemStack fuel = entity.items.get(SLOT_FUEL);

        ItemStack result = calculateShulkerBoxResult(level, input, strict);
        boolean canProcess = result != null && canBurn(entity.items, entity.getMaxStackSize(), result);

        if (!isLit && !fuel.isEmpty() && canProcess) {
            //#if MC>=12102
            int burnDuration = entity.getBurnDuration(level.fuelValues(), fuel);
            //#else
            //$$ int burnDuration = AbstractFurnaceBlockEntity.getFuel().getOrDefault(fuel.getItem(), 0);
            //#endif
            entity.litTimeRemaining = burnDuration;
            entity.litTotalTime = burnDuration;
            if (burnDuration > 0) {
                isLit = true;
                consumeFuel(entity.items, fuel);
                stateChanged = true;
            }
        }

        if (canProcess) {
            entity.cookingTotalTime = SHULKER_COOK_TIME;
        }

        if (isLit && canProcess) {
            entity.cookingTimer++;
            if (entity.cookingTimer >= entity.cookingTotalTime) {
                entity.cookingTimer = 0;
                burn(entity.items, input, result);
                stateChanged = true;
            }
        } else if (!canProcess) {
            entity.cookingTimer = 0;
        } else if (entity.cookingTimer > 0) {
            entity.cookingTimer = Mth.clamp(entity.cookingTimer - 2, 0, entity.cookingTotalTime);
        }

        if (wasLit != isLit) {
            stateChanged = true;
            state = state.setValue(AbstractFurnaceBlock.LIT, isLit);
            level.setBlock(pos, state, 3);
        }

        if (stateChanged) {
            entity.setChanged();
        }

        return true;
    }

    private static ItemStack calculateShulkerBoxResult(ServerLevel level, ItemStack shulkerStack, boolean strict) {
        ItemContainerContents container = shulkerStack.get(DataComponents.CONTAINER);

        if (container == null) {
            return null;
        }
        NonNullList<ItemStack> source = NonNullList.withSize(27, ItemStack.EMPTY);
        container.copyInto(source);

        NonNullList<ItemStack> result = NonNullList.withSize(27, ItemStack.EMPTY);

        boolean hasItem = !strict;

        for (int i = 0; i < 27; i++) {
            ItemStack inner = source.get(i);
            if (inner.isEmpty()) {
                continue;
            }
            if (inner.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) {
                ItemStack innerResult = calculateShulkerBoxResult(level, inner, strict);
                if (innerResult == null) {
                    return null;
                }
                result.set(i, innerResult);
                hasItem = true;
                continue;
            }

            SingleRecipeInput input = new SingleRecipeInput(inner);
            Optional<RecipeHolder<SmeltingRecipe>> recipeOpt =
                level.recipeAccess().getRecipeFor(RecipeType.SMELTING, input, level);

            if (recipeOpt.isEmpty()) {
                if (strict) {
                    return null;
                }
                result.set(i, inner.copy());
                continue;
            }

            ItemStack oneResult;
            //#if MC>=260100
            oneResult = recipeOpt.get().value().assemble(input);
            //#else
            //$$ oneResult = recipeOpt.get().value().assemble(input, level.registryAccess());
            //#endif

            int total = oneResult.getCount() * inner.getCount();

            result.set(i, oneResult.copyWithCount(total));

            hasItem = true;
        }

        ItemStack resultStack = shulkerStack.copyWithCount(1);
        resultStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(result));

        if (hasItem) {
            return resultStack;
        }
        return null;
    }

    private static void consumeFuel(NonNullList<ItemStack> items, ItemStack fuel) {
        Item fuelItem = fuel.getItem();
        fuel.shrink(1);
        if (fuel.isEmpty()) {
            //#if MC>=260100
            net.minecraft.world.item.ItemStackTemplate craftingRemainder = fuelItem.getCraftingRemainder();
            items.set(SLOT_FUEL, craftingRemainder != null ? craftingRemainder.create() : ItemStack.EMPTY);
            //#elseif MC>=12102
            //$$ items.set(SLOT_FUEL, fuelItem.getCraftingRemainder());
            //#else
            //$$ Item remainder = fuelItem.getCraftingRemainingItem();
            //$$ items.set(SLOT_FUEL, remainder != null ? new ItemStack(remainder) : ItemStack.EMPTY);
            //#endif
        }
    }

    private static boolean canBurn(NonNullList<ItemStack> items, int maxStackSize, ItemStack recipeResult) {
        ItemStack output = items.get(SLOT_RESULT);
        if (output.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(output, recipeResult)) {
            return false;
        }
        int mergedCount = output.getCount() + recipeResult.getCount();
        int mergedLimit = Math.min(maxStackSize, recipeResult.getMaxStackSize());
        return mergedCount <= mergedLimit;
    }

    private static void burn(NonNullList<ItemStack> items, ItemStack input, ItemStack recipeResult) {
        ItemStack output = items.get(SLOT_RESULT);
        if (output.isEmpty()) {
            items.set(SLOT_RESULT, recipeResult.copy());
        } else {
            output.grow(recipeResult.getCount());
        }
        input.shrink(1);
    }
}
