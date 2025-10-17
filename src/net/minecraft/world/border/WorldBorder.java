/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.border;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.border.WorldBorderListener;
import net.minecraft.world.border.WorldBorderStage;

public class WorldBorder
extends PersistentState {
    public static final double STATIC_AREA_SIZE = 5.9999968E7;
    public static final double MAX_CENTER_COORDINATES = 2.9999984E7;
    public static final Codec<WorldBorder> CODEC = Properties.CODEC.xmap(Properties::toWorldBorder, Properties::new);
    public static final PersistentStateType<WorldBorder> TYPE = new PersistentStateType<WorldBorder>("world_border", context -> Properties.DEFAULT.toWorldBorder(), context -> CODEC, DataFixTypes.SAVED_DATA_WORLD_BORDER);
    private final List<WorldBorderListener> listeners = Lists.newArrayList();
    double damagePerBlock = 0.2;
    double safeZone = 5.0;
    int warningTime = 15;
    int warningBlocks = 5;
    double centerX;
    double centerZ;
    int maxRadius = 29999984;
    Area area = new StaticArea(5.9999968E7);

    public boolean contains(BlockPos pos) {
        return this.contains(pos.getX(), pos.getZ());
    }

    public boolean contains(Vec3d pos) {
        return this.contains(pos.x, pos.z);
    }

    public boolean contains(ChunkPos chunkPos) {
        return this.contains(chunkPos.getStartX(), chunkPos.getStartZ()) && this.contains(chunkPos.getEndX(), chunkPos.getEndZ());
    }

    public boolean contains(Box box) {
        return this.contains(box.minX, box.minZ, box.maxX - (double)1.0E-5f, box.maxZ - (double)1.0E-5f);
    }

    private boolean contains(double minX, double minZ, double maxX, double maxZ) {
        return this.contains(minX, minZ) && this.contains(maxX, maxZ);
    }

    public boolean contains(double x, double z) {
        return this.contains(x, z, 0.0);
    }

    public boolean contains(double x, double z, double margin) {
        return x >= this.getBoundWest() - margin && x < this.getBoundEast() + margin && z >= this.getBoundNorth() - margin && z < this.getBoundSouth() + margin;
    }

    public BlockPos clampFloored(BlockPos pos) {
        return this.clampFloored(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockPos clampFloored(Vec3d pos) {
        return this.clampFloored(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockPos clampFloored(double x, double y, double z) {
        return BlockPos.ofFloored(this.clamp(x, y, z));
    }

    public Vec3d clamp(Vec3d pos) {
        return this.clamp(pos.x, pos.y, pos.z);
    }

    public Vec3d clamp(double x, double y, double z) {
        return new Vec3d(MathHelper.clamp(x, this.getBoundWest(), this.getBoundEast() - (double)1.0E-5f), y, MathHelper.clamp(z, this.getBoundNorth(), this.getBoundSouth() - (double)1.0E-5f));
    }

    public double getDistanceInsideBorder(Entity entity) {
        return this.getDistanceInsideBorder(entity.getX(), entity.getZ());
    }

    public VoxelShape asVoxelShape() {
        return this.area.asVoxelShape();
    }

    public double getDistanceInsideBorder(double x, double z) {
        double f = z - this.getBoundNorth();
        double g = this.getBoundSouth() - z;
        double h = x - this.getBoundWest();
        double i = this.getBoundEast() - x;
        double j = Math.min(h, i);
        j = Math.min(j, f);
        return Math.min(j, g);
    }

    public boolean canCollide(Entity entity, Box box) {
        double d = Math.max(MathHelper.absMax(box.getLengthX(), box.getLengthZ()), 1.0);
        return this.getDistanceInsideBorder(entity) < d * 2.0 && this.contains(entity.getX(), entity.getZ(), d);
    }

    public WorldBorderStage getStage() {
        return this.area.getStage();
    }

    public double getBoundWest() {
        return this.area.getBoundWest();
    }

    public double getBoundNorth() {
        return this.area.getBoundNorth();
    }

    public double getBoundEast() {
        return this.area.getBoundEast();
    }

    public double getBoundSouth() {
        return this.area.getBoundSouth();
    }

    public double getCenterX() {
        return this.centerX;
    }

    public double getCenterZ() {
        return this.centerZ;
    }

    public void setCenter(double x, double z) {
        this.centerX = x;
        this.centerZ = z;
        this.area.onCenterChanged();
        this.markDirty();
        for (WorldBorderListener lv : this.getListeners()) {
            lv.onCenterChanged(this, x, z);
        }
    }

    public double getSize() {
        return this.area.getSize();
    }

    public long getSizeLerpTime() {
        return this.area.getSizeLerpTime();
    }

    public double getSizeLerpTarget() {
        return this.area.getSizeLerpTarget();
    }

    public void setSize(double size) {
        this.area = new StaticArea(size);
        this.markDirty();
        for (WorldBorderListener lv : this.getListeners()) {
            lv.onSizeChange(this, size);
        }
    }

    public void interpolateSize(double fromSize, double toSize, long time) {
        this.area = fromSize == toSize ? new StaticArea(toSize) : new MovingArea(fromSize, toSize, time);
        this.markDirty();
        for (WorldBorderListener lv : this.getListeners()) {
            lv.onInterpolateSize(this, fromSize, toSize, time);
        }
    }

    protected List<WorldBorderListener> getListeners() {
        return Lists.newArrayList(this.listeners);
    }

    public void addListener(WorldBorderListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(WorldBorderListener listener) {
        this.listeners.remove(listener);
    }

    public void setMaxRadius(int maxRadius) {
        this.maxRadius = maxRadius;
        this.area.onMaxRadiusChanged();
    }

    public int getMaxRadius() {
        return this.maxRadius;
    }

    public double getSafeZone() {
        return this.safeZone;
    }

    public void setSafeZone(double safeZone) {
        this.safeZone = safeZone;
        this.markDirty();
        for (WorldBorderListener lv : this.getListeners()) {
            lv.onSafeZoneChanged(this, safeZone);
        }
    }

    public double getDamagePerBlock() {
        return this.damagePerBlock;
    }

    public void setDamagePerBlock(double damagePerBlock) {
        this.damagePerBlock = damagePerBlock;
        this.markDirty();
        for (WorldBorderListener lv : this.getListeners()) {
            lv.onDamagePerBlockChanged(this, damagePerBlock);
        }
    }

    public double getShrinkingSpeed() {
        return this.area.getShrinkingSpeed();
    }

    public int getWarningTime() {
        return this.warningTime;
    }

    public void setWarningTime(int warningTime) {
        this.warningTime = warningTime;
        this.markDirty();
        for (WorldBorderListener lv : this.getListeners()) {
            lv.onWarningTimeChanged(this, warningTime);
        }
    }

    public int getWarningBlocks() {
        return this.warningBlocks;
    }

    public void setWarningBlocks(int warningBlocks) {
        this.warningBlocks = warningBlocks;
        this.markDirty();
        for (WorldBorderListener lv : this.getListeners()) {
            lv.onWarningBlocksChanged(this, warningBlocks);
        }
    }

    public void tick() {
        this.area = this.area.getAreaInstance();
    }

    public void load(Properties properties) {
        this.setCenter(properties.centerX(), properties.centerZ());
        this.setDamagePerBlock(properties.damagePerBlock());
        this.setSafeZone(properties.safeZone());
        this.setWarningBlocks(properties.warningBlocks());
        this.setWarningTime(properties.warningTime());
        if (properties.lerpTime() > 0L) {
            this.interpolateSize(properties.size(), properties.lerpTarget(), properties.lerpTime());
        } else {
            this.setSize(properties.size());
        }
    }

    class StaticArea
    implements Area {
        private final double size;
        private double boundWest;
        private double boundNorth;
        private double boundEast;
        private double boundSouth;
        private VoxelShape shape;

        public StaticArea(double size) {
            this.size = size;
            this.recalculateBounds();
        }

        @Override
        public double getBoundWest() {
            return this.boundWest;
        }

        @Override
        public double getBoundEast() {
            return this.boundEast;
        }

        @Override
        public double getBoundNorth() {
            return this.boundNorth;
        }

        @Override
        public double getBoundSouth() {
            return this.boundSouth;
        }

        @Override
        public double getSize() {
            return this.size;
        }

        @Override
        public WorldBorderStage getStage() {
            return WorldBorderStage.STATIONARY;
        }

        @Override
        public double getShrinkingSpeed() {
            return 0.0;
        }

        @Override
        public long getSizeLerpTime() {
            return 0L;
        }

        @Override
        public double getSizeLerpTarget() {
            return this.size;
        }

        private void recalculateBounds() {
            this.boundWest = MathHelper.clamp(WorldBorder.this.getCenterX() - this.size / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
            this.boundNorth = MathHelper.clamp(WorldBorder.this.getCenterZ() - this.size / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
            this.boundEast = MathHelper.clamp(WorldBorder.this.getCenterX() + this.size / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
            this.boundSouth = MathHelper.clamp(WorldBorder.this.getCenterZ() + this.size / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
            this.shape = VoxelShapes.combineAndSimplify(VoxelShapes.UNBOUNDED, VoxelShapes.cuboid(Math.floor(this.getBoundWest()), Double.NEGATIVE_INFINITY, Math.floor(this.getBoundNorth()), Math.ceil(this.getBoundEast()), Double.POSITIVE_INFINITY, Math.ceil(this.getBoundSouth())), BooleanBiFunction.ONLY_FIRST);
        }

        @Override
        public void onMaxRadiusChanged() {
            this.recalculateBounds();
        }

        @Override
        public void onCenterChanged() {
            this.recalculateBounds();
        }

        @Override
        public Area getAreaInstance() {
            return this;
        }

        @Override
        public VoxelShape asVoxelShape() {
            return this.shape;
        }
    }

    static interface Area {
        public double getBoundWest();

        public double getBoundEast();

        public double getBoundNorth();

        public double getBoundSouth();

        public double getSize();

        public double getShrinkingSpeed();

        public long getSizeLerpTime();

        public double getSizeLerpTarget();

        public WorldBorderStage getStage();

        public void onMaxRadiusChanged();

        public void onCenterChanged();

        public Area getAreaInstance();

        public VoxelShape asVoxelShape();
    }

    class MovingArea
    implements Area {
        private final double oldSize;
        private final double newSize;
        private final long timeEnd;
        private final long timeStart;
        private final double timeDuration;

        MovingArea(double oldSize, double newSize, long timeDuration) {
            this.oldSize = oldSize;
            this.newSize = newSize;
            this.timeDuration = timeDuration;
            this.timeStart = Util.getMeasuringTimeMs();
            this.timeEnd = this.timeStart + timeDuration;
        }

        @Override
        public double getBoundWest() {
            return MathHelper.clamp(WorldBorder.this.getCenterX() - this.getSize() / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
        }

        @Override
        public double getBoundNorth() {
            return MathHelper.clamp(WorldBorder.this.getCenterZ() - this.getSize() / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
        }

        @Override
        public double getBoundEast() {
            return MathHelper.clamp(WorldBorder.this.getCenterX() + this.getSize() / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
        }

        @Override
        public double getBoundSouth() {
            return MathHelper.clamp(WorldBorder.this.getCenterZ() + this.getSize() / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
        }

        @Override
        public double getSize() {
            double d = (double)(Util.getMeasuringTimeMs() - this.timeStart) / this.timeDuration;
            return d < 1.0 ? MathHelper.lerp(d, this.oldSize, this.newSize) : this.newSize;
        }

        @Override
        public double getShrinkingSpeed() {
            return Math.abs(this.oldSize - this.newSize) / (double)(this.timeEnd - this.timeStart);
        }

        @Override
        public long getSizeLerpTime() {
            return this.timeEnd - Util.getMeasuringTimeMs();
        }

        @Override
        public double getSizeLerpTarget() {
            return this.newSize;
        }

        @Override
        public WorldBorderStage getStage() {
            return this.newSize < this.oldSize ? WorldBorderStage.SHRINKING : WorldBorderStage.GROWING;
        }

        @Override
        public void onCenterChanged() {
        }

        @Override
        public void onMaxRadiusChanged() {
        }

        @Override
        public Area getAreaInstance() {
            if (this.getSizeLerpTime() <= 0L) {
                WorldBorder.this.markDirty();
                return new StaticArea(this.newSize);
            }
            return this;
        }

        @Override
        public VoxelShape asVoxelShape() {
            return VoxelShapes.combineAndSimplify(VoxelShapes.UNBOUNDED, VoxelShapes.cuboid(Math.floor(this.getBoundWest()), Double.NEGATIVE_INFINITY, Math.floor(this.getBoundNorth()), Math.ceil(this.getBoundEast()), Double.POSITIVE_INFINITY, Math.ceil(this.getBoundSouth())), BooleanBiFunction.ONLY_FIRST);
        }
    }

    public record Properties(double centerX, double centerZ, double damagePerBlock, double safeZone, int warningBlocks, int warningTime, double size, long lerpTime, double lerpTarget) {
        public static final Properties DEFAULT = new Properties(0.0, 0.0, 0.2, 5.0, 5, 15, 5.9999968E7, 0L, 0.0);
        public static final Codec<Properties> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.doubleRange(-2.9999984E7, 2.9999984E7).fieldOf("center_x")).forGetter(Properties::centerX), ((MapCodec)Codec.doubleRange(-2.9999984E7, 2.9999984E7).fieldOf("center_z")).forGetter(Properties::centerZ), ((MapCodec)Codec.DOUBLE.fieldOf("damage_per_block")).forGetter(Properties::damagePerBlock), ((MapCodec)Codec.DOUBLE.fieldOf("safe_zone")).forGetter(Properties::safeZone), ((MapCodec)Codec.INT.fieldOf("warning_blocks")).forGetter(Properties::warningBlocks), ((MapCodec)Codec.INT.fieldOf("warning_time")).forGetter(Properties::warningTime), ((MapCodec)Codec.DOUBLE.fieldOf("size")).forGetter(Properties::size), ((MapCodec)Codec.LONG.fieldOf("lerp_time")).forGetter(Properties::lerpTime), ((MapCodec)Codec.DOUBLE.fieldOf("lerp_target")).forGetter(Properties::lerpTarget)).apply((Applicative<Properties, ?>)instance, Properties::new));

        public Properties(WorldBorder worldBorder) {
            this(worldBorder.centerX, worldBorder.centerZ, worldBorder.damagePerBlock, worldBorder.safeZone, worldBorder.warningBlocks, worldBorder.warningTime, worldBorder.area.getSize(), worldBorder.area.getSizeLerpTime(), worldBorder.area.getSizeLerpTarget());
        }

        public WorldBorder toWorldBorder() {
            WorldBorder lv = new WorldBorder();
            lv.load(this);
            return lv;
        }
    }
}

