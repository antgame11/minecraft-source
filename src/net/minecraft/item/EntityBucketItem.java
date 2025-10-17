/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class EntityBucketItem
extends BucketItem {
    private final EntityType<? extends MobEntity> entityType;
    private final SoundEvent emptyingSound;

    public EntityBucketItem(EntityType<? extends MobEntity> type, Fluid fluid, SoundEvent emptyingSound, Item.Settings settings) {
        super(fluid, settings);
        this.entityType = type;
        this.emptyingSound = emptyingSound;
    }

    @Override
    public void onEmptied(@Nullable LivingEntity user, World world, ItemStack stack, BlockPos pos) {
        if (world instanceof ServerWorld) {
            this.spawnEntity((ServerWorld)world, stack, pos);
            world.emitGameEvent((Entity)user, GameEvent.ENTITY_PLACE, pos);
        }
    }

    @Override
    protected void playEmptyingSound(@Nullable LivingEntity user, WorldAccess world, BlockPos pos) {
        world.playSound(user, pos, this.emptyingSound, SoundCategory.NEUTRAL, 1.0f, 1.0f);
    }

    private void spawnEntity(ServerWorld world, ItemStack stack, BlockPos pos) {
        MobEntity lv = this.entityType.create(world, EntityType.copier(world, stack, null), pos, SpawnReason.BUCKET, true, false);
        if (lv instanceof Bucketable) {
            Bucketable lv2 = (Bucketable)((Object)lv);
            NbtComponent lv3 = stack.getOrDefault(DataComponentTypes.BUCKET_ENTITY_DATA, NbtComponent.DEFAULT);
            lv2.copyDataFromNbt(lv3.copyNbt());
            lv2.setFromBucket(true);
        }
        if (lv != null) {
            world.spawnEntityAndPassengers(lv);
            lv.playAmbientSound();
        }
    }
}

