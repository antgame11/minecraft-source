/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.hud.debug;

import java.util.ArrayList;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.client.gui.hud.debug.DebugHudLines;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class LookingAtBlockDebugHudEntry
implements DebugHudEntry {
    private static final Identifier SECTION_ID = Identifier.ofVanilla("looking_at_block");

    @Override
    public void render(DebugHudLines lines, @Nullable World world, @Nullable WorldChunk clientChunk, @Nullable WorldChunk chunk) {
        World lv2;
        Entity lv = MinecraftClient.getInstance().getCameraEntity();
        World world2 = lv2 = SharedConstants.SHOW_SERVER_DEBUG_VALUES ? world : MinecraftClient.getInstance().world;
        if (lv == null || lv2 == null) {
            return;
        }
        HitResult lv3 = lv.raycast(20.0, 0.0f, false);
        ArrayList<String> list = new ArrayList<String>();
        if (lv3.getType() == HitResult.Type.BLOCK) {
            BlockPos lv4 = ((BlockHitResult)lv3).getBlockPos();
            BlockState lv5 = lv2.getBlockState(lv4);
            list.add(String.valueOf(Formatting.UNDERLINE) + "Targeted Block: " + lv4.getX() + ", " + lv4.getY() + ", " + lv4.getZ());
            list.add(String.valueOf(Registries.BLOCK.getId(lv5.getBlock())));
            for (Map.Entry<Property<?>, Comparable<?>> entry : lv5.getEntries().entrySet()) {
                list.add(this.getBlockPropertyLine(entry));
            }
            lv5.streamTags().map(tag -> "#" + String.valueOf(tag.id())).forEach(list::add);
        }
        lines.addLinesToSection(SECTION_ID, list);
    }

    private String getBlockPropertyLine(Map.Entry<Property<?>, Comparable<?>> propertyAndValue) {
        Property<?> lv = propertyAndValue.getKey();
        Comparable<?> comparable = propertyAndValue.getValue();
        Object string = Util.getValueAsString(lv, comparable);
        if (Boolean.TRUE.equals(comparable)) {
            string = String.valueOf(Formatting.GREEN) + (String)string;
        } else if (Boolean.FALSE.equals(comparable)) {
            string = String.valueOf(Formatting.RED) + (String)string;
        }
        return lv.getName() + ": " + (String)string;
    }
}

