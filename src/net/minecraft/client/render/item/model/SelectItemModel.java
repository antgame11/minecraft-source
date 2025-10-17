/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item.model;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.ItemModelTypes;
import net.minecraft.client.render.item.property.select.SelectProperties;
import net.minecraft.client.render.item.property.select.SelectProperty;
import net.minecraft.client.render.model.ResolvableModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.world.DataCache;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.ContextSwapper;
import net.minecraft.util.HeldItemContext;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SelectItemModel<T>
implements ItemModel {
    private final SelectProperty<T> property;
    private final ModelSelector<T> selector;

    public SelectItemModel(SelectProperty<T> property, ModelSelector<T> selector) {
        this.property = property;
        this.selector = selector;
    }

    @Override
    public void update(ItemRenderState state, ItemStack stack, ItemModelManager resolver, ItemDisplayContext displayContext, @Nullable ClientWorld world, @Nullable HeldItemContext heldItemContext, int seed) {
        state.addModelKey(this);
        T object = this.property.getValue(stack, world, heldItemContext == null ? null : heldItemContext.getEntity(), seed, displayContext);
        ItemModel lv = this.selector.get(object, world);
        if (lv != null) {
            lv.update(state, stack, resolver, displayContext, world, heldItemContext, seed);
        }
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface ModelSelector<T> {
        @Nullable
        public ItemModel get(@Nullable T var1, @Nullable ClientWorld var2);
    }

    @Environment(value=EnvType.CLIENT)
    public record SwitchCase<T>(List<T> values, ItemModel.Unbaked model) {
        public static <T> Codec<SwitchCase<T>> createCodec(Codec<T> conditionCodec) {
            return RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codecs.nonEmptyList(Codecs.listOrSingle(conditionCodec)).fieldOf("when")).forGetter(SwitchCase::values), ((MapCodec)ItemModelTypes.CODEC.fieldOf("model")).forGetter(SwitchCase::model)).apply((Applicative<SwitchCase, ?>)instance, SwitchCase::new));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record UnbakedSwitch<P extends SelectProperty<T>, T>(P property, List<SwitchCase<T>> cases) {
        public static final MapCodec<UnbakedSwitch<?, ?>> CODEC = SelectProperties.CODEC.dispatchMap("property", unbakedSwitch -> unbakedSwitch.property().getType(), SelectProperty.Type::switchCodec);

        public ItemModel bake(ItemModel.BakeContext context, ItemModel fallback) {
            Object2ObjectOpenHashMap object2ObjectMap = new Object2ObjectOpenHashMap();
            for (SwitchCase<T> lv : this.cases) {
                ItemModel.Unbaked lv2 = lv.model;
                ItemModel lv3 = lv2.bake(context);
                for (Object object : lv.values) {
                    object2ObjectMap.put(object, lv3);
                }
            }
            object2ObjectMap.defaultReturnValue(fallback);
            return new SelectItemModel(this.property, this.buildModelSelector(object2ObjectMap, context.contextSwapper()));
        }

        private ModelSelector<T> buildModelSelector(Object2ObjectMap<T, ItemModel> models, @Nullable ContextSwapper contextSwapper) {
            if (contextSwapper == null) {
                return (value, world) -> (ItemModel)models.get(value);
            }
            ItemModel lv = models.defaultReturnValue();
            DataCache<ClientWorld, Object2ObjectMap> lv2 = new DataCache<ClientWorld, Object2ObjectMap>(world2 -> {
                Object2ObjectOpenHashMap object2ObjectMap2 = new Object2ObjectOpenHashMap(models.size());
                object2ObjectMap2.defaultReturnValue(lv);
                models.forEach((value, world) -> contextSwapper.swapContext(this.property.valueCodec(), value, world2.getRegistryManager()).ifSuccess(swappedValue -> object2ObjectMap2.put(swappedValue, (ItemModel)world)));
                return object2ObjectMap2;
            });
            return (value, world) -> {
                if (world == null) {
                    return (ItemModel)models.get(value);
                }
                if (value == null) {
                    return lv;
                }
                return (ItemModel)((Object2ObjectMap)lv2.compute(world)).get(value);
            };
        }

        public void resolveCases(ResolvableModel.Resolver resolver) {
            for (SwitchCase<T> lv : this.cases) {
                lv.model.resolve(resolver);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked(UnbakedSwitch<?, ?> unbakedSwitch, Optional<ItemModel.Unbaked> fallback) implements ItemModel.Unbaked
    {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(UnbakedSwitch.CODEC.forGetter(Unbaked::unbakedSwitch), ItemModelTypes.CODEC.optionalFieldOf("fallback").forGetter(Unbaked::fallback)).apply((Applicative<Unbaked, ?>)instance, Unbaked::new));

        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public ItemModel bake(ItemModel.BakeContext context) {
            ItemModel lv = this.fallback.map(model -> model.bake(context)).orElse(context.missingItemModel());
            return this.unbakedSwitch.bake(context, lv);
        }

        @Override
        public void resolve(ResolvableModel.Resolver resolver) {
            this.unbakedSwitch.resolveCases(resolver);
            this.fallback.ifPresent(model -> model.resolve(resolver));
        }
    }
}

