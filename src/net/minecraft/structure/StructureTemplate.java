/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.BitSetVoxelSet;
import net.minecraft.util.shape.VoxelSet;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class StructureTemplate {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String PALETTE_KEY = "palette";
    public static final String PALETTES_KEY = "palettes";
    public static final String ENTITIES_KEY = "entities";
    public static final String BLOCKS_KEY = "blocks";
    public static final String BLOCKS_POS_KEY = "pos";
    public static final String BLOCKS_STATE_KEY = "state";
    public static final String BLOCKS_NBT_KEY = "nbt";
    public static final String ENTITIES_POS_KEY = "pos";
    public static final String ENTITIES_BLOCK_POS_KEY = "blockPos";
    public static final String ENTITIES_NBT_KEY = "nbt";
    public static final String SIZE_KEY = "size";
    private final List<PalettedBlockInfoList> blockInfoLists = Lists.newArrayList();
    private final List<StructureEntityInfo> entities = Lists.newArrayList();
    private Vec3i size = Vec3i.ZERO;
    private String author = "?";

    public Vec3i getSize() {
        return this.size;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return this.author;
    }

    public void saveFromWorld(World world, BlockPos start, Vec3i dimensions, boolean includeEntities, List<Block> ignoredBlocks) {
        if (dimensions.getX() < 1 || dimensions.getY() < 1 || dimensions.getZ() < 1) {
            return;
        }
        BlockPos lv = start.add(dimensions).add(-1, -1, -1);
        ArrayList<StructureBlockInfo> list2 = Lists.newArrayList();
        ArrayList<StructureBlockInfo> list3 = Lists.newArrayList();
        ArrayList<StructureBlockInfo> list4 = Lists.newArrayList();
        BlockPos lv2 = new BlockPos(Math.min(start.getX(), lv.getX()), Math.min(start.getY(), lv.getY()), Math.min(start.getZ(), lv.getZ()));
        BlockPos lv3 = new BlockPos(Math.max(start.getX(), lv.getX()), Math.max(start.getY(), lv.getY()), Math.max(start.getZ(), lv.getZ()));
        this.size = dimensions;
        try (ErrorReporter.Logging lv4 = new ErrorReporter.Logging(LOGGER);){
            for (BlockPos lv5 : BlockPos.iterate(lv2, lv3)) {
                StructureBlockInfo lv10;
                BlockPos lv6 = lv5.subtract(lv2);
                BlockState lv7 = world.getBlockState(lv5);
                if (ignoredBlocks.stream().anyMatch(lv7::isOf)) continue;
                BlockEntity lv8 = world.getBlockEntity(lv5);
                if (lv8 != null) {
                    NbtWriteView lv9 = NbtWriteView.create(lv4, world.getRegistryManager());
                    lv8.writeDataWithId(lv9);
                    lv10 = new StructureBlockInfo(lv6, lv7, lv9.getNbt());
                } else {
                    lv10 = new StructureBlockInfo(lv6, lv7, null);
                }
                StructureTemplate.categorize(lv10, list2, list3, list4);
            }
            List<StructureBlockInfo> list5 = StructureTemplate.combineSorted(list2, list3, list4);
            this.blockInfoLists.clear();
            this.blockInfoLists.add(new PalettedBlockInfoList(list5));
            if (includeEntities) {
                this.addEntitiesFromWorld(world, lv2, lv3, lv4);
            } else {
                this.entities.clear();
            }
        }
    }

    private static void categorize(StructureBlockInfo blockInfo, List<StructureBlockInfo> fullBlocks, List<StructureBlockInfo> blocksWithNbt, List<StructureBlockInfo> otherBlocks) {
        if (blockInfo.nbt != null) {
            blocksWithNbt.add(blockInfo);
        } else if (!blockInfo.state.getBlock().hasDynamicBounds() && blockInfo.state.isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN)) {
            fullBlocks.add(blockInfo);
        } else {
            otherBlocks.add(blockInfo);
        }
    }

    private static List<StructureBlockInfo> combineSorted(List<StructureBlockInfo> fullBlocks, List<StructureBlockInfo> blocksWithNbt, List<StructureBlockInfo> otherBlocks) {
        Comparator<StructureBlockInfo> comparator = Comparator.comparingInt(blockInfo -> blockInfo.pos.getY()).thenComparingInt(blockInfo -> blockInfo.pos.getX()).thenComparingInt(blockInfo -> blockInfo.pos.getZ());
        fullBlocks.sort(comparator);
        otherBlocks.sort(comparator);
        blocksWithNbt.sort(comparator);
        ArrayList<StructureBlockInfo> list4 = Lists.newArrayList();
        list4.addAll(fullBlocks);
        list4.addAll(otherBlocks);
        list4.addAll(blocksWithNbt);
        return list4;
    }

    private void addEntitiesFromWorld(World world, BlockPos firstCorner, BlockPos secondCorner, ErrorReporter errorReporter) {
        List<Entity> list = world.getEntitiesByClass(Entity.class, Box.enclosing(firstCorner, secondCorner), entity -> !(entity instanceof PlayerEntity));
        this.entities.clear();
        for (Entity lv : list) {
            BlockPos lv5;
            Vec3d lv2 = new Vec3d(lv.getX() - (double)firstCorner.getX(), lv.getY() - (double)firstCorner.getY(), lv.getZ() - (double)firstCorner.getZ());
            NbtWriteView lv3 = NbtWriteView.create(errorReporter.makeChild(lv.getErrorReporterContext()), lv.getRegistryManager());
            lv.saveData(lv3);
            if (lv instanceof PaintingEntity) {
                PaintingEntity lv4 = (PaintingEntity)lv;
                lv5 = lv4.getAttachedBlockPos().subtract(firstCorner);
            } else {
                lv5 = BlockPos.ofFloored(lv2);
            }
            this.entities.add(new StructureEntityInfo(lv2, lv5, lv3.getNbt().copy()));
        }
    }

    public List<StructureBlockInfo> getInfosForBlock(BlockPos pos, StructurePlacementData placementData, Block block) {
        return this.getInfosForBlock(pos, placementData, block, true);
    }

    public List<JigsawBlockInfo> getJigsawInfos(BlockPos pos, BlockRotation rotation) {
        if (this.blockInfoLists.isEmpty()) {
            return new ArrayList<JigsawBlockInfo>();
        }
        StructurePlacementData lv = new StructurePlacementData().setRotation(rotation);
        List<JigsawBlockInfo> list = lv.getRandomBlockInfos(this.blockInfoLists, pos).getOrCreateJigsawBlockInfos();
        ArrayList<JigsawBlockInfo> list2 = new ArrayList<JigsawBlockInfo>(list.size());
        for (JigsawBlockInfo lv2 : list) {
            StructureBlockInfo lv3 = lv2.info;
            list2.add(lv2.withInfo(new StructureBlockInfo(StructureTemplate.transform(lv, lv3.pos()).add(pos), lv3.state.rotate(lv.getRotation()), lv3.nbt)));
        }
        return list2;
    }

    public ObjectArrayList<StructureBlockInfo> getInfosForBlock(BlockPos pos, StructurePlacementData placementData, Block block, boolean transformed) {
        ObjectArrayList<StructureBlockInfo> objectArrayList = new ObjectArrayList<StructureBlockInfo>();
        BlockBox lv = placementData.getBoundingBox();
        if (this.blockInfoLists.isEmpty()) {
            return objectArrayList;
        }
        for (StructureBlockInfo lv2 : placementData.getRandomBlockInfos(this.blockInfoLists, pos).getAllOf(block)) {
            BlockPos lv3;
            BlockPos blockPos = lv3 = transformed ? StructureTemplate.transform(placementData, lv2.pos).add(pos) : lv2.pos;
            if (lv != null && !lv.contains(lv3)) continue;
            objectArrayList.add(new StructureBlockInfo(lv3, lv2.state.rotate(placementData.getRotation()), lv2.nbt));
        }
        return objectArrayList;
    }

    public BlockPos transformBox(StructurePlacementData placementData1, BlockPos pos1, StructurePlacementData placementData2, BlockPos pos2) {
        BlockPos lv = StructureTemplate.transform(placementData1, pos1);
        BlockPos lv2 = StructureTemplate.transform(placementData2, pos2);
        return lv.subtract(lv2);
    }

    public static BlockPos transform(StructurePlacementData placementData, BlockPos pos) {
        return StructureTemplate.transformAround(pos, placementData.getMirror(), placementData.getRotation(), placementData.getPosition());
    }

    public boolean place(ServerWorldAccess world, BlockPos pos, BlockPos pivot, StructurePlacementData placementData, Random random, int flags) {
        if (this.blockInfoLists.isEmpty()) {
            return false;
        }
        List<StructureBlockInfo> list = placementData.getRandomBlockInfos(this.blockInfoLists, pos).getAll();
        if (list.isEmpty() && (placementData.shouldIgnoreEntities() || this.entities.isEmpty()) || this.size.getX() < 1 || this.size.getY() < 1 || this.size.getZ() < 1) {
            return false;
        }
        BlockBox lv = placementData.getBoundingBox();
        ArrayList<BlockPos> list2 = Lists.newArrayListWithCapacity(placementData.shouldApplyWaterlogging() ? list.size() : 0);
        ArrayList<BlockPos> list3 = Lists.newArrayListWithCapacity(placementData.shouldApplyWaterlogging() ? list.size() : 0);
        ArrayList<Pair<BlockPos, NbtCompound>> list4 = Lists.newArrayListWithCapacity(list.size());
        int j = Integer.MAX_VALUE;
        int k = Integer.MAX_VALUE;
        int l = Integer.MAX_VALUE;
        int m = Integer.MIN_VALUE;
        int n = Integer.MIN_VALUE;
        int o = Integer.MIN_VALUE;
        List<StructureBlockInfo> list5 = StructureTemplate.process(world, pos, pivot, placementData, list);
        try (ErrorReporter.Logging lv2 = new ErrorReporter.Logging(LOGGER);){
            for (StructureBlockInfo lv3 : list5) {
                BlockEntity lv7;
                BlockPos lv4 = lv3.pos;
                if (lv != null && !lv.contains(lv4)) continue;
                FluidState fluidState = placementData.shouldApplyWaterlogging() ? world.getFluidState(lv4) : null;
                BlockState lv6 = lv3.state.mirror(placementData.getMirror()).rotate(placementData.getRotation());
                if (lv3.nbt != null) {
                    world.setBlockState(lv4, Blocks.BARRIER.getDefaultState(), Block.FORCE_STATE_AND_SKIP_CALLBACKS_AND_DROPS | Block.SKIP_REDRAW_AND_BLOCK_ENTITY_REPLACED_CALLBACK);
                }
                if (!world.setBlockState(lv4, lv6, flags)) continue;
                j = Math.min(j, lv4.getX());
                k = Math.min(k, lv4.getY());
                l = Math.min(l, lv4.getZ());
                m = Math.max(m, lv4.getX());
                n = Math.max(n, lv4.getY());
                o = Math.max(o, lv4.getZ());
                list4.add(Pair.of(lv4, lv3.nbt));
                if (lv3.nbt != null && (lv7 = world.getBlockEntity(lv4)) != null) {
                    if (!SharedConstants.STRUCTURE_EDIT_MODE && lv7 instanceof LootableInventory) {
                        lv3.nbt.putLong("LootTableSeed", random.nextLong());
                    }
                    lv7.read(NbtReadView.create(lv2.makeChild(lv7.getReporterContext()), world.getRegistryManager(), lv3.nbt));
                }
                if (fluidState == null) continue;
                if (lv6.getFluidState().isStill()) {
                    list3.add(lv4);
                    continue;
                }
                if (!(lv6.getBlock() instanceof FluidFillable)) continue;
                ((FluidFillable)((Object)lv6.getBlock())).tryFillWithFluid(world, lv4, lv6, fluidState);
                if (fluidState.isStill()) continue;
                list2.add(lv4);
            }
            boolean bl = true;
            Direction[] lvs = new Direction[]{Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
            while (bl && !list2.isEmpty()) {
                bl = false;
                Iterator iterator = list2.iterator();
                while (iterator.hasNext()) {
                    BlockState lv12;
                    Object lv13;
                    BlockPos blockPos = (BlockPos)iterator.next();
                    FluidState lv9 = world.getFluidState(blockPos);
                    for (int p = 0; p < lvs.length && !lv9.isStill(); ++p) {
                        BlockPos lv10 = blockPos.offset(lvs[p]);
                        FluidState fluidState = world.getFluidState(lv10);
                        if (!fluidState.isStill() || list3.contains(lv10)) continue;
                        lv9 = fluidState;
                    }
                    if (!lv9.isStill() || !((lv13 = (lv12 = world.getBlockState(blockPos)).getBlock()) instanceof FluidFillable)) continue;
                    ((FluidFillable)lv13).tryFillWithFluid(world, blockPos, lv12, lv9);
                    bl = true;
                    iterator.remove();
                }
            }
            if (j <= m) {
                if (!placementData.shouldUpdateNeighbors()) {
                    BitSetVoxelSet lv14 = new BitSetVoxelSet(m - j + 1, n - k + 1, o - l + 1);
                    int n2 = j;
                    int r = k;
                    int p = l;
                    for (Pair pair : list4) {
                        BlockPos lv15 = (BlockPos)pair.getFirst();
                        ((VoxelSet)lv14).set(lv15.getX() - n2, lv15.getY() - r, lv15.getZ() - p);
                    }
                    StructureTemplate.updateCorner(world, flags, lv14, n2, r, p);
                }
                for (Pair pair : list4) {
                    BlockEntity lv7;
                    BlockPos lv16 = (BlockPos)pair.getFirst();
                    if (!placementData.shouldUpdateNeighbors()) {
                        BlockState lv17;
                        BlockState lv12 = world.getBlockState(lv16);
                        if (lv12 != (lv17 = Block.postProcessState(lv12, world, lv16))) {
                            world.setBlockState(lv16, lv17, flags & ~Block.NOTIFY_NEIGHBORS | Block.FORCE_STATE);
                        }
                        world.updateNeighbors(lv16, lv17.getBlock());
                    }
                    if (pair.getSecond() == null || (lv7 = world.getBlockEntity(lv16)) == null) continue;
                    lv7.markDirty();
                }
            }
            if (!placementData.shouldIgnoreEntities()) {
                this.spawnEntities(world, pos, placementData.getMirror(), placementData.getRotation(), placementData.getPosition(), lv, placementData.shouldInitializeMobs(), lv2);
            }
        }
        return true;
    }

    public static void updateCorner(WorldAccess world, int flags, VoxelSet set, BlockPos startPos) {
        StructureTemplate.updateCorner(world, flags, set, startPos.getX(), startPos.getY(), startPos.getZ());
    }

    public static void updateCorner(WorldAccess world, int flags, VoxelSet set, int startX, int startY, int startZ) {
        BlockPos.Mutable lv = new BlockPos.Mutable();
        BlockPos.Mutable lv2 = new BlockPos.Mutable();
        set.forEachDirection((direction, x, y, z) -> {
            BlockState lv4;
            lv.set(startX + x, startY + y, startZ + z);
            lv2.set((Vec3i)lv, direction);
            BlockState lv = world.getBlockState(lv);
            BlockState lv2 = world.getBlockState(lv2);
            BlockState lv3 = lv.getStateForNeighborUpdate(world, world, lv, direction, lv2, lv2, world.getRandom());
            if (lv != lv3) {
                world.setBlockState(lv, lv3, flags & ~Block.NOTIFY_NEIGHBORS);
            }
            if (lv2 != (lv4 = lv2.getStateForNeighborUpdate(world, world, lv2, direction.getOpposite(), lv, lv3, world.getRandom()))) {
                world.setBlockState(lv2, lv4, flags & ~Block.NOTIFY_NEIGHBORS);
            }
        });
    }

    public static List<StructureBlockInfo> process(ServerWorldAccess world, BlockPos pos, BlockPos pivot, StructurePlacementData placementData, List<StructureBlockInfo> infos) {
        ArrayList<StructureBlockInfo> list2 = new ArrayList<StructureBlockInfo>();
        List<StructureBlockInfo> list3 = new ArrayList<StructureBlockInfo>();
        for (StructureBlockInfo lv : infos) {
            BlockPos lv2 = StructureTemplate.transform(placementData, lv.pos).add(pos);
            StructureBlockInfo lv3 = new StructureBlockInfo(lv2, lv.state, lv.nbt != null ? lv.nbt.copy() : null);
            Iterator<StructureProcessor> iterator = placementData.getProcessors().iterator();
            while (lv3 != null && iterator.hasNext()) {
                lv3 = iterator.next().process(world, pos, pivot, lv, lv3, placementData);
            }
            if (lv3 == null) continue;
            list3.add(lv3);
            list2.add(lv);
        }
        for (StructureProcessor lv4 : placementData.getProcessors()) {
            list3 = lv4.reprocess(world, pos, pivot, list2, list3, placementData);
        }
        return list3;
    }

    private void spawnEntities(ServerWorldAccess world, BlockPos pos, BlockMirror mirror, BlockRotation rotation, BlockPos pivot, @Nullable BlockBox area, boolean initializeMobs, ErrorReporter errorReporter) {
        for (StructureEntityInfo lv : this.entities) {
            BlockPos lv2 = StructureTemplate.transformAround(lv.blockPos, mirror, rotation, pivot).add(pos);
            if (area != null && !area.contains(lv2)) continue;
            NbtCompound lv3 = lv.nbt.copy();
            Vec3d lv4 = StructureTemplate.transformAround(lv.pos, mirror, rotation, pivot);
            Vec3d lv5 = lv4.add(pos.getX(), pos.getY(), pos.getZ());
            NbtList lv6 = new NbtList();
            lv6.add(NbtDouble.of(lv5.x));
            lv6.add(NbtDouble.of(lv5.y));
            lv6.add(NbtDouble.of(lv5.z));
            lv3.put("Pos", lv6);
            lv3.remove("UUID");
            StructureTemplate.getEntity(errorReporter, world, lv3).ifPresent(entity -> {
                float f = entity.applyRotation(rotation);
                entity.refreshPositionAndAngles(arg3.x, arg3.y, arg3.z, f += entity.applyMirror(mirror) - entity.getYaw(), entity.getPitch());
                entity.setBodyYaw(f);
                entity.setHeadYaw(f);
                if (initializeMobs && entity instanceof MobEntity) {
                    MobEntity lv = (MobEntity)entity;
                    lv.initialize(world, world.getLocalDifficulty(BlockPos.ofFloored(lv5)), SpawnReason.STRUCTURE, null);
                }
                world.spawnEntityAndPassengers((Entity)entity);
            });
        }
    }

    private static Optional<Entity> getEntity(ErrorReporter errorReporter, ServerWorldAccess world, NbtCompound nbt) {
        try {
            return EntityType.getEntityFromData(NbtReadView.create(errorReporter, world.getRegistryManager(), nbt), world.toServerWorld(), SpawnReason.STRUCTURE);
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    public Vec3i getRotatedSize(BlockRotation rotation) {
        switch (rotation) {
            case COUNTERCLOCKWISE_90: 
            case CLOCKWISE_90: {
                return new Vec3i(this.size.getZ(), this.size.getY(), this.size.getX());
            }
        }
        return this.size;
    }

    public static BlockPos transformAround(BlockPos pos, BlockMirror mirror, BlockRotation rotation, BlockPos pivot) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        boolean bl = true;
        switch (mirror) {
            case LEFT_RIGHT: {
                k = -k;
                break;
            }
            case FRONT_BACK: {
                i = -i;
                break;
            }
            default: {
                bl = false;
            }
        }
        int l = pivot.getX();
        int m = pivot.getZ();
        switch (rotation) {
            case CLOCKWISE_180: {
                return new BlockPos(l + l - i, j, m + m - k);
            }
            case COUNTERCLOCKWISE_90: {
                return new BlockPos(l - m + k, j, l + m - i);
            }
            case CLOCKWISE_90: {
                return new BlockPos(l + m - k, j, m - l + i);
            }
        }
        return bl ? new BlockPos(i, j, k) : pos;
    }

    public static Vec3d transformAround(Vec3d point, BlockMirror mirror, BlockRotation rotation, BlockPos pivot) {
        double d = point.x;
        double e = point.y;
        double f = point.z;
        boolean bl = true;
        switch (mirror) {
            case LEFT_RIGHT: {
                f = 1.0 - f;
                break;
            }
            case FRONT_BACK: {
                d = 1.0 - d;
                break;
            }
            default: {
                bl = false;
            }
        }
        int i = pivot.getX();
        int j = pivot.getZ();
        switch (rotation) {
            case CLOCKWISE_180: {
                return new Vec3d((double)(i + i + 1) - d, e, (double)(j + j + 1) - f);
            }
            case COUNTERCLOCKWISE_90: {
                return new Vec3d((double)(i - j) + f, e, (double)(i + j + 1) - d);
            }
            case CLOCKWISE_90: {
                return new Vec3d((double)(i + j + 1) - f, e, (double)(j - i) + d);
            }
        }
        return bl ? new Vec3d(d, e, f) : point;
    }

    public BlockPos offsetByTransformedSize(BlockPos pos, BlockMirror mirror, BlockRotation rotation) {
        return StructureTemplate.applyTransformedOffset(pos, mirror, rotation, this.getSize().getX(), this.getSize().getZ());
    }

    public static BlockPos applyTransformedOffset(BlockPos pos, BlockMirror mirror, BlockRotation rotation, int offsetX, int offsetZ) {
        int k = mirror == BlockMirror.FRONT_BACK ? --offsetX : 0;
        int l = mirror == BlockMirror.LEFT_RIGHT ? --offsetZ : 0;
        BlockPos lv = pos;
        switch (rotation) {
            case NONE: {
                lv = pos.add(k, 0, l);
                break;
            }
            case CLOCKWISE_90: {
                lv = pos.add(offsetZ - l, 0, k);
                break;
            }
            case CLOCKWISE_180: {
                lv = pos.add(offsetX - k, 0, offsetZ - l);
                break;
            }
            case COUNTERCLOCKWISE_90: {
                lv = pos.add(l, 0, offsetX - k);
            }
        }
        return lv;
    }

    public BlockBox calculateBoundingBox(StructurePlacementData placementData, BlockPos pos) {
        return this.calculateBoundingBox(pos, placementData.getRotation(), placementData.getPosition(), placementData.getMirror());
    }

    public BlockBox calculateBoundingBox(BlockPos pos, BlockRotation rotation, BlockPos pivot, BlockMirror mirror) {
        return StructureTemplate.createBox(pos, rotation, pivot, mirror, this.size);
    }

    @VisibleForTesting
    protected static BlockBox createBox(BlockPos pos, BlockRotation rotation, BlockPos pivot, BlockMirror mirror, Vec3i dimensions) {
        Vec3i lv = dimensions.add(-1, -1, -1);
        BlockPos lv2 = StructureTemplate.transformAround(BlockPos.ORIGIN, mirror, rotation, pivot);
        BlockPos lv3 = StructureTemplate.transformAround(BlockPos.ORIGIN.add(lv), mirror, rotation, pivot);
        return BlockBox.create(lv2, lv3).move(pos);
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        if (this.blockInfoLists.isEmpty()) {
            nbt.put(BLOCKS_KEY, new NbtList());
            nbt.put(PALETTE_KEY, new NbtList());
        } else {
            ArrayList<Palette> list = Lists.newArrayList();
            Palette lv = new Palette();
            list.add(lv);
            for (int i = 1; i < this.blockInfoLists.size(); ++i) {
                list.add(new Palette());
            }
            NbtList lv2 = new NbtList();
            List<StructureBlockInfo> list2 = this.blockInfoLists.get(0).getAll();
            for (int j = 0; j < list2.size(); ++j) {
                StructureBlockInfo lv3 = list2.get(j);
                NbtCompound lv4 = new NbtCompound();
                lv4.put("pos", this.createNbtIntList(lv3.pos.getX(), lv3.pos.getY(), lv3.pos.getZ()));
                int k = lv.getId(lv3.state);
                lv4.putInt(BLOCKS_STATE_KEY, k);
                if (lv3.nbt != null) {
                    lv4.put("nbt", lv3.nbt);
                }
                lv2.add(lv4);
                for (int l = 1; l < this.blockInfoLists.size(); ++l) {
                    Palette lv5 = (Palette)list.get(l);
                    lv5.set(this.blockInfoLists.get((int)l).getAll().get((int)j).state, k);
                }
            }
            nbt.put(BLOCKS_KEY, lv2);
            if (list.size() == 1) {
                lv6 = new NbtList();
                for (BlockState lv7 : lv) {
                    lv6.add(NbtHelper.fromBlockState(lv7));
                }
                nbt.put(PALETTE_KEY, lv6);
            } else {
                lv6 = new NbtList();
                for (Palette lv8 : list) {
                    NbtList lv9 = new NbtList();
                    for (BlockState lv10 : lv8) {
                        lv9.add(NbtHelper.fromBlockState(lv10));
                    }
                    lv6.add(lv9);
                }
                nbt.put(PALETTES_KEY, lv6);
            }
        }
        NbtList lv11 = new NbtList();
        for (StructureEntityInfo lv12 : this.entities) {
            NbtCompound lv13 = new NbtCompound();
            lv13.put("pos", this.createNbtDoubleList(lv12.pos.x, lv12.pos.y, lv12.pos.z));
            lv13.put(ENTITIES_BLOCK_POS_KEY, this.createNbtIntList(lv12.blockPos.getX(), lv12.blockPos.getY(), lv12.blockPos.getZ()));
            if (lv12.nbt != null) {
                lv13.put("nbt", lv12.nbt);
            }
            lv11.add(lv13);
        }
        nbt.put(ENTITIES_KEY, lv11);
        nbt.put(SIZE_KEY, this.createNbtIntList(this.size.getX(), this.size.getY(), this.size.getZ()));
        return NbtHelper.putDataVersion(nbt);
    }

    public void readNbt(RegistryEntryLookup<Block> blockLookup, NbtCompound nbt) {
        this.blockInfoLists.clear();
        this.entities.clear();
        NbtList lv = nbt.getListOrEmpty(SIZE_KEY);
        this.size = new Vec3i(lv.getInt(0, 0), lv.getInt(1, 0), lv.getInt(2, 0));
        NbtList lv2 = nbt.getListOrEmpty(BLOCKS_KEY);
        Optional<NbtList> optional = nbt.getList(PALETTES_KEY);
        if (optional.isPresent()) {
            for (int i = 0; i < optional.get().size(); ++i) {
                this.loadPalettedBlockInfo(blockLookup, optional.get().getListOrEmpty(i), lv2);
            }
        } else {
            this.loadPalettedBlockInfo(blockLookup, nbt.getListOrEmpty(PALETTE_KEY), lv2);
        }
        nbt.getListOrEmpty(ENTITIES_KEY).streamCompounds().forEach(nbtx -> {
            NbtList lv = nbtx.getListOrEmpty("pos");
            Vec3d lv2 = new Vec3d(lv.getDouble(0, 0.0), lv.getDouble(1, 0.0), lv.getDouble(2, 0.0));
            NbtList lv3 = nbtx.getListOrEmpty(ENTITIES_BLOCK_POS_KEY);
            BlockPos lv4 = new BlockPos(lv3.getInt(0, 0), lv3.getInt(1, 0), lv3.getInt(2, 0));
            nbtx.getCompound("nbt").ifPresent(blockEntityNbt -> this.entities.add(new StructureEntityInfo(lv2, lv4, (NbtCompound)blockEntityNbt)));
        });
    }

    private void loadPalettedBlockInfo(RegistryEntryLookup<Block> blockLookup, NbtList palette, NbtList blocks) {
        Palette lv = new Palette();
        for (int i = 0; i < palette.size(); ++i) {
            lv.set(NbtHelper.toBlockState(blockLookup, palette.getCompoundOrEmpty(i)), i);
        }
        ArrayList<StructureBlockInfo> list = Lists.newArrayList();
        ArrayList<StructureBlockInfo> list2 = Lists.newArrayList();
        ArrayList<StructureBlockInfo> list3 = Lists.newArrayList();
        blocks.streamCompounds().forEach(nbt -> {
            NbtList lv = nbt.getListOrEmpty("pos");
            BlockPos lv2 = new BlockPos(lv.getInt(0, 0), lv.getInt(1, 0), lv.getInt(2, 0));
            BlockState lv3 = lv.getState(nbt.getInt(BLOCKS_STATE_KEY, 0));
            NbtCompound lv4 = nbt.getCompound("nbt").orElse(null);
            StructureBlockInfo lv5 = new StructureBlockInfo(lv2, lv3, lv4);
            StructureTemplate.categorize(lv5, list, list2, list3);
        });
        List<StructureBlockInfo> list4 = StructureTemplate.combineSorted(list, list2, list3);
        this.blockInfoLists.add(new PalettedBlockInfoList(list4));
    }

    private NbtList createNbtIntList(int ... ints) {
        NbtList lv = new NbtList();
        for (int i : ints) {
            lv.add(NbtInt.of(i));
        }
        return lv;
    }

    private NbtList createNbtDoubleList(double ... doubles) {
        NbtList lv = new NbtList();
        for (double d : doubles) {
            lv.add(NbtDouble.of(d));
        }
        return lv;
    }

    public static JigsawBlockEntity.Joint readJoint(NbtCompound nbt, BlockState state) {
        return nbt.get("joint", JigsawBlockEntity.Joint.CODEC).orElseGet(() -> StructureTemplate.getJointFromFacing(state));
    }

    public static JigsawBlockEntity.Joint getJointFromFacing(BlockState state) {
        return JigsawBlock.getFacing(state).getAxis().isHorizontal() ? JigsawBlockEntity.Joint.ALIGNED : JigsawBlockEntity.Joint.ROLLABLE;
    }

    public record StructureBlockInfo(BlockPos pos, BlockState state, @Nullable NbtCompound nbt) {
        @Override
        public String toString() {
            return String.format(Locale.ROOT, "<StructureBlockInfo | %s | %s | %s>", this.pos, this.state, this.nbt);
        }

        @Nullable
        public NbtCompound nbt() {
            return this.nbt;
        }
    }

    public static final class PalettedBlockInfoList {
        private final List<StructureBlockInfo> infos;
        private final Map<Block, List<StructureBlockInfo>> blockToInfos = Maps.newHashMap();
        @Nullable
        private List<JigsawBlockInfo> jigsawBlockInfos;

        PalettedBlockInfoList(List<StructureBlockInfo> infos) {
            this.infos = infos;
        }

        public List<JigsawBlockInfo> getOrCreateJigsawBlockInfos() {
            if (this.jigsawBlockInfos == null) {
                this.jigsawBlockInfos = this.getAllOf(Blocks.JIGSAW).stream().map(JigsawBlockInfo::of).toList();
            }
            return this.jigsawBlockInfos;
        }

        public List<StructureBlockInfo> getAll() {
            return this.infos;
        }

        public List<StructureBlockInfo> getAllOf(Block block) {
            return this.blockToInfos.computeIfAbsent(block, block2 -> this.infos.stream().filter(info -> info.state.isOf((Block)block2)).collect(Collectors.toList()));
        }
    }

    public static class StructureEntityInfo {
        public final Vec3d pos;
        public final BlockPos blockPos;
        public final NbtCompound nbt;

        public StructureEntityInfo(Vec3d pos, BlockPos blockPos, NbtCompound nbt) {
            this.pos = pos;
            this.blockPos = blockPos;
            this.nbt = nbt;
        }
    }

    public record JigsawBlockInfo(StructureBlockInfo info, JigsawBlockEntity.Joint jointType, Identifier name, RegistryKey<StructurePool> pool, Identifier target, int placementPriority, int selectionPriority) {
        public static JigsawBlockInfo of(StructureBlockInfo structureBlockInfo) {
            NbtCompound lv = Objects.requireNonNull(structureBlockInfo.nbt(), () -> String.valueOf(structureBlockInfo) + " nbt was null");
            return new JigsawBlockInfo(structureBlockInfo, StructureTemplate.readJoint(lv, structureBlockInfo.state()), lv.get("name", Identifier.CODEC).orElse(JigsawBlockEntity.DEFAULT_NAME), lv.get("pool", JigsawBlockEntity.STRUCTURE_POOL_KEY_CODEC).orElse(StructurePools.EMPTY), lv.get("target", Identifier.CODEC).orElse(JigsawBlockEntity.DEFAULT_NAME), lv.getInt("placement_priority", 0), lv.getInt("selection_priority", 0));
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "<JigsawBlockInfo | %s | %s | name: %s | pool: %s | target: %s | placement: %d | selection: %d | %s>", this.info.pos, this.info.state, this.name, this.pool.getValue(), this.target, this.placementPriority, this.selectionPriority, this.info.nbt);
        }

        public JigsawBlockInfo withInfo(StructureBlockInfo structureBlockInfo) {
            return new JigsawBlockInfo(structureBlockInfo, this.jointType, this.name, this.pool, this.target, this.placementPriority, this.selectionPriority);
        }
    }

    static class Palette
    implements Iterable<BlockState> {
        public static final BlockState AIR = Blocks.AIR.getDefaultState();
        private final IdList<BlockState> ids = new IdList(16);
        private int currentIndex;

        Palette() {
        }

        public int getId(BlockState state) {
            int i = this.ids.getRawId(state);
            if (i == -1) {
                i = this.currentIndex++;
                this.ids.set(state, i);
            }
            return i;
        }

        @Nullable
        public BlockState getState(int id) {
            BlockState lv = this.ids.get(id);
            return lv == null ? AIR : lv;
        }

        @Override
        public Iterator<BlockState> iterator() {
            return this.ids.iterator();
        }

        public void set(BlockState state, int id) {
            this.ids.set(state, id);
        }
    }
}

