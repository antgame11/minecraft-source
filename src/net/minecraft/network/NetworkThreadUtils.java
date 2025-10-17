/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network;

import com.mojang.logging.LogUtils;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.PacketApplyBatcher;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class NetworkThreadUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <T extends PacketListener> void forceMainThread(Packet<T> packet, T listener, ServerWorld world) throws OffThreadException {
        NetworkThreadUtils.forceMainThread(packet, listener, world.getServer().getPacketApplyBatcher());
    }

    public static <T extends PacketListener> void forceMainThread(Packet<T> packet, T listener, PacketApplyBatcher batcher) throws OffThreadException {
        if (!batcher.isOnThread()) {
            batcher.add(listener, packet);
            throw OffThreadException.INSTANCE;
        }
    }

    public static <T extends PacketListener> CrashException createCrashException(Exception exception, Packet<T> packet, T listener) {
        if (exception instanceof CrashException) {
            CrashException lv = (CrashException)exception;
            NetworkThreadUtils.fillCrashReport(lv.getReport(), listener, packet);
            return lv;
        }
        CrashReport lv2 = CrashReport.create(exception, "Main thread packet handler");
        NetworkThreadUtils.fillCrashReport(lv2, listener, packet);
        return new CrashException(lv2);
    }

    public static <T extends PacketListener> void fillCrashReport(CrashReport report, T listener, @Nullable Packet<T> packet) {
        if (packet != null) {
            CrashReportSection lv = report.addElement("Incoming Packet");
            lv.add("Type", () -> packet.getPacketType().toString());
            lv.add("Is Terminal", () -> Boolean.toString(packet.transitionsNetworkState()));
            lv.add("Is Skippable", () -> Boolean.toString(packet.isWritingErrorSkippable()));
        }
        listener.fillCrashReport(report);
    }
}

