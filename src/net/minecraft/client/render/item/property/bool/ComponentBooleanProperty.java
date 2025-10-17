/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item.property.bool;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.bool.BooleanProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.component.ComponentPredicate;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record ComponentBooleanProperty(ComponentPredicate.Typed<?> predicate) implements BooleanProperty
{
    public static final MapCodec<ComponentBooleanProperty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(ComponentPredicate.createCodec("predicate").forGetter(ComponentBooleanProperty::predicate)).apply((Applicative<ComponentBooleanProperty, ?>)instance, ComponentBooleanProperty::new));

    @Override
    public boolean test(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        return this.predicate.predicate().test(stack);
    }

    public MapCodec<ComponentBooleanProperty> getCodec() {
        return CODEC;
    }
}

