/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.vehicle;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.block.BlockState;
import net.minecraft.block.LilyPadBlock;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.PositionInterpolator;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.BoatPaddleStateC2SPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractBoatEntity
extends VehicleEntity
implements Leashable {
    private static final TrackedData<Boolean> LEFT_PADDLE_MOVING = DataTracker.registerData(AbstractBoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> RIGHT_PADDLE_MOVING = DataTracker.registerData(AbstractBoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> BUBBLE_WOBBLE_TICKS = DataTracker.registerData(AbstractBoatEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final int field_54427 = 0;
    public static final int field_54445 = 1;
    private static final int field_54451 = 60;
    private static final float NEXT_PADDLE_PHASE = 0.3926991f;
    public static final double EMIT_SOUND_EVENT_PADDLE_ROTATION = 0.7853981852531433;
    public static final int field_54447 = 60;
    private final float[] paddlePhases = new float[2];
    private float ticksUnderwater;
    private float yawVelocity;
    private final PositionInterpolator interpolator = new PositionInterpolator((Entity)this, 3);
    private boolean pressingLeft;
    private boolean pressingRight;
    private boolean pressingForward;
    private boolean pressingBack;
    private double waterLevel;
    private float nearbySlipperiness;
    private Location location;
    private Location lastLocation;
    private double fallVelocity;
    private boolean onBubbleColumnSurface;
    private boolean bubbleColumnIsDrag;
    private float bubbleWobbleStrength;
    private float bubbleWobble;
    private float lastBubbleWobble;
    @Nullable
    private Leashable.LeashData leashData;
    private final Supplier<Item> itemSupplier;

    public AbstractBoatEntity(EntityType<? extends AbstractBoatEntity> type, World world, Supplier<Item> itemSupplier) {
        super(type, world);
        this.itemSupplier = itemSupplier;
        this.intersectionChecked = true;
    }

    public void initPosition(double x, double y, double z) {
        this.setPosition(x, y, z);
        this.lastX = x;
        this.lastY = y;
        this.lastZ = z;
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.EVENTS;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(LEFT_PADDLE_MOVING, false);
        builder.add(RIGHT_PADDLE_MOVING, false);
        builder.add(BUBBLE_WOBBLE_TICKS, 0);
    }

    @Override
    public boolean collidesWith(Entity other) {
        return AbstractBoatEntity.canCollide(this, other);
    }

    public static boolean canCollide(Entity entity, Entity other) {
        return (other.isCollidable(entity) || other.isPushable()) && !entity.isConnectedThroughVehicle(other);
    }

    @Override
    public boolean isCollidable(@Nullable Entity entity) {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public Vec3d positionInPortal(Direction.Axis portalAxis, BlockLocating.Rectangle portalRect) {
        return LivingEntity.positionInPortal(super.positionInPortal(portalAxis, portalRect));
    }

    protected abstract double getPassengerAttachmentY(EntityDimensions var1);

    @Override
    protected Vec3d getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
        float g = this.getPassengerHorizontalOffset();
        if (this.getPassengerList().size() > 1) {
            int i = this.getPassengerList().indexOf(passenger);
            g = i == 0 ? 0.2f : -0.6f;
            if (passenger instanceof AnimalEntity) {
                g += 0.2f;
            }
        }
        return new Vec3d(0.0, this.getPassengerAttachmentY(dimensions), g).rotateY(-this.getYaw() * ((float)Math.PI / 180));
    }

    @Override
    public void onBubbleColumnSurfaceCollision(boolean drag, BlockPos pos) {
        if (this.getEntityWorld() instanceof ServerWorld) {
            this.onBubbleColumnSurface = true;
            this.bubbleColumnIsDrag = drag;
            if (this.getBubbleWobbleTicks() == 0) {
                this.setBubbleWobbleTicks(60);
            }
        }
        if (!this.isSubmergedInWater() && this.random.nextInt(100) == 0) {
            this.getEntityWorld().playSoundClient(this.getX(), this.getY(), this.getZ(), this.getSplashSound(), this.getSoundCategory(), 1.0f, 0.8f + 0.4f * this.random.nextFloat(), false);
            this.getEntityWorld().addParticleClient(ParticleTypes.SPLASH, this.getX() + (double)this.random.nextFloat(), this.getY() + 0.7, this.getZ() + (double)this.random.nextFloat(), 0.0, 0.0, 0.0);
            this.emitGameEvent(GameEvent.SPLASH, this.getControllingPassenger());
        }
    }

    @Override
    public void pushAwayFrom(Entity entity) {
        if (entity instanceof AbstractBoatEntity) {
            if (entity.getBoundingBox().minY < this.getBoundingBox().maxY) {
                super.pushAwayFrom(entity);
            }
        } else if (entity.getBoundingBox().minY <= this.getBoundingBox().minY) {
            super.pushAwayFrom(entity);
        }
    }

    @Override
    public void animateDamage(float yaw) {
        this.setDamageWobbleSide(-this.getDamageWobbleSide());
        this.setDamageWobbleTicks(10);
        this.setDamageWobbleStrength(this.getDamageWobbleStrength() * 11.0f);
    }

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    @Override
    public PositionInterpolator getInterpolator() {
        return this.interpolator;
    }

    @Override
    public Direction getMovementDirection() {
        return this.getHorizontalFacing().rotateYClockwise();
    }

    @Override
    public void tick() {
        this.lastLocation = this.location;
        this.location = this.checkLocation();
        this.ticksUnderwater = this.location == Location.UNDER_WATER || this.location == Location.UNDER_FLOWING_WATER ? (this.ticksUnderwater += 1.0f) : 0.0f;
        if (!this.getEntityWorld().isClient() && this.ticksUnderwater >= 60.0f) {
            this.removeAllPassengers();
        }
        if (this.getDamageWobbleTicks() > 0) {
            this.setDamageWobbleTicks(this.getDamageWobbleTicks() - 1);
        }
        if (this.getDamageWobbleStrength() > 0.0f) {
            this.setDamageWobbleStrength(this.getDamageWobbleStrength() - 1.0f);
        }
        super.tick();
        this.interpolator.tick();
        if (this.isLogicalSideForUpdatingMovement()) {
            if (!(this.getFirstPassenger() instanceof PlayerEntity)) {
                this.setPaddlesMoving(false, false);
            }
            this.updateVelocity();
            if (this.getEntityWorld().isClient()) {
                this.updatePaddles();
                this.getEntityWorld().sendPacket(new BoatPaddleStateC2SPacket(this.isPaddleMoving(0), this.isPaddleMoving(1)));
            }
            this.move(MovementType.SELF, this.getVelocity());
        } else {
            this.setVelocity(Vec3d.ZERO);
        }
        this.tickBlockCollision();
        this.tickBlockCollision();
        this.handleBubbleColumn();
        for (int i = 0; i <= 1; ++i) {
            if (this.isPaddleMoving(i)) {
                SoundEvent lv;
                if (!this.isSilent() && (double)(this.paddlePhases[i] % ((float)Math.PI * 2)) <= 0.7853981852531433 && (double)((this.paddlePhases[i] + 0.3926991f) % ((float)Math.PI * 2)) >= 0.7853981852531433 && (lv = this.getPaddleSound()) != null) {
                    Vec3d lv2 = this.getRotationVec(1.0f);
                    double d = i == 1 ? -lv2.z : lv2.z;
                    double e = i == 1 ? lv2.x : -lv2.x;
                    this.getEntityWorld().playSound(null, this.getX() + d, this.getY(), this.getZ() + e, lv, this.getSoundCategory(), 1.0f, 0.8f + 0.4f * this.random.nextFloat());
                }
                int n = i;
                this.paddlePhases[n] = this.paddlePhases[n] + 0.3926991f;
                continue;
            }
            this.paddlePhases[i] = 0.0f;
        }
        List<Entity> list = this.getEntityWorld().getOtherEntities(this, this.getBoundingBox().expand(0.2f, -0.01f, 0.2f), EntityPredicates.canBePushedBy(this));
        if (!list.isEmpty()) {
            boolean bl = !this.getEntityWorld().isClient() && !(this.getControllingPassenger() instanceof PlayerEntity);
            for (Entity lv3 : list) {
                if (lv3.hasPassenger(this)) continue;
                if (bl && this.getPassengerList().size() < this.getMaxPassengers() && !lv3.hasVehicle() && this.isSmallerThanBoat(lv3) && lv3 instanceof LivingEntity && !lv3.getType().isIn(EntityTypeTags.CANNOT_BE_PUSHED_ONTO_BOATS)) {
                    lv3.startRiding(this);
                    continue;
                }
                this.pushAwayFrom(lv3);
            }
        }
    }

    private void handleBubbleColumn() {
        if (this.getEntityWorld().isClient()) {
            int i = this.getBubbleWobbleTicks();
            this.bubbleWobbleStrength = i > 0 ? (this.bubbleWobbleStrength += 0.05f) : (this.bubbleWobbleStrength -= 0.1f);
            this.bubbleWobbleStrength = MathHelper.clamp(this.bubbleWobbleStrength, 0.0f, 1.0f);
            this.lastBubbleWobble = this.bubbleWobble;
            this.bubbleWobble = 10.0f * (float)Math.sin(0.5 * (double)this.age) * this.bubbleWobbleStrength;
        } else {
            int i;
            if (!this.onBubbleColumnSurface) {
                this.setBubbleWobbleTicks(0);
            }
            if ((i = this.getBubbleWobbleTicks()) > 0) {
                this.setBubbleWobbleTicks(--i);
                int j = 60 - i - 1;
                if (j > 0 && i == 0) {
                    this.setBubbleWobbleTicks(0);
                    Vec3d lv = this.getVelocity();
                    if (this.bubbleColumnIsDrag) {
                        this.setVelocity(lv.add(0.0, -0.7, 0.0));
                        this.removeAllPassengers();
                    } else {
                        this.setVelocity(lv.x, this.hasPassenger((Entity passenger) -> passenger instanceof PlayerEntity) ? 2.7 : 0.6, lv.z);
                    }
                }
                this.onBubbleColumnSurface = false;
            }
        }
    }

    @Nullable
    protected SoundEvent getPaddleSound() {
        return switch (this.checkLocation().ordinal()) {
            case 0, 1, 2 -> SoundEvents.ENTITY_BOAT_PADDLE_WATER;
            case 3 -> SoundEvents.ENTITY_BOAT_PADDLE_LAND;
            default -> null;
        };
    }

    public void setPaddlesMoving(boolean left, boolean right) {
        this.dataTracker.set(LEFT_PADDLE_MOVING, left);
        this.dataTracker.set(RIGHT_PADDLE_MOVING, right);
    }

    public float lerpPaddlePhase(int paddle, float tickProgress) {
        if (this.isPaddleMoving(paddle)) {
            return MathHelper.clampedLerp(this.paddlePhases[paddle] - 0.3926991f, this.paddlePhases[paddle], tickProgress);
        }
        return 0.0f;
    }

    @Override
    @Nullable
    public Leashable.LeashData getLeashData() {
        return this.leashData;
    }

    @Override
    public void setLeashData(@Nullable Leashable.LeashData leashData) {
        this.leashData = leashData;
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, 0.88f * this.getHeight(), 0.64f * this.getWidth());
    }

    @Override
    public boolean canUseQuadLeashAttachmentPoint() {
        return true;
    }

    @Override
    public Vec3d[] getQuadLeashOffsets() {
        return Leashable.createQuadLeashOffsets(this, 0.0, 0.64, 0.382, 0.88);
    }

    private Location checkLocation() {
        Location lv = this.getUnderWaterLocation();
        if (lv != null) {
            this.waterLevel = this.getBoundingBox().maxY;
            return lv;
        }
        if (this.checkBoatInWater()) {
            return Location.IN_WATER;
        }
        float f = this.getNearbySlipperiness();
        if (f > 0.0f) {
            this.nearbySlipperiness = f;
            return Location.ON_LAND;
        }
        return Location.IN_AIR;
    }

    public float getWaterHeightBelow() {
        Box lv = this.getBoundingBox();
        int i = MathHelper.floor(lv.minX);
        int j = MathHelper.ceil(lv.maxX);
        int k = MathHelper.floor(lv.maxY);
        int l = MathHelper.ceil(lv.maxY - this.fallVelocity);
        int m = MathHelper.floor(lv.minZ);
        int n = MathHelper.ceil(lv.maxZ);
        BlockPos.Mutable lv2 = new BlockPos.Mutable();
        block0: for (int o = k; o < l; ++o) {
            float f = 0.0f;
            for (int p = i; p < j; ++p) {
                for (int q = m; q < n; ++q) {
                    lv2.set(p, o, q);
                    FluidState lv3 = this.getEntityWorld().getFluidState(lv2);
                    if (lv3.isIn(FluidTags.WATER)) {
                        f = Math.max(f, lv3.getHeight(this.getEntityWorld(), lv2));
                    }
                    if (f >= 1.0f) continue block0;
                }
            }
            if (!(f < 1.0f)) continue;
            return (float)lv2.getY() + f;
        }
        return l + 1;
    }

    public float getNearbySlipperiness() {
        Box lv = this.getBoundingBox();
        Box lv2 = new Box(lv.minX, lv.minY - 0.001, lv.minZ, lv.maxX, lv.minY, lv.maxZ);
        int i = MathHelper.floor(lv2.minX) - 1;
        int j = MathHelper.ceil(lv2.maxX) + 1;
        int k = MathHelper.floor(lv2.minY) - 1;
        int l = MathHelper.ceil(lv2.maxY) + 1;
        int m = MathHelper.floor(lv2.minZ) - 1;
        int n = MathHelper.ceil(lv2.maxZ) + 1;
        VoxelShape lv3 = VoxelShapes.cuboid(lv2);
        float f = 0.0f;
        int o = 0;
        BlockPos.Mutable lv4 = new BlockPos.Mutable();
        for (int p = i; p < j; ++p) {
            for (int q = m; q < n; ++q) {
                int r = (p == i || p == j - 1 ? 1 : 0) + (q == m || q == n - 1 ? 1 : 0);
                if (r == 2) continue;
                for (int s = k; s < l; ++s) {
                    if (r > 0 && (s == k || s == l - 1)) continue;
                    lv4.set(p, s, q);
                    BlockState lv5 = this.getEntityWorld().getBlockState(lv4);
                    if (lv5.getBlock() instanceof LilyPadBlock || !VoxelShapes.matchesAnywhere(lv5.getCollisionShape(this.getEntityWorld(), lv4).offset(lv4), lv3, BooleanBiFunction.AND)) continue;
                    f += lv5.getBlock().getSlipperiness();
                    ++o;
                }
            }
        }
        return f / (float)o;
    }

    private boolean checkBoatInWater() {
        Box lv = this.getBoundingBox();
        int i = MathHelper.floor(lv.minX);
        int j = MathHelper.ceil(lv.maxX);
        int k = MathHelper.floor(lv.minY);
        int l = MathHelper.ceil(lv.minY + 0.001);
        int m = MathHelper.floor(lv.minZ);
        int n = MathHelper.ceil(lv.maxZ);
        boolean bl = false;
        this.waterLevel = -1.7976931348623157E308;
        BlockPos.Mutable lv2 = new BlockPos.Mutable();
        for (int o = i; o < j; ++o) {
            for (int p = k; p < l; ++p) {
                for (int q = m; q < n; ++q) {
                    lv2.set(o, p, q);
                    FluidState lv3 = this.getEntityWorld().getFluidState(lv2);
                    if (!lv3.isIn(FluidTags.WATER)) continue;
                    float f = (float)p + lv3.getHeight(this.getEntityWorld(), lv2);
                    this.waterLevel = Math.max((double)f, this.waterLevel);
                    bl |= lv.minY < (double)f;
                }
            }
        }
        return bl;
    }

    @Nullable
    private Location getUnderWaterLocation() {
        Box lv = this.getBoundingBox();
        double d = lv.maxY + 0.001;
        int i = MathHelper.floor(lv.minX);
        int j = MathHelper.ceil(lv.maxX);
        int k = MathHelper.floor(lv.maxY);
        int l = MathHelper.ceil(d);
        int m = MathHelper.floor(lv.minZ);
        int n = MathHelper.ceil(lv.maxZ);
        boolean bl = false;
        BlockPos.Mutable lv2 = new BlockPos.Mutable();
        for (int o = i; o < j; ++o) {
            for (int p = k; p < l; ++p) {
                for (int q = m; q < n; ++q) {
                    lv2.set(o, p, q);
                    FluidState lv3 = this.getEntityWorld().getFluidState(lv2);
                    if (!lv3.isIn(FluidTags.WATER) || !(d < (double)((float)lv2.getY() + lv3.getHeight(this.getEntityWorld(), lv2)))) continue;
                    if (lv3.isStill()) {
                        bl = true;
                        continue;
                    }
                    return Location.UNDER_FLOWING_WATER;
                }
            }
        }
        return bl ? Location.UNDER_WATER : null;
    }

    @Override
    protected double getGravity() {
        return 0.04;
    }

    private void updateVelocity() {
        double d = -this.getFinalGravity();
        double e = 0.0;
        float f = 0.05f;
        if (this.lastLocation == Location.IN_AIR && this.location != Location.IN_AIR && this.location != Location.ON_LAND) {
            this.waterLevel = this.getBodyY(1.0);
            double g = (double)(this.getWaterHeightBelow() - this.getHeight()) + 0.101;
            if (this.getEntityWorld().isSpaceEmpty(this, this.getBoundingBox().offset(0.0, g - this.getY(), 0.0))) {
                this.setPosition(this.getX(), g, this.getZ());
                this.setVelocity(this.getVelocity().multiply(1.0, 0.0, 1.0));
                this.fallVelocity = 0.0;
            }
            this.location = Location.IN_WATER;
        } else {
            if (this.location == Location.IN_WATER) {
                e = (this.waterLevel - this.getY()) / (double)this.getHeight();
                f = 0.9f;
            } else if (this.location == Location.UNDER_FLOWING_WATER) {
                d = -7.0E-4;
                f = 0.9f;
            } else if (this.location == Location.UNDER_WATER) {
                e = 0.01f;
                f = 0.45f;
            } else if (this.location == Location.IN_AIR) {
                f = 0.9f;
            } else if (this.location == Location.ON_LAND) {
                f = this.nearbySlipperiness;
                if (this.getControllingPassenger() instanceof PlayerEntity) {
                    this.nearbySlipperiness /= 2.0f;
                }
            }
            Vec3d lv = this.getVelocity();
            this.setVelocity(lv.x * (double)f, lv.y + d, lv.z * (double)f);
            this.yawVelocity *= f;
            if (e > 0.0) {
                Vec3d lv2 = this.getVelocity();
                this.setVelocity(lv2.x, (lv2.y + e * (this.getGravity() / 0.65)) * 0.75, lv2.z);
            }
        }
    }

    private void updatePaddles() {
        if (!this.hasPassengers()) {
            return;
        }
        float f = 0.0f;
        if (this.pressingLeft) {
            this.yawVelocity -= 1.0f;
        }
        if (this.pressingRight) {
            this.yawVelocity += 1.0f;
        }
        if (this.pressingRight != this.pressingLeft && !this.pressingForward && !this.pressingBack) {
            f += 0.005f;
        }
        this.setYaw(this.getYaw() + this.yawVelocity);
        if (this.pressingForward) {
            f += 0.04f;
        }
        if (this.pressingBack) {
            f -= 0.005f;
        }
        this.setVelocity(this.getVelocity().add(MathHelper.sin(-this.getYaw() * ((float)Math.PI / 180)) * f, 0.0, MathHelper.cos(this.getYaw() * ((float)Math.PI / 180)) * f));
        this.setPaddlesMoving(this.pressingRight && !this.pressingLeft || this.pressingForward, this.pressingLeft && !this.pressingRight || this.pressingForward);
    }

    protected float getPassengerHorizontalOffset() {
        return 0.0f;
    }

    public boolean isSmallerThanBoat(Entity entity) {
        return entity.getWidth() < this.getWidth();
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, Entity.PositionUpdater positionUpdater) {
        super.updatePassengerPosition(passenger, positionUpdater);
        if (passenger.getType().isIn(EntityTypeTags.CAN_TURN_IN_BOATS)) {
            return;
        }
        passenger.setYaw(passenger.getYaw() + this.yawVelocity);
        passenger.setHeadYaw(passenger.getHeadYaw() + this.yawVelocity);
        this.clampPassengerYaw(passenger);
        if (passenger instanceof AnimalEntity && this.getPassengerList().size() == this.getMaxPassengers()) {
            int i = passenger.getId() % 2 == 0 ? 90 : 270;
            passenger.setBodyYaw(((AnimalEntity)passenger).bodyYaw + (float)i);
            passenger.setHeadYaw(passenger.getHeadYaw() + (float)i);
        }
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        Vec3d lv = AbstractBoatEntity.getPassengerDismountOffset(this.getWidth() * MathHelper.SQUARE_ROOT_OF_TWO, passenger.getWidth(), passenger.getYaw());
        double d = this.getX() + lv.x;
        double e = this.getZ() + lv.z;
        BlockPos lv2 = BlockPos.ofFloored(d, this.getBoundingBox().maxY, e);
        BlockPos lv3 = lv2.down();
        if (!this.getEntityWorld().isWater(lv3)) {
            double g;
            ArrayList<Vec3d> list = Lists.newArrayList();
            double f = this.getEntityWorld().getDismountHeight(lv2);
            if (Dismounting.canDismountInBlock(f)) {
                list.add(new Vec3d(d, (double)lv2.getY() + f, e));
            }
            if (Dismounting.canDismountInBlock(g = this.getEntityWorld().getDismountHeight(lv3))) {
                list.add(new Vec3d(d, (double)lv3.getY() + g, e));
            }
            for (EntityPose lv4 : passenger.getPoses()) {
                for (Vec3d lv5 : list) {
                    if (!Dismounting.canPlaceEntityAt(this.getEntityWorld(), lv5, passenger, lv4)) continue;
                    passenger.setPose(lv4);
                    return lv5;
                }
            }
        }
        return super.updatePassengerForDismount(passenger);
    }

    protected void clampPassengerYaw(Entity passenger) {
        passenger.setBodyYaw(this.getYaw());
        float f = MathHelper.wrapDegrees(passenger.getYaw() - this.getYaw());
        float g = MathHelper.clamp(f, -105.0f, 105.0f);
        passenger.lastYaw += g - f;
        passenger.setYaw(passenger.getYaw() + g - f);
        passenger.setHeadYaw(passenger.getYaw());
    }

    @Override
    public void onPassengerLookAround(Entity passenger) {
        this.clampPassengerYaw(passenger);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        this.writeLeashData(view, this.leashData);
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.readLeashData(view);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        ActionResult lv = super.interact(player, hand);
        if (lv != ActionResult.PASS) {
            return lv;
        }
        if (!player.shouldCancelInteraction() && this.ticksUnderwater < 60.0f && (this.getEntityWorld().isClient() || player.startRiding(this))) {
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!this.getEntityWorld().isClient() && reason.shouldDestroy() && this.isLeashed()) {
            this.detachLeash();
        }
        super.remove(reason);
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
        this.fallVelocity = this.getVelocity().y;
        if (this.hasVehicle()) {
            return;
        }
        if (onGround) {
            this.onLanding();
        } else if (!this.getEntityWorld().getFluidState(this.getBlockPos().down()).isIn(FluidTags.WATER) && heightDifference < 0.0) {
            this.fallDistance -= (double)((float)heightDifference);
        }
    }

    public boolean isPaddleMoving(int paddle) {
        return this.dataTracker.get(paddle == 0 ? LEFT_PADDLE_MOVING : RIGHT_PADDLE_MOVING) != false && this.getControllingPassenger() != null;
    }

    private void setBubbleWobbleTicks(int bubbleWobbleTicks) {
        this.dataTracker.set(BUBBLE_WOBBLE_TICKS, bubbleWobbleTicks);
    }

    private int getBubbleWobbleTicks() {
        return this.dataTracker.get(BUBBLE_WOBBLE_TICKS);
    }

    public float lerpBubbleWobble(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastBubbleWobble, this.bubbleWobble);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengerList().size() < this.getMaxPassengers() && !this.isSubmergedIn(FluidTags.WATER);
    }

    protected int getMaxPassengers() {
        return 2;
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        LivingEntity lv;
        Entity entity = this.getFirstPassenger();
        return entity instanceof LivingEntity ? (lv = (LivingEntity)entity) : super.getControllingPassenger();
    }

    public void setInputs(boolean pressingLeft, boolean pressingRight, boolean pressingForward, boolean pressingBack) {
        this.pressingLeft = pressingLeft;
        this.pressingRight = pressingRight;
        this.pressingForward = pressingForward;
        this.pressingBack = pressingBack;
    }

    @Override
    public boolean isSubmergedInWater() {
        return this.location == Location.UNDER_WATER || this.location == Location.UNDER_FLOWING_WATER;
    }

    @Override
    protected final Item asItem() {
        return this.itemSupplier.get();
    }

    @Override
    public final ItemStack getPickBlockStack() {
        return new ItemStack(this.itemSupplier.get());
    }

    public static enum Location {
        IN_WATER,
        UNDER_WATER,
        UNDER_FLOWING_WATER,
        ON_LAND,
        IN_AIR;

    }
}

