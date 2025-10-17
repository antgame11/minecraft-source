/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item.model;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.ItemModelTypes;
import net.minecraft.client.render.item.property.PropertyTester;
import net.minecraft.client.render.item.property.bool.BooleanProperties;
import net.minecraft.client.render.item.property.bool.BooleanProperty;
import net.minecraft.client.render.model.ResolvableModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.world.DataCache;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.ContextSwapper;
import net.minecraft.util.HeldItemContext;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ConditionItemModel
implements ItemModel {
    private final PropertyTester property;
    private final ItemModel onTrue;
    private final ItemModel onFalse;

    public ConditionItemModel(PropertyTester property, ItemModel onTrue, ItemModel onFalse) {
        this.property = property;
        this.onTrue = onTrue;
        this.onFalse = onFalse;
    }

    @Override
    public void update(ItemRenderState state, ItemStack stack, ItemModelManager resolver, ItemDisplayContext displayContext, @Nullable ClientWorld world, @Nullable HeldItemContext heldItemContext, int seed) {
        state.addModelKey(this);
        (this.property.test(stack, world, heldItemContext == null ? null : heldItemContext.getEntity(), seed, displayContext) ? this.onTrue : this.onFalse).update(state, stack, resolver, displayContext, world, heldItemContext, seed);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked(BooleanProperty property, ItemModel.Unbaked onTrue, ItemModel.Unbaked onFalse) implements ItemModel.Unbaked
    {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(BooleanProperties.CODEC.forGetter(Unbaked::property), ((MapCodec)ItemModelTypes.CODEC.fieldOf("on_true")).forGetter(Unbaked::onTrue), ((MapCodec)ItemModelTypes.CODEC.fieldOf("on_false")).forGetter(Unbaked::onFalse)).apply((Applicative<Unbaked, ?>)instance, Unbaked::new));

        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public ItemModel bake(ItemModel.BakeContext context) {
            return new ConditionItemModel(this.makeWorldIndependentProperty(this.property, context.contextSwapper()), this.onTrue.bake(context), this.onFalse.bake(context));
        }

        private PropertyTester makeWorldIndependentProperty(BooleanProperty property, @Nullable ContextSwapper contextSwapper) {
            if (contextSwapper == null) {
                return property;
            }
            DataCache<ClientWorld, PropertyTester> lv = new DataCache<ClientWorld, PropertyTester>(world -> Unbaked.swapContext(property, contextSwapper, world));
            return (stack, world, entity, seed, transformationMode) -> {
                BooleanProperty lv = world == null ? property : (PropertyTester)lv.compute(world);
                return lv.test(stack, world, entity, seed, transformationMode);
            };
        }

        private static <T extends BooleanProperty> T swapContext(T value, ContextSwapper contextSwapper, ClientWorld world) {
            return (T)contextSwapper.swapContext(value.getCodec().codec(), value, world.getRegistryManager()).result().orElse(value);
        }

        @Override
        public void resolve(ResolvableModel.Resolver resolver) {
            this.onTrue.resolve(resolver);
            this.onFalse.resolve(resolver);
        }
    }
}

