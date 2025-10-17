/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.text.object;

import com.mojang.serialization.MapCodec;
import net.minecraft.text.StyleSpriteSource;

public interface TextObjectContents {
    public StyleSpriteSource spriteSource();

    public String asText();

    public MapCodec<? extends TextObjectContents> getCodec();
}

