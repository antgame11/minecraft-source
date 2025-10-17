/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.c2s.play;

import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.block.entity.TestInstanceBlockEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.registry.RegistryKey;
import net.minecraft.test.TestInstance;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public record TestInstanceBlockActionC2SPacket(BlockPos pos, Action action, TestInstanceBlockEntity.Data data) implements Packet<ServerPlayPacketListener>
{
    public static final PacketCodec<RegistryByteBuf, TestInstanceBlockActionC2SPacket> CODEC = PacketCodec.tuple(BlockPos.PACKET_CODEC, TestInstanceBlockActionC2SPacket::pos, Action.CODEC, TestInstanceBlockActionC2SPacket::action, TestInstanceBlockEntity.Data.PACKET_CODEC, TestInstanceBlockActionC2SPacket::data, TestInstanceBlockActionC2SPacket::new);

    public TestInstanceBlockActionC2SPacket(BlockPos pos, Action actin, Optional<RegistryKey<TestInstance>> optional, Vec3i arg3, BlockRotation arg4, boolean bl) {
        this(pos, actin, new TestInstanceBlockEntity.Data(optional, arg3, arg4, bl, TestInstanceBlockEntity.Status.CLEARED, Optional.empty()));
    }

    @Override
    public PacketType<TestInstanceBlockActionC2SPacket> getPacketType() {
        return PlayPackets.TEST_INSTANCE_BLOCK_ACTION;
    }

    @Override
    public void apply(ServerPlayPacketListener arg) {
        arg.onTestInstanceBlockAction(this);
    }

    public static enum Action {
        INIT(0),
        QUERY(1),
        SET(2),
        RESET(3),
        SAVE(4),
        EXPORT(5),
        RUN(6);

        private static final IntFunction<Action> INDEX_MAPPER;
        public static final PacketCodec<ByteBuf, Action> CODEC;
        private final int index;

        private Action(int index) {
            this.index = index;
        }

        static {
            INDEX_MAPPER = ValueLists.createIndexToValueFunction(action -> action.index, Action.values(), ValueLists.OutOfBoundsHandling.ZERO);
            CODEC = PacketCodecs.indexed(INDEX_MAPPER, action -> action.index);
        }
    }
}

