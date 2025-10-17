/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.entity;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Objects;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BrushableBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class BrushableBlockEntity
extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String LOOT_TABLE_NBT_KEY = "LootTable";
    private static final String LOOT_TABLE_SEED_NBT_KEY = "LootTableSeed";
    private static final String HIT_DIRECTION_NBT_KEY = "hit_direction";
    private static final String ITEM_NBT_KEY = "item";
    private static final int field_42806 = 10;
    private static final int field_42807 = 40;
    private static final int field_42808 = 10;
    private int brushesCount;
    private long nextDustTime;
    private long nextBrushTime;
    private ItemStack item = ItemStack.EMPTY;
    @Nullable
    private Direction hitDirection;
    @Nullable
    private RegistryKey<LootTable> lootTable;
    private long lootTableSeed;

    public BrushableBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.BRUSHABLE_BLOCK, pos, state);
    }

    public boolean brush(long worldTime, ServerWorld world, LivingEntity brusher, Direction hitDirection, ItemStack brush) {
        if (this.hitDirection == null) {
            this.hitDirection = hitDirection;
        }
        this.nextDustTime = worldTime + 40L;
        if (worldTime < this.nextBrushTime) {
            return false;
        }
        this.nextBrushTime = worldTime + 10L;
        this.generateItem(world, brusher, brush);
        int i = this.getDustedLevel();
        if (++this.brushesCount >= 10) {
            this.finishBrushing(world, brusher, brush);
            return true;
        }
        world.scheduleBlockTick(this.getPos(), this.getCachedState().getBlock(), 2);
        int j = this.getDustedLevel();
        if (i != j) {
            BlockState lv = this.getCachedState();
            BlockState lv2 = (BlockState)lv.with(Properties.DUSTED, j);
            world.setBlockState(this.getPos(), lv2, Block.NOTIFY_ALL);
        }
        return false;
    }

    private void generateItem(ServerWorld world, LivingEntity brusher, ItemStack brush) {
        if (this.lootTable == null) {
            return;
        }
        LootTable lv = world.getServer().getReloadableRegistries().getLootTable(this.lootTable);
        if (brusher instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv2 = (ServerPlayerEntity)brusher;
            Criteria.PLAYER_GENERATES_CONTAINER_LOOT.trigger(lv2, this.lootTable);
        }
        LootWorldContext lv3 = new LootWorldContext.Builder(world).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(this.pos)).luck(brusher.getLuck()).add(LootContextParameters.THIS_ENTITY, brusher).add(LootContextParameters.TOOL, brush).build(LootContextTypes.ARCHAEOLOGY);
        ObjectArrayList<ItemStack> objectArrayList = lv.generateLoot(lv3, this.lootTableSeed);
        this.item = switch (objectArrayList.size()) {
            case 0 -> ItemStack.EMPTY;
            case 1 -> (ItemStack)objectArrayList.getFirst();
            default -> {
                LOGGER.warn("Expected max 1 loot from loot table {}, but got {}", (Object)this.lootTable.getValue(), (Object)objectArrayList.size());
                yield (ItemStack)objectArrayList.getFirst();
            }
        };
        this.lootTable = null;
        this.markDirty();
    }

    private void finishBrushing(ServerWorld world, LivingEntity brusher, ItemStack brush) {
        Block lv4;
        this.spawnItem(world, brusher, brush);
        BlockState lv = this.getCachedState();
        world.syncWorldEvent(WorldEvents.BLOCK_FINISHED_BRUSHING, this.getPos(), Block.getRawIdFromState(lv));
        Block lv2 = this.getCachedState().getBlock();
        if (lv2 instanceof BrushableBlock) {
            BrushableBlock lv3 = (BrushableBlock)lv2;
            lv4 = lv3.getBaseBlock();
        } else {
            lv4 = Blocks.AIR;
        }
        world.setBlockState(this.pos, lv4.getDefaultState(), Block.NOTIFY_ALL);
    }

    private void spawnItem(ServerWorld world, LivingEntity brusher, ItemStack brush) {
        this.generateItem(world, brusher, brush);
        if (!this.item.isEmpty()) {
            double d = EntityType.ITEM.getWidth();
            double e = 1.0 - d;
            double f = d / 2.0;
            Direction lv = Objects.requireNonNullElse(this.hitDirection, Direction.UP);
            BlockPos lv2 = this.pos.offset(lv, 1);
            double g = (double)lv2.getX() + 0.5 * e + f;
            double h = (double)lv2.getY() + 0.5 + (double)(EntityType.ITEM.getHeight() / 2.0f);
            double i = (double)lv2.getZ() + 0.5 * e + f;
            ItemEntity lv3 = new ItemEntity(world, g, h, i, this.item.split(world.random.nextInt(21) + 10));
            lv3.setVelocity(Vec3d.ZERO);
            world.spawnEntity(lv3);
            this.item = ItemStack.EMPTY;
        }
    }

    public void scheduledTick(ServerWorld world) {
        if (this.brushesCount != 0 && world.getTime() >= this.nextDustTime) {
            int i = this.getDustedLevel();
            this.brushesCount = Math.max(0, this.brushesCount - 2);
            int j = this.getDustedLevel();
            if (i != j) {
                world.setBlockState(this.getPos(), (BlockState)this.getCachedState().with(Properties.DUSTED, j), Block.NOTIFY_ALL);
            }
            int k = 4;
            this.nextDustTime = world.getTime() + 4L;
        }
        if (this.brushesCount == 0) {
            this.hitDirection = null;
            this.nextDustTime = 0L;
            this.nextBrushTime = 0L;
        } else {
            world.scheduleBlockTick(this.getPos(), this.getCachedState().getBlock(), 2);
        }
    }

    private boolean readLootTableFromData(ReadView view) {
        this.lootTable = view.read(LOOT_TABLE_NBT_KEY, LootTable.TABLE_KEY).orElse(null);
        this.lootTableSeed = view.getLong(LOOT_TABLE_SEED_NBT_KEY, 0L);
        return this.lootTable != null;
    }

    private boolean writeLootTableToData(WriteView view) {
        if (this.lootTable == null) {
            return false;
        }
        view.put(LOOT_TABLE_NBT_KEY, LootTable.TABLE_KEY, this.lootTable);
        if (this.lootTableSeed != 0L) {
            view.putLong(LOOT_TABLE_SEED_NBT_KEY, this.lootTableSeed);
        }
        return true;
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        NbtCompound lv = super.toInitialChunkDataNbt(registries);
        lv.putNullable(HIT_DIRECTION_NBT_KEY, Direction.INDEX_CODEC, this.hitDirection);
        if (!this.item.isEmpty()) {
            RegistryOps<NbtElement> lv2 = registries.getOps(NbtOps.INSTANCE);
            lv.put(ITEM_NBT_KEY, ItemStack.CODEC, lv2, this.item);
        }
        return lv;
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.item = !this.readLootTableFromData(view) ? view.read(ITEM_NBT_KEY, ItemStack.CODEC).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
        this.hitDirection = view.read(HIT_DIRECTION_NBT_KEY, Direction.INDEX_CODEC).orElse(null);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        if (!this.writeLootTableToData(view) && !this.item.isEmpty()) {
            view.put(ITEM_NBT_KEY, ItemStack.CODEC, this.item);
        }
    }

    public void setLootTable(RegistryKey<LootTable> lootTable, long seed) {
        this.lootTable = lootTable;
        this.lootTableSeed = seed;
    }

    private int getDustedLevel() {
        if (this.brushesCount == 0) {
            return 0;
        }
        if (this.brushesCount < 3) {
            return 1;
        }
        if (this.brushesCount < 6) {
            return 2;
        }
        return 3;
    }

    @Nullable
    public Direction getHitDirection() {
        return this.hitDirection;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public /* synthetic */ Packet toUpdatePacket() {
        return this.toUpdatePacket();
    }
}

