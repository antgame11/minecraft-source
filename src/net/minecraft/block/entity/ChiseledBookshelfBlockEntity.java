/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.entity;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChiseledBookshelfBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.ListInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import org.slf4j.Logger;

public class ChiseledBookshelfBlockEntity
extends BlockEntity
implements ListInventory {
    public static final int MAX_BOOKS = 6;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_LAST_INTERACTED_SLOT = -1;
    private final DefaultedList<ItemStack> heldStacks = DefaultedList.ofSize(6, ItemStack.EMPTY);
    private int lastInteractedSlot = -1;

    public ChiseledBookshelfBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.CHISELED_BOOKSHELF, pos, state);
    }

    private void updateState(int interactedSlot) {
        if (interactedSlot < 0 || interactedSlot >= 6) {
            LOGGER.error("Expected slot 0-5, got {}", (Object)interactedSlot);
            return;
        }
        this.lastInteractedSlot = interactedSlot;
        BlockState lv = this.getCachedState();
        for (int j = 0; j < ChiseledBookshelfBlock.SLOT_OCCUPIED_PROPERTIES.size(); ++j) {
            boolean bl = !this.getStack(j).isEmpty();
            BooleanProperty lv2 = ChiseledBookshelfBlock.SLOT_OCCUPIED_PROPERTIES.get(j);
            lv = (BlockState)lv.with(lv2, bl);
        }
        Objects.requireNonNull(this.world).setBlockState(this.pos, lv, Block.NOTIFY_ALL);
        this.world.emitGameEvent(GameEvent.BLOCK_CHANGE, this.pos, GameEvent.Emitter.of(lv));
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.heldStacks.clear();
        Inventories.readData(view, this.heldStacks);
        this.lastInteractedSlot = view.getInt("last_interacted_slot", -1);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, this.heldStacks, true);
        view.putInt("last_interacted_slot", this.lastInteractedSlot);
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    @Override
    public boolean canAccept(ItemStack stack) {
        return stack.isIn(ItemTags.BOOKSHELF_BOOKS);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack lv = Objects.requireNonNullElse(this.getHeldStacks().get(slot), ItemStack.EMPTY);
        this.getHeldStacks().set(slot, ItemStack.EMPTY);
        if (!lv.isEmpty()) {
            this.updateState(slot);
        }
        return lv;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (this.canAccept(stack)) {
            this.getHeldStacks().set(slot, stack);
            this.updateState(slot);
        } else if (stack.isEmpty()) {
            this.removeStack(slot, this.getMaxCountPerStack());
        }
    }

    @Override
    public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        return hopperInventory.containsAny((ItemStack arg3) -> {
            if (arg3.isEmpty()) {
                return true;
            }
            return ItemStack.areItemsAndComponentsEqual(stack, arg3) && arg3.getCount() + stack.getCount() <= hopperInventory.getMaxCount((ItemStack)arg3);
        });
    }

    @Override
    public DefaultedList<ItemStack> getHeldStacks() {
        return this.heldStacks;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    public int getLastInteractedSlot() {
        return this.lastInteractedSlot;
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        components.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyTo(this.heldStacks);
    }

    @Override
    protected void addComponents(ComponentMap.Builder builder) {
        super.addComponents(builder);
        builder.add(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(this.heldStacks));
    }

    @Override
    public void removeFromCopiedStackData(WriteView view) {
        view.remove("Items");
    }
}

