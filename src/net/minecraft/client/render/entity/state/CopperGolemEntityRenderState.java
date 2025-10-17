/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.state;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable;
import net.minecraft.client.render.entity.state.ArmedEntityRenderState;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.passive.CopperGolemState;

@Environment(value=EnvType.CLIENT)
public class CopperGolemEntityRenderState
extends ArmedEntityRenderState {
    public Oxidizable.OxidationLevel oxidationLevel = Oxidizable.OxidationLevel.UNAFFECTED;
    public CopperGolemState copperGolemState = CopperGolemState.IDLE;
    public final AnimationState spinHeadAnimationState = new AnimationState();
    public final AnimationState gettingItemAnimationState = new AnimationState();
    public final AnimationState gettingNoItemAnimationState = new AnimationState();
    public final AnimationState droppingItemAnimationState = new AnimationState();
    public final AnimationState droppingNoItemAnimationState = new AnimationState();
    public Optional<BlockState> headBlockItemStack = Optional.empty();
}

