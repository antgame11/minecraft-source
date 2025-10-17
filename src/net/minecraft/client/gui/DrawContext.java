/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.WoodType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.cursor.Cursor;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.render.state.ColoredQuadGuiElementRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.ItemGuiElementRenderState;
import net.minecraft.client.gui.render.state.TextGuiElementRenderState;
import net.minecraft.client.gui.render.state.TexturedQuadGuiElementRenderState;
import net.minecraft.client.gui.render.state.TiledTexturedQuadGuiElementRenderState;
import net.minecraft.client.gui.render.state.special.BannerResultGuiElementRenderState;
import net.minecraft.client.gui.render.state.special.BookModelGuiElementRenderState;
import net.minecraft.client.gui.render.state.special.EntityGuiElementRenderState;
import net.minecraft.client.gui.render.state.special.PlayerSkinGuiElementRenderState;
import net.minecraft.client.gui.render.state.special.ProfilerChartGuiElementRenderState;
import net.minecraft.client.gui.render.state.special.SignGuiElementRenderState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.model.Model;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.MapRenderState;
import net.minecraft.client.render.block.entity.model.BannerFlagBlockModel;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.item.KeyedItemRenderState;
import net.minecraft.client.resource.metadata.GuiResourceMetadata;
import net.minecraft.client.texture.AtlasManager;
import net.minecraft.client.texture.Scaling;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.Window;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Atlases;
import net.minecraft.util.Colors;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.ProfilerTiming;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;
import org.joml.Vector2ic;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class DrawContext {
    private static final int BACKGROUND_MARGIN = 2;
    private final MinecraftClient client;
    private final Matrix3x2fStack matrices;
    private final ScissorStack scissorStack = new ScissorStack();
    private final SpriteHolder spriteHolder;
    private final SpriteAtlasTexture spriteAtlasTexture;
    private final GuiRenderState state;
    private Cursor cursor = Cursor.DEFAULT;
    @Nullable
    private Runnable tooltipDrawer;
    private final List<StrokedRectangle> strokedRectangles = new ArrayList<StrokedRectangle>();

    private DrawContext(MinecraftClient client, Matrix3x2fStack matrices, GuiRenderState state) {
        this.client = client;
        this.matrices = matrices;
        AtlasManager lv = client.getAtlasManager();
        this.spriteHolder = lv;
        this.spriteAtlasTexture = lv.getAtlasTexture(Atlases.GUI);
        this.state = state;
    }

    public DrawContext(MinecraftClient client, GuiRenderState state) {
        this(client, new Matrix3x2fStack(16), state);
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    public void applyCursorTo(Window window) {
        window.setCursor(this.cursor);
    }

    public int getScaledWindowWidth() {
        return this.client.getWindow().getScaledWidth();
    }

    public int getScaledWindowHeight() {
        return this.client.getWindow().getScaledHeight();
    }

    public void createNewRootLayer() {
        this.state.createNewRootLayer();
    }

    public void applyBlur() {
        this.state.applyBlur();
    }

    public Matrix3x2fStack getMatrices() {
        return this.matrices;
    }

    public void drawHorizontalLine(int x1, int x2, int y, int color) {
        if (x2 < x1) {
            int m = x1;
            x1 = x2;
            x2 = m;
        }
        this.fill(x1, y, x2 + 1, y + 1, color);
    }

    public void drawVerticalLine(int x, int y1, int y2, int color) {
        if (y2 < y1) {
            int m = y1;
            y1 = y2;
            y2 = m;
        }
        this.fill(x, y1 + 1, x + 1, y2, color);
    }

    public void enableScissor(int x1, int y1, int x2, int y2) {
        ScreenRect lv = new ScreenRect(x1, y1, x2 - x1, y2 - y1).transform(this.matrices);
        this.scissorStack.push(lv);
    }

    public void disableScissor() {
        this.scissorStack.pop();
    }

    public boolean scissorContains(int x, int y) {
        return this.scissorStack.contains(x, y);
    }

    public void fill(int x1, int y1, int x2, int y2, int color) {
        this.fill(RenderPipelines.GUI, x1, y1, x2, y2, color);
    }

    public void fill(RenderPipeline pipeline, int x1, int y1, int x2, int y2, int color) {
        int n;
        if (x1 < x2) {
            n = x1;
            x1 = x2;
            x2 = n;
        }
        if (y1 < y2) {
            n = y1;
            y1 = y2;
            y2 = n;
        }
        this.fill(pipeline, TextureSetup.empty(), x1, y1, x2, y2, color, null);
    }

    public void fillGradient(int startX, int startY, int endX, int endY, int colorStart, int colorEnd) {
        this.fill(RenderPipelines.GUI, TextureSetup.empty(), startX, startY, endX, endY, colorStart, colorEnd);
    }

    public void fill(RenderPipeline pipeline, TextureSetup textureSetup, int x1, int y1, int x2, int y2) {
        this.fill(pipeline, textureSetup, x1, y1, x2, y2, -1, null);
    }

    private void fill(RenderPipeline pipeline, TextureSetup textureSetup, int x1, int y1, int x2, int y2, int color, @Nullable Integer color2) {
        this.state.addSimpleElement(new ColoredQuadGuiElementRenderState(pipeline, textureSetup, new Matrix3x2f(this.matrices), x1, y1, x2, y2, color, color2 != null ? color2 : color, this.scissorStack.peekLast()));
    }

    public void drawSelection(int x1, int y1, int x2, int y2) {
        this.fill(RenderPipelines.GUI_INVERT, x1, y1, x2, y2, Colors.WHITE);
        this.fill(RenderPipelines.GUI_TEXT_HIGHLIGHT, x1, y1, x2, y2, Colors.BLUE);
    }

    public void drawCenteredTextWithShadow(TextRenderer textRenderer, String text, int centerX, int y, int color) {
        this.drawTextWithShadow(textRenderer, text, centerX - textRenderer.getWidth(text) / 2, y, color);
    }

    public void drawCenteredTextWithShadow(TextRenderer textRenderer, Text text, int centerX, int y, int color) {
        OrderedText lv = text.asOrderedText();
        this.drawTextWithShadow(textRenderer, lv, centerX - textRenderer.getWidth(lv) / 2, y, color);
    }

    public void drawCenteredTextWithShadow(TextRenderer textRenderer, OrderedText text, int centerX, int y, int color) {
        this.drawTextWithShadow(textRenderer, text, centerX - textRenderer.getWidth(text) / 2, y, color);
    }

    public void drawTextWithShadow(TextRenderer textRenderer, @Nullable String text, int x, int y, int color) {
        this.drawText(textRenderer, text, x, y, color, true);
    }

    public void drawText(TextRenderer textRenderer, @Nullable String text, int x, int y, int color, boolean shadow) {
        if (text == null) {
            return;
        }
        this.drawText(textRenderer, Language.getInstance().reorder(StringVisitable.plain(text)), x, y, color, shadow);
    }

    public void drawTextWithShadow(TextRenderer textRenderer, OrderedText text, int x, int y, int color) {
        this.drawText(textRenderer, text, x, y, color, true);
    }

    public void drawText(TextRenderer textRenderer, OrderedText text, int x, int y, int color, boolean shadow) {
        if (ColorHelper.getAlpha(color) == 0) {
            return;
        }
        this.state.addText(new TextGuiElementRenderState(textRenderer, text, new Matrix3x2f(this.matrices), x, y, color, 0, shadow, this.scissorStack.peekLast()));
    }

    public void drawTextWithShadow(TextRenderer textRenderer, Text text, int x, int y, int color) {
        this.drawText(textRenderer, text, x, y, color, true);
    }

    public void drawText(TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow) {
        this.drawText(textRenderer, text.asOrderedText(), x, y, color, shadow);
    }

    public void drawWrappedTextWithShadow(TextRenderer textRenderer, StringVisitable text, int x, int y, int width, int color) {
        this.drawWrappedText(textRenderer, text, x, y, width, color, true);
    }

    public void drawWrappedText(TextRenderer textRenderer, StringVisitable text, int x, int y, int width, int color, boolean shadow) {
        for (OrderedText lv : textRenderer.wrapLines(text, width)) {
            this.drawText(textRenderer, lv, x, y, color, shadow);
            y += textRenderer.fontHeight;
        }
    }

    public void drawTextWithBackground(TextRenderer textRenderer, Text text, int x, int y, int width, int color) {
        int m = this.client.options.getTextBackgroundColor(0.0f);
        if (m != 0) {
            int n = 2;
            this.fill(x - 2, y - 2, x + width + 2, y + textRenderer.fontHeight + 2, ColorHelper.mix(m, color));
        }
        this.drawText(textRenderer, text, x, y, color, true);
    }

    public void drawStrokedRectangle(int x, int y, int width, int height, int color) {
        this.strokedRectangles.add(new StrokedRectangle(x, y, width, height, color));
    }

    public void drawGuiTexture(RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height) {
        this.drawGuiTexture(pipeline, sprite, x, y, width, height, -1);
    }

    public void drawGuiTexture(RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, float alpha) {
        this.drawGuiTexture(pipeline, sprite, x, y, width, height, ColorHelper.withAlpha(alpha, Colors.WHITE));
    }

    private static Scaling getScaling(Sprite sprite) {
        return sprite.getContents().getAdditionalMetadataValue(GuiResourceMetadata.SERIALIZER).orElse(GuiResourceMetadata.DEFAULT).scaling();
    }

    public void drawGuiTexture(RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, int color) {
        Scaling lv2;
        Sprite lv = this.spriteAtlasTexture.getSprite(sprite);
        Scaling scaling = lv2 = DrawContext.getScaling(lv);
        Objects.requireNonNull(scaling);
        Scaling scaling2 = scaling;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Scaling.Stretch.class, Scaling.Tile.class, Scaling.NineSlice.class}, (Object)scaling2, n)) {
            case 0: {
                Scaling.Stretch lv3 = (Scaling.Stretch)scaling2;
                this.drawSpriteStretched(pipeline, lv, x, y, width, height, color);
                break;
            }
            case 1: {
                Scaling.Tile lv4 = (Scaling.Tile)scaling2;
                this.drawSpriteTiled(pipeline, lv, x, y, width, height, 0, 0, lv4.width(), lv4.height(), lv4.width(), lv4.height(), color);
                break;
            }
            case 2: {
                Scaling.NineSlice lv5 = (Scaling.NineSlice)scaling2;
                this.drawSpriteNineSliced(pipeline, lv, lv5, x, y, width, height, color);
                break;
            }
        }
    }

    public void drawGuiTexture(RenderPipeline pipeline, Identifier sprite, int textureWidth, int textureHeight, int u, int v, int x, int y, int width, int height) {
        this.drawGuiTexture(pipeline, sprite, textureWidth, textureHeight, u, v, x, y, width, height, -1);
    }

    public void drawGuiTexture(RenderPipeline pipeline, Identifier sprite, int textureWidth, int textureHeight, int u, int v, int x, int y, int width, int height, int color) {
        Sprite lv = this.spriteAtlasTexture.getSprite(sprite);
        Scaling lv2 = DrawContext.getScaling(lv);
        if (lv2 instanceof Scaling.Stretch) {
            this.drawSpriteRegion(pipeline, lv, textureWidth, textureHeight, u, v, x, y, width, height, color);
        } else {
            this.enableScissor(x, y, x + width, y + height);
            this.drawGuiTexture(pipeline, sprite, x - u, y - v, textureWidth, textureHeight, color);
            this.disableScissor();
        }
    }

    public void drawSpriteStretched(RenderPipeline pipeline, Sprite sprite, int x, int y, int width, int height) {
        this.drawSpriteStretched(pipeline, sprite, x, y, width, height, -1);
    }

    public void drawSpriteStretched(RenderPipeline pipeline, Sprite sprite, int x, int y, int width, int height, int color) {
        if (width == 0 || height == 0) {
            return;
        }
        this.drawTexturedQuad(pipeline, sprite.getAtlasId(), x, x + width, y, y + height, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV(), color);
    }

    private void drawSpriteRegion(RenderPipeline pipeline, Sprite sprite, int textureWidth, int textureHeight, int u, int v, int x, int y, int width, int height, int color) {
        if (width == 0 || height == 0) {
            return;
        }
        this.drawTexturedQuad(pipeline, sprite.getAtlasId(), x, x + width, y, y + height, sprite.getFrameU((float)u / (float)textureWidth), sprite.getFrameU((float)(u + width) / (float)textureWidth), sprite.getFrameV((float)v / (float)textureHeight), sprite.getFrameV((float)(v + height) / (float)textureHeight), color);
    }

    private void drawSpriteNineSliced(RenderPipeline pipeline, Sprite sprite, Scaling.NineSlice nineSlice, int x, int y, int width, int height, int color) {
        Scaling.NineSlice.Border lv = nineSlice.border();
        int n = Math.min(lv.left(), width / 2);
        int o = Math.min(lv.right(), width / 2);
        int p = Math.min(lv.top(), height / 2);
        int q = Math.min(lv.bottom(), height / 2);
        if (width == nineSlice.width() && height == nineSlice.height()) {
            this.drawSpriteRegion(pipeline, sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, width, height, color);
            return;
        }
        if (height == nineSlice.height()) {
            this.drawSpriteRegion(pipeline, sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, n, height, color);
            this.drawInnerSprite(pipeline, nineSlice, sprite, x + n, y, width - o - n, height, n, 0, nineSlice.width() - o - n, nineSlice.height(), nineSlice.width(), nineSlice.height(), color);
            this.drawSpriteRegion(pipeline, sprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - o, 0, x + width - o, y, o, height, color);
            return;
        }
        if (width == nineSlice.width()) {
            this.drawSpriteRegion(pipeline, sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, width, p, color);
            this.drawInnerSprite(pipeline, nineSlice, sprite, x, y + p, width, height - q - p, 0, p, nineSlice.width(), nineSlice.height() - q - p, nineSlice.width(), nineSlice.height(), color);
            this.drawSpriteRegion(pipeline, sprite, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - q, x, y + height - q, width, q, color);
            return;
        }
        this.drawSpriteRegion(pipeline, sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, n, p, color);
        this.drawInnerSprite(pipeline, nineSlice, sprite, x + n, y, width - o - n, p, n, 0, nineSlice.width() - o - n, p, nineSlice.width(), nineSlice.height(), color);
        this.drawSpriteRegion(pipeline, sprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - o, 0, x + width - o, y, o, p, color);
        this.drawSpriteRegion(pipeline, sprite, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - q, x, y + height - q, n, q, color);
        this.drawInnerSprite(pipeline, nineSlice, sprite, x + n, y + height - q, width - o - n, q, n, nineSlice.height() - q, nineSlice.width() - o - n, q, nineSlice.width(), nineSlice.height(), color);
        this.drawSpriteRegion(pipeline, sprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - o, nineSlice.height() - q, x + width - o, y + height - q, o, q, color);
        this.drawInnerSprite(pipeline, nineSlice, sprite, x, y + p, n, height - q - p, 0, p, n, nineSlice.height() - q - p, nineSlice.width(), nineSlice.height(), color);
        this.drawInnerSprite(pipeline, nineSlice, sprite, x + n, y + p, width - o - n, height - q - p, n, p, nineSlice.width() - o - n, nineSlice.height() - q - p, nineSlice.width(), nineSlice.height(), color);
        this.drawInnerSprite(pipeline, nineSlice, sprite, x + width - o, y + p, o, height - q - p, nineSlice.width() - o, p, o, nineSlice.height() - q - p, nineSlice.width(), nineSlice.height(), color);
    }

    private void drawInnerSprite(RenderPipeline pipeline, Scaling.NineSlice nineSlice, Sprite sprite, int x, int y, int width, int height, int u, int v, int tileWidth, int tileHeight, int textureWidth, int textureHeight, int color) {
        if (width <= 0 || height <= 0) {
            return;
        }
        if (nineSlice.stretchInner()) {
            this.drawTexturedQuad(pipeline, sprite.getAtlasId(), x, x + width, y, y + height, sprite.getFrameU((float)u / (float)textureWidth), sprite.getFrameU((float)(u + tileWidth) / (float)textureWidth), sprite.getFrameV((float)v / (float)textureHeight), sprite.getFrameV((float)(v + tileHeight) / (float)textureHeight), color);
        } else {
            this.drawSpriteTiled(pipeline, sprite, x, y, width, height, u, v, tileWidth, tileHeight, textureWidth, textureHeight, color);
        }
    }

    private void drawSpriteTiled(RenderPipeline pipeline, Sprite sprite, int x, int y, int width, int height, int u, int v, int tileWidth, int tileHeight, int textureWidth, int textureHeight, int color) {
        if (width <= 0 || height <= 0) {
            return;
        }
        if (tileWidth <= 0 || tileHeight <= 0) {
            throw new IllegalArgumentException("Tile size must be positive, got " + tileWidth + "x" + tileHeight);
        }
        GpuTextureView gpuTextureView = this.client.getTextureManager().getTexture(sprite.getAtlasId()).getGlTextureView();
        this.drawTiledTexturedQuad(pipeline, gpuTextureView, tileWidth, tileHeight, x, y, x + width, y + height, sprite.getFrameU((float)u / (float)textureWidth), sprite.getFrameU((float)(u + tileWidth) / (float)textureWidth), sprite.getFrameV((float)v / (float)textureHeight), sprite.getFrameV((float)(v + tileHeight) / (float)textureHeight), color);
    }

    public void drawTexture(RenderPipeline pipeline, Identifier sprite, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color) {
        this.drawTexture(pipeline, sprite, x, y, u, v, width, height, width, height, textureWidth, textureHeight, color);
    }

    public void drawTexture(RenderPipeline pipeline, Identifier sprite, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        this.drawTexture(pipeline, sprite, x, y, u, v, width, height, width, height, textureWidth, textureHeight);
    }

    public void drawTexture(RenderPipeline pipeline, Identifier sprite, int x, int y, float u, float v, int width, int height, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        this.drawTexture(pipeline, sprite, x, y, u, v, width, height, regionWidth, regionHeight, textureWidth, textureHeight, -1);
    }

    public void drawTexture(RenderPipeline pipeline, Identifier sprite, int x, int y, float u, float v, int width, int height, int regionWidth, int regionHeight, int textureWidth, int textureHeight, int color) {
        this.drawTexturedQuad(pipeline, sprite, x, x + width, y, y + height, (u + 0.0f) / (float)textureWidth, (u + (float)regionWidth) / (float)textureWidth, (v + 0.0f) / (float)textureHeight, (v + (float)regionHeight) / (float)textureHeight, color);
    }

    public void drawTexturedQuad(Identifier sprite, int x1, int y1, int x2, int y2, float u1, float u2, float v1, float v2) {
        this.drawTexturedQuad(RenderPipelines.GUI_TEXTURED, sprite, x1, x2, y1, y2, u1, u2, v1, v2, -1);
    }

    private void drawTexturedQuad(RenderPipeline pipeline, Identifier sprite, int x1, int x2, int y1, int y2, float u1, float u2, float v1, float v2, int color) {
        GpuTextureView gpuTextureView = this.client.getTextureManager().getTexture(sprite).getGlTextureView();
        this.drawTexturedQuad(pipeline, gpuTextureView, x1, y1, x2, y2, u1, u2, v1, v2, color);
    }

    private void drawTexturedQuad(RenderPipeline pipeline, GpuTextureView texture, int x1, int y1, int x2, int y2, float u1, float u2, float v1, float v2, int color) {
        this.state.addSimpleElement(new TexturedQuadGuiElementRenderState(pipeline, TextureSetup.withoutGlTexture(texture), new Matrix3x2f(this.matrices), x1, y1, x2, y2, u1, u2, v1, v2, color, this.scissorStack.peekLast()));
    }

    private void drawTiledTexturedQuad(RenderPipeline pipeline, GpuTextureView texture, int tileWidth, int tileHeight, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color) {
        this.state.addSimpleElement(new TiledTexturedQuadGuiElementRenderState(pipeline, TextureSetup.withoutGlTexture(texture), new Matrix3x2f(this.matrices), tileWidth, tileHeight, x0, y0, x1, y1, u0, u1, v0, v1, color, this.scissorStack.peekLast()));
    }

    public void drawItem(ItemStack item, int x, int y) {
        this.drawItem(this.client.player, this.client.world, item, x, y, 0);
    }

    public void drawItem(ItemStack stack, int x, int y, int seed) {
        this.drawItem(this.client.player, this.client.world, stack, x, y, seed);
    }

    public void drawItemWithoutEntity(ItemStack stack, int x, int y) {
        this.drawItemWithoutEntity(stack, x, y, 0);
    }

    public void drawItemWithoutEntity(ItemStack stack, int x, int y, int seed) {
        this.drawItem(null, this.client.world, stack, x, y, seed);
    }

    public void drawItem(LivingEntity entity, ItemStack stack, int x, int y, int seed) {
        this.drawItem(entity, entity.getEntityWorld(), stack, x, y, seed);
    }

    private void drawItem(@Nullable LivingEntity entity, @Nullable World world, ItemStack stack, int x, int y, int seed) {
        if (stack.isEmpty()) {
            return;
        }
        KeyedItemRenderState lv = new KeyedItemRenderState();
        this.client.getItemModelManager().clearAndUpdate(lv, stack, ItemDisplayContext.GUI, world, entity, seed);
        try {
            this.state.addItem(new ItemGuiElementRenderState(stack.getItem().getName().toString(), new Matrix3x2f(this.matrices), lv, x, y, this.scissorStack.peekLast()));
        } catch (Throwable throwable) {
            CrashReport lv2 = CrashReport.create(throwable, "Rendering item");
            CrashReportSection lv3 = lv2.addElement("Item being rendered");
            lv3.add("Item Type", () -> String.valueOf(stack.getItem()));
            lv3.add("Item Components", () -> String.valueOf(stack.getComponents()));
            lv3.add("Item Foil", () -> String.valueOf(stack.hasGlint()));
            throw new CrashException(lv2);
        }
    }

    public void drawStackOverlay(TextRenderer textRenderer, ItemStack stack, int x, int y) {
        this.drawStackOverlay(textRenderer, stack, x, y, null);
    }

    public void drawStackOverlay(TextRenderer textRenderer, ItemStack stack, int x, int y, @Nullable String stackCountText) {
        if (stack.isEmpty()) {
            return;
        }
        this.matrices.pushMatrix();
        this.drawItemBar(stack, x, y);
        this.drawCooldownProgress(stack, x, y);
        this.drawStackCount(textRenderer, stack, x, y, stackCountText);
        this.matrices.popMatrix();
    }

    public void drawTooltip(Text text, int x, int y) {
        this.drawTooltip(List.of(text.asOrderedText()), x, y);
    }

    public void drawTooltip(List<OrderedText> text, int x, int y) {
        this.drawTooltip(this.client.textRenderer, text, HoveredTooltipPositioner.INSTANCE, x, y, false);
    }

    public void drawItemTooltip(TextRenderer textRenderer, ItemStack stack, int x, int y) {
        this.drawTooltip(textRenderer, Screen.getTooltipFromItem(this.client, stack), stack.getTooltipData(), x, y, stack.get(DataComponentTypes.TOOLTIP_STYLE));
    }

    public void drawTooltip(TextRenderer textRenderer, List<Text> text, Optional<TooltipData> data, int x, int y) {
        this.drawTooltip(textRenderer, text, data, x, y, null);
    }

    public void drawTooltip(TextRenderer textRenderer, List<Text> text, Optional<TooltipData> data, int x, int y, @Nullable Identifier texture) {
        List<TooltipComponent> list2 = text.stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Util.toArrayList());
        data.ifPresent(datax -> list2.add(list2.isEmpty() ? 0 : 1, TooltipComponent.of(datax)));
        this.drawTooltip(textRenderer, list2, x, y, HoveredTooltipPositioner.INSTANCE, texture, false);
    }

    public void drawTooltip(TextRenderer textRenderer, Text text, int x, int y) {
        this.drawTooltip(textRenderer, text, x, y, null);
    }

    public void drawTooltip(TextRenderer textRenderer, Text text, int x, int y, @Nullable Identifier texture) {
        this.drawOrderedTooltip(textRenderer, List.of(text.asOrderedText()), x, y, texture);
    }

    public void drawTooltip(TextRenderer textRenderer, List<Text> text, int x, int y) {
        this.drawTooltip(textRenderer, text, x, y, null);
    }

    public void drawTooltip(TextRenderer textRenderer, List<Text> text, int x, int y, @Nullable Identifier texture) {
        this.drawTooltip(textRenderer, text.stream().map(Text::asOrderedText).map(TooltipComponent::of).toList(), x, y, HoveredTooltipPositioner.INSTANCE, texture, false);
    }

    public void drawOrderedTooltip(TextRenderer textRenderer, List<? extends OrderedText> text, int x, int y) {
        this.drawOrderedTooltip(textRenderer, text, x, y, null);
    }

    public void drawOrderedTooltip(TextRenderer textRenderer, List<? extends OrderedText> text, int x, int y, @Nullable Identifier texture) {
        this.drawTooltip(textRenderer, text.stream().map(TooltipComponent::of).collect(Collectors.toList()), x, y, HoveredTooltipPositioner.INSTANCE, texture, false);
    }

    public void drawTooltip(TextRenderer textRenderer, List<OrderedText> text, TooltipPositioner positioner, int x, int y, boolean focused) {
        this.drawTooltip(textRenderer, text.stream().map(TooltipComponent::of).collect(Collectors.toList()), x, y, positioner, null, focused);
    }

    private void drawTooltip(TextRenderer textRenderer, List<TooltipComponent> components, int x, int y, TooltipPositioner positioner, @Nullable Identifier texture, boolean focused) {
        if (components.isEmpty()) {
            return;
        }
        if (this.tooltipDrawer == null || focused) {
            this.tooltipDrawer = () -> this.drawTooltipImmediately(textRenderer, components, x, y, positioner, texture);
        }
    }

    public void drawTooltipImmediately(TextRenderer textRenderer, List<TooltipComponent> components, int x, int y, TooltipPositioner positioner, @Nullable Identifier texture) {
        TooltipComponent lv2;
        int s;
        int k = 0;
        int l = components.size() == 1 ? -2 : 0;
        for (TooltipComponent lv : components) {
            int m = lv.getWidth(textRenderer);
            if (m > k) {
                k = m;
            }
            l += lv.getHeight(textRenderer);
        }
        int n = k;
        int o = l;
        Vector2ic vector2ic = positioner.getPosition(this.getScaledWindowWidth(), this.getScaledWindowHeight(), x, y, n, o);
        int p = vector2ic.x();
        int q = vector2ic.y();
        this.matrices.pushMatrix();
        TooltipBackgroundRenderer.render(this, p, q, n, o, texture);
        int r = q;
        for (s = 0; s < components.size(); ++s) {
            lv2 = components.get(s);
            lv2.drawText(this, textRenderer, p, r);
            r += lv2.getHeight(textRenderer) + (s == 0 ? 2 : 0);
        }
        r = q;
        for (s = 0; s < components.size(); ++s) {
            lv2 = components.get(s);
            lv2.drawItems(textRenderer, p, r, n, o, this);
            r += lv2.getHeight(textRenderer) + (s == 0 ? 2 : 0);
        }
        this.matrices.popMatrix();
    }

    public void drawDeferredElements() {
        if (!this.strokedRectangles.isEmpty()) {
            this.createNewRootLayer();
            for (StrokedRectangle lv : this.strokedRectangles) {
                lv.draw(this);
            }
            this.strokedRectangles.clear();
        }
        if (this.tooltipDrawer != null) {
            this.createNewRootLayer();
            this.tooltipDrawer.run();
            this.tooltipDrawer = null;
        }
    }

    private void drawItemBar(ItemStack stack, int x, int y) {
        if (stack.isItemBarVisible()) {
            int k = x + 2;
            int l = y + 13;
            this.fill(RenderPipelines.GUI, k, l, k + 13, l + 2, Colors.BLACK);
            this.fill(RenderPipelines.GUI, k, l, k + stack.getItemBarStep(), l + 1, ColorHelper.fullAlpha(stack.getItemBarColor()));
        }
    }

    private void drawStackCount(TextRenderer textRenderer, ItemStack stack, int x, int y, @Nullable String stackCountText) {
        if (stack.getCount() != 1 || stackCountText != null) {
            String string2 = stackCountText == null ? String.valueOf(stack.getCount()) : stackCountText;
            this.drawText(textRenderer, string2, x + 19 - 2 - textRenderer.getWidth(string2), y + 6 + 3, Colors.WHITE, true);
        }
    }

    private void drawCooldownProgress(ItemStack stack, int x, int y) {
        float f;
        ClientPlayerEntity lv = this.client.player;
        float f2 = f = lv == null ? 0.0f : lv.getItemCooldownManager().getCooldownProgress(stack, this.client.getRenderTickCounter().getTickProgress(true));
        if (f > 0.0f) {
            int k = y + MathHelper.floor(16.0f * (1.0f - f));
            int l = k + MathHelper.ceil(16.0f * f);
            this.fill(RenderPipelines.GUI, x, k, x + 16, l, Integer.MAX_VALUE);
        }
    }

    /*
     * WARNING - Removed back jump from a try to a catch block - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void drawHoverEvent(TextRenderer textRenderer, @Nullable Style style, int x, int y) {
        if (style == null) {
            return;
        }
        if (style.getClickEvent() != null) {
            this.setCursor(StandardCursors.POINTING_HAND);
        }
        if (style.getHoverEvent() == null) return;
        HoverEvent hoverEvent = style.getHoverEvent();
        Objects.requireNonNull(hoverEvent);
        HoverEvent hoverEvent2 = hoverEvent;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{HoverEvent.ShowItem.class, HoverEvent.ShowEntity.class, HoverEvent.ShowText.class}, (Object)hoverEvent2, n)) {
            case 0: {
                HoverEvent.ShowItem showItem = (HoverEvent.ShowItem)hoverEvent2;
                try {
                    ItemStack itemStack;
                    ItemStack lv = itemStack = showItem.item();
                    this.drawItemTooltip(textRenderer, lv, x, y);
                    return;
                } catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
            }
            case 1: {
                HoverEvent.ShowEntity showEntity = (HoverEvent.ShowEntity)hoverEvent2;
                {
                    HoverEvent.EntityContent entityContent;
                    HoverEvent.EntityContent lv2 = entityContent = showEntity.entity();
                    if (!this.client.options.advancedItemTooltips) return;
                    this.drawTooltip(textRenderer, lv2.asTooltip(), x, y);
                    return;
                }
            }
            case 2: {
                HoverEvent.ShowText showText = (HoverEvent.ShowText)hoverEvent2;
                {
                    Text text;
                    Text lv3 = text = showText.value();
                    this.drawOrderedTooltip(textRenderer, textRenderer.wrapLines(lv3, Math.max(this.getScaledWindowWidth() / 2, 200)), x, y);
                    return;
                }
            }
        }
    }

    public void drawMap(MapRenderState mapState) {
        MinecraftClient lv = MinecraftClient.getInstance();
        TextureManager lv2 = lv.getTextureManager();
        GpuTextureView gpuTextureView = lv2.getTexture(mapState.texture).getGlTextureView();
        this.drawTexturedQuad(RenderPipelines.GUI_TEXTURED, gpuTextureView, 0, 0, 128, 128, 0.0f, 1.0f, 0.0f, 1.0f, -1);
        for (MapRenderState.Decoration lv3 : mapState.decorations) {
            if (!lv3.alwaysRendered) continue;
            this.matrices.pushMatrix();
            this.matrices.translate((float)lv3.x / 2.0f + 64.0f, (float)lv3.z / 2.0f + 64.0f);
            this.matrices.rotate((float)Math.PI / 180 * (float)lv3.rotation * 360.0f / 16.0f);
            this.matrices.scale(4.0f, 4.0f);
            this.matrices.translate(-0.125f, 0.125f);
            Sprite lv4 = lv3.sprite;
            if (lv4 != null) {
                GpuTextureView gpuTextureView2 = lv2.getTexture(lv4.getAtlasId()).getGlTextureView();
                this.drawTexturedQuad(RenderPipelines.GUI_TEXTURED, gpuTextureView2, -1, -1, 1, 1, lv4.getMinU(), lv4.getMaxU(), lv4.getMaxV(), lv4.getMinV(), -1);
            }
            this.matrices.popMatrix();
            if (lv3.name == null) continue;
            TextRenderer lv5 = lv.textRenderer;
            float f = lv5.getWidth(lv3.name);
            float f2 = 25.0f / f;
            Objects.requireNonNull(lv5);
            float g = MathHelper.clamp(f2, 0.0f, 6.0f / 9.0f);
            this.matrices.pushMatrix();
            this.matrices.translate((float)lv3.x / 2.0f + 64.0f - f * g / 2.0f, (float)lv3.z / 2.0f + 64.0f + 4.0f);
            this.matrices.scale(g, g);
            this.state.addText(new TextGuiElementRenderState(lv5, lv3.name.asOrderedText(), new Matrix3x2f(this.matrices), 0, 0, -1, Integer.MIN_VALUE, false, this.scissorStack.peekLast()));
            this.matrices.popMatrix();
        }
    }

    public void addEntity(EntityRenderState entityState, float scale, Vector3f translation, Quaternionf rotation, @Nullable Quaternionf overrideCameraAngle, int x1, int y1, int x2, int y2) {
        this.state.addSpecialElement(new EntityGuiElementRenderState(entityState, translation, rotation, overrideCameraAngle, x1, y1, x2, y2, scale, this.scissorStack.peekLast()));
    }

    public void addPlayerSkin(PlayerEntityModel playerModel, Identifier texture, float scale, float xRotation, float yRotation, float yPivot, int x1, int y1, int x2, int y2) {
        this.state.addSpecialElement(new PlayerSkinGuiElementRenderState(playerModel, texture, xRotation, yRotation, yPivot, x1, y1, x2, y2, scale, this.scissorStack.peekLast()));
    }

    public void addBookModel(BookModel bookModel, Identifier texture, float scale, float open, float flip, int x1, int y1, int x2, int y2) {
        this.state.addSpecialElement(new BookModelGuiElementRenderState(bookModel, texture, open, flip, x1, y1, x2, y2, scale, this.scissorStack.peekLast()));
    }

    public void addBannerResult(BannerFlagBlockModel bannerModel, DyeColor baseColor, BannerPatternsComponent resultBannerPatterns, int x1, int y1, int x2, int y2) {
        this.state.addSpecialElement(new BannerResultGuiElementRenderState(bannerModel, baseColor, resultBannerPatterns, x1, y1, x2, y2, this.scissorStack.peekLast()));
    }

    public void addSign(Model.SinglePartModel model, float scale, WoodType woodType, int x1, int y1, int x2, int y2) {
        this.state.addSpecialElement(new SignGuiElementRenderState(model, woodType, x1, y1, x2, y2, scale, this.scissorStack.peekLast()));
    }

    public void addProfilerChart(List<ProfilerTiming> chartData, int x1, int y1, int x2, int y2) {
        this.state.addSpecialElement(new ProfilerChartGuiElementRenderState(chartData, x1, y1, x2, y2, this.scissorStack.peekLast()));
    }

    public Sprite getSprite(SpriteIdentifier id) {
        return this.spriteHolder.getSprite(id);
    }

    @Environment(value=EnvType.CLIENT)
    static class ScissorStack {
        private final Deque<ScreenRect> stack = new ArrayDeque<ScreenRect>();

        ScissorStack() {
        }

        public ScreenRect push(ScreenRect rect) {
            ScreenRect lv = this.stack.peekLast();
            if (lv != null) {
                ScreenRect lv2 = Objects.requireNonNullElse(rect.intersection(lv), ScreenRect.empty());
                this.stack.addLast(lv2);
                return lv2;
            }
            this.stack.addLast(rect);
            return rect;
        }

        @Nullable
        public ScreenRect pop() {
            if (this.stack.isEmpty()) {
                throw new IllegalStateException("Scissor stack underflow");
            }
            this.stack.removeLast();
            return this.stack.peekLast();
        }

        @Nullable
        public ScreenRect peekLast() {
            return this.stack.peekLast();
        }

        public boolean contains(int x, int y) {
            if (this.stack.isEmpty()) {
                return true;
            }
            return this.stack.peek().contains(x, y);
        }
    }

    @Environment(value=EnvType.CLIENT)
    record StrokedRectangle(int x, int y, int width, int height, int color) {
        public void draw(DrawContext context) {
            context.fill(this.x, this.y, this.x + this.width, this.y + 1, this.color);
            context.fill(this.x, this.y + this.height - 1, this.x + this.width, this.y + this.height, this.color);
            context.fill(this.x, this.y + 1, this.x + 1, this.y + this.height - 1, this.color);
            context.fill(this.x + this.width - 1, this.y + 1, this.x + this.width, this.y + this.height - 1, this.color);
        }
    }
}

