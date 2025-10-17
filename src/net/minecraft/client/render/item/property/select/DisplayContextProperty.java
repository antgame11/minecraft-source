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
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record DisplayContextProperty() implements SelectProperty<ItemDisplayContext>
{
    public static final Codec<ItemDisplayContext> VALUE_CODEC = ItemDisplayContext.CODEC;
    public static final SelectProperty.Type<DisplayContextProperty, ItemDisplayContext> TYPE = SelectProperty.Type.create(MapCodec.unit(new DisplayContextProperty()), VALUE_CODEC);

    @Override
    public ItemDisplayContext getValue(ItemStack arg, @Nullable ClientWorld arg2, @Nullable LivingEntity arg3, int i, ItemDisplayContext arg4) {
        return arg4;
    }

    @Override
    public SelectProperty.Type<DisplayContextProperty, ItemDisplayContext> getType() {
        return TYPE;
    }

    @Override
    public Codec<ItemDisplayContext> valueCodec() {
        return VALUE_CODEC;
    }

    @Override
    public /* synthetic */ Object getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity user, int seed, ItemDisplayContext displayContext) {
        return this.getValue(stack, world, user, seed, displayContext);
    }
}

