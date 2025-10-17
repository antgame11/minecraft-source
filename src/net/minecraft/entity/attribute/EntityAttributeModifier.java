/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.attribute;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public record EntityAttributeModifier(Identifier id, double value, Operation operation) {
    public static final MapCodec<EntityAttributeModifier> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("id")).forGetter(EntityAttributeModifier::id), ((MapCodec)Codec.DOUBLE.fieldOf("amount")).forGetter(EntityAttributeModifier::value), ((MapCodec)Operation.CODEC.fieldOf("operation")).forGetter(EntityAttributeModifier::operation)).apply((Applicative<EntityAttributeModifier, ?>)instance, EntityAttributeModifier::new));
    public static final Codec<EntityAttributeModifier> CODEC = MAP_CODEC.codec();
    public static final PacketCodec<ByteBuf, EntityAttributeModifier> PACKET_CODEC = PacketCodec.tuple(Identifier.PACKET_CODEC, EntityAttributeModifier::id, PacketCodecs.DOUBLE, EntityAttributeModifier::value, Operation.PACKET_CODEC, EntityAttributeModifier::operation, EntityAttributeModifier::new);

    public boolean idMatches(Identifier id) {
        return id.equals(this.id);
    }

    public static enum Operation implements StringIdentifiable
    {
        ADD_VALUE("add_value", 0),
        ADD_MULTIPLIED_BASE("add_multiplied_base", 1),
        ADD_MULTIPLIED_TOTAL("add_multiplied_total", 2);

        public static final IntFunction<Operation> ID_TO_VALUE;
        public static final PacketCodec<ByteBuf, Operation> PACKET_CODEC;
        public static final Codec<Operation> CODEC;
        private final String name;
        private final int id;

        private Operation(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        @Override
        public String asString() {
            return this.name;
        }

        static {
            ID_TO_VALUE = ValueLists.createIndexToValueFunction(Operation::getId, Operation.values(), ValueLists.OutOfBoundsHandling.ZERO);
            PACKET_CODEC = PacketCodecs.indexed(ID_TO_VALUE, Operation::getId);
            CODEC = StringIdentifiable.createCodec(Operation::values);
        }
    }
}

