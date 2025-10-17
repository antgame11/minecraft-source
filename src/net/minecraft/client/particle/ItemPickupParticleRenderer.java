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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.Submittable;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public class ItemPickupParticleRenderer
extends ParticleRenderer<ItemPickupParticle> {
    public ItemPickupParticleRenderer(ParticleManager arg) {
        super(arg);
    }

    @Override
    public Submittable render(Frustum frustum, Camera camera, float tickProgress) {
        return new Result(this.particles.stream().map(particle -> Instance.create(particle, camera, tickProgress)).toList());
    }

    @Environment(value=EnvType.CLIENT)
    record Result(List<Instance> instances) implements Submittable
    {
        @Override
        public void submit(OrderedRenderCommandQueue arg, CameraRenderState arg2) {
            MatrixStack lv = new MatrixStack();
            EntityRenderManager lv2 = MinecraftClient.getInstance().getEntityRenderDispatcher();
            for (Instance lv3 : this.instances) {
                lv2.render(lv3.itemRenderState, arg2, lv3.xOffset, lv3.yOffset, lv3.zOffset, lv, arg);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    record Instance(EntityRenderState itemRenderState, double xOffset, double yOffset, double zOffset) {
        public static Instance create(ItemPickupParticle particle, Camera camera, float tickProgress) {
            float g = ((float)particle.ticksExisted + tickProgress) / 3.0f;
            g *= g;
            double d = MathHelper.lerp((double)tickProgress, particle.lastTargetX, particle.targetX);
            double e = MathHelper.lerp((double)tickProgress, particle.lastTargetY, particle.targetY);
            double h = MathHelper.lerp((double)tickProgress, particle.lastTargetZ, particle.targetZ);
            double i = MathHelper.lerp((double)g, particle.renderState.x, d);
            double j = MathHelper.lerp((double)g, particle.renderState.y, e);
            double k = MathHelper.lerp((double)g, particle.renderState.z, h);
            Vec3d lv = camera.getPos();
            return new Instance(particle.renderState, i - lv.getX(), j - lv.getY(), k - lv.getZ());
        }
    }
}

