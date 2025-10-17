/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.vehicle;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface VehicleInventory
extends Inventory,
NamedScreenHandlerFactory {
    public Vec3d getEntityPos();

    public Box getBoundingBox();

    @Nullable
    public RegistryKey<LootTable> getLootTable();

    public void setLootTable(@Nullable RegistryKey<LootTable> var1);

    public long getLootTableSeed();

    public void setLootTableSeed(long var1);

    public DefaultedList<ItemStack> getInventory();

    public void resetInventory();

    public World getEntityWorld();

    public boolean isRemoved();

    @Override
    default public boolean isEmpty() {
        return this.isInventoryEmpty();
    }

    default public void writeInventoryToData(WriteView view) {
        if (this.getLootTable() != null) {
            view.putString("LootTable", this.getLootTable().getValue().toString());
            if (this.getLootTableSeed() != 0L) {
                view.putLong("LootTableSeed", this.getLootTableSeed());
            }
        } else {
            Inventories.writeData(view, this.getInventory());
        }
    }

    default public void readInventoryFromData(ReadView view) {
        this.resetInventory();
        RegistryKey lv = view.read("LootTable", LootTable.TABLE_KEY).orElse(null);
        this.setLootTable(lv);
        this.setLootTableSeed(view.getLong("LootTableSeed", 0L));
        if (lv == null) {
            Inventories.readData(view, this.getInventory());
        }
    }

    default public void onBroken(DamageSource source, ServerWorld world, Entity vehicle) {
        if (!world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
            return;
        }
        ItemScatterer.spawn((World)world, vehicle, (Inventory)this);
        Entity lv = source.getSource();
        if (lv != null && lv.getType() == EntityType.PLAYER) {
            PiglinBrain.onGuardedBlockInteracted(world, (PlayerEntity)lv, true);
        }
    }

    default public ActionResult open(PlayerEntity player) {
        player.openHandledScreen(this);
        return ActionResult.SUCCESS;
    }

    default public void generateInventoryLoot(@Nullable PlayerEntity player) {
        MinecraftServer minecraftServer = this.getEntityWorld().getServer();
        if (this.getLootTable() != null && minecraftServer != null) {
            LootTable lv = minecraftServer.getReloadableRegistries().getLootTable(this.getLootTable());
            if (player != null) {
                Criteria.PLAYER_GENERATES_CONTAINER_LOOT.trigger((ServerPlayerEntity)player, this.getLootTable());
            }
            this.setLootTable(null);
            LootWorldContext.Builder lv2 = new LootWorldContext.Builder((ServerWorld)this.getEntityWorld()).add(LootContextParameters.ORIGIN, this.getEntityPos());
            if (player != null) {
                lv2.luck(player.getLuck()).add(LootContextParameters.THIS_ENTITY, player);
            }
            lv.supplyInventory(this, lv2.build(LootContextTypes.CHEST), this.getLootTableSeed());
        }
    }

    default public void clearInventory() {
        this.generateInventoryLoot(null);
        this.getInventory().clear();
    }

    default public boolean isInventoryEmpty() {
        for (ItemStack lv : this.getInventory()) {
            if (lv.isEmpty()) continue;
            return false;
        }
        return true;
    }

    default public ItemStack removeInventoryStack(int slot) {
        this.generateInventoryLoot(null);
        ItemStack lv = this.getInventory().get(slot);
        if (lv.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.getInventory().set(slot, ItemStack.EMPTY);
        return lv;
    }

    default public ItemStack getInventoryStack(int slot) {
        this.generateInventoryLoot(null);
        return this.getInventory().get(slot);
    }

    default public ItemStack removeInventoryStack(int slot, int amount) {
        this.generateInventoryLoot(null);
        return Inventories.splitStack(this.getInventory(), slot, amount);
    }

    default public void setInventoryStack(int slot, ItemStack stack) {
        this.generateInventoryLoot(null);
        this.getInventory().set(slot, stack);
        stack.capCount(this.getMaxCount(stack));
    }

    default public StackReference getInventoryStackReference(final int slot) {
        if (slot >= 0 && slot < this.size()) {
            return new StackReference(){

                @Override
                public ItemStack get() {
                    return VehicleInventory.this.getInventoryStack(slot);
                }

                @Override
                public boolean set(ItemStack stack) {
                    VehicleInventory.this.setInventoryStack(slot, stack);
                    return true;
                }
            };
        }
        return StackReference.EMPTY;
    }

    default public boolean canPlayerAccess(PlayerEntity player) {
        return !this.isRemoved() && player.canInteractWithEntityIn(this.getBoundingBox(), 4.0);
    }
}

