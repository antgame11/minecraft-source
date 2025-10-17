/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.dialog;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record DialogButtonData(Text label, Optional<Text> tooltip, int width) {
    public static final int DEFAULT_WIDTH = 150;
    public static final MapCodec<DialogButtonData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)TextCodecs.CODEC.fieldOf("label")).forGetter(DialogButtonData::label), TextCodecs.CODEC.optionalFieldOf("tooltip").forGetter(DialogButtonData::tooltip), Dialog.WIDTH_CODEC.optionalFieldOf("width", 150).forGetter(DialogButtonData::width)).apply((Applicative<DialogButtonData, ?>)instance, DialogButtonData::new));

    public DialogButtonData(Text label, int width) {
        this(label, Optional.empty(), width);
    }
}

