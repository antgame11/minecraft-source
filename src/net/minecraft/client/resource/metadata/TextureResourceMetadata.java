/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.resource.metadata;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;

@Environment(value=EnvType.CLIENT)
public record TextureResourceMetadata(boolean blur, boolean clamp) {
    public static final boolean field_32980 = false;
    public static final boolean field_32981 = false;
    public static final Codec<TextureResourceMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.BOOL.optionalFieldOf("blur", false).forGetter(TextureResourceMetadata::blur), Codec.BOOL.optionalFieldOf("clamp", false).forGetter(TextureResourceMetadata::clamp)).apply((Applicative<TextureResourceMetadata, ?>)instance, TextureResourceMetadata::new));
    public static final ResourceMetadataSerializer<TextureResourceMetadata> SERIALIZER = new ResourceMetadataSerializer<TextureResourceMetadata>("texture", CODEC);
}

