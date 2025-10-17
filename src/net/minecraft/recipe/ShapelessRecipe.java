/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ShapelessRecipe
implements CraftingRecipe {
    final String group;
    final CraftingRecipeCategory category;
    final ItemStack result;
    final List<Ingredient> ingredients;
    @Nullable
    private IngredientPlacement ingredientPlacement;

    public ShapelessRecipe(String group, CraftingRecipeCategory category, ItemStack result, List<Ingredient> ingredients) {
        this.group = group;
        this.category = category;
        this.result = result;
        this.ingredients = ingredients;
    }

    @Override
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        return RecipeSerializer.SHAPELESS;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public CraftingRecipeCategory getCategory() {
        return this.category;
    }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        if (this.ingredientPlacement == null) {
            this.ingredientPlacement = IngredientPlacement.forShapeless(this.ingredients);
        }
        return this.ingredientPlacement;
    }

    @Override
    public boolean matches(CraftingRecipeInput arg, World arg2) {
        if (arg.getStackCount() != this.ingredients.size()) {
            return false;
        }
        if (arg.size() == 1 && this.ingredients.size() == 1) {
            return this.ingredients.getFirst().test(arg.getStackInSlot(0));
        }
        return arg.getRecipeMatcher().isCraftable(this, null);
    }

    @Override
    public ItemStack craft(CraftingRecipeInput arg, RegistryWrapper.WrapperLookup arg2) {
        return this.result.copy();
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        return List.of(new ShapelessCraftingRecipeDisplay(this.ingredients.stream().map(Ingredient::toDisplay).toList(), new SlotDisplay.StackSlotDisplay(this.result), new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }

    public static class Serializer
    implements RecipeSerializer<ShapelessRecipe> {
        private static final MapCodec<ShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group), ((MapCodec)CraftingRecipeCategory.CODEC.fieldOf("category")).orElse(CraftingRecipeCategory.MISC).forGetter(recipe -> recipe.category), ((MapCodec)ItemStack.VALIDATED_CODEC.fieldOf("result")).forGetter(recipe -> recipe.result), ((MapCodec)Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients")).forGetter(recipe -> recipe.ingredients)).apply((Applicative<ShapelessRecipe, ?>)instance, ShapelessRecipe::new));
        public static final PacketCodec<RegistryByteBuf, ShapelessRecipe> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.STRING, recipe -> recipe.group, CraftingRecipeCategory.PACKET_CODEC, recipe -> recipe.category, ItemStack.PACKET_CODEC, recipe -> recipe.result, Ingredient.PACKET_CODEC.collect(PacketCodecs.toList()), recipe -> recipe.ingredients, ShapelessRecipe::new);

        @Override
        public MapCodec<ShapelessRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, ShapelessRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}

