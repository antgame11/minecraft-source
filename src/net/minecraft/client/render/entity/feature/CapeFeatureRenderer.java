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
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentModelLoader;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.model.PlayerCapeModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public class CapeFeatureRenderer
extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private final BipedEntityModel<PlayerEntityRenderState> model;
    private final EquipmentModelLoader equipmentModelLoader;

    public CapeFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context, LoadedEntityModels modelLoader, EquipmentModelLoader equipmentModelLoader) {
        super(context);
        this.model = new PlayerCapeModel(modelLoader.getModelPart(EntityModelLayers.PLAYER_CAPE));
        this.equipmentModelLoader = equipmentModelLoader;
    }

    private boolean hasCustomModelForLayer(ItemStack stack, EquipmentModel.LayerType layerType) {
        EquippableComponent lv = stack.get(DataComponentTypes.EQUIPPABLE);
        if (lv == null || lv.assetId().isEmpty()) {
            return false;
        }
        EquipmentModel lv2 = this.equipmentModelLoader.get(lv.assetId().get());
        return !lv2.getLayers(layerType).isEmpty();
    }

    @Override
    public void render(MatrixStack arg, OrderedRenderCommandQueue arg2, int i, PlayerEntityRenderState arg3, float f, float g) {
        if (arg3.invisible || !arg3.capeVisible) {
            return;
        }
        SkinTextures lv = arg3.skinTextures;
        if (lv.cape() == null) {
            return;
        }
        if (this.hasCustomModelForLayer(arg3.equippedChestStack, EquipmentModel.LayerType.WINGS)) {
            return;
        }
        arg.push();
        if (this.hasCustomModelForLayer(arg3.equippedChestStack, EquipmentModel.LayerType.HUMANOID)) {
            arg.translate(0.0f, -0.053125f, 0.06875f);
        }
        arg2.submitModel(this.model, arg3, arg, RenderLayer.getEntitySolid(lv.cape().texturePath()), i, OverlayTexture.DEFAULT_UV, arg3.outlineColor, null);
        arg.pop();
    }
}

