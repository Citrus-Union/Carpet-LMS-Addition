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
package cn.nm.lms.carpetlmsaddition.rule.recipe.crafting.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public abstract class ShapedRecipe extends DualVersionCustomRecipe {
    private static final Function<CraftingInput, NonNullList<ItemStack>> DEFAULT_REMAINDER =
        input -> NonNullList.withSize(input.size(), ItemStack.EMPTY);
    private final BooleanSupplier enabled;
    private final int width;
    private final int height;
    private final List<@Nullable Item> key;
    private final Item resultItem;
    private final int resultCount;
    private final Function<CraftingInput, NonNullList<ItemStack>> remainder;

    protected ShapedRecipe(CraftingBookCategory category, BooleanSupplier enabled, int width, int height,
        List<@Nullable Item> key, Item resultItem) {
        this(category, enabled, width, height, key, resultItem, 1, DEFAULT_REMAINDER);
    }

    protected ShapedRecipe(CraftingBookCategory category, BooleanSupplier enabled, int width, int height,
        List<@Nullable Item> key, Item resultItem, int resultCount,
        Function<CraftingInput, NonNullList<ItemStack>> remainder) {
        super(category);
        this.enabled = enabled;
        this.width = width;
        this.height = height;
        this.key = key;
        this.resultItem = resultItem;
        this.resultCount = resultCount;
        this.remainder = remainder;
    }

    @Override
    public boolean matches(CraftingInput input, Level world) {
        if (!enabled.getAsBoolean()) {
            return false;
        }

        if (input.width() != width || input.height() != height) {
            return false;
        }

        for (int y = 0; y < input.height(); y++) {
            for (int x = 0; x < input.width(); x++) {
                Item expect = key.get(y * width + x);
                ItemStack stack = input.getItem(x, y);
                if (expect == null) {
                    if (!stack.isEmpty()) {
                        return false;
                    }
                    continue;
                }
                if (stack.isEmpty()) {
                    return false;
                }
                if (!stack.is(expect)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    //#if MC>=260100
    public ItemStack assemble(CraftingInput recipeInput) {
        ItemStack stack = new ItemStack(resultItem, resultCount);
        stack.setDamageValue(0);
        return stack;
    }

    //#else
    //$$ public ItemStack assemble(
    //$$ CraftingInput recipeInput,
    //$$ net.minecraft.core.HolderLookup.Provider registries
    //$$ )
    //$$ {
    //$$ ItemStack stack = new ItemStack(resultItem, resultCount);
    //$$ stack.setDamageValue(0);
    //$$ return stack;
    //$$ }
    //#endif

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        if (enabled.getAsBoolean()) {
            return remainder.apply(input);
        }
        return NonNullList.withSize(input.size(), ItemStack.EMPTY);
    }

    //#if MC>=12102
    @Override
    public List<net.minecraft.world.item.crafting.display.RecipeDisplay> display() {
        if (!enabled.getAsBoolean()) {
            return Collections.emptyList();
        }
        net.minecraft.world.item.crafting.display.SlotDisplay empty =
            net.minecraft.world.item.crafting.display.SlotDisplay.Empty.INSTANCE;
        List<net.minecraft.world.item.crafting.display.SlotDisplay> ingredients = new ArrayList<>(key.size());
        for (@Nullable
        Item item : key) {
            ingredients.add(
                item == null ? empty : new net.minecraft.world.item.crafting.display.SlotDisplay.ItemSlotDisplay(item));
        }
        return List.of(new net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay(width, height,
            ingredients, new net.minecraft.world.item.crafting.display.SlotDisplay.ItemSlotDisplay(resultItem),
            new net.minecraft.world.item.crafting.display.SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }

    @Override
    public net.minecraft.world.item.crafting.PlacementInfo placementInfo() {
        if (!enabled.getAsBoolean()) {
            return net.minecraft.world.item.crafting.PlacementInfo.NOT_PLACEABLE;
        }
        List<Optional<Ingredient>> slots = new ArrayList<>(key.size());
        for (@Nullable
        Item item : key) {
            if (item == null) {
                slots.add(Optional.empty());
            } else {
                slots.add(Optional.of(Ingredient.of(item)));
            }
        }
        return net.minecraft.world.item.crafting.PlacementInfo.createFromOptionals(slots);
    }
    //#else
    //$$ @Override
    //$$ public boolean canCraftInDimensions(int width, int height) {
    //$$     return width >= this.width && height >= this.height;
    //$$ }
    //$$
    //$$ @Override
    //$$ public ItemStack getResultItem(net.minecraft.core.HolderLookup.Provider registries) {
    //$$     return new ItemStack(resultItem, resultCount);
    //$$ }
    //$$
    //$$ @Override
    //$$ public NonNullList<Ingredient> getIngredients() {
    //$$     NonNullList<Ingredient> ingredients = NonNullList.withSize(key.size(), Ingredient.EMPTY);
    //$$     for (int i = 0; i < key.size(); i++) {
    //$$         Item item = key.get(i);
    //$$         ingredients.set(i, item == null ? Ingredient.EMPTY : Ingredient.of(item));
    //$$     }
    //$$     return ingredients;
    //$$ }
    //#endif

    protected abstract RecipeSerializer<? extends CustomRecipe> getSerializer0();

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return getSerializer0();
    }
}
