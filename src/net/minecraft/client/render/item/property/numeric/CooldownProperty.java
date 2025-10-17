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
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HeldItemContext;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record CooldownProperty() implements NumericProperty
{
    public static final MapCodec<CooldownProperty> CODEC = MapCodec.unit(new CooldownProperty());

    @Override
    public float getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable HeldItemContext context, int seed) {
        float f;
        LivingEntity livingEntity;
        if (context != null && (livingEntity = context.getEntity()) instanceof PlayerEntity) {
            PlayerEntity lv = (PlayerEntity)livingEntity;
            f = lv.getItemCooldownManager().getCooldownProgress(stack, 0.0f);
        } else {
            f = 0.0f;
        }
        return f;
    }

    public MapCodec<CooldownProperty> getCodec() {
        return CODEC;
    }
}

