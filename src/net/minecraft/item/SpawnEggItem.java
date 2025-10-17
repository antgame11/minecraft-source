/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.Spawner;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class SpawnEggItem
extends Item {
    private static final Map<EntityType<?>, SpawnEggItem> SPAWN_EGGS = Maps.newIdentityHashMap();

    public SpawnEggItem(Item.Settings arg) {
        super(arg);
        TypedEntityData<EntityType<?>> lv = this.getComponents().get(DataComponentTypes.ENTITY_DATA);
        if (lv != null) {
            SPAWN_EGGS.put(lv.getType(), this);
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World lv = context.getWorld();
        if (!(lv instanceof ServerWorld)) {
            return ActionResult.SUCCESS;
        }
        ServerWorld lv2 = (ServerWorld)lv;
        ItemStack lv3 = context.getStack();
        BlockPos lv4 = context.getBlockPos();
        Direction lv5 = context.getSide();
        BlockState lv6 = lv.getBlockState(lv4);
        BlockEntity blockEntity = lv.getBlockEntity(lv4);
        if (blockEntity instanceof Spawner) {
            Spawner lv7 = (Spawner)((Object)blockEntity);
            EntityType<?> lv8 = this.getEntityType(lv3);
            if (lv8 == null) {
                return ActionResult.FAIL;
            }
            if (!lv2.getServer().areSpawnerBlocksEnabled()) {
                PlayerEntity playerEntity = context.getPlayer();
                if (playerEntity instanceof ServerPlayerEntity) {
                    ServerPlayerEntity lv9 = (ServerPlayerEntity)playerEntity;
                    lv9.sendMessage(Text.translatable("advMode.notEnabled.spawner"));
                }
                return ActionResult.FAIL;
            }
            lv7.setEntityType(lv8, lv.getRandom());
            lv.updateListeners(lv4, lv6, lv6, Block.NOTIFY_ALL);
            lv.emitGameEvent((Entity)context.getPlayer(), GameEvent.BLOCK_CHANGE, lv4);
            lv3.decrement(1);
            return ActionResult.SUCCESS;
        }
        BlockPos lv10 = lv6.getCollisionShape(lv, lv4).isEmpty() ? lv4 : lv4.offset(lv5);
        return this.spawnMobEntity(context.getPlayer(), lv3, lv, lv10, true, !Objects.equals(lv4, lv10) && lv5 == Direction.UP);
    }

    private ActionResult spawnMobEntity(@Nullable LivingEntity entity, ItemStack stack, World world, BlockPos pos, boolean bl, boolean bl2) {
        EntityType<?> lv = this.getEntityType(stack);
        if (lv == null) {
            return ActionResult.FAIL;
        }
        if (!lv.isAllowedInPeaceful() && world.getDifficulty() == Difficulty.PEACEFUL) {
            return ActionResult.FAIL;
        }
        if (lv.spawnFromItemStack((ServerWorld)world, stack, entity, pos, SpawnReason.SPAWN_ITEM_USE, bl, bl2) != null) {
            stack.decrementUnlessCreative(1, entity);
            world.emitGameEvent((Entity)entity, GameEvent.ENTITY_PLACE, pos);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        BlockHitResult lv2 = SpawnEggItem.raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (lv2.getType() != HitResult.Type.BLOCK) {
            return ActionResult.PASS;
        }
        if (!(world instanceof ServerWorld)) {
            return ActionResult.SUCCESS;
        }
        ServerWorld lv3 = (ServerWorld)world;
        BlockPos lv4 = lv2.getBlockPos();
        if (!(world.getBlockState(lv4).getBlock() instanceof FluidBlock)) {
            return ActionResult.PASS;
        }
        if (!world.canEntityModifyAt(user, lv4) || !user.canPlaceOn(lv4, lv2.getSide(), lv)) {
            return ActionResult.FAIL;
        }
        ActionResult lv5 = this.spawnMobEntity(user, lv, world, lv4, false, false);
        if (lv5 == ActionResult.SUCCESS) {
            user.incrementStat(Stats.USED.getOrCreateStat(this));
        }
        return lv5;
    }

    public boolean isOfSameEntityType(ItemStack stack, EntityType<?> entityType) {
        return Objects.equals(this.getEntityType(stack), entityType);
    }

    @Nullable
    public static SpawnEggItem forEntity(@Nullable EntityType<?> type) {
        return SPAWN_EGGS.get(type);
    }

    public static Iterable<SpawnEggItem> getAll() {
        return Iterables.unmodifiableIterable(SPAWN_EGGS.values());
    }

    @Nullable
    public EntityType<?> getEntityType(ItemStack stack) {
        TypedEntityData<EntityType<?>> lv = stack.get(DataComponentTypes.ENTITY_DATA);
        if (lv != null) {
            return lv.getType();
        }
        return null;
    }

    @Override
    public FeatureSet getRequiredFeatures() {
        return Optional.ofNullable(this.getComponents().get(DataComponentTypes.ENTITY_DATA)).map(TypedEntityData::getType).map(EntityType::getRequiredFeatures).orElseGet(FeatureSet::empty);
    }

    public Optional<MobEntity> spawnBaby(PlayerEntity user, MobEntity entity, EntityType<? extends MobEntity> entityType, ServerWorld world, Vec3d pos, ItemStack stack) {
        if (!this.isOfSameEntityType(stack, entityType)) {
            return Optional.empty();
        }
        MobEntity lv = entity instanceof PassiveEntity ? ((PassiveEntity)entity).createChild(world, (PassiveEntity)entity) : entityType.create(world, SpawnReason.SPAWN_ITEM_USE);
        if (lv == null) {
            return Optional.empty();
        }
        lv.setBaby(true);
        if (!lv.isBaby()) {
            return Optional.empty();
        }
        lv.refreshPositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), 0.0f, 0.0f);
        lv.copyComponentsFrom(stack);
        world.spawnEntityAndPassengers(lv);
        stack.decrementUnlessCreative(1, user);
        return Optional.of(lv);
    }

    @Override
    public boolean shouldShowOperatorBlockWarnings(ItemStack stack, @Nullable PlayerEntity player) {
        TypedEntityData<EntityType<?>> lv;
        if (player != null && player.getPermissionLevel() >= 2 && (lv = stack.get(DataComponentTypes.ENTITY_DATA)) != null) {
            return lv.getType().canPotentiallyExecuteCommands();
        }
        return false;
    }
}

