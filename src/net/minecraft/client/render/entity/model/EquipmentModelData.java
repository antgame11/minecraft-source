/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableMap;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.entity.EquipmentSlot;

@Environment(value=EnvType.CLIENT)
public record EquipmentModelData<T>(T head, T chest, T legs, T feet) {
    public T getModelData(EquipmentSlot slot) {
        return switch (slot) {
            case EquipmentSlot.HEAD -> this.head;
            case EquipmentSlot.CHEST -> this.chest;
            case EquipmentSlot.LEGS -> this.legs;
            case EquipmentSlot.FEET -> this.feet;
            default -> throw new IllegalStateException("No model for slot: " + String.valueOf(slot));
        };
    }

    public <U> EquipmentModelData<U> map(Function<? super T, ? extends U> f) {
        return new EquipmentModelData<U>(f.apply(this.head), f.apply(this.chest), f.apply(this.legs), f.apply(this.feet));
    }

    public void addTo(EquipmentModelData<TexturedModelData> texturedModelData, ImmutableMap.Builder<T, TexturedModelData> builder) {
        builder.put(this.head, (TexturedModelData)texturedModelData.head);
        builder.put(this.chest, (TexturedModelData)texturedModelData.chest);
        builder.put(this.legs, (TexturedModelData)texturedModelData.legs);
        builder.put(this.feet, (TexturedModelData)texturedModelData.feet);
    }

    public static <M extends BipedEntityModel<?>> EquipmentModelData<M> mapToEntityModel(EquipmentModelData<EntityModelLayer> data, LoadedEntityModels models, Function<ModelPart, M> modelPartToModel) {
        return data.map(layer -> (BipedEntityModel)modelPartToModel.apply(models.getModelPart((EntityModelLayer)layer)));
    }
}

