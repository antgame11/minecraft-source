/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtShort;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.util.Nullables;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.BelowZeroRetrogen;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ChunkType;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.PalettesFactory;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadableContainer;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.gen.carver.CarvingMask;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.StorageKey;
import net.minecraft.world.tick.ChunkTickScheduler;
import net.minecraft.world.tick.SimpleTickScheduler;
import net.minecraft.world.tick.Tick;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public record SerializedChunk(PalettesFactory containerFactory, ChunkPos chunkPos, int minSectionY, long lastUpdateTime, long inhabitedTime, ChunkStatus chunkStatus, @Nullable BlendingData.Serialized blendingData, @Nullable BelowZeroRetrogen belowZeroRetrogen, UpgradeData upgradeData, @Nullable long[] carvingMask, Map<Heightmap.Type, long[]> heightmaps, Chunk.TickSchedulers packedTicks, ShortList[] postProcessingSections, boolean lightCorrect, List<SectionData> sectionData, List<NbtCompound> entities, List<NbtCompound> blockEntities, NbtCompound structureData) {
    private static final Codec<List<Tick<Block>>> BLOCK_TICKS_CODEC = Tick.createCodec(Registries.BLOCK.getCodec()).listOf();
    private static final Codec<List<Tick<Fluid>>> FLUID_TICKS_CODEC = Tick.createCodec(Registries.FLUID.getCodec()).listOf();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String UPGRADE_DATA_KEY = "UpgradeData";
    private static final String BLOCK_TICKS = "block_ticks";
    private static final String FLUID_TICKS = "fluid_ticks";
    public static final String X_POS_KEY = "xPos";
    public static final String Z_POS_KEY = "zPos";
    public static final String HEIGHTMAPS_KEY = "Heightmaps";
    public static final String IS_LIGHT_ON_KEY = "isLightOn";
    public static final String SECTIONS_KEY = "sections";
    public static final String BLOCK_LIGHT_KEY = "BlockLight";
    public static final String SKY_LIGHT_KEY = "SkyLight";

    @Nullable
    public static SerializedChunk fromNbt(HeightLimitView world, PalettesFactory palettesFactory, NbtCompound nbt) {
        if (nbt.getString("Status").isEmpty()) {
            return null;
        }
        ChunkPos lv = new ChunkPos(nbt.getInt(X_POS_KEY, 0), nbt.getInt(Z_POS_KEY, 0));
        long l = nbt.getLong("LastUpdate", 0L);
        long m = nbt.getLong("InhabitedTime", 0L);
        ChunkStatus lv2 = nbt.get("Status", ChunkStatus.CODEC).orElse(ChunkStatus.EMPTY);
        UpgradeData lv3 = nbt.getCompound(UPGRADE_DATA_KEY).map(upgradeData -> new UpgradeData((NbtCompound)upgradeData, world)).orElse(UpgradeData.NO_UPGRADE_DATA);
        boolean bl = nbt.getBoolean(IS_LIGHT_ON_KEY, false);
        BlendingData.Serialized lv4 = nbt.get("blending_data", BlendingData.Serialized.CODEC).orElse(null);
        BelowZeroRetrogen lv5 = nbt.get("below_zero_retrogen", BelowZeroRetrogen.CODEC).orElse(null);
        long[] ls = nbt.getLongArray("carving_mask").orElse(null);
        EnumMap<Heightmap.Type, long[]> map = new EnumMap<Heightmap.Type, long[]>(Heightmap.Type.class);
        nbt.getCompound(HEIGHTMAPS_KEY).ifPresent(heightmaps -> {
            for (Heightmap.Type lv : lv2.getHeightmapTypes()) {
                heightmaps.getLongArray(lv.getId()).ifPresent(heightmapType -> map.put(lv, (long[])heightmapType));
            }
        });
        List<Tick<Block>> list = Tick.filter(nbt.get(BLOCK_TICKS, BLOCK_TICKS_CODEC).orElse(List.of()), lv);
        List<Tick<Fluid>> list2 = Tick.filter(nbt.get(FLUID_TICKS, FLUID_TICKS_CODEC).orElse(List.of()), lv);
        Chunk.TickSchedulers lv6 = new Chunk.TickSchedulers(list, list2);
        NbtList lv7 = nbt.getListOrEmpty("PostProcessing");
        ShortList[] shortLists = new ShortList[lv7.size()];
        for (int i = 0; i < lv7.size(); ++i) {
            NbtList lv8 = lv7.getListOrEmpty(i);
            ShortArrayList shortList = new ShortArrayList(lv8.size());
            for (int j = 0; j < lv8.size(); ++j) {
                shortList.add(lv8.getShort(j, (short)0));
            }
            shortLists[i] = shortList;
        }
        List<NbtCompound> list3 = nbt.getList("entities").stream().flatMap(NbtList::streamCompounds).toList();
        List<NbtCompound> list4 = nbt.getList("block_entities").stream().flatMap(NbtList::streamCompounds).toList();
        NbtCompound lv9 = nbt.getCompoundOrEmpty("structures");
        NbtList lv10 = nbt.getListOrEmpty(SECTIONS_KEY);
        ArrayList<SectionData> list5 = new ArrayList<SectionData>(lv10.size());
        Codec<ReadableContainer<RegistryEntry<Biome>>> codec = palettesFactory.biomeContainerCodec();
        Codec<PalettedContainer<BlockState>> codec2 = palettesFactory.blockStatesContainerCodec();
        for (int k = 0; k < lv10.size(); ++k) {
            ChunkSection lv14;
            Optional<NbtCompound> optional = lv10.getCompound(k);
            if (optional.isEmpty()) continue;
            NbtCompound lv11 = optional.get();
            byte n = lv11.getByte("Y", (byte)0);
            if (n >= world.getBottomSectionCoord() && n <= world.getTopSectionCoord()) {
                PalettedContainer lv12 = lv11.getCompound("block_states").map(blockStates -> (PalettedContainer)codec2.parse(NbtOps.INSTANCE, blockStates).promotePartial(error -> SerializedChunk.logRecoverableError(lv, n, error)).getOrThrow(ChunkLoadingException::new)).orElseGet(palettesFactory::getBlockStateContainer);
                ReadableContainer lv13 = lv11.getCompound("biomes").map(biomes -> (ReadableContainer)codec.parse(NbtOps.INSTANCE, biomes).promotePartial(error -> SerializedChunk.logRecoverableError(lv, n, error)).getOrThrow(ChunkLoadingException::new)).orElseGet(palettesFactory::getBiomeContainer);
                lv14 = new ChunkSection(lv12, lv13);
            } else {
                lv14 = null;
            }
            ChunkNibbleArray lv15 = lv11.getByteArray(BLOCK_LIGHT_KEY).map(ChunkNibbleArray::new).orElse(null);
            ChunkNibbleArray lv16 = lv11.getByteArray(SKY_LIGHT_KEY).map(ChunkNibbleArray::new).orElse(null);
            list5.add(new SectionData(n, lv14, lv15, lv16));
        }
        return new SerializedChunk(palettesFactory, lv, world.getBottomSectionCoord(), l, m, lv2, lv4, lv5, lv3, ls, map, lv6, shortLists, bl, list5, list3, list4, lv9);
    }

    public ProtoChunk convert(ServerWorld world, PointOfInterestStorage poiStorage, StorageKey key, ChunkPos expectedPos) {
        Chunk lv9;
        if (!Objects.equals(expectedPos, this.chunkPos)) {
            LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", expectedPos, expectedPos, this.chunkPos);
            world.getServer().onChunkMisplacement(this.chunkPos, expectedPos, key);
        }
        int i = world.countVerticalSections();
        ChunkSection[] lvs = new ChunkSection[i];
        boolean bl = world.getDimension().hasSkyLight();
        ServerChunkManager lv = world.getChunkManager();
        LightingProvider lv2 = ((ChunkManager)lv).getLightingProvider();
        PalettesFactory lv3 = world.getPalettesFactory();
        boolean bl2 = false;
        for (SectionData lv4 : this.sectionData) {
            boolean bl4;
            ChunkSectionPos lv5 = ChunkSectionPos.from(expectedPos, lv4.y);
            if (lv4.chunkSection != null) {
                lvs[world.sectionCoordToIndex((int)lv4.y)] = lv4.chunkSection;
                poiStorage.initForPalette(lv5, lv4.chunkSection);
            }
            boolean bl3 = lv4.blockLight != null;
            boolean bl5 = bl4 = bl && lv4.skyLight != null;
            if (!bl3 && !bl4) continue;
            if (!bl2) {
                lv2.setRetainData(expectedPos, true);
                bl2 = true;
            }
            if (bl3) {
                lv2.enqueueSectionData(LightType.BLOCK, lv5, lv4.blockLight);
            }
            if (!bl4) continue;
            lv2.enqueueSectionData(LightType.SKY, lv5, lv4.skyLight);
        }
        ChunkType lv6 = this.chunkStatus.getChunkType();
        if (lv6 == ChunkType.LEVELCHUNK) {
            ChunkTickScheduler<Block> lv7 = new ChunkTickScheduler<Block>(this.packedTicks.blocks());
            ChunkTickScheduler<Fluid> lv8 = new ChunkTickScheduler<Fluid>(this.packedTicks.fluids());
            lv9 = new WorldChunk(world.toServerWorld(), expectedPos, this.upgradeData, lv7, lv8, this.inhabitedTime, lvs, SerializedChunk.getEntityLoadingCallback(world, this.entities, this.blockEntities), BlendingData.fromSerialized(this.blendingData));
        } else {
            SimpleTickScheduler<Block> lv10 = SimpleTickScheduler.tick(this.packedTicks.blocks());
            SimpleTickScheduler<Fluid> lv11 = SimpleTickScheduler.tick(this.packedTicks.fluids());
            ProtoChunk lv12 = new ProtoChunk(expectedPos, this.upgradeData, lvs, lv10, lv11, world, lv3, BlendingData.fromSerialized(this.blendingData));
            lv9 = lv12;
            lv9.setInhabitedTime(this.inhabitedTime);
            if (this.belowZeroRetrogen != null) {
                lv12.setBelowZeroRetrogen(this.belowZeroRetrogen);
            }
            lv12.setStatus(this.chunkStatus);
            if (this.chunkStatus.isAtLeast(ChunkStatus.INITIALIZE_LIGHT)) {
                lv12.setLightingProvider(lv2);
            }
        }
        lv9.setLightOn(this.lightCorrect);
        EnumSet<Heightmap.Type> enumSet = EnumSet.noneOf(Heightmap.Type.class);
        for (Heightmap.Type lv13 : lv9.getStatus().getHeightmapTypes()) {
            long[] ls = this.heightmaps.get(lv13);
            if (ls != null) {
                lv9.setHeightmap(lv13, ls);
                continue;
            }
            enumSet.add(lv13);
        }
        Heightmap.populateHeightmaps(lv9, enumSet);
        lv9.setStructureStarts(SerializedChunk.readStructureStarts(StructureContext.from(world), this.structureData, world.getSeed()));
        lv9.setStructureReferences(SerializedChunk.readStructureReferences(world.getRegistryManager(), expectedPos, this.structureData));
        for (int j = 0; j < this.postProcessingSections.length; ++j) {
            lv9.markBlocksForPostProcessing(this.postProcessingSections[j], j);
        }
        if (lv6 == ChunkType.LEVELCHUNK) {
            return new WrapperProtoChunk((WorldChunk)lv9, false);
        }
        ProtoChunk lv14 = (ProtoChunk)lv9;
        for (NbtCompound lv15 : this.entities) {
            lv14.addEntity(lv15);
        }
        for (NbtCompound lv15 : this.blockEntities) {
            lv14.addPendingBlockEntityNbt(lv15);
        }
        if (this.carvingMask != null) {
            lv14.setCarvingMask(new CarvingMask(this.carvingMask, lv9.getBottomY()));
        }
        return lv14;
    }

    private static void logRecoverableError(ChunkPos chunkPos, int y, String message) {
        LOGGER.error("Recoverable errors when loading section [{}, {}, {}]: {}", chunkPos.x, y, chunkPos.z, message);
    }

    public static SerializedChunk fromChunk(ServerWorld world, Chunk chunk) {
        if (!chunk.isSerializable()) {
            throw new IllegalArgumentException("Chunk can't be serialized: " + String.valueOf(chunk));
        }
        ChunkPos lv = chunk.getPos();
        ArrayList<SectionData> list = new ArrayList<SectionData>();
        ChunkSection[] lvs = chunk.getSectionArray();
        ServerLightingProvider lv2 = world.getChunkManager().getLightingProvider();
        for (int i = lv2.getBottomY(); i < lv2.getTopY(); ++i) {
            ChunkNibbleArray lv6;
            int j = chunk.sectionCoordToIndex(i);
            boolean bl = j >= 0 && j < lvs.length;
            ChunkNibbleArray lv3 = lv2.get(LightType.BLOCK).getLightSection(ChunkSectionPos.from(lv, i));
            ChunkNibbleArray lv4 = lv2.get(LightType.SKY).getLightSection(ChunkSectionPos.from(lv, i));
            ChunkNibbleArray chunkNibbleArray = lv3 != null && !lv3.isUninitialized() ? lv3.copy() : null;
            ChunkNibbleArray chunkNibbleArray2 = lv6 = lv4 != null && !lv4.isUninitialized() ? lv4.copy() : null;
            if (!bl && chunkNibbleArray == null && lv6 == null) continue;
            ChunkSection lv7 = bl ? lvs[j].copy() : null;
            list.add(new SectionData(i, lv7, chunkNibbleArray, lv6));
        }
        ArrayList<NbtCompound> list2 = new ArrayList<NbtCompound>(chunk.getBlockEntityPositions().size());
        for (BlockPos lv8 : chunk.getBlockEntityPositions()) {
            NbtCompound lv9 = chunk.getPackedBlockEntityNbt(lv8, world.getRegistryManager());
            if (lv9 == null) continue;
            list2.add(lv9);
        }
        ArrayList<NbtCompound> list3 = new ArrayList<NbtCompound>();
        long[] ls = null;
        if (chunk.getStatus().getChunkType() == ChunkType.PROTOCHUNK) {
            ProtoChunk lv10 = (ProtoChunk)chunk;
            list3.addAll(lv10.getEntities());
            CarvingMask lv11 = lv10.getCarvingMask();
            if (lv11 != null) {
                ls = lv11.getMask();
            }
        }
        EnumMap<Heightmap.Type, long[]> map = new EnumMap<Heightmap.Type, long[]>(Heightmap.Type.class);
        for (Map.Entry entry : chunk.getHeightmaps()) {
            if (!chunk.getStatus().getHeightmapTypes().contains(entry.getKey())) continue;
            long[] ms = ((Heightmap)entry.getValue()).asLongArray();
            map.put((Heightmap.Type)entry.getKey(), (long[])ms.clone());
        }
        Chunk.TickSchedulers lv12 = chunk.getTickSchedulers(world.getTime());
        ShortList[] shortListArray = (ShortList[])Arrays.stream(chunk.getPostProcessingLists()).map(postProcessings -> postProcessings != null ? new ShortArrayList((ShortList)postProcessings) : null).toArray(ShortList[]::new);
        NbtCompound lv13 = SerializedChunk.writeStructures(StructureContext.from(world), lv, chunk.getStructureStarts(), chunk.getStructureReferences());
        return new SerializedChunk(world.getPalettesFactory(), lv, chunk.getBottomSectionCoord(), world.getTime(), chunk.getInhabitedTime(), chunk.getStatus(), Nullables.map(chunk.getBlendingData(), BlendingData::toSerialized), chunk.getBelowZeroRetrogen(), chunk.getUpgradeData().copy(), ls, map, lv12, shortListArray, chunk.isLightOn(), list, list3, list2, lv13);
    }

    public NbtCompound serialize() {
        NbtCompound lv = NbtHelper.putDataVersion(new NbtCompound());
        lv.putInt(X_POS_KEY, this.chunkPos.x);
        lv.putInt("yPos", this.minSectionY);
        lv.putInt(Z_POS_KEY, this.chunkPos.z);
        lv.putLong("LastUpdate", this.lastUpdateTime);
        lv.putLong("InhabitedTime", this.inhabitedTime);
        lv.putString("Status", Registries.CHUNK_STATUS.getId(this.chunkStatus).toString());
        lv.putNullable("blending_data", BlendingData.Serialized.CODEC, this.blendingData);
        lv.putNullable("below_zero_retrogen", BelowZeroRetrogen.CODEC, this.belowZeroRetrogen);
        if (!this.upgradeData.isDone()) {
            lv.put(UPGRADE_DATA_KEY, this.upgradeData.toNbt());
        }
        NbtList lv2 = new NbtList();
        Codec<PalettedContainer<BlockState>> codec = this.containerFactory.blockStatesContainerCodec();
        Codec<ReadableContainer<RegistryEntry<Biome>>> codec2 = this.containerFactory.biomeContainerCodec();
        for (SectionData lv3 : this.sectionData) {
            NbtCompound lv4 = new NbtCompound();
            ChunkSection lv5 = lv3.chunkSection;
            if (lv5 != null) {
                lv4.put("block_states", codec, lv5.getBlockStateContainer());
                lv4.put("biomes", codec2, lv5.getBiomeContainer());
            }
            if (lv3.blockLight != null) {
                lv4.putByteArray(BLOCK_LIGHT_KEY, lv3.blockLight.asByteArray());
            }
            if (lv3.skyLight != null) {
                lv4.putByteArray(SKY_LIGHT_KEY, lv3.skyLight.asByteArray());
            }
            if (lv4.isEmpty()) continue;
            lv4.putByte("Y", (byte)lv3.y);
            lv2.add(lv4);
        }
        lv.put(SECTIONS_KEY, lv2);
        if (this.lightCorrect) {
            lv.putBoolean(IS_LIGHT_ON_KEY, true);
        }
        NbtList lv6 = new NbtList();
        lv6.addAll(this.blockEntities);
        lv.put("block_entities", lv6);
        if (this.chunkStatus.getChunkType() == ChunkType.PROTOCHUNK) {
            NbtList lv7 = new NbtList();
            lv7.addAll(this.entities);
            lv.put("entities", lv7);
            if (this.carvingMask != null) {
                lv.putLongArray("carving_mask", this.carvingMask);
            }
        }
        SerializedChunk.serializeTicks(lv, this.packedTicks);
        lv.put("PostProcessing", SerializedChunk.toNbt(this.postProcessingSections));
        NbtCompound lv8 = new NbtCompound();
        this.heightmaps.forEach((type, values) -> lv8.put(type.getId(), new NbtLongArray((long[])values)));
        lv.put(HEIGHTMAPS_KEY, lv8);
        lv.put("structures", this.structureData);
        return lv;
    }

    private static void serializeTicks(NbtCompound nbt, Chunk.TickSchedulers schedulers) {
        nbt.put(BLOCK_TICKS, BLOCK_TICKS_CODEC, schedulers.blocks());
        nbt.put(FLUID_TICKS, FLUID_TICKS_CODEC, schedulers.fluids());
    }

    public static ChunkStatus getChunkStatus(@Nullable NbtCompound nbt) {
        return nbt != null ? nbt.get("Status", ChunkStatus.CODEC).orElse(ChunkStatus.EMPTY) : ChunkStatus.EMPTY;
    }

    @Nullable
    private static WorldChunk.EntityLoader getEntityLoadingCallback(ServerWorld world, List<NbtCompound> entities, List<NbtCompound> blockEntities) {
        if (entities.isEmpty() && blockEntities.isEmpty()) {
            return null;
        }
        return chunk -> {
            if (!entities.isEmpty()) {
                try (ErrorReporter.Logging lv = new ErrorReporter.Logging(chunk.getErrorReporterContext(), LOGGER);){
                    world.loadEntities(EntityType.streamFromData(NbtReadView.createList(lv, world.getRegistryManager(), entities), world, SpawnReason.LOAD));
                }
            }
            for (NbtCompound lv2 : blockEntities) {
                boolean bl = lv2.getBoolean("keepPacked", false);
                if (bl) {
                    chunk.addPendingBlockEntityNbt(lv2);
                    continue;
                }
                BlockPos lv3 = BlockEntity.posFromNbt(chunk.getPos(), lv2);
                BlockEntity lv4 = BlockEntity.createFromNbt(lv3, chunk.getBlockState(lv3), lv2, world.getRegistryManager());
                if (lv4 == null) continue;
                chunk.setBlockEntity(lv4);
            }
        };
    }

    private static NbtCompound writeStructures(StructureContext context, ChunkPos pos, Map<Structure, StructureStart> starts, Map<Structure, LongSet> references) {
        NbtCompound lv = new NbtCompound();
        NbtCompound lv2 = new NbtCompound();
        RegistryWrapper.Impl lv3 = context.registryManager().getOrThrow(RegistryKeys.STRUCTURE);
        for (Map.Entry<Structure, StructureStart> entry : starts.entrySet()) {
            Identifier lv4 = lv3.getId(entry.getKey());
            lv2.put(lv4.toString(), entry.getValue().toNbt(context, pos));
        }
        lv.put("starts", lv2);
        NbtCompound lv5 = new NbtCompound();
        for (Map.Entry<Structure, LongSet> entry2 : references.entrySet()) {
            if (entry2.getValue().isEmpty()) continue;
            Identifier lv6 = lv3.getId(entry2.getKey());
            lv5.putLongArray(lv6.toString(), entry2.getValue().toLongArray());
        }
        lv.put("References", lv5);
        return lv;
    }

    private static Map<Structure, StructureStart> readStructureStarts(StructureContext context, NbtCompound nbt, long worldSeed) {
        HashMap<Structure, StructureStart> map = Maps.newHashMap();
        RegistryWrapper.Impl lv = context.registryManager().getOrThrow(RegistryKeys.STRUCTURE);
        NbtCompound lv2 = nbt.getCompoundOrEmpty("starts");
        for (String string : lv2.getKeys()) {
            Identifier lv3 = Identifier.tryParse(string);
            Structure lv4 = (Structure)lv.get(lv3);
            if (lv4 == null) {
                LOGGER.error("Unknown structure start: {}", (Object)lv3);
                continue;
            }
            StructureStart lv5 = StructureStart.fromNbt(context, lv2.getCompoundOrEmpty(string), worldSeed);
            if (lv5 == null) continue;
            map.put(lv4, lv5);
        }
        return map;
    }

    private static Map<Structure, LongSet> readStructureReferences(DynamicRegistryManager registryManager, ChunkPos pos, NbtCompound nbt) {
        HashMap<Structure, LongSet> map = Maps.newHashMap();
        RegistryWrapper.Impl lv = registryManager.getOrThrow(RegistryKeys.STRUCTURE);
        NbtCompound lv2 = nbt.getCompoundOrEmpty("References");
        lv2.forEach((id, chunkPos) -> {
            Identifier lv = Identifier.tryParse(id);
            Structure lv2 = (Structure)((Registry)lv).get(lv);
            if (lv2 == null) {
                LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", (Object)lv, (Object)pos);
                return;
            }
            Optional<long[]> optional = chunkPos.asLongArray();
            if (optional.isEmpty()) {
                return;
            }
            map.put(lv2, new LongOpenHashSet(Arrays.stream(optional.get()).filter(packedPos -> {
                ChunkPos lv = new ChunkPos(packedPos);
                if (lv.getChebyshevDistance(pos) > 8) {
                    LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", lv, lv, pos);
                    return false;
                }
                return true;
            }).toArray()));
        });
        return map;
    }

    private static NbtList toNbt(ShortList[] lists) {
        NbtList lv = new NbtList();
        for (ShortList shortList : lists) {
            NbtList lv2 = new NbtList();
            if (shortList != null) {
                for (int i = 0; i < shortList.size(); ++i) {
                    lv2.add(NbtShort.of(shortList.getShort(i)));
                }
            }
            lv.add(lv2);
        }
        return lv;
    }

    @Nullable
    public BlendingData.Serialized blendingData() {
        return this.blendingData;
    }

    @Nullable
    public BelowZeroRetrogen belowZeroRetrogen() {
        return this.belowZeroRetrogen;
    }

    @Nullable
    public long[] carvingMask() {
        return this.carvingMask;
    }

    public record SectionData(int y, @Nullable ChunkSection chunkSection, @Nullable ChunkNibbleArray blockLight, @Nullable ChunkNibbleArray skyLight) {
        @Nullable
        public ChunkSection chunkSection() {
            return this.chunkSection;
        }

        @Nullable
        public ChunkNibbleArray blockLight() {
            return this.blockLight;
        }

        @Nullable
        public ChunkNibbleArray skyLight() {
            return this.skyLight;
        }
    }

    public static class ChunkLoadingException
    extends NbtException {
        public ChunkLoadingException(String string) {
            super(string);
        }
    }
}

