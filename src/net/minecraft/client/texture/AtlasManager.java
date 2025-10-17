/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.texture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.resource.metadata.GuiResourceMetadata;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.util.Atlases;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class AtlasManager
implements ResourceReloader,
SpriteHolder,
AutoCloseable {
    private static final List<Metadata> ATLAS_METADATA = List.of(new Metadata(TexturedRenderLayers.ARMOR_TRIMS_ATLAS_TEXTURE, Atlases.ARMOR_TRIMS, false), new Metadata(TexturedRenderLayers.BANNER_PATTERNS_ATLAS_TEXTURE, Atlases.BANNER_PATTERNS, false), new Metadata(TexturedRenderLayers.BEDS_ATLAS_TEXTURE, Atlases.BEDS, false), new Metadata(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Atlases.BLOCKS, true), new Metadata(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, Atlases.CHESTS, false), new Metadata(TexturedRenderLayers.DECORATED_POT_ATLAS_TEXTURE, Atlases.DECORATED_POT, false), new Metadata(TexturedRenderLayers.GUI_ATLAS_TEXTURE, Atlases.GUI, false, Set.of(GuiResourceMetadata.SERIALIZER)), new Metadata(TexturedRenderLayers.MAP_DECORATIONS_ATLAS_TEXTURE, Atlases.MAP_DECORATIONS, false), new Metadata(TexturedRenderLayers.PAINTINGS_ATLAS_TEXTURE, Atlases.PAINTINGS, false), new Metadata(SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE, Atlases.PARTICLES, false), new Metadata(TexturedRenderLayers.SHIELD_PATTERNS_ATLAS_TEXTURE, Atlases.SHIELD_PATTERNS, false), new Metadata(TexturedRenderLayers.SHULKER_BOXES_ATLAS_TEXTURE, Atlases.SHULKER_BOXES, false), new Metadata(TexturedRenderLayers.SIGNS_ATLAS_TEXTURE, Atlases.SIGNS, false));
    public static final ResourceReloader.Key<Stitch> stitchKey = new ResourceReloader.Key();
    private final Map<Identifier, Entry> entriesByTextureId = new HashMap<Identifier, Entry>();
    private final Map<Identifier, Entry> entriesByDefinitionId = new HashMap<Identifier, Entry>();
    private Map<SpriteIdentifier, Sprite> sprites = Map.of();
    private int mipmapLevels;

    public AtlasManager(TextureManager textureManager, int mipmapLevels) {
        for (Metadata lv : ATLAS_METADATA) {
            SpriteAtlasTexture lv2 = new SpriteAtlasTexture(lv.textureId);
            textureManager.registerTexture(lv.textureId, lv2);
            Entry lv3 = new Entry(lv2, lv);
            this.entriesByTextureId.put(lv.textureId, lv3);
            this.entriesByDefinitionId.put(lv.definitionId, lv3);
        }
        this.mipmapLevels = mipmapLevels;
    }

    public SpriteAtlasTexture getAtlasTexture(Identifier id) {
        Entry lv = this.entriesByDefinitionId.get(id);
        if (lv == null) {
            throw new IllegalArgumentException("Invalid atlas id: " + String.valueOf(id));
        }
        return lv.atlas();
    }

    public void acceptAtlasTextures(BiConsumer<Identifier, SpriteAtlasTexture> consumer) {
        this.entriesByDefinitionId.forEach((definitionId, entry) -> consumer.accept((Identifier)definitionId, entry.atlas));
    }

    public void setMipmapLevels(int mipmapLevels) {
        this.mipmapLevels = mipmapLevels;
    }

    @Override
    public void close() {
        this.sprites = Map.of();
        this.entriesByDefinitionId.values().forEach(Entry::close);
        this.entriesByDefinitionId.clear();
        this.entriesByTextureId.clear();
    }

    @Override
    public Sprite getSprite(SpriteIdentifier id) {
        Sprite lv = this.sprites.get(id);
        if (lv != null) {
            return lv;
        }
        Identifier lv2 = id.getAtlasId();
        Entry lv3 = this.entriesByTextureId.get(lv2);
        if (lv3 == null) {
            throw new IllegalArgumentException("Invalid atlas texture id: " + String.valueOf(lv2));
        }
        return lv3.atlas().getMissingSprite();
    }

    @Override
    public void prepareSharedState(ResourceReloader.Store arg) {
        int i = this.entriesByDefinitionId.size();
        ArrayList<CompletableEntry> list = new ArrayList<CompletableEntry>(i);
        HashMap<Identifier, CompletableFuture<SpriteLoader.StitchResult>> map = new HashMap<Identifier, CompletableFuture<SpriteLoader.StitchResult>>(i);
        ArrayList list2 = new ArrayList(i);
        this.entriesByDefinitionId.forEach((textureId, metadata) -> {
            CompletableFuture<SpriteLoader.StitchResult> completableFuture = new CompletableFuture<SpriteLoader.StitchResult>();
            map.put((Identifier)textureId, completableFuture);
            list.add(new CompletableEntry((Entry)metadata, completableFuture));
            list2.add(completableFuture.thenCompose(SpriteLoader.StitchResult::readyForUpload));
        });
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf((CompletableFuture[])list2.toArray(CompletableFuture[]::new));
        arg.put(stitchKey, new Stitch(list, map, completableFuture));
    }

    @Override
    public CompletableFuture<Void> reload(ResourceReloader.Store store, Executor prepareExecutor, ResourceReloader.Synchronizer reloadSynchronizer, Executor applyExecutor) {
        Stitch lv = store.getOrThrow(stitchKey);
        ResourceManager lv2 = store.getResourceManager();
        lv.entries.forEach(entry -> entry.entry.load(lv2, prepareExecutor, this.mipmapLevels).whenComplete((stitchResult, throwable) -> {
            if (stitchResult != null) {
                arg.preparations.complete((SpriteLoader.StitchResult)stitchResult);
            } else {
                arg.preparations.completeExceptionally((Throwable)throwable);
            }
        }));
        return ((CompletableFuture)lv.readyForUpload.thenCompose(reloadSynchronizer::whenPrepared)).thenAcceptAsync(object -> {
            this.sprites = lv.createSpriteMap();
        }, applyExecutor);
    }

    @Environment(value=EnvType.CLIENT)
    public record Metadata(Identifier textureId, Identifier definitionId, boolean createMipmaps, Set<ResourceMetadataSerializer<?>> additionalMetadata) {
        public Metadata(Identifier textureId, Identifier definitionId, boolean createMipmaps) {
            this(textureId, definitionId, createMipmaps, Set.of());
        }
    }

    @Environment(value=EnvType.CLIENT)
    record Entry(SpriteAtlasTexture atlas, Metadata metadata) implements AutoCloseable
    {
        @Override
        public void close() {
            this.atlas.clear();
        }

        CompletableFuture<SpriteLoader.StitchResult> load(ResourceManager manager, Executor executor, int mipLevel) {
            return SpriteLoader.fromAtlas(this.atlas).load(manager, this.metadata.definitionId, this.metadata.createMipmaps ? mipLevel : 0, executor, this.metadata.additionalMetadata);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Stitch {
        final List<CompletableEntry> entries;
        private final Map<Identifier, CompletableFuture<SpriteLoader.StitchResult>> preparations;
        final CompletableFuture<?> readyForUpload;

        Stitch(List<CompletableEntry> entries, Map<Identifier, CompletableFuture<SpriteLoader.StitchResult>> preparations, CompletableFuture<?> readyForUpload) {
            this.entries = entries;
            this.preparations = preparations;
            this.readyForUpload = readyForUpload;
        }

        public Map<SpriteIdentifier, Sprite> createSpriteMap() {
            HashMap<SpriteIdentifier, Sprite> map = new HashMap<SpriteIdentifier, Sprite>();
            this.entries.forEach(entry -> entry.fillSpriteMap(map));
            return map;
        }

        public CompletableFuture<SpriteLoader.StitchResult> getPreparations(Identifier atlasTextureId) {
            return Objects.requireNonNull(this.preparations.get(atlasTextureId));
        }
    }

    @Environment(value=EnvType.CLIENT)
    record CompletableEntry(Entry entry, CompletableFuture<SpriteLoader.StitchResult> preparations) {
        public void fillSpriteMap(Map<SpriteIdentifier, Sprite> sprites) {
            SpriteLoader.StitchResult lv = this.preparations.join();
            this.entry.atlas.upload(lv);
            lv.sprites().forEach((id, sprite) -> sprites.put(new SpriteIdentifier(this.entry.metadata.textureId, (Identifier)id), (Sprite)sprite));
        }
    }
}

