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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.recipe.display.CuttingRecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.recipe.display.SlotDisplayContexts;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.context.ContextParameterMap;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class StonecutterScreen
extends HandledScreen<StonecutterScreenHandler> {
    private static final Identifier SCROLLER_TEXTURE = Identifier.ofVanilla("container/stonecutter/scroller");
    private static final Identifier SCROLLER_DISABLED_TEXTURE = Identifier.ofVanilla("container/stonecutter/scroller_disabled");
    private static final Identifier RECIPE_SELECTED_TEXTURE = Identifier.ofVanilla("container/stonecutter/recipe_selected");
    private static final Identifier RECIPE_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("container/stonecutter/recipe_highlighted");
    private static final Identifier RECIPE_TEXTURE = Identifier.ofVanilla("container/stonecutter/recipe");
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/stonecutter.png");
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int SCROLLBAR_HEIGHT = 15;
    private static final int RECIPE_LIST_COLUMNS = 4;
    private static final int RECIPE_LIST_ROWS = 3;
    private static final int RECIPE_ENTRY_WIDTH = 16;
    private static final int RECIPE_ENTRY_HEIGHT = 18;
    private static final int SCROLLBAR_AREA_HEIGHT = 54;
    private static final int RECIPE_LIST_OFFSET_X = 52;
    private static final int RECIPE_LIST_OFFSET_Y = 14;
    private float scrollAmount;
    private boolean mouseClicked;
    private int scrollOffset;
    private boolean canCraft;

    public StonecutterScreen(StonecutterScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        handler.setContentsChangedListener(this::onInventoryChange);
        --this.titleY;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        int k = this.x;
        int l = this.y;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, k, l, 0.0f, 0.0f, this.backgroundWidth, this.backgroundHeight, 256, 256);
        int m = (int)(41.0f * this.scrollAmount);
        Identifier lv = this.shouldScroll() ? SCROLLER_TEXTURE : SCROLLER_DISABLED_TEXTURE;
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv, k + 119, l + 15 + m, 12, 15);
        int n = this.x + 52;
        int o = this.y + 14;
        int p = this.scrollOffset + 12;
        this.renderRecipeBackground(context, mouseX, mouseY, n, o, p);
        this.renderRecipeIcons(context, n, o, p);
    }

    @Override
    protected void drawMouseoverTooltip(DrawContext context, int x, int y) {
        super.drawMouseoverTooltip(context, x, y);
        if (this.canCraft) {
            int k = this.x + 52;
            int l = this.y + 14;
            int m = this.scrollOffset + 12;
            CuttingRecipeDisplay.Grouping<StonecuttingRecipe> lv = ((StonecutterScreenHandler)this.handler).getAvailableRecipes();
            for (int n = this.scrollOffset; n < m && n < lv.size(); ++n) {
                int o = n - this.scrollOffset;
                int p = k + o % 4 * 16;
                int q = l + o / 4 * 18 + 2;
                if (x < p || x >= p + 16 || y < q || y >= q + 18) continue;
                ContextParameterMap lv2 = SlotDisplayContexts.createParameters(this.client.world);
                SlotDisplay lv3 = lv.entries().get(n).recipe().optionDisplay();
                context.drawItemTooltip(this.textRenderer, lv3.getFirst(lv2), x, y);
            }
        }
    }

    private void renderRecipeBackground(DrawContext context, int mouseX, int mouseY, int x, int y, int scrollOffset) {
        for (int n = this.scrollOffset; n < scrollOffset && n < ((StonecutterScreenHandler)this.handler).getAvailableRecipeCount(); ++n) {
            int o = n - this.scrollOffset;
            int p = x + o % 4 * 16;
            int q = o / 4;
            int r = y + q * 18 + 2;
            Identifier lv = n == ((StonecutterScreenHandler)this.handler).getSelectedRecipe() ? RECIPE_SELECTED_TEXTURE : (mouseX >= p && mouseY >= r && mouseX < p + 16 && mouseY < r + 18 ? RECIPE_HIGHLIGHTED_TEXTURE : RECIPE_TEXTURE);
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv, p, r - 1, 16, 18);
        }
    }

    private void renderRecipeIcons(DrawContext context, int x, int y, int scrollOffset) {
        CuttingRecipeDisplay.Grouping<StonecuttingRecipe> lv = ((StonecutterScreenHandler)this.handler).getAvailableRecipes();
        ContextParameterMap lv2 = SlotDisplayContexts.createParameters(this.client.world);
        for (int l = this.scrollOffset; l < scrollOffset && l < lv.size(); ++l) {
            int m = l - this.scrollOffset;
            int n = x + m % 4 * 16;
            int o = m / 4;
            int p = y + o * 18 + 2;
            SlotDisplay lv3 = lv.entries().get(l).recipe().optionDisplay();
            context.drawItem(lv3.getFirst(lv2), n, p);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        this.mouseClicked = false;
        if (this.canCraft) {
            int i = this.x + 52;
            int j = this.y + 14;
            int k = this.scrollOffset + 12;
            for (int l = this.scrollOffset; l < k; ++l) {
                int m = l - this.scrollOffset;
                double d = click.x() - (double)(i + m % 4 * 16);
                double e = click.y() - (double)(j + m / 4 * 18);
                if (!(d >= 0.0) || !(e >= 0.0) || !(d < 16.0) || !(e < 18.0) || !((StonecutterScreenHandler)this.handler).onButtonClick(this.client.player, l)) continue;
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0f));
                this.client.interactionManager.clickButton(((StonecutterScreenHandler)this.handler).syncId, l);
                return true;
            }
            i = this.x + 119;
            j = this.y + 9;
            if (click.x() >= (double)i && click.x() < (double)(i + 12) && click.y() >= (double)j && click.y() < (double)(j + 54)) {
                this.mouseClicked = true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (this.mouseClicked && this.shouldScroll()) {
            int i = this.y + 14;
            int j = i + 54;
            this.scrollAmount = ((float)click.y() - (float)i - 7.5f) / ((float)(j - i) - 15.0f);
            this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0.0f, 1.0f);
            this.scrollOffset = (int)((double)(this.scrollAmount * (float)this.getMaxScroll()) + 0.5) * 4;
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        if (this.shouldScroll()) {
            int i = this.getMaxScroll();
            float h = (float)verticalAmount / (float)i;
            this.scrollAmount = MathHelper.clamp(this.scrollAmount - h, 0.0f, 1.0f);
            this.scrollOffset = (int)((double)(this.scrollAmount * (float)i) + 0.5) * 4;
        }
        return true;
    }

    private boolean shouldScroll() {
        return this.canCraft && ((StonecutterScreenHandler)this.handler).getAvailableRecipeCount() > 12;
    }

    protected int getMaxScroll() {
        return (((StonecutterScreenHandler)this.handler).getAvailableRecipeCount() + 4 - 1) / 4 - 3;
    }

    private void onInventoryChange() {
        this.canCraft = ((StonecutterScreenHandler)this.handler).canCraft();
        if (!this.canCraft) {
            this.scrollAmount = 0.0f;
            this.scrollOffset = 0;
        }
    }
}

