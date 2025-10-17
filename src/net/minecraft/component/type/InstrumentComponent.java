/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Instrument;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.LazyRegistryEntryReference;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;

public record InstrumentComponent(LazyRegistryEntryReference<Instrument> instrument) implements TooltipAppender
{
    public static final Codec<InstrumentComponent> CODEC = LazyRegistryEntryReference.createCodec(RegistryKeys.INSTRUMENT, Instrument.ENTRY_CODEC).xmap(InstrumentComponent::new, InstrumentComponent::instrument);
    public static final PacketCodec<RegistryByteBuf, InstrumentComponent> PACKET_CODEC = LazyRegistryEntryReference.createPacketCodec(RegistryKeys.INSTRUMENT, Instrument.ENTRY_PACKET_CODEC).xmap(InstrumentComponent::new, InstrumentComponent::instrument);

    public InstrumentComponent(RegistryEntry<Instrument> instrument) {
        this(new LazyRegistryEntryReference<Instrument>(instrument));
    }

    @Deprecated
    public InstrumentComponent(RegistryKey<Instrument> instrument) {
        this(new LazyRegistryEntryReference<Instrument>(instrument));
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        RegistryWrapper.WrapperLookup lv = context.getRegistryLookup();
        if (lv == null) {
            return;
        }
        Optional<RegistryEntry<Instrument>> optional = this.getInstrument(lv);
        if (optional.isPresent()) {
            MutableText lv2 = optional.get().value().description().copy();
            Texts.setStyleIfAbsent(lv2, Style.EMPTY.withColor(Formatting.GRAY));
            textConsumer.accept(lv2);
        }
    }

    public Optional<RegistryEntry<Instrument>> getInstrument(RegistryWrapper.WrapperLookup registries) {
        return this.instrument.resolveEntry(registries);
    }
}

