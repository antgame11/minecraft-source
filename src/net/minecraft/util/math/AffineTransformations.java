/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.util.Util;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MatrixUtil;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class AffineTransformations {
    private static final Map<Direction, AffineTransformation> DIRECTION_ROTATIONS = Maps.newEnumMap(Map.of(Direction.SOUTH, AffineTransformation.identity(), Direction.EAST, new AffineTransformation(null, new Quaternionf().rotateY(1.5707964f), null, null), Direction.WEST, new AffineTransformation(null, new Quaternionf().rotateY(-1.5707964f), null, null), Direction.NORTH, new AffineTransformation(null, new Quaternionf().rotateY((float)Math.PI), null, null), Direction.UP, new AffineTransformation(null, new Quaternionf().rotateX(-1.5707964f), null, null), Direction.DOWN, new AffineTransformation(null, new Quaternionf().rotateX(1.5707964f), null, null)));
    private static final Map<Direction, AffineTransformation> INVERTED_DIRECTION_ROTATIONS = Maps.newEnumMap(Util.transformMapValues(DIRECTION_ROTATIONS, AffineTransformation::invert));

    public static AffineTransformation setupUvLock(AffineTransformation transformation) {
        Matrix4f matrix4f = new Matrix4f().translation(0.5f, 0.5f, 0.5f);
        matrix4f.mul(transformation.getMatrix());
        matrix4f.translate(-0.5f, -0.5f, -0.5f);
        return new AffineTransformation(matrix4f);
    }

    public static AffineTransformation method_35829(AffineTransformation transformation) {
        Matrix4f matrix4f = new Matrix4f().translation(-0.5f, -0.5f, -0.5f);
        matrix4f.mul(transformation.getMatrix());
        matrix4f.translate(0.5f, 0.5f, 0.5f);
        return new AffineTransformation(matrix4f);
    }

    public static AffineTransformation getTransformed(AffineTransformation arg, Direction arg2) {
        if (MatrixUtil.isIdentity(arg.getMatrix())) {
            return arg;
        }
        AffineTransformation lv = DIRECTION_ROTATIONS.get(arg2);
        lv = arg.multiply(lv);
        Vector3f vector3f = lv.getMatrix().transformDirection(new Vector3f(0.0f, 0.0f, 1.0f));
        Direction lv2 = Direction.getFacing(vector3f.x, vector3f.y, vector3f.z);
        return INVERTED_DIRECTION_ROTATIONS.get(lv2).multiply(lv);
    }
}

