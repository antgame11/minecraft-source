/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.recipe.book.RecipeBookCategories;
import net.minecraft.recipe.book.RecipeBookCategory;

public class BlastingRecipe
extends AbstractCookingRecipe {
    public BlastingRecipe(String string, CookingRecipeCategory arg, Ingredient arg2, ItemStack arg3, float f, int i) {
        super(string, arg, arg2, arg3, f, i);
    }

    @Override
    protected Item getCookerItem() {
        return Items.BLAST_FURNACE;
    }

    @Override
    public RecipeSerializer<BlastingRecipe> getSerializer() {
        return RecipeSerializer.BLASTING;
    }

    @Override
    public RecipeType<BlastingRecipe> getType() {
        return RecipeType.BLASTING;
    }

    @Override
    public RecipeBookCategory getRecipeBookCategory() {
        return switch (this.getCategory()) {
            default -> throw new MatchException(null, null);
            case CookingRecipeCategory.BLOCKS -> RecipeBookCategories.BLAST_FURNACE_BLOCKS;
            case CookingRecipeCategory.FOOD, CookingRecipeCategory.MISC -> RecipeBookCategories.BLAST_FURNACE_MISC;
        };
    }
}

