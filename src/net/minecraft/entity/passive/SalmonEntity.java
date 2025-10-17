/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.SchoolingFishEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.function.ValueLists;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SalmonEntity
extends SchoolingFishEntity {
    private static final String TYPE_KEY = "type";
    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(SalmonEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public SalmonEntity(EntityType<? extends SalmonEntity> arg, World arg2) {
        super((EntityType<? extends SchoolingFishEntity>)arg, arg2);
        this.calculateDimensions();
    }

    @Override
    public int getMaxGroupSize() {
        return 5;
    }

    @Override
    public ItemStack getBucketItem() {
        return new ItemStack(Items.SALMON_BUCKET);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_SALMON_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_SALMON_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_SALMON_HURT;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.ENTITY_SALMON_FLOP;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VARIANT, Variant.DEFAULT.getIndex());
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (VARIANT.equals(data)) {
            this.calculateDimensions();
        }
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.put(TYPE_KEY, Variant.CODEC, this.getVariant());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setVariant(view.read(TYPE_KEY, Variant.CODEC).orElse(Variant.DEFAULT));
    }

    @Override
    public void copyDataToStack(ItemStack stack) {
        Bucketable.copyDataToStack(this, stack);
        stack.copy(DataComponentTypes.SALMON_SIZE, this);
    }

    private void setVariant(Variant variant) {
        this.dataTracker.set(VARIANT, variant.index);
    }

    public Variant getVariant() {
        return Variant.FROM_INDEX.apply(this.dataTracker.get(VARIANT));
    }

    @Override
    @Nullable
    public <T> T get(ComponentType<? extends T> type) {
        if (type == DataComponentTypes.SALMON_SIZE) {
            return SalmonEntity.castComponentValue(type, this.getVariant());
        }
        return super.get(type);
    }

    @Override
    protected void copyComponentsFrom(ComponentsAccess from) {
        this.copyComponentFrom(from, DataComponentTypes.SALMON_SIZE);
        super.copyComponentsFrom(from);
    }

    @Override
    protected <T> boolean setApplicableComponent(ComponentType<T> type, T value) {
        if (type == DataComponentTypes.SALMON_SIZE) {
            this.setVariant(SalmonEntity.castComponentValue(DataComponentTypes.SALMON_SIZE, value));
            return true;
        }
        return super.setApplicableComponent(type, value);
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Pool.Builder<Variant> lv = Pool.builder();
        lv.add(Variant.SMALL, 30);
        lv.add(Variant.MEDIUM, 50);
        lv.add(Variant.LARGE, 15);
        lv.build().getOrEmpty(this.random).ifPresent(this::setVariant);
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    public float getVariantScale() {
        return this.getVariant().scale;
    }

    @Override
    protected EntityDimensions getBaseDimensions(EntityPose pose) {
        return super.getBaseDimensions(pose).scaled(this.getVariantScale());
    }

    public static enum Variant implements StringIdentifiable
    {
        SMALL("small", 0, 0.5f),
        MEDIUM("medium", 1, 1.0f),
        LARGE("large", 2, 1.5f);

        public static final Variant DEFAULT;
        public static final StringIdentifiable.EnumCodec<Variant> CODEC;
        static final IntFunction<Variant> FROM_INDEX;
        public static final PacketCodec<ByteBuf, Variant> PACKET_CODEC;
        private final String id;
        final int index;
        final float scale;

        private Variant(String id, int index, float scale) {
            this.id = id;
            this.index = index;
            this.scale = scale;
        }

        @Override
        public String asString() {
            return this.id;
        }

        int getIndex() {
            return this.index;
        }

        static {
            DEFAULT = MEDIUM;
            CODEC = StringIdentifiable.createCodec(Variant::values);
            FROM_INDEX = ValueLists.createIndexToValueFunction(Variant::getIndex, Variant.values(), ValueLists.OutOfBoundsHandling.CLAMP);
            PACKET_CODEC = PacketCodecs.indexed(FROM_INDEX, Variant::getIndex);
        }
    }
}

