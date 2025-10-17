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
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BiomeDebugHudEntry
implements DebugHudEntry {
    private static final Identifier SECTION_ID = Identifier.ofVanilla("biome");

    @Override
    public void render(DebugHudLines lines, @Nullable World world, @Nullable WorldChunk clientChunk, @Nullable WorldChunk chunk) {
        MinecraftClient lv = MinecraftClient.getInstance();
        Entity lv2 = lv.getCameraEntity();
        if (lv2 == null || lv.world == null) {
            return;
        }
        BlockPos lv3 = lv2.getBlockPos();
        if (lv.world.isInHeightLimit(lv3.getY())) {
            if (SharedConstants.SHOW_SERVER_DEBUG_VALUES && world instanceof ServerWorld) {
                lines.addLinesToSection(SECTION_ID, List.of("Biome: " + BiomeDebugHudEntry.getBiomeAsString(lv.world.getBiome(lv3)), "Server Biome: " + BiomeDebugHudEntry.getBiomeAsString(world.getBiome(lv3))));
            } else {
                lines.addLine("Biome: " + BiomeDebugHudEntry.getBiomeAsString(lv.world.getBiome(lv3)));
            }
        }
    }

    private static String getBiomeAsString(RegistryEntry<Biome> biome) {
        return biome.getKeyOrValue().map(key -> key.getValue().toString(), value -> "[unregistered " + String.valueOf(value) + "]");
    }
}

