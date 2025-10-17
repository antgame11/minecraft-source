/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.text.object;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.object.TextObjectContents;

public record PlayerTextObjectContents(ProfileComponent player, boolean hat) implements TextObjectContents
{
    public static final MapCodec<PlayerTextObjectContents> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)ProfileComponent.CODEC.fieldOf("player")).forGetter(PlayerTextObjectContents::player), Codec.BOOL.optionalFieldOf("hat", true).forGetter(PlayerTextObjectContents::hat)).apply((Applicative<PlayerTextObjectContents, ?>)instance, PlayerTextObjectContents::new));

    @Override
    public StyleSpriteSource spriteSource() {
        return new StyleSpriteSource.Player(this.player, this.hat);
    }

    @Override
    public String asText() {
        return this.player.getName().map(name -> "[" + name + " head]").orElse("[unknown player head]");
    }

    public MapCodec<PlayerTextObjectContents> getCodec() {
        return CODEC;
    }
}

