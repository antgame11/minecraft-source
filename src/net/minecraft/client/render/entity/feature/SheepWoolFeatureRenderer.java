/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.model.SheepEntityModel;
import net.minecraft.client.render.entity.model.SheepWoolEntityModel;
import net.minecraft.client.render.entity.state.SheepEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class SheepWoolFeatureRenderer
extends FeatureRenderer<SheepEntityRenderState, SheepEntityModel> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/sheep/sheep_wool.png");
    private final EntityModel<SheepEntityRenderState> woolModel;
    private final EntityModel<SheepEntityRenderState> babyWoolModel;

    public SheepWoolFeatureRenderer(FeatureRendererContext<SheepEntityRenderState, SheepEntityModel> context, LoadedEntityModels loader) {
        super(context);
        this.woolModel = new SheepWoolEntityModel(loader.getModelPart(EntityModelLayers.SHEEP_WOOL));
        this.babyWoolModel = new SheepWoolEntityModel(loader.getModelPart(EntityModelLayers.SHEEP_BABY_WOOL));
    }

    @Override
    public void render(MatrixStack arg, OrderedRenderCommandQueue arg2, int i, SheepEntityRenderState arg3, float f, float g) {
        EntityModel<SheepEntityRenderState> lv;
        if (arg3.sheared) {
            return;
        }
        EntityModel<SheepEntityRenderState> entityModel = lv = arg3.baby ? this.babyWoolModel : this.woolModel;
        if (arg3.invisible) {
            if (arg3.hasOutline()) {
                arg2.submitModel(lv, arg3, arg, RenderLayer.getOutline(TEXTURE), i, LivingEntityRenderer.getOverlay(arg3, 0.0f), -16777216, null, arg3.outlineColor, null);
            }
            return;
        }
        SheepWoolFeatureRenderer.render(lv, TEXTURE, arg, arg2, i, arg3, arg3.getRgbColor(), 0);
    }
}

