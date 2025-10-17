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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BlockEntityRenderState {
    public BlockPos pos = BlockPos.ORIGIN;
    public BlockState blockState = Blocks.AIR.getDefaultState();
    public BlockEntityType<?> type = BlockEntityType.TEST_BLOCK;
    public int lightmapCoordinates;
    @Nullable
    public ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay;

    public static void updateBlockEntityRenderState(BlockEntity blockEntity, BlockEntityRenderState state, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        state.pos = blockEntity.getPos();
        state.blockState = blockEntity.getCachedState();
        state.type = blockEntity.getType();
        state.lightmapCoordinates = blockEntity.getWorld() != null ? WorldRenderer.getLightmapCoordinates(blockEntity.getWorld(), blockEntity.getPos()) : 0xF000F0;
        state.crumblingOverlay = crumblingOverlay;
    }

    public void populateCrashReport(CrashReportSection crashReportSection) {
        crashReportSection.add("BlockEntityRenderState", this.getClass().getCanonicalName());
        crashReportSection.add("Position", this.pos);
        crashReportSection.add("Block state", this.blockState::toString);
    }
}

