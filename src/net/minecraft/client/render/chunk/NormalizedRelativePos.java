/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.chunk;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public final class NormalizedRelativePos {
    private int x;
    private int y;
    private int z;

    public static NormalizedRelativePos of(Vec3d cameraPos, long sectionPos) {
        return new NormalizedRelativePos().with(cameraPos, sectionPos);
    }

    public NormalizedRelativePos with(Vec3d cameraPos, long sectionPos) {
        this.x = NormalizedRelativePos.normalize(cameraPos.getX(), ChunkSectionPos.unpackX(sectionPos));
        this.y = NormalizedRelativePos.normalize(cameraPos.getY(), ChunkSectionPos.unpackY(sectionPos));
        this.z = NormalizedRelativePos.normalize(cameraPos.getZ(), ChunkSectionPos.unpackZ(sectionPos));
        return this;
    }

    private static int normalize(double cameraCoord, int sectionCoord) {
        int j = ChunkSectionPos.getSectionCoordFloored(cameraCoord) - sectionCoord;
        return MathHelper.clamp(j, -1, 1);
    }

    public boolean isOnCameraAxis() {
        return this.x == 0 || this.y == 0 || this.z == 0;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof NormalizedRelativePos) {
            NormalizedRelativePos lv = (NormalizedRelativePos)o;
            return this.x == lv.x && this.y == lv.y && this.z == lv.z;
        }
        return false;
    }
}

