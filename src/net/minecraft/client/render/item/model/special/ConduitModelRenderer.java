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
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.ConduitBlockEntityRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.item.model.special.SimpleSpecialModelRenderer;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class ConduitModelRenderer
implements SimpleSpecialModelRenderer {
    private final SpriteHolder spriteHolder;
    private final ModelPart shell;

    public ConduitModelRenderer(SpriteHolder spriteHolder, ModelPart shell) {
        this.spriteHolder = spriteHolder;
        this.shell = shell;
    }

    @Override
    public void render(ItemDisplayContext displayContext, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay, boolean glint, int k) {
        matrices.push();
        matrices.translate(0.5f, 0.5f, 0.5f);
        queue.submitModelPart(this.shell, matrices, ConduitBlockEntityRenderer.BASE_TEXTURE.getRenderLayer(RenderLayer::getEntitySolid), light, overlay, this.spriteHolder.getSprite(ConduitBlockEntityRenderer.BASE_TEXTURE), false, false, -1, null, k);
        matrices.pop();
    }

    @Override
    public void collectVertices(Set<Vector3f> vertices) {
        MatrixStack lv = new MatrixStack();
        lv.translate(0.5f, 0.5f, 0.5f);
        this.shell.collectVertices(lv, vertices);
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
            return new ConduitModelRenderer(context.spriteHolder(), context.entityModelSet().getModelPart(EntityModelLayers.CONDUIT_SHELL));
        }
    }
}

