/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.screen;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public abstract class ForgingScreenHandler
extends ScreenHandler {
    private static final int field_41901 = 9;
    private static final int field_41902 = 3;
    private static final int field_54595 = 0;
    protected final ScreenHandlerContext context;
    protected final PlayerEntity player;
    protected final Inventory input;
    protected final CraftingResultInventory output = new CraftingResultInventory(){

        @Override
        public void markDirty() {
            ForgingScreenHandler.this.onContentChanged(this);
        }
    };
    private final int resultSlotIndex;

    protected boolean canTakeOutput(PlayerEntity player, boolean present) {
        return true;
    }

    protected abstract void onTakeOutput(PlayerEntity var1, ItemStack var2);

    protected abstract boolean canUse(BlockState var1);

    public ForgingScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, ForgingSlotsManager forgingSlotsManager) {
        super(type, syncId);
        this.context = context;
        this.player = playerInventory.player;
        this.input = this.createInputInventory(forgingSlotsManager.getInputSlotCount());
        this.resultSlotIndex = forgingSlotsManager.getResultSlotIndex();
        this.addInputSlots(forgingSlotsManager);
        this.addResultSlot(forgingSlotsManager);
        this.addPlayerSlots(playerInventory, 8, 84);
    }

    private void addInputSlots(ForgingSlotsManager forgingSlotsManager) {
        for (final ForgingSlotsManager.ForgingSlot lv : forgingSlotsManager.getInputSlots()) {
            this.addSlot(new Slot(this, this.input, lv.slotId(), lv.x(), lv.y()){

                @Override
                public boolean canInsert(ItemStack stack) {
                    return lv.mayPlace().test(stack);
                }
            });
        }
    }

    private void addResultSlot(ForgingSlotsManager forgingSlotsManager) {
        this.addSlot(new Slot(this.output, forgingSlotsManager.getResultSlot().slotId(), forgingSlotsManager.getResultSlot().x(), forgingSlotsManager.getResultSlot().y()){

            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public boolean canTakeItems(PlayerEntity playerEntity) {
                return ForgingScreenHandler.this.canTakeOutput(playerEntity, this.hasStack());
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                ForgingScreenHandler.this.onTakeOutput(player, stack);
            }
        });
    }

    public abstract void updateResult();

    private SimpleInventory createInputInventory(int size) {
        return new SimpleInventory(size){

            @Override
            public void markDirty() {
                super.markDirty();
                ForgingScreenHandler.this.onContentChanged(this);
            }
        };
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        if (inventory == this.input) {
            this.updateResult();
        }
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.context.run((world, pos) -> this.dropInventory(player, this.input));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.context.get((world, pos) -> {
            if (!this.canUse(world.getBlockState((BlockPos)pos))) {
                return false;
            }
            return player.canInteractWithBlockAt((BlockPos)pos, 4.0);
        }, true);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack lv = ItemStack.EMPTY;
        Slot lv2 = (Slot)this.slots.get(slot);
        if (lv2 != null && lv2.hasStack()) {
            ItemStack lv3 = lv2.getStack();
            lv = lv3.copy();
            int j = this.getPlayerInventoryStartIndex();
            int k = this.getPlayerHotbarEndIndex();
            if (slot == this.getResultSlotIndex()) {
                if (!this.insertItem(lv3, j, k, true)) {
                    return ItemStack.EMPTY;
                }
                lv2.onQuickTransfer(lv3, lv);
            } else if (slot >= 0 && slot < this.getResultSlotIndex() ? !this.insertItem(lv3, j, k, false) : (this.isValidIngredient(lv3) && slot >= this.getPlayerInventoryStartIndex() && slot < this.getPlayerHotbarEndIndex() ? !this.insertItem(lv3, 0, this.getResultSlotIndex(), false) : (slot >= this.getPlayerInventoryStartIndex() && slot < this.getPlayerInventoryEndIndex() ? !this.insertItem(lv3, this.getPlayerHotbarStartIndex(), this.getPlayerHotbarEndIndex(), false) : slot >= this.getPlayerHotbarStartIndex() && slot < this.getPlayerHotbarEndIndex() && !this.insertItem(lv3, this.getPlayerInventoryStartIndex(), this.getPlayerInventoryEndIndex(), false)))) {
                return ItemStack.EMPTY;
            }
            if (lv3.isEmpty()) {
                lv2.setStack(ItemStack.EMPTY);
            } else {
                lv2.markDirty();
            }
            if (lv3.getCount() == lv.getCount()) {
                return ItemStack.EMPTY;
            }
            lv2.onTakeItem(player, lv3);
        }
        return lv;
    }

    protected boolean isValidIngredient(ItemStack stack) {
        return true;
    }

    public int getResultSlotIndex() {
        return this.resultSlotIndex;
    }

    private int getPlayerInventoryStartIndex() {
        return this.getResultSlotIndex() + 1;
    }

    private int getPlayerInventoryEndIndex() {
        return this.getPlayerInventoryStartIndex() + 27;
    }

    private int getPlayerHotbarStartIndex() {
        return this.getPlayerInventoryEndIndex();
    }

    private int getPlayerHotbarEndIndex() {
        return this.getPlayerHotbarStartIndex() + 9;
    }
}

