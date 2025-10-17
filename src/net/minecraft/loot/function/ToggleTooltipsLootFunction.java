/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;

public class ToggleTooltipsLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<ToggleTooltipsLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> ToggleTooltipsLootFunction.addConditionsField(instance).and(((MapCodec)Codec.unboundedMap(ComponentType.CODEC, Codec.BOOL).fieldOf("toggles")).forGetter(lootFunction -> lootFunction.toggles)).apply((Applicative<ToggleTooltipsLootFunction, ?>)instance, ToggleTooltipsLootFunction::new));
    private final Map<ComponentType<?>, Boolean> toggles;

    private ToggleTooltipsLootFunction(List<LootCondition> conditions, Map<ComponentType<?>, Boolean> toggles) {
        super(conditions);
        this.toggles = toggles;
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        stack.apply(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplayComponent.DEFAULT, arg -> {
            Iterator<Map.Entry<ComponentType<?>, Boolean>> iterator = this.toggles.entrySet().iterator();
            while (iterator.hasNext()) {
                boolean bl;
                Map.Entry<ComponentType<?>, Boolean> entry;
                arg = arg.with(entry.getKey(), !(bl = (entry = iterator.next()).getValue().booleanValue()));
            }
            return arg;
        });
        return stack;
    }

    public LootFunctionType<ToggleTooltipsLootFunction> getType() {
        return LootFunctionTypes.TOGGLE_TOOLTIPS;
    }
}

