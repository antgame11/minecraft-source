/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.InstrumentComponent;
import net.minecraft.item.Instrument;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;

public class SetInstrumentLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<SetInstrumentLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetInstrumentLootFunction.addConditionsField(instance).and(((MapCodec)TagKey.codec(RegistryKeys.INSTRUMENT).fieldOf("options")).forGetter(function -> function.options)).apply((Applicative<SetInstrumentLootFunction, ?>)instance, SetInstrumentLootFunction::new));
    private final TagKey<Instrument> options;

    private SetInstrumentLootFunction(List<LootCondition> conditions, TagKey<Instrument> options) {
        super(conditions);
        this.options = options;
    }

    public LootFunctionType<SetInstrumentLootFunction> getType() {
        return LootFunctionTypes.SET_INSTRUMENT;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        RegistryWrapper.Impl lv = context.getWorld().getRegistryManager().getOrThrow(RegistryKeys.INSTRUMENT);
        Optional<RegistryEntry<Instrument>> optional = lv.getRandomEntry(this.options, context.getRandom());
        if (optional.isPresent()) {
            stack.set(DataComponentTypes.INSTRUMENT, new InstrumentComponent(optional.get()));
        }
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> builder(TagKey<Instrument> options) {
        return SetInstrumentLootFunction.builder((List<LootCondition> conditions) -> new SetInstrumentLootFunction((List<LootCondition>)conditions, options));
    }
}

