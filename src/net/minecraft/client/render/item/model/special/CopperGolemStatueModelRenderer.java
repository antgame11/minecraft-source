/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item.model.special;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.CopperGolemStatueBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.model.CopperGolemStatueModel;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.item.model.special.SimpleSpecialModelRenderer;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.CopperGolemOxidationLevels;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class CopperGolemStatueModelRenderer
implements SimpleSpecialModelRenderer {
    private final CopperGolemStatueModel model;
    private final Identifier texture;
    static final Map<CopperGolemStatueBlock.Pose, EntityModelLayer> POSE_TO_LAYER = Map.of(CopperGolemStatueBlock.Pose.STANDING, EntityModelLayers.COPPER_GOLEM, CopperGolemStatueBlock.Pose.SITTING, EntityModelLayers.COPPER_GOLEM_SITTING, CopperGolemStatueBlock.Pose.STAR, EntityModelLayers.COPPER_GOLEM_STAR, CopperGolemStatueBlock.Pose.RUNNING, EntityModelLayers.COPPER_GOLEM_RUNNING);

    public CopperGolemStatueModelRenderer(CopperGolemStatueModel model, Identifier texture) {
        this.model = model;
        this.texture = texture;
    }

    @Override
    public void render(ItemDisplayContext displayContext, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay, boolean glint, int k) {
        this.setAngles(matrices);
        queue.submitModel(this.model, Direction.SOUTH, matrices, RenderLayer.getEntityCutoutNoCull(this.texture), light, overlay, -1, null, k, null);
    }

    @Override
    public void collectVertices(Set<Vector3f> vertices) {
        MatrixStack lv = new MatrixStack();
        this.setAngles(lv);
        this.model.getRootPart().collectVertices(lv, vertices);
    }

    private void setAngles(MatrixStack matrices) {
        matrices.translate(0.5f, 1.5f, 0.5f);
        matrices.scale(-1.0f, -1.0f, 1.0f);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked(Identifier texture, CopperGolemStatueBlock.Pose pose) implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("texture")).forGetter(Unbaked::texture), ((MapCodec)CopperGolemStatueBlock.Pose.CODEC.fieldOf("pose")).forGetter(Unbaked::pose)).apply((Applicative<Unbaked, ?>)instance, Unbaked::new));

        public Unbaked(Oxidizable.OxidationLevel oxidationLevel, CopperGolemStatueBlock.Pose pose) {
            this(CopperGolemOxidationLevels.get(oxidationLevel).texture(), pose);
        }

        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakeContext context) {
            CopperGolemStatueModel lv = new CopperGolemStatueModel(context.entityModelSet().getModelPart(POSE_TO_LAYER.get(this.pose)));
            return new CopperGolemStatueModelRenderer(lv, this.texture);
        }
    }
}

