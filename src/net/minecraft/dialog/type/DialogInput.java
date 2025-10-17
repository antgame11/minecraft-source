/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.dialog.type;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.dialog.action.ParsedTemplate;
import net.minecraft.dialog.input.InputControl;

public record DialogInput(String key, InputControl control) {
    public static final Codec<DialogInput> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ParsedTemplate.NAME_CODEC.fieldOf("key")).forGetter(DialogInput::key), InputControl.CODEC.forGetter(DialogInput::control)).apply((Applicative<DialogInput, ?>)instance, DialogInput::new));
}

