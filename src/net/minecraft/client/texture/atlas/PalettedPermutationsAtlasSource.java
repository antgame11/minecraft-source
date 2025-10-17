/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.texture.atlas;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.client.texture.SpriteOpener;
import net.minecraft.client.texture.atlas.AtlasSource;
import net.minecraft.client.texture.atlas.AtlasSprite;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public record PalettedPermutationsAtlasSource(List<Identifier> textures, Identifier paletteKey, Map<String, Identifier> permutations, String separator) implements AtlasSource
{
    static final Logger LOGGER = LogUtils.getLogger();
    public static final String DEFAULT_SEPARATOR = "_";
    public static final MapCodec<PalettedPermutationsAtlasSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.list(Identifier.CODEC).fieldOf("textures")).forGetter(PalettedPermutationsAtlasSource::textures), ((MapCodec)Identifier.CODEC.fieldOf("palette_key")).forGetter(PalettedPermutationsAtlasSource::paletteKey), ((MapCodec)Codec.unboundedMap(Codec.STRING, Identifier.CODEC).fieldOf("permutations")).forGetter(PalettedPermutationsAtlasSource::permutations), Codec.STRING.optionalFieldOf("separator", DEFAULT_SEPARATOR).forGetter(PalettedPermutationsAtlasSource::separator)).apply((Applicative<PalettedPermutationsAtlasSource, ?>)instance, PalettedPermutationsAtlasSource::new));

    public PalettedPermutationsAtlasSource(List<Identifier> textures, Identifier paletteKey, Map<String, Identifier> permutations) {
        this(textures, paletteKey, permutations, DEFAULT_SEPARATOR);
    }

    @Override
    public void load(ResourceManager resourceManager, AtlasSource.SpriteRegions regions) {
        Supplier<int[]> supplier = Suppliers.memoize(() -> PalettedPermutationsAtlasSource.open(resourceManager, this.paletteKey));
        HashMap map = new HashMap();
        this.permutations.forEach((key, texture) -> map.put(key, Suppliers.memoize(() -> PalettedPermutationsAtlasSource.toMapper((int[])((java.util.function.Supplier)supplier).get(), PalettedPermutationsAtlasSource.open(resourceManager, texture)))));
        for (Identifier lv : this.textures) {
            Identifier lv2 = RESOURCE_FINDER.toResourcePath(lv);
            Optional<Resource> optional = resourceManager.getResource(lv2);
            if (optional.isEmpty()) {
                LOGGER.warn("Unable to find texture {}", (Object)lv2);
                continue;
            }
            AtlasSprite lv3 = new AtlasSprite(lv2, optional.get(), map.size());
            for (Map.Entry entry : map.entrySet()) {
                Identifier lv4 = lv.withSuffixedPath(this.separator + (String)entry.getKey());
                regions.add(lv4, new PalettedSpriteRegion(lv3, (java.util.function.Supplier)entry.getValue(), lv4));
            }
        }
    }

    private static IntUnaryOperator toMapper(int[] from, int[] to) {
        if (to.length != from.length) {
            LOGGER.warn("Palette mapping has different sizes: {} and {}", (Object)from.length, (Object)to.length);
            throw new IllegalArgumentException();
        }
        Int2IntOpenHashMap int2IntMap = new Int2IntOpenHashMap(to.length);
        for (int i = 0; i < from.length; ++i) {
            int j = from[i];
            if (ColorHelper.getAlpha(j) == 0) continue;
            int2IntMap.put(ColorHelper.zeroAlpha(j), to[i]);
        }
        return color -> {
            int j = ColorHelper.getAlpha(color);
            if (j == 0) {
                return color;
            }
            int k = ColorHelper.zeroAlpha(color);
            int l = int2IntMap.getOrDefault(k, ColorHelper.fullAlpha(k));
            int m = ColorHelper.getAlpha(l);
            return ColorHelper.withAlpha(j * m / 255, l);
        };
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private static int[] open(ResourceManager resourceManager, Identifier texture) {
        Optional<Resource> optional = resourceManager.getResource(RESOURCE_FINDER.toResourcePath(texture));
        if (optional.isEmpty()) {
            LOGGER.error("Failed to load palette image {}", (Object)texture);
            throw new IllegalArgumentException();
        }
        try (InputStream inputStream = optional.get().getInputStream();){
            NativeImage lv = NativeImage.read(inputStream);
            try {
                int[] nArray = lv.copyPixelsArgb();
                if (lv != null) {
                    lv.close();
                }
                return nArray;
            } catch (Throwable throwable) {
                if (lv != null) {
                    try {
                        lv.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        } catch (Exception exception) {
            LOGGER.error("Couldn't load texture {}", (Object)texture, (Object)exception);
            throw new IllegalArgumentException();
        }
    }

    public MapCodec<PalettedPermutationsAtlasSource> getCodec() {
        return CODEC;
    }

    @Environment(value=EnvType.CLIENT)
    record PalettedSpriteRegion(AtlasSprite baseImage, java.util.function.Supplier<IntUnaryOperator> palette, Identifier permutationLocation) implements AtlasSource.SpriteRegion
    {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        @Nullable
        public SpriteContents apply(SpriteOpener arg) {
            try {
                NativeImage lv = this.baseImage.read().applyToCopy(this.palette.get());
                SpriteContents spriteContents = new SpriteContents(this.permutationLocation, new SpriteDimensions(lv.getWidth(), lv.getHeight()), lv);
                return spriteContents;
            } catch (IOException | IllegalArgumentException exception) {
                LOGGER.error("unable to apply palette to {}", (Object)this.permutationLocation, (Object)exception);
                SpriteContents spriteContents = null;
                return spriteContents;
            } finally {
                this.baseImage.close();
            }
        }

        @Override
        public void close() {
            this.baseImage.close();
        }

        @Override
        @Nullable
        public /* synthetic */ Object apply(Object opener) {
            return this.apply((SpriteOpener)opener);
        }
    }
}

