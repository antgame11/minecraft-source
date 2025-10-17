/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import com.mojang.serialization.Codec;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class EntityEquipment {
    public static final Codec<EntityEquipment> CODEC = Codec.unboundedMap(EquipmentSlot.CODEC, ItemStack.CODEC).xmap(map -> {
        EnumMap<EquipmentSlot, ItemStack> enumMap = new EnumMap<EquipmentSlot, ItemStack>(EquipmentSlot.class);
        enumMap.putAll((Map<EquipmentSlot, ItemStack>)map);
        return new EntityEquipment(enumMap);
    }, equipment -> {
        EnumMap<EquipmentSlot, ItemStack> map = new EnumMap<EquipmentSlot, ItemStack>(equipment.map);
        map.values().removeIf(ItemStack::isEmpty);
        return map;
    });
    private final EnumMap<EquipmentSlot, ItemStack> map;

    private EntityEquipment(EnumMap<EquipmentSlot, ItemStack> map) {
        this.map = map;
    }

    public EntityEquipment() {
        this(new EnumMap<EquipmentSlot, ItemStack>(EquipmentSlot.class));
    }

    public ItemStack put(EquipmentSlot slot, ItemStack arg2) {
        return Objects.requireNonNullElse(this.map.put(slot, arg2), ItemStack.EMPTY);
    }

    public ItemStack get(EquipmentSlot slot) {
        return this.map.getOrDefault(slot, ItemStack.EMPTY);
    }

    public boolean isEmpty() {
        for (ItemStack lv : this.map.values()) {
            if (lv.isEmpty()) continue;
            return false;
        }
        return true;
    }

    public void tick(Entity entity) {
        for (Map.Entry<EquipmentSlot, ItemStack> entry : this.map.entrySet()) {
            ItemStack lv = entry.getValue();
            if (lv.isEmpty()) continue;
            lv.inventoryTick(entity.getEntityWorld(), entity, entry.getKey());
        }
    }

    public void copyFrom(EntityEquipment equipment) {
        this.map.clear();
        this.map.putAll(equipment.map);
    }

    public void dropAll(LivingEntity entity) {
        for (ItemStack lv : this.map.values()) {
            entity.dropItem(lv, true, false);
        }
        this.clear();
    }

    public void clear() {
        this.map.replaceAll((slot, stack) -> ItemStack.EMPTY);
    }
}

