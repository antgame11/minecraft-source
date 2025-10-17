/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface Leashable {
    public static final String LEASH_NBT_KEY = "leash";
    public static final double DEFAULT_SNAPPING_DISTANCE = 12.0;
    public static final double DEFAULT_ELASTIC_DISTANCE = 6.0;
    public static final double field_60003 = 16.0;
    public static final Vec3d ELASTICITY_MULTIPLIER = new Vec3d(0.8, 0.2, 0.8);
    public static final float field_59997 = 0.7f;
    public static final double field_59998 = 10.0;
    public static final double field_59999 = 0.11;
    public static final List<Vec3d> HELD_ENTITY_ATTACHMENT_POINT = ImmutableList.of(new Vec3d(0.0, 0.5, 0.5));
    public static final List<Vec3d> LEASH_HOLDER_ATTACHMENT_POINT = ImmutableList.of(new Vec3d(0.0, 0.5, 0.0));
    public static final List<Vec3d> QUAD_LEASH_ATTACHMENT_POINTS = ImmutableList.of(new Vec3d(-0.5, 0.5, 0.5), new Vec3d(-0.5, 0.5, -0.5), new Vec3d(0.5, 0.5, -0.5), new Vec3d(0.5, 0.5, 0.5));

    @Nullable
    public LeashData getLeashData();

    public void setLeashData(@Nullable LeashData var1);

    default public boolean isLeashed() {
        return this.getLeashData() != null && this.getLeashData().leashHolder != null;
    }

    default public boolean mightBeLeashed() {
        return this.getLeashData() != null;
    }

    default public boolean canBeLeashedTo(Entity entity) {
        if (this == entity) {
            return false;
        }
        if (this.getDistanceToCenter(entity) > this.getLeashSnappingDistance()) {
            return false;
        }
        return this.canBeLeashed();
    }

    default public double getDistanceToCenter(Entity entity) {
        return entity.getBoundingBox().getCenter().distanceTo(((Entity)((Object)this)).getBoundingBox().getCenter());
    }

    default public boolean canBeLeashed() {
        return true;
    }

    default public void setUnresolvedLeashHolderId(int unresolvedLeashHolderId) {
        this.setLeashData(new LeashData(unresolvedLeashHolderId));
        Leashable.detachLeash((Entity)((Object)this), false, false);
    }

    default public void readLeashData(ReadView view) {
        LeashData lv = view.read(LEASH_NBT_KEY, LeashData.CODEC).orElse(null);
        if (this.getLeashData() != null && lv == null) {
            this.detachLeashWithoutDrop();
        }
        this.setLeashData(lv);
    }

    default public void writeLeashData(WriteView view, @Nullable LeashData leashData) {
        view.putNullable(LEASH_NBT_KEY, LeashData.CODEC, leashData);
    }

    private static <E extends Entity> void resolveLeashData(E entity, LeashData leashData) {
        World world;
        if (leashData.unresolvedLeashData != null && (world = entity.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            Optional<UUID> optional = leashData.unresolvedLeashData.left();
            Optional<BlockPos> optional2 = leashData.unresolvedLeashData.right();
            if (optional.isPresent()) {
                Entity lv2 = lv.getEntity(optional.get());
                if (lv2 != null) {
                    Leashable.attachLeash(entity, lv2, true);
                    return;
                }
            } else if (optional2.isPresent()) {
                Leashable.attachLeash(entity, LeashKnotEntity.getOrCreate(lv, optional2.get()), true);
                return;
            }
            if (entity.age > 100) {
                entity.dropItem(lv, Items.LEAD);
                ((Leashable)((Object)entity)).setLeashData(null);
            }
        }
    }

    default public void detachLeash() {
        Leashable.detachLeash((Entity)((Object)this), true, true);
    }

    default public void detachLeashWithoutDrop() {
        Leashable.detachLeash((Entity)((Object)this), true, false);
    }

    default public void onLeashRemoved() {
    }

    private static <E extends Entity> void detachLeash(E entity, boolean sendPacket, boolean dropItem) {
        LeashData lv = ((Leashable)((Object)entity)).getLeashData();
        if (lv != null && lv.leashHolder != null) {
            ((Leashable)((Object)entity)).setLeashData(null);
            ((Leashable)((Object)entity)).onLeashRemoved();
            World world = entity.getEntityWorld();
            if (world instanceof ServerWorld) {
                ServerWorld lv2 = (ServerWorld)world;
                if (dropItem) {
                    entity.dropItem(lv2, Items.LEAD);
                }
                if (sendPacket) {
                    lv2.getChunkManager().sendToOtherNearbyPlayers(entity, new EntityAttachS2CPacket(entity, null));
                }
                lv.leashHolder.onHeldLeashUpdate((Leashable)((Object)entity));
            }
        }
    }

    public static <E extends Entity> void tickLeash(ServerWorld world, E entity) {
        Entity lv2;
        LeashData lv = ((Leashable)((Object)entity)).getLeashData();
        if (lv != null && lv.unresolvedLeashData != null) {
            Leashable.resolveLeashData(entity, lv);
        }
        if (lv == null || lv.leashHolder == null) {
            return;
        }
        if (!entity.isInteractable() || !lv.leashHolder.isInteractable()) {
            if (world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                ((Leashable)((Object)entity)).detachLeash();
            } else {
                ((Leashable)((Object)entity)).detachLeashWithoutDrop();
            }
        }
        if ((lv2 = ((Leashable)((Object)entity)).getLeashHolder()) != null && lv2.getEntityWorld() == entity.getEntityWorld()) {
            double d = ((Leashable)((Object)entity)).getDistanceToCenter(lv2);
            ((Leashable)((Object)entity)).beforeLeashTick(lv2);
            if (d > ((Leashable)((Object)entity)).getLeashSnappingDistance()) {
                world.playSound(null, lv2.getX(), lv2.getY(), lv2.getZ(), SoundEvents.ITEM_LEAD_BREAK, SoundCategory.NEUTRAL, 1.0f, 1.0f);
                ((Leashable)((Object)entity)).snapLongLeash();
            } else if (d > ((Leashable)((Object)entity)).getElasticLeashDistance() - (double)lv2.getWidth() - (double)entity.getWidth() && ((Leashable)((Object)entity)).applyElasticity(lv2, lv)) {
                ((Leashable)((Object)entity)).onLongLeashTick();
            } else {
                ((Leashable)((Object)entity)).onShortLeashTick(lv2);
            }
            entity.setYaw((float)((double)entity.getYaw() - lv.momentum));
            lv.momentum *= (double)Leashable.getSlipperiness(entity);
        }
    }

    default public void onLongLeashTick() {
        Entity lv = (Entity)((Object)this);
        lv.limitFallDistance();
    }

    default public double getLeashSnappingDistance() {
        return 12.0;
    }

    default public double getElasticLeashDistance() {
        return 6.0;
    }

    public static <E extends Entity> float getSlipperiness(E entity) {
        if (entity.isOnGround()) {
            return entity.getEntityWorld().getBlockState(entity.getVelocityAffectingPos()).getBlock().getSlipperiness() * 0.91f;
        }
        if (entity.isInFluid()) {
            return 0.8f;
        }
        return 0.91f;
    }

    default public void beforeLeashTick(Entity leashHolder) {
        leashHolder.tickHeldLeash(this);
    }

    default public void snapLongLeash() {
        this.detachLeash();
    }

    default public void onShortLeashTick(Entity entity) {
    }

    default public boolean applyElasticity(Entity leashHolder, LeashData leashData) {
        boolean bl = leashHolder.hasQuadLeashAttachmentPoints() && this.canUseQuadLeashAttachmentPoint();
        List<Elasticity> list = Leashable.calculateLeashElasticities((Entity)((Object)this), leashHolder, bl ? QUAD_LEASH_ATTACHMENT_POINTS : HELD_ENTITY_ATTACHMENT_POINT, bl ? QUAD_LEASH_ATTACHMENT_POINTS : LEASH_HOLDER_ATTACHMENT_POINT);
        if (list.isEmpty()) {
            return false;
        }
        Elasticity lv = Elasticity.sumOf(list).multiply(bl ? 0.25 : 1.0);
        leashData.momentum += 10.0 * lv.torque();
        Vec3d lv2 = Leashable.getLeashHolderMovement(leashHolder).subtract(((Entity)((Object)this)).getMovement());
        ((Entity)((Object)this)).addVelocityInternal(lv.force().multiply(ELASTICITY_MULTIPLIER).add(lv2.multiply(0.11)));
        return true;
    }

    private static Vec3d getLeashHolderMovement(Entity leashHolder) {
        MobEntity lv;
        if (leashHolder instanceof MobEntity && (lv = (MobEntity)leashHolder).isAiDisabled()) {
            return Vec3d.ZERO;
        }
        return leashHolder.getMovement();
    }

    private static <E extends Entity> List<Elasticity> calculateLeashElasticities(E heldEntity, Entity leashHolder, List<Vec3d> heldEntityAttachmentPoints, List<Vec3d> leashHolderAttachmentPoints) {
        double d = ((Leashable)((Object)heldEntity)).getElasticLeashDistance();
        Vec3d lv = Leashable.getLeashHolderMovement(heldEntity);
        float f = heldEntity.getYaw() * ((float)Math.PI / 180);
        Vec3d lv2 = new Vec3d(heldEntity.getWidth(), heldEntity.getHeight(), heldEntity.getWidth());
        float g = leashHolder.getYaw() * ((float)Math.PI / 180);
        Vec3d lv3 = new Vec3d(leashHolder.getWidth(), leashHolder.getHeight(), leashHolder.getWidth());
        ArrayList<Elasticity> list3 = new ArrayList<Elasticity>();
        for (int i = 0; i < heldEntityAttachmentPoints.size(); ++i) {
            Vec3d lv4 = heldEntityAttachmentPoints.get(i).multiply(lv2).rotateY(-f);
            Vec3d lv5 = heldEntity.getEntityPos().add(lv4);
            Vec3d lv6 = leashHolderAttachmentPoints.get(i).multiply(lv3).rotateY(-g);
            Vec3d lv7 = leashHolder.getEntityPos().add(lv6);
            Leashable.calculateLeashElasticity(lv7, lv5, d, lv, lv4).ifPresent(list3::add);
        }
        return list3;
    }

    private static Optional<Elasticity> calculateLeashElasticity(Vec3d leashHolderAttachmentPos, Vec3d heldEntityAttachmentPos, double elasticDistance, Vec3d heldEntityMovement, Vec3d heldEntityAttachmentPoint) {
        boolean bl;
        double e = heldEntityAttachmentPos.distanceTo(leashHolderAttachmentPos);
        if (e < elasticDistance) {
            return Optional.empty();
        }
        Vec3d lv = leashHolderAttachmentPos.subtract(heldEntityAttachmentPos).normalize().multiply(e - elasticDistance);
        double f = Elasticity.calculateTorque(heldEntityAttachmentPoint, lv);
        boolean bl2 = bl = heldEntityMovement.dotProduct(lv) >= 0.0;
        if (bl) {
            lv = lv.multiply(0.3f);
        }
        return Optional.of(new Elasticity(lv, f));
    }

    default public boolean canUseQuadLeashAttachmentPoint() {
        return false;
    }

    default public Vec3d[] getQuadLeashOffsets() {
        return Leashable.createQuadLeashOffsets((Entity)((Object)this), 0.0, 0.5, 0.5, 0.5);
    }

    public static Vec3d[] createQuadLeashOffsets(Entity leashedEntity, double addedZOffset, double zOffset, double xOffset, double yOffset) {
        float h = leashedEntity.getWidth();
        double i = addedZOffset * (double)h;
        double j = zOffset * (double)h;
        double k = xOffset * (double)h;
        double l = yOffset * (double)leashedEntity.getHeight();
        return new Vec3d[]{new Vec3d(-k, l, j + i), new Vec3d(-k, l, -j + i), new Vec3d(k, l, -j + i), new Vec3d(k, l, j + i)};
    }

    default public Vec3d getLeashOffset(float tickProgress) {
        return this.getLeashOffset();
    }

    default public Vec3d getLeashOffset() {
        Entity lv = (Entity)((Object)this);
        return new Vec3d(0.0, lv.getStandingEyeHeight(), lv.getWidth() * 0.4f);
    }

    default public void attachLeash(Entity leashHolder, boolean sendPacket) {
        if (this == leashHolder) {
            return;
        }
        Leashable.attachLeash((Entity)((Object)this), leashHolder, sendPacket);
    }

    private static <E extends Entity> void attachLeash(E entity, Entity leashHolder, boolean sendPacket) {
        World world;
        LeashData lv = ((Leashable)((Object)entity)).getLeashData();
        if (lv == null) {
            lv = new LeashData(leashHolder);
            ((Leashable)((Object)entity)).setLeashData(lv);
        } else {
            Entity lv2 = lv.leashHolder;
            lv.setLeashHolder(leashHolder);
            if (lv2 != null && lv2 != leashHolder) {
                lv2.onHeldLeashUpdate((Leashable)((Object)entity));
            }
        }
        if (sendPacket && (world = entity.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld lv3 = (ServerWorld)world;
            lv3.getChunkManager().sendToOtherNearbyPlayers(entity, new EntityAttachS2CPacket(entity, leashHolder));
        }
        if (entity.hasVehicle()) {
            entity.stopRiding();
        }
    }

    @Nullable
    default public Entity getLeashHolder() {
        return Leashable.getLeashHolder((Entity)((Object)this));
    }

    @Nullable
    private static <E extends Entity> Entity getLeashHolder(E entity) {
        Entity entity2;
        LeashData lv = ((Leashable)((Object)entity)).getLeashData();
        if (lv == null) {
            return null;
        }
        if (lv.unresolvedLeashHolderId != 0 && entity.getEntityWorld().isClient() && (entity2 = entity.getEntityWorld().getEntityById(lv.unresolvedLeashHolderId)) instanceof Entity) {
            Entity lv2 = entity2;
            lv.setLeashHolder(lv2);
        }
        return lv.leashHolder;
    }

    public static List<Leashable> collectLeashablesHeldBy(Entity leashHolder) {
        return Leashable.collectLeashablesAround(leashHolder, leashable -> leashable.getLeashHolder() == leashHolder);
    }

    public static List<Leashable> collectLeashablesAround(Entity entity, Predicate<Leashable> leashablePredicate) {
        return Leashable.collectLeashablesAround(entity.getEntityWorld(), entity.getBoundingBox().getCenter(), leashablePredicate);
    }

    public static List<Leashable> collectLeashablesAround(World world, Vec3d pos, Predicate<Leashable> leashablePredicate) {
        double d = 32.0;
        Box lv = Box.of(pos, 32.0, 32.0, 32.0);
        return world.getEntitiesByClass(Entity.class, lv, entity -> {
            Leashable lv;
            return entity instanceof Leashable && leashablePredicate.test(lv = (Leashable)((Object)entity));
        }).stream().map(Leashable.class::cast).toList();
    }

    public static final class LeashData {
        public static final Codec<LeashData> CODEC = Codec.xor(((MapCodec)Uuids.INT_STREAM_CODEC.fieldOf("UUID")).codec(), BlockPos.CODEC).xmap(LeashData::new, data -> {
            Entity lv = data.leashHolder;
            if (lv instanceof LeashKnotEntity) {
                LeashKnotEntity lv2 = (LeashKnotEntity)lv;
                return Either.right(lv2.getAttachedBlockPos());
            }
            if (data.leashHolder != null) {
                return Either.left(data.leashHolder.getUuid());
            }
            return Objects.requireNonNull(data.unresolvedLeashData, "Invalid LeashData had no attachment");
        });
        int unresolvedLeashHolderId;
        @Nullable
        public Entity leashHolder;
        @Nullable
        public Either<UUID, BlockPos> unresolvedLeashData;
        public double momentum;

        private LeashData(Either<UUID, BlockPos> unresolvedLeashData) {
            this.unresolvedLeashData = unresolvedLeashData;
        }

        LeashData(Entity leashHolder) {
            this.leashHolder = leashHolder;
        }

        LeashData(int unresolvedLeashHolderId) {
            this.unresolvedLeashHolderId = unresolvedLeashHolderId;
        }

        public void setLeashHolder(Entity leashHolder) {
            this.leashHolder = leashHolder;
            this.unresolvedLeashData = null;
            this.unresolvedLeashHolderId = 0;
        }
    }

    public record Elasticity(Vec3d force, double torque) {
        static Elasticity ZERO = new Elasticity(Vec3d.ZERO, 0.0);

        static double calculateTorque(Vec3d force, Vec3d force2) {
            return force.z * force2.x - force.x * force2.z;
        }

        static Elasticity sumOf(List<Elasticity> elasticities) {
            if (elasticities.isEmpty()) {
                return ZERO;
            }
            double d = 0.0;
            double e = 0.0;
            double f = 0.0;
            double g = 0.0;
            for (Elasticity lv : elasticities) {
                Vec3d lv2 = lv.force;
                d += lv2.x;
                e += lv2.y;
                f += lv2.z;
                g += lv.torque;
            }
            return new Elasticity(new Vec3d(d, e, f), g);
        }

        public Elasticity multiply(double value) {
            return new Elasticity(this.force.multiply(value), this.torque * value);
        }
    }
}

