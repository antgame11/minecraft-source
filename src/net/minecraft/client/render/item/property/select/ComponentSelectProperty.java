/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item.property.select;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.model.SelectItemModel;
import net.minecraft.client.render.item.property.select.SelectProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record ComponentSelectProperty<T>(ComponentType<T> componentType) implements SelectProperty<T>
{
    private static final SelectProperty.Type<? extends ComponentSelectProperty<?>, ?> TYPE = ComponentSelectProperty.createType();

    private static <T> SelectProperty.Type<ComponentSelectProperty<T>, T> createType() {
        Codec<ComponentType> codec;
        Codec<ComponentType> codec2 = codec = Registries.DATA_COMPONENT_TYPE.getCodec().validate(componentType -> {
            if (componentType.shouldSkipSerialization()) {
                return DataResult.error(() -> "Component can't be serialized");
            }
            return DataResult.success(componentType);
        });
        MapCodec mapCodec = codec2.dispatchMap("component", unbakedSwitch -> ((ComponentSelectProperty)unbakedSwitch.property()).componentType, componentType -> SelectProperty.Type.createCaseListCodec(componentType.getCodecOrThrow()).xmap(cases -> new SelectItemModel.UnbakedSwitch(new ComponentSelectProperty(componentType), cases), SelectItemModel.UnbakedSwitch::cases));
        return new SelectProperty.Type(mapCodec);
    }

    public static <T> SelectProperty.Type<ComponentSelectProperty<T>, T> getTypeInstance() {
        return TYPE;
    }

    @Override
    @Nullable
    public T getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity user, int seed, ItemDisplayContext displayContext) {
        return stack.get(this.componentType);
    }

    @Override
    public SelectProperty.Type<ComponentSelectProperty<T>, T> getType() {
        return ComponentSelectProperty.getTypeInstance();
    }

    @Override
    public Codec<T> valueCodec() {
        return this.componentType.getCodecOrThrow();
    }
}

