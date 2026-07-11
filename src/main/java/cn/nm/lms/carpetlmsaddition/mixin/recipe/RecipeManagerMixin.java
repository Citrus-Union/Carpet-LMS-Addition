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
package cn.nm.lms.carpetlmsaddition.mixin.recipe;

// spotless:off
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import cn.nm.lms.carpetlmsaddition.rule.recipe.runtime.RecipeRuleHelper;

//#if MC>=12102
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//#else
//$$ import java.util.Map;
//$$ import com.google.gson.JsonElement;
//$$ import net.minecraft.resources.ResourceLocation;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//#endif
// spotless:on

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    //#if MC>=12102
    @Inject(method = "prepare", at = @At("RETURN"), cancellable = true)
    private void injectLmsRecipes$lms(ResourceManager resourceManager, ProfilerFiller profiler,
        CallbackInfoReturnable<RecipeMap> cir) {
        RecipeMap original = cir.getReturnValue();
        //#if MC>=260300
        cir.setReturnValue(RecipeRuleHelper.mergeWithManagedRecipes(original));
        //#else
        //$$ cir.setReturnValue(RecipeMap.create(RecipeRuleHelper.mergeWithManagedRecipes(original.values())));
        //#endif
    }
    //#else
    //$$ @Inject(method = "apply", at = @At("RETURN"))
    //$$ private void injectLmsRecipes$lms(Map<ResourceLocation, JsonElement> object, ResourceManager
    //$$     resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
    //$$     RecipeManager manager = (RecipeManager) (Object) this;
    //$$     manager.replaceRecipes(RecipeRuleHelper.mergeWithManagedRecipes(manager.getRecipes()));
    //$$ }
    //#endif
}
