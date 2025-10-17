/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.TargetUtil;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.Nullable;

public class MoveItemsTask
extends MultiTickTask<PathAwareEntity> {
    public static final int INTERACTION_TICKS = 60;
    private static final int VISITED_POSITION_EXPIRY = 6000;
    private static final int MAX_STACK_SIZE_AT_ONCE = 16;
    private static final int VISITS_UNTIL_COOLDOWN = 10;
    private static final int field_62427 = 50;
    private static final int field_63014 = 1;
    private static final int COOLDOWN_EXPIRY = 140;
    private static final double QUEUING_RANGE = 3.0;
    private static final double INTERACTION_RANGE = 0.5;
    private static final double field_62428 = 1.0;
    private static final double field_62911 = 2.0;
    private final float speed;
    private final int horizontalRange;
    private final int verticalRange;
    private final Predicate<BlockState> inputContainerPredicate;
    private final Predicate<BlockState> outputContainerPredicate;
    private final Predicate<Storage> storagePredicate;
    private final Consumer<PathAwareEntity> travellingCallback;
    private final Map<InteractionState, InteractionCallback> interactionCallbacks;
    @Nullable
    private Storage targetStorage = null;
    private NavigationState navigationState;
    @Nullable
    private InteractionState interactionState;
    private int interactionTicks;

    public MoveItemsTask(float speed, Predicate<BlockState> inputContainerPredicate, Predicate<BlockState> outputChestPredicate, int horizontalRange, int verticalRange, Map<InteractionState, InteractionCallback> interactionCallbacks, Consumer<PathAwareEntity> travellingCallback, Predicate<Storage> storagePredicate) {
        super(ImmutableMap.of(MemoryModuleType.VISITED_BLOCK_POSITIONS, MemoryModuleState.REGISTERED, MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS, MemoryModuleState.REGISTERED, MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.IS_PANICKING, MemoryModuleState.VALUE_ABSENT));
        this.speed = speed;
        this.inputContainerPredicate = inputContainerPredicate;
        this.outputContainerPredicate = outputChestPredicate;
        this.horizontalRange = horizontalRange;
        this.verticalRange = verticalRange;
        this.travellingCallback = travellingCallback;
        this.storagePredicate = storagePredicate;
        this.interactionCallbacks = interactionCallbacks;
        this.navigationState = NavigationState.TRAVELLING;
    }

    @Override
    protected void run(ServerWorld arg, PathAwareEntity arg2, long l) {
        EntityNavigation entityNavigation = arg2.getNavigation();
        if (entityNavigation instanceof MobNavigation) {
            MobNavigation lv = (MobNavigation)entityNavigation;
            lv.setSkipRetarget(true);
        }
    }

    @Override
    protected boolean shouldRun(ServerWorld arg, PathAwareEntity arg2) {
        return !arg2.isLeashed();
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, PathAwareEntity arg2, long l) {
        return arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS).isEmpty() && !arg2.isPanicking() && !arg2.isLeashed();
    }

    @Override
    protected boolean isTimeLimitExceeded(long time) {
        return false;
    }

    @Override
    protected void keepRunning(ServerWorld arg, PathAwareEntity arg2, long l) {
        boolean bl = this.tick(arg, arg2);
        if (this.targetStorage == null) {
            this.finishRunning(arg, arg2, l);
            return;
        }
        if (bl) {
            return;
        }
        if (this.navigationState.equals((Object)NavigationState.QUEUING)) {
            this.tickQueuing(this.targetStorage, arg, arg2);
        }
        if (this.navigationState.equals((Object)NavigationState.TRAVELLING)) {
            this.tickTravelling(this.targetStorage, arg, arg2);
        }
        if (this.navigationState.equals((Object)NavigationState.INTERACTING)) {
            this.tickInteracting(this.targetStorage, arg, arg2);
        }
    }

    private boolean tick(ServerWorld world, PathAwareEntity entity) {
        if (!this.hasValidTargetStorage(world, entity)) {
            this.invalidateTargetStorage(entity);
            Optional<Storage> optional = this.findStorage(world, entity);
            if (optional.isPresent()) {
                this.targetStorage = optional.get();
                this.transitionToTravelling(entity);
                this.markVisited(entity, world, this.targetStorage.pos);
                return true;
            }
            this.cooldown(entity);
            return true;
        }
        return false;
    }

    private void tickQueuing(Storage storage, World world, PathAwareEntity entity) {
        if (!this.matchesStoragePredicate(storage, world)) {
            this.onCannotUseStorage(entity);
        }
    }

    protected void tickTravelling(Storage storage, World world, PathAwareEntity entity) {
        if (this.isWithinRange(3.0, storage, world, entity, this.atCenterY(entity)) && this.matchesStoragePredicate(storage, world)) {
            this.transitionToQueuing(entity);
        } else if (this.isWithinRange(MoveItemsTask.getSightRange(entity), storage, world, entity, this.atCenterY(entity))) {
            this.transitionToInteracting(storage, entity);
        } else {
            this.walkTowardsTargetStorage(entity);
        }
    }

    private Vec3d atCenterY(PathAwareEntity entity) {
        return this.atCenterY(entity, entity.getEntityPos());
    }

    protected void tickInteracting(Storage storage, World world, PathAwareEntity entity) {
        if (!this.isWithinRange(2.0, storage, world, entity, this.atCenterY(entity))) {
            this.transitionToTravelling(entity);
        } else {
            ++this.interactionTicks;
            this.setLookTarget(storage, entity);
            if (this.interactionTicks >= 60) {
                this.selectInteractionState(entity, storage.inventory, this::takeStack, (entityx, inventory) -> this.invalidateTargetStorage(entity), this::placeStack, (entityx, inventory) -> this.invalidateTargetStorage(entity));
                this.transitionToTravelling(entity);
            }
        }
    }

    private void transitionToQueuing(PathAwareEntity entity) {
        this.resetNavigation(entity);
        this.setNavigationState(NavigationState.QUEUING);
    }

    private void onCannotUseStorage(PathAwareEntity entity) {
        this.setNavigationState(NavigationState.TRAVELLING);
        this.walkTowardsTargetStorage(entity);
    }

    private void walkTowardsTargetStorage(PathAwareEntity entity) {
        if (this.targetStorage != null) {
            TargetUtil.walkTowards((LivingEntity)entity, this.targetStorage.pos, this.speed, 0);
        }
    }

    private void transitionToInteracting(Storage storage, PathAwareEntity entity) {
        this.selectInteractionState(entity, storage.inventory, this.createSetInteractionStateCallback(InteractionState.PICKUP_ITEM), this.createSetInteractionStateCallback(InteractionState.PICKUP_NO_ITEM), this.createSetInteractionStateCallback(InteractionState.PLACE_ITEM), this.createSetInteractionStateCallback(InteractionState.PLACE_NO_ITEM));
        this.setNavigationState(NavigationState.INTERACTING);
    }

    private void transitionToTravelling(PathAwareEntity entity) {
        this.travellingCallback.accept(entity);
        this.setNavigationState(NavigationState.TRAVELLING);
        this.interactionState = null;
        this.interactionTicks = 0;
    }

    private BiConsumer<PathAwareEntity, Inventory> createSetInteractionStateCallback(InteractionState state) {
        return (entity, inventory) -> this.setInteractionState(state);
    }

    private void setNavigationState(NavigationState state) {
        this.navigationState = state;
    }

    private void setInteractionState(InteractionState state) {
        this.interactionState = state;
    }

    private void setLookTarget(Storage storage, PathAwareEntity entity) {
        entity.getBrain().remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(storage.pos));
        this.resetNavigation(entity);
        if (this.interactionState != null) {
            Optional.ofNullable(this.interactionCallbacks.get((Object)this.interactionState)).ifPresent(consumer -> consumer.accept(entity, storage, this.interactionTicks));
        }
    }

    private void selectInteractionState(PathAwareEntity entity, Inventory inventory, BiConsumer<PathAwareEntity, Inventory> pickupItemCallback, BiConsumer<PathAwareEntity, Inventory> pickupNoItemCallback, BiConsumer<PathAwareEntity, Inventory> placeItemCallback, BiConsumer<PathAwareEntity, Inventory> placeNoItemCallback) {
        if (MoveItemsTask.canPickUpItem(entity)) {
            if (MoveItemsTask.hasItem(inventory)) {
                pickupItemCallback.accept(entity, inventory);
            } else {
                pickupNoItemCallback.accept(entity, inventory);
            }
        } else if (MoveItemsTask.canInsert(entity, inventory)) {
            placeItemCallback.accept(entity, inventory);
        } else {
            placeNoItemCallback.accept(entity, inventory);
        }
    }

    private Optional<Storage> findStorage(ServerWorld world, PathAwareEntity entity) {
        Box lv = this.getSearchBoundingBox(entity);
        Set<GlobalPos> set = MoveItemsTask.getVisitedPositions(entity);
        Set<GlobalPos> set2 = MoveItemsTask.getUnreachablePositions(entity);
        List<ChunkPos> list = ChunkPos.stream(new ChunkPos(entity.getBlockPos()), Math.floorDiv(this.getHorizontalRange(entity), 16) + 1).toList();
        Storage lv2 = null;
        double d = 3.4028234663852886E38;
        for (ChunkPos lv3 : list) {
            WorldChunk lv4 = world.getChunkManager().getWorldChunk(lv3.x, lv3.z);
            if (lv4 == null) continue;
            for (BlockEntity lv5 : lv4.getBlockEntities().values()) {
                Storage lv7;
                ChestBlockEntity lv6;
                double e;
                if (!(lv5 instanceof ChestBlockEntity) || !((e = (lv6 = (ChestBlockEntity)lv5).getPos().getSquaredDistance(entity.getEntityPos())) < d) || (lv7 = this.getStorageFor(entity, world, lv6, set, set2, lv)) == null) continue;
                lv2 = lv7;
                d = e;
            }
        }
        return lv2 == null ? Optional.empty() : Optional.of(lv2);
    }

    @Nullable
    private Storage getStorageFor(PathAwareEntity entity, World world, BlockEntity blockEntity, Set<GlobalPos> visitedPositions, Set<GlobalPos> unreachablePositions, Box box) {
        BlockPos lv = blockEntity.getPos();
        boolean bl = box.contains(lv.getX(), lv.getY(), lv.getZ());
        if (!bl) {
            return null;
        }
        Storage lv2 = Storage.forContainer(blockEntity, world);
        if (lv2 == null) {
            return null;
        }
        boolean bl2 = this.testContainer(entity, lv2.state) && !this.hasVisited(visitedPositions, unreachablePositions, lv2, world) && !this.isLocked(lv2);
        return bl2 ? lv2 : null;
    }

    private boolean isLocked(Storage storage) {
        LockableContainerBlockEntity lv;
        BlockEntity blockEntity = storage.blockEntity;
        return blockEntity instanceof LockableContainerBlockEntity && (lv = (LockableContainerBlockEntity)blockEntity).isLocked();
    }

    private boolean hasValidTargetStorage(World world, PathAwareEntity entity) {
        boolean bl;
        boolean bl2 = bl = this.targetStorage != null && this.testContainer(entity, this.targetStorage.state) && this.isUnchanged(world, this.targetStorage);
        if (bl && !this.isChestBlocked(world, this.targetStorage)) {
            if (!this.navigationState.equals((Object)NavigationState.TRAVELLING)) {
                return true;
            }
            if (this.canNavigateTo(world, this.targetStorage, entity)) {
                return true;
            }
            this.markUnreachable(entity, world, this.targetStorage.pos);
        }
        return false;
    }

    private boolean canNavigateTo(World world, Storage storage, PathAwareEntity entity) {
        Path lv = entity.getNavigation().getCurrentPath() == null ? entity.getNavigation().findPathTo(storage.pos, 0) : entity.getNavigation().getCurrentPath();
        Vec3d lv2 = this.getTargetPos(lv, entity);
        boolean bl = this.isWithinRange(MoveItemsTask.getSightRange(entity), storage, world, entity, lv2);
        boolean bl2 = lv == null && !bl;
        return bl2 || this.isVisible(world, bl, lv2, storage, entity);
    }

    private Vec3d getTargetPos(@Nullable Path path, PathAwareEntity entity) {
        boolean bl = path == null || path.getEnd() == null;
        Vec3d lv = bl ? entity.getEntityPos() : path.getEnd().getBlockPos().toBottomCenterPos();
        return this.atCenterY(entity, lv);
    }

    private Vec3d atCenterY(PathAwareEntity entity, Vec3d pos) {
        return pos.add(0.0, entity.getBoundingBox().getLengthY() / 2.0, 0.0);
    }

    private boolean isChestBlocked(World world, Storage storage) {
        return ChestBlock.isChestBlocked(world, storage.pos);
    }

    private boolean isUnchanged(World world, Storage storage) {
        return storage.blockEntity.equals(world.getBlockEntity(storage.pos));
    }

    private Stream<Storage> getContainerStorages(Storage storage, World world) {
        if (storage.state.get(ChestBlock.CHEST_TYPE, ChestType.SINGLE) != ChestType.SINGLE) {
            Storage lv = Storage.forContainer(ChestBlock.getPosInFrontOf(storage.pos, storage.state), world);
            return lv != null ? Stream.of(storage, lv) : Stream.of(storage);
        }
        return Stream.of(storage);
    }

    private Box getSearchBoundingBox(PathAwareEntity entity) {
        int i = this.getHorizontalRange(entity);
        return new Box(entity.getBlockPos()).expand(i, this.getVerticalRange(entity), i);
    }

    private int getHorizontalRange(PathAwareEntity entity) {
        return entity.hasVehicle() ? 1 : this.horizontalRange;
    }

    private int getVerticalRange(PathAwareEntity entity) {
        return entity.hasVehicle() ? 1 : this.verticalRange;
    }

    private static Set<GlobalPos> getVisitedPositions(PathAwareEntity entity) {
        return entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS).orElse(Set.of());
    }

    private static Set<GlobalPos> getUnreachablePositions(PathAwareEntity entity) {
        return entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS).orElse(Set.of());
    }

    private boolean hasVisited(Set<GlobalPos> visitedPositions, Set<GlobalPos> checkedPositions, Storage storage, World visited) {
        return this.getContainerStorages(storage, visited).map(checkedStorage -> new GlobalPos(visited.getRegistryKey(), checkedStorage.pos)).anyMatch(pos -> visitedPositions.contains(pos) || checkedPositions.contains(pos));
    }

    private static boolean hasFinishedNavigation(PathAwareEntity entity) {
        return entity.getNavigation().getCurrentPath() != null && entity.getNavigation().getCurrentPath().isFinished();
    }

    protected void markVisited(PathAwareEntity entity, World world, BlockPos pos) {
        HashSet<GlobalPos> set = new HashSet<GlobalPos>(MoveItemsTask.getVisitedPositions(entity));
        set.add(new GlobalPos(world.getRegistryKey(), pos));
        if (set.size() > 10) {
            this.cooldown(entity);
        } else {
            entity.getBrain().remember(MemoryModuleType.VISITED_BLOCK_POSITIONS, set, 6000L);
        }
    }

    protected void markUnreachable(PathAwareEntity entity, World world, BlockPos blockPos) {
        HashSet<GlobalPos> set = new HashSet<GlobalPos>(MoveItemsTask.getVisitedPositions(entity));
        set.remove(new GlobalPos(world.getRegistryKey(), blockPos));
        HashSet<GlobalPos> set2 = new HashSet<GlobalPos>(MoveItemsTask.getUnreachablePositions(entity));
        set2.add(new GlobalPos(world.getRegistryKey(), blockPos));
        if (set2.size() > 50) {
            this.cooldown(entity);
        } else {
            entity.getBrain().remember(MemoryModuleType.VISITED_BLOCK_POSITIONS, set, 6000L);
            entity.getBrain().remember(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS, set2, 6000L);
        }
    }

    private boolean testContainer(PathAwareEntity entity, BlockState state) {
        return MoveItemsTask.canPickUpItem(entity) ? this.inputContainerPredicate.test(state) : this.outputContainerPredicate.test(state);
    }

    private static double getSightRange(PathAwareEntity entity) {
        return MoveItemsTask.hasFinishedNavigation(entity) ? 1.0 : 0.5;
    }

    private boolean isWithinRange(double range, Storage storage, World world, PathAwareEntity entity, Vec3d pos) {
        Box lv = entity.getBoundingBox();
        Box lv2 = Box.of(pos, lv.getLengthX(), lv.getLengthY(), lv.getLengthZ());
        return storage.state.getCollisionShape(world, storage.pos).getBoundingBox().expand(range, 0.5, range).offset(storage.pos).intersects(lv2);
    }

    private boolean isVisible(World world, boolean nextToStorage, Vec3d pos, Storage storage, PathAwareEntity entity) {
        return nextToStorage && this.isVisible(storage, world, entity, pos);
    }

    private boolean isVisible(Storage storage, World world, PathAwareEntity entity, Vec3d pos) {
        Vec3d lv = storage.pos.toCenterPos();
        return Direction.stream().map(direction -> lv.add(0.5 * (double)direction.getOffsetX(), 0.5 * (double)direction.getOffsetY(), 0.5 * (double)direction.getOffsetZ())).map(storagePos -> world.raycast(new RaycastContext(pos, (Vec3d)storagePos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity))).anyMatch(hitResult -> hitResult.getType() == HitResult.Type.BLOCK && hitResult.getBlockPos().equals(arg.pos));
    }

    private boolean matchesStoragePredicate(Storage storage, World world) {
        return this.getContainerStorages(storage, world).anyMatch(this.storagePredicate);
    }

    private static boolean canPickUpItem(PathAwareEntity entity) {
        return entity.getMainHandStack().isEmpty();
    }

    private static boolean hasItem(Inventory inventory) {
        return !inventory.isEmpty();
    }

    private static boolean canInsert(PathAwareEntity entity, Inventory inventory) {
        return inventory.isEmpty() || MoveItemsTask.hasExistingStack(entity, inventory);
    }

    private static boolean hasExistingStack(PathAwareEntity entity, Inventory inventory) {
        ItemStack lv = entity.getMainHandStack();
        for (ItemStack lv2 : inventory) {
            if (!ItemStack.areItemsEqual(lv2, lv)) continue;
            return true;
        }
        return false;
    }

    private void takeStack(PathAwareEntity entity, Inventory inventory) {
        entity.equipStack(EquipmentSlot.MAINHAND, MoveItemsTask.extractStack(inventory));
        entity.setDropGuaranteed(EquipmentSlot.MAINHAND);
        inventory.markDirty();
        this.resetVisitedPositions(entity);
    }

    private void placeStack(PathAwareEntity entity, Inventory inventory) {
        ItemStack lv = MoveItemsTask.insertStack(entity, inventory);
        inventory.markDirty();
        entity.equipStack(EquipmentSlot.MAINHAND, lv);
        if (lv.isEmpty()) {
            this.resetVisitedPositions(entity);
        } else {
            this.invalidateTargetStorage(entity);
        }
    }

    private static ItemStack extractStack(Inventory inventory) {
        int i = 0;
        for (ItemStack lv : inventory) {
            if (!lv.isEmpty()) {
                int j = Math.min(lv.getCount(), 16);
                return inventory.removeStack(i, j);
            }
            ++i;
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack insertStack(PathAwareEntity entity, Inventory inventory) {
        int i = 0;
        ItemStack lv = entity.getMainHandStack();
        for (ItemStack lv2 : inventory) {
            if (lv2.isEmpty()) {
                inventory.setStack(i, lv);
                return ItemStack.EMPTY;
            }
            if (ItemStack.areItemsAndComponentsEqual(lv2, lv) && lv2.getCount() < lv2.getMaxCount()) {
                int j = lv2.getMaxCount() - lv2.getCount();
                int k = Math.min(j, lv.getCount());
                lv2.setCount(lv2.getCount() + k);
                lv.setCount(lv.getCount() - j);
                inventory.setStack(i, lv2);
                if (lv.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
            ++i;
        }
        return lv;
    }

    protected void invalidateTargetStorage(PathAwareEntity entity) {
        this.interactionTicks = 0;
        this.targetStorage = null;
        entity.getNavigation().stop();
        entity.getBrain().forget(MemoryModuleType.WALK_TARGET);
    }

    protected void resetVisitedPositions(PathAwareEntity entity) {
        this.invalidateTargetStorage(entity);
        entity.getBrain().forget(MemoryModuleType.VISITED_BLOCK_POSITIONS);
        entity.getBrain().forget(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS);
    }

    private void cooldown(PathAwareEntity entity) {
        this.invalidateTargetStorage(entity);
        entity.getBrain().remember(MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS, 140);
        entity.getBrain().forget(MemoryModuleType.VISITED_BLOCK_POSITIONS);
        entity.getBrain().forget(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS);
    }

    @Override
    protected void finishRunning(ServerWorld arg, PathAwareEntity arg2, long l) {
        this.transitionToTravelling(arg2);
        EntityNavigation entityNavigation = arg2.getNavigation();
        if (entityNavigation instanceof MobNavigation) {
            MobNavigation lv = (MobNavigation)entityNavigation;
            lv.setSkipRetarget(false);
        }
    }

    private void resetNavigation(PathAwareEntity entity) {
        entity.getNavigation().stop();
        entity.setSidewaysSpeed(0.0f);
        entity.setUpwardSpeed(0.0f);
        entity.setMovementSpeed(0.0f);
        entity.setVelocity(0.0, entity.getVelocity().y, 0.0);
    }

    @Override
    protected /* synthetic */ boolean shouldKeepRunning(ServerWorld world, LivingEntity entity, long time) {
        return this.shouldKeepRunning(world, (PathAwareEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (PathAwareEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (PathAwareEntity)entity, time);
    }

    public record Storage(BlockPos pos, Inventory inventory, BlockEntity blockEntity, BlockState state) {
        @Nullable
        public static Storage forContainer(BlockEntity blockEntity, World world) {
            BlockPos lv = blockEntity.getPos();
            BlockState lv2 = blockEntity.getCachedState();
            Inventory lv3 = Storage.getInventory(blockEntity, lv2, world, lv);
            if (lv3 != null) {
                return new Storage(lv, lv3, blockEntity, lv2);
            }
            return null;
        }

        @Nullable
        public static Storage forContainer(BlockPos pos, World world) {
            BlockEntity lv = world.getBlockEntity(pos);
            return lv == null ? null : Storage.forContainer(lv, world);
        }

        @Nullable
        private static Inventory getInventory(BlockEntity blockEntity, BlockState state, World world, BlockPos pos) {
            Block block = state.getBlock();
            if (block instanceof ChestBlock) {
                ChestBlock lv = (ChestBlock)block;
                return ChestBlock.getInventory(lv, state, world, pos, false);
            }
            if (blockEntity instanceof Inventory) {
                Inventory lv2 = (Inventory)((Object)blockEntity);
                return lv2;
            }
            return null;
        }
    }

    public static enum NavigationState {
        TRAVELLING,
        QUEUING,
        INTERACTING;

    }

    public static enum InteractionState {
        PICKUP_ITEM,
        PICKUP_NO_ITEM,
        PLACE_ITEM,
        PLACE_NO_ITEM;

    }

    @FunctionalInterface
    public static interface InteractionCallback
    extends TriConsumer<PathAwareEntity, Storage, Integer> {
    }
}

