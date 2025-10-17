/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.texture;

import com.mojang.logging.LogUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteOpener;
import net.minecraft.client.texture.TextureStitcher;
import net.minecraft.client.texture.TextureStitcherCannotFitException;
import net.minecraft.client.texture.atlas.AtlasLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.util.profiler.ScopedProfiler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class SpriteLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Identifier id;
    private final int maxTextureSize;
    private final int width;
    private final int height;

    public SpriteLoader(Identifier id, int maxTextureSize, int width, int height) {
        this.id = id;
        this.maxTextureSize = maxTextureSize;
        this.width = width;
        this.height = height;
    }

    public static SpriteLoader fromAtlas(SpriteAtlasTexture atlasTexture) {
        return new SpriteLoader(atlasTexture.getId(), atlasTexture.getMaxTextureSize(), atlasTexture.getWidth(), atlasTexture.getHeight());
    }

    private StitchResult stitch(List<SpriteContents> sprites, int mipLevel, Executor executor) {
        try (ScopedProfiler lv = Profilers.get().scoped(() -> "stitch " + String.valueOf(this.id));){
            int m;
            int j = this.maxTextureSize;
            TextureStitcher<SpriteContents> lv2 = new TextureStitcher<SpriteContents>(j, j, mipLevel);
            int k = Integer.MAX_VALUE;
            int l = 1 << mipLevel;
            for (SpriteContents lv3 : sprites) {
                k = Math.min(k, Math.min(lv3.getWidth(), lv3.getHeight()));
                m = Math.min(Integer.lowestOneBit(lv3.getWidth()), Integer.lowestOneBit(lv3.getHeight()));
                if (m < l) {
                    LOGGER.warn("Texture {} with size {}x{} limits mip level from {} to {}", lv3.getId(), lv3.getWidth(), lv3.getHeight(), MathHelper.floorLog2(l), MathHelper.floorLog2(m));
                    l = m;
                }
                lv2.add(lv3);
            }
            int n = Math.min(k, l);
            int o = MathHelper.floorLog2(n);
            if (o < mipLevel) {
                LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", this.id, mipLevel, o, n);
                m = o;
            } else {
                m = mipLevel;
            }
            try {
                lv2.stitch();
            } catch (TextureStitcherCannotFitException lv4) {
                CrashReport lv5 = CrashReport.create(lv4, "Stitching");
                CrashReportSection lv6 = lv5.addElement("Stitcher");
                lv6.add("Sprites", lv4.getSprites().stream().map(sprite -> String.format(Locale.ROOT, "%s[%dx%d]", sprite.getId(), sprite.getWidth(), sprite.getHeight())).collect(Collectors.joining(",")));
                lv6.add("Max Texture Size", j);
                throw new CrashException(lv5);
            }
            int p = Math.max(lv2.getWidth(), this.width);
            int q = Math.max(lv2.getHeight(), this.height);
            Map<Identifier, Sprite> map = this.collectStitchedSprites(lv2, p, q);
            Sprite lv7 = map.get(MissingSprite.getMissingSpriteId());
            CompletableFuture<Object> completableFuture = m > 0 ? CompletableFuture.runAsync(() -> map.values().forEach(sprite -> sprite.getContents().generateMipmaps(m)), executor) : CompletableFuture.completedFuture(null);
            StitchResult stitchResult = new StitchResult(p, q, m, lv7, map, completableFuture);
            return stitchResult;
        }
    }

    private static CompletableFuture<List<SpriteContents>> loadAll(SpriteOpener opener, List<Function<SpriteOpener, SpriteContents>> sources, Executor executor) {
        List<CompletableFuture> list2 = sources.stream().map(sprite -> CompletableFuture.supplyAsync(() -> (SpriteContents)sprite.apply(opener), executor)).toList();
        return Util.combineSafe(list2).thenApply(sprites -> sprites.stream().filter(Objects::nonNull).toList());
    }

    public CompletableFuture<StitchResult> load(ResourceManager resourceManager, Identifier path, int mipLevel, Executor executor, Set<ResourceMetadataSerializer<?>> additionalMetadata) {
        SpriteOpener lv = SpriteOpener.create(additionalMetadata);
        return ((CompletableFuture)CompletableFuture.supplyAsync(() -> AtlasLoader.of(resourceManager, path).loadSources(resourceManager), executor).thenCompose(sources -> SpriteLoader.loadAll(lv, sources, executor))).thenApply(sprites -> this.stitch((List<SpriteContents>)sprites, mipLevel, executor));
    }

    private Map<Identifier, Sprite> collectStitchedSprites(TextureStitcher<SpriteContents> stitcher, int atlasWidth, int atlasHeight) {
        HashMap<Identifier, Sprite> map = new HashMap<Identifier, Sprite>();
        stitcher.getStitchedSprites((info, x, y) -> map.put(info.getId(), new Sprite(this.id, (SpriteContents)info, atlasWidth, atlasHeight, x, y)));
        return map;
    }

    @Environment(value=EnvType.CLIENT)
    public record StitchResult(int width, int height, int mipLevel, Sprite missing, Map<Identifier, Sprite> sprites, CompletableFuture<Void> readyForUpload) {
        @Nullable
        public Sprite getSprite(Identifier id) {
            return this.sprites.get(id);
        }
    }
}

