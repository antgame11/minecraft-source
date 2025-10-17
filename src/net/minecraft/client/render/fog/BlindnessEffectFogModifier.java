/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.fog;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.StatusEffectFogModifier;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class BlindnessEffectFogModifier
extends StatusEffectFogModifier {
    @Override
    public RegistryEntry<StatusEffect> getStatusEffect() {
        return StatusEffects.BLINDNESS;
    }

    @Override
    public void applyStartEndModifier(FogData data, Entity cameraEntity, BlockPos cameraPos, ClientWorld world, float viewDistance, RenderTickCounter tickCounter) {
        LivingEntity lv;
        StatusEffectInstance lv2;
        if (cameraEntity instanceof LivingEntity && (lv2 = (lv = (LivingEntity)cameraEntity).getStatusEffect(this.getStatusEffect())) != null) {
            float g = lv2.isInfinite() ? 5.0f : MathHelper.lerp(Math.min(1.0f, (float)lv2.getDuration() / 20.0f), viewDistance, 5.0f);
            data.environmentalStart = g * 0.25f;
            data.environmentalEnd = g;
            data.skyEnd = g * 0.8f;
            data.cloudEnd = g * 0.8f;
        }
    }

    @Override
    public float applyDarknessModifier(LivingEntity cameraEntity, float darkness, float tickProgress) {
        StatusEffectInstance lv = cameraEntity.getStatusEffect(this.getStatusEffect());
        if (lv != null) {
            darkness = lv.isDurationBelow(19) ? Math.max((float)lv.getDuration() / 20.0f, darkness) : 1.0f;
        }
        return darkness;
    }
}

