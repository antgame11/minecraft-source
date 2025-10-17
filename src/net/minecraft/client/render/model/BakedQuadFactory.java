/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.CubeFace;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.render.model.json.ModelRotation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.AxisRotation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MatrixUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public class BakedQuadFactory {
    public static final int field_32796 = 8;
    public static final int field_32797 = 4;
    private static final int field_32799 = 3;
    public static final int field_32798 = 4;
    private static final Vector3fc field_60149 = new Vector3f(1.0f, 1.0f, 1.0f);
    private static final Vector3fc field_60150 = new Vector3f(0.5f, 0.5f, 0.5f);

    @VisibleForTesting
    static ModelElementFace.UV setDefaultUV(Vector3fc from, Vector3fc to, Direction facing) {
        return switch (facing) {
            default -> throw new MatchException(null, null);
            case Direction.DOWN -> new ModelElementFace.UV(from.x(), 16.0f - to.z(), to.x(), 16.0f - from.z());
            case Direction.UP -> new ModelElementFace.UV(from.x(), from.z(), to.x(), to.z());
            case Direction.NORTH -> new ModelElementFace.UV(16.0f - to.x(), 16.0f - to.y(), 16.0f - from.x(), 16.0f - from.y());
            case Direction.SOUTH -> new ModelElementFace.UV(from.x(), 16.0f - to.y(), to.x(), 16.0f - from.y());
            case Direction.WEST -> new ModelElementFace.UV(from.z(), 16.0f - to.y(), to.z(), 16.0f - from.y());
            case Direction.EAST -> new ModelElementFace.UV(16.0f - to.z(), 16.0f - to.y(), 16.0f - from.z(), 16.0f - from.y());
        };
    }

    public static BakedQuad bake(Vector3fc from, Vector3fc to, ModelElementFace facing, Sprite sprite, Direction direction, ModelBakeSettings settings, @Nullable ModelRotation rotation, boolean shade, int lightEmission) {
        ModelElementFace.UV lv = facing.uvs();
        if (lv == null) {
            lv = BakedQuadFactory.setDefaultUV(from, to, direction);
        }
        lv = BakedQuadFactory.compactUV(sprite, lv);
        Matrix4fc matrix4fc = settings.reverse(direction);
        int[] is = BakedQuadFactory.packVertexData(lv, facing.rotation(), matrix4fc, sprite, direction, BakedQuadFactory.getPositionMatrix(from, to), settings.getRotation(), rotation);
        Direction lv2 = BakedQuadFactory.decodeDirection(is);
        if (rotation == null) {
            BakedQuadFactory.encodeDirection(is, lv2);
        }
        return new BakedQuad(is, facing.tintIndex(), lv2, sprite, shade, lightEmission);
    }

    private static ModelElementFace.UV compactUV(Sprite sprite, ModelElementFace.UV uv) {
        float f = uv.minU();
        float g = uv.minV();
        float h = uv.maxU();
        float i = uv.maxV();
        float j = sprite.getUvScaleDelta();
        float k = (f + f + h + h) / 4.0f;
        float l = (g + g + i + i) / 4.0f;
        return new ModelElementFace.UV(MathHelper.lerp(j, f, k), MathHelper.lerp(j, g, l), MathHelper.lerp(j, h, k), MathHelper.lerp(j, i, l));
    }

    private static int[] packVertexData(ModelElementFace.UV texture, AxisRotation rotation, Matrix4fc matrix4fc, Sprite sprite, Direction facing, float[] fs, AffineTransformation transform, @Nullable ModelRotation modelRotation) {
        CubeFace lv = CubeFace.getFace(facing);
        int[] is = new int[32];
        for (int i = 0; i < 4; ++i) {
            BakedQuadFactory.packVertexData(is, i, lv, texture, rotation, matrix4fc, fs, sprite, transform, modelRotation);
        }
        return is;
    }

    private static float[] getPositionMatrix(Vector3fc from, Vector3fc to) {
        float[] fs = new float[Direction.values().length];
        fs[CubeFace.DirectionIds.WEST] = from.x() / 16.0f;
        fs[CubeFace.DirectionIds.DOWN] = from.y() / 16.0f;
        fs[CubeFace.DirectionIds.NORTH] = from.z() / 16.0f;
        fs[CubeFace.DirectionIds.EAST] = to.x() / 16.0f;
        fs[CubeFace.DirectionIds.UP] = to.y() / 16.0f;
        fs[CubeFace.DirectionIds.SOUTH] = to.z() / 16.0f;
        return fs;
    }

    private static void packVertexData(int[] vertices, int cornerIndex, CubeFace arg, ModelElementFace.UV texture, AxisRotation arg3, Matrix4fc matrix4fc, float[] fs, Sprite arg4, AffineTransformation arg5, @Nullable ModelRotation arg6) {
        float j;
        float h;
        CubeFace.Corner lv = arg.getCorner(cornerIndex);
        Vector3f vector3f = new Vector3f(fs[lv.xSide], fs[lv.ySide], fs[lv.zSide]);
        BakedQuadFactory.rotateVertex(vector3f, arg6);
        BakedQuadFactory.transformVertex(vector3f, arg5);
        float f = ModelElementFace.getUValue(texture, arg3, cornerIndex);
        float g = ModelElementFace.getVValue(texture, arg3, cornerIndex);
        if (MatrixUtil.isIdentity(matrix4fc)) {
            h = f;
            j = g;
        } else {
            Vector3f vector3f2 = matrix4fc.transformPosition(new Vector3f(BakedQuadFactory.setCenterBack(f), BakedQuadFactory.setCenterBack(g), 0.0f));
            h = BakedQuadFactory.setCenterForward(vector3f2.x);
            j = BakedQuadFactory.setCenterForward(vector3f2.y);
        }
        BakedQuadFactory.packVertexData(vertices, cornerIndex, vector3f, arg4, h, j);
    }

    private static float setCenterBack(float f) {
        return f - 0.5f;
    }

    private static float setCenterForward(float f) {
        return f + 0.5f;
    }

    private static void packVertexData(int[] vertices, int cornerIndex, Vector3f pos, Sprite sprite, float f, float g) {
        int j = cornerIndex * 8;
        vertices[j] = Float.floatToRawIntBits(pos.x());
        vertices[j + 1] = Float.floatToRawIntBits(pos.y());
        vertices[j + 2] = Float.floatToRawIntBits(pos.z());
        vertices[j + 3] = -1;
        vertices[j + 4] = Float.floatToRawIntBits(sprite.getFrameU(f));
        vertices[j + 4 + 1] = Float.floatToRawIntBits(sprite.getFrameV(g));
    }

    private static void rotateVertex(Vector3f vertex, @Nullable ModelRotation rotation) {
        if (rotation == null) {
            return;
        }
        Vector3fc vector3fc = rotation.axis().getPositiveDirection().getFloatVector();
        Matrix4f matrix4fc = new Matrix4f().rotation(rotation.angle() * ((float)Math.PI / 180), vector3fc);
        Vector3fc vector3fc2 = rotation.rescale() ? BakedQuadFactory.method_71135(rotation) : field_60149;
        BakedQuadFactory.transformVertex(vertex, rotation.origin(), matrix4fc, vector3fc2);
    }

    private static Vector3fc method_71135(ModelRotation arg) {
        if (arg.angle() == 0.0f) {
            return field_60149;
        }
        float f = Math.abs(arg.angle());
        float g = 1.0f / MathHelper.cos(f * ((float)Math.PI / 180));
        return switch (arg.axis()) {
            default -> throw new MatchException(null, null);
            case Direction.Axis.X -> new Vector3f(1.0f, g, g);
            case Direction.Axis.Y -> new Vector3f(g, 1.0f, g);
            case Direction.Axis.Z -> new Vector3f(g, g, 1.0f);
        };
    }

    private static void transformVertex(Vector3f vertex, AffineTransformation transformation) {
        if (transformation == AffineTransformation.identity()) {
            return;
        }
        BakedQuadFactory.transformVertex(vertex, field_60150, transformation.getMatrix(), field_60149);
    }

    private static void transformVertex(Vector3f vertex, Vector3fc vector3fc, Matrix4fc matrix4fc, Vector3fc vector3fc2) {
        vertex.sub(vector3fc);
        matrix4fc.transformPosition(vertex);
        vertex.mul(vector3fc2);
        vertex.add(vector3fc);
    }

    private static Direction decodeDirection(int[] rotationMatrix) {
        Vector3f vector3f = BakedQuadFactory.bakeVectors(rotationMatrix, 0);
        Vector3f vector3f2 = BakedQuadFactory.bakeVectors(rotationMatrix, 8);
        Vector3f vector3f3 = BakedQuadFactory.bakeVectors(rotationMatrix, 16);
        Vector3f vector3f4 = new Vector3f(vector3f).sub(vector3f2);
        Vector3f vector3f5 = new Vector3f(vector3f3).sub(vector3f2);
        Vector3f vector3f6 = new Vector3f(vector3f5).cross(vector3f4).normalize();
        if (!vector3f6.isFinite()) {
            return Direction.UP;
        }
        Direction lv = null;
        float f = 0.0f;
        for (Direction lv2 : Direction.values()) {
            float g = vector3f6.dot(lv2.getFloatVector());
            if (!(g >= 0.0f) || !(g > f)) continue;
            f = g;
            lv = lv2;
        }
        if (lv == null) {
            return Direction.UP;
        }
        return lv;
    }

    private static float bakeVectorX(int[] is, int i) {
        return Float.intBitsToFloat(is[i]);
    }

    private static float bakeVectorY(int[] is, int i) {
        return Float.intBitsToFloat(is[i + 1]);
    }

    private static float bakeVectorZ(int[] is, int i) {
        return Float.intBitsToFloat(is[i + 2]);
    }

    private static Vector3f bakeVectors(int[] is, int i) {
        return new Vector3f(BakedQuadFactory.bakeVectorX(is, i), BakedQuadFactory.bakeVectorY(is, i), BakedQuadFactory.bakeVectorZ(is, i));
    }

    private static void encodeDirection(int[] rotationMatrix, Direction direction) {
        float h;
        int j;
        int[] js = new int[rotationMatrix.length];
        System.arraycopy(rotationMatrix, 0, js, 0, rotationMatrix.length);
        float[] fs = new float[Direction.values().length];
        fs[CubeFace.DirectionIds.WEST] = 999.0f;
        fs[CubeFace.DirectionIds.DOWN] = 999.0f;
        fs[CubeFace.DirectionIds.NORTH] = 999.0f;
        fs[CubeFace.DirectionIds.EAST] = -999.0f;
        fs[CubeFace.DirectionIds.UP] = -999.0f;
        fs[CubeFace.DirectionIds.SOUTH] = -999.0f;
        for (int i = 0; i < 4; ++i) {
            j = 8 * i;
            float f = BakedQuadFactory.bakeVectorX(js, j);
            float g = BakedQuadFactory.bakeVectorY(js, j);
            h = BakedQuadFactory.bakeVectorZ(js, j);
            if (f < fs[CubeFace.DirectionIds.WEST]) {
                fs[CubeFace.DirectionIds.WEST] = f;
            }
            if (g < fs[CubeFace.DirectionIds.DOWN]) {
                fs[CubeFace.DirectionIds.DOWN] = g;
            }
            if (h < fs[CubeFace.DirectionIds.NORTH]) {
                fs[CubeFace.DirectionIds.NORTH] = h;
            }
            if (f > fs[CubeFace.DirectionIds.EAST]) {
                fs[CubeFace.DirectionIds.EAST] = f;
            }
            if (g > fs[CubeFace.DirectionIds.UP]) {
                fs[CubeFace.DirectionIds.UP] = g;
            }
            if (!(h > fs[CubeFace.DirectionIds.SOUTH])) continue;
            fs[CubeFace.DirectionIds.SOUTH] = h;
        }
        CubeFace lv = CubeFace.getFace(direction);
        for (j = 0; j < 4; ++j) {
            int k = 8 * j;
            CubeFace.Corner lv2 = lv.getCorner(j);
            h = fs[lv2.xSide];
            float l = fs[lv2.ySide];
            float m = fs[lv2.zSide];
            rotationMatrix[k] = Float.floatToRawIntBits(h);
            rotationMatrix[k + 1] = Float.floatToRawIntBits(l);
            rotationMatrix[k + 2] = Float.floatToRawIntBits(m);
            for (int n = 0; n < 4; ++n) {
                int o = 8 * n;
                float p = BakedQuadFactory.bakeVectorX(js, o);
                float q = BakedQuadFactory.bakeVectorY(js, o);
                float r = BakedQuadFactory.bakeVectorZ(js, o);
                if (!MathHelper.approximatelyEquals(h, p) || !MathHelper.approximatelyEquals(l, q) || !MathHelper.approximatelyEquals(m, r)) continue;
                rotationMatrix[k + 4] = js[o + 4];
                rotationMatrix[k + 4 + 1] = js[o + 4 + 1];
            }
        }
    }

    public static void calculatePosition(int[] is, Consumer<Vector3f> consumer) {
        for (int i = 0; i < 4; ++i) {
            consumer.accept(BakedQuadFactory.bakeVectors(is, 8 * i));
        }
    }
}

