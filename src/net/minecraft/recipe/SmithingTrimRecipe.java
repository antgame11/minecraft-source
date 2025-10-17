/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.item.equipment.trim.ArmorTrimMaterials;
import net.minecraft.item.equipment.trim.ArmorTrimPattern;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.recipe.display.SmithingRecipeDisplay;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

public class SmithingTrimRecipe
implements SmithingRecipe {
    final Ingredient template;
    final Ingredient base;
    final Ingredient addition;
    final RegistryEntry<ArmorTrimPattern> pattern;
    @Nullable
    private IngredientPlacement ingredientPlacement;

    public SmithingTrimRecipe(Ingredient template, Ingredient base, Ingredient addition, RegistryEntry<ArmorTrimPattern> pattern) {
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.pattern = pattern;
    }

    @Override
    public ItemStack craft(SmithingRecipeInput arg, RegistryWrapper.WrapperLookup arg2) {
        return SmithingTrimRecipe.craft(arg2, arg.base(), arg.addition(), this.pattern);
    }

    public static ItemStack craft(RegistryWrapper.WrapperLookup registries, ItemStack base, ItemStack addition, RegistryEntry<ArmorTrimPattern> pattern) {
        Optional<RegistryEntry<ArmorTrimMaterial>> optional = ArmorTrimMaterials.get(registries, addition);
        if (optional.isPresent()) {
            ArmorTrim lv2;
            ArmorTrim lv = base.get(DataComponentTypes.TRIM);
            if (Objects.equals(lv, lv2 = new ArmorTrim(optional.get(), pattern))) {
                return ItemStack.EMPTY;
            }
            ItemStack lv3 = base.copyWithCount(1);
            lv3.set(DataComponentTypes.TRIM, lv2);
            return lv3;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Optional<Ingredient> template() {
        return Optional.of(this.template);
    }

    @Override
    public Ingredient base() {
        return this.base;
    }

    @Override
    public Optional<Ingredient> addition() {
        return Optional.of(this.addition);
    }

    @Override
    public RecipeSerializer<SmithingTrimRecipe> getSerializer() {
        return RecipeSerializer.SMITHING_TRIM;
    }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        if (this.ingredientPlacement == null) {
            this.ingredientPlacement = IngredientPlacement.forShapeless(List.of(this.template, this.base, this.addition));
        }
        return this.ingredientPlacement;
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        SlotDisplay lv = this.base.toDisplay();
        SlotDisplay lv2 = this.addition.toDisplay();
        SlotDisplay lv3 = this.template.toDisplay();
        return List.of(new SmithingRecipeDisplay(lv3, lv, lv2, new SlotDisplay.SmithingTrimSlotDisplay(lv, lv2, this.pattern), new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)));
    }

    public static class Serializer
    implements RecipeSerializer<SmithingTrimRecipe> {
        private static final MapCodec<SmithingTrimRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Ingredient.CODEC.fieldOf("template")).forGetter(recipe -> recipe.template), ((MapCodec)Ingredient.CODEC.fieldOf("base")).forGetter(recipe -> recipe.base), ((MapCodec)Ingredient.CODEC.fieldOf("addition")).forGetter(recipe -> recipe.addition), ((MapCodec)ArmorTrimPattern.ENTRY_CODEC.fieldOf("pattern")).forGetter(recipe -> recipe.pattern)).apply((Applicative<SmithingTrimRecipe, ?>)instance, SmithingTrimRecipe::new));
        public static final PacketCodec<RegistryByteBuf, SmithingTrimRecipe> PACKET_CODEC = PacketCodec.tuple(Ingredient.PACKET_CODEC, recipe -> recipe.template, Ingredient.PACKET_CODEC, recipe -> recipe.base, Ingredient.PACKET_CODEC, recipe -> recipe.addition, ArmorTrimPattern.ENTRY_PACKET_CODEC, recipe -> recipe.pattern, SmithingTrimRecipe::new);

        @Override
        public MapCodec<SmithingTrimRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, SmithingTrimRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}

