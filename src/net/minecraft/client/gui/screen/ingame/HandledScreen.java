/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.ingame;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.gui.tooltip.BundleTooltipSubmenuHandler;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipSubmenuHandler;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

@Environment(value=EnvType.CLIENT)
public abstract class HandledScreen<T extends ScreenHandler>
extends Screen
implements ScreenHandlerProvider<T> {
    public static final Identifier BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/container/inventory.png");
    private static final Identifier SLOT_HIGHLIGHT_BACK_TEXTURE = Identifier.ofVanilla("container/slot_highlight_back");
    private static final Identifier SLOT_HIGHLIGHT_FRONT_TEXTURE = Identifier.ofVanilla("container/slot_highlight_front");
    protected static final int field_52802 = 256;
    protected static final int field_52803 = 256;
    private static final float field_32318 = 100.0f;
    private static final int field_32319 = 500;
    protected int backgroundWidth = 176;
    protected int backgroundHeight = 166;
    protected int titleX;
    protected int titleY;
    protected int playerInventoryTitleX;
    protected int playerInventoryTitleY;
    private final List<TooltipSubmenuHandler> tooltipSubmenuHandlers;
    protected final T handler;
    protected final Text playerInventoryTitle;
    @Nullable
    protected Slot focusedSlot;
    @Nullable
    private Slot touchDragSlotStart;
    @Nullable
    private Slot touchHoveredSlot;
    @Nullable
    private Slot lastClickedSlot;
    @Nullable
    private LetGoTouchStack letGoTouchStack;
    protected int x;
    protected int y;
    private boolean touchIsRightClickDrag;
    private ItemStack touchDragStack = ItemStack.EMPTY;
    private long touchDropTimer;
    protected final Set<Slot> cursorDragSlots = Sets.newHashSet();
    protected boolean cursorDragging;
    private int heldButtonType;
    private int heldButtonCode;
    private boolean cancelNextRelease;
    private int draggedStackRemainder;
    private boolean doubleClicking;
    private ItemStack quickMovingStack = ItemStack.EMPTY;

    public HandledScreen(T handler, PlayerInventory inventory, Text title) {
        super(title);
        this.handler = handler;
        this.playerInventoryTitle = inventory.getDisplayName();
        this.cancelNextRelease = true;
        this.titleX = 8;
        this.titleY = 6;
        this.playerInventoryTitleX = 8;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
        this.tooltipSubmenuHandlers = new ArrayList<TooltipSubmenuHandler>();
    }

    @Override
    protected void init() {
        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;
        this.tooltipSubmenuHandlers.clear();
        this.addTooltipSubmenuHandler(new BundleTooltipSubmenuHandler(this.client));
    }

    protected void addTooltipSubmenuHandler(TooltipSubmenuHandler handler) {
        this.tooltipSubmenuHandlers.add(handler);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        this.renderMain(context, mouseX, mouseY, deltaTicks);
        this.renderCursorStack(context, mouseX, mouseY);
        this.renderLetGoTouchStack(context);
    }

    public void renderMain(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        int k = this.x;
        int l = this.y;
        super.render(context, mouseX, mouseY, deltaTicks);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(k, l);
        this.drawForeground(context, mouseX, mouseY);
        Slot lv = this.focusedSlot;
        this.focusedSlot = this.getSlotAt(mouseX, mouseY);
        this.drawSlotHighlightBack(context);
        this.drawSlots(context);
        this.drawSlotHighlightFront(context);
        if (lv != null && lv != this.focusedSlot) {
            this.resetTooltipSubmenus(lv);
        }
        context.getMatrices().popMatrix();
    }

    public void renderCursorStack(DrawContext context, int mouseX, int mouseY) {
        ItemStack lv;
        ItemStack itemStack = lv = this.touchDragStack.isEmpty() ? ((ScreenHandler)this.handler).getCursorStack() : this.touchDragStack;
        if (!lv.isEmpty()) {
            int k = 8;
            int l = this.touchDragStack.isEmpty() ? 8 : 16;
            String string = null;
            if (!this.touchDragStack.isEmpty() && this.touchIsRightClickDrag) {
                lv = lv.copyWithCount(MathHelper.ceil((float)lv.getCount() / 2.0f));
            } else if (this.cursorDragging && this.cursorDragSlots.size() > 1 && (lv = lv.copyWithCount(this.draggedStackRemainder)).isEmpty()) {
                string = String.valueOf(Formatting.YELLOW) + "0";
            }
            context.createNewRootLayer();
            this.drawItem(context, lv, mouseX - 8, mouseY - l, string);
        }
    }

    public void renderLetGoTouchStack(DrawContext context) {
        if (this.letGoTouchStack != null) {
            float f = MathHelper.clamp((float)(Util.getMeasuringTimeMs() - this.letGoTouchStack.time) / 100.0f, 0.0f, 1.0f);
            int i = this.letGoTouchStack.end.x - this.letGoTouchStack.start.x;
            int j = this.letGoTouchStack.end.y - this.letGoTouchStack.start.y;
            int k = this.letGoTouchStack.start.x + (int)((float)i * f);
            int l = this.letGoTouchStack.start.y + (int)((float)j * f);
            context.createNewRootLayer();
            this.drawItem(context, this.letGoTouchStack.item, k, l, null);
            if (f >= 1.0f) {
                this.letGoTouchStack = null;
            }
        }
    }

    protected void drawSlots(DrawContext context) {
        for (Slot lv : ((ScreenHandler)this.handler).slots) {
            if (!lv.isEnabled()) continue;
            this.drawSlot(context, lv);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.renderBackground(context, mouseX, mouseY, deltaTicks);
        this.drawBackground(context, deltaTicks, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.focusedSlot != null && this.focusedSlot.hasStack()) {
            for (TooltipSubmenuHandler lv : this.tooltipSubmenuHandlers) {
                if (!lv.isApplicableTo(this.focusedSlot) || !lv.onScroll(horizontalAmount, verticalAmount, this.focusedSlot.id, this.focusedSlot.getStack())) continue;
                return true;
            }
        }
        return false;
    }

    private void drawSlotHighlightBack(DrawContext context) {
        if (this.focusedSlot != null && this.focusedSlot.canBeHighlighted()) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_TEXTURE, this.focusedSlot.x - 4, this.focusedSlot.y - 4, 24, 24);
        }
    }

    private void drawSlotHighlightFront(DrawContext context) {
        if (this.focusedSlot != null && this.focusedSlot.canBeHighlighted()) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_TEXTURE, this.focusedSlot.x - 4, this.focusedSlot.y - 4, 24, 24);
        }
    }

    protected void drawMouseoverTooltip(DrawContext context, int x, int y) {
        if (this.focusedSlot == null || !this.focusedSlot.hasStack()) {
            return;
        }
        ItemStack lv = this.focusedSlot.getStack();
        if (((ScreenHandler)this.handler).getCursorStack().isEmpty() || this.isItemTooltipSticky(lv)) {
            context.drawTooltip(this.textRenderer, this.getTooltipFromItem(lv), lv.getTooltipData(), x, y, lv.get(DataComponentTypes.TOOLTIP_STYLE));
        }
    }

    private boolean isItemTooltipSticky(ItemStack item) {
        return item.getTooltipData().map(TooltipComponent::of).map(TooltipComponent::isSticky).orElse(false);
    }

    protected List<Text> getTooltipFromItem(ItemStack stack) {
        return HandledScreen.getTooltipFromItem(this.client, stack);
    }

    private void drawItem(DrawContext context, ItemStack stack, int x, int y, @Nullable String amountText) {
        context.drawItem(stack, x, y);
        context.drawStackOverlay(this.textRenderer, stack, x, y - (this.touchDragStack.isEmpty() ? 0 : 8), amountText);
    }

    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, Colors.DARK_GRAY, false);
        context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, Colors.DARK_GRAY, false);
    }

    protected abstract void drawBackground(DrawContext var1, float var2, int var3, int var4);

    protected void drawSlot(DrawContext context, Slot slot) {
        Identifier lv3;
        int k;
        int i = slot.x;
        int j = slot.y;
        ItemStack lv = slot.getStack();
        boolean bl = false;
        boolean bl2 = slot == this.touchDragSlotStart && !this.touchDragStack.isEmpty() && !this.touchIsRightClickDrag;
        ItemStack lv2 = ((ScreenHandler)this.handler).getCursorStack();
        String string = null;
        if (slot == this.touchDragSlotStart && !this.touchDragStack.isEmpty() && this.touchIsRightClickDrag && !lv.isEmpty()) {
            lv = lv.copyWithCount(lv.getCount() / 2);
        } else if (this.cursorDragging && this.cursorDragSlots.contains(slot) && !lv2.isEmpty()) {
            if (this.cursorDragSlots.size() == 1) {
                return;
            }
            if (ScreenHandler.canInsertItemIntoSlot(slot, lv2, true) && ((ScreenHandler)this.handler).canInsertIntoSlot(slot)) {
                bl = true;
                k = Math.min(lv2.getMaxCount(), slot.getMaxItemCount(lv2));
                int l = slot.getStack().isEmpty() ? 0 : slot.getStack().getCount();
                int m = ScreenHandler.calculateStackSize(this.cursorDragSlots, this.heldButtonType, lv2) + l;
                if (m > k) {
                    m = k;
                    string = Formatting.YELLOW.toString() + k;
                }
                lv = lv2.copyWithCount(m);
            } else {
                this.cursorDragSlots.remove(slot);
                this.calculateOffset();
            }
        }
        if (lv.isEmpty() && slot.isEnabled() && (lv3 = slot.getBackgroundSprite()) != null) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv3, i, j, 16, 16);
            bl2 = true;
        }
        if (!bl2) {
            if (bl) {
                context.fill(i, j, i + 16, j + 16, -2130706433);
            }
            k = slot.x + slot.y * this.backgroundWidth;
            if (slot.disablesDynamicDisplay()) {
                context.drawItemWithoutEntity(lv, i, j, k);
            } else {
                context.drawItem(lv, i, j, k);
            }
            context.drawStackOverlay(this.textRenderer, lv, i, j, string);
        }
    }

    private void calculateOffset() {
        ItemStack lv = ((ScreenHandler)this.handler).getCursorStack();
        if (lv.isEmpty() || !this.cursorDragging) {
            return;
        }
        if (this.heldButtonType == 2) {
            this.draggedStackRemainder = lv.getMaxCount();
            return;
        }
        this.draggedStackRemainder = lv.getCount();
        for (Slot lv2 : this.cursorDragSlots) {
            ItemStack lv3 = lv2.getStack();
            int i = lv3.isEmpty() ? 0 : lv3.getCount();
            int j = Math.min(lv.getMaxCount(), lv2.getMaxItemCount(lv));
            int k = Math.min(ScreenHandler.calculateStackSize(this.cursorDragSlots, this.heldButtonType, lv) + i, j);
            this.draggedStackRemainder -= k - i;
        }
    }

    @Nullable
    private Slot getSlotAt(double mouseX, double mouseY) {
        for (Slot lv : ((ScreenHandler)this.handler).slots) {
            if (!lv.isEnabled() || !this.isPointOverSlot(lv, mouseX, mouseY)) continue;
            return lv;
        }
        return null;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) {
            return true;
        }
        boolean bl2 = this.client.options.pickItemKey.matchesMouse(click) && this.client.player.isInCreativeMode();
        Slot lv = this.getSlotAt(click.x(), click.y());
        this.doubleClicking = this.lastClickedSlot == lv && doubled;
        this.cancelNextRelease = false;
        if (click.button() == 0 || click.button() == InputUtil.GLFW_MOUSE_BUTTON_RIGHT || bl2) {
            int i = this.x;
            int j = this.y;
            boolean bl3 = this.isClickOutsideBounds(click.x(), click.y(), i, j);
            int k = -1;
            if (lv != null) {
                k = lv.id;
            }
            if (bl3) {
                k = -999;
            }
            if (this.client.options.getTouchscreen().getValue().booleanValue() && bl3 && ((ScreenHandler)this.handler).getCursorStack().isEmpty()) {
                this.close();
                return true;
            }
            if (k != -1) {
                if (this.client.options.getTouchscreen().getValue().booleanValue()) {
                    if (lv != null && lv.hasStack()) {
                        this.touchDragSlotStart = lv;
                        this.touchDragStack = ItemStack.EMPTY;
                        this.touchIsRightClickDrag = click.button() == InputUtil.GLFW_MOUSE_BUTTON_RIGHT;
                    } else {
                        this.touchDragSlotStart = null;
                    }
                } else if (!this.cursorDragging) {
                    if (((ScreenHandler)this.handler).getCursorStack().isEmpty()) {
                        if (bl2) {
                            this.onMouseClick(lv, k, click.button(), SlotActionType.CLONE);
                        } else {
                            boolean bl4 = k != -999 && click.hasShift();
                            SlotActionType lv2 = SlotActionType.PICKUP;
                            if (bl4) {
                                this.quickMovingStack = lv != null && lv.hasStack() ? lv.getStack().copy() : ItemStack.EMPTY;
                                lv2 = SlotActionType.QUICK_MOVE;
                            } else if (k == -999) {
                                lv2 = SlotActionType.THROW;
                            }
                            this.onMouseClick(lv, k, click.button(), lv2);
                        }
                        this.cancelNextRelease = true;
                    } else {
                        this.cursorDragging = true;
                        this.heldButtonCode = click.button();
                        this.cursorDragSlots.clear();
                        if (click.button() == 0) {
                            this.heldButtonType = 0;
                        } else if (click.button() == InputUtil.GLFW_MOUSE_BUTTON_RIGHT) {
                            this.heldButtonType = 1;
                        } else if (bl2) {
                            this.heldButtonType = 2;
                        }
                    }
                }
            }
        } else {
            this.onMouseClick(click);
        }
        this.lastClickedSlot = lv;
        return true;
    }

    private void onMouseClick(Click arg) {
        if (this.focusedSlot != null && ((ScreenHandler)this.handler).getCursorStack().isEmpty()) {
            if (this.client.options.swapHandsKey.matchesMouse(arg)) {
                this.onMouseClick(this.focusedSlot, this.focusedSlot.id, 40, SlotActionType.SWAP);
                return;
            }
            for (int i = 0; i < 9; ++i) {
                if (!this.client.options.hotbarKeys[i].matchesMouse(arg)) continue;
                this.onMouseClick(this.focusedSlot, this.focusedSlot.id, i, SlotActionType.SWAP);
            }
        }
    }

    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top) {
        return mouseX < (double)left || mouseY < (double)top || mouseX >= (double)(left + this.backgroundWidth) || mouseY >= (double)(top + this.backgroundHeight);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        Slot lv = this.getSlotAt(click.x(), click.y());
        ItemStack lv2 = ((ScreenHandler)this.handler).getCursorStack();
        if (this.touchDragSlotStart != null && this.client.options.getTouchscreen().getValue().booleanValue()) {
            if (click.button() == 0 || click.button() == InputUtil.GLFW_MOUSE_BUTTON_RIGHT) {
                if (this.touchDragStack.isEmpty()) {
                    if (lv != this.touchDragSlotStart && !this.touchDragSlotStart.getStack().isEmpty()) {
                        this.touchDragStack = this.touchDragSlotStart.getStack().copy();
                    }
                } else if (this.touchDragStack.getCount() > 1 && lv != null && ScreenHandler.canInsertItemIntoSlot(lv, this.touchDragStack, false)) {
                    long l = Util.getMeasuringTimeMs();
                    if (this.touchHoveredSlot == lv) {
                        if (l - this.touchDropTimer > 500L) {
                            this.onMouseClick(this.touchDragSlotStart, this.touchDragSlotStart.id, 0, SlotActionType.PICKUP);
                            this.onMouseClick(lv, lv.id, 1, SlotActionType.PICKUP);
                            this.onMouseClick(this.touchDragSlotStart, this.touchDragSlotStart.id, 0, SlotActionType.PICKUP);
                            this.touchDropTimer = l + 750L;
                            this.touchDragStack.decrement(1);
                        }
                    } else {
                        this.touchHoveredSlot = lv;
                        this.touchDropTimer = l;
                    }
                }
            }
            return true;
        }
        if (this.cursorDragging && lv != null && !lv2.isEmpty() && (lv2.getCount() > this.cursorDragSlots.size() || this.heldButtonType == 2) && ScreenHandler.canInsertItemIntoSlot(lv, lv2, true) && lv.canInsert(lv2) && ((ScreenHandler)this.handler).canInsertIntoSlot(lv)) {
            this.cursorDragSlots.add(lv);
            this.calculateOffset();
            return true;
        }
        if (lv == null && ((ScreenHandler)this.handler).getCursorStack().isEmpty()) {
            return super.mouseDragged(click, offsetX, offsetY);
        }
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        Slot lv = this.getSlotAt(click.x(), click.y());
        int i = this.x;
        int j = this.y;
        boolean bl = this.isClickOutsideBounds(click.x(), click.y(), i, j);
        int k = -1;
        if (lv != null) {
            k = lv.id;
        }
        if (bl) {
            k = -999;
        }
        if (this.doubleClicking && lv != null && click.button() == 0 && ((ScreenHandler)this.handler).canInsertIntoSlot(ItemStack.EMPTY, lv)) {
            if (click.hasShift()) {
                if (!this.quickMovingStack.isEmpty()) {
                    for (Slot lv2 : ((ScreenHandler)this.handler).slots) {
                        if (lv2 == null || !lv2.canTakeItems(this.client.player) || !lv2.hasStack() || lv2.inventory != lv.inventory || !ScreenHandler.canInsertItemIntoSlot(lv2, this.quickMovingStack, true)) continue;
                        this.onMouseClick(lv2, lv2.id, click.button(), SlotActionType.QUICK_MOVE);
                    }
                }
            } else {
                this.onMouseClick(lv, k, click.button(), SlotActionType.PICKUP_ALL);
            }
            this.doubleClicking = false;
        } else {
            if (this.cursorDragging && this.heldButtonCode != click.button()) {
                this.cursorDragging = false;
                this.cursorDragSlots.clear();
                this.cancelNextRelease = true;
                return true;
            }
            if (this.cancelNextRelease) {
                this.cancelNextRelease = false;
                return true;
            }
            if (this.touchDragSlotStart != null && this.client.options.getTouchscreen().getValue().booleanValue()) {
                if (click.button() == 0 || click.button() == InputUtil.GLFW_MOUSE_BUTTON_RIGHT) {
                    if (this.touchDragStack.isEmpty() && lv != this.touchDragSlotStart) {
                        this.touchDragStack = this.touchDragSlotStart.getStack();
                    }
                    boolean bl2 = ScreenHandler.canInsertItemIntoSlot(lv, this.touchDragStack, false);
                    if (k != -1 && !this.touchDragStack.isEmpty() && bl2) {
                        this.onMouseClick(this.touchDragSlotStart, this.touchDragSlotStart.id, click.button(), SlotActionType.PICKUP);
                        this.onMouseClick(lv, k, 0, SlotActionType.PICKUP);
                        if (((ScreenHandler)this.handler).getCursorStack().isEmpty()) {
                            this.letGoTouchStack = null;
                        } else {
                            this.onMouseClick(this.touchDragSlotStart, this.touchDragSlotStart.id, click.button(), SlotActionType.PICKUP);
                            this.letGoTouchStack = new LetGoTouchStack(this.touchDragStack, new Vector2i((int)click.x(), (int)click.y()), new Vector2i(this.touchDragSlotStart.x + i, this.touchDragSlotStart.y + j), Util.getMeasuringTimeMs());
                        }
                    } else if (!this.touchDragStack.isEmpty()) {
                        this.letGoTouchStack = new LetGoTouchStack(this.touchDragStack, new Vector2i((int)click.x(), (int)click.y()), new Vector2i(this.touchDragSlotStart.x + i, this.touchDragSlotStart.y + j), Util.getMeasuringTimeMs());
                    }
                    this.endTouchDrag();
                }
            } else if (this.cursorDragging && !this.cursorDragSlots.isEmpty()) {
                this.onMouseClick(null, -999, ScreenHandler.packQuickCraftData(0, this.heldButtonType), SlotActionType.QUICK_CRAFT);
                for (Slot lv2 : this.cursorDragSlots) {
                    this.onMouseClick(lv2, lv2.id, ScreenHandler.packQuickCraftData(1, this.heldButtonType), SlotActionType.QUICK_CRAFT);
                }
                this.onMouseClick(null, -999, ScreenHandler.packQuickCraftData(2, this.heldButtonType), SlotActionType.QUICK_CRAFT);
            } else if (!((ScreenHandler)this.handler).getCursorStack().isEmpty()) {
                if (this.client.options.pickItemKey.matchesMouse(click)) {
                    this.onMouseClick(lv, k, click.button(), SlotActionType.CLONE);
                } else {
                    boolean bl2;
                    boolean bl3 = bl2 = k != -999 && click.hasShift();
                    if (bl2) {
                        this.quickMovingStack = lv != null && lv.hasStack() ? lv.getStack().copy() : ItemStack.EMPTY;
                    }
                    this.onMouseClick(lv, k, click.button(), bl2 ? SlotActionType.QUICK_MOVE : SlotActionType.PICKUP);
                }
            }
        }
        this.cursorDragging = false;
        return true;
    }

    public void endTouchDrag() {
        this.touchDragStack = ItemStack.EMPTY;
        this.touchDragSlotStart = null;
    }

    private boolean isPointOverSlot(Slot slot, double pointX, double pointY) {
        return this.isPointWithinBounds(slot.x, slot.y, 16, 16, pointX, pointY);
    }

    protected boolean isPointWithinBounds(int x, int y, int width, int height, double pointX, double pointY) {
        int m = this.x;
        int n = this.y;
        return (pointX -= (double)m) >= (double)(x - 1) && pointX < (double)(x + width + 1) && (pointY -= (double)n) >= (double)(y - 1) && pointY < (double)(y + height + 1);
    }

    private void resetTooltipSubmenus(Slot slot) {
        if (slot.hasStack()) {
            for (TooltipSubmenuHandler lv : this.tooltipSubmenuHandlers) {
                if (!lv.isApplicableTo(slot)) continue;
                lv.reset(slot);
            }
        }
    }

    protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
        if (slot != null) {
            slotId = slot.id;
        }
        this.onMouseClick(slot, actionType);
        this.client.interactionManager.clickSlot(((ScreenHandler)this.handler).syncId, slotId, button, actionType, this.client.player);
    }

    void onMouseClick(@Nullable Slot slot, SlotActionType actionType) {
        if (slot != null && slot.hasStack()) {
            for (TooltipSubmenuHandler lv : this.tooltipSubmenuHandlers) {
                if (!lv.isApplicableTo(slot)) continue;
                lv.onMouseClick(slot, actionType);
            }
        }
    }

    protected void onSlotChangedState(int slotId, int handlerId, boolean newState) {
        this.client.interactionManager.slotChangedState(slotId, handlerId, newState);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (super.keyPressed(input)) {
            return true;
        }
        if (this.client.options.inventoryKey.matchesKey(input)) {
            this.close();
            return true;
        }
        this.handleHotbarKeyPressed(input);
        if (this.focusedSlot != null && this.focusedSlot.hasStack()) {
            if (this.client.options.pickItemKey.matchesKey(input)) {
                this.onMouseClick(this.focusedSlot, this.focusedSlot.id, 0, SlotActionType.CLONE);
            } else if (this.client.options.dropKey.matchesKey(input)) {
                this.onMouseClick(this.focusedSlot, this.focusedSlot.id, input.hasCtrl() ? 1 : 0, SlotActionType.THROW);
            }
        }
        return true;
    }

    protected boolean handleHotbarKeyPressed(KeyInput arg) {
        if (((ScreenHandler)this.handler).getCursorStack().isEmpty() && this.focusedSlot != null) {
            if (this.client.options.swapHandsKey.matchesKey(arg)) {
                this.onMouseClick(this.focusedSlot, this.focusedSlot.id, 40, SlotActionType.SWAP);
                return true;
            }
            for (int i = 0; i < 9; ++i) {
                if (!this.client.options.hotbarKeys[i].matchesKey(arg)) continue;
                this.onMouseClick(this.focusedSlot, this.focusedSlot.id, i, SlotActionType.SWAP);
                return true;
            }
        }
        return false;
    }

    @Override
    public void removed() {
        if (this.client.player == null) {
            return;
        }
        ((ScreenHandler)this.handler).onClosed(this.client.player);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean deferSubtitles() {
        return true;
    }

    @Override
    public final void tick() {
        super.tick();
        if (!this.client.player.isAlive() || this.client.player.isRemoved()) {
            this.client.player.closeHandledScreen();
        } else {
            this.handledScreenTick();
        }
    }

    protected void handledScreenTick() {
    }

    @Override
    public T getScreenHandler() {
        return this.handler;
    }

    @Override
    public void close() {
        this.client.player.closeHandledScreen();
        if (this.focusedSlot != null) {
            this.resetTooltipSubmenus(this.focusedSlot);
        }
        super.close();
    }

    @Environment(value=EnvType.CLIENT)
    record LetGoTouchStack(ItemStack item, Vector2i start, Vector2i end, long time) {
    }
}

