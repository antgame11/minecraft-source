/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item.property.select;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.select.SelectProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record ChargeTypeProperty() implements SelectProperty<CrossbowItem.ChargeType>
{
    public static final Codec<CrossbowItem.ChargeType> VALUE_CODEC = CrossbowItem.ChargeType.CODEC;
    public static final SelectProperty.Type<ChargeTypeProperty, CrossbowItem.ChargeType> TYPE = SelectProperty.Type.create(MapCodec.unit(new ChargeTypeProperty()), VALUE_CODEC);

    @Override
    public CrossbowItem.ChargeType getValue(ItemStack arg, @Nullable ClientWorld arg2, @Nullable LivingEntity arg3, int i, ItemDisplayContext arg4) {
        ChargedProjectilesComponent lv = arg.get(DataComponentTypes.CHARGED_PROJECTILES);
        if (lv == null || lv.isEmpty()) {
            return CrossbowItem.ChargeType.NONE;
        }
        if (lv.contains(Items.FIREWORK_ROCKET)) {
            return CrossbowItem.ChargeType.ROCKET;
        }
        return CrossbowItem.ChargeType.ARROW;
    }

    @Override
    public SelectProperty.Type<ChargeTypeProperty, CrossbowItem.ChargeType> getType() {
        return TYPE;
    }

    @Override
    public Codec<CrossbowItem.ChargeType> valueCodec() {
        return VALUE_CODEC;
    }

    @Override
    public /* synthetic */ Object getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity user, int seed, ItemDisplayContext displayContext) {
        return this.getValue(stack, world, user, seed, displayContext);
    }
}

