/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BabyModelPair;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.CowEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.CowVariant;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class CowEntityRenderer
extends MobEntityRenderer<CowEntity, CowEntityRenderState, CowEntityModel> {
    private final Map<CowVariant.Model, BabyModelPair<CowEntityModel>> babyModelPairMap;

    public CowEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new CowEntityModel(arg.getPart(EntityModelLayers.COW)), 0.7f);
        this.babyModelPairMap = CowEntityRenderer.createBabyModelPairMap(arg);
    }

    private static Map<CowVariant.Model, BabyModelPair<CowEntityModel>> createBabyModelPairMap(EntityRendererFactory.Context context) {
        return Maps.newEnumMap(Map.of(CowVariant.Model.NORMAL, new BabyModelPair<CowEntityModel>(new CowEntityModel(context.getPart(EntityModelLayers.COW)), new CowEntityModel(context.getPart(EntityModelLayers.COW_BABY))), CowVariant.Model.WARM, new BabyModelPair<CowEntityModel>(new CowEntityModel(context.getPart(EntityModelLayers.WARM_COW)), new CowEntityModel(context.getPart(EntityModelLayers.WARM_COW_BABY))), CowVariant.Model.COLD, new BabyModelPair<CowEntityModel>(new CowEntityModel(context.getPart(EntityModelLayers.COLD_COW)), new CowEntityModel(context.getPart(EntityModelLayers.COLD_COW_BABY)))));
    }

    @Override
    public Identifier getTexture(CowEntityRenderState arg) {
        return arg.variant == null ? MissingSprite.getMissingSpriteId() : arg.variant.modelAndTexture().asset().texturePath();
    }

    @Override
    public CowEntityRenderState createRenderState() {
        return new CowEntityRenderState();
    }

    @Override
    public void updateRenderState(CowEntity arg, CowEntityRenderState arg2, float f) {
        super.updateRenderState(arg, arg2, f);
        arg2.variant = arg.getVariant().value();
    }

    @Override
    public void render(CowEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        if (arg.variant == null) {
            return;
        }
        this.model = this.babyModelPairMap.get(arg.variant.modelAndTexture().model()).get(arg.baby);
        super.render(arg, arg2, arg3, arg4);
    }

    @Override
    public /* synthetic */ Identifier getTexture(LivingEntityRenderState state) {
        return this.getTexture((CowEntityRenderState)state);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

