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
import net.minecraft.client.render.entity.model.DonkeyEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.HorseSaddleEntityModel;
import net.minecraft.client.render.entity.state.DonkeyEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class AbstractDonkeyEntityRenderer<T extends AbstractDonkeyEntity>
extends AbstractHorseEntityRenderer<T, DonkeyEntityRenderState, DonkeyEntityModel> {
    private final Identifier texture;

    public AbstractDonkeyEntityRenderer(EntityRendererFactory.Context context, Type type) {
        super(context, new DonkeyEntityModel(context.getPart(type.adultModelLayer)), new DonkeyEntityModel(context.getPart(type.babyModelLayer)));
        this.texture = type.texture;
        this.addFeature(new SaddleFeatureRenderer<DonkeyEntityRenderState, DonkeyEntityModel, HorseSaddleEntityModel>(this, context.getEquipmentRenderer(), type.saddleLayerType, arg -> arg.saddleStack, new HorseSaddleEntityModel(context.getPart(type.adultSaddleModelLayer)), new HorseSaddleEntityModel(context.getPart(type.babySaddleModelLayer))));
    }

    @Override
    public Identifier getTexture(DonkeyEntityRenderState arg) {
        return this.texture;
    }

    @Override
    public DonkeyEntityRenderState createRenderState() {
        return new DonkeyEntityRenderState();
    }

    @Override
    public void updateRenderState(T arg, DonkeyEntityRenderState arg2, float f) {
        super.updateRenderState(arg, arg2, f);
        arg2.hasChest = ((AbstractDonkeyEntity)arg).hasChest();
    }

    @Override
    public /* synthetic */ Identifier getTexture(LivingEntityRenderState state) {
        return this.getTexture((DonkeyEntityRenderState)state);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Type {
        DONKEY(Identifier.ofVanilla("textures/entity/horse/donkey.png"), EntityModelLayers.DONKEY, EntityModelLayers.DONKEY_BABY, EquipmentModel.LayerType.DONKEY_SADDLE, EntityModelLayers.DONKEY_SADDLE, EntityModelLayers.DONKEY_BABY_SADDLE),
        MULE(Identifier.ofVanilla("textures/entity/horse/mule.png"), EntityModelLayers.MULE, EntityModelLayers.MULE_BABY, EquipmentModel.LayerType.MULE_SADDLE, EntityModelLayers.MULE_SADDLE, EntityModelLayers.MULE_BABY_SADDLE);

        final Identifier texture;
        final EntityModelLayer adultModelLayer;
        final EntityModelLayer babyModelLayer;
        final EquipmentModel.LayerType saddleLayerType;
        final EntityModelLayer adultSaddleModelLayer;
        final EntityModelLayer babySaddleModelLayer;

        private Type(Identifier texture, EntityModelLayer adultModelLayer, EntityModelLayer babyModelLayer, EquipmentModel.LayerType saddleLayerType, EntityModelLayer adultSaddleModelLayer, EntityModelLayer babySaddleModelLayer) {
            this.texture = texture;
            this.adultModelLayer = adultModelLayer;
            this.babyModelLayer = babyModelLayer;
            this.saddleLayerType = saddleLayerType;
            this.adultSaddleModelLayer = adultSaddleModelLayer;
            this.babySaddleModelLayer = babySaddleModelLayer;
        }
    }
}

