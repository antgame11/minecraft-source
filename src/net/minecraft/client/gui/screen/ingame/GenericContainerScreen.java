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
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class GenericContainerScreen
extends HandledScreen<GenericContainerScreenHandler> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/generic_54.png");
    private final int rows;

    public GenericContainerScreen(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        int i = 222;
        int j = 114;
        this.rows = handler.getRows();
        this.backgroundHeight = 114 + this.rows * 18;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        int k = (this.width - this.backgroundWidth) / 2;
        int l = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, k, l, 0.0f, 0.0f, this.backgroundWidth, this.rows * 18 + 17, 256, 256);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, k, l + this.rows * 18 + 17, 0.0f, 126.0f, this.backgroundWidth, 96, 256, 256);
    }
}

