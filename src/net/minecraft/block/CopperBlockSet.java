/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Oxidizable;
import org.apache.commons.lang3.function.TriFunction;

public record CopperBlockSet(Block unaffected, Block exposed, Block weathered, Block oxidized, Block waxed, Block waxedExposed, Block waxedWeathered, Block waxedOxidized) {
    public static <WaxedBlock extends Block, WeatheringBlock extends Block> CopperBlockSet create(String baseId, TriFunction<String, Function<AbstractBlock.Settings, Block>, AbstractBlock.Settings, Block> registerFunction, Function<AbstractBlock.Settings, WaxedBlock> waxedBlockFactory, BiFunction<Oxidizable.OxidationLevel, AbstractBlock.Settings, WeatheringBlock> unwaxedBlockFactory, Function<Oxidizable.OxidationLevel, AbstractBlock.Settings> settingsFromOxidationLevel) {
        return new CopperBlockSet(registerFunction.apply(baseId, settings -> (Block)unwaxedBlockFactory.apply(Oxidizable.OxidationLevel.UNAFFECTED, (AbstractBlock.Settings)settings), settingsFromOxidationLevel.apply(Oxidizable.OxidationLevel.UNAFFECTED)), registerFunction.apply("exposed_" + baseId, settings -> (Block)unwaxedBlockFactory.apply(Oxidizable.OxidationLevel.EXPOSED, (AbstractBlock.Settings)settings), settingsFromOxidationLevel.apply(Oxidizable.OxidationLevel.EXPOSED)), registerFunction.apply("weathered_" + baseId, settings -> (Block)unwaxedBlockFactory.apply(Oxidizable.OxidationLevel.WEATHERED, (AbstractBlock.Settings)settings), settingsFromOxidationLevel.apply(Oxidizable.OxidationLevel.WEATHERED)), registerFunction.apply("oxidized_" + baseId, settings -> (Block)unwaxedBlockFactory.apply(Oxidizable.OxidationLevel.OXIDIZED, (AbstractBlock.Settings)settings), settingsFromOxidationLevel.apply(Oxidizable.OxidationLevel.OXIDIZED)), registerFunction.apply("waxed_" + baseId, waxedBlockFactory::apply, settingsFromOxidationLevel.apply(Oxidizable.OxidationLevel.UNAFFECTED)), registerFunction.apply("waxed_exposed_" + baseId, waxedBlockFactory::apply, settingsFromOxidationLevel.apply(Oxidizable.OxidationLevel.EXPOSED)), registerFunction.apply("waxed_weathered_" + baseId, waxedBlockFactory::apply, settingsFromOxidationLevel.apply(Oxidizable.OxidationLevel.WEATHERED)), registerFunction.apply("waxed_oxidized_" + baseId, waxedBlockFactory::apply, settingsFromOxidationLevel.apply(Oxidizable.OxidationLevel.OXIDIZED)));
    }

    public ImmutableBiMap<Block, Block> getOxidizingMap() {
        return ImmutableBiMap.of(this.unaffected, this.exposed, this.exposed, this.weathered, this.weathered, this.oxidized);
    }

    public ImmutableBiMap<Block, Block> getWaxingMap() {
        return ImmutableBiMap.of(this.unaffected, this.waxed, this.exposed, this.waxedExposed, this.weathered, this.waxedWeathered, this.oxidized, this.waxedOxidized);
    }

    public ImmutableList<Block> getAll() {
        return ImmutableList.of(this.unaffected, this.waxed, this.exposed, this.waxedExposed, this.weathered, this.waxedWeathered, this.oxidized, this.waxedOxidized);
    }

    public void forEach(Consumer<Block> consumer) {
        consumer.accept(this.unaffected);
        consumer.accept(this.exposed);
        consumer.accept(this.weathered);
        consumer.accept(this.oxidized);
        consumer.accept(this.waxed);
        consumer.accept(this.waxedExposed);
        consumer.accept(this.waxedWeathered);
        consumer.accept(this.waxedOxidized);
    }
}

