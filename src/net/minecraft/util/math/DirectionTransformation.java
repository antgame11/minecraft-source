/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.block.enums.Orientation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisRotation;
import net.minecraft.util.math.AxisTransformation;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;

public enum DirectionTransformation implements StringIdentifiable
{
    IDENTITY("identity", AxisTransformation.P123, false, false, false),
    ROT_180_FACE_XY("rot_180_face_xy", AxisTransformation.P123, true, true, false),
    ROT_180_FACE_XZ("rot_180_face_xz", AxisTransformation.P123, true, false, true),
    ROT_180_FACE_YZ("rot_180_face_yz", AxisTransformation.P123, false, true, true),
    ROT_120_NNN("rot_120_nnn", AxisTransformation.P231, false, false, false),
    ROT_120_NNP("rot_120_nnp", AxisTransformation.P312, true, false, true),
    ROT_120_NPN("rot_120_npn", AxisTransformation.P312, false, true, true),
    ROT_120_NPP("rot_120_npp", AxisTransformation.P231, true, false, true),
    ROT_120_PNN("rot_120_pnn", AxisTransformation.P312, true, true, false),
    ROT_120_PNP("rot_120_pnp", AxisTransformation.P231, true, true, false),
    ROT_120_PPN("rot_120_ppn", AxisTransformation.P231, false, true, true),
    ROT_120_PPP("rot_120_ppp", AxisTransformation.P312, false, false, false),
    ROT_180_EDGE_XY_NEG("rot_180_edge_xy_neg", AxisTransformation.P213, true, true, true),
    ROT_180_EDGE_XY_POS("rot_180_edge_xy_pos", AxisTransformation.P213, false, false, true),
    ROT_180_EDGE_XZ_NEG("rot_180_edge_xz_neg", AxisTransformation.P321, true, true, true),
    ROT_180_EDGE_XZ_POS("rot_180_edge_xz_pos", AxisTransformation.P321, false, true, false),
    ROT_180_EDGE_YZ_NEG("rot_180_edge_yz_neg", AxisTransformation.P132, true, true, true),
    ROT_180_EDGE_YZ_POS("rot_180_edge_yz_pos", AxisTransformation.P132, true, false, false),
    ROT_90_X_NEG("rot_90_x_neg", AxisTransformation.P132, false, false, true),
    ROT_90_X_POS("rot_90_x_pos", AxisTransformation.P132, false, true, false),
    ROT_90_Y_NEG("rot_90_y_neg", AxisTransformation.P321, true, false, false),
    ROT_90_Y_POS("rot_90_y_pos", AxisTransformation.P321, false, false, true),
    ROT_90_Z_NEG("rot_90_z_neg", AxisTransformation.P213, false, true, false),
    ROT_90_Z_POS("rot_90_z_pos", AxisTransformation.P213, true, false, false),
    INVERSION("inversion", AxisTransformation.P123, true, true, true),
    INVERT_X("invert_x", AxisTransformation.P123, true, false, false),
    INVERT_Y("invert_y", AxisTransformation.P123, false, true, false),
    INVERT_Z("invert_z", AxisTransformation.P123, false, false, true),
    ROT_60_REF_NNN("rot_60_ref_nnn", AxisTransformation.P312, true, true, true),
    ROT_60_REF_NNP("rot_60_ref_nnp", AxisTransformation.P231, true, false, false),
    ROT_60_REF_NPN("rot_60_ref_npn", AxisTransformation.P231, false, false, true),
    ROT_60_REF_NPP("rot_60_ref_npp", AxisTransformation.P312, false, false, true),
    ROT_60_REF_PNN("rot_60_ref_pnn", AxisTransformation.P231, false, true, false),
    ROT_60_REF_PNP("rot_60_ref_pnp", AxisTransformation.P312, true, false, false),
    ROT_60_REF_PPN("rot_60_ref_ppn", AxisTransformation.P312, false, true, false),
    ROT_60_REF_PPP("rot_60_ref_ppp", AxisTransformation.P231, true, true, true),
    SWAP_XY("swap_xy", AxisTransformation.P213, false, false, false),
    SWAP_YZ("swap_yz", AxisTransformation.P132, false, false, false),
    SWAP_XZ("swap_xz", AxisTransformation.P321, false, false, false),
    SWAP_NEG_XY("swap_neg_xy", AxisTransformation.P213, true, true, false),
    SWAP_NEG_YZ("swap_neg_yz", AxisTransformation.P132, false, true, true),
    SWAP_NEG_XZ("swap_neg_xz", AxisTransformation.P321, true, false, true),
    ROT_90_REF_X_NEG("rot_90_ref_x_neg", AxisTransformation.P132, true, false, true),
    ROT_90_REF_X_POS("rot_90_ref_x_pos", AxisTransformation.P132, true, true, false),
    ROT_90_REF_Y_NEG("rot_90_ref_y_neg", AxisTransformation.P321, true, true, false),
    ROT_90_REF_Y_POS("rot_90_ref_y_pos", AxisTransformation.P321, false, true, true),
    ROT_90_REF_Z_NEG("rot_90_ref_z_neg", AxisTransformation.P213, false, true, true),
    ROT_90_REF_Z_POS("rot_90_ref_z_pos", AxisTransformation.P213, true, false, true);

    private static final Direction.Axis[] AXES;
    private final Matrix3fc matrix;
    private final String name;
    @Nullable
    private Map<Direction, Direction> mappings;
    private final boolean flipX;
    private final boolean flipY;
    private final boolean flipZ;
    private final AxisTransformation axisTransformation;
    private static final DirectionTransformation[][] COMBINATIONS;
    private static final DirectionTransformation[] INVERSES;
    private static final DirectionTransformation[][] BY_ROTATIONS;

    private DirectionTransformation(String name, AxisTransformation axisTransformation, boolean flipX, boolean flipY, boolean flipZ) {
        this.name = name;
        this.flipX = flipX;
        this.flipY = flipY;
        this.flipZ = flipZ;
        this.axisTransformation = axisTransformation;
        Matrix3f matrix3f = new Matrix3f().scaling(flipX ? -1.0f : 1.0f, flipY ? -1.0f : 1.0f, flipZ ? -1.0f : 1.0f);
        matrix3f.mul(axisTransformation.getMatrix());
        this.matrix = matrix3f;
    }

    private BooleanList getAxisFlips() {
        return new BooleanArrayList(new boolean[]{this.flipX, this.flipY, this.flipZ});
    }

    public DirectionTransformation prepend(DirectionTransformation transformation) {
        return COMBINATIONS[this.ordinal()][transformation.ordinal()];
    }

    public DirectionTransformation inverse() {
        return INVERSES[this.ordinal()];
    }

    public Matrix3fc getMatrix() {
        return this.matrix;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    public Direction map(Direction direction2) {
        if (this.mappings == null) {
            this.mappings = Util.mapEnum(Direction.class, direction -> {
                Direction.Axis lv = direction.getAxis();
                Direction.AxisDirection lv2 = direction.getDirection();
                Direction.Axis lv3 = this.map(lv);
                Direction.AxisDirection lv4 = this.shouldFlipDirection(lv3) ? lv2.getOpposite() : lv2;
                return Direction.from(lv3, lv4);
            });
        }
        return this.mappings.get(direction2);
    }

    public boolean shouldFlipDirection(Direction.Axis axis) {
        return switch (axis) {
            default -> throw new MatchException(null, null);
            case Direction.Axis.X -> this.flipX;
            case Direction.Axis.Y -> this.flipY;
            case Direction.Axis.Z -> this.flipZ;
        };
    }

    public Direction.Axis map(Direction.Axis axis) {
        return AXES[this.axisTransformation.map(axis.ordinal())];
    }

    public Orientation mapJigsawOrientation(Orientation orientation) {
        return Orientation.byDirections(this.map(orientation.getFacing()), this.map(orientation.getRotation()));
    }

    public static DirectionTransformation fromRotations(AxisRotation x, AxisRotation y) {
        return BY_ROTATIONS[x.ordinal()][y.ordinal()];
    }

    static {
        AXES = Direction.Axis.values();
        COMBINATIONS = Util.make(new DirectionTransformation[DirectionTransformation.values().length][DirectionTransformation.values().length], combinations -> {
            Map<Pair, DirectionTransformation> map = Arrays.stream(DirectionTransformation.values()).collect(Collectors.toMap(transformation -> Pair.of(transformation.axisTransformation, transformation.getAxisFlips()), transformation -> transformation));
            for (DirectionTransformation lv : DirectionTransformation.values()) {
                for (DirectionTransformation lv2 : DirectionTransformation.values()) {
                    BooleanList booleanList = lv.getAxisFlips();
                    BooleanList booleanList2 = lv2.getAxisFlips();
                    AxisTransformation lv3 = lv2.axisTransformation.prepend(lv.axisTransformation);
                    BooleanArrayList booleanArrayList = new BooleanArrayList(3);
                    for (int i = 0; i < 3; ++i) {
                        booleanArrayList.add(booleanList.getBoolean(i) ^ booleanList2.getBoolean(lv.axisTransformation.map(i)));
                    }
                    combinations[lv.ordinal()][lv2.ordinal()] = map.get(Pair.of(lv3, booleanArrayList));
                }
            }
        });
        INVERSES = (DirectionTransformation[])Arrays.stream(DirectionTransformation.values()).map((? super T a) -> Arrays.stream(DirectionTransformation.values()).filter(b -> a.prepend((DirectionTransformation)b) == IDENTITY).findAny().get()).toArray(DirectionTransformation[]::new);
        BY_ROTATIONS = Util.make(new DirectionTransformation[AxisRotation.values().length][AxisRotation.values().length], byRotations -> {
            for (AxisRotation lv : AxisRotation.values()) {
                for (AxisRotation lv2 : AxisRotation.values()) {
                    int i;
                    DirectionTransformation lv3 = IDENTITY;
                    for (i = 0; i < lv2.index; ++i) {
                        lv3 = lv3.prepend(ROT_90_Y_NEG);
                    }
                    for (i = 0; i < lv.index; ++i) {
                        lv3 = lv3.prepend(ROT_90_X_NEG);
                    }
                    byRotations[lv.ordinal()][lv2.ordinal()] = lv3;
                }
            }
        });
    }
}

