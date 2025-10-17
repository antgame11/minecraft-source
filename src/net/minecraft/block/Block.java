/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.SharedConstants;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SideShapeType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class Block
extends AbstractBlock
implements ItemConvertible {
    public static final MapCodec<Block> CODEC = Block.createCodec(Block::new);
    private static final Logger LOGGER = LogUtils.getLogger();
    private final RegistryEntry.Reference<Block> registryEntry = Registries.BLOCK.createEntry(this);
    public static final IdList<BlockState> STATE_IDS = new IdList();
    private static final LoadingCache<VoxelShape, Boolean> FULL_CUBE_SHAPE_CACHE = CacheBuilder.newBuilder().maximumSize(512L).weakKeys().build(new CacheLoader<VoxelShape, Boolean>(){

        @Override
        public Boolean load(VoxelShape arg) {
            return !VoxelShapes.matchesAnywhere(VoxelShapes.fullCube(), arg, BooleanBiFunction.NOT_SAME);
        }

        @Override
        public /* synthetic */ Object load(Object shape) throws Exception {
            return this.load((VoxelShape)shape);
        }
    });
    public static final int NOTIFY_NEIGHBORS = 1;
    public static final int NOTIFY_LISTENERS = 2;
    public static final int NO_REDRAW = 4;
    public static final int REDRAW_ON_MAIN_THREAD = 8;
    public static final int FORCE_STATE = 16;
    public static final int SKIP_DROPS = 32;
    public static final int MOVED = 64;
    public static final int SKIP_REDSTONE_WIRE_STATE_REPLACEMENT = 128;
    public static final int SKIP_BLOCK_ENTITY_REPLACED_CALLBACK = 256;
    public static final int SKIP_BLOCK_ADDED_CALLBACK = 512;
    public static final int SKIP_REDRAW_AND_BLOCK_ENTITY_REPLACED_CALLBACK = 260;
    public static final int NOTIFY_ALL = 3;
    public static final int NOTIFY_ALL_AND_REDRAW = 11;
    public static final int FORCE_STATE_AND_SKIP_CALLBACKS_AND_DROPS = 816;
    public static final float field_31023 = -1.0f;
    public static final float field_31024 = 0.0f;
    public static final int field_31025 = 512;
    protected final StateManager<Block, BlockState> stateManager;
    private BlockState defaultState;
    @Nullable
    private Item cachedItem;
    private static final int FACE_CULL_MAP_SIZE = 256;
    private static final ThreadLocal<Object2ByteLinkedOpenHashMap<VoxelShapePair>> FACE_CULL_MAP = ThreadLocal.withInitial(() -> {
        Object2ByteLinkedOpenHashMap<VoxelShapePair> object2ByteLinkedOpenHashMap = new Object2ByteLinkedOpenHashMap<VoxelShapePair>(256, 0.25f){

            @Override
            protected void rehash(int newN) {
            }
        };
        object2ByteLinkedOpenHashMap.defaultReturnValue((byte)127);
        return object2ByteLinkedOpenHashMap;
    });

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    public static int getRawIdFromState(@Nullable BlockState state) {
        if (state == null) {
            return 0;
        }
        int i = STATE_IDS.getRawId(state);
        return i == -1 ? 0 : i;
    }

    public static BlockState getStateFromRawId(int stateId) {
        BlockState lv = STATE_IDS.get(stateId);
        return lv == null ? Blocks.AIR.getDefaultState() : lv;
    }

    public static Block getBlockFromItem(@Nullable Item item) {
        if (item instanceof BlockItem) {
            return ((BlockItem)item).getBlock();
        }
        return Blocks.AIR;
    }

    public static BlockState pushEntitiesUpBeforeBlockChange(BlockState from, BlockState to, WorldAccess world, BlockPos pos) {
        VoxelShape lv = VoxelShapes.combine(from.getCollisionShape(world, pos), to.getCollisionShape(world, pos), BooleanBiFunction.ONLY_SECOND).offset(pos);
        if (lv.isEmpty()) {
            return to;
        }
        List<Entity> list = world.getOtherEntities(null, lv.getBoundingBox());
        for (Entity lv2 : list) {
            double d = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, lv2.getBoundingBox().offset(0.0, 1.0, 0.0), List.of(lv), -1.0);
            lv2.requestTeleportOffset(0.0, 1.0 + d, 0.0);
        }
        return to;
    }

    public static VoxelShape createCuboidShape(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return VoxelShapes.cuboid(minX / 16.0, minY / 16.0, minZ / 16.0, maxX / 16.0, maxY / 16.0, maxZ / 16.0);
    }

    public static VoxelShape[] createShapeArray(int size, IntFunction<VoxelShape> indexToShape) {
        return (VoxelShape[])IntStream.rangeClosed(0, size).mapToObj(indexToShape).toArray(VoxelShape[]::new);
    }

    public static VoxelShape createCubeShape(double size) {
        return Block.createCuboidShape(size, size, size);
    }

    public static VoxelShape createCuboidShape(double sizeX, double sizeY, double sizeZ) {
        double g = sizeY / 2.0;
        return Block.createColumnShape(sizeX, sizeZ, 8.0 - g, 8.0 + g);
    }

    public static VoxelShape createColumnShape(double sizeXz, double minY, double maxY) {
        return Block.createColumnShape(sizeXz, sizeXz, minY, maxY);
    }

    public static VoxelShape createColumnShape(double sizeX, double sizeZ, double minY, double maxY) {
        double h = sizeX / 2.0;
        double i = sizeZ / 2.0;
        return Block.createCuboidShape(8.0 - h, minY, 8.0 - i, 8.0 + h, maxY, 8.0 + i);
    }

    public static VoxelShape createCuboidZShape(double sizeXy, double minZ, double maxZ) {
        return Block.createCuboidZShape(sizeXy, sizeXy, minZ, maxZ);
    }

    public static VoxelShape createCuboidZShape(double sizeX, double sizeY, double minZ, double maxZ) {
        double h = sizeY / 2.0;
        return Block.createCuboidZShape(sizeX, 8.0 - h, 8.0 + h, minZ, maxZ);
    }

    public static VoxelShape createCuboidZShape(double sizeX, double minY, double maxY, double minZ, double maxZ) {
        double i = sizeX / 2.0;
        return Block.createCuboidShape(8.0 - i, minY, minZ, 8.0 + i, maxY, maxZ);
    }

    public static BlockState postProcessState(BlockState state, WorldAccess world, BlockPos pos) {
        BlockState lv = state;
        BlockPos.Mutable lv2 = new BlockPos.Mutable();
        for (Direction lv3 : DIRECTIONS) {
            lv2.set((Vec3i)pos, lv3);
            lv = lv.getStateForNeighborUpdate(world, world, pos, lv3, lv2, world.getBlockState(lv2), world.getRandom());
        }
        return lv;
    }

    public static void replace(BlockState state, BlockState newState, WorldAccess world, BlockPos pos, int flags) {
        Block.replace(state, newState, world, pos, flags, 512);
    }

    public static void replace(BlockState state, BlockState newState, WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth) {
        if (newState != state) {
            if (newState.isAir()) {
                if (!world.isClient()) {
                    world.breakBlock(pos, (flags & SKIP_DROPS) == 0, null, maxUpdateDepth);
                }
            } else {
                world.setBlockState(pos, newState, flags & ~SKIP_DROPS, maxUpdateDepth);
            }
        }
    }

    public Block(AbstractBlock.Settings arg) {
        super(arg);
        String string;
        StateManager.Builder<Block, BlockState> lv = new StateManager.Builder<Block, BlockState>(this);
        this.appendProperties(lv);
        this.stateManager = lv.build(Block::getDefaultState, BlockState::new);
        this.setDefaultState(this.stateManager.getDefaultState());
        if (SharedConstants.isDevelopment && !(string = this.getClass().getSimpleName()).endsWith("Block")) {
            LOGGER.error("Block classes should end with Block and {} doesn't.", (Object)string);
        }
    }

    public static boolean cannotConnect(BlockState state) {
        return state.getBlock() instanceof LeavesBlock || state.isOf(Blocks.BARRIER) || state.isOf(Blocks.CARVED_PUMPKIN) || state.isOf(Blocks.JACK_O_LANTERN) || state.isOf(Blocks.MELON) || state.isOf(Blocks.PUMPKIN) || state.isIn(BlockTags.SHULKER_BOXES);
    }

    protected static boolean generateBlockInteractLoot(ServerWorld world, RegistryKey<LootTable> lootTable, BlockState state, @Nullable BlockEntity blockEntity, @Nullable ItemStack tool, @Nullable Entity interactingEntity, BiConsumer<ServerWorld, ItemStack> lootConsumer) {
        return Block.generateLoot(world, lootTable, context -> context.add(LootContextParameters.BLOCK_STATE, state).addOptional(LootContextParameters.BLOCK_ENTITY, blockEntity).addOptional(LootContextParameters.INTERACTING_ENTITY, interactingEntity).addOptional(LootContextParameters.TOOL, tool).build(LootContextTypes.BLOCK_INTERACT), lootConsumer);
    }

    protected static boolean generateLoot(ServerWorld world, RegistryKey<LootTable> lootTable, Function<LootWorldContext.Builder, LootWorldContext> contextFactory, BiConsumer<ServerWorld, ItemStack> lootConsumer) {
        LootWorldContext lv2;
        LootTable lv = world.getServer().getReloadableRegistries().getLootTable(lootTable);
        ObjectArrayList<ItemStack> list = lv.generateLoot(lv2 = contextFactory.apply(new LootWorldContext.Builder(world)));
        if (!list.isEmpty()) {
            list.forEach(stack -> lootConsumer.accept(world, (ItemStack)stack));
            return true;
        }
        return false;
    }

    public static boolean shouldDrawSide(BlockState state, BlockState otherState, Direction side) {
        VoxelShape lv = otherState.getCullingFace(side.getOpposite());
        if (lv == VoxelShapes.fullCube()) {
            return false;
        }
        if (state.isSideInvisible(otherState, side)) {
            return false;
        }
        if (lv == VoxelShapes.empty()) {
            return true;
        }
        VoxelShape lv2 = state.getCullingFace(side);
        if (lv2 == VoxelShapes.empty()) {
            return true;
        }
        VoxelShapePair lv3 = new VoxelShapePair(lv2, lv);
        Object2ByteLinkedOpenHashMap<VoxelShapePair> object2ByteLinkedOpenHashMap = FACE_CULL_MAP.get();
        byte b = object2ByteLinkedOpenHashMap.getAndMoveToFirst(lv3);
        if (b != 127) {
            return b != 0;
        }
        boolean bl = VoxelShapes.matchesAnywhere(lv2, lv, BooleanBiFunction.ONLY_FIRST);
        if (object2ByteLinkedOpenHashMap.size() == 256) {
            object2ByteLinkedOpenHashMap.removeLastByte();
        }
        object2ByteLinkedOpenHashMap.putAndMoveToFirst(lv3, (byte)(bl ? 1 : 0));
        return bl;
    }

    public static boolean hasTopRim(BlockView world, BlockPos pos) {
        return world.getBlockState(pos).isSideSolid(world, pos, Direction.UP, SideShapeType.RIGID);
    }

    public static boolean sideCoversSmallSquare(WorldView world, BlockPos pos, Direction side) {
        BlockState lv = world.getBlockState(pos);
        if (side == Direction.DOWN && lv.isIn(BlockTags.UNSTABLE_BOTTOM_CENTER)) {
            return false;
        }
        return lv.isSideSolid(world, pos, side, SideShapeType.CENTER);
    }

    public static boolean isFaceFullSquare(VoxelShape shape, Direction side) {
        VoxelShape lv = shape.getFace(side);
        return Block.isShapeFullCube(lv);
    }

    public static boolean isShapeFullCube(VoxelShape shape) {
        return FULL_CUBE_SHAPE_CACHE.getUnchecked(shape);
    }

    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
    }

    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
    }

    public static List<ItemStack> getDroppedStacks(BlockState state, ServerWorld world, BlockPos pos, @Nullable BlockEntity blockEntity) {
        LootWorldContext.Builder lv = new LootWorldContext.Builder(world).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos)).add(LootContextParameters.TOOL, ItemStack.EMPTY).addOptional(LootContextParameters.BLOCK_ENTITY, blockEntity);
        return state.getDroppedStacks(lv);
    }

    public static List<ItemStack> getDroppedStacks(BlockState state, ServerWorld world, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack stack) {
        LootWorldContext.Builder lv = new LootWorldContext.Builder(world).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos)).add(LootContextParameters.TOOL, stack).addOptional(LootContextParameters.THIS_ENTITY, entity).addOptional(LootContextParameters.BLOCK_ENTITY, blockEntity);
        return state.getDroppedStacks(lv);
    }

    public static void dropStacks(BlockState state, World world, BlockPos pos) {
        if (world instanceof ServerWorld) {
            Block.getDroppedStacks(state, (ServerWorld)world, pos, null).forEach(stack -> Block.dropStack(world, pos, stack));
            state.onStacksDropped((ServerWorld)world, pos, ItemStack.EMPTY, true);
        }
    }

    public static void dropStacks(BlockState state, WorldAccess world, BlockPos pos, @Nullable BlockEntity blockEntity) {
        if (world instanceof ServerWorld) {
            Block.getDroppedStacks(state, (ServerWorld)world, pos, blockEntity).forEach(stack -> Block.dropStack((World)((ServerWorld)world), pos, stack));
            state.onStacksDropped((ServerWorld)world, pos, ItemStack.EMPTY, true);
        }
    }

    public static void dropStacks(BlockState state, World world, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack tool) {
        if (world instanceof ServerWorld) {
            Block.getDroppedStacks(state, (ServerWorld)world, pos, blockEntity, entity, tool).forEach(stack -> Block.dropStack(world, pos, stack));
            state.onStacksDropped((ServerWorld)world, pos, tool, true);
        }
    }

    public static void dropStack(World world, BlockPos pos, ItemStack stack) {
        double d = (double)EntityType.ITEM.getHeight() / 2.0;
        double e = (double)pos.getX() + 0.5 + MathHelper.nextDouble(world.random, -0.25, 0.25);
        double f = (double)pos.getY() + 0.5 + MathHelper.nextDouble(world.random, -0.25, 0.25) - d;
        double g = (double)pos.getZ() + 0.5 + MathHelper.nextDouble(world.random, -0.25, 0.25);
        Block.dropStack(world, () -> new ItemEntity(world, e, f, g, stack), stack);
    }

    public static void dropStack(World world, BlockPos pos, Direction direction, ItemStack stack) {
        int i = direction.getOffsetX();
        int j = direction.getOffsetY();
        int k = direction.getOffsetZ();
        double d = (double)EntityType.ITEM.getWidth() / 2.0;
        double e = (double)EntityType.ITEM.getHeight() / 2.0;
        double f = (double)pos.getX() + 0.5 + (i == 0 ? MathHelper.nextDouble(world.random, -0.25, 0.25) : (double)i * (0.5 + d));
        double g = (double)pos.getY() + 0.5 + (j == 0 ? MathHelper.nextDouble(world.random, -0.25, 0.25) : (double)j * (0.5 + e)) - e;
        double h = (double)pos.getZ() + 0.5 + (k == 0 ? MathHelper.nextDouble(world.random, -0.25, 0.25) : (double)k * (0.5 + d));
        double l = i == 0 ? MathHelper.nextDouble(world.random, -0.1, 0.1) : (double)i * 0.1;
        double m = j == 0 ? MathHelper.nextDouble(world.random, 0.0, 0.1) : (double)j * 0.1 + 0.1;
        double n = k == 0 ? MathHelper.nextDouble(world.random, -0.1, 0.1) : (double)k * 0.1;
        Block.dropStack(world, () -> new ItemEntity(world, f, g, h, stack, l, m, n), stack);
    }

    private static void dropStack(World world, Supplier<ItemEntity> itemEntitySupplier, ItemStack stack) {
        block3: {
            block2: {
                if (!(world instanceof ServerWorld)) break block2;
                ServerWorld lv = (ServerWorld)world;
                if (!stack.isEmpty() && lv.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) break block3;
            }
            return;
        }
        ItemEntity lv2 = itemEntitySupplier.get();
        lv2.setToDefaultPickupDelay();
        world.spawnEntity(lv2);
    }

    protected void dropExperience(ServerWorld world, BlockPos pos, int size) {
        if (world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
            ExperienceOrbEntity.spawn(world, Vec3d.ofCenter(pos), size);
        }
    }

    public float getBlastResistance() {
        return this.resistance;
    }

    public void onDestroyedByExplosion(ServerWorld world, BlockPos pos, Explosion explosion) {
    }

    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState();
    }

    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        player.incrementStat(Stats.MINED.getOrCreateStat(this));
        player.addExhaustion(0.005f);
        Block.dropStacks(state, world, pos, blockEntity, player, tool);
    }

    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
    }

    public boolean canMobSpawnInside(BlockState state) {
        return !state.isSolid() && !state.isLiquid();
    }

    public MutableText getName() {
        return Text.translatable(this.getTranslationKey());
    }

    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        entity.handleFallDamage(fallDistance, 1.0f, entity.getDamageSources().fall());
    }

    public void onEntityLand(BlockView world, Entity entity) {
        entity.setVelocity(entity.getVelocity().multiply(1.0, 0.0, 1.0));
    }

    public float getSlipperiness() {
        return this.slipperiness;
    }

    public float getVelocityMultiplier() {
        return this.velocityMultiplier;
    }

    public float getJumpVelocityMultiplier() {
        return this.jumpVelocityMultiplier;
    }

    protected void spawnBreakParticles(World world, PlayerEntity player, BlockPos pos, BlockState state) {
        world.syncWorldEvent(player, WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
    }

    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        this.spawnBreakParticles(world, player, pos, state);
        if (state.isIn(BlockTags.GUARDED_BY_PIGLINS) && world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            PiglinBrain.onGuardedBlockInteracted(lv, player, false);
        }
        world.emitGameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Emitter.of(player, state));
        return state;
    }

    public void precipitationTick(BlockState state, World world, BlockPos pos, Biome.Precipitation precipitation) {
    }

    public boolean shouldDropItemsOnExplosion(Explosion explosion) {
        return true;
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
    }

    public StateManager<Block, BlockState> getStateManager() {
        return this.stateManager;
    }

    protected final void setDefaultState(BlockState state) {
        this.defaultState = state;
    }

    public final BlockState getDefaultState() {
        return this.defaultState;
    }

    public final BlockState getStateWithProperties(BlockState state) {
        BlockState lv = this.getDefaultState();
        for (Property<?> lv2 : state.getBlock().getStateManager().getProperties()) {
            if (!lv.contains(lv2)) continue;
            lv = Block.copyProperty(state, lv, lv2);
        }
        return lv;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState source, BlockState target, Property<T> property) {
        return (BlockState)target.with(property, source.get(property));
    }

    @Override
    public Item asItem() {
        if (this.cachedItem == null) {
            this.cachedItem = Item.fromBlock(this);
        }
        return this.cachedItem;
    }

    public boolean hasDynamicBounds() {
        return this.dynamicBounds;
    }

    public String toString() {
        return "Block{" + Registries.BLOCK.getEntry(this).getIdAsString() + "}";
    }

    @Override
    protected Block asBlock() {
        return this;
    }

    protected Function<BlockState, VoxelShape> createShapeFunction(Function<BlockState, VoxelShape> stateToShape) {
        return this.stateManager.getStates().stream().collect(ImmutableMap.toImmutableMap(Function.identity(), stateToShape))::get;
    }

    protected Function<BlockState, VoxelShape> createShapeFunction(Function<BlockState, VoxelShape> stateToShape, Property<?> ... properties) {
        Map<Property, Object> map = Arrays.stream(properties).collect(Collectors.toMap(property -> property, property -> property.getValues().getFirst()));
        ImmutableMap immutableMap = this.stateManager.getStates().stream().filter(state -> map.entrySet().stream().allMatch(entry -> state.get((Property)entry.getKey()) == entry.getValue())).collect(ImmutableMap.toImmutableMap(Function.identity(), stateToShape));
        return state -> {
            for (Map.Entry entry : map.entrySet()) {
                state = Block.applyValueToState(state, (Property)entry.getKey(), entry.getValue());
            }
            return (VoxelShape)immutableMap.get(state);
        };
    }

    private static <S extends State<?, S>, T extends Comparable<T>> S applyValueToState(S state, Property<T> property, Object value) {
        return (S)((State)state.with(property, (Comparable)((Comparable)value)));
    }

    @Deprecated
    public RegistryEntry.Reference<Block> getRegistryEntry() {
        return this.registryEntry;
    }

    protected void dropExperienceWhenMined(ServerWorld world, BlockPos pos, ItemStack tool, IntProvider experience) {
        int i = EnchantmentHelper.getBlockExperience(world, tool, experience.get(world.getRandom()));
        if (i > 0) {
            this.dropExperience(world, pos, i);
        }
    }

    record VoxelShapePair(VoxelShape first, VoxelShape second) {
        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof VoxelShapePair)) return false;
            VoxelShapePair lv = (VoxelShapePair)o;
            if (this.first != lv.first) return false;
            if (this.second != lv.second) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this.first) * 31 + System.identityHashCode(this.second);
        }
    }
}

