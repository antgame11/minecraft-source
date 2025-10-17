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
import net.minecraft.client.gui.hud.debug.PlayerPositionDebugHudEntry;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class PlayerSectionPositionDebugHudEntry
implements DebugHudEntry {
    @Override
    public void render(DebugHudLines lines, @Nullable World world, @Nullable WorldChunk clientChunk, @Nullable WorldChunk chunk) {
        MinecraftClient lv = MinecraftClient.getInstance();
        Entity lv2 = lv.getCameraEntity();
        if (lv2 == null) {
            return;
        }
        BlockPos lv3 = lv.getCameraEntity().getBlockPos();
        lines.addLineToSection(PlayerPositionDebugHudEntry.SECTION_ID, String.format(Locale.ROOT, "Section-relative: %02d %02d %02d", lv3.getX() & 0xF, lv3.getY() & 0xF, lv3.getZ() & 0xF));
    }

    @Override
    public boolean canShow(boolean reducedDebugInfo) {
        return true;
    }
}

