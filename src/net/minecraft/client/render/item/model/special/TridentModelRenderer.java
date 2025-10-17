/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item.model.special;

import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.TridentEntityModel;
import net.minecraft.client.render.item.model.special.SimpleSpecialModelRenderer;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class TridentModelRenderer
implements SimpleSpecialModelRenderer {
    private final TridentEntityModel model;

    public TridentModelRenderer(TridentEntityModel model) {
        this.model = model;
    }

    @Override
    public void render(ItemDisplayContext displayContext, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay, boolean glint, int k) {
        matrices.push();
        matrices.scale(1.0f, -1.0f, -1.0f);
        queue.submitModelPart(this.model.getRootPart(), matrices, this.model.getLayer(TridentEntityModel.TEXTURE), light, overlay, null, false, glint, -1, null, k);
        matrices.pop();
    }

    @Override
    public void collectVertices(Set<Vector3f> vertices) {
        MatrixStack lv = new MatrixStack();
        lv.scale(1.0f, -1.0f, -1.0f);
        this.model.getRootPart().collectVertices(lv, vertices);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked() implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new Unbaked());

        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakeContext context) {
            return new TridentModelRenderer(new TridentEntityModel(context.entityModelSet().getModelPart(EntityModelLayers.TRIDENT)));
        }
    }
}

