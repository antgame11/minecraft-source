/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.tooltip;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.texture.PlayerSkinCache;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.util.Colors;

@Environment(value=EnvType.CLIENT)
public class ProfilesTooltipComponent
implements TooltipComponent {
    private static final int field_52140 = 10;
    private static final int field_52141 = 2;
    private final List<PlayerSkinCache.Entry> profiles;

    public ProfilesTooltipComponent(ProfilesData data) {
        this.profiles = data.profiles();
    }

    @Override
    public int getHeight(TextRenderer textRenderer) {
        return this.profiles.size() * 12 + 2;
    }

    private static String getName(PlayerSkinCache.Entry entry) {
        return entry.getProfile().name();
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        int i = 0;
        for (PlayerSkinCache.Entry lv : this.profiles) {
            int j = textRenderer.getWidth(ProfilesTooltipComponent.getName(lv));
            if (j <= i) continue;
            i = j;
        }
        return i + 10 + 6;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, int width, int height, DrawContext context) {
        for (int m = 0; m < this.profiles.size(); ++m) {
            PlayerSkinCache.Entry lv = this.profiles.get(m);
            int n = y + 2 + m * 12;
            PlayerSkinDrawer.draw(context, lv.getTextures(), x + 2, n, 10);
            context.drawTextWithShadow(textRenderer, ProfilesTooltipComponent.getName(lv), x + 10 + 4, n + 2, Colors.WHITE);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record ProfilesData(List<PlayerSkinCache.Entry> profiles) implements TooltipData
    {
    }
}

