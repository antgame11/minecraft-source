/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import net.minecraft.entity.EquipmentSlot;

public enum Hand {
    MAIN_HAND,
    OFF_HAND;


    public EquipmentSlot getEquipmentSlot() {
        return this == MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
    }
}

