/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.hud.debug;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.client.gui.hud.debug.DebugHudLines;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class EntitySpawnCountsDebugHudEntry
implements DebugHudEntry {
    @Override
    public void render(DebugHudLines lines, @Nullable World world, @Nullable WorldChunk clientChunk, @Nullable WorldChunk chunk) {
        ServerWorld lv3;
        MinecraftClient lv = MinecraftClient.getInstance();
        Entity lv2 = lv.getCameraEntity();
        ServerWorld serverWorld = lv3 = world instanceof ServerWorld ? (ServerWorld)world : null;
        if (lv2 == null || lv3 == null) {
            return;
        }
        ServerChunkManager lv4 = lv3.getChunkManager();
        SpawnHelper.Info lv5 = lv4.getSpawnInfo();
        if (lv5 != null) {
            Object2IntMap<SpawnGroup> object2IntMap = lv5.getGroupToCount();
            int i = lv5.getSpawningChunkCount();
            lines.addLine("SC: " + i + ", " + Stream.of(SpawnGroup.values()).map(spawnGroup -> Character.toUpperCase(spawnGroup.getName().charAt(0)) + ": " + object2IntMap.getInt(spawnGroup)).collect(Collectors.joining(", ")));
        }
    }
}

