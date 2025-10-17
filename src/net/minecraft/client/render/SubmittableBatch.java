/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Submittable;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.state.CameraRenderState;

@Environment(value=EnvType.CLIENT)
public class SubmittableBatch {
    public final List<Submittable> batch = new ArrayList<Submittable>();

    public void onFrameEnd() {
        this.batch.forEach(Submittable::onFrameEnd);
        this.batch.clear();
    }

    public void add(Submittable submittable) {
        this.batch.add(submittable);
    }

    public void submit(OrderedRenderCommandQueueImpl queue, CameraRenderState arg2) {
        for (Submittable lv : this.batch) {
            lv.submit(queue, arg2);
        }
    }
}

