/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.inventory;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface LootableInventory
extends Inventory {
    public static final String LOOT_TABLE_KEY = "LootTable";
    public static final String LOOT_TABLE_SEED_KEY = "LootTableSeed";

    @Nullable
    public RegistryKey<LootTable> getLootTable();

    public void setLootTable(@Nullable RegistryKey<LootTable> var1);

    default public void setLootTable(RegistryKey<LootTable> lootTableId, long lootTableSeed) {
        this.setLootTable(lootTableId);
        this.setLootTableSeed(lootTableSeed);
    }

    public long getLootTableSeed();

    public void setLootTableSeed(long var1);

    public BlockPos getPos();

    @Nullable
    public World getWorld();

    public static void setLootTable(BlockView world, Random random, BlockPos pos, RegistryKey<LootTable> lootTableId) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof LootableInventory) {
            LootableInventory lv2 = (LootableInventory)((Object)lv);
            lv2.setLootTable(lootTableId, random.nextLong());
        }
    }

    default public boolean readLootTable(ReadView view) {
        RegistryKey lv = view.read(LOOT_TABLE_KEY, LootTable.TABLE_KEY).orElse(null);
        this.setLootTable(lv);
        this.setLootTableSeed(view.getLong(LOOT_TABLE_SEED_KEY, 0L));
        return lv != null;
    }

    default public boolean writeLootTable(WriteView view) {
        RegistryKey<LootTable> lv = this.getLootTable();
        if (lv == null) {
            return false;
        }
        view.put(LOOT_TABLE_KEY, LootTable.TABLE_KEY, lv);
        long l = this.getLootTableSeed();
        if (l != 0L) {
            view.putLong(LOOT_TABLE_SEED_KEY, l);
        }
        return true;
    }

    default public void generateLoot(@Nullable PlayerEntity player) {
        World lv = this.getWorld();
        BlockPos lv2 = this.getPos();
        RegistryKey<LootTable> lv3 = this.getLootTable();
        if (lv3 != null && lv != null && lv.getServer() != null) {
            LootTable lv4 = lv.getServer().getReloadableRegistries().getLootTable(lv3);
            if (player instanceof ServerPlayerEntity) {
                Criteria.PLAYER_GENERATES_CONTAINER_LOOT.trigger((ServerPlayerEntity)player, lv3);
            }
            this.setLootTable(null);
            LootWorldContext.Builder lv5 = new LootWorldContext.Builder((ServerWorld)lv).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(lv2));
            if (player != null) {
                lv5.luck(player.getLuck()).add(LootContextParameters.THIS_ENTITY, player);
            }
            lv4.supplyInventory(this, lv5.build(LootContextTypes.CHEST), this.getLootTableSeed());
        }
    }
}

