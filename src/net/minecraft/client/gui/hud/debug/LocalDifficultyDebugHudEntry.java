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
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class LocalDifficultyDebugHudEntry
implements DebugHudEntry {
    @Override
    public void render(DebugHudLines lines, @Nullable World world, @Nullable WorldChunk clientChunk, @Nullable WorldChunk chunk) {
        MinecraftClient lv = MinecraftClient.getInstance();
        Entity lv2 = lv.getCameraEntity();
        if (lv2 == null || lv.world == null || chunk == null || world == null) {
            return;
        }
        BlockPos lv3 = lv2.getBlockPos();
        if (lv.world.isInHeightLimit(lv3.getY())) {
            float f = world.getMoonSize();
            long l = chunk.getInhabitedTime();
            LocalDifficulty lv4 = new LocalDifficulty(world.getDifficulty(), world.getTimeOfDay(), l, f);
            lines.addLine(String.format(Locale.ROOT, "Local Difficulty: %.2f // %.2f (Day %d)", Float.valueOf(lv4.getLocalDifficulty()), Float.valueOf(lv4.getClampedLocalDifficulty()), lv.world.getTimeOfDay() / 24000L));
        }
    }
}

