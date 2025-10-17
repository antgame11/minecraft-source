/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.DirectionTransformation;
import net.minecraft.util.math.random.Random;

public enum BlockRotation implements StringIdentifiable
{
    NONE(0, "none", DirectionTransformation.IDENTITY),
    CLOCKWISE_90(1, "clockwise_90", DirectionTransformation.ROT_90_Y_NEG),
    CLOCKWISE_180(2, "180", DirectionTransformation.ROT_180_FACE_XZ),
    COUNTERCLOCKWISE_90(3, "counterclockwise_90", DirectionTransformation.ROT_90_Y_POS);

    public static final IntFunction<BlockRotation> INDEX_MAPPER;
    public static final Codec<BlockRotation> CODEC;
    public static final PacketCodec<ByteBuf, BlockRotation> PACKET_CODEC;
    @Deprecated
    public static final Codec<BlockRotation> ENUM_NAME_CODEC;
    private final int index;
    private final String id;
    private final DirectionTransformation directionTransformation;

    private BlockRotation(int index, String id, DirectionTransformation directionTransformation) {
        this.index = index;
        this.id = id;
        this.directionTransformation = directionTransformation;
    }

    public BlockRotation rotate(BlockRotation rotation) {
        return switch (rotation.ordinal()) {
            case 2 -> {
                switch (this.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield CLOCKWISE_180;
                    }
                    case 1: {
                        yield COUNTERCLOCKWISE_90;
                    }
                    case 2: {
                        yield NONE;
                    }
                    case 3: 
                }
                yield CLOCKWISE_90;
            }
            case 3 -> {
                switch (this.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield COUNTERCLOCKWISE_90;
                    }
                    case 1: {
                        yield NONE;
                    }
                    case 2: {
                        yield CLOCKWISE_90;
                    }
                    case 3: 
                }
                yield CLOCKWISE_180;
            }
            case 1 -> {
                switch (this.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield CLOCKWISE_90;
                    }
                    case 1: {
                        yield CLOCKWISE_180;
                    }
                    case 2: {
                        yield COUNTERCLOCKWISE_90;
                    }
                    case 3: 
                }
                yield NONE;
            }
            default -> this;
        };
    }

    public DirectionTransformation getDirectionTransformation() {
        return this.directionTransformation;
    }

    public Direction rotate(Direction direction) {
        if (direction.getAxis() == Direction.Axis.Y) {
            return direction;
        }
        return switch (this.ordinal()) {
            case 2 -> direction.getOpposite();
            case 3 -> direction.rotateYCounterclockwise();
            case 1 -> direction.rotateYClockwise();
            default -> direction;
        };
    }

    public int rotate(int rotation, int fullTurn) {
        return switch (this.ordinal()) {
            case 2 -> (rotation + fullTurn / 2) % fullTurn;
            case 3 -> (rotation + fullTurn * 3 / 4) % fullTurn;
            case 1 -> (rotation + fullTurn / 4) % fullTurn;
            default -> rotation;
        };
    }

    public static BlockRotation random(Random random) {
        return Util.getRandom(BlockRotation.values(), random);
    }

    public static List<BlockRotation> randomRotationOrder(Random random) {
        return Util.copyShuffled(BlockRotation.values(), random);
    }

    @Override
    public String asString() {
        return this.id;
    }

    private int getIndex() {
        return this.index;
    }

    static {
        INDEX_MAPPER = ValueLists.createIndexToValueFunction(BlockRotation::getIndex, BlockRotation.values(), ValueLists.OutOfBoundsHandling.WRAP);
        CODEC = StringIdentifiable.createCodec(BlockRotation::values);
        PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, BlockRotation::getIndex);
        ENUM_NAME_CODEC = Codecs.enumByName(BlockRotation::valueOf);
    }
}

