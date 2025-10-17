/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model;

import java.util.EnumMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.util.Util;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.AffineTransformations;
import net.minecraft.util.math.AxisRotation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.DirectionTransformation;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

@Environment(value=EnvType.CLIENT)
public enum ModelRotation implements ModelBakeSettings
{
    X0_Y0(AxisRotation.R0, AxisRotation.R0),
    X0_Y90(AxisRotation.R0, AxisRotation.R90),
    X0_Y180(AxisRotation.R0, AxisRotation.R180),
    X0_Y270(AxisRotation.R0, AxisRotation.R270),
    X90_Y0(AxisRotation.R90, AxisRotation.R0),
    X90_Y90(AxisRotation.R90, AxisRotation.R90),
    X90_Y180(AxisRotation.R90, AxisRotation.R180),
    X90_Y270(AxisRotation.R90, AxisRotation.R270),
    X180_Y0(AxisRotation.R180, AxisRotation.R0),
    X180_Y90(AxisRotation.R180, AxisRotation.R90),
    X180_Y180(AxisRotation.R180, AxisRotation.R180),
    X180_Y270(AxisRotation.R180, AxisRotation.R270),
    X270_Y0(AxisRotation.R270, AxisRotation.R0),
    X270_Y90(AxisRotation.R270, AxisRotation.R90),
    X270_Y180(AxisRotation.R270, AxisRotation.R180),
    X270_Y270(AxisRotation.R270, AxisRotation.R270);

    private static final ModelRotation[][] ROTATION_MAP;
    private final AxisRotation xRotation;
    private final AxisRotation yRotation;
    final AffineTransformation rotation;
    private final DirectionTransformation directionTransformation;
    final Map<Direction, Matrix4fc> faces = new EnumMap<Direction, Matrix4fc>(Direction.class);
    final Map<Direction, Matrix4fc> invertedFaces = new EnumMap<Direction, Matrix4fc>(Direction.class);
    private final UVModel uvModel = new UVModel(this);

    private ModelRotation(AxisRotation x, AxisRotation y) {
        this.xRotation = x;
        this.yRotation = y;
        this.directionTransformation = DirectionTransformation.fromRotations(x, y);
        this.rotation = this.directionTransformation != DirectionTransformation.IDENTITY ? new AffineTransformation(new Matrix4f(this.directionTransformation.getMatrix())) : AffineTransformation.identity();
        for (Direction lv : Direction.values()) {
            Matrix4fc matrix4fc = AffineTransformations.getTransformed(this.rotation, lv).getMatrix();
            this.faces.put(lv, matrix4fc);
            this.invertedFaces.put(lv, matrix4fc.invertAffine(new Matrix4f()));
        }
    }

    @Override
    public AffineTransformation getRotation() {
        return this.rotation;
    }

    public static ModelRotation rotate(AxisRotation xRotation, AxisRotation yRotation) {
        return ROTATION_MAP[xRotation.ordinal()][yRotation.ordinal()];
    }

    public DirectionTransformation getDirectionTransformation() {
        return this.directionTransformation;
    }

    public ModelBakeSettings getUVModel() {
        return this.uvModel;
    }

    static {
        ROTATION_MAP = Util.make(new ModelRotation[AxisRotation.values().length][AxisRotation.values().length], args -> {
            ModelRotation[] modelRotationArray = ModelRotation.values();
            int n = modelRotationArray.length;
            for (int i = 0; i < n; ++i) {
                ModelRotation lv;
                args[lv.xRotation.ordinal()][lv.yRotation.ordinal()] = lv = modelRotationArray[i];
            }
        });
    }

    @Environment(value=EnvType.CLIENT)
    record UVModel(ModelRotation parent) implements ModelBakeSettings
    {
        @Override
        public AffineTransformation getRotation() {
            return this.parent.rotation;
        }

        @Override
        public Matrix4fc forward(Direction facing) {
            return this.parent.faces.getOrDefault(facing, TRANSFORM_NONE);
        }

        @Override
        public Matrix4fc reverse(Direction facing) {
            return this.parent.invertedFaces.getOrDefault(facing, TRANSFORM_NONE);
        }
    }
}

