/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraft.world.debug.DebugSubscriptionTypes;
import net.minecraft.world.debug.DebugTrackable;
import net.minecraft.world.debug.data.StructureDebugData;
import net.minecraft.world.event.listener.GameEventDispatcher;
import net.minecraft.world.event.listener.GameEventListener;
import net.minecraft.world.event.listener.SimpleGameEventDispatcher;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import net.minecraft.world.tick.BasicTickScheduler;
import net.minecraft.world.tick.ChunkTickScheduler;
import net.minecraft.world.tick.WorldTickScheduler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class WorldChunk
extends Chunk
implements DebugTrackable {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final BlockEntityTickInvoker EMPTY_BLOCK_ENTITY_TICKER = new BlockEntityTickInvoker(){

        @Override
        public void tick() {
        }

        @Override
        public boolean isRemoved() {
            return true;
        }

        @Override
        public BlockPos getPos() {
            return BlockPos.ORIGIN;
        }

        @Override
        public String getName() {
            return "<null>";
        }
    };
    private final Map<BlockPos, WrappedBlockEntityTickInvoker> blockEntityTickers = Maps.newHashMap();
    private boolean loadedToWorld;
    final World world;
    @Nullable
    private Supplier<ChunkLevelType> levelTypeProvider;
    @Nullable
    private EntityLoader entityLoader;
    private final Int2ObjectMap<GameEventDispatcher> gameEventDispatchers;
    private final ChunkTickScheduler<Block> blockTickScheduler;
    private final ChunkTickScheduler<Fluid> fluidTickScheduler;
    private UnsavedListener unsavedListener = pos -> {};

    public WorldChunk(World world, ChunkPos pos) {
        this(world, pos, UpgradeData.NO_UPGRADE_DATA, new ChunkTickScheduler<Block>(), new ChunkTickScheduler<Fluid>(), 0L, null, null, null);
    }

    public WorldChunk(World world, ChunkPos pos2, UpgradeData upgradeData, ChunkTickScheduler<Block> blockTickScheduler, ChunkTickScheduler<Fluid> fluidTickScheduler, long inhabitedTime, @Nullable ChunkSection[] sectionArrayInitializer, @Nullable EntityLoader entityLoader, @Nullable BlendingData blendingData) {
        super(pos2, upgradeData, world, world.getPalettesFactory(), inhabitedTime, sectionArrayInitializer, blendingData);
        this.world = world;
        this.gameEventDispatchers = new Int2ObjectOpenHashMap<GameEventDispatcher>();
        for (Heightmap.Type lv : Heightmap.Type.values()) {
            if (!ChunkStatus.FULL.getHeightmapTypes().contains(lv)) continue;
            this.heightmaps.put(lv, new Heightmap(this, lv));
        }
        this.entityLoader = entityLoader;
        this.blockTickScheduler = blockTickScheduler;
        this.fluidTickScheduler = fluidTickScheduler;
    }

    public WorldChunk(ServerWorld world, ProtoChunk protoChunk, @Nullable EntityLoader entityLoader) {
        this(world, protoChunk.getPos(), protoChunk.getUpgradeData(), protoChunk.getBlockProtoTickScheduler(), protoChunk.getFluidProtoTickScheduler(), protoChunk.getInhabitedTime(), protoChunk.getSectionArray(), entityLoader, protoChunk.getBlendingData());
        if (!Collections.disjoint(protoChunk.blockEntityNbts.keySet(), protoChunk.blockEntities.keySet())) {
            LOGGER.error("Chunk at {} contains duplicated block entities", (Object)protoChunk.getPos());
        }
        for (BlockEntity lv : protoChunk.getBlockEntities().values()) {
            this.setBlockEntity(lv);
        }
        this.blockEntityNbts.putAll(protoChunk.getBlockEntityNbts());
        for (int i = 0; i < protoChunk.getPostProcessingLists().length; ++i) {
            this.postProcessingLists[i] = protoChunk.getPostProcessingLists()[i];
        }
        this.setStructureStarts(protoChunk.getStructureStarts());
        this.setStructureReferences(protoChunk.getStructureReferences());
        for (Map.Entry<Heightmap.Type, Heightmap> entry : protoChunk.getHeightmaps()) {
            if (!ChunkStatus.FULL.getHeightmapTypes().contains(entry.getKey())) continue;
            this.setHeightmap(entry.getKey(), entry.getValue().asLongArray());
        }
        this.chunkSkyLight = protoChunk.chunkSkyLight;
        this.setLightOn(protoChunk.isLightOn());
        this.markNeedsSaving();
    }

    public void setUnsavedListener(UnsavedListener unsavedListener) {
        this.unsavedListener = unsavedListener;
        if (this.needsSaving()) {
            unsavedListener.setUnsaved(this.pos);
        }
    }

    @Override
    public void markNeedsSaving() {
        boolean bl = this.needsSaving();
        super.markNeedsSaving();
        if (!bl) {
            this.unsavedListener.setUnsaved(this.pos);
        }
    }

    @Override
    public BasicTickScheduler<Block> getBlockTickScheduler() {
        return this.blockTickScheduler;
    }

    @Override
    public BasicTickScheduler<Fluid> getFluidTickScheduler() {
        return this.fluidTickScheduler;
    }

    @Override
    public Chunk.TickSchedulers getTickSchedulers(long time) {
        return new Chunk.TickSchedulers(this.blockTickScheduler.collectTicks(time), this.fluidTickScheduler.collectTicks(time));
    }

    @Override
    public GameEventDispatcher getGameEventDispatcher(int ySectionCoord) {
        World world = this.world;
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            return this.gameEventDispatchers.computeIfAbsent(ySectionCoord, sectionCoord -> new SimpleGameEventDispatcher(lv, ySectionCoord, this::removeGameEventDispatcher));
        }
        return super.getGameEventDispatcher(ySectionCoord);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        if (this.world.isDebugWorld()) {
            BlockState lv = null;
            if (j == 60) {
                lv = Blocks.BARRIER.getDefaultState();
            }
            if (j == 70) {
                lv = DebugChunkGenerator.getBlockState(i, k);
            }
            return lv == null ? Blocks.AIR.getDefaultState() : lv;
        }
        try {
            ChunkSection lv2;
            int l = this.getSectionIndex(j);
            if (l >= 0 && l < this.sectionArray.length && !(lv2 = this.sectionArray[l]).isEmpty()) {
                return lv2.getBlockState(i & 0xF, j & 0xF, k & 0xF);
            }
            return Blocks.AIR.getDefaultState();
        } catch (Throwable throwable) {
            CrashReport lv3 = CrashReport.create(throwable, "Getting block state");
            CrashReportSection lv4 = lv3.addElement("Block being got");
            lv4.add("Location", () -> CrashReportSection.createPositionString((HeightLimitView)this, i, j, k));
            throw new CrashException(lv3);
        }
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return this.getFluidState(pos.getX(), pos.getY(), pos.getZ());
    }

    public FluidState getFluidState(int x, int y, int z) {
        try {
            ChunkSection lv;
            int l = this.getSectionIndex(y);
            if (l >= 0 && l < this.sectionArray.length && !(lv = this.sectionArray[l]).isEmpty()) {
                return lv.getFluidState(x & 0xF, y & 0xF, z & 0xF);
            }
            return Fluids.EMPTY.getDefaultState();
        } catch (Throwable throwable) {
            CrashReport lv2 = CrashReport.create(throwable, "Getting fluid state");
            CrashReportSection lv3 = lv2.addElement("Block being got");
            lv3.add("Location", () -> CrashReportSection.createPositionString((HeightLimitView)this, x, y, z));
            throw new CrashException(lv2);
        }
    }

    @Override
    @Nullable
    public BlockState setBlockState(BlockPos pos, BlockState state, int flags) {
        World world;
        BlockEntity lv5;
        boolean bl5;
        int m;
        int l;
        int j = pos.getY();
        ChunkSection lv = this.getSection(this.getSectionIndex(j));
        boolean bl = lv.isEmpty();
        if (bl && state.isAir()) {
            return null;
        }
        int k = pos.getX() & 0xF;
        BlockState lv2 = lv.setBlockState(k, l = j & 0xF, m = pos.getZ() & 0xF, state);
        if (lv2 == state) {
            return null;
        }
        Block lv3 = state.getBlock();
        ((Heightmap)this.heightmaps.get(Heightmap.Type.MOTION_BLOCKING)).trackUpdate(k, j, m, state);
        ((Heightmap)this.heightmaps.get(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES)).trackUpdate(k, j, m, state);
        ((Heightmap)this.heightmaps.get(Heightmap.Type.OCEAN_FLOOR)).trackUpdate(k, j, m, state);
        ((Heightmap)this.heightmaps.get(Heightmap.Type.WORLD_SURFACE)).trackUpdate(k, j, m, state);
        boolean bl2 = lv.isEmpty();
        if (bl != bl2) {
            this.world.getChunkManager().getLightingProvider().setSectionStatus(pos, bl2);
            this.world.getChunkManager().onSectionStatusChanged(this.pos.x, ChunkSectionPos.getSectionCoord(j), this.pos.z, bl2);
        }
        if (ChunkLightProvider.needsLightUpdate(lv2, state)) {
            Profiler lv4 = Profilers.get();
            lv4.push("updateSkyLightSources");
            this.chunkSkyLight.isSkyLightAccessible(this, k, j, m);
            lv4.swap("queueCheckLight");
            this.world.getChunkManager().getLightingProvider().checkBlock(pos);
            lv4.pop();
        }
        boolean bl3 = !lv2.isOf(lv3);
        boolean bl4 = (flags & Block.MOVED) != 0;
        boolean bl6 = bl5 = (flags & Block.SKIP_BLOCK_ENTITY_REPLACED_CALLBACK) == 0;
        if (bl3 && lv2.hasBlockEntity() && !state.keepBlockEntityWhenReplacedWith(lv2)) {
            if (!this.world.isClient() && bl5 && (lv5 = this.world.getBlockEntity(pos)) != null) {
                lv5.onBlockReplaced(pos, lv2);
            }
            this.removeBlockEntity(pos);
        }
        if ((bl3 || lv3 instanceof AbstractRailBlock) && (world = this.world) instanceof ServerWorld) {
            ServerWorld lv6 = (ServerWorld)world;
            if ((flags & Block.NOTIFY_NEIGHBORS) != 0 || bl4) {
                lv2.onStateReplaced(lv6, pos, bl4);
            }
        }
        if (!lv.getBlockState(k, l, m).isOf(lv3)) {
            return null;
        }
        if (!this.world.isClient() && (flags & Block.SKIP_BLOCK_ADDED_CALLBACK) == 0) {
            state.onBlockAdded(this.world, pos, lv2, bl4);
        }
        if (state.hasBlockEntity()) {
            lv5 = this.getBlockEntity(pos, CreationType.CHECK);
            if (lv5 != null && !lv5.supports(state)) {
                LOGGER.warn("Found mismatched block entity @ {}: type = {}, state = {}", pos, lv5.getType().getRegistryEntry().registryKey().getValue(), state);
                this.removeBlockEntity(pos);
                lv5 = null;
            }
            if (lv5 == null) {
                lv5 = ((BlockEntityProvider)((Object)lv3)).createBlockEntity(pos, state);
                if (lv5 != null) {
                    this.addBlockEntity(lv5);
                }
            } else {
                lv5.setCachedState(state);
                this.updateTicker(lv5);
            }
        }
        this.markNeedsSaving();
        return lv2;
    }

    @Override
    @Deprecated
    public void addEntity(Entity entity) {
    }

    @Nullable
    private BlockEntity createBlockEntity(BlockPos pos) {
        BlockState lv = this.getBlockState(pos);
        if (!lv.hasBlockEntity()) {
            return null;
        }
        return ((BlockEntityProvider)((Object)lv.getBlock())).createBlockEntity(pos, lv);
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        return this.getBlockEntity(pos, CreationType.CHECK);
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos, CreationType creationType) {
        BlockEntity lv3;
        NbtCompound lv2;
        BlockEntity lv = (BlockEntity)this.blockEntities.get(pos);
        if (lv == null && (lv2 = (NbtCompound)this.blockEntityNbts.remove(pos)) != null && (lv3 = this.loadBlockEntity(pos, lv2)) != null) {
            return lv3;
        }
        if (lv == null) {
            if (creationType == CreationType.IMMEDIATE && (lv = this.createBlockEntity(pos)) != null) {
                this.addBlockEntity(lv);
            }
        } else if (lv.isRemoved()) {
            this.blockEntities.remove(pos);
            return null;
        }
        return lv;
    }

    public void addBlockEntity(BlockEntity blockEntity) {
        this.setBlockEntity(blockEntity);
        if (this.canTickBlockEntities()) {
            World world = this.world;
            if (world instanceof ServerWorld) {
                ServerWorld lv = (ServerWorld)world;
                this.updateGameEventListener(blockEntity, lv);
            }
            this.world.loadBlockEntity(blockEntity);
            this.updateTicker(blockEntity);
        }
    }

    private boolean canTickBlockEntities() {
        return this.loadedToWorld || this.world.isClient();
    }

    boolean canTickBlockEntity(BlockPos pos) {
        if (!this.world.getWorldBorder().contains(pos)) {
            return false;
        }
        World world = this.world;
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            return this.getLevelType().isAfter(ChunkLevelType.BLOCK_TICKING) && lv.isChunkLoaded(ChunkPos.toLong(pos));
        }
        return true;
    }

    @Override
    public void setBlockEntity(BlockEntity blockEntity) {
        BlockPos lv = blockEntity.getPos();
        BlockState lv2 = this.getBlockState(lv);
        if (!lv2.hasBlockEntity()) {
            LOGGER.warn("Trying to set block entity {} at position {}, but state {} does not allow it", blockEntity, lv, lv2);
            return;
        }
        BlockState lv3 = blockEntity.getCachedState();
        if (lv2 != lv3) {
            if (!blockEntity.getType().supports(lv2)) {
                LOGGER.warn("Trying to set block entity {} at position {}, but state {} does not allow it", blockEntity, lv, lv2);
                return;
            }
            if (lv2.getBlock() != lv3.getBlock()) {
                LOGGER.warn("Block state mismatch on block entity {} in position {}, {} != {}, updating", blockEntity, lv, lv2, lv3);
            }
            blockEntity.setCachedState(lv2);
        }
        blockEntity.setWorld(this.world);
        blockEntity.cancelRemoval();
        BlockEntity lv4 = this.blockEntities.put(lv.toImmutable(), blockEntity);
        if (lv4 != null && lv4 != blockEntity) {
            lv4.markRemoved();
        }
    }

    @Override
    @Nullable
    public NbtCompound getPackedBlockEntityNbt(BlockPos pos, RegistryWrapper.WrapperLookup registries) {
        BlockEntity lv = this.getBlockEntity(pos);
        if (lv != null && !lv.isRemoved()) {
            NbtCompound lv2 = lv.createNbtWithIdentifyingData(this.world.getRegistryManager());
            lv2.putBoolean("keepPacked", false);
            return lv2;
        }
        NbtCompound lv2 = (NbtCompound)this.blockEntityNbts.get(pos);
        if (lv2 != null) {
            lv2 = lv2.copy();
            lv2.putBoolean("keepPacked", true);
        }
        return lv2;
    }

    @Override
    public void removeBlockEntity(BlockPos pos) {
        BlockEntity lv;
        if (this.canTickBlockEntities() && (lv = (BlockEntity)this.blockEntities.remove(pos)) != null) {
            World world = this.world;
            if (world instanceof ServerWorld) {
                ServerWorld lv2 = (ServerWorld)world;
                this.removeGameEventListener(lv, lv2);
                lv2.getSubscriptionTracker().untrackBlockEntity(pos);
            }
            lv.markRemoved();
        }
        this.removeBlockEntityTicker(pos);
    }

    private <T extends BlockEntity> void removeGameEventListener(T blockEntity, ServerWorld world) {
        GameEventListener lv2;
        Block lv = blockEntity.getCachedState().getBlock();
        if (lv instanceof BlockEntityProvider && (lv2 = ((BlockEntityProvider)((Object)lv)).getGameEventListener(world, blockEntity)) != null) {
            int i = ChunkSectionPos.getSectionCoord(blockEntity.getPos().getY());
            GameEventDispatcher lv3 = this.getGameEventDispatcher(i);
            lv3.removeListener(lv2);
        }
    }

    private void removeGameEventDispatcher(int ySectionCoord) {
        this.gameEventDispatchers.remove(ySectionCoord);
    }

    private void removeBlockEntityTicker(BlockPos pos) {
        WrappedBlockEntityTickInvoker lv = this.blockEntityTickers.remove(pos);
        if (lv != null) {
            lv.setWrapped(EMPTY_BLOCK_ENTITY_TICKER);
        }
    }

    public void loadEntities() {
        if (this.entityLoader != null) {
            this.entityLoader.run(this);
            this.entityLoader = null;
        }
    }

    public boolean isEmpty() {
        return false;
    }

    public void loadFromPacket(PacketByteBuf buf, Map<Heightmap.Type, long[]> heightmaps, Consumer<ChunkData.BlockEntityVisitor> blockEntityVisitorConsumer) {
        this.clear();
        for (ChunkSection lv : this.sectionArray) {
            lv.readDataPacket(buf);
        }
        heightmaps.forEach(this::setHeightmap);
        this.refreshSurfaceY();
        try (ErrorReporter.Logging lv2 = new ErrorReporter.Logging(this.getErrorReporterContext(), LOGGER);){
            blockEntityVisitorConsumer.accept((pos, blockEntityType, nbt) -> {
                BlockEntity lv = this.getBlockEntity(pos, CreationType.IMMEDIATE);
                if (lv != null && nbt != null && lv.getType() == blockEntityType) {
                    lv.read(NbtReadView.create(lv2.makeChild(lv.getReporterContext()), this.world.getRegistryManager(), nbt));
                }
            });
        }
    }

    public void loadBiomeFromPacket(PacketByteBuf buf) {
        for (ChunkSection lv : this.sectionArray) {
            lv.readBiomePacket(buf);
        }
    }

    public void setLoadedToWorld(boolean loadedToWorld) {
        this.loadedToWorld = loadedToWorld;
    }

    public World getWorld() {
        return this.world;
    }

    public Map<BlockPos, BlockEntity> getBlockEntities() {
        return this.blockEntities;
    }

    public void runPostProcessing(ServerWorld world) {
        ChunkPos lv = this.getPos();
        for (int i = 0; i < this.postProcessingLists.length; ++i) {
            if (this.postProcessingLists[i] == null) continue;
            for (Short short_ : this.postProcessingLists[i]) {
                BlockState lv5;
                BlockPos lv2 = ProtoChunk.joinBlockPos(short_, this.sectionIndexToCoord(i), lv);
                BlockState lv3 = this.getBlockState(lv2);
                FluidState lv4 = lv3.getFluidState();
                if (!lv4.isEmpty()) {
                    lv4.onScheduledTick(world, lv2, lv3);
                }
                if (lv3.getBlock() instanceof FluidBlock || (lv5 = Block.postProcessState(lv3, world, lv2)) == lv3) continue;
                world.setBlockState(lv2, lv5, Block.SKIP_REDRAW_AND_BLOCK_ENTITY_REPLACED_CALLBACK | Block.FORCE_STATE);
            }
            this.postProcessingLists[i].clear();
        }
        for (BlockPos lv6 : ImmutableList.copyOf(this.blockEntityNbts.keySet())) {
            this.getBlockEntity(lv6);
        }
        this.blockEntityNbts.clear();
        this.upgradeData.upgrade(this);
    }

    @Nullable
    private BlockEntity loadBlockEntity(BlockPos pos, NbtCompound nbt) {
        BlockEntity lv2;
        BlockState lv = this.getBlockState(pos);
        if ("DUMMY".equals(nbt.getString("id", ""))) {
            if (lv.hasBlockEntity()) {
                lv2 = ((BlockEntityProvider)((Object)lv.getBlock())).createBlockEntity(pos, lv);
            } else {
                lv2 = null;
                LOGGER.warn("Tried to load a DUMMY block entity @ {} but found not block entity block {} at location", (Object)pos, (Object)lv);
            }
        } else {
            lv2 = BlockEntity.createFromNbt(pos, lv, nbt, this.world.getRegistryManager());
        }
        if (lv2 != null) {
            lv2.setWorld(this.world);
            this.addBlockEntity(lv2);
        } else {
            LOGGER.warn("Tried to load a block entity for block {} but failed at location {}", (Object)lv, (Object)pos);
        }
        return lv2;
    }

    public void disableTickSchedulers(long time) {
        this.blockTickScheduler.disable(time);
        this.fluidTickScheduler.disable(time);
    }

    public void addChunkTickSchedulers(ServerWorld world) {
        ((WorldTickScheduler)world.getBlockTickScheduler()).addChunkTickScheduler(this.pos, this.blockTickScheduler);
        ((WorldTickScheduler)world.getFluidTickScheduler()).addChunkTickScheduler(this.pos, this.fluidTickScheduler);
    }

    public void removeChunkTickSchedulers(ServerWorld world) {
        ((WorldTickScheduler)world.getBlockTickScheduler()).removeChunkTickScheduler(this.pos);
        ((WorldTickScheduler)world.getFluidTickScheduler()).removeChunkTickScheduler(this.pos);
    }

    @Override
    public void registerTracking(ServerWorld world, DebugTrackable.Tracker tracker) {
        if (!this.getStructureStarts().isEmpty()) {
            tracker.track(DebugSubscriptionTypes.STRUCTURES, () -> {
                ArrayList<StructureDebugData> list = new ArrayList<StructureDebugData>();
                for (StructureStart lv : this.getStructureStarts().values()) {
                    BlockBox lv2 = lv.getBoundingBox();
                    List<StructurePiece> list2 = lv.getChildren();
                    ArrayList<StructureDebugData.Piece> list3 = new ArrayList<StructureDebugData.Piece>(list2.size());
                    for (int i = 0; i < list2.size(); ++i) {
                        boolean bl = i == 0;
                        list3.add(new StructureDebugData.Piece(list2.get(i).getBoundingBox(), bl));
                    }
                    list.add(new StructureDebugData(lv2, list3));
                }
                return list;
            });
        }
        tracker.track(DebugSubscriptionTypes.RAIDS, () -> world.getRaidManager().getRaidCenters(this.pos));
    }

    @Override
    public ChunkStatus getStatus() {
        return ChunkStatus.FULL;
    }

    public ChunkLevelType getLevelType() {
        if (this.levelTypeProvider == null) {
            return ChunkLevelType.FULL;
        }
        return this.levelTypeProvider.get();
    }

    public void setLevelTypeProvider(Supplier<ChunkLevelType> levelTypeProvider) {
        this.levelTypeProvider = levelTypeProvider;
    }

    public void clear() {
        this.blockEntities.values().forEach(BlockEntity::markRemoved);
        this.blockEntities.clear();
        this.blockEntityTickers.values().forEach(ticker -> ticker.setWrapped(EMPTY_BLOCK_ENTITY_TICKER));
        this.blockEntityTickers.clear();
    }

    public void updateAllBlockEntities() {
        this.blockEntities.values().forEach(blockEntity -> {
            World lv = this.world;
            if (lv instanceof ServerWorld) {
                ServerWorld lv2 = (ServerWorld)lv;
                this.updateGameEventListener(blockEntity, lv2);
            }
            this.world.loadBlockEntity((BlockEntity)blockEntity);
            this.updateTicker(blockEntity);
        });
    }

    private <T extends BlockEntity> void updateGameEventListener(T blockEntity, ServerWorld world) {
        GameEventListener lv2;
        Block lv = blockEntity.getCachedState().getBlock();
        if (lv instanceof BlockEntityProvider && (lv2 = ((BlockEntityProvider)((Object)lv)).getGameEventListener(world, blockEntity)) != null) {
            this.getGameEventDispatcher(ChunkSectionPos.getSectionCoord(blockEntity.getPos().getY())).addListener(lv2);
        }
    }

    private <T extends BlockEntity> void updateTicker(T blockEntity) {
        BlockState lv = blockEntity.getCachedState();
        BlockEntityTicker<?> lv2 = lv.getBlockEntityTicker(this.world, blockEntity.getType());
        if (lv2 == null) {
            this.removeBlockEntityTicker(blockEntity.getPos());
        } else {
            this.blockEntityTickers.compute(blockEntity.getPos(), (pos, ticker) -> {
                BlockEntityTickInvoker lv = this.wrapTicker(blockEntity, lv2);
                if (ticker != null) {
                    ticker.setWrapped(lv);
                    return ticker;
                }
                if (this.canTickBlockEntities()) {
                    WrappedBlockEntityTickInvoker lv2 = new WrappedBlockEntityTickInvoker(lv);
                    this.world.addBlockEntityTicker(lv2);
                    return lv2;
                }
                return null;
            });
        }
    }

    private <T extends BlockEntity> BlockEntityTickInvoker wrapTicker(T blockEntity, BlockEntityTicker<T> blockEntityTicker) {
        return new DirectBlockEntityTickInvoker(this, blockEntity, blockEntityTicker);
    }

    @FunctionalInterface
    public static interface EntityLoader {
        public void run(WorldChunk var1);
    }

    @FunctionalInterface
    public static interface UnsavedListener {
        public void setUnsaved(ChunkPos var1);
    }

    public static enum CreationType {
        IMMEDIATE,
        QUEUED,
        CHECK;

    }

    static class WrappedBlockEntityTickInvoker
    implements BlockEntityTickInvoker {
        private BlockEntityTickInvoker wrapped;

        WrappedBlockEntityTickInvoker(BlockEntityTickInvoker wrapped) {
            this.wrapped = wrapped;
        }

        void setWrapped(BlockEntityTickInvoker wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public void tick() {
            this.wrapped.tick();
        }

        @Override
        public boolean isRemoved() {
            return this.wrapped.isRemoved();
        }

        @Override
        public BlockPos getPos() {
            return this.wrapped.getPos();
        }

        @Override
        public String getName() {
            return this.wrapped.getName();
        }

        public String toString() {
            return String.valueOf(this.wrapped) + " <wrapped>";
        }
    }

    static class DirectBlockEntityTickInvoker<T extends BlockEntity>
    implements BlockEntityTickInvoker {
        private final T blockEntity;
        private final BlockEntityTicker<T> ticker;
        private boolean hasWarned;
        final /* synthetic */ WorldChunk worldChunk;

        DirectBlockEntityTickInvoker(T blockEntity, BlockEntityTicker<T> ticker) {
            this.worldChunk = arg;
            this.blockEntity = blockEntity;
            this.ticker = ticker;
        }

        @Override
        public void tick() {
            BlockPos lv;
            if (!((BlockEntity)this.blockEntity).isRemoved() && ((BlockEntity)this.blockEntity).hasWorld() && this.worldChunk.canTickBlockEntity(lv = ((BlockEntity)this.blockEntity).getPos())) {
                try {
                    Profiler lv2 = Profilers.get();
                    lv2.push(this::getName);
                    BlockState lv3 = this.worldChunk.getBlockState(lv);
                    if (((BlockEntity)this.blockEntity).getType().supports(lv3)) {
                        this.ticker.tick(this.worldChunk.world, ((BlockEntity)this.blockEntity).getPos(), lv3, this.blockEntity);
                        this.hasWarned = false;
                    } else if (!this.hasWarned) {
                        this.hasWarned = true;
                        LOGGER.warn("Block entity {} @ {} state {} invalid for ticking:", LogUtils.defer(this::getName), LogUtils.defer(this::getPos), lv3);
                    }
                    lv2.pop();
                } catch (Throwable throwable) {
                    CrashReport lv4 = CrashReport.create(throwable, "Ticking block entity");
                    CrashReportSection lv5 = lv4.addElement("Block entity being ticked");
                    ((BlockEntity)this.blockEntity).populateCrashReport(lv5);
                    throw new CrashException(lv4);
                }
            }
        }

        @Override
        public boolean isRemoved() {
            return ((BlockEntity)this.blockEntity).isRemoved();
        }

        @Override
        public BlockPos getPos() {
            return ((BlockEntity)this.blockEntity).getPos();
        }

        @Override
        public String getName() {
            return BlockEntityType.getId(((BlockEntity)this.blockEntity).getType()).toString();
        }

        public String toString() {
            return "Level ticker for " + this.getName() + "@" + String.valueOf(this.getPos());
        }
    }
}

