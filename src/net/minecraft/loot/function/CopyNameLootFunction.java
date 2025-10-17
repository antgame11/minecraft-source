/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.util.Nameable;
import net.minecraft.util.context.ContextParameter;
import net.minecraft.util.dynamic.Codecs;

public class CopyNameLootFunction
extends ConditionalLootFunction {
    private static final Codecs.IdMapper<String, NameSource> SOURCES = new Codecs.IdMapper();
    public static final MapCodec<CopyNameLootFunction> CODEC;
    private final NameSource source;

    private CopyNameLootFunction(List<LootCondition> conditions, NameSource source) {
        super(conditions);
        this.source = source;
    }

    public LootFunctionType<CopyNameLootFunction> getType() {
        return LootFunctionTypes.COPY_NAME;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return Set.of(this.source.param);
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        Object object = context.get(this.source.param);
        if (object instanceof Nameable) {
            Nameable lv = (Nameable)object;
            stack.set(DataComponentTypes.CUSTOM_NAME, lv.getCustomName());
        }
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> builder(NameSource source) {
        return CopyNameLootFunction.builder((List<LootCondition> conditions) -> new CopyNameLootFunction((List<LootCondition>)conditions, source));
    }

    static {
        for (LootContext.EntityReference entityReference : LootContext.EntityReference.values()) {
            SOURCES.put(entityReference.asString(), new NameSource(entityReference.getParameter()));
        }
        for (Enum enum_ : LootContext.BlockEntityReference.values()) {
            SOURCES.put(((LootContext.BlockEntityReference)enum_).asString(), new NameSource(((LootContext.BlockEntityReference)enum_).getParameter()));
        }
        CODEC = RecordCodecBuilder.mapCodec(instance -> CopyNameLootFunction.addConditionsField(instance).and(((MapCodec)SOURCES.getCodec(Codec.STRING).fieldOf("source")).forGetter(function -> function.source)).apply((Applicative<CopyNameLootFunction, ?>)instance, CopyNameLootFunction::new));
    }

    public record NameSource(ContextParameter<?> param) {
    }
}

