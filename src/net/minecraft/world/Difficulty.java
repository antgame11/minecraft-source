/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import org.jetbrains.annotations.Nullable;

public enum Difficulty implements StringIdentifiable
{
    PEACEFUL(0, "peaceful"),
    EASY(1, "easy"),
    NORMAL(2, "normal"),
    HARD(3, "hard");

    public static final StringIdentifiable.EnumCodec<Difficulty> CODEC;
    private static final IntFunction<Difficulty> BY_ID;
    public static final PacketCodec<ByteBuf, Difficulty> PACKET_CODEC;
    private final int id;
    private final String name;

    private Difficulty(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public Text getTranslatableName() {
        return Text.translatable("options.difficulty." + this.name);
    }

    public Text getInfo() {
        return Text.translatable("options.difficulty." + this.name + ".info");
    }

    @Deprecated
    public static Difficulty byId(int id) {
        return BY_ID.apply(id);
    }

    @Nullable
    public static Difficulty byName(String name) {
        return CODEC.byId(name);
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    static {
        CODEC = StringIdentifiable.createCodec(Difficulty::values);
        BY_ID = ValueLists.createIndexToValueFunction(Difficulty::getId, Difficulty.values(), ValueLists.OutOfBoundsHandling.WRAP);
        PACKET_CODEC = PacketCodecs.indexed(BY_ID, Difficulty::getId);
    }
}

