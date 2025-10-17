/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;

public class ShapelessRecipeJsonBuilder
implements CraftingRecipeJsonBuilder {
    private final RegistryEntryLookup<Item> registryLookup;
    private final RecipeCategory category;
    private final ItemStack output;
    private final List<Ingredient> inputs = new ArrayList<Ingredient>();
    private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap();
    @Nullable
    private String group;

    private ShapelessRecipeJsonBuilder(RegistryEntryLookup<Item> registryLookup, RecipeCategory category, ItemStack output) {
        this.registryLookup = registryLookup;
        this.category = category;
        this.output = output;
    }

    public static ShapelessRecipeJsonBuilder create(RegistryEntryLookup<Item> registryLookup, RecipeCategory category, ItemStack output) {
        return new ShapelessRecipeJsonBuilder(registryLookup, category, output);
    }

    public static ShapelessRecipeJsonBuilder create(RegistryEntryLookup<Item> registryLookup, RecipeCategory category, ItemConvertible output) {
        return ShapelessRecipeJsonBuilder.create(registryLookup, category, output, 1);
    }

    public static ShapelessRecipeJsonBuilder create(RegistryEntryLookup<Item> registryLookup, RecipeCategory category, ItemConvertible output, int count) {
        return new ShapelessRecipeJsonBuilder(registryLookup, category, output.asItem().getDefaultStack().copyWithCount(count));
    }

    public ShapelessRecipeJsonBuilder input(TagKey<Item> tag) {
        return this.input(Ingredient.ofTag(this.registryLookup.getOrThrow(tag)));
    }

    public ShapelessRecipeJsonBuilder input(ItemConvertible item) {
        return this.input(item, 1);
    }

    public ShapelessRecipeJsonBuilder input(ItemConvertible item, int amount) {
        for (int j = 0; j < amount; ++j) {
            this.input(Ingredient.ofItem(item));
        }
        return this;
    }

    public ShapelessRecipeJsonBuilder input(Ingredient ingredient) {
        return this.input(ingredient, 1);
    }

    public ShapelessRecipeJsonBuilder input(Ingredient ingredient, int amount) {
        for (int j = 0; j < amount; ++j) {
            this.inputs.add(ingredient);
        }
        return this;
    }

    @Override
    public ShapelessRecipeJsonBuilder criterion(String string, AdvancementCriterion<?> arg) {
        this.criteria.put(string, arg);
        return this;
    }

    @Override
    public ShapelessRecipeJsonBuilder group(@Nullable String string) {
        this.group = string;
        return this;
    }

    @Override
    public Item getOutputItem() {
        return this.output.getItem();
    }

    @Override
    public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> recipeKey) {
        this.validate(recipeKey);
        Advancement.Builder lv = exporter.getAdvancementBuilder().criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeKey)).rewards(AdvancementRewards.Builder.recipe(recipeKey)).criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        this.criteria.forEach(lv::criterion);
        ShapelessRecipe lv2 = new ShapelessRecipe(Objects.requireNonNullElse(this.group, ""), CraftingRecipeJsonBuilder.toCraftingCategory(this.category), this.output, this.inputs);
        exporter.accept(recipeKey, lv2, lv.build(recipeKey.getValue().withPrefixedPath("recipes/" + this.category.getName() + "/")));
    }

    private void validate(RegistryKey<Recipe<?>> recipeKey) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + String.valueOf(recipeKey.getValue()));
        }
    }

    @Override
    public /* synthetic */ CraftingRecipeJsonBuilder group(@Nullable String group) {
        return this.group(group);
    }

    public /* synthetic */ CraftingRecipeJsonBuilder criterion(String name, AdvancementCriterion criterion) {
        return this.criterion(name, criterion);
    }
}

