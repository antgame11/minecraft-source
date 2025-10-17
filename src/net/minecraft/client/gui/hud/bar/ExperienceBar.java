/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.hud.bar;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.bar.Bar;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class ExperienceBar
implements Bar {
    private static final Identifier BACKGROUND = Identifier.ofVanilla("hud/experience_bar_background");
    private static final Identifier PROGRESS = Identifier.ofVanilla("hud/experience_bar_progress");
    private final MinecraftClient client;

    public ExperienceBar(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void renderBar(DrawContext context, RenderTickCounter tickCounter) {
        ClientPlayerEntity lv = this.client.player;
        int i = this.getCenterX(this.client.getWindow());
        int j = this.getCenterY(this.client.getWindow());
        int k = lv.getNextLevelExperience();
        if (k > 0) {
            int l = (int)(lv.experienceProgress * 183.0f);
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND, i, j, 182, 5);
            if (l > 0) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, PROGRESS, 182, 5, 0, 0, i, j, l, 5);
            }
        }
    }

    @Override
    public void renderAddons(DrawContext context, RenderTickCounter tickCounter) {
    }
}

