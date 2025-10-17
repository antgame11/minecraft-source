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
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record ContextEntityTypeProperty() implements SelectProperty<RegistryKey<EntityType<?>>>
{
    public static final Codec<RegistryKey<EntityType<?>>> VALUE_CODEC = RegistryKey.createCodec(RegistryKeys.ENTITY_TYPE);
    public static final SelectProperty.Type<ContextEntityTypeProperty, RegistryKey<EntityType<?>>> TYPE = SelectProperty.Type.create(MapCodec.unit(new ContextEntityTypeProperty()), VALUE_CODEC);

    @Override
    @Nullable
    public RegistryKey<EntityType<?>> getValue(ItemStack arg, @Nullable ClientWorld arg2, @Nullable LivingEntity arg3, int i, ItemDisplayContext arg4) {
        return arg3 == null ? null : arg3.getType().getRegistryEntry().registryKey();
    }

    @Override
    public SelectProperty.Type<ContextEntityTypeProperty, RegistryKey<EntityType<?>>> getType() {
        return TYPE;
    }

    @Override
    public Codec<RegistryKey<EntityType<?>>> valueCodec() {
        return VALUE_CODEC;
    }

    @Override
    @Nullable
    public /* synthetic */ Object getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity user, int seed, ItemDisplayContext displayContext) {
        return this.getValue(stack, world, user, seed, displayContext);
    }
}

