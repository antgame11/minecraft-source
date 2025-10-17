/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public enum EntityPose implements StringIdentifiable
{
    STANDING(0, "standing"),
    GLIDING(1, "fall_flying"),
    SLEEPING(2, "sleeping"),
    SWIMMING(3, "swimming"),
    SPIN_ATTACK(4, "spin_attack"),
    CROUCHING(5, "crouching"),
    LONG_JUMPING(6, "long_jumping"),
    DYING(7, "dying"),
    CROAKING(8, "croaking"),
    USING_TONGUE(9, "using_tongue"),
    SITTING(10, "sitting"),
    ROARING(11, "roaring"),
    SNIFFING(12, "sniffing"),
    EMERGING(13, "emerging"),
    DIGGING(14, "digging"),
    SLIDING(15, "sliding"),
    SHOOTING(16, "shooting"),
    INHALING(17, "inhaling");

    public static final IntFunction<EntityPose> INDEX_TO_VALUE;
    public static final Codec<EntityPose> CODEC;
    public static final PacketCodec<ByteBuf, EntityPose> PACKET_CODEC;
    private final int index;
    private final String name;

    private EntityPose(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public int getIndex() {
        return this.index;
    }

    @Override
    public String asString() {
        return this.name;
    }

    static {
        INDEX_TO_VALUE = ValueLists.createIndexToValueFunction(EntityPose::getIndex, EntityPose.values(), ValueLists.OutOfBoundsHandling.ZERO);
        CODEC = StringIdentifiable.createCodec(EntityPose::values);
        PACKET_CODEC = PacketCodecs.indexed(INDEX_TO_VALUE, EntityPose::getIndex);
    }
}

