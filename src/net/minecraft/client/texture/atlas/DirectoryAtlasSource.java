/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.texture.atlas;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.atlas.AtlasSource;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public record DirectoryAtlasSource(String sourcePath, String idPrefix) implements AtlasSource
{
    public static final MapCodec<DirectoryAtlasSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("source")).forGetter(DirectoryAtlasSource::sourcePath), ((MapCodec)Codec.STRING.fieldOf("prefix")).forGetter(DirectoryAtlasSource::idPrefix)).apply((Applicative<DirectoryAtlasSource, ?>)instance, DirectoryAtlasSource::new));

    @Override
    public void load(ResourceManager resourceManager, AtlasSource.SpriteRegions regions) {
        ResourceFinder lv = new ResourceFinder("textures/" + this.sourcePath, ".png");
        lv.findResources(resourceManager).forEach((id, resource) -> {
            Identifier lv = lv.toResourceId((Identifier)id).withPrefixedPath(this.idPrefix);
            regions.add(lv, (Resource)resource);
        });
    }

    public MapCodec<DirectoryAtlasSource> getCodec() {
        return CODEC;
    }
}

