package cn.nm.lms.carpetlmsaddition

import cn.nm.lms.carpetlmsaddition.rules.renewableelytra.RenewableElytraRecipe
import net.minecraft.recipe.SpecialCraftingRecipe.SpecialRecipeSerializer
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object CarpetLMSAdditionRecipes {
    @JvmField
    val RENEWABLE_ELYTRA: SpecialRecipeSerializer<RenewableElytraRecipe> =
        SpecialRecipeSerializer { RenewableElytraRecipe() }

    fun register() {
        val serializers =
            mapOf(
                "renewableelytra" to RENEWABLE_ELYTRA,
            )

        serializers.forEach { (path, serializer) ->
            Registry.register(
                Registries.RECIPE_SERIALIZER,
                Identifier.of("carpet-lms-addition", path),
                serializer,
            )
        }
    }
}
