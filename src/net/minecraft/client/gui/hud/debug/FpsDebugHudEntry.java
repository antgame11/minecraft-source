/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.hud.debug;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.client.gui.hud.debug.DebugHudLines;
import net.minecraft.client.option.GameOptions;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class FpsDebugHudEntry
implements DebugHudEntry {
    @Override
    public void render(DebugHudLines lines, @Nullable World world, @Nullable WorldChunk clientChunk, @Nullable WorldChunk chunk) {
        MinecraftClient lv = MinecraftClient.getInstance();
        int i = lv.getInactivityFpsLimiter().update();
        GameOptions lv2 = lv.options;
        lines.addPriorityLine(String.format(Locale.ROOT, "%d fps T: %s%s", lv.getCurrentFps(), i == 260 ? "inf" : Integer.valueOf(i), lv2.getEnableVsync().getValue() != false ? " vsync" : ""));
    }

    @Override
    public boolean canShow(boolean reducedDebugInfo) {
        return true;
    }
}

