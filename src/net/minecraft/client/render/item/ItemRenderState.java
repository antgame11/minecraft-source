/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public class ItemRenderState {
    ItemDisplayContext displayContext = ItemDisplayContext.NONE;
    private int layerCount;
    private boolean animated;
    private boolean oversizedInGui;
    @Nullable
    private Box cachedModelBoundingBox;
    private LayerRenderState[] layers = new LayerRenderState[]{new LayerRenderState()};

    public void addLayers(int add) {
        int k = this.layerCount + add;
        int j = this.layers.length;
        if (k > j) {
            this.layers = Arrays.copyOf(this.layers, k);
            for (int l = j; l < k; ++l) {
                this.layers[l] = new LayerRenderState();
            }
        }
    }

    public LayerRenderState newLayer() {
        this.addLayers(1);
        return this.layers[this.layerCount++];
    }

    public void clear() {
        this.displayContext = ItemDisplayContext.NONE;
        for (int i = 0; i < this.layerCount; ++i) {
            this.layers[i].clear();
        }
        this.layerCount = 0;
        this.animated = false;
        this.oversizedInGui = false;
        this.cachedModelBoundingBox = null;
    }

    public void markAnimated() {
        this.animated = true;
    }

    public boolean isAnimated() {
        return this.animated;
    }

    public void addModelKey(Object modelKey) {
    }

    private LayerRenderState getFirstLayer() {
        return this.layers[0];
    }

    public boolean isEmpty() {
        return this.layerCount == 0;
    }

    public boolean isSideLit() {
        return this.getFirstLayer().useLight;
    }

    @Nullable
    public Sprite getParticleSprite(Random random) {
        if (this.layerCount == 0) {
            return null;
        }
        return this.layers[random.nextInt((int)this.layerCount)].particle;
    }

    public void load(Consumer<Vector3fc> posConsumer) {
        Vector3f vector3f = new Vector3f();
        MatrixStack.Entry lv = new MatrixStack.Entry();
        for (int i = 0; i < this.layerCount; ++i) {
            Vector3f[] vector3fs;
            LayerRenderState lv2 = this.layers[i];
            lv2.transform.apply(this.displayContext.isLeftHand(), lv);
            Matrix4f matrix4f = lv.getPositionMatrix();
            for (Vector3f vector3f2 : vector3fs = lv2.vertices.get()) {
                posConsumer.accept(vector3f.set(vector3f2).mulPosition(matrix4f));
            }
            lv.loadIdentity();
        }
    }

    public void render(MatrixStack matrices, OrderedRenderCommandQueue arg2, int light, int overlay, int k) {
        for (int l = 0; l < this.layerCount; ++l) {
            this.layers[l].render(matrices, arg2, light, overlay, k);
        }
    }

    public Box getModelBoundingBox() {
        Box lv2;
        if (this.cachedModelBoundingBox != null) {
            return this.cachedModelBoundingBox;
        }
        Box.Builder lv = new Box.Builder();
        this.load(lv::encompass);
        this.cachedModelBoundingBox = lv2 = lv.build();
        return lv2;
    }

    public void setOversizedInGui(boolean oversizedInGui) {
        this.oversizedInGui = oversizedInGui;
    }

    public boolean isOversizedInGui() {
        return this.oversizedInGui;
    }

    @Environment(value=EnvType.CLIENT)
    public class LayerRenderState {
        private static final Vector3f[] EMPTY = new Vector3f[0];
        public static final Supplier<Vector3f[]> DEFAULT = () -> EMPTY;
        private final List<BakedQuad> quads = new ArrayList<BakedQuad>();
        boolean useLight;
        @Nullable
        Sprite particle;
        Transformation transform = Transformation.IDENTITY;
        @Nullable
        private RenderLayer renderLayer;
        private Glint glint = Glint.NONE;
        private int[] tints = new int[0];
        @Nullable
        private SpecialModelRenderer<Object> specialModelType;
        @Nullable
        private Object data;
        Supplier<Vector3f[]> vertices = DEFAULT;

        public void clear() {
            this.quads.clear();
            this.renderLayer = null;
            this.glint = Glint.NONE;
            this.specialModelType = null;
            this.data = null;
            Arrays.fill(this.tints, -1);
            this.useLight = false;
            this.particle = null;
            this.transform = Transformation.IDENTITY;
            this.vertices = DEFAULT;
        }

        public List<BakedQuad> getQuads() {
            return this.quads;
        }

        public void setRenderLayer(RenderLayer layer) {
            this.renderLayer = layer;
        }

        public void setUseLight(boolean useLight) {
            this.useLight = useLight;
        }

        public void setVertices(Supplier<Vector3f[]> vertices) {
            this.vertices = vertices;
        }

        public void setParticle(Sprite particle) {
            this.particle = particle;
        }

        public void setTransform(Transformation transform) {
            this.transform = transform;
        }

        public <T> void setSpecialModel(SpecialModelRenderer<T> specialModelType, @Nullable T data) {
            this.specialModelType = LayerRenderState.eraseType(specialModelType);
            this.data = data;
        }

        private static SpecialModelRenderer<Object> eraseType(SpecialModelRenderer<?> specialModelType) {
            return specialModelType;
        }

        public void setGlint(Glint glint) {
            this.glint = glint;
        }

        public int[] initTints(int maxIndex) {
            if (maxIndex > this.tints.length) {
                this.tints = new int[maxIndex];
                Arrays.fill(this.tints, -1);
            }
            return this.tints;
        }

        void render(MatrixStack matrices, OrderedRenderCommandQueue arg2, int light, int overlay, int k) {
            matrices.push();
            this.transform.apply(ItemRenderState.this.displayContext.isLeftHand(), matrices.peek());
            if (this.specialModelType != null) {
                this.specialModelType.render(this.data, ItemRenderState.this.displayContext, matrices, arg2, light, overlay, this.glint != Glint.NONE, k);
            } else if (this.renderLayer != null) {
                arg2.submitItem(matrices, ItemRenderState.this.displayContext, light, overlay, k, this.tints, this.quads, this.renderLayer, this.glint);
            }
            matrices.pop();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Glint {
        NONE,
        STANDARD,
        SPECIAL;

    }
}

