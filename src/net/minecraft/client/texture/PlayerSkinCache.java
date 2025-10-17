/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.texture;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderLayerSet;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.server.GameProfileResolver;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class PlayerSkinCache {
    public static final RenderLayer DEFAULT_RENDER_LAYER = PlayerSkinCache.getRenderLayer(DefaultSkinHelper.getSteve());
    public static final Duration TIME_TO_LIVE = Duration.ofMinutes(5L);
    private final LoadingCache<ProfileComponent, CompletableFuture<Optional<Entry>>> fetchingCache = CacheBuilder.newBuilder().expireAfterAccess(TIME_TO_LIVE).build(new CacheLoader<ProfileComponent, CompletableFuture<Optional<Entry>>>(){

        @Override
        public CompletableFuture<Optional<Entry>> load(ProfileComponent arg) {
            return arg.resolve(PlayerSkinCache.this.gameProfileResolver).thenCompose(gameProfile -> PlayerSkinCache.this.playerSkinProvider.fetchSkinTextures((GameProfile)gameProfile).thenApply(optional -> optional.map(arg2 -> new Entry((GameProfile)gameProfile, (SkinTextures)arg2, arg.getOverride()))));
        }

        @Override
        public /* synthetic */ Object load(Object profile) throws Exception {
            return this.load((ProfileComponent)profile);
        }
    });
    private final LoadingCache<ProfileComponent, Entry> immediateCache = CacheBuilder.newBuilder().expireAfterAccess(TIME_TO_LIVE).build(new CacheLoader<ProfileComponent, Entry>(){

        @Override
        public Entry load(ProfileComponent arg) {
            GameProfile gameProfile = arg.getGameProfile();
            return new Entry(gameProfile, DefaultSkinHelper.getSkinTextures(gameProfile), arg.getOverride());
        }

        @Override
        public /* synthetic */ Object load(Object profile) throws Exception {
            return this.load((ProfileComponent)profile);
        }
    });
    final TextureManager textureManager;
    final PlayerSkinProvider playerSkinProvider;
    final GameProfileResolver gameProfileResolver;

    public PlayerSkinCache(TextureManager textureManager, PlayerSkinProvider playerSkinProvider, GameProfileResolver gameProfileResolver) {
        this.textureManager = textureManager;
        this.playerSkinProvider = playerSkinProvider;
        this.gameProfileResolver = gameProfileResolver;
    }

    public Entry get(ProfileComponent profile) {
        Entry lv = this.getFuture(profile).getNow(Optional.empty()).orElse(null);
        if (lv != null) {
            return lv;
        }
        return this.immediateCache.getUnchecked(profile);
    }

    public Supplier<Entry> getSupplier(ProfileComponent profile) {
        Entry lv = this.immediateCache.getUnchecked(profile);
        CompletableFuture<Optional<Entry>> completableFuture = this.fetchingCache.getUnchecked(profile);
        Optional optional = completableFuture.getNow(null);
        if (optional != null) {
            Entry lv2 = optional.orElse(lv);
            return () -> lv2;
        }
        return () -> completableFuture.getNow(Optional.empty()).orElse(lv);
    }

    public CompletableFuture<Optional<Entry>> getFuture(ProfileComponent profile) {
        return this.fetchingCache.getUnchecked(profile);
    }

    static RenderLayer getRenderLayer(SkinTextures skinTextures) {
        return SkullBlockEntityRenderer.getTranslucentRenderLayer(skinTextures.body().texturePath());
    }

    @Environment(value=EnvType.CLIENT)
    public final class Entry {
        private final GameProfile profile;
        private final SkinTextures textures;
        @Nullable
        private RenderLayer renderLayer;
        @Nullable
        private GpuTextureView textureView;
        @Nullable
        private TextRenderLayerSet textRenderLayers;

        public Entry(GameProfile profile, SkinTextures textures, SkinTextures.SkinOverride arg3) {
            this.profile = profile;
            this.textures = textures.withOverride(arg3);
        }

        public GameProfile getProfile() {
            return this.profile;
        }

        public SkinTextures getTextures() {
            return this.textures;
        }

        public RenderLayer getRenderLayer() {
            if (this.renderLayer == null) {
                this.renderLayer = PlayerSkinCache.getRenderLayer(this.textures);
            }
            return this.renderLayer;
        }

        public GpuTextureView getTextureView() {
            if (this.textureView == null) {
                this.textureView = PlayerSkinCache.this.textureManager.getTexture(this.textures.body().texturePath()).getGlTextureView();
            }
            return this.textureView;
        }

        public TextRenderLayerSet getTextRenderLayers() {
            if (this.textRenderLayers == null) {
                this.textRenderLayers = TextRenderLayerSet.of(this.textures.body().texturePath());
            }
            return this.textRenderLayers;
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Entry)) return false;
            Entry lv = (Entry)o;
            if (!this.profile.equals(lv.profile)) return false;
            if (!this.textures.equals(lv.textures)) return false;
            return true;
        }

        public int hashCode() {
            int i = 1;
            i = 31 * i + this.profile.hashCode();
            i = 31 * i + this.textures.hashCode();
            return i;
        }
    }
}

