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
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SimplePerformanceImpactorsDebugHudEntry
implements DebugHudEntry {
    @Override
    public void render(DebugHudLines lines, @Nullable World world, @Nullable WorldChunk clientChunk, @Nullable WorldChunk chunk) {
        MinecraftClient lv = MinecraftClient.getInstance();
        GameOptions lv2 = lv.options;
        lines.addLine(String.format(Locale.ROOT, "%s%s B: %d", lv2.getGraphicsMode().getValue(), lv2.getCloudRenderMode().getValue() == CloudRenderMode.OFF ? "" : (lv2.getCloudRenderMode().getValue() == CloudRenderMode.FAST ? " fast-clouds" : " fancy-clouds"), lv2.getBiomeBlendRadius().getValue()));
    }

    @Override
    public boolean canShow(boolean reducedDebugInfo) {
        return true;
    }
}

