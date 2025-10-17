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
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.model.SheepEntityModel;
import net.minecraft.client.render.entity.model.SheepWoolEntityModel;
import net.minecraft.client.render.entity.state.SheepEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class SheepWoolUndercoatFeatureRenderer
extends FeatureRenderer<SheepEntityRenderState, SheepEntityModel> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/sheep/sheep_wool_undercoat.png");
    private final EntityModel<SheepEntityRenderState> model;
    private final EntityModel<SheepEntityRenderState> babyModel;

    public SheepWoolUndercoatFeatureRenderer(FeatureRendererContext<SheepEntityRenderState, SheepEntityModel> context, LoadedEntityModels loader) {
        super(context);
        this.model = new SheepWoolEntityModel(loader.getModelPart(EntityModelLayers.SHEEP_WOOL_UNDERCOAT));
        this.babyModel = new SheepWoolEntityModel(loader.getModelPart(EntityModelLayers.SHEEP_BABY_WOOL_UNDERCOAT));
    }

    @Override
    public void render(MatrixStack arg, OrderedRenderCommandQueue arg2, int i, SheepEntityRenderState arg3, float f, float g) {
        if (arg3.invisible || !arg3.rainbow && arg3.color == DyeColor.WHITE) {
            return;
        }
        EntityModel<SheepEntityRenderState> lv = arg3.baby ? this.babyModel : this.model;
        SheepWoolUndercoatFeatureRenderer.render(lv, TEXTURE, arg, arg2, i, arg3, arg3.getRgbColor(), 1);
    }
}

