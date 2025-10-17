/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.dialog.body;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.dialog.body.DialogBody;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record PlainMessageDialogBody(Text contents, int width) implements DialogBody
{
    public static final int DEFAULT_WIDTH = 200;
    public static final MapCodec<PlainMessageDialogBody> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)TextCodecs.CODEC.fieldOf("contents")).forGetter(PlainMessageDialogBody::contents), Dialog.WIDTH_CODEC.optionalFieldOf("width", 200).forGetter(PlainMessageDialogBody::width)).apply((Applicative<PlainMessageDialogBody, ?>)instance, PlainMessageDialogBody::new));
    public static final Codec<PlainMessageDialogBody> ALTERNATIVE_CODEC = Codec.withAlternative(CODEC.codec(), TextCodecs.CODEC, contents -> new PlainMessageDialogBody((Text)contents, 200));

    public MapCodec<PlainMessageDialogBody> getTypeCodec() {
        return CODEC;
    }
}

