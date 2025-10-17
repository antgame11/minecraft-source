/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item.property.select;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.select.SelectProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record TrimMaterialProperty() implements SelectProperty<RegistryKey<ArmorTrimMaterial>>
{
    public static final Codec<RegistryKey<ArmorTrimMaterial>> VALUE_CODEC = RegistryKey.createCodec(RegistryKeys.TRIM_MATERIAL);
    public static final SelectProperty.Type<TrimMaterialProperty, RegistryKey<ArmorTrimMaterial>> TYPE = SelectProperty.Type.create(MapCodec.unit(new TrimMaterialProperty()), VALUE_CODEC);

    @Override
    @Nullable
    public RegistryKey<ArmorTrimMaterial> getValue(ItemStack arg, @Nullable ClientWorld arg2, @Nullable LivingEntity arg3, int i, ItemDisplayContext arg4) {
        ArmorTrim lv = arg.get(DataComponentTypes.TRIM);
        if (lv == null) {
            return null;
        }
        return lv.material().getKey().orElse(null);
    }

    @Override
    public SelectProperty.Type<TrimMaterialProperty, RegistryKey<ArmorTrimMaterial>> getType() {
        return TYPE;
    }

    @Override
    public Codec<RegistryKey<ArmorTrimMaterial>> valueCodec() {
        return VALUE_CODEC;
    }

    @Override
    @Nullable
    public /* synthetic */ Object getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity user, int seed, ItemDisplayContext displayContext) {
        return this.getValue(stack, world, user, seed, displayContext);
    }
}

