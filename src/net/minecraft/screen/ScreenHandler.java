/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.screen;

import com.google.common.base.Suppliers;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.Property;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerSyncHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.sync.ItemStackHash;
import net.minecraft.screen.sync.TrackedSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class ScreenHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int EMPTY_SPACE_SLOT_INDEX = -999;
    public static final int field_30731 = 0;
    public static final int field_30732 = 1;
    public static final int field_30733 = 2;
    public static final int field_30734 = 0;
    public static final int field_30735 = 1;
    public static final int field_30736 = 2;
    public static final int field_30737 = Integer.MAX_VALUE;
    public static final int field_52557 = 9;
    public static final int field_52558 = 18;
    private final DefaultedList<ItemStack> trackedStacks = DefaultedList.of();
    public final DefaultedList<Slot> slots = DefaultedList.of();
    private final List<Property> properties = Lists.newArrayList();
    private ItemStack cursorStack = ItemStack.EMPTY;
    private final DefaultedList<TrackedSlot> trackedSlots = DefaultedList.of();
    private final IntList trackedPropertyValues = new IntArrayList();
    private TrackedSlot trackedCursorSlot = TrackedSlot.ALWAYS_IN_SYNC;
    private int revision;
    @Nullable
    private final ScreenHandlerType<?> type;
    public final int syncId;
    private int quickCraftButton = -1;
    private int quickCraftStage;
    private final Set<Slot> quickCraftSlots = Sets.newHashSet();
    private final List<ScreenHandlerListener> listeners = Lists.newArrayList();
    @Nullable
    private ScreenHandlerSyncHandler syncHandler;
    private boolean disableSync;

    protected ScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId) {
        this.type = type;
        this.syncId = syncId;
    }

    protected void addPlayerHotbarSlots(Inventory playerInventory, int left, int y) {
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, left + k * 18, y));
        }
    }

    protected void addPlayerInventorySlots(Inventory playerInventory, int left, int top) {
        for (int k = 0; k < 3; ++k) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + (k + 1) * 9, left + l * 18, top + k * 18));
            }
        }
    }

    protected void addPlayerSlots(Inventory playerInventory, int left, int top) {
        this.addPlayerInventorySlots(playerInventory, left, top);
        int k = 4;
        int l = 58;
        this.addPlayerHotbarSlots(playerInventory, left, top + 58);
    }

    protected static boolean canUse(ScreenHandlerContext context, PlayerEntity player, Block block) {
        return context.get((world, pos) -> {
            if (!world.getBlockState((BlockPos)pos).isOf(block)) {
                return false;
            }
            return player.canInteractWithBlockAt((BlockPos)pos, 4.0);
        }, true);
    }

    public ScreenHandlerType<?> getType() {
        if (this.type == null) {
            throw new UnsupportedOperationException("Unable to construct this menu by type");
        }
        return this.type;
    }

    protected static void checkSize(Inventory inventory, int expectedSize) {
        int j = inventory.size();
        if (j < expectedSize) {
            throw new IllegalArgumentException("Container size " + j + " is smaller than expected " + expectedSize);
        }
    }

    protected static void checkDataCount(PropertyDelegate data, int expectedCount) {
        int j = data.size();
        if (j < expectedCount) {
            throw new IllegalArgumentException("Container data count " + j + " is smaller than expected " + expectedCount);
        }
    }

    public boolean isValid(int slot) {
        return slot == -1 || slot == -999 || slot < this.slots.size();
    }

    protected Slot addSlot(Slot slot) {
        slot.id = this.slots.size();
        this.slots.add(slot);
        this.trackedStacks.add(ItemStack.EMPTY);
        this.trackedSlots.add(this.syncHandler != null ? this.syncHandler.createTrackedSlot() : TrackedSlot.ALWAYS_IN_SYNC);
        return slot;
    }

    protected Property addProperty(Property property) {
        this.properties.add(property);
        this.trackedPropertyValues.add(0);
        return property;
    }

    protected void addProperties(PropertyDelegate propertyDelegate) {
        for (int i = 0; i < propertyDelegate.size(); ++i) {
            this.addProperty(Property.create(propertyDelegate, i));
        }
    }

    public void addListener(ScreenHandlerListener listener) {
        if (this.listeners.contains(listener)) {
            return;
        }
        this.listeners.add(listener);
        this.sendContentUpdates();
    }

    public void updateSyncHandler(ScreenHandlerSyncHandler handler) {
        this.syncHandler = handler;
        this.trackedCursorSlot = handler.createTrackedSlot();
        this.trackedSlots.replaceAll(slot -> handler.createTrackedSlot());
        this.syncState();
    }

    public void syncState() {
        ArrayList<ItemStack> list = new ArrayList<ItemStack>(this.slots.size());
        int j = this.slots.size();
        for (int i = 0; i < j; ++i) {
            ItemStack lv = this.slots.get(i).getStack();
            list.add(lv.copy());
            this.trackedSlots.get(i).setReceivedStack(lv);
        }
        ItemStack lv2 = this.getCursorStack();
        this.trackedCursorSlot.setReceivedStack(lv2);
        int k = this.properties.size();
        for (j = 0; j < k; ++j) {
            this.trackedPropertyValues.set(j, this.properties.get(j).get());
        }
        if (this.syncHandler != null) {
            this.syncHandler.updateState(this, list, lv2.copy(), this.trackedPropertyValues.toIntArray());
        }
    }

    public void removeListener(ScreenHandlerListener listener) {
        this.listeners.remove(listener);
    }

    public DefaultedList<ItemStack> getStacks() {
        DefaultedList<ItemStack> lv = DefaultedList.of();
        for (Slot lv2 : this.slots) {
            lv.add(lv2.getStack());
        }
        return lv;
    }

    public void sendContentUpdates() {
        int i;
        for (i = 0; i < this.slots.size(); ++i) {
            ItemStack lv = this.slots.get(i).getStack();
            com.google.common.base.Supplier<ItemStack> supplier = Suppliers.memoize(lv::copy);
            this.updateTrackedSlot(i, lv, supplier);
            this.checkSlotUpdates(i, lv, supplier);
        }
        this.checkCursorStackUpdates();
        for (i = 0; i < this.properties.size(); ++i) {
            Property lv2 = this.properties.get(i);
            int j = lv2.get();
            if (lv2.hasChanged()) {
                this.notifyPropertyUpdate(i, j);
            }
            this.checkPropertyUpdates(i, j);
        }
    }

    public void updateToClient() {
        int i;
        for (i = 0; i < this.slots.size(); ++i) {
            ItemStack lv = this.slots.get(i).getStack();
            this.updateTrackedSlot(i, lv, lv::copy);
        }
        for (i = 0; i < this.properties.size(); ++i) {
            Property lv2 = this.properties.get(i);
            if (!lv2.hasChanged()) continue;
            this.notifyPropertyUpdate(i, lv2.get());
        }
        this.syncState();
    }

    private void notifyPropertyUpdate(int index, int value) {
        for (ScreenHandlerListener lv : this.listeners) {
            lv.onPropertyUpdate(this, index, value);
        }
    }

    private void updateTrackedSlot(int slot, ItemStack stack, Supplier<ItemStack> copySupplier) {
        ItemStack lv = this.trackedStacks.get(slot);
        if (!ItemStack.areEqual(lv, stack)) {
            ItemStack lv2 = copySupplier.get();
            this.trackedStacks.set(slot, lv2);
            for (ScreenHandlerListener lv3 : this.listeners) {
                lv3.onSlotUpdate(this, slot, lv2);
            }
        }
    }

    private void checkSlotUpdates(int slot, ItemStack stack, Supplier<ItemStack> copySupplier) {
        if (this.disableSync) {
            return;
        }
        TrackedSlot lv = this.trackedSlots.get(slot);
        if (!lv.isInSync(stack)) {
            lv.setReceivedStack(stack);
            if (this.syncHandler != null) {
                this.syncHandler.updateSlot(this, slot, copySupplier.get());
            }
        }
    }

    private void checkPropertyUpdates(int id, int value) {
        if (this.disableSync) {
            return;
        }
        int k = this.trackedPropertyValues.getInt(id);
        if (k != value) {
            this.trackedPropertyValues.set(id, value);
            if (this.syncHandler != null) {
                this.syncHandler.updateProperty(this, id, value);
            }
        }
    }

    private void checkCursorStackUpdates() {
        if (this.disableSync) {
            return;
        }
        ItemStack lv = this.getCursorStack();
        if (!this.trackedCursorSlot.isInSync(lv)) {
            this.trackedCursorSlot.setReceivedStack(lv);
            if (this.syncHandler != null) {
                this.syncHandler.updateCursorStack(this, lv.copy());
            }
        }
    }

    public void setReceivedStack(int slot, ItemStack stack) {
        this.trackedSlots.get(slot).setReceivedStack(stack);
    }

    public void setReceivedHash(int slot, ItemStackHash hash) {
        if (slot < 0 || slot >= this.trackedSlots.size()) {
            LOGGER.debug("Incorrect slot index: {} available slots: {}", (Object)slot, (Object)this.trackedSlots.size());
            return;
        }
        this.trackedSlots.get(slot).setReceivedHash(hash);
    }

    public void setReceivedCursorHash(ItemStackHash cursorStackHash) {
        this.trackedCursorSlot.setReceivedHash(cursorStackHash);
    }

    public boolean onButtonClick(PlayerEntity player, int id) {
        return false;
    }

    public Slot getSlot(int index) {
        return this.slots.get(index);
    }

    public abstract ItemStack quickMove(PlayerEntity var1, int var2);

    public void selectBundleStack(int slot, int selectedStack) {
        if (slot >= 0 && slot < this.slots.size()) {
            ItemStack lv = this.slots.get(slot).getStack();
            BundleItem.setSelectedStackIndex(lv, selectedStack);
        }
    }

    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        try {
            this.internalOnSlotClick(slotIndex, button, actionType, player);
        } catch (Exception exception) {
            CrashReport lv = CrashReport.create(exception, "Container click");
            CrashReportSection lv2 = lv.addElement("Click info");
            lv2.add("Menu Type", () -> this.type != null ? Registries.SCREEN_HANDLER.getId(this.type).toString() : "<no type>");
            lv2.add("Menu Class", () -> this.getClass().getCanonicalName());
            lv2.add("Slot Count", this.slots.size());
            lv2.add("Slot", slotIndex);
            lv2.add("Button", button);
            lv2.add("Type", (Object)actionType);
            throw new CrashException(lv);
        }
    }

    private void internalOnSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        block40: {
            block52: {
                int l;
                block51: {
                    block47: {
                        ItemStack lv3;
                        Slot lv2;
                        ItemStack lv9;
                        PlayerInventory lv;
                        block50: {
                            block49: {
                                block48: {
                                    block45: {
                                        ClickType lv7;
                                        block46: {
                                            block44: {
                                                block38: {
                                                    block43: {
                                                        ItemStack lv32;
                                                        block42: {
                                                            block41: {
                                                                block39: {
                                                                    lv = player.getInventory();
                                                                    if (actionType != SlotActionType.QUICK_CRAFT) break block38;
                                                                    int k = this.quickCraftStage;
                                                                    this.quickCraftStage = ScreenHandler.unpackQuickCraftStage(button);
                                                                    if (k == 1 && this.quickCraftStage == 2 || k == this.quickCraftStage) break block39;
                                                                    this.endQuickCraft();
                                                                    break block40;
                                                                }
                                                                if (!this.getCursorStack().isEmpty()) break block41;
                                                                this.endQuickCraft();
                                                                break block40;
                                                            }
                                                            if (this.quickCraftStage != 0) break block42;
                                                            this.quickCraftButton = ScreenHandler.unpackQuickCraftButton(button);
                                                            if (ScreenHandler.shouldQuickCraftContinue(this.quickCraftButton, player)) {
                                                                this.quickCraftStage = 1;
                                                                this.quickCraftSlots.clear();
                                                            } else {
                                                                this.endQuickCraft();
                                                            }
                                                            break block40;
                                                        }
                                                        if (this.quickCraftStage != 1) break block43;
                                                        Slot lv22 = this.slots.get(slotIndex);
                                                        if (!ScreenHandler.canInsertItemIntoSlot(lv22, lv32 = this.getCursorStack(), true) || !lv22.canInsert(lv32) || this.quickCraftButton != 2 && lv32.getCount() <= this.quickCraftSlots.size() || !this.canInsertIntoSlot(lv22)) break block40;
                                                        this.quickCraftSlots.add(lv22);
                                                        break block40;
                                                    }
                                                    if (this.quickCraftStage == 2) {
                                                        if (!this.quickCraftSlots.isEmpty()) {
                                                            if (this.quickCraftSlots.size() == 1) {
                                                                int l2 = this.quickCraftSlots.iterator().next().id;
                                                                this.endQuickCraft();
                                                                this.internalOnSlotClick(l2, this.quickCraftButton, SlotActionType.PICKUP, player);
                                                                return;
                                                            }
                                                            ItemStack lv4 = this.getCursorStack().copy();
                                                            if (lv4.isEmpty()) {
                                                                this.endQuickCraft();
                                                                return;
                                                            }
                                                            int m = this.getCursorStack().getCount();
                                                            for (Slot lv5 : this.quickCraftSlots) {
                                                                ItemStack lv6 = this.getCursorStack();
                                                                if (lv5 == null || !ScreenHandler.canInsertItemIntoSlot(lv5, lv6, true) || !lv5.canInsert(lv6) || this.quickCraftButton != 2 && lv6.getCount() < this.quickCraftSlots.size() || !this.canInsertIntoSlot(lv5)) continue;
                                                                int n = lv5.hasStack() ? lv5.getStack().getCount() : 0;
                                                                int o = Math.min(lv4.getMaxCount(), lv5.getMaxItemCount(lv4));
                                                                int p = Math.min(ScreenHandler.calculateStackSize(this.quickCraftSlots, this.quickCraftButton, lv4) + n, o);
                                                                m -= p - n;
                                                                lv5.setStack(lv4.copyWithCount(p));
                                                            }
                                                            lv4.setCount(m);
                                                            this.setCursorStack(lv4);
                                                        }
                                                        this.endQuickCraft();
                                                    } else {
                                                        this.endQuickCraft();
                                                    }
                                                    break block40;
                                                }
                                                if (this.quickCraftStage == 0) break block44;
                                                this.endQuickCraft();
                                                break block40;
                                            }
                                            if (actionType != SlotActionType.PICKUP && actionType != SlotActionType.QUICK_MOVE || button != 0 && button != 1) break block45;
                                            ClickType clickType = lv7 = button == 0 ? ClickType.LEFT : ClickType.RIGHT;
                                            if (slotIndex != EMPTY_SPACE_SLOT_INDEX) break block46;
                                            if (this.getCursorStack().isEmpty()) break block40;
                                            if (lv7 == ClickType.LEFT) {
                                                player.dropItem(this.getCursorStack(), true);
                                                this.setCursorStack(ItemStack.EMPTY);
                                            } else {
                                                player.dropItem(this.getCursorStack().split(1), true);
                                            }
                                            break block40;
                                        }
                                        if (actionType == SlotActionType.QUICK_MOVE) {
                                            if (slotIndex < 0) {
                                                return;
                                            }
                                            Slot lv23 = this.slots.get(slotIndex);
                                            if (!lv23.canTakeItems(player)) {
                                                return;
                                            }
                                            ItemStack lv33 = this.quickMove(player, slotIndex);
                                            while (!lv33.isEmpty() && ItemStack.areItemsEqual(lv23.getStack(), lv33)) {
                                                lv33 = this.quickMove(player, slotIndex);
                                            }
                                        } else {
                                            if (slotIndex < 0) {
                                                return;
                                            }
                                            Slot lv24 = this.slots.get(slotIndex);
                                            ItemStack lv34 = lv24.getStack();
                                            ItemStack lv8 = this.getCursorStack();
                                            player.onPickupSlotClick(lv8, lv24.getStack(), lv7);
                                            if (!this.handleSlotClick(player, lv7, lv24, lv34, lv8)) {
                                                if (lv34.isEmpty()) {
                                                    if (!lv8.isEmpty()) {
                                                        int q = lv7 == ClickType.LEFT ? lv8.getCount() : 1;
                                                        this.setCursorStack(lv24.insertStack(lv8, q));
                                                    }
                                                } else if (lv24.canTakeItems(player)) {
                                                    if (lv8.isEmpty()) {
                                                        int q = lv7 == ClickType.LEFT ? lv34.getCount() : (lv34.getCount() + 1) / 2;
                                                        Optional<ItemStack> optional = lv24.tryTakeStackRange(q, Integer.MAX_VALUE, player);
                                                        optional.ifPresent(stack -> {
                                                            this.setCursorStack((ItemStack)stack);
                                                            lv24.onTakeItem(player, (ItemStack)stack);
                                                        });
                                                    } else if (lv24.canInsert(lv8)) {
                                                        if (ItemStack.areItemsAndComponentsEqual(lv34, lv8)) {
                                                            int q = lv7 == ClickType.LEFT ? lv8.getCount() : 1;
                                                            this.setCursorStack(lv24.insertStack(lv8, q));
                                                        } else if (lv8.getCount() <= lv24.getMaxItemCount(lv8)) {
                                                            this.setCursorStack(lv34);
                                                            lv24.setStack(lv8);
                                                        }
                                                    } else if (ItemStack.areItemsAndComponentsEqual(lv34, lv8)) {
                                                        Optional<ItemStack> optional2 = lv24.tryTakeStackRange(lv34.getCount(), lv8.getMaxCount() - lv8.getCount(), player);
                                                        optional2.ifPresent(stack -> {
                                                            lv8.increment(stack.getCount());
                                                            lv24.onTakeItem(player, (ItemStack)stack);
                                                        });
                                                    }
                                                }
                                            }
                                            lv24.markDirty();
                                        }
                                        break block40;
                                    }
                                    if (actionType != SlotActionType.SWAP || (button < 0 || button >= 9) && button != 40) break block47;
                                    lv9 = lv.getStack(button);
                                    lv2 = this.slots.get(slotIndex);
                                    lv3 = lv2.getStack();
                                    if (lv9.isEmpty() && lv3.isEmpty()) break block40;
                                    if (!lv9.isEmpty()) break block48;
                                    if (!lv2.canTakeItems(player)) break block40;
                                    lv.setStack(button, lv3);
                                    lv2.onTake(lv3.getCount());
                                    lv2.setStack(ItemStack.EMPTY);
                                    lv2.onTakeItem(player, lv3);
                                    break block40;
                                }
                                if (!lv3.isEmpty()) break block49;
                                if (!lv2.canInsert(lv9)) break block40;
                                int r = lv2.getMaxItemCount(lv9);
                                if (lv9.getCount() > r) {
                                    lv2.setStack(lv9.split(r));
                                } else {
                                    lv.setStack(button, ItemStack.EMPTY);
                                    lv2.setStack(lv9);
                                }
                                break block40;
                            }
                            if (!lv2.canTakeItems(player) || !lv2.canInsert(lv9)) break block40;
                            int r = lv2.getMaxItemCount(lv9);
                            if (lv9.getCount() <= r) break block50;
                            lv2.setStack(lv9.split(r));
                            lv2.onTakeItem(player, lv3);
                            if (lv.insertStack(lv3)) break block40;
                            player.dropItem(lv3, true);
                            break block40;
                        }
                        lv.setStack(button, lv3);
                        lv2.setStack(lv9);
                        lv2.onTakeItem(player, lv3);
                        break block40;
                    }
                    if (actionType != SlotActionType.CLONE || !player.isInCreativeMode() || !this.getCursorStack().isEmpty() || slotIndex < 0) break block51;
                    Slot lv10 = this.slots.get(slotIndex);
                    if (!lv10.hasStack()) break block40;
                    ItemStack lv4 = lv10.getStack();
                    this.setCursorStack(lv4.copyWithCount(lv4.getMaxCount()));
                    break block40;
                }
                if (actionType != SlotActionType.THROW || !this.getCursorStack().isEmpty() || slotIndex < 0) break block52;
                Slot lv10 = this.slots.get(slotIndex);
                int n = l = button == 0 ? 1 : lv10.getStack().getCount();
                if (!player.canDropItems()) {
                    return;
                }
                ItemStack lv3 = lv10.takeStackRange(l, Integer.MAX_VALUE, player);
                player.dropItem(lv3, true);
                player.dropCreativeStack(lv3);
                if (button != 1) break block40;
                while (!lv3.isEmpty() && ItemStack.areItemsEqual(lv10.getStack(), lv3)) {
                    if (!player.canDropItems()) {
                        return;
                    }
                    lv3 = lv10.takeStackRange(l, Integer.MAX_VALUE, player);
                    player.dropItem(lv3, true);
                    player.dropCreativeStack(lv3);
                }
                break block40;
            }
            if (actionType == SlotActionType.PICKUP_ALL && slotIndex >= 0) {
                Slot lv10 = this.slots.get(slotIndex);
                ItemStack lv4 = this.getCursorStack();
                if (!(lv4.isEmpty() || lv10.hasStack() && lv10.canTakeItems(player))) {
                    int m = button == 0 ? 0 : this.slots.size() - 1;
                    int r = button == 0 ? 1 : -1;
                    for (int q = 0; q < 2; ++q) {
                        for (int s = m; s >= 0 && s < this.slots.size() && lv4.getCount() < lv4.getMaxCount(); s += r) {
                            Slot lv11 = this.slots.get(s);
                            if (!lv11.hasStack() || !ScreenHandler.canInsertItemIntoSlot(lv11, lv4, true) || !lv11.canTakeItems(player) || !this.canInsertIntoSlot(lv4, lv11)) continue;
                            ItemStack lv12 = lv11.getStack();
                            if (q == 0 && lv12.getCount() == lv12.getMaxCount()) continue;
                            ItemStack lv13 = lv11.takeStackRange(lv12.getCount(), lv4.getMaxCount() - lv4.getCount(), player);
                            lv4.increment(lv13.getCount());
                        }
                    }
                }
            }
        }
    }

    private boolean handleSlotClick(PlayerEntity player, ClickType clickType, Slot slot, ItemStack stack, ItemStack cursorStack) {
        FeatureSet lv = player.getEntityWorld().getEnabledFeatures();
        if (cursorStack.isItemEnabled(lv) && cursorStack.onStackClicked(slot, clickType, player)) {
            return true;
        }
        return stack.isItemEnabled(lv) && stack.onClicked(cursorStack, slot, clickType, player, this.getCursorStackReference());
    }

    private StackReference getCursorStackReference() {
        return new StackReference(){

            @Override
            public ItemStack get() {
                return ScreenHandler.this.getCursorStack();
            }

            @Override
            public boolean set(ItemStack stack) {
                ScreenHandler.this.setCursorStack(stack);
                return true;
            }
        };
    }

    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return true;
    }

    public void onClosed(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity)) {
            return;
        }
        ItemStack lv = this.getCursorStack();
        if (!lv.isEmpty()) {
            ScreenHandler.offerOrDropStack(player, lv);
            this.setCursorStack(ItemStack.EMPTY);
        }
    }

    private static void offerOrDropStack(PlayerEntity player, ItemStack stack) {
        ServerPlayerEntity lv;
        boolean bl2;
        boolean bl = player.isRemoved() && player.getRemovalReason() != Entity.RemovalReason.CHANGED_DIMENSION;
        boolean bl3 = bl2 = player instanceof ServerPlayerEntity && (lv = (ServerPlayerEntity)player).isDisconnected();
        if (bl || bl2) {
            player.dropItem(stack, false);
        } else if (player instanceof ServerPlayerEntity) {
            player.getInventory().offerOrDrop(stack);
        }
    }

    protected void dropInventory(PlayerEntity player, Inventory inventory) {
        for (int i = 0; i < inventory.size(); ++i) {
            ScreenHandler.offerOrDropStack(player, inventory.removeStack(i));
        }
    }

    public void onContentChanged(Inventory inventory) {
        this.sendContentUpdates();
    }

    public void setStackInSlot(int slot, int revision, ItemStack stack) {
        this.getSlot(slot).setStackNoCallbacks(stack);
        this.revision = revision;
    }

    public void updateSlotStacks(int revision, List<ItemStack> stacks, ItemStack cursorStack) {
        for (int j = 0; j < stacks.size(); ++j) {
            this.getSlot(j).setStackNoCallbacks(stacks.get(j));
        }
        this.cursorStack = cursorStack;
        this.revision = revision;
    }

    public void setProperty(int id, int value) {
        this.properties.get(id).set(value);
    }

    public abstract boolean canUse(PlayerEntity var1);

    protected boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        int l;
        ItemStack lv2;
        Slot lv;
        boolean bl2 = false;
        int k = startIndex;
        if (fromLast) {
            k = endIndex - 1;
        }
        if (stack.isStackable()) {
            while (!stack.isEmpty() && (fromLast ? k >= startIndex : k < endIndex)) {
                lv = this.slots.get(k);
                lv2 = lv.getStack();
                if (!lv2.isEmpty() && ItemStack.areItemsAndComponentsEqual(stack, lv2)) {
                    int m;
                    l = lv2.getCount() + stack.getCount();
                    if (l <= (m = lv.getMaxItemCount(lv2))) {
                        stack.setCount(0);
                        lv2.setCount(l);
                        lv.markDirty();
                        bl2 = true;
                    } else if (lv2.getCount() < m) {
                        stack.decrement(m - lv2.getCount());
                        lv2.setCount(m);
                        lv.markDirty();
                        bl2 = true;
                    }
                }
                if (fromLast) {
                    --k;
                    continue;
                }
                ++k;
            }
        }
        if (!stack.isEmpty()) {
            k = fromLast ? endIndex - 1 : startIndex;
            while (fromLast ? k >= startIndex : k < endIndex) {
                lv = this.slots.get(k);
                lv2 = lv.getStack();
                if (lv2.isEmpty() && lv.canInsert(stack)) {
                    l = lv.getMaxItemCount(stack);
                    lv.setStack(stack.split(Math.min(stack.getCount(), l)));
                    lv.markDirty();
                    bl2 = true;
                    break;
                }
                if (fromLast) {
                    --k;
                    continue;
                }
                ++k;
            }
        }
        return bl2;
    }

    public static int unpackQuickCraftButton(int quickCraftData) {
        return quickCraftData >> 2 & 3;
    }

    public static int unpackQuickCraftStage(int quickCraftData) {
        return quickCraftData & 3;
    }

    public static int packQuickCraftData(int quickCraftStage, int buttonId) {
        return quickCraftStage & 3 | (buttonId & 3) << 2;
    }

    public static boolean shouldQuickCraftContinue(int stage, PlayerEntity player) {
        if (stage == 0) {
            return true;
        }
        if (stage == 1) {
            return true;
        }
        return stage == 2 && player.isInCreativeMode();
    }

    protected void endQuickCraft() {
        this.quickCraftStage = 0;
        this.quickCraftSlots.clear();
    }

    public static boolean canInsertItemIntoSlot(@Nullable Slot slot, ItemStack stack, boolean allowOverflow) {
        boolean bl2;
        boolean bl = bl2 = slot == null || !slot.hasStack();
        if (!bl2 && ItemStack.areItemsAndComponentsEqual(stack, slot.getStack())) {
            return slot.getStack().getCount() + (allowOverflow ? 0 : stack.getCount()) <= stack.getMaxCount();
        }
        return bl2;
    }

    public static int calculateStackSize(Set<Slot> slots, int mode, ItemStack stack) {
        return switch (mode) {
            case 0 -> MathHelper.floor((float)stack.getCount() / (float)slots.size());
            case 1 -> 1;
            case 2 -> stack.getMaxCount();
            default -> stack.getCount();
        };
    }

    public boolean canInsertIntoSlot(Slot slot) {
        return true;
    }

    public static int calculateComparatorOutput(@Nullable BlockEntity entity) {
        if (entity instanceof Inventory) {
            return ScreenHandler.calculateComparatorOutput((Inventory)((Object)entity));
        }
        return 0;
    }

    public static int calculateComparatorOutput(@Nullable Inventory inventory) {
        if (inventory == null) {
            return 0;
        }
        float f = 0.0f;
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack lv = inventory.getStack(i);
            if (lv.isEmpty()) continue;
            f += (float)lv.getCount() / (float)inventory.getMaxCount(lv);
        }
        return MathHelper.lerpPositive(f /= (float)inventory.size(), 0, 15);
    }

    public void setCursorStack(ItemStack stack) {
        this.cursorStack = stack;
    }

    public ItemStack getCursorStack() {
        return this.cursorStack;
    }

    public void disableSyncing() {
        this.disableSync = true;
    }

    public void enableSyncing() {
        this.disableSync = false;
    }

    public void copySharedSlots(ScreenHandler handler) {
        Slot lv;
        int i;
        HashBasedTable<Inventory, Integer, Integer> table = HashBasedTable.create();
        for (i = 0; i < handler.slots.size(); ++i) {
            lv = handler.slots.get(i);
            table.put(lv.inventory, lv.getIndex(), i);
        }
        for (i = 0; i < this.slots.size(); ++i) {
            lv = this.slots.get(i);
            Integer integer = (Integer)table.get(lv.inventory, lv.getIndex());
            if (integer == null) continue;
            this.trackedStacks.set(i, handler.trackedStacks.get(integer));
            TrackedSlot lv2 = handler.trackedSlots.get(integer);
            TrackedSlot lv3 = this.trackedSlots.get(i);
            if (!(lv2 instanceof TrackedSlot.Impl)) continue;
            TrackedSlot.Impl lv4 = (TrackedSlot.Impl)lv2;
            if (!(lv3 instanceof TrackedSlot.Impl)) continue;
            TrackedSlot.Impl lv5 = (TrackedSlot.Impl)lv3;
            lv5.copyFrom(lv4);
        }
    }

    public OptionalInt getSlotIndex(Inventory inventory, int index) {
        for (int j = 0; j < this.slots.size(); ++j) {
            Slot lv = this.slots.get(j);
            if (lv.inventory != inventory || index != lv.getIndex()) continue;
            return OptionalInt.of(j);
        }
        return OptionalInt.empty();
    }

    public int getRevision() {
        return this.revision;
    }

    public int nextRevision() {
        this.revision = this.revision + 1 & Short.MAX_VALUE;
        return this.revision;
    }
}

