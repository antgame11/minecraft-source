/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.HashSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.debug.DebugTrackable;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class BlockEntity
implements DebugTrackable {
    private static final Codec<BlockEntityType<?>> TYPE_CODEC = Registries.BLOCK_ENTITY_TYPE.getCodec();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final BlockEntityType<?> type;
    @Nullable
    protected World world;
    protected final BlockPos pos;
    protected boolean removed;
    private BlockState cachedState;
    private ComponentMap components = ComponentMap.EMPTY;

    public BlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this.type = type;
        this.pos = pos.toImmutable();
        this.validateSupports(state);
        this.cachedState = state;
    }

    private void validateSupports(BlockState state) {
        if (!this.supports(state)) {
            throw new IllegalStateException("Invalid block entity " + this.getNameForReport() + " state at " + String.valueOf(this.pos) + ", got " + String.valueOf(state));
        }
    }

    public boolean supports(BlockState state) {
        return this.type.supports(state);
    }

    public static BlockPos posFromNbt(ChunkPos chunkPos, NbtCompound nbt) {
        int i = nbt.getInt("x", 0);
        int j = nbt.getInt("y", 0);
        int k = nbt.getInt("z", 0);
        int l = ChunkSectionPos.getSectionCoord(i);
        int m = ChunkSectionPos.getSectionCoord(k);
        if (l != chunkPos.x || m != chunkPos.z) {
            LOGGER.warn("Block entity {} found in a wrong chunk, expected position from chunk {}", (Object)nbt, (Object)chunkPos);
            i = chunkPos.getOffsetX(ChunkSectionPos.getLocalCoord(i));
            k = chunkPos.getOffsetZ(ChunkSectionPos.getLocalCoord(k));
        }
        return new BlockPos(i, j, k);
    }

    @Nullable
    public World getWorld() {
        return this.world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public boolean hasWorld() {
        return this.world != null;
    }

    protected void readData(ReadView view) {
    }

    public final void read(ReadView view) {
        this.readData(view);
        this.components = view.read("components", ComponentMap.CODEC).orElse(ComponentMap.EMPTY);
    }

    public final void readComponentlessData(ReadView view) {
        this.readData(view);
    }

    protected void writeData(WriteView view) {
    }

    public final NbtCompound createNbtWithIdentifyingData(RegistryWrapper.WrapperLookup registries) {
        try (ErrorReporter.Logging lv = new ErrorReporter.Logging(this.getReporterContext(), LOGGER);){
            NbtWriteView lv2 = NbtWriteView.create(lv, registries);
            this.writeFullData(lv2);
            NbtCompound nbtCompound = lv2.getNbt();
            return nbtCompound;
        }
    }

    public void writeFullData(WriteView view) {
        this.writeDataWithoutId(view);
        this.writeIdentifyingData(view);
    }

    public void writeDataWithId(WriteView view) {
        this.writeDataWithoutId(view);
        this.writeId(view);
    }

    public final NbtCompound createNbt(RegistryWrapper.WrapperLookup registries) {
        try (ErrorReporter.Logging lv = new ErrorReporter.Logging(this.getReporterContext(), LOGGER);){
            NbtWriteView lv2 = NbtWriteView.create(lv, registries);
            this.writeDataWithoutId(lv2);
            NbtCompound nbtCompound = lv2.getNbt();
            return nbtCompound;
        }
    }

    public void writeDataWithoutId(WriteView data) {
        this.writeData(data);
        data.put("components", ComponentMap.CODEC, this.components);
    }

    public final NbtCompound createComponentlessNbt(RegistryWrapper.WrapperLookup registries) {
        try (ErrorReporter.Logging lv = new ErrorReporter.Logging(this.getReporterContext(), LOGGER);){
            NbtWriteView lv2 = NbtWriteView.create(lv, registries);
            this.writeComponentlessData(lv2);
            NbtCompound nbtCompound = lv2.getNbt();
            return nbtCompound;
        }
    }

    public void writeComponentlessData(WriteView view) {
        this.writeData(view);
    }

    private void writeId(WriteView view) {
        BlockEntity.writeId(view, this.getType());
    }

    public static void writeId(WriteView view, BlockEntityType<?> type) {
        view.put("id", TYPE_CODEC, type);
    }

    private void writeIdentifyingData(WriteView view) {
        this.writeId(view);
        view.putInt("x", this.pos.getX());
        view.putInt("y", this.pos.getY());
        view.putInt("z", this.pos.getZ());
    }

    @Nullable
    public static BlockEntity createFromNbt(BlockPos pos, BlockState state, NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        Object lv2;
        BlockEntityType lv = nbt.get("id", TYPE_CODEC).orElse(null);
        if (lv == null) {
            LOGGER.error("Skipping block entity with invalid type: {}", (Object)nbt.get("id"));
            return null;
        }
        try {
            lv2 = lv.instantiate(pos, state);
        } catch (Throwable throwable) {
            LOGGER.error("Failed to create block entity {} for block {} at position {} ", lv, pos, state, throwable);
            return null;
        }
        ErrorReporter.Logging lv3 = new ErrorReporter.Logging(((BlockEntity)lv2).getReporterContext(), LOGGER);
        try {
            ((BlockEntity)lv2).read(NbtReadView.create(lv3, registries, nbt));
            Object t = lv2;
            lv3.close();
            return t;
        } catch (Throwable throwable) {
            try {
                try {
                    lv3.close();
                } catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            } catch (Throwable throwable3) {
                LOGGER.error("Failed to load data for block entity {} for block {} at position {}", lv, pos, state, throwable3);
                return null;
            }
        }
    }

    public void markDirty() {
        if (this.world != null) {
            BlockEntity.markDirty(this.world, this.pos, this.cachedState);
        }
    }

    protected static void markDirty(World world, BlockPos pos, BlockState state) {
        world.markDirty(pos);
        if (!state.isAir()) {
            world.updateComparators(pos, state.getBlock());
        }
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public BlockState getCachedState() {
        return this.cachedState;
    }

    @Nullable
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return null;
    }

    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return new NbtCompound();
    }

    public boolean isRemoved() {
        return this.removed;
    }

    public void markRemoved() {
        this.removed = true;
    }

    public void cancelRemoval() {
        this.removed = false;
    }

    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        BlockEntity blockEntity = this;
        if (blockEntity instanceof Inventory) {
            Inventory lv = (Inventory)((Object)blockEntity);
            if (this.world != null) {
                ItemScatterer.spawn(this.world, pos, lv);
            }
        }
    }

    public boolean onSyncedBlockEvent(int type, int data) {
        return false;
    }

    public void populateCrashReport(CrashReportSection crashReportSection) {
        crashReportSection.add("Name", this::getNameForReport);
        crashReportSection.add("Cached block", this.getCachedState()::toString);
        if (this.world == null) {
            crashReportSection.add("Block location", () -> String.valueOf(this.pos) + " (world missing)");
        } else {
            crashReportSection.add("Actual block", this.world.getBlockState(this.pos)::toString);
            CrashReportSection.addBlockLocation(crashReportSection, this.world, this.pos);
        }
    }

    public String getNameForReport() {
        return String.valueOf(Registries.BLOCK_ENTITY_TYPE.getId(this.getType())) + " // " + this.getClass().getCanonicalName();
    }

    public BlockEntityType<?> getType() {
        return this.type;
    }

    @Deprecated
    public void setCachedState(BlockState state) {
        this.validateSupports(state);
        this.cachedState = state;
    }

    protected void readComponents(ComponentsAccess components) {
    }

    public final void readComponents(ItemStack stack) {
        this.readComponents(stack.getDefaultComponents(), stack.getComponentChanges());
    }

    public final void readComponents(ComponentMap defaultComponents, ComponentChanges components) {
        final HashSet<ComponentType<TooltipAppender>> set = new HashSet<ComponentType<TooltipAppender>>();
        set.add(DataComponentTypes.BLOCK_ENTITY_DATA);
        set.add(DataComponentTypes.BLOCK_STATE);
        final MergedComponentMap lv = MergedComponentMap.create(defaultComponents, components);
        this.readComponents(new ComponentsAccess(){

            @Override
            @Nullable
            public <T> T get(ComponentType<? extends T> type) {
                set.add(type);
                return lv.get(type);
            }

            @Override
            public <T> T getOrDefault(ComponentType<? extends T> type, T fallback) {
                set.add(type);
                return lv.getOrDefault(type, fallback);
            }
        });
        ComponentChanges lv2 = components.withRemovedIf(set::contains);
        this.components = lv2.toAddedRemovedPair().added();
    }

    protected void addComponents(ComponentMap.Builder builder) {
    }

    @Deprecated
    public void removeFromCopiedStackData(WriteView view) {
    }

    public final ComponentMap createComponentMap() {
        ComponentMap.Builder lv = ComponentMap.builder();
        lv.addAll(this.components);
        this.addComponents(lv);
        return lv.build();
    }

    public ComponentMap getComponents() {
        return this.components;
    }

    public void setComponents(ComponentMap components) {
        this.components = components;
    }

    @Nullable
    public static Text tryParseCustomName(ReadView view, String key) {
        return view.read(key, TextCodecs.CODEC).orElse(null);
    }

    public ErrorReporter.Context getReporterContext() {
        return new ReporterContext(this);
    }

    @Override
    public void registerTracking(ServerWorld world, DebugTrackable.Tracker tracker) {
    }

    record ReporterContext(BlockEntity blockEntity) implements ErrorReporter.Context
    {
        @Override
        public String getName() {
            return this.blockEntity.getNameForReport() + "@" + String.valueOf(this.blockEntity.getPos());
        }
    }
}

