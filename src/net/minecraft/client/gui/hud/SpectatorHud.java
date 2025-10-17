/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.hud;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.spectator.SpectatorMenu;
import net.minecraft.client.gui.hud.spectator.SpectatorMenuCloseCallback;
import net.minecraft.client.gui.hud.spectator.SpectatorMenuCommand;
import net.minecraft.client.gui.hud.spectator.SpectatorMenuState;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SpectatorHud
implements SpectatorMenuCloseCallback {
    private static final Identifier HOTBAR_TEXTURE = Identifier.ofVanilla("hud/hotbar");
    private static final Identifier HOTBAR_SELECTION_TEXTURE = Identifier.ofVanilla("hud/hotbar_selection");
    private static final long FADE_OUT_DELAY = 5000L;
    private static final long FADE_OUT_DURATION = 2000L;
    private final MinecraftClient client;
    private long lastInteractionTime;
    @Nullable
    private SpectatorMenu spectatorMenu;

    public SpectatorHud(MinecraftClient client) {
        this.client = client;
    }

    public void selectSlot(int slot) {
        this.lastInteractionTime = Util.getMeasuringTimeMs();
        if (this.spectatorMenu != null) {
            this.spectatorMenu.useCommand(slot);
        } else {
            this.spectatorMenu = new SpectatorMenu(this);
        }
    }

    private float getSpectatorMenuHeight() {
        long l = this.lastInteractionTime - Util.getMeasuringTimeMs() + 5000L;
        return MathHelper.clamp((float)l / 2000.0f, 0.0f, 1.0f);
    }

    public void renderSpectatorMenu(DrawContext context) {
        if (this.spectatorMenu == null) {
            return;
        }
        float f = this.getSpectatorMenuHeight();
        if (f <= 0.0f) {
            this.spectatorMenu.close();
            return;
        }
        int i = context.getScaledWindowWidth() / 2;
        int j = MathHelper.floor((float)context.getScaledWindowHeight() - 22.0f * f);
        SpectatorMenuState lv = this.spectatorMenu.getCurrentState();
        this.renderSpectatorMenu(context, f, i, j, lv);
    }

    protected void renderSpectatorMenu(DrawContext context, float height, int x, int y, SpectatorMenuState state) {
        int k = ColorHelper.getWhite(height);
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR_TEXTURE, x - 91, y, 182, 22, k);
        if (state.getSelectedSlot() >= 0) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION_TEXTURE, x - 91 - 1 + state.getSelectedSlot() * 20, y - 1, 24, 23, k);
        }
        for (int l = 0; l < 9; ++l) {
            this.renderSpectatorCommand(context, l, context.getScaledWindowWidth() / 2 - 90 + l * 20 + 2, y + 3, height, state.getCommand(l));
        }
    }

    private void renderSpectatorCommand(DrawContext context, int slot, int x, float y, float height, SpectatorMenuCommand command) {
        if (command != SpectatorMenu.BLANK_COMMAND) {
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(x, y);
            float h = command.isEnabled() ? 1.0f : 0.25f;
            command.renderIcon(context, h, height);
            context.getMatrices().popMatrix();
            if (height > 0.0f && command.isEnabled()) {
                Text lv = this.client.options.hotbarKeys[slot].getBoundKeyLocalizedText();
                context.drawTextWithShadow(this.client.textRenderer, lv, x + 19 - 2 - this.client.textRenderer.getWidth(lv), (int)y + 6 + 3, ColorHelper.withAlpha(height, Colors.WHITE));
            }
        }
    }

    public void render(DrawContext context) {
        float f = this.getSpectatorMenuHeight();
        if (f > 0.0f && this.spectatorMenu != null) {
            SpectatorMenuCommand lv = this.spectatorMenu.getSelectedCommand();
            Text lv2 = lv == SpectatorMenu.BLANK_COMMAND ? this.spectatorMenu.getCurrentGroup().getPrompt() : lv.getName();
            int i = this.client.textRenderer.getWidth(lv2);
            int j = (context.getScaledWindowWidth() - i) / 2;
            int k = context.getScaledWindowHeight() - 35;
            context.drawTextWithBackground(this.client.textRenderer, lv2, j, k, i, ColorHelper.withAlpha(f, Colors.WHITE));
        }
    }

    @Override
    public void close(SpectatorMenu menu) {
        this.spectatorMenu = null;
        this.lastInteractionTime = 0L;
    }

    public boolean isOpen() {
        return this.spectatorMenu != null;
    }

    public void cycleSlot(int offset) {
        int j;
        for (j = this.spectatorMenu.getSelectedSlot() + offset; !(j < 0 || j > 8 || this.spectatorMenu.getCommand(j) != SpectatorMenu.BLANK_COMMAND && this.spectatorMenu.getCommand(j).isEnabled()); j += offset) {
        }
        if (j >= 0 && j <= 8) {
            this.spectatorMenu.useCommand(j);
            this.lastInteractionTime = Util.getMeasuringTimeMs();
        }
    }

    public void useSelectedCommand() {
        this.lastInteractionTime = Util.getMeasuringTimeMs();
        if (this.isOpen()) {
            int i = this.spectatorMenu.getSelectedSlot();
            if (i != -1) {
                this.spectatorMenu.useCommand(i);
            }
        } else {
            this.spectatorMenu = new SpectatorMenu(this);
        }
    }
}

