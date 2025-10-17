/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.decoration.painting;

import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Variants;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.PaintingVariantTags;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PaintingEntity
extends AbstractDecorationEntity {
    private static final TrackedData<RegistryEntry<PaintingVariant>> VARIANT = DataTracker.registerData(PaintingEntity.class, TrackedDataHandlerRegistry.PAINTING_VARIANT);
    public static final float field_51595 = 0.0625f;

    public PaintingEntity(EntityType<? extends PaintingEntity> arg, World arg2) {
        super((EntityType<? extends AbstractDecorationEntity>)arg, arg2);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VARIANT, Variants.getDefaultOrThrow(this.getRegistryManager(), RegistryKeys.PAINTING_VARIANT));
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (VARIANT.equals(data)) {
            this.updateAttachmentPosition();
        }
    }

    private void setVariant(RegistryEntry<PaintingVariant> variant) {
        this.dataTracker.set(VARIANT, variant);
    }

    public RegistryEntry<PaintingVariant> getVariant() {
        return this.dataTracker.get(VARIANT);
    }

    @Override
    @Nullable
    public <T> T get(ComponentType<? extends T> type) {
        if (type == DataComponentTypes.PAINTING_VARIANT) {
            return PaintingEntity.castComponentValue(type, this.getVariant());
        }
        return super.get(type);
    }

    @Override
    protected void copyComponentsFrom(ComponentsAccess from) {
        this.copyComponentFrom(from, DataComponentTypes.PAINTING_VARIANT);
        super.copyComponentsFrom(from);
    }

    @Override
    protected <T> boolean setApplicableComponent(ComponentType<T> type, T value) {
        if (type == DataComponentTypes.PAINTING_VARIANT) {
            this.setVariant(PaintingEntity.castComponentValue(DataComponentTypes.PAINTING_VARIANT, value));
            return true;
        }
        return super.setApplicableComponent(type, value);
    }

    public static Optional<PaintingEntity> placePainting(World world, BlockPos pos, Direction facing) {
        PaintingEntity lv = new PaintingEntity(world, pos);
        ArrayList<RegistryEntry> list = new ArrayList<RegistryEntry>();
        world.getRegistryManager().getOrThrow(RegistryKeys.PAINTING_VARIANT).iterateEntries(PaintingVariantTags.PLACEABLE).forEach(list::add);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        lv.setFacing(facing);
        list.removeIf(variant -> {
            lv.setVariant((RegistryEntry<PaintingVariant>)variant);
            return !lv.canStayAttached();
        });
        if (list.isEmpty()) {
            return Optional.empty();
        }
        int i = list.stream().mapToInt(PaintingEntity::getSize).max().orElse(0);
        list.removeIf(variant -> PaintingEntity.getSize(variant) < i);
        Optional optional = Util.getRandomOrEmpty(list, lv.random);
        if (optional.isEmpty()) {
            return Optional.empty();
        }
        lv.setVariant((RegistryEntry)optional.get());
        lv.setFacing(facing);
        return Optional.of(lv);
    }

    private static int getSize(RegistryEntry<PaintingVariant> variant) {
        return variant.value().getArea();
    }

    private PaintingEntity(World world, BlockPos pos) {
        super((EntityType<? extends AbstractDecorationEntity>)EntityType.PAINTING, world, pos);
    }

    public PaintingEntity(World world, BlockPos pos, Direction direction, RegistryEntry<PaintingVariant> variant) {
        this(world, pos);
        this.setVariant(variant);
        this.setFacing(direction);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.put("facing", Direction.HORIZONTAL_QUARTER_TURNS_CODEC, this.getHorizontalFacing());
        super.writeCustomData(view);
        Variants.writeData(view, this.getVariant());
    }

    @Override
    protected void readCustomData(ReadView view) {
        Direction lv = view.read("facing", Direction.HORIZONTAL_QUARTER_TURNS_CODEC).orElse(Direction.SOUTH);
        super.readCustomData(view);
        this.setFacing(lv);
        Variants.fromData(view, RegistryKeys.PAINTING_VARIANT).ifPresent(this::setVariant);
    }

    @Override
    protected Box calculateBoundingBox(BlockPos pos, Direction side) {
        float f = 0.46875f;
        Vec3d lv = Vec3d.ofCenter(pos).offset(side, -0.46875);
        PaintingVariant lv2 = this.getVariant().value();
        double d = this.getOffset(lv2.width());
        double e = this.getOffset(lv2.height());
        Direction lv3 = side.rotateYCounterclockwise();
        Vec3d lv4 = lv.offset(lv3, d).offset(Direction.UP, e);
        Direction.Axis lv5 = side.getAxis();
        double g = lv5 == Direction.Axis.X ? 0.0625 : (double)lv2.width();
        double h = lv2.height();
        double i = lv5 == Direction.Axis.Z ? 0.0625 : (double)lv2.width();
        return Box.of(lv4, g, h, i);
    }

    private double getOffset(int length) {
        return length % 2 == 0 ? 0.5 : 0.0;
    }

    @Override
    public void onBreak(ServerWorld world, @Nullable Entity breaker) {
        PlayerEntity lv;
        if (!world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
            return;
        }
        this.playSound(SoundEvents.ENTITY_PAINTING_BREAK, 1.0f, 1.0f);
        if (breaker instanceof PlayerEntity && (lv = (PlayerEntity)breaker).isInCreativeMode()) {
            return;
        }
        this.dropItem(world, Items.PAINTING);
    }

    @Override
    public void onPlace() {
        this.playSound(SoundEvents.ENTITY_PAINTING_PLACE, 1.0f, 1.0f);
    }

    @Override
    public void refreshPositionAndAngles(double x, double y, double z, float yaw, float pitch) {
        this.setPosition(x, y, z);
    }

    @Override
    public Vec3d getSyncedPos() {
        return Vec3d.of(this.attachedBlockPos);
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        return new EntitySpawnS2CPacket((Entity)this, this.getHorizontalFacing().getIndex(), this.getAttachedBlockPos());
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        this.setFacing(Direction.byIndex(packet.getEntityData()));
    }

    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(Items.PAINTING);
    }
}

