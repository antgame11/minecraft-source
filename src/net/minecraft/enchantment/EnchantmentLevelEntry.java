/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;

public record EnchantmentLevelEntry(RegistryEntry<Enchantment> enchantment, int level) {
    public int getWeight() {
        return this.enchantment().value().getWeight();
    }
}

