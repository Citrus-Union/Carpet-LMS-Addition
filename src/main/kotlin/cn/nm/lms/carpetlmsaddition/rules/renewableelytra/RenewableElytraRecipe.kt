package cn.nm.lms.carpetlmsaddition.rules.renewableelytra

import cn.nm.lms.carpetlmsaddition.CarpetLMSAdditionRecipes
import cn.nm.lms.carpetlmsaddition.lib.recipe.ShapedRecipe
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.SpecialCraftingRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.util.collection.DefaultedList

class RenewableElytraRecipe :
    ShapedRecipe(
        category = CraftingRecipeCategory.EQUIPMENT,
        enabled = { RenewableElytra.renewableElytra },
        width = 3,
        height = 3,
        key =
            listOf(
                Items.PHANTOM_MEMBRANE,
                Items.END_CRYSTAL,
                Items.PHANTOM_MEMBRANE,
                Items.PHANTOM_MEMBRANE,
                Items.ELYTRA,
                Items.PHANTOM_MEMBRANE,
                Items.PHANTOM_MEMBRANE,
                Items.END_CRYSTAL,
                Items.PHANTOM_MEMBRANE,
            ),
        resultItem = Items.ELYTRA,
        remainder = { input ->
            val rem = DefaultedList.ofSize(input.size(), ItemStack.EMPTY)
            for (i in 0 until input.size()) {
                val s = input.getStackInSlot(i)
                if (s.isOf(Items.ELYTRA)) {
                    rem[i] = s.copy().also { it.count = 1 }
                }
            }
            rem
        },
    ) {
    override val serializer0: RecipeSerializer<out SpecialCraftingRecipe> =
        CarpetLMSAdditionRecipes.RENEWABLE_ELYTRA
}
