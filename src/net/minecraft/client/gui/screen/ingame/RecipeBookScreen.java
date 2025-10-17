/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.ingame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

@Environment(value=EnvType.CLIENT)
public abstract class RecipeBookScreen<T extends AbstractRecipeScreenHandler>
extends HandledScreen<T>
implements RecipeBookProvider {
    private final RecipeBookWidget<?> recipeBook;
    private boolean narrow;

    public RecipeBookScreen(T handler, RecipeBookWidget<?> recipeBook, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.recipeBook = recipeBook;
    }

    @Override
    protected void init() {
        super.init();
        this.narrow = this.width < 379;
        this.recipeBook.initialize(this.width, this.height, this.client, this.narrow);
        this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);
        this.addRecipeBook();
    }

    protected abstract ScreenPos getRecipeBookButtonPos();

    private void addRecipeBook() {
        ScreenPos lv = this.getRecipeBookButtonPos();
        this.addDrawableChild(new TexturedButtonWidget(lv.x(), lv.y(), 20, 18, RecipeBookWidget.BUTTON_TEXTURES, button -> {
            this.recipeBook.toggleOpen();
            this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);
            ScreenPos lv = this.getRecipeBookButtonPos();
            button.setPosition(lv.x(), lv.y());
            this.onRecipeBookToggled();
        }));
        this.addSelectableChild(this.recipeBook);
    }

    protected void onRecipeBookToggled() {
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (this.recipeBook.isOpen() && this.narrow) {
            this.renderBackground(context, mouseX, mouseY, deltaTicks);
        } else {
            super.renderMain(context, mouseX, mouseY, deltaTicks);
        }
        context.createNewRootLayer();
        this.recipeBook.render(context, mouseX, mouseY, deltaTicks);
        context.createNewRootLayer();
        this.renderCursorStack(context, mouseX, mouseY);
        this.renderLetGoTouchStack(context);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
        this.recipeBook.drawTooltip(context, mouseX, mouseY, this.focusedSlot);
    }

    @Override
    protected void drawSlots(DrawContext context) {
        super.drawSlots(context);
        this.recipeBook.drawGhostSlots(context, this.shouldAddPaddingToGhostResult());
    }

    protected boolean shouldAddPaddingToGhostResult() {
        return true;
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (this.recipeBook.charTyped(input)) {
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (this.recipeBook.keyPressed(input)) {
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (this.recipeBook.mouseClicked(click, doubled)) {
            this.setFocused(this.recipeBook);
            return true;
        }
        if (this.narrow && this.recipeBook.isOpen()) {
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (this.recipeBook.mouseDragged(click, offsetX, offsetY)) {
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    protected boolean isPointWithinBounds(int x, int y, int width, int height, double pointX, double pointY) {
        return (!this.narrow || !this.recipeBook.isOpen()) && super.isPointWithinBounds(x, y, width, height, pointX, pointY);
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top) {
        boolean bl = mouseX < (double)left || mouseY < (double)top || mouseX >= (double)(left + this.backgroundWidth) || mouseY >= (double)(top + this.backgroundHeight);
        return this.recipeBook.isClickOutsideBounds(mouseX, mouseY, this.x, this.y, this.backgroundWidth, this.backgroundHeight) && bl;
    }

    @Override
    protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
        super.onMouseClick(slot, slotId, button, actionType);
        this.recipeBook.onMouseClick(slot);
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        this.recipeBook.update();
    }

    @Override
    public void refreshRecipeBook() {
        this.recipeBook.refresh();
    }

    @Override
    public void onCraftFailed(RecipeDisplay display) {
        this.recipeBook.onCraftFailed(display);
    }
}

