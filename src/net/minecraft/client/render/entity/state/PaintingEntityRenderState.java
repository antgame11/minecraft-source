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
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class PaintingEntityRenderState
extends EntityRenderState {
    public Direction facing = Direction.NORTH;
    @Nullable
    public PaintingVariant variant;
    public int[] lightmapCoordinates = new int[0];
}

