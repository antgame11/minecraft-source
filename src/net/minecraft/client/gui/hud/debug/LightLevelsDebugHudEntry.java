/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.hud.debug;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.client.gui.hud.debug.DebugHudLines;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class LightLevelsDebugHudEntry
implements DebugHudEntry {
    public static final Identifier SECTION_ID = Identifier.ofVanilla("light");

    @Override
    public void render(DebugHudLines lines, @Nullable World world, @Nullable WorldChunk clientChunk, @Nullable WorldChunk chunk) {
        MinecraftClient lv = MinecraftClient.getInstance();
        Entity lv2 = lv.getCameraEntity();
        if (lv2 == null || lv.world == null) {
            return;
        }
        BlockPos lv3 = lv2.getBlockPos();
        int i = lv.world.getChunkManager().getLightingProvider().getLight(lv3, 0);
        int j = lv.world.getLightLevel(LightType.SKY, lv3);
        int k = lv.world.getLightLevel(LightType.BLOCK, lv3);
        String string = "Client Light: " + i + " (" + j + " sky, " + k + " block)";
        if (SharedConstants.SHOW_SERVER_DEBUG_VALUES) {
            Object string2;
            if (chunk != null) {
                LightingProvider lv4 = chunk.getWorld().getLightingProvider();
                string2 = "Server Light: (" + lv4.get(LightType.SKY).getLightLevel(lv3) + " sky, " + lv4.get(LightType.BLOCK).getLightLevel(lv3) + " block)";
            } else {
                string2 = "Server Light: (?? sky, ?? block)";
            }
            lines.addLinesToSection(SECTION_ID, List.of(string, string2));
        } else {
            lines.addLineToSection(SECTION_ID, string);
        }
    }
}

