/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.hud.debug;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.client.gui.hud.debug.DebugHudLines;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class HeightmapDebugHudEntry
implements DebugHudEntry {
    private static final Map<Heightmap.Type, String> HEIGHTMAP_TYPE_TO_STRING = Maps.newEnumMap(Map.of(Heightmap.Type.WORLD_SURFACE_WG, "SW", Heightmap.Type.WORLD_SURFACE, "S", Heightmap.Type.OCEAN_FLOOR_WG, "OW", Heightmap.Type.OCEAN_FLOOR, "O", Heightmap.Type.MOTION_BLOCKING, "M", Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, "ML"));
    private static final Identifier SECTION_ID = Identifier.ofVanilla("heightmaps");

    @Override
    public void render(DebugHudLines lines, @Nullable World world, @Nullable WorldChunk clientChunk, @Nullable WorldChunk chunk) {
        MinecraftClient lv = MinecraftClient.getInstance();
        Entity lv2 = lv.getCameraEntity();
        if (lv2 == null || lv.world == null || clientChunk == null) {
            return;
        }
        BlockPos lv3 = lv2.getBlockPos();
        ArrayList<String> list = new ArrayList<String>();
        StringBuilder stringBuilder = new StringBuilder("CH");
        for (Heightmap.Type lv4 : Heightmap.Type.values()) {
            if (!lv4.shouldSendToClient()) continue;
            stringBuilder.append(" ").append(HEIGHTMAP_TYPE_TO_STRING.get(lv4)).append(": ").append(clientChunk.sampleHeightmap(lv4, lv3.getX(), lv3.getZ()));
        }
        list.add(stringBuilder.toString());
        stringBuilder.setLength(0);
        stringBuilder.append("SH");
        for (Heightmap.Type lv4 : Heightmap.Type.values()) {
            if (!lv4.isStoredServerSide()) continue;
            stringBuilder.append(" ").append(HEIGHTMAP_TYPE_TO_STRING.get(lv4)).append(": ");
            if (chunk != null) {
                stringBuilder.append(chunk.sampleHeightmap(lv4, lv3.getX(), lv3.getZ()));
                continue;
            }
            stringBuilder.append("??");
        }
        list.add(stringBuilder.toString());
        lines.addLinesToSection(SECTION_ID, list);
    }
}

