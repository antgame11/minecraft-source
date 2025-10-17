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
import net.minecraft.client.render.entity.AbstractHorseEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.feature.SaddleFeatureRenderer;
import net.minecraft.client.render.entity.model.AbstractHorseEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.HorseEntityModel;
import net.minecraft.client.render.entity.model.HorseSaddleEntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.LivingHorseEntityRenderState;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class UndeadHorseEntityRenderer
extends AbstractHorseEntityRenderer<AbstractHorseEntity, LivingHorseEntityRenderState, AbstractHorseEntityModel<LivingHorseEntityRenderState>> {
    private final Identifier texture;

    public UndeadHorseEntityRenderer(EntityRendererFactory.Context ctx, Type type) {
        super(ctx, new HorseEntityModel(ctx.getPart(type.modelLayer)), new HorseEntityModel(ctx.getPart(type.babyModelLayer)));
        this.texture = type.texture;
        this.addFeature(new SaddleFeatureRenderer<LivingHorseEntityRenderState, AbstractHorseEntityModel<LivingHorseEntityRenderState>, HorseSaddleEntityModel>(this, ctx.getEquipmentRenderer(), type.saddleLayerType, arg -> arg.saddleStack, new HorseSaddleEntityModel(ctx.getPart(type.saddleModelLayer)), new HorseSaddleEntityModel(ctx.getPart(type.babySaddleModelLayer))));
    }

    @Override
    public Identifier getTexture(LivingHorseEntityRenderState arg) {
        return this.texture;
    }

    @Override
    public LivingHorseEntityRenderState createRenderState() {
        return new LivingHorseEntityRenderState();
    }

    @Override
    public /* synthetic */ Identifier getTexture(LivingEntityRenderState state) {
        return this.getTexture((LivingHorseEntityRenderState)state);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Type {
        SKELETON(Identifier.ofVanilla("textures/entity/horse/horse_skeleton.png"), EntityModelLayers.SKELETON_HORSE, EntityModelLayers.SKELETON_HORSE_BABY, EquipmentModel.LayerType.SKELETON_HORSE_SADDLE, EntityModelLayers.SKELETON_HORSE_SADDLE, EntityModelLayers.SKELETON_HORSE_BABY_SADDLE),
        ZOMBIE(Identifier.ofVanilla("textures/entity/horse/horse_zombie.png"), EntityModelLayers.ZOMBIE_HORSE, EntityModelLayers.ZOMBIE_HORSE_BABY, EquipmentModel.LayerType.ZOMBIE_HORSE_SADDLE, EntityModelLayers.ZOMBIE_HORSE_SADDLE, EntityModelLayers.ZOMBIE_HORSE_BABY_SADDLE);

        final Identifier texture;
        final EntityModelLayer modelLayer;
        final EntityModelLayer babyModelLayer;
        final EquipmentModel.LayerType saddleLayerType;
        final EntityModelLayer saddleModelLayer;
        final EntityModelLayer babySaddleModelLayer;

        private Type(Identifier texture, EntityModelLayer modelLayer, EntityModelLayer babyModelLayer, EquipmentModel.LayerType saddleLayerType, EntityModelLayer saddleModelLayer, EntityModelLayer babySaddleModelLayer) {
            this.texture = texture;
            this.modelLayer = modelLayer;
            this.babyModelLayer = babyModelLayer;
            this.saddleLayerType = saddleLayerType;
            this.saddleModelLayer = saddleModelLayer;
            this.babySaddleModelLayer = babySaddleModelLayer;
        }
    }
}

