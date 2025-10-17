/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public interface InteractibleSlotContainer {
    public int getRows();

    public int getColumns();

    default public OptionalInt getHitSlot(BlockHitResult hitResult, Direction facing) {
        return InteractibleSlotContainer.getHitPosOnFront(hitResult, facing).map(vec2f -> {
            int i = InteractibleSlotContainer.getSlotAlongAxis(1.0f - vec2f.y, this.getRows());
            int j = InteractibleSlotContainer.getSlotAlongAxis(vec2f.x, this.getColumns());
            return OptionalInt.of(j + i * this.getColumns());
        }).orElseGet(OptionalInt::empty);
    }

    private static Optional<Vec2f> getHitPosOnFront(BlockHitResult hitResult, Direction facing) {
        Direction lv = hitResult.getSide();
        if (facing != lv) {
            return Optional.empty();
        }
        BlockPos lv2 = hitResult.getBlockPos().offset(lv);
        Vec3d lv3 = hitResult.getPos().subtract(lv2.getX(), lv2.getY(), lv2.getZ());
        double d = lv3.getX();
        double e = lv3.getY();
        double f = lv3.getZ();
        return switch (lv) {
            default -> throw new MatchException(null, null);
            case Direction.NORTH -> Optional.of(new Vec2f((float)(1.0 - d), (float)e));
            case Direction.SOUTH -> Optional.of(new Vec2f((float)d, (float)e));
            case Direction.WEST -> Optional.of(new Vec2f((float)f, (float)e));
            case Direction.EAST -> Optional.of(new Vec2f((float)(1.0 - f), (float)e));
            case Direction.DOWN, Direction.UP -> Optional.empty();
        };
    }

    private static int getSlotAlongAxis(float pos, int max) {
        float g = pos * 16.0f;
        float h = 16.0f / (float)max;
        return MathHelper.clamp(MathHelper.floor(g / h), 0, max - 1);
    }
}

