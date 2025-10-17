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
import net.minecraft.util.Arm;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record MainHandProperty() implements SelectProperty<Arm>
{
    public static final Codec<Arm> VALUE_CODEC = Arm.CODEC;
    public static final SelectProperty.Type<MainHandProperty, Arm> TYPE = SelectProperty.Type.create(MapCodec.unit(new MainHandProperty()), VALUE_CODEC);

    @Override
    @Nullable
    public Arm getValue(ItemStack arg, @Nullable ClientWorld arg2, @Nullable LivingEntity arg3, int i, ItemDisplayContext arg4) {
        return arg3 == null ? null : arg3.getMainArm();
    }

    @Override
    public SelectProperty.Type<MainHandProperty, Arm> getType() {
        return TYPE;
    }

    @Override
    public Codec<Arm> valueCodec() {
        return VALUE_CODEC;
    }

    @Override
    @Nullable
    public /* synthetic */ Object getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity user, int seed, ItemDisplayContext displayContext) {
        return this.getValue(stack, world, user, seed, displayContext);
    }
}

