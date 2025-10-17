/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.chunk;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
class RenderedChunk {
    private final Map<BlockPos, BlockEntity> blockEntities;
    @Nullable
    private final PalettedContainer<BlockState> blockPalette;
    private final boolean debugWorld;
    private final HeightLimitView heightLimitView;

    RenderedChunk(WorldChunk chunk, int sectionIndex) {
        this.heightLimitView = chunk;
        this.debugWorld = chunk.getWorld().isDebugWorld();
        this.blockEntities = ImmutableMap.copyOf(chunk.getBlockEntities());
        if (chunk instanceof EmptyChunk) {
            this.blockPalette = null;
        } else {
            ChunkSection lv;
            ChunkSection[] lvs = chunk.getSectionArray();
            this.blockPalette = sectionIndex < 0 || sectionIndex >= lvs.length ? null : ((lv = lvs[sectionIndex]).isEmpty() ? null : lv.getBlockStateContainer().copy());
        }
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        return this.blockEntities.get(pos);
    }

    public BlockState getBlockState(BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        if (this.debugWorld) {
            BlockState lv = null;
            if (j == 60) {
                lv = Blocks.BARRIER.getDefaultState();
            }
            if (j == 70) {
                lv = DebugChunkGenerator.getBlockState(i, k);
            }
            return lv == null ? Blocks.AIR.getDefaultState() : lv;
        }
        if (this.blockPalette == null) {
            return Blocks.AIR.getDefaultState();
        }
        try {
            return this.blockPalette.get(i & 0xF, j & 0xF, k & 0xF);
        } catch (Throwable throwable) {
            CrashReport lv2 = CrashReport.create(throwable, "Getting block state");
            CrashReportSection lv3 = lv2.addElement("Block being got");
            lv3.add("Location", () -> CrashReportSection.createPositionString(this.heightLimitView, i, j, k));
            throw new CrashException(lv2);
        }
    }
}

