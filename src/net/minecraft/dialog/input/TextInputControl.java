/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.dialog.input;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.dialog.input.InputControl;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.dynamic.Codecs;

public record TextInputControl(int width, Text label, boolean labelVisible, String initial, int maxLength, Optional<Multiline> multiline) implements InputControl
{
    public static final MapCodec<TextInputControl> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Dialog.WIDTH_CODEC.optionalFieldOf("width", 200).forGetter(TextInputControl::width), ((MapCodec)TextCodecs.CODEC.fieldOf("label")).forGetter(TextInputControl::label), Codec.BOOL.optionalFieldOf("label_visible", true).forGetter(TextInputControl::labelVisible), Codec.STRING.optionalFieldOf("initial", "").forGetter(TextInputControl::initial), Codecs.POSITIVE_INT.optionalFieldOf("max_length", 32).forGetter(TextInputControl::maxLength), Multiline.CODEC.optionalFieldOf("multiline").forGetter(TextInputControl::multiline)).apply((Applicative<TextInputControl, ?>)instance, TextInputControl::new)).validate(inputControl -> {
        if (inputControl.initial.length() > inputControl.maxLength()) {
            return DataResult.error(() -> "Default text length exceeds allowed size");
        }
        return DataResult.success(inputControl);
    });

    public MapCodec<TextInputControl> getCodec() {
        return CODEC;
    }

    public record Multiline(Optional<Integer> maxLines, Optional<Integer> height) {
        public static final int MAX_HEIGHT = 512;
        public static final Codec<Multiline> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codecs.POSITIVE_INT.optionalFieldOf("max_lines").forGetter(Multiline::maxLines), Codecs.rangedInt(1, 512).optionalFieldOf("height").forGetter(Multiline::height)).apply((Applicative<Multiline, ?>)instance, Multiline::new));
    }
}

