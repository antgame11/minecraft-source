/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.hud.debug;

import it.unimi.dsi.fastutil.longs.LongSets;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.client.gui.hud.debug.DebugHudLines;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class PlayerPositionDebugHudEntry
implements DebugHudEntry {
    public static final Identifier SECTION_ID = Identifier.ofVanilla("position");

    @Override
    public void render(DebugHudLines lines, @Nullable World world, @Nullable WorldChunk clientChunk, @Nullable WorldChunk chunk) {
        MinecraftClient lv = MinecraftClient.getInstance();
        Entity lv2 = lv.getCameraEntity();
        if (lv2 == null) {
            return;
        }
        BlockPos lv3 = lv.getCameraEntity().getBlockPos();
        ChunkPos lv4 = new ChunkPos(lv3);
        Direction lv5 = lv2.getHorizontalFacing();
        String string = switch (lv5) {
            case Direction.NORTH -> "Towards negative Z";
            case Direction.SOUTH -> "Towards positive Z";
            case Direction.WEST -> "Towards negative X";
            case Direction.EAST -> "Towards positive X";
            default -> "Invalid";
        };
        LongSets.EmptySet longSet = world instanceof ServerWorld ? ((ServerWorld)world).getForcedChunks() : LongSets.EMPTY_SET;
        lines.addLinesToSection(SECTION_ID, List.of(String.format(Locale.ROOT, "XYZ: %.3f / %.5f / %.3f", lv.getCameraEntity().getX(), lv.getCameraEntity().getY(), lv.getCameraEntity().getZ()), String.format(Locale.ROOT, "Block: %d %d %d", lv3.getX(), lv3.getY(), lv3.getZ()), String.format(Locale.ROOT, "Chunk: %d %d %d [%d %d in r.%d.%d.mca]", lv4.x, ChunkSectionPos.getSectionCoord(lv3.getY()), lv4.z, lv4.getRegionRelativeX(), lv4.getRegionRelativeZ(), lv4.getRegionX(), lv4.getRegionZ()), String.format(Locale.ROOT, "Facing: %s (%s) (%.1f / %.1f)", lv5, string, Float.valueOf(MathHelper.wrapDegrees(lv2.getYaw())), Float.valueOf(MathHelper.wrapDegrees(lv2.getPitch()))), String.valueOf(lv.world.getRegistryKey().getValue()) + " FC: " + longSet.size()));
    }
}

