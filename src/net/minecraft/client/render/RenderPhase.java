/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

@Environment(value=EnvType.CLIENT)
public abstract class RenderPhase {
    public static final double field_42230 = 8.0;
    protected final String name;
    private final Runnable beginAction;
    private final Runnable endAction;
    protected static final Texture MIPMAP_BLOCK_ATLAS_TEXTURE = new Texture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, true);
    protected static final Texture BLOCK_ATLAS_TEXTURE = new Texture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, false);
    protected static final TextureBase NO_TEXTURE = new TextureBase();
    protected static final Texturing DEFAULT_TEXTURING = new Texturing("default_texturing", () -> {}, () -> {});
    protected static final Texturing GLINT_TEXTURING = new Texturing("glint_texturing", () -> RenderPhase.setupGlintTexturing(8.0f), RenderSystem::resetTextureMatrix);
    protected static final Texturing ENTITY_GLINT_TEXTURING = new Texturing("entity_glint_texturing", () -> RenderPhase.setupGlintTexturing(0.5f), RenderSystem::resetTextureMatrix);
    protected static final Texturing ARMOR_ENTITY_GLINT_TEXTURING = new Texturing("armor_entity_glint_texturing", () -> RenderPhase.setupGlintTexturing(0.16f), RenderSystem::resetTextureMatrix);
    protected static final Lightmap ENABLE_LIGHTMAP = new Lightmap(true);
    protected static final Lightmap DISABLE_LIGHTMAP = new Lightmap(false);
    protected static final Overlay ENABLE_OVERLAY_COLOR = new Overlay(true);
    protected static final Overlay DISABLE_OVERLAY_COLOR = new Overlay(false);
    protected static final Layering NO_LAYERING = new Layering("no_layering", () -> {}, () -> {});
    protected static final Layering VIEW_OFFSET_Z_LAYERING = new Layering("view_offset_z_layering", () -> {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        RenderSystem.getProjectionType().apply(matrix4fStack, 1.0f);
    }, () -> {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.popMatrix();
    });
    protected static final Layering VIEW_OFFSET_Z_LAYERING_FORWARD = new Layering("view_offset_z_layering_forward", () -> {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        RenderSystem.getProjectionType().apply(matrix4fStack, -1.0f);
    }, () -> {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.popMatrix();
    });
    protected static final Target MAIN_TARGET = new Target("main_target", () -> MinecraftClient.getInstance().getFramebuffer());
    protected static final Target OUTLINE_TARGET = new Target("outline_target", () -> {
        Framebuffer lv = MinecraftClient.getInstance().worldRenderer.getEntityOutlinesFramebuffer();
        if (lv != null) {
            return lv;
        }
        return MinecraftClient.getInstance().getFramebuffer();
    });
    protected static final Target WEATHER_TARGET = new Target("weather_target", () -> {
        Framebuffer lv = MinecraftClient.getInstance().worldRenderer.getWeatherFramebuffer();
        if (lv != null) {
            return lv;
        }
        return MinecraftClient.getInstance().getFramebuffer();
    });
    protected static final Target ITEM_ENTITY_TARGET = new Target("item_entity_target", () -> {
        Framebuffer lv = MinecraftClient.getInstance().worldRenderer.getEntityFramebuffer();
        if (lv != null) {
            return lv;
        }
        return MinecraftClient.getInstance().getFramebuffer();
    });
    protected static final LineWidth FULL_LINE_WIDTH = new LineWidth(OptionalDouble.of(1.0));

    public RenderPhase(String name, Runnable beginAction, Runnable endAction) {
        this.name = name;
        this.beginAction = beginAction;
        this.endAction = endAction;
    }

    public void startDrawing() {
        this.beginAction.run();
    }

    public void endDrawing() {
        this.endAction.run();
    }

    public String toString() {
        return this.name;
    }

    public String getName() {
        return this.name;
    }

    private static void setupGlintTexturing(float scale) {
        long l = (long)((double)Util.getMeasuringTimeMs() * MinecraftClient.getInstance().options.getGlintSpeed().getValue() * 8.0);
        float g = (float)(l % 110000L) / 110000.0f;
        float h = (float)(l % 30000L) / 30000.0f;
        Matrix4f matrix4f = new Matrix4f().translation(-g, h, 0.0f);
        matrix4f.rotateZ(0.17453292f).scale(scale);
        RenderSystem.setTextureMatrix(matrix4f);
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Texture
    extends TextureBase {
        private final Optional<Identifier> id;
        private final boolean mipmap;

        public Texture(Identifier id, boolean mipmap) {
            super(() -> {
                TextureManager lv = MinecraftClient.getInstance().getTextureManager();
                AbstractTexture lv2 = lv.getTexture(id);
                lv2.setUseMipmaps(mipmap);
                RenderSystem.setShaderTexture(0, lv2.getGlTextureView());
            }, () -> {});
            this.id = Optional.of(id);
            this.mipmap = mipmap;
        }

        @Override
        public String toString() {
            return this.name + "[" + String.valueOf(this.id) + "(mipmap=" + this.mipmap + ")]";
        }

        @Override
        protected Optional<Identifier> getId() {
            return this.id;
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class TextureBase
    extends RenderPhase {
        public TextureBase(Runnable apply, Runnable unapply) {
            super("texture", apply, unapply);
        }

        TextureBase() {
            super("texture", () -> {}, () -> {});
        }

        protected Optional<Identifier> getId() {
            return Optional.empty();
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Texturing
    extends RenderPhase {
        public Texturing(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Lightmap
    extends Toggleable {
        public Lightmap(boolean lightmap) {
            super("lightmap", () -> {
                if (lightmap) {
                    MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().enable();
                }
            }, () -> {
                if (lightmap) {
                    MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().disable();
                }
            }, lightmap);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Overlay
    extends Toggleable {
        public Overlay(boolean overlayColor) {
            super("overlay", () -> {
                if (overlayColor) {
                    MinecraftClient.getInstance().gameRenderer.getOverlayTexture().setupOverlayColor();
                }
            }, () -> {
                if (overlayColor) {
                    MinecraftClient.getInstance().gameRenderer.getOverlayTexture().teardownOverlayColor();
                }
            }, overlayColor);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Layering
    extends RenderPhase {
        public Layering(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Target
    extends RenderPhase {
        private final Supplier<Framebuffer> framebuffer;

        public Target(String name, Supplier<Framebuffer> framebuffer) {
            super(name, () -> {}, () -> {});
            this.framebuffer = framebuffer;
        }

        public Framebuffer get() {
            return this.framebuffer.get();
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class LineWidth
    extends RenderPhase {
        private final OptionalDouble width;

        public LineWidth(OptionalDouble width) {
            super("line_width", () -> {
                if (!Objects.equals(width, OptionalDouble.of(1.0))) {
                    if (width.isPresent()) {
                        RenderSystem.lineWidth((float)width.getAsDouble());
                    } else {
                        RenderSystem.lineWidth(Math.max(2.5f, (float)MinecraftClient.getInstance().getWindow().getFramebufferWidth() / 1920.0f * 2.5f));
                    }
                }
            }, () -> {
                if (!Objects.equals(width, OptionalDouble.of(1.0))) {
                    RenderSystem.lineWidth(1.0f);
                }
            });
            this.width = width;
        }

        @Override
        public String toString() {
            return this.name + "[" + String.valueOf(this.width.isPresent() ? Double.valueOf(this.width.getAsDouble()) : "window_scale") + "]";
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Toggleable
    extends RenderPhase {
        private final boolean enabled;

        public Toggleable(String name, Runnable apply, Runnable unapply, boolean enabled) {
            super(name, apply, unapply);
            this.enabled = enabled;
        }

        @Override
        public String toString() {
            return this.name + "[" + this.enabled + "]";
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static final class OffsetTexturing
    extends Texturing {
        public OffsetTexturing(float x, float y) {
            super("offset_texturing", () -> RenderSystem.setTextureMatrix(new Matrix4f().translation(x, y, 0.0f)), () -> RenderSystem.resetTextureMatrix());
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Textures
    extends TextureBase {
        private final Optional<Identifier> id;

        Textures(List<TextureEntry> textures) {
            super(() -> {
                for (int i = 0; i < textures.size(); ++i) {
                    TextureEntry lv = (TextureEntry)textures.get(i);
                    TextureManager lv2 = MinecraftClient.getInstance().getTextureManager();
                    AbstractTexture lv3 = lv2.getTexture(lv.id);
                    lv3.setUseMipmaps(lv.mipmap);
                    RenderSystem.setShaderTexture(i, lv3.getGlTextureView());
                }
            }, () -> {});
            this.id = textures.isEmpty() ? Optional.empty() : Optional.of(textures.getFirst().id);
        }

        @Override
        protected Optional<Identifier> getId() {
            return this.id;
        }

        public static Builder create() {
            return new Builder();
        }

        @Environment(value=EnvType.CLIENT)
        record TextureEntry(Identifier id, boolean mipmap) {
        }

        @Environment(value=EnvType.CLIENT)
        public static final class Builder {
            private final ImmutableList.Builder<TextureEntry> textures = new ImmutableList.Builder();

            public Builder add(Identifier id, boolean blur) {
                this.textures.add((Object)new TextureEntry(id, blur));
                return this;
            }

            public Textures build() {
                return new Textures((List<TextureEntry>)((Object)this.textures.build()));
            }
        }
    }
}

