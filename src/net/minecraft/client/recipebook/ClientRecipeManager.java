/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.recipebook;

import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipePropertySet;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.recipe.display.CuttingRecipeDisplay;
import net.minecraft.registry.RegistryKey;

@Environment(value=EnvType.CLIENT)
public class ClientRecipeManager
implements RecipeManager {
    private final Map<RegistryKey<RecipePropertySet>, RecipePropertySet> propertySets;
    private final CuttingRecipeDisplay.Grouping<StonecuttingRecipe> recipes;

    public ClientRecipeManager(Map<RegistryKey<RecipePropertySet>, RecipePropertySet> propertySets, CuttingRecipeDisplay.Grouping<StonecuttingRecipe> recipes) {
        this.propertySets = propertySets;
        this.recipes = recipes;
    }

    @Override
    public RecipePropertySet getPropertySet(RegistryKey<RecipePropertySet> key) {
        return this.propertySets.getOrDefault(key, RecipePropertySet.EMPTY);
    }

    @Override
    public CuttingRecipeDisplay.Grouping<StonecuttingRecipe> getStonecutterRecipes() {
        return this.recipes;
    }
}

