/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Urls;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class DemoScreen
extends Screen {
    private static final Identifier DEMO_BG = Identifier.ofVanilla("textures/gui/demo_background.png");
    private static final int DEMO_BG_WIDTH = 256;
    private static final int DEMO_BG_HEIGHT = 256;
    private MultilineText movementText = MultilineText.EMPTY;
    private MultilineText fullWrappedText = MultilineText.EMPTY;

    public DemoScreen() {
        super(Text.translatable("demo.help.title"));
    }

    @Override
    protected void init() {
        int i = -16;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("demo.help.buy"), button -> {
            button.active = false;
            Util.getOperatingSystem().open(Urls.BUY_JAVA);
        }).dimensions(this.width / 2 - 116, this.height / 2 + 62 + -16, 114, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("demo.help.later"), button -> {
            this.client.setScreen(null);
            this.client.mouse.lockCursor();
        }).dimensions(this.width / 2 + 2, this.height / 2 + 62 + -16, 114, 20).build());
        GameOptions lv = this.client.options;
        this.movementText = MultilineText.create(this.textRenderer, Text.translatable("demo.help.movementShort", lv.forwardKey.getBoundKeyLocalizedText(), lv.leftKey.getBoundKeyLocalizedText(), lv.backKey.getBoundKeyLocalizedText(), lv.rightKey.getBoundKeyLocalizedText()), Text.translatable("demo.help.movementMouse"), Text.translatable("demo.help.jump", lv.jumpKey.getBoundKeyLocalizedText()), Text.translatable("demo.help.inventory", lv.inventoryKey.getBoundKeyLocalizedText()));
        this.fullWrappedText = MultilineText.create(this.textRenderer, (Text)Text.translatable("demo.help.fullWrapped"), 218);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.renderBackground(context, mouseX, mouseY, deltaTicks);
        int k = (this.width - 248) / 2;
        int l = (this.height - 166) / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, DEMO_BG, k, l, 0.0f, 0.0f, 248, 166, 256, 256);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        int k = (this.width - 248) / 2 + 10;
        int l = (this.height - 166) / 2 + 8;
        context.drawText(this.textRenderer, this.title, k, l, -14737633, false);
        l = this.movementText.draw(context, MultilineText.Alignment.LEFT, k, l + 12, 12, false, -11579569);
        this.fullWrappedText.draw(context, MultilineText.Alignment.LEFT, k, l + 20, this.textRenderer.fontHeight, false, -14737633);
    }
}

