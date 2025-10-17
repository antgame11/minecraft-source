/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.enums;

import com.mojang.serialization.Codec;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Codecs;

public enum StructureBlockMode implements StringIdentifiable
{
    SAVE("save"),
    LOAD("load"),
    CORNER("corner"),
    DATA("data");

    @Deprecated
    public static final Codec<StructureBlockMode> CODEC;
    private final String name;
    private final Text text;

    private StructureBlockMode(String name) {
        this.name = name;
        this.text = Text.translatable("structure_block.mode_info." + name);
    }

    @Override
    public String asString() {
        return this.name;
    }

    public Text asText() {
        return this.text;
    }

    static {
        CODEC = Codecs.enumByName(StructureBlockMode::valueOf);
    }
}

