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
import net.minecraft.util.math.MathHelper;

public record NumberRangeInputControl(int width, Text label, String labelFormat, RangeInfo rangeInfo) implements InputControl
{
    public static final MapCodec<NumberRangeInputControl> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Dialog.WIDTH_CODEC.optionalFieldOf("width", 200).forGetter(NumberRangeInputControl::width), ((MapCodec)TextCodecs.CODEC.fieldOf("label")).forGetter(NumberRangeInputControl::label), Codec.STRING.optionalFieldOf("label_format", "options.generic_value").forGetter(NumberRangeInputControl::labelFormat), RangeInfo.CODEC.forGetter(NumberRangeInputControl::rangeInfo)).apply((Applicative<NumberRangeInputControl, ?>)instance, NumberRangeInputControl::new));

    public MapCodec<NumberRangeInputControl> getCodec() {
        return CODEC;
    }

    public Text getFormattedLabel(String value) {
        return Text.translatable(this.labelFormat, this.label, value);
    }

    public record RangeInfo(float start, float end, Optional<Float> initial, Optional<Float> step) {
        public static final MapCodec<RangeInfo> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("start")).forGetter(RangeInfo::start), ((MapCodec)Codec.FLOAT.fieldOf("end")).forGetter(RangeInfo::end), Codec.FLOAT.optionalFieldOf("initial").forGetter(RangeInfo::initial), Codecs.POSITIVE_FLOAT.optionalFieldOf("step").forGetter(RangeInfo::step)).apply((Applicative<RangeInfo, ?>)instance, RangeInfo::new)).validate(rangeInfo -> {
            if (rangeInfo.initial.isPresent()) {
                double d = rangeInfo.initial.get().floatValue();
                double e = Math.min(rangeInfo.start, rangeInfo.end);
                double f = Math.max(rangeInfo.start, rangeInfo.end);
                if (d < e || d > f) {
                    return DataResult.error(() -> "Initial value " + d + " is outside of range [" + e + ", " + f + "]");
                }
            }
            return DataResult.success(rangeInfo);
        });

        public float sliderProgressToValue(float sliderProgress) {
            float j;
            int k;
            float g = MathHelper.lerp(sliderProgress, this.start, this.end);
            if (this.step.isEmpty()) {
                return g;
            }
            float h = this.step.get().floatValue();
            float i = this.getInitialValue();
            float l = i + (float)(k = Math.round((j = g - i) / h)) * h;
            if (!this.isValueOutOfRange(l)) {
                return l;
            }
            int m = k - MathHelper.sign(k);
            return i + (float)m * h;
        }

        private boolean isValueOutOfRange(float value) {
            float g = this.valueToSliderProgress(value);
            return (double)g < 0.0 || (double)g > 1.0;
        }

        private float getInitialValue() {
            if (this.initial.isPresent()) {
                return this.initial.get().floatValue();
            }
            return (this.start + this.end) / 2.0f;
        }

        public float getInitialSliderProgress() {
            float f = this.getInitialValue();
            return this.valueToSliderProgress(f);
        }

        private float valueToSliderProgress(float value) {
            if (this.start == this.end) {
                return 0.5f;
            }
            return MathHelper.getLerpProgress(value, this.start, this.end);
        }
    }
}

