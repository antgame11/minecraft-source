/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.AnimationState;

@Environment(value=EnvType.CLIENT)
public class SnifferEntityRenderState
extends LivingEntityRenderState {
    public boolean searching;
    public final AnimationState diggingAnimationState = new AnimationState();
    public final AnimationState sniffingAnimationState = new AnimationState();
    public final AnimationState risingAnimationState = new AnimationState();
    public final AnimationState feelingHappyAnimationState = new AnimationState();
    public final AnimationState scentingAnimationState = new AnimationState();
}

