/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.animation.AnimationDefinition;
import net.minecraft.client.render.entity.animation.Keyframe;
import net.minecraft.client.render.entity.animation.Transformation;
import net.minecraft.entity.AnimationState;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class Animation {
    private final AnimationDefinition definition;
    private final List<TransformationEntry> entries;

    private Animation(AnimationDefinition definition, List<TransformationEntry> entries) {
        this.definition = definition;
        this.entries = entries;
    }

    static Animation of(ModelPart root, AnimationDefinition definition) {
        ArrayList<TransformationEntry> list = new ArrayList<TransformationEntry>();
        Function<String, ModelPart> function = root.createPartGetter();
        for (Map.Entry<String, List<Transformation>> entry : definition.boneAnimations().entrySet()) {
            String string = entry.getKey();
            List<Transformation> list2 = entry.getValue();
            ModelPart lv = function.apply(string);
            if (lv == null) {
                throw new IllegalArgumentException("Cannot animate " + string + ", which does not exist in model");
            }
            for (Transformation lv2 : list2) {
                list.add(new TransformationEntry(lv, lv2.target(), lv2.keyframes()));
            }
        }
        return new Animation(definition, List.copyOf(list));
    }

    public void applyStatic() {
        this.apply(0L, 1.0f);
    }

    public void applyWalking(float limbSwingAnimationProgress, float limbSwingAmplitude, float h, float i) {
        long l = (long)(limbSwingAnimationProgress * 50.0f * h);
        float j = Math.min(limbSwingAmplitude * i, 1.0f);
        this.apply(l, j);
    }

    public void apply(AnimationState animationState, float age) {
        this.apply(animationState, age, 1.0f);
    }

    public void apply(AnimationState animationState, float age, float speedMultiplier) {
        animationState.run(state -> this.apply((long)((float)state.getTimeInMilliseconds(age) * speedMultiplier), 1.0f));
    }

    public void apply(long timeInMilliseconds, float scale) {
        float g = this.getRunningSeconds(timeInMilliseconds);
        Vector3f vector3f = new Vector3f();
        for (TransformationEntry lv : this.entries) {
            lv.apply(g, scale, vector3f);
        }
    }

    private float getRunningSeconds(long timeInMilliseconds) {
        float f = (float)timeInMilliseconds / 1000.0f;
        return this.definition.looping() ? f % this.definition.lengthInSeconds() : f;
    }

    @Environment(value=EnvType.CLIENT)
    record TransformationEntry(ModelPart part, Transformation.Target target, Keyframe[] keyframes) {
        public void apply(float runningSeconds, float scale, Vector3f vec) {
            int i = Math.max(0, MathHelper.binarySearch(0, this.keyframes.length, index -> runningSeconds <= this.keyframes[index].timestamp()) - 1);
            int j = Math.min(this.keyframes.length - 1, i + 1);
            Keyframe lv = this.keyframes[i];
            Keyframe lv2 = this.keyframes[j];
            float h = runningSeconds - lv.timestamp();
            float k = j != i ? MathHelper.clamp(h / (lv2.timestamp() - lv.timestamp()), 0.0f, 1.0f) : 0.0f;
            lv2.interpolation().apply(vec, k, this.keyframes, i, j, scale);
            this.target.apply(this.part, vec);
        }
    }
}

