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
import net.minecraft.block.SkullBlock;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.util.math.Direction;

@Environment(value=EnvType.CLIENT)
public class SkullBlockEntityRenderState
extends BlockEntityRenderState {
    public float poweredTicks;
    public Direction facing = Direction.NORTH;
    public float yaw;
    public SkullBlock.SkullType skullType = SkullBlock.Type.ZOMBIE;
    public RenderLayer renderLayer;
}

