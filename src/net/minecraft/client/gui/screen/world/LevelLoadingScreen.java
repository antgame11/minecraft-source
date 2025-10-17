/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.world;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.render.block.entity.AbstractEndPortalBlockEntityRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.world.ClientChunkLoadProgress;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.ChunkLoadMap;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class LevelLoadingScreen
extends Screen {
    private static final Text DOWNLOADING_TERRAIN_TEXT = Text.translatable("multiplayer.downloadingTerrain");
    private static final Text READY_TO_PLAY_MESSAGE = Text.translatable("narrator.ready_to_play");
    private static final long NARRATION_DELAY = 2000L;
    private static final int field_61630 = 200;
    private ClientChunkLoadProgress chunkLoadProgress;
    private float loadProgress;
    private long lastNarrationTime = -1L;
    private WorldEntryReason reason;
    @Nullable
    private Sprite netherPortalSprite;
    private static final Object2IntMap<ChunkStatus> STATUS_TO_COLOR = Util.make(new Object2IntOpenHashMap(), map -> {
        map.defaultReturnValue(0);
        map.put(ChunkStatus.EMPTY, 0x545454);
        map.put(ChunkStatus.STRUCTURE_STARTS, 0x999999);
        map.put(ChunkStatus.STRUCTURE_REFERENCES, 6250897);
        map.put(ChunkStatus.BIOMES, 8434258);
        map.put(ChunkStatus.NOISE, 0xD1D1D1);
        map.put(ChunkStatus.SURFACE, 7497737);
        map.put(ChunkStatus.CARVERS, 3159410);
        map.put(ChunkStatus.FEATURES, 2213376);
        map.put(ChunkStatus.INITIALIZE_LIGHT, 0xCCCCCC);
        map.put(ChunkStatus.LIGHT, 16769184);
        map.put(ChunkStatus.SPAWN, 15884384);
        map.put(ChunkStatus.FULL, 0xFFFFFF);
    });

    public LevelLoadingScreen(ClientChunkLoadProgress progressProvider, WorldEntryReason reason) {
        super(NarratorManager.EMPTY);
        this.chunkLoadProgress = progressProvider;
        this.reason = reason;
    }

    public void init(ClientChunkLoadProgress chunkLoadProgress, WorldEntryReason reason) {
        this.chunkLoadProgress = chunkLoadProgress;
        this.reason = reason;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected boolean hasUsageText() {
        return false;
    }

    @Override
    protected void addElementNarrations(NarrationMessageBuilder builder) {
        if (this.chunkLoadProgress.hasProgress()) {
            builder.put(NarrationPart.TITLE, (Text)Text.translatable("loading.progress", MathHelper.floor(this.chunkLoadProgress.getLoadProgress() * 100.0f)));
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.loadProgress += (this.chunkLoadProgress.getLoadProgress() - this.loadProgress) * 0.2f;
        if (this.chunkLoadProgress.isDone()) {
            this.close();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        int o;
        super.render(context, mouseX, mouseY, deltaTicks);
        long l = Util.getMeasuringTimeMs();
        if (l - this.lastNarrationTime > 2000L) {
            this.lastNarrationTime = l;
            this.narrateScreenIfNarrationEnabled(true);
        }
        int k = this.width / 2;
        int m = this.height / 2;
        ChunkLoadMap lv = this.chunkLoadProgress.getChunkLoadMap();
        if (lv != null) {
            int n = 2;
            LevelLoadingScreen.drawChunkMap(context, k, m, 2, 0, lv);
            o = m - lv.getRadius() * 2 - this.textRenderer.fontHeight * 3;
        } else {
            o = m - 50;
        }
        context.drawCenteredTextWithShadow(this.textRenderer, DOWNLOADING_TERRAIN_TEXT, k, o, Colors.WHITE);
        if (this.chunkLoadProgress.hasProgress()) {
            this.drawLoadingBar(context, k - 100, o + this.textRenderer.fontHeight + 3, 200, 2, this.loadProgress);
        }
    }

    private void drawLoadingBar(DrawContext context, int x1, int y1, int width, int height, float delta) {
        context.fill(x1, y1, x1 + width, y1 + height, Colors.BLACK);
        context.fill(x1, y1, x1 + Math.round(delta * (float)width), y1 + height, Colors.GREEN);
    }

    public static void drawChunkMap(DrawContext context, int centerX, int centerY, int chunkLength, int chunkGap, ChunkLoadMap map) {
        int r;
        int m = chunkLength + chunkGap;
        int n = map.getRadius() * 2 + 1;
        int o = n * m - chunkGap;
        int p = centerX - o / 2;
        int q = centerY - o / 2;
        if (SharedConstants.CHUNKS) {
            r = m / 2 + 1;
            context.fill(centerX - r, centerY - r, centerX + r, centerY + r, Colors.RED);
        }
        for (r = 0; r < n; ++r) {
            for (int s = 0; s < n; ++s) {
                ChunkStatus lv = map.getStatus(r, s);
                int t = p + r * m;
                int u = q + s * m;
                context.fill(t, u, t + chunkLength, u + chunkLength, ColorHelper.fullAlpha(STATUS_TO_COLOR.getInt(lv)));
            }
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        switch (this.reason.ordinal()) {
            case 2: {
                this.renderPanoramaBackground(context, deltaTicks);
                this.applyBlur(context);
                this.renderDarkening(context);
                break;
            }
            case 0: {
                context.drawSpriteStretched(RenderPipelines.GUI_OPAQUE_TEX_BG, this.getNetherPortalSprite(), 0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight());
                break;
            }
            case 1: {
                TextureManager lv = MinecraftClient.getInstance().getTextureManager();
                TextureSetup lv2 = TextureSetup.of(lv.getTexture(AbstractEndPortalBlockEntityRenderer.SKY_TEXTURE).getGlTextureView(), lv.getTexture(AbstractEndPortalBlockEntityRenderer.PORTAL_TEXTURE).getGlTextureView());
                context.fill(RenderPipelines.END_PORTAL, lv2, 0, 0, this.width, this.height);
            }
        }
    }

    private Sprite getNetherPortalSprite() {
        if (this.netherPortalSprite != null) {
            return this.netherPortalSprite;
        }
        this.netherPortalSprite = this.client.getBlockRenderManager().getModels().getModelParticleSprite(Blocks.NETHER_PORTAL.getDefaultState());
        return this.netherPortalSprite;
    }

    @Override
    public void close() {
        this.client.getNarratorManager().narrateSystemImmediately(READY_TO_PLAY_MESSAGE);
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum WorldEntryReason {
        NETHER_PORTAL,
        END_PORTAL,
        OTHER;

    }
}

