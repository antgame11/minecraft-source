/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.dialog.input;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.dialog.input.InputControl;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record BooleanInputControl(Text label, boolean initial, String onTrue, String onFalse) implements InputControl
{
    public static final MapCodec<BooleanInputControl> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)TextCodecs.CODEC.fieldOf("label")).forGetter(BooleanInputControl::label), Codec.BOOL.optionalFieldOf("initial", false).forGetter(BooleanInputControl::initial), Codec.STRING.optionalFieldOf("on_true", "true").forGetter(BooleanInputControl::onTrue), Codec.STRING.optionalFieldOf("on_false", "false").forGetter(BooleanInputControl::onFalse)).apply((Applicative<BooleanInputControl, ?>)instance, BooleanInputControl::new));

    public MapCodec<BooleanInputControl> getCodec() {
        return CODEC;
    }
}

