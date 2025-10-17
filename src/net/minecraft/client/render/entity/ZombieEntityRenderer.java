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
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ZombieBaseEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EquipmentModelData;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.ZombieEntityRenderState;
import net.minecraft.entity.mob.ZombieEntity;

@Environment(value=EnvType.CLIENT)
public class ZombieEntityRenderer
extends ZombieBaseEntityRenderer<ZombieEntity, ZombieEntityRenderState, ZombieEntityModel<ZombieEntityRenderState>> {
    public ZombieEntityRenderer(EntityRendererFactory.Context arg) {
        this(arg, EntityModelLayers.ZOMBIE, EntityModelLayers.ZOMBIE_BABY, EntityModelLayers.ZOMBIE_EQUIPMENT, EntityModelLayers.ZOMBIE_BABY_EQUIPMENT);
    }

    @Override
    public ZombieEntityRenderState createRenderState() {
        return new ZombieEntityRenderState();
    }

    public ZombieEntityRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer, EntityModelLayer legsArmorLayer, EquipmentModelData<EntityModelLayer> arg4, EquipmentModelData<EntityModelLayer> arg5) {
        super(ctx, new ZombieEntityModel(ctx.getPart(layer)), new ZombieEntityModel(ctx.getPart(legsArmorLayer)), EquipmentModelData.mapToEntityModel(arg4, ctx.getEntityModels(), ZombieEntityModel::new), EquipmentModelData.mapToEntityModel(arg5, ctx.getEntityModels(), ZombieEntityModel::new));
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

