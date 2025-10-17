/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.entry;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.entry.LootPoolEntryTypes;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.ErrorReporter;

public class LootTableEntry
extends LeafEntry {
    public static final MapCodec<LootTableEntry> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.either(LootTable.TABLE_KEY, LootTable.CODEC).fieldOf("value")).forGetter(entry -> entry.value)).and(LootTableEntry.addLeafFields(instance)).apply(instance, LootTableEntry::new));
    public static final ErrorReporter.Context INLINE_CONTEXT = new ErrorReporter.Context(){

        @Override
        public String getName() {
            return "->{inline}";
        }
    };
    private final Either<RegistryKey<LootTable>, LootTable> value;

    private LootTableEntry(Either<RegistryKey<LootTable>, LootTable> value, int weight, int quality, List<LootCondition> conditions, List<LootFunction> functions) {
        super(weight, quality, conditions, functions);
        this.value = value;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntryTypes.LOOT_TABLE;
    }

    @Override
    public void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
        this.value.map(key -> context.getLookup().getOptionalEntry(key).map(RegistryEntry::value).orElse(LootTable.EMPTY), table -> table).generateUnprocessedLoot(context, lootConsumer);
    }

    @Override
    public void validate(LootTableReporter reporter) {
        Optional<RegistryKey<LootTable>> optional = this.value.left();
        if (optional.isPresent()) {
            RegistryKey<LootTable> lv = optional.get();
            if (!reporter.canUseReferences()) {
                reporter.report(new LootTableReporter.ReferenceNotAllowedError(lv));
                return;
            }
            if (reporter.isInStack(lv)) {
                reporter.report(new LootTableReporter.RecursionError(lv));
                return;
            }
        }
        super.validate(reporter);
        this.value.ifLeft(key -> reporter.getDataLookup().getOptionalEntry(key).ifPresentOrElse(entry -> ((LootTable)entry.value()).validate(reporter.makeChild(new ErrorReporter.ReferenceLootTableContext((RegistryKey<?>)key), (RegistryKey<?>)key)), () -> reporter.report(new LootTableReporter.MissingElementError((RegistryKey<?>)key)))).ifRight(table -> table.validate(reporter.makeChild(INLINE_CONTEXT)));
    }

    public static LeafEntry.Builder<?> builder(RegistryKey<LootTable> key) {
        return LootTableEntry.builder((int weight, int quality, List<LootCondition> conditions, List<LootFunction> functions) -> new LootTableEntry(Either.left(key), weight, quality, conditions, functions));
    }

    public static LeafEntry.Builder<?> builder(LootTable table) {
        return LootTableEntry.builder((int weight, int quality, List<LootCondition> conditions, List<LootFunction> functions) -> new LootTableEntry(Either.right(table), weight, quality, conditions, functions));
    }
}

