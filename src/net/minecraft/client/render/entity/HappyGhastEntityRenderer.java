/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.AgeableMobEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.feature.HappyGhastRopesFeatureRenderer;
import net.minecraft.client.render.entity.feature.SaddleFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.HappyGhastEntityModel;
import net.minecraft.client.render.entity.model.HappyGhastHarnessEntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.HappyGhastEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

@Environment(value=EnvType.CLIENT)
public class HappyGhastEntityRenderer
extends AgeableMobEntityRenderer<HappyGhastEntity, HappyGhastEntityRenderState, HappyGhastEntityModel> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/ghast/happy_ghast.png");
    private static final Identifier BABY_TEXTURE = Identifier.ofVanilla("textures/entity/ghast/happy_ghast_baby.png");
    private static final Identifier ROPES_TEXTURE = Identifier.ofVanilla("textures/entity/ghast/happy_ghast_ropes.png");

    public HappyGhastEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new HappyGhastEntityModel(arg.getPart(EntityModelLayers.HAPPY_GHAST)), new HappyGhastEntityModel(arg.getPart(EntityModelLayers.HAPPY_GHAST_BABY)), 2.0f);
        this.addFeature(new SaddleFeatureRenderer<HappyGhastEntityRenderState, HappyGhastEntityModel, HappyGhastHarnessEntityModel>(this, arg.getEquipmentRenderer(), EquipmentModel.LayerType.HAPPY_GHAST_BODY, state -> state.harnessStack, new HappyGhastHarnessEntityModel(arg.getPart(EntityModelLayers.HAPPY_GHAST_HARNESS)), new HappyGhastHarnessEntityModel(arg.getPart(EntityModelLayers.HAPPY_GHAST_BABY_HARNESS))));
        this.addFeature(new HappyGhastRopesFeatureRenderer<HappyGhastEntityModel>(this, arg.getEntityModels(), ROPES_TEXTURE));
    }

    @Override
    public Identifier getTexture(HappyGhastEntityRenderState arg) {
        if (arg.baby) {
            return BABY_TEXTURE;
        }
        return TEXTURE;
    }

    @Override
    public HappyGhastEntityRenderState createRenderState() {
        return new HappyGhastEntityRenderState();
    }

    @Override
    protected Box getBoundingBox(HappyGhastEntity arg) {
        Box lv = super.getBoundingBox(arg);
        float f = arg.getHeight();
        return lv.withMinY(lv.minY - (double)(f / 2.0f));
    }

    @Override
    public void updateRenderState(HappyGhastEntity arg, HappyGhastEntityRenderState arg2, float f) {
        super.updateRenderState(arg, arg2, f);
        arg2.harnessStack = arg.getEquippedStack(EquipmentSlot.BODY).copy();
        arg2.hasPassengers = arg.hasPassengers();
        arg2.hasRopes = arg.hasRopes();
    }

    @Override
    public /* synthetic */ Identifier getTexture(LivingEntityRenderState state) {
        return this.getTexture((HappyGhastEntityRenderState)state);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

