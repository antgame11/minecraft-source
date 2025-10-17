/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedGeometry;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ErrorCollectingSpriteGetter;
import net.minecraft.client.render.model.Geometry;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.SimpleModel;
import net.minecraft.client.render.model.UnbakedGeometry;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.AxisRotation;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class GeneratedItemModel
implements UnbakedModel {
    public static final Identifier GENERATED = Identifier.ofVanilla("builtin/generated");
    public static final List<String> LAYERS = List.of("layer0", "layer1", "layer2", "layer3", "layer4");
    private static final float field_32806 = 7.5f;
    private static final float field_32807 = 8.5f;
    private static final ModelTextures.Textures TEXTURES = new ModelTextures.Textures.Builder().addTextureReference("particle", "layer0").build();
    private static final ModelElementFace.UV FACING_SOUTH_UV = new ModelElementFace.UV(0.0f, 0.0f, 16.0f, 16.0f);
    private static final ModelElementFace.UV FACING_NORTH_UV = new ModelElementFace.UV(16.0f, 0.0f, 0.0f, 16.0f);

    @Override
    public ModelTextures.Textures textures() {
        return TEXTURES;
    }

    @Override
    public Geometry geometry() {
        return GeneratedItemModel::bakeGeometry;
    }

    @Override
    @Nullable
    public UnbakedModel.GuiLight guiLight() {
        return UnbakedModel.GuiLight.ITEM;
    }

    private static BakedGeometry bakeGeometry(ModelTextures textures, Baker baker, ModelBakeSettings settings, SimpleModel model) {
        return GeneratedItemModel.bakeGeometry(textures, baker.getSpriteGetter(), settings, model);
    }

    private static BakedGeometry bakeGeometry(ModelTextures textures, ErrorCollectingSpriteGetter arg2, ModelBakeSettings settings, SimpleModel model) {
        String string;
        SpriteIdentifier lv;
        ArrayList<ModelElement> list = new ArrayList<ModelElement>();
        for (int i = 0; i < LAYERS.size() && (lv = textures.get(string = LAYERS.get(i))) != null; ++i) {
            SpriteContents lv2 = arg2.get(lv, model).getContents();
            list.addAll(GeneratedItemModel.addLayerElements(i, string, lv2));
        }
        return UnbakedGeometry.bakeGeometry(list, textures, arg2, settings, model);
    }

    private static List<ModelElement> addLayerElements(int tintIndex, String name, SpriteContents arg) {
        Map<Direction, ModelElementFace> map = Map.of(Direction.SOUTH, new ModelElementFace(null, tintIndex, name, FACING_SOUTH_UV, AxisRotation.R0), Direction.NORTH, new ModelElementFace(null, tintIndex, name, FACING_NORTH_UV, AxisRotation.R0));
        ArrayList<ModelElement> list = new ArrayList<ModelElement>();
        list.add(new ModelElement(new Vector3f(0.0f, 0.0f, 7.5f), new Vector3f(16.0f, 16.0f, 8.5f), map));
        list.addAll(GeneratedItemModel.addSubComponents(arg, name, tintIndex));
        return list;
    }

    private static List<ModelElement> addSubComponents(SpriteContents arg, String string, int i) {
        float f = arg.getWidth();
        float g = arg.getHeight();
        ArrayList<ModelElement> list = new ArrayList<ModelElement>();
        for (Frame lv : GeneratedItemModel.getFrames(arg)) {
            float h = 0.0f;
            float j = 0.0f;
            float k = 0.0f;
            float l = 0.0f;
            float m = 0.0f;
            float n = 0.0f;
            float o = 0.0f;
            float p = 0.0f;
            float q = 16.0f / f;
            float r = 16.0f / g;
            float s = lv.getMin();
            float t = lv.getMax();
            float u = lv.getLevel();
            Side lv2 = lv.getSide();
            switch (lv2.ordinal()) {
                case 0: {
                    h = m = s;
                    k = n = t + 1.0f;
                    j = o = u;
                    l = u;
                    p = u + 1.0f;
                    break;
                }
                case 1: {
                    o = u;
                    p = u + 1.0f;
                    h = m = s;
                    k = n = t + 1.0f;
                    j = u + 1.0f;
                    l = u + 1.0f;
                    break;
                }
                case 2: {
                    h = m = u;
                    k = u;
                    n = u + 1.0f;
                    j = p = s;
                    l = o = t + 1.0f;
                    break;
                }
                case 3: {
                    m = u;
                    n = u + 1.0f;
                    h = u + 1.0f;
                    k = u + 1.0f;
                    j = p = s;
                    l = o = t + 1.0f;
                }
            }
            h *= q;
            k *= q;
            j *= r;
            l *= r;
            j = 16.0f - j;
            l = 16.0f - l;
            Map<Direction, ModelElementFace> map = Map.of(lv2.getDirection(), new ModelElementFace(null, i, string, new ModelElementFace.UV(m *= q, o *= r, n *= q, p *= r), AxisRotation.R0));
            switch (lv2.ordinal()) {
                case 0: {
                    list.add(new ModelElement(new Vector3f(h, j, 7.5f), new Vector3f(k, j, 8.5f), map));
                    break;
                }
                case 1: {
                    list.add(new ModelElement(new Vector3f(h, l, 7.5f), new Vector3f(k, l, 8.5f), map));
                    break;
                }
                case 2: {
                    list.add(new ModelElement(new Vector3f(h, j, 7.5f), new Vector3f(h, l, 8.5f), map));
                    break;
                }
                case 3: {
                    list.add(new ModelElement(new Vector3f(k, j, 7.5f), new Vector3f(k, l, 8.5f), map));
                }
            }
        }
        return list;
    }

    private static List<Frame> getFrames(SpriteContents arg) {
        int i = arg.getWidth();
        int j = arg.getHeight();
        ArrayList<Frame> list = new ArrayList<Frame>();
        arg.getDistinctFrameCount().forEach(k -> {
            for (int l = 0; l < j; ++l) {
                for (int m = 0; m < i; ++m) {
                    boolean bl = !GeneratedItemModel.isPixelTransparent(arg, k, m, l, i, j);
                    GeneratedItemModel.buildCube(Side.UP, list, arg, k, m, l, i, j, bl);
                    GeneratedItemModel.buildCube(Side.DOWN, list, arg, k, m, l, i, j, bl);
                    GeneratedItemModel.buildCube(Side.LEFT, list, arg, k, m, l, i, j, bl);
                    GeneratedItemModel.buildCube(Side.RIGHT, list, arg, k, m, l, i, j, bl);
                }
            }
        });
        return list;
    }

    private static void buildCube(Side arg, List<Frame> list, SpriteContents arg2, int i, int j, int k, int l, int m, boolean bl) {
        boolean bl2;
        boolean bl3 = bl2 = GeneratedItemModel.isPixelTransparent(arg2, i, j + arg.getOffsetX(), k + arg.getOffsetY(), l, m) && bl;
        if (bl2) {
            GeneratedItemModel.buildCube(list, arg, j, k);
        }
    }

    private static void buildCube(List<Frame> list, Side arg, int i, int j) {
        int m;
        Frame lv = null;
        for (Frame lv2 : list) {
            int k;
            if (lv2.getSide() != arg) continue;
            int n = k = arg.isVertical() ? j : i;
            if (lv2.getLevel() != k) continue;
            lv = lv2;
            break;
        }
        int l = arg.isVertical() ? j : i;
        int n = m = arg.isVertical() ? i : j;
        if (lv == null) {
            list.add(new Frame(arg, m, l));
        } else {
            lv.expand(m);
        }
    }

    private static boolean isPixelTransparent(SpriteContents arg, int i, int j, int k, int l, int m) {
        if (j < 0 || k < 0 || j >= l || k >= m) {
            return true;
        }
        return arg.isPixelTransparent(i, j, k);
    }

    @Environment(value=EnvType.CLIENT)
    static class Frame {
        private final Side side;
        private int min;
        private int max;
        private final int level;

        public Frame(Side side, int width, int depth) {
            this.side = side;
            this.min = width;
            this.max = width;
            this.level = depth;
        }

        public void expand(int newValue) {
            if (newValue < this.min) {
                this.min = newValue;
            } else if (newValue > this.max) {
                this.max = newValue;
            }
        }

        public Side getSide() {
            return this.side;
        }

        public int getMin() {
            return this.min;
        }

        public int getMax() {
            return this.max;
        }

        public int getLevel() {
            return this.level;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum Side {
        UP(Direction.UP, 0, -1),
        DOWN(Direction.DOWN, 0, 1),
        LEFT(Direction.EAST, -1, 0),
        RIGHT(Direction.WEST, 1, 0);

        private final Direction direction;
        private final int offsetX;
        private final int offsetY;

        private Side(Direction direction, int offsetX, int offsetY) {
            this.direction = direction;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        public Direction getDirection() {
            return this.direction;
        }

        public int getOffsetX() {
            return this.offsetX;
        }

        public int getOffsetY() {
            return this.offsetY;
        }

        boolean isVertical() {
            return this == DOWN || this == UP;
        }
    }
}

