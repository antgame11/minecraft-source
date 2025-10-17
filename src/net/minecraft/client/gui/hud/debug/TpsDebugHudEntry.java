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
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.ServerTickManager;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.tick.TickManager;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TpsDebugHudEntry
implements DebugHudEntry {
    @Override
    public void render(DebugHudLines lines, @Nullable World world, @Nullable WorldChunk clientChunk, @Nullable WorldChunk chunk) {
        String string3;
        MinecraftClient lv = MinecraftClient.getInstance();
        IntegratedServer lv2 = lv.getServer();
        ClientPlayNetworkHandler lv3 = lv.getNetworkHandler();
        if (lv3 == null || world == null) {
            return;
        }
        ClientConnection lv4 = lv3.getConnection();
        float f = lv4.getAveragePacketsSent();
        float g = lv4.getAveragePacketsReceived();
        TickManager lv5 = world.getTickManager();
        String string = lv5.isStepping() ? " (frozen - stepping)" : (lv5.isFrozen() ? " (frozen)" : "");
        if (lv2 != null) {
            ServerTickManager lv6 = lv2.getTickManager();
            boolean bl = lv6.isSprinting();
            if (bl) {
                string = " (sprinting)";
            }
            String string2 = bl ? "-" : String.format(Locale.ROOT, "%.1f", Float.valueOf(lv5.getMillisPerTick()));
            string3 = String.format(Locale.ROOT, "Integrated server @ %.1f/%s ms%s, %.0f tx, %.0f rx", Float.valueOf(lv2.getAverageTickTime()), string2, string, Float.valueOf(f), Float.valueOf(g));
        } else {
            string3 = String.format(Locale.ROOT, "\"%s\" server%s, %.0f tx, %.0f rx", lv3.getBrand(), string, Float.valueOf(f), Float.valueOf(g));
        }
        lines.addLine(string3);
    }

    @Override
    public boolean canShow(boolean reducedDebugInfo) {
        return true;
    }
}

