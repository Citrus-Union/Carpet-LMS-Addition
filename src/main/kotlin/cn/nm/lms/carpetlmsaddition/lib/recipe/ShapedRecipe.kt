package cn.nm.lms.carpetlmsaddition.lib.recipe

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.IngredientPlacement
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.SpecialCraftingRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.recipe.display.RecipeDisplay
import net.minecraft.recipe.display.ShapedCraftingRecipeDisplay
import net.minecraft.recipe.display.SlotDisplay
import net.minecraft.recipe.input.CraftingRecipeInput
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World
import java.util.Optional

abstract class ShapedRecipe(
    category: CraftingRecipeCategory,
    private val enabled: () -> Boolean,
    private val width: Int,
    private val height: Int,
    private val key: List<Item?>,
    private val resultItem: Item,
    private val resultCount: Int = 1,
    private val remainder: (CraftingRecipeInput) -> DefaultedList<ItemStack> =
        { input -> DefaultedList.ofSize(input.size(), ItemStack.EMPTY) },
) : SpecialCraftingRecipe(category) {
    override fun matches(
        input: CraftingRecipeInput,
        world: World,
    ): Boolean {
        if (!enabled()) return false
        if (input.width != width || input.height != height) return false

        for (y in 0 until input.height) {
            for (x in 0 until input.width) {
                val expect = key[y * width + x]
                val stack = input.getStackInSlot(x, y)

                if (expect == null) {
                    if (!stack.isEmpty) return false
                } else {
                    if (stack.isEmpty || !stack.isOf(expect)) return false
                }
            }
        }
        return true
    }

    override fun craft(
        recipeInput: CraftingRecipeInput,
        registries: RegistryWrapper.WrapperLookup,
    ): ItemStack = ItemStack(resultItem, resultCount).also { it.damage = 0 }

    override fun getRecipeRemainders(input: CraftingRecipeInput): DefaultedList<ItemStack> =
        if (enabled()) {
            remainder(input)
        } else {
            DefaultedList.ofSize(input.size(), ItemStack.EMPTY)
        }

    override fun isIgnoredInRecipeBook(): Boolean = false

    override fun getDisplays(): List<RecipeDisplay> {
        if (!enabled()) return emptyList()

        val empty = SlotDisplay.EmptySlotDisplay.INSTANCE
        val ingredients =
            key.map {
                if (it == null) empty else SlotDisplay.ItemSlotDisplay(it)
            }

        return listOf(
            ShapedCraftingRecipeDisplay(
                width,
                height,
                ingredients,
                SlotDisplay.ItemSlotDisplay(resultItem),
                SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE),
            ),
        )
    }

    override fun getIngredientPlacement(): IngredientPlacement {
        if (!enabled()) return IngredientPlacement.NONE
        val slots =
            key.map {
                if (it == null) {
                    Optional.empty()
                } else {
                    Optional.of(Ingredient.ofItems(it))
                }
            }
        return IngredientPlacement.forMultipleSlots(slots)
    }

    abstract val serializer0: RecipeSerializer<out SpecialCraftingRecipe>

    override fun getSerializer(): RecipeSerializer<out SpecialCraftingRecipe> = serializer0
}
