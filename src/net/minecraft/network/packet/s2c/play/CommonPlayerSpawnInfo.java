/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import java.util.Optional;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

public record CommonPlayerSpawnInfo(RegistryEntry<DimensionType> dimensionType, RegistryKey<World> dimension, long seed, GameMode gameMode, @Nullable GameMode lastGameMode, boolean isDebug, boolean isFlat, Optional<GlobalPos> lastDeathLocation, int portalCooldown, int seaLevel) {
    public CommonPlayerSpawnInfo(RegistryByteBuf buf) {
        this((RegistryEntry)DimensionType.PACKET_CODEC.decode(buf), buf.readRegistryKey(RegistryKeys.WORLD), buf.readLong(), GameMode.byIndex(buf.readByte()), GameMode.getOrNull(buf.readByte()), buf.readBoolean(), buf.readBoolean(), buf.readOptional(PacketByteBuf::readGlobalPos), buf.readVarInt(), buf.readVarInt());
    }

    public void write(RegistryByteBuf buf) {
        DimensionType.PACKET_CODEC.encode(buf, this.dimensionType);
        buf.writeRegistryKey(this.dimension);
        buf.writeLong(this.seed);
        buf.writeByte(this.gameMode.getIndex());
        buf.writeByte(GameMode.getId(this.lastGameMode));
        buf.writeBoolean(this.isDebug);
        buf.writeBoolean(this.isFlat);
        buf.writeOptional(this.lastDeathLocation, PacketByteBuf::writeGlobalPos);
        buf.writeVarInt(this.portalCooldown);
        buf.writeVarInt(this.seaLevel);
    }

    @Nullable
    public GameMode lastGameMode() {
        return this.lastGameMode;
    }
}

