/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record OutlineRenderState(BlockPos pos, boolean isTranslucent, boolean highContrast, VoxelShape shape, @Nullable VoxelShape collisionShape, @Nullable VoxelShape occlusionShape, @Nullable VoxelShape interactionShape) {
    public OutlineRenderState(BlockPos pos, boolean isTranslucent, boolean highContrast, VoxelShape shape) {
        this(pos, isTranslucent, highContrast, shape, null, null, null);
    }

    @Nullable
    public VoxelShape collisionShape() {
        return this.collisionShape;
    }

    @Nullable
    public VoxelShape occlusionShape() {
        return this.occlusionShape;
    }

    @Nullable
    public VoxelShape interactionShape() {
        return this.interactionShape;
    }
}

