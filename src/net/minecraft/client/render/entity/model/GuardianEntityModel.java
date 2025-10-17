/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.ModelTransformer;
import net.minecraft.client.render.entity.state.GuardianEntityRenderState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public class GuardianEntityModel
extends EntityModel<GuardianEntityRenderState> {
    public static final ModelTransformer ELDER_TRANSFORMER = ModelTransformer.scaling(2.35f);
    private static final float[] SPIKE_PITCHES = new float[]{1.75f, 0.25f, 0.0f, 0.0f, 0.5f, 0.5f, 0.5f, 0.5f, 1.25f, 0.75f, 0.0f, 0.0f};
    private static final float[] SPIKE_YAWS = new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.25f, 1.75f, 1.25f, 0.75f, 0.0f, 0.0f, 0.0f, 0.0f};
    private static final float[] SPIKE_ROLLS = new float[]{0.0f, 0.0f, 0.25f, 1.75f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.75f, 1.25f};
    private static final float[] SPIKE_PIVOTS_X = new float[]{0.0f, 0.0f, 8.0f, -8.0f, -8.0f, 8.0f, 8.0f, -8.0f, 0.0f, 0.0f, 8.0f, -8.0f};
    private static final float[] SPIKE_PIVOTS_Y = new float[]{-8.0f, -8.0f, -8.0f, -8.0f, 0.0f, 0.0f, 0.0f, 0.0f, 8.0f, 8.0f, 8.0f, 8.0f};
    private static final float[] SPIKE_PIVOTS_Z = new float[]{8.0f, -8.0f, 0.0f, 0.0f, -8.0f, -8.0f, 8.0f, 8.0f, 8.0f, -8.0f, 0.0f, 0.0f};
    private static final String EYE = "eye";
    private static final String TAIL0 = "tail0";
    private static final String TAIL1 = "tail1";
    private static final String TAIL2 = "tail2";
    private final ModelPart head;
    private final ModelPart eye;
    private final ModelPart[] spikes = new ModelPart[12];
    private final ModelPart[] tail;

    public GuardianEntityModel(ModelPart arg) {
        super(arg);
        this.head = arg.getChild(EntityModelPartNames.HEAD);
        for (int i = 0; i < this.spikes.length; ++i) {
            this.spikes[i] = this.head.getChild(GuardianEntityModel.getSpikeName(i));
        }
        this.eye = this.head.getChild(EYE);
        this.tail = new ModelPart[3];
        this.tail[0] = this.head.getChild(TAIL0);
        this.tail[1] = this.tail[0].getChild(TAIL1);
        this.tail[2] = this.tail[1].getChild(TAIL2);
    }

    private static String getSpikeName(int index) {
        return "spike" + index;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        ModelPartData lv3 = lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-6.0f, 10.0f, -8.0f, 12.0f, 12.0f, 16.0f).uv(0, 28).cuboid(-8.0f, 10.0f, -6.0f, 2.0f, 12.0f, 12.0f).uv(0, 28).cuboid(6.0f, 10.0f, -6.0f, 2.0f, 12.0f, 12.0f, true).uv(16, 40).cuboid(-6.0f, 8.0f, -6.0f, 12.0f, 2.0f, 12.0f).uv(16, 40).cuboid(-6.0f, 22.0f, -6.0f, 12.0f, 2.0f, 12.0f), ModelTransform.NONE);
        ModelPartBuilder lv4 = ModelPartBuilder.create().uv(0, 0).cuboid(-1.0f, -4.5f, -1.0f, 2.0f, 9.0f, 2.0f);
        for (int i = 0; i < 12; ++i) {
            float f = GuardianEntityModel.getSpikePivotX(i, 0.0f, 0.0f);
            float g = GuardianEntityModel.getSpikePivotY(i, 0.0f, 0.0f);
            float h = GuardianEntityModel.getSpikePivotZ(i, 0.0f, 0.0f);
            float j = (float)Math.PI * SPIKE_PITCHES[i];
            float k = (float)Math.PI * SPIKE_YAWS[i];
            float l = (float)Math.PI * SPIKE_ROLLS[i];
            lv3.addChild(GuardianEntityModel.getSpikeName(i), lv4, ModelTransform.of(f, g, h, j, k, l));
        }
        lv3.addChild(EYE, ModelPartBuilder.create().uv(8, 0).cuboid(-1.0f, 15.0f, 0.0f, 2.0f, 2.0f, 1.0f), ModelTransform.origin(0.0f, 0.0f, -8.25f));
        ModelPartData lv5 = lv3.addChild(TAIL0, ModelPartBuilder.create().uv(40, 0).cuboid(-2.0f, 14.0f, 7.0f, 4.0f, 4.0f, 8.0f), ModelTransform.NONE);
        ModelPartData lv6 = lv5.addChild(TAIL1, ModelPartBuilder.create().uv(0, 54).cuboid(0.0f, 14.0f, 0.0f, 3.0f, 3.0f, 7.0f), ModelTransform.origin(-1.5f, 0.5f, 14.0f));
        lv6.addChild(TAIL2, ModelPartBuilder.create().uv(41, 32).cuboid(0.0f, 14.0f, 0.0f, 2.0f, 2.0f, 6.0f).uv(25, 19).cuboid(1.0f, 10.5f, 3.0f, 1.0f, 9.0f, 9.0f), ModelTransform.origin(0.5f, 0.5f, 6.0f));
        return TexturedModelData.of(lv, 64, 64);
    }

    public static TexturedModelData getElderTexturedModelData() {
        return GuardianEntityModel.getTexturedModelData().transform(ELDER_TRANSFORMER);
    }

    @Override
    public void setAngles(GuardianEntityRenderState arg) {
        super.setAngles(arg);
        this.head.yaw = arg.relativeHeadYaw * ((float)Math.PI / 180);
        this.head.pitch = arg.pitch * ((float)Math.PI / 180);
        float f = (1.0f - arg.spikesExtension) * 0.55f;
        this.updateSpikeExtensions(arg.age, f);
        if (arg.lookAtPos != null && arg.rotationVec != null) {
            double d = arg.lookAtPos.y - arg.cameraPosVec.y;
            this.eye.originY = d > 0.0 ? 0.0f : 1.0f;
            Vec3d lv = arg.rotationVec;
            lv = new Vec3d(lv.x, 0.0, lv.z);
            Vec3d lv2 = new Vec3d(arg.cameraPosVec.x - arg.lookAtPos.x, 0.0, arg.cameraPosVec.z - arg.lookAtPos.z).normalize().rotateY(1.5707964f);
            double e = lv.dotProduct(lv2);
            this.eye.originX = MathHelper.sqrt((float)Math.abs(e)) * 2.0f * (float)Math.signum(e);
        }
        this.eye.visible = true;
        float g = arg.tailAngle;
        this.tail[0].yaw = MathHelper.sin(g) * (float)Math.PI * 0.05f;
        this.tail[1].yaw = MathHelper.sin(g) * (float)Math.PI * 0.1f;
        this.tail[2].yaw = MathHelper.sin(g) * (float)Math.PI * 0.15f;
    }

    private void updateSpikeExtensions(float animationProgress, float extension) {
        for (int i = 0; i < 12; ++i) {
            this.spikes[i].originX = GuardianEntityModel.getSpikePivotX(i, animationProgress, extension);
            this.spikes[i].originY = GuardianEntityModel.getSpikePivotY(i, animationProgress, extension);
            this.spikes[i].originZ = GuardianEntityModel.getSpikePivotZ(i, animationProgress, extension);
        }
    }

    private static float getAngle(int index, float animationProgress, float magnitude) {
        return 1.0f + MathHelper.cos(animationProgress * 1.5f + (float)index) * 0.01f - magnitude;
    }

    private static float getSpikePivotX(int index, float animationProgress, float extension) {
        return SPIKE_PIVOTS_X[index] * GuardianEntityModel.getAngle(index, animationProgress, extension);
    }

    private static float getSpikePivotY(int index, float animationProgress, float extension) {
        return 16.0f + SPIKE_PIVOTS_Y[index] * GuardianEntityModel.getAngle(index, animationProgress, extension);
    }

    private static float getSpikePivotZ(int index, float animationProgress, float extension) {
        return SPIKE_PIVOTS_Z[index] * GuardianEntityModel.getAngle(index, animationProgress, extension);
    }
}

