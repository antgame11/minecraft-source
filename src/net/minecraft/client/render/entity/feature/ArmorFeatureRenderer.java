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
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EquipmentModelData;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public class ArmorFeatureRenderer<S extends BipedEntityRenderState, M extends BipedEntityModel<S>, A extends BipedEntityModel<S>>
extends FeatureRenderer<S, M> {
    private final EquipmentModelData<A> field_61804;
    private final EquipmentModelData<A> field_61805;
    private final EquipmentRenderer equipmentRenderer;

    public ArmorFeatureRenderer(FeatureRendererContext<S, M> arg, EquipmentModelData<A> arg2, EquipmentRenderer arg3) {
        this(arg, arg2, arg2, arg3);
    }

    public ArmorFeatureRenderer(FeatureRendererContext<S, M> arg, EquipmentModelData<A> arg2, EquipmentModelData<A> arg3, EquipmentRenderer arg4) {
        super(arg);
        this.field_61804 = arg2;
        this.field_61805 = arg3;
        this.equipmentRenderer = arg4;
    }

    public static boolean hasModel(ItemStack stack, EquipmentSlot slot) {
        EquippableComponent lv = stack.get(DataComponentTypes.EQUIPPABLE);
        return lv != null && ArmorFeatureRenderer.hasModel(lv, slot);
    }

    private static boolean hasModel(EquippableComponent component, EquipmentSlot slot) {
        return component.assetId().isPresent() && component.slot() == slot;
    }

    @Override
    public void render(MatrixStack arg, OrderedRenderCommandQueue arg2, int i, S arg3, float f, float g) {
        this.renderArmor(arg, arg2, ((BipedEntityRenderState)arg3).equippedChestStack, EquipmentSlot.CHEST, i, arg3);
        this.renderArmor(arg, arg2, ((BipedEntityRenderState)arg3).equippedLegsStack, EquipmentSlot.LEGS, i, arg3);
        this.renderArmor(arg, arg2, ((BipedEntityRenderState)arg3).equippedFeetStack, EquipmentSlot.FEET, i, arg3);
        this.renderArmor(arg, arg2, ((BipedEntityRenderState)arg3).equippedHeadStack, EquipmentSlot.HEAD, i, arg3);
    }

    private void renderArmor(MatrixStack matrices, OrderedRenderCommandQueue arg2, ItemStack stack, EquipmentSlot slot, int light, S arg5) {
        EquippableComponent lv = stack.get(DataComponentTypes.EQUIPPABLE);
        if (lv == null || !ArmorFeatureRenderer.hasModel(lv, slot)) {
            return;
        }
        A lv2 = this.getModel(arg5, slot);
        EquipmentModel.LayerType lv3 = this.usesInnerModel(slot) ? EquipmentModel.LayerType.HUMANOID_LEGGINGS : EquipmentModel.LayerType.HUMANOID;
        this.equipmentRenderer.render(lv3, lv.assetId().orElseThrow(), lv2, arg5, stack, matrices, arg2, light, ((BipedEntityRenderState)arg5).outlineColor);
    }

    private A getModel(S state, EquipmentSlot slot) {
        return (A)((BipedEntityModel)(((BipedEntityRenderState)state).baby ? this.field_61805 : this.field_61804).getModelData(slot));
    }

    private boolean usesInnerModel(EquipmentSlot slot) {
        return slot == EquipmentSlot.LEGS;
    }
}

