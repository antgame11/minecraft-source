/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.particle;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.particle.ElderGuardianParticle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Submittable;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Unit;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class ElderGuardianParticleRenderer
extends ParticleRenderer<ElderGuardianParticle> {
    public ElderGuardianParticleRenderer(ParticleManager arg) {
        super(arg);
    }

    @Override
    public Submittable render(Frustum frustum, Camera camera, float tickProgress) {
        return new Result(this.particles.stream().map(arg2 -> State.create(arg2, camera, tickProgress)).toList());
    }

    @Environment(value=EnvType.CLIENT)
    record Result(List<State> states) implements Submittable
    {
        @Override
        public void submit(OrderedRenderCommandQueue arg, CameraRenderState arg2) {
            for (State lv : this.states) {
                arg.submitModel(lv.model, Unit.INSTANCE, lv.matrices, lv.renderLayer, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, lv.color, null, 0, null);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    record State(Model<Unit> model, MatrixStack matrices, RenderLayer renderLayer, int color) {
        public static State create(ElderGuardianParticle particle, Camera camera, float tickProgress) {
            float g = ((float)particle.age + tickProgress) / (float)particle.maxAge;
            float h = 0.05f + 0.5f * MathHelper.sin(g * (float)Math.PI);
            int i = ColorHelper.fromFloats(h, 1.0f, 1.0f, 1.0f);
            MatrixStack lv = new MatrixStack();
            lv.push();
            lv.multiply(camera.getRotation());
            lv.multiply(RotationAxis.POSITIVE_X.rotationDegrees(60.0f - 150.0f * g));
            float j = 0.42553192f;
            lv.scale(0.42553192f, -0.42553192f, -0.42553192f);
            lv.translate(0.0f, -0.56f, 3.5f);
            return new State(particle.model, lv, particle.renderLayer, i);
        }
    }
}

