/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.hud.debug;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.client.gui.hud.debug.DebugHudLines;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
class GameVersionDebugHudEntry
implements DebugHudEntry {
    GameVersionDebugHudEntry() {
    }

    @Override
    public void render(DebugHudLines lines, @Nullable World world, @Nullable WorldChunk clientChunk, @Nullable WorldChunk chunk) {
        lines.addPriorityLine("Minecraft " + SharedConstants.getGameVersion().name() + " (" + MinecraftClient.getInstance().getGameVersion() + "/" + ClientBrandRetriever.getClientModName() + ")");
    }

    @Override
    public boolean canShow(boolean reducedDebugInfo) {
        return true;
    }
}

