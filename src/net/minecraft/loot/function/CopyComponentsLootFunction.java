/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextParameter;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;

public class CopyComponentsLootFunction
extends ConditionalLootFunction {
    private static final Codecs.IdMapper<String, ComponentsSource<?>> SOURCES = new Codecs.IdMapper();
    public static final MapCodec<CopyComponentsLootFunction> CODEC;
    private final ComponentsSource<?> source;
    private final Optional<List<ComponentType<?>>> include;
    private final Optional<List<ComponentType<?>>> exclude;
    private final Predicate<ComponentType<?>> filter;

    CopyComponentsLootFunction(List<LootCondition> conditions, ComponentsSource<?> source, Optional<List<ComponentType<?>>> include, Optional<List<ComponentType<?>>> exclude) {
        super(conditions);
        this.source = source;
        this.include = include.map(List::copyOf);
        this.exclude = exclude.map(List::copyOf);
        ArrayList list2 = new ArrayList(2);
        exclude.ifPresent(excludedTypes -> list2.add(type -> !excludedTypes.contains(type)));
        include.ifPresent(includedTypes -> list2.add(includedTypes::contains));
        this.filter = Util.allOf(list2);
    }

    public LootFunctionType<CopyComponentsLootFunction> getType() {
        return LootFunctionTypes.COPY_COMPONENTS;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return Set.of(this.source.contextParam());
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        ComponentsAccess lv = this.source.getComponents(context);
        if (lv != null) {
            if (lv instanceof ComponentMap) {
                ComponentMap lv2 = (ComponentMap)lv;
                stack.applyComponentsFrom(lv2.filtered(this.filter));
            } else {
                Collection collection = this.exclude.orElse(List.of());
                this.include.map(Collection::stream).orElse(Registries.DATA_COMPONENT_TYPE.streamEntries().map(RegistryEntry::value)).forEach(type -> {
                    if (collection.contains(type)) {
                        return;
                    }
                    Component lv = lv.getTyped(type);
                    if (lv != null) {
                        stack.set(lv);
                    }
                });
            }
        }
        return stack;
    }

    public static Builder entity(ContextParameter<? extends Entity> parameter) {
        return new Builder(new EntityComponentsSource(parameter));
    }

    public static Builder blockEntity(ContextParameter<? extends BlockEntity> parameter) {
        return new Builder(new BlockEntityComponentsSource(parameter));
    }

    static {
        for (LootContext.EntityReference entityReference : LootContext.EntityReference.values()) {
            SOURCES.put(entityReference.asString(), new EntityComponentsSource(entityReference.getParameter()));
        }
        for (Enum enum_ : LootContext.BlockEntityReference.values()) {
            SOURCES.put(((LootContext.BlockEntityReference)enum_).asString(), new BlockEntityComponentsSource(((LootContext.BlockEntityReference)enum_).getParameter()));
        }
        for (Enum enum_ : LootContext.ItemStackReference.values()) {
            SOURCES.put(((LootContext.ItemStackReference)enum_).asString(), new ItemStackComponentsSource(((LootContext.ItemStackReference)enum_).getParameter()));
        }
        CODEC = RecordCodecBuilder.mapCodec(instance -> CopyComponentsLootFunction.addConditionsField(instance).and(instance.group(((MapCodec)SOURCES.getCodec(Codec.STRING).fieldOf("source")).forGetter(function -> function.source), ComponentType.CODEC.listOf().optionalFieldOf("include").forGetter(function -> function.include), ComponentType.CODEC.listOf().optionalFieldOf("exclude").forGetter(function -> function.exclude))).apply((Applicative<CopyComponentsLootFunction, ?>)instance, CopyComponentsLootFunction::new));
    }

    public static interface ComponentsSource<T> {
        public ContextParameter<? extends T> contextParam();

        public ComponentsAccess getComponents(T var1);

        @Nullable
        default public ComponentsAccess getComponents(LootContext context) {
            T object = context.get(this.contextParam());
            return object != null ? this.getComponents(object) : null;
        }
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private final ComponentsSource<?> source;
        private Optional<ImmutableList.Builder<ComponentType<?>>> include = Optional.empty();
        private Optional<ImmutableList.Builder<ComponentType<?>>> exclude = Optional.empty();

        Builder(ComponentsSource<?> source) {
            this.source = source;
        }

        public Builder include(ComponentType<?> type) {
            if (this.include.isEmpty()) {
                this.include = Optional.of(ImmutableList.builder());
            }
            this.include.get().add((Object)type);
            return this;
        }

        public Builder exclude(ComponentType<?> type) {
            if (this.exclude.isEmpty()) {
                this.exclude = Optional.of(ImmutableList.builder());
            }
            this.exclude.get().add((Object)type);
            return this;
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        @Override
        public LootFunction build() {
            return new CopyComponentsLootFunction(this.getConditions(), this.source, this.include.map(ImmutableList.Builder::build), this.exclude.map(ImmutableList.Builder::build));
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }

    record EntityComponentsSource(ContextParameter<? extends Entity> contextParam) implements ComponentsSource<Entity>
    {
        @Override
        public ComponentsAccess getComponents(Entity arg) {
            return arg;
        }
    }

    record BlockEntityComponentsSource(ContextParameter<? extends BlockEntity> contextParam) implements ComponentsSource<BlockEntity>
    {
        @Override
        public ComponentsAccess getComponents(BlockEntity arg) {
            return arg.createComponentMap();
        }
    }

    record ItemStackComponentsSource(ContextParameter<? extends ItemStack> contextParam) implements ComponentsSource<ItemStack>
    {
        @Override
        public ComponentsAccess getComponents(ItemStack arg) {
            return arg.getComponents();
        }
    }
}

