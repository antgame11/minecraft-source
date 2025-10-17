/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.feature;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CopperGolemStatueBlock;
import net.minecraft.block.FlowerbedBlock;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class CopperGolemHeadBlockFeatureRenderer<S extends EntityRenderState, M extends EntityModel<S>>
extends FeatureRenderer<S, M> {
    private final Function<S, Optional<BlockState>> state;
    private final Consumer<MatrixStack> matrixTransformer;

    public CopperGolemHeadBlockFeatureRenderer(FeatureRendererContext<S, M> context, Function<S, Optional<BlockState>> state, Consumer<MatrixStack> matrixTransformer) {
        super(context);
        this.state = state;
        this.matrixTransformer = matrixTransformer;
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, S state, float limbAngle, float limbDistance) {
        Optional<BlockState> optional = this.state.apply(state);
        if (optional.isEmpty()) {
            return;
        }
        BlockState lv = optional.get();
        Block lv2 = lv.getBlock();
        boolean bl = lv2 instanceof CopperGolemStatueBlock;
        matrices.push();
        this.matrixTransformer.accept(matrices);
        if (!bl) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
        }
        if (bl || lv2 instanceof AbstractSkullBlock || lv2 instanceof AbstractBannerBlock || lv2 instanceof AbstractChestBlock) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
        }
        if (lv2 instanceof FlowerbedBlock) {
            matrices.translate(-0.25, -1.5, -0.25);
        } else if (!bl) {
            matrices.translate(-0.5, -1.5, -0.5);
        } else {
            matrices.translate(-0.5, 0.0, -0.5);
        }
        queue.submitBlock(matrices, lv, light, OverlayTexture.DEFAULT_UV, ((EntityRenderState)state).outlineColor);
        matrices.pop();
    }
}

