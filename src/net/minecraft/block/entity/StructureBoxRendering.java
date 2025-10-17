/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.entity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public interface StructureBoxRendering {
    public RenderMode getRenderMode();

    public StructureBox getStructureBox();

    public static enum RenderMode {
        NONE,
        BOX,
        BOX_AND_INVISIBLE_BLOCKS;

    }

    public record StructureBox(BlockPos localPos, Vec3i size) {
        public static StructureBox create(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            int o = Math.min(minX, maxX);
            int p = Math.min(minY, maxY);
            int q = Math.min(minZ, maxZ);
            return new StructureBox(new BlockPos(o, p, q), new Vec3i(Math.max(minX, maxX) - o, Math.max(minY, maxY) - p, Math.max(minZ, maxZ) - q));
        }
    }
}

