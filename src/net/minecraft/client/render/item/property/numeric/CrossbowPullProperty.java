/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item.property.numeric;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.client.render.item.property.numeric.UseDurationProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HeldItemContext;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CrossbowPullProperty
implements NumericProperty {
    public static final MapCodec<CrossbowPullProperty> CODEC = MapCodec.unit(new CrossbowPullProperty());

    @Override
    public float getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable HeldItemContext context, int seed) {
        LivingEntity lv;
        LivingEntity livingEntity = lv = context == null ? null : context.getEntity();
        if (lv == null) {
            return 0.0f;
        }
        if (CrossbowItem.isCharged(stack)) {
            return 0.0f;
        }
        int j = CrossbowItem.getPullTime(stack, lv);
        return (float)UseDurationProperty.getTicksUsedSoFar(stack, lv) / (float)j;
    }

    public MapCodec<CrossbowPullProperty> getCodec() {
        return CODEC;
    }
}

