/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class ItemEnchantmentsComponent
implements TooltipAppender {
    public static final ItemEnchantmentsComponent DEFAULT = new ItemEnchantmentsComponent(new Object2IntOpenHashMap<RegistryEntry<Enchantment>>());
    private static final Codec<Integer> ENCHANTMENT_LEVEL_CODEC = Codec.intRange(1, 255);
    public static final Codec<ItemEnchantmentsComponent> CODEC = Codec.unboundedMap(Enchantment.ENTRY_CODEC, ENCHANTMENT_LEVEL_CODEC).xmap(map -> new ItemEnchantmentsComponent(new Object2IntOpenHashMap<RegistryEntry<Enchantment>>((Map<RegistryEntry<Enchantment>, Integer>)map)), arg -> arg.enchantments);
    public static final PacketCodec<RegistryByteBuf, ItemEnchantmentsComponent> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.map(Object2IntOpenHashMap::new, Enchantment.ENTRY_PACKET_CODEC, PacketCodecs.VAR_INT), component -> component.enchantments, ItemEnchantmentsComponent::new);
    final Object2IntOpenHashMap<RegistryEntry<Enchantment>> enchantments;

    ItemEnchantmentsComponent(Object2IntOpenHashMap<RegistryEntry<Enchantment>> enchantments) {
        this.enchantments = enchantments;
        for (Object2IntMap.Entry entry : enchantments.object2IntEntrySet()) {
            int i = entry.getIntValue();
            if (i >= 0 && i <= 255) continue;
            throw new IllegalArgumentException("Enchantment " + String.valueOf(entry.getKey()) + " has invalid level " + i);
        }
    }

    public int getLevel(RegistryEntry<Enchantment> enchantment) {
        return this.enchantments.getInt(enchantment);
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        RegistryWrapper.WrapperLookup lv = context.getRegistryLookup();
        RegistryEntryList<Enchantment> lv2 = ItemEnchantmentsComponent.getTooltipOrderList(lv, RegistryKeys.ENCHANTMENT, EnchantmentTags.TOOLTIP_ORDER);
        for (RegistryEntry registryEntry : lv2) {
            int i = this.enchantments.getInt(registryEntry);
            if (i <= 0) continue;
            textConsumer.accept(Enchantment.getName(registryEntry, i));
        }
        for (Object2IntMap.Entry entry : this.enchantments.object2IntEntrySet()) {
            RegistryEntry lv4 = (RegistryEntry)entry.getKey();
            if (lv2.contains(lv4)) continue;
            textConsumer.accept(Enchantment.getName((RegistryEntry)entry.getKey(), entry.getIntValue()));
        }
    }

    private static <T> RegistryEntryList<T> getTooltipOrderList(@Nullable RegistryWrapper.WrapperLookup registries, RegistryKey<Registry<T>> registryRef, TagKey<T> tooltipOrderTag) {
        Optional<RegistryEntryList.Named<T>> optional;
        if (registries != null && (optional = registries.getOrThrow(registryRef).getOptional(tooltipOrderTag)).isPresent()) {
            return optional.get();
        }
        return RegistryEntryList.of(new RegistryEntry[0]);
    }

    public Set<RegistryEntry<Enchantment>> getEnchantments() {
        return Collections.unmodifiableSet(this.enchantments.keySet());
    }

    public Set<Object2IntMap.Entry<RegistryEntry<Enchantment>>> getEnchantmentEntries() {
        return Collections.unmodifiableSet(this.enchantments.object2IntEntrySet());
    }

    public int getSize() {
        return this.enchantments.size();
    }

    public boolean isEmpty() {
        return this.enchantments.isEmpty();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ItemEnchantmentsComponent) {
            ItemEnchantmentsComponent lv = (ItemEnchantmentsComponent)o;
            return this.enchantments.equals(lv.enchantments);
        }
        return false;
    }

    public int hashCode() {
        return this.enchantments.hashCode();
    }

    public String toString() {
        return "ItemEnchantments{enchantments=" + String.valueOf(this.enchantments) + "}";
    }

    public static class Builder {
        private final Object2IntOpenHashMap<RegistryEntry<Enchantment>> enchantments = new Object2IntOpenHashMap();

        public Builder(ItemEnchantmentsComponent enchantmentsComponent) {
            this.enchantments.putAll((Map<RegistryEntry<Enchantment>, Integer>)enchantmentsComponent.enchantments);
        }

        public void set(RegistryEntry<Enchantment> enchantment, int level) {
            if (level <= 0) {
                this.enchantments.removeInt(enchantment);
            } else {
                this.enchantments.put(enchantment, Math.min(level, 255));
            }
        }

        public void add(RegistryEntry<Enchantment> enchantment, int level) {
            if (level > 0) {
                this.enchantments.merge(enchantment, Math.min(level, 255), Integer::max);
            }
        }

        public void remove(Predicate<RegistryEntry<Enchantment>> predicate) {
            this.enchantments.keySet().removeIf(predicate);
        }

        public int getLevel(RegistryEntry<Enchantment> enchantment) {
            return this.enchantments.getOrDefault((Object)enchantment, 0);
        }

        public Set<RegistryEntry<Enchantment>> getEnchantments() {
            return this.enchantments.keySet();
        }

        public ItemEnchantmentsComponent build() {
            return new ItemEnchantmentsComponent(this.enchantments);
        }
    }
}

