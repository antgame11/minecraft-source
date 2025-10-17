/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math;

import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.math.MathHelper;

public enum AxisRotation {
    R0(0),
    R90(1),
    R180(2),
    R270(3);

    public static final Codec<AxisRotation> CODEC;
    public final int index;

    private AxisRotation(int index) {
        this.index = index;
    }

    @Deprecated
    public static AxisRotation fromDegrees(int degrees) {
        return switch (MathHelper.floorMod(degrees, 360)) {
            case 0 -> R0;
            case 90 -> R90;
            case 180 -> R180;
            case 270 -> R270;
            default -> throw new JsonParseException("Invalid rotation " + degrees + " found, only 0/90/180/270 allowed");
        };
    }

    public int rotate(int index) {
        return (index + this.index) % 4;
    }

    static {
        CODEC = Codec.INT.comapFlatMap(degrees -> switch (MathHelper.floorMod(degrees, 360)) {
            case 0 -> DataResult.success(R0);
            case 90 -> DataResult.success(R90);
            case 180 -> DataResult.success(R180);
            case 270 -> DataResult.success(R270);
            default -> DataResult.error(() -> "Invalid rotation " + degrees + " found, only 0/90/180/270 allowed");
        }, rotation -> switch (rotation.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> 0;
            case 1 -> 90;
            case 2 -> 180;
            case 3 -> 270;
        });
    }
}

