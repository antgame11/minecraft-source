/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.render.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.ColoredQuadGuiElementRenderState;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.gui.render.state.ItemGuiElementRenderState;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.gui.render.state.TextGuiElementRenderState;
import net.minecraft.client.gui.render.state.TexturedQuadGuiElementRenderState;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

@Environment(value=EnvType.CLIENT)
public class GuiRenderState {
    private static final int field_60454 = 0x774444FF;
    private final List<Layer> rootLayers = new ArrayList<Layer>();
    private int blurLayer = Integer.MAX_VALUE;
    private Layer currentLayer;
    private final Set<Object> itemModelKeys = new HashSet<Object>();
    @Nullable
    private ScreenRect currentLayerBounds;

    public GuiRenderState() {
        this.createNewRootLayer();
    }

    public void createNewRootLayer() {
        this.currentLayer = new Layer(null);
        this.rootLayers.add(this.currentLayer);
    }

    public void applyBlur() {
        if (this.blurLayer != Integer.MAX_VALUE) {
            throw new IllegalStateException("Can only blur once per frame");
        }
        this.blurLayer = this.rootLayers.size() - 1;
    }

    public void goUpLayer() {
        if (this.currentLayer.up == null) {
            this.currentLayer.up = new Layer(this.currentLayer);
        }
        this.currentLayer = this.currentLayer.up;
    }

    public void addItem(ItemGuiElementRenderState state) {
        if (!this.findAndGoToLayerToAdd(state)) {
            return;
        }
        this.itemModelKeys.add(state.state().getModelKey());
        this.currentLayer.addItem(state);
        this.onElementAdded(state.bounds());
    }

    public void addText(TextGuiElementRenderState state) {
        if (!this.findAndGoToLayerToAdd(state)) {
            return;
        }
        this.currentLayer.addText(state);
        this.onElementAdded(state.bounds());
    }

    public void addSpecialElement(SpecialGuiElementRenderState state) {
        if (!this.findAndGoToLayerToAdd(state)) {
            return;
        }
        this.currentLayer.addSpecialElement(state);
        this.onElementAdded(state.bounds());
    }

    public void addSimpleElement(SimpleGuiElementRenderState state) {
        if (!this.findAndGoToLayerToAdd(state)) {
            return;
        }
        this.currentLayer.addSimpleElement(state);
        this.onElementAdded(state.bounds());
    }

    private void onElementAdded(@Nullable ScreenRect bounds) {
        if (!SharedConstants.RENDER_UI_LAYERING_RECTANGLES || bounds == null) {
            return;
        }
        this.goUpLayer();
        this.currentLayer.addSimpleElement(new ColoredQuadGuiElementRenderState(RenderPipelines.GUI, TextureSetup.empty(), new Matrix3x2f(), 0, 0, 10000, 10000, 0x774444FF, 0x774444FF, bounds));
    }

    private boolean findAndGoToLayerToAdd(GuiElementRenderState state) {
        ScreenRect lv = state.bounds();
        if (lv == null) {
            return false;
        }
        if (this.currentLayerBounds != null && this.currentLayerBounds.contains(lv)) {
            this.goUpLayer();
        } else {
            this.findAndGoToLayerIntersecting(lv);
        }
        this.currentLayerBounds = lv;
        return true;
    }

    private void findAndGoToLayerIntersecting(ScreenRect bounds) {
        Layer lv = this.rootLayers.getLast();
        while (lv.up != null) {
            lv = lv.up;
        }
        boolean bl = false;
        while (!bl) {
            boolean bl2 = bl = this.anyIntersect(bounds, lv.simpleElementRenderStates) || this.anyIntersect(bounds, lv.itemElementRenderStates) || this.anyIntersect(bounds, lv.textElementRenderStates) || this.anyIntersect(bounds, lv.specialElementRenderStates);
            if (lv.parent == null) break;
            if (bl) continue;
            lv = lv.parent;
        }
        this.currentLayer = lv;
        if (bl) {
            this.goUpLayer();
        }
    }

    private boolean anyIntersect(ScreenRect bounds, @Nullable List<? extends GuiElementRenderState> elementRenderStates) {
        if (elementRenderStates != null) {
            for (GuiElementRenderState guiElementRenderState : elementRenderStates) {
                ScreenRect lv2 = guiElementRenderState.bounds();
                if (lv2 == null || !lv2.intersects(bounds)) continue;
                return true;
            }
        }
        return false;
    }

    public void addSimpleElementToCurrentLayer(TexturedQuadGuiElementRenderState state) {
        this.currentLayer.addSimpleElement(state);
    }

    public void addPreparedTextElement(SimpleGuiElementRenderState state) {
        this.currentLayer.addPreparedText(state);
    }

    public Set<Object> getItemModelKeys() {
        return this.itemModelKeys;
    }

    public void forEachSimpleElement(Consumer<SimpleGuiElementRenderState> consumer, LayerFilter filter) {
        this.forEachLayer(layer -> {
            if (layer.simpleElementRenderStates == null && layer.preparedTextElementRenderStates == null) {
                return;
            }
            if (layer.simpleElementRenderStates != null) {
                for (SimpleGuiElementRenderState lv : layer.simpleElementRenderStates) {
                    consumer.accept(lv);
                }
            }
            if (layer.preparedTextElementRenderStates != null) {
                for (SimpleGuiElementRenderState lv : layer.preparedTextElementRenderStates) {
                    consumer.accept(lv);
                }
            }
        }, filter);
    }

    public void forEachItemElement(Consumer<ItemGuiElementRenderState> itemElementStateConsumer) {
        Layer lv = this.currentLayer;
        this.forEachLayer(layer -> {
            if (layer.itemElementRenderStates != null) {
                this.currentLayer = layer;
                for (ItemGuiElementRenderState lv : layer.itemElementRenderStates) {
                    itemElementStateConsumer.accept(lv);
                }
            }
        }, LayerFilter.ALL);
        this.currentLayer = lv;
    }

    public void forEachTextElement(Consumer<TextGuiElementRenderState> textElementStateConsumer) {
        Layer lv = this.currentLayer;
        this.forEachLayer(layer -> {
            if (layer.textElementRenderStates != null) {
                for (TextGuiElementRenderState lv : layer.textElementRenderStates) {
                    this.currentLayer = layer;
                    textElementStateConsumer.accept(lv);
                }
            }
        }, LayerFilter.ALL);
        this.currentLayer = lv;
    }

    public void forEachSpecialElement(Consumer<SpecialGuiElementRenderState> specialElementStateConsumer) {
        Layer lv = this.currentLayer;
        this.forEachLayer(layer -> {
            if (layer.specialElementRenderStates != null) {
                this.currentLayer = layer;
                for (SpecialGuiElementRenderState lv : layer.specialElementRenderStates) {
                    specialElementStateConsumer.accept(lv);
                }
            }
        }, LayerFilter.ALL);
        this.currentLayer = lv;
    }

    public void sortSimpleElements(Comparator<SimpleGuiElementRenderState> simpleElementStateComparator) {
        this.forEachLayer(layer -> {
            if (layer.simpleElementRenderStates != null) {
                if (SharedConstants.SHUFFLE_UI_RENDERING_ORDER) {
                    Collections.shuffle(layer.simpleElementRenderStates);
                }
                layer.simpleElementRenderStates.sort(simpleElementStateComparator);
            }
        }, LayerFilter.ALL);
    }

    private void forEachLayer(Consumer<Layer> layerConsumer, LayerFilter filter) {
        int i = 0;
        int j = this.rootLayers.size();
        if (filter == LayerFilter.BEFORE_BLUR) {
            j = Math.min(this.blurLayer, this.rootLayers.size());
        } else if (filter == LayerFilter.AFTER_BLUR) {
            i = this.blurLayer;
        }
        for (int k = i; k < j; ++k) {
            Layer lv = this.rootLayers.get(k);
            this.traverseLayers(lv, layerConsumer);
        }
    }

    private void traverseLayers(Layer layer, Consumer<Layer> layerConsumer) {
        layerConsumer.accept(layer);
        if (layer.up != null) {
            this.traverseLayers(layer.up, layerConsumer);
        }
    }

    public void clear() {
        this.itemModelKeys.clear();
        this.rootLayers.clear();
        this.blurLayer = Integer.MAX_VALUE;
        this.createNewRootLayer();
    }

    @Environment(value=EnvType.CLIENT)
    static class Layer {
        @Nullable
        public final Layer parent;
        @Nullable
        public Layer up;
        @Nullable
        public List<SimpleGuiElementRenderState> simpleElementRenderStates;
        @Nullable
        public List<SimpleGuiElementRenderState> preparedTextElementRenderStates;
        @Nullable
        public List<ItemGuiElementRenderState> itemElementRenderStates;
        @Nullable
        public List<TextGuiElementRenderState> textElementRenderStates;
        @Nullable
        public List<SpecialGuiElementRenderState> specialElementRenderStates;

        Layer(@Nullable Layer parent) {
            this.parent = parent;
        }

        public void addItem(ItemGuiElementRenderState state) {
            if (this.itemElementRenderStates == null) {
                this.itemElementRenderStates = new ArrayList<ItemGuiElementRenderState>();
            }
            this.itemElementRenderStates.add(state);
        }

        public void addText(TextGuiElementRenderState state) {
            if (this.textElementRenderStates == null) {
                this.textElementRenderStates = new ArrayList<TextGuiElementRenderState>();
            }
            this.textElementRenderStates.add(state);
        }

        public void addSpecialElement(SpecialGuiElementRenderState state) {
            if (this.specialElementRenderStates == null) {
                this.specialElementRenderStates = new ArrayList<SpecialGuiElementRenderState>();
            }
            this.specialElementRenderStates.add(state);
        }

        public void addSimpleElement(SimpleGuiElementRenderState state) {
            if (this.simpleElementRenderStates == null) {
                this.simpleElementRenderStates = new ArrayList<SimpleGuiElementRenderState>();
            }
            this.simpleElementRenderStates.add(state);
        }

        public void addPreparedText(SimpleGuiElementRenderState state) {
            if (this.preparedTextElementRenderStates == null) {
                this.preparedTextElementRenderStates = new ArrayList<SimpleGuiElementRenderState>();
            }
            this.preparedTextElementRenderStates.add(state);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum LayerFilter {
        ALL,
        BEFORE_BLUR,
        AFTER_BLUR;

    }
}

