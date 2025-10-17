/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockStateRaycastContext;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.Nullable;

public interface BlockView
extends HeightLimitView {
    @Nullable
    public BlockEntity getBlockEntity(BlockPos var1);

    default public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos pos, BlockEntityType<T> type) {
        BlockEntity lv = this.getBlockEntity(pos);
        if (lv == null || lv.getType() != type) {
            return Optional.empty();
        }
        return Optional.of(lv);
    }

    public BlockState getBlockState(BlockPos var1);

    public FluidState getFluidState(BlockPos var1);

    default public int getLuminance(BlockPos pos) {
        return this.getBlockState(pos).getLuminance();
    }

    default public Stream<BlockState> getStatesInBox(Box box) {
        return BlockPos.stream(box).map(this::getBlockState);
    }

    default public BlockHitResult raycast(BlockStateRaycastContext context) {
        return BlockView.raycast(context.getStart(), context.getEnd(), context, (innerContext, pos) -> {
            BlockState lv = this.getBlockState((BlockPos)pos);
            Vec3d lv2 = innerContext.getStart().subtract(innerContext.getEnd());
            return innerContext.getStatePredicate().test(lv) ? new BlockHitResult(innerContext.getEnd(), Direction.getFacing(lv2.x, lv2.y, lv2.z), BlockPos.ofFloored(innerContext.getEnd()), false) : null;
        }, innerContext -> {
            Vec3d lv = innerContext.getStart().subtract(innerContext.getEnd());
            return BlockHitResult.createMissed(innerContext.getEnd(), Direction.getFacing(lv.x, lv.y, lv.z), BlockPos.ofFloored(innerContext.getEnd()));
        });
    }

    default public BlockHitResult raycast(RaycastContext context) {
        return BlockView.raycast(context.getStart(), context.getEnd(), context, (innerContext, pos) -> {
            BlockState lv = this.getBlockState((BlockPos)pos);
            FluidState lv2 = this.getFluidState((BlockPos)pos);
            Vec3d lv3 = innerContext.getStart();
            Vec3d lv4 = innerContext.getEnd();
            VoxelShape lv5 = innerContext.getBlockShape(lv, this, (BlockPos)pos);
            BlockHitResult lv6 = this.raycastBlock(lv3, lv4, (BlockPos)pos, lv5, lv);
            VoxelShape lv7 = innerContext.getFluidShape(lv2, this, (BlockPos)pos);
            BlockHitResult lv8 = lv7.raycast(lv3, lv4, (BlockPos)pos);
            double d = lv6 == null ? Double.MAX_VALUE : innerContext.getStart().squaredDistanceTo(lv6.getPos());
            double e = lv8 == null ? Double.MAX_VALUE : innerContext.getStart().squaredDistanceTo(lv8.getPos());
            return d <= e ? lv6 : lv8;
        }, innerContext -> {
            Vec3d lv = innerContext.getStart().subtract(innerContext.getEnd());
            return BlockHitResult.createMissed(innerContext.getEnd(), Direction.getFacing(lv.x, lv.y, lv.z), BlockPos.ofFloored(innerContext.getEnd()));
        });
    }

    @Nullable
    default public BlockHitResult raycastBlock(Vec3d start, Vec3d end, BlockPos pos, VoxelShape shape, BlockState state) {
        BlockHitResult lv2;
        BlockHitResult lv = shape.raycast(start, end, pos);
        if (lv != null && (lv2 = state.getRaycastShape(this, pos).raycast(start, end, pos)) != null && lv2.getPos().subtract(start).lengthSquared() < lv.getPos().subtract(start).lengthSquared()) {
            return lv.withSide(lv2.getSide());
        }
        return lv;
    }

    default public double getDismountHeight(VoxelShape blockCollisionShape, Supplier<VoxelShape> belowBlockCollisionShapeGetter) {
        if (!blockCollisionShape.isEmpty()) {
            return blockCollisionShape.getMax(Direction.Axis.Y);
        }
        double d = belowBlockCollisionShapeGetter.get().getMax(Direction.Axis.Y);
        if (d >= 1.0) {
            return d - 1.0;
        }
        return Double.NEGATIVE_INFINITY;
    }

    default public double getDismountHeight(BlockPos pos) {
        return this.getDismountHeight(this.getBlockState(pos).getCollisionShape(this, pos), () -> {
            BlockPos lv = pos.down();
            return this.getBlockState(lv).getCollisionShape(this, lv);
        });
    }

    public static <T, C> T raycast(Vec3d start, Vec3d end, C context, BiFunction<C, BlockPos, T> blockHitFactory, Function<C, T> missFactory) {
        int l;
        int k;
        if (start.equals(end)) {
            return missFactory.apply(context);
        }
        double d = MathHelper.lerp(-1.0E-7, end.x, start.x);
        double e = MathHelper.lerp(-1.0E-7, end.y, start.y);
        double f = MathHelper.lerp(-1.0E-7, end.z, start.z);
        double g = MathHelper.lerp(-1.0E-7, start.x, end.x);
        double h = MathHelper.lerp(-1.0E-7, start.y, end.y);
        double i = MathHelper.lerp(-1.0E-7, start.z, end.z);
        int j = MathHelper.floor(g);
        BlockPos.Mutable lv = new BlockPos.Mutable(j, k = MathHelper.floor(h), l = MathHelper.floor(i));
        T object2 = blockHitFactory.apply(context, lv);
        if (object2 != null) {
            return object2;
        }
        double m = d - g;
        double n = e - h;
        double o = f - i;
        int p = MathHelper.sign(m);
        int q = MathHelper.sign(n);
        int r = MathHelper.sign(o);
        double s = p == 0 ? Double.MAX_VALUE : (double)p / m;
        double t = q == 0 ? Double.MAX_VALUE : (double)q / n;
        double u = r == 0 ? Double.MAX_VALUE : (double)r / o;
        double v = s * (p > 0 ? 1.0 - MathHelper.fractionalPart(g) : MathHelper.fractionalPart(g));
        double w = t * (q > 0 ? 1.0 - MathHelper.fractionalPart(h) : MathHelper.fractionalPart(h));
        double x = u * (r > 0 ? 1.0 - MathHelper.fractionalPart(i) : MathHelper.fractionalPart(i));
        while (v <= 1.0 || w <= 1.0 || x <= 1.0) {
            T object3;
            if (v < w) {
                if (v < x) {
                    j += p;
                    v += s;
                } else {
                    l += r;
                    x += u;
                }
            } else if (w < x) {
                k += q;
                w += t;
            } else {
                l += r;
                x += u;
            }
            if ((object3 = blockHitFactory.apply(context, lv.set(j, k, l))) == null) continue;
            return object3;
        }
        return missFactory.apply(context);
    }

    public static boolean collectCollisionsBetween(Vec3d from, Vec3d to, Box box, CollisionVisitor visitor) {
        Vec3d lv = to.subtract(from);
        if (lv.lengthSquared() < (double)MathHelper.square(1.0E-5f)) {
            for (BlockPos lv2 : BlockPos.iterate(box)) {
                if (visitor.visit(lv2, 0)) continue;
                return false;
            }
            return true;
        }
        LongOpenHashSet longSet = new LongOpenHashSet();
        for (BlockPos lv3 : BlockPos.method_73159(box.offset(lv.multiply(-1.0)), lv)) {
            if (!visitor.visit(lv3, 0)) {
                return false;
            }
            longSet.add(lv3.asLong());
        }
        int i = BlockView.collectCollisionsBetween(longSet, lv, box, visitor);
        if (i < 0) {
            return false;
        }
        for (BlockPos lv4 : BlockPos.method_73159(box, lv)) {
            if (!longSet.add(lv4.asLong()) || visitor.visit(lv4, i + 1)) continue;
            return false;
        }
        return true;
    }

    private static int collectCollisionsBetween(LongSet visited, Vec3d delta, Box box, CollisionVisitor visitor) {
        double d = box.getLengthX();
        double e = box.getLengthY();
        double f = box.getLengthZ();
        Vec3i lv = BlockView.method_73110(delta);
        Vec3d lv2 = box.getCenter();
        Vec3d lv3 = new Vec3d(lv2.getX() + d * 0.5 * (double)lv.getX(), lv2.getY() + e * 0.5 * (double)lv.getY(), lv2.getZ() + f * 0.5 * (double)lv.getZ());
        Vec3d lv4 = lv3.subtract(delta);
        int i = MathHelper.floor(lv4.x);
        int j = MathHelper.floor(lv4.y);
        int k = MathHelper.floor(lv4.z);
        int l = MathHelper.sign(delta.x);
        int m = MathHelper.sign(delta.y);
        int n = MathHelper.sign(delta.z);
        double g = l == 0 ? Double.MAX_VALUE : (double)l / delta.x;
        double h = m == 0 ? Double.MAX_VALUE : (double)m / delta.y;
        double o = n == 0 ? Double.MAX_VALUE : (double)n / delta.z;
        double p = g * (l > 0 ? 1.0 - MathHelper.fractionalPart(lv4.x) : MathHelper.fractionalPart(lv4.x));
        double q = h * (m > 0 ? 1.0 - MathHelper.fractionalPart(lv4.y) : MathHelper.fractionalPart(lv4.y));
        double r = o * (n > 0 ? 1.0 - MathHelper.fractionalPart(lv4.z) : MathHelper.fractionalPart(lv4.z));
        int s = 0;
        while (p <= 1.0 || q <= 1.0 || r <= 1.0) {
            if (p < q) {
                if (p < r) {
                    i += l;
                    p += g;
                } else {
                    k += n;
                    r += o;
                }
            } else if (q < r) {
                j += m;
                q += h;
            } else {
                k += n;
                r += o;
            }
            Optional<Vec3d> optional = Box.raycast(i, j, k, i + 1, j + 1, k + 1, lv4, lv3);
            if (optional.isEmpty()) continue;
            Vec3d lv5 = optional.get();
            double t = MathHelper.clamp(lv5.x, (double)i + (double)1.0E-5f, (double)i + 1.0 - (double)1.0E-5f);
            double u = MathHelper.clamp(lv5.y, (double)j + (double)1.0E-5f, (double)j + 1.0 - (double)1.0E-5f);
            double v = MathHelper.clamp(lv5.z, (double)k + (double)1.0E-5f, (double)k + 1.0 - (double)1.0E-5f);
            int w = MathHelper.floor(t - d * (double)lv.getX());
            int x = MathHelper.floor(u - e * (double)lv.getY());
            int y = MathHelper.floor(v - f * (double)lv.getZ());
            int z = ++s;
            for (BlockPos lv6 : BlockPos.method_73158(i, j, k, w, x, y, delta)) {
                if (!visited.add(lv6.asLong()) || visitor.visit(lv6, z)) continue;
                return -1;
            }
        }
        return s;
    }

    private static Vec3i method_73110(Vec3d arg) {
        int k;
        double d = Math.abs(Vec3d.X.dotProduct(arg));
        double e = Math.abs(Vec3d.Y.dotProduct(arg));
        double f = Math.abs(Vec3d.Z.dotProduct(arg));
        int i = arg.x >= 0.0 ? 1 : -1;
        int j = arg.y >= 0.0 ? 1 : -1;
        int n = k = arg.z >= 0.0 ? 1 : -1;
        if (d <= e && d <= f) {
            return new Vec3i(-i, -k, j);
        }
        if (e <= f) {
            return new Vec3i(k, -j, -i);
        }
        return new Vec3i(-j, i, -k);
    }

    @FunctionalInterface
    public static interface CollisionVisitor {
        public boolean visit(BlockPos var1, int var2);
    }
}

