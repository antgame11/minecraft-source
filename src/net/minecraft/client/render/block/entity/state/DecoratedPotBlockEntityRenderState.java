/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.block.entity.Sherds;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DecoratedPotBlockEntityRenderState
extends BlockEntityRenderState {
    public float field_62713;
    @Nullable
    public DecoratedPotBlockEntity.WobbleType wobbleType;
    public float wobbleAnimationProgress;
    public Sherds sherds = Sherds.DEFAULT;
    public Direction facing = Direction.NORTH;
}

