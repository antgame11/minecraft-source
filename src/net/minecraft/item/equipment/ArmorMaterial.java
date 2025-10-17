/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item.equipment;

import java.util.Map;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public record ArmorMaterial(int durability, Map<EquipmentType, Integer> defense, int enchantmentValue, RegistryEntry<SoundEvent> equipSound, float toughness, float knockbackResistance, TagKey<Item> repairIngredient, RegistryKey<EquipmentAsset> assetId) {
    public AttributeModifiersComponent createAttributeModifiers(EquipmentType equipmentType) {
        int i = this.defense.getOrDefault(equipmentType, 0);
        AttributeModifiersComponent.Builder lv = AttributeModifiersComponent.builder();
        AttributeModifierSlot lv2 = AttributeModifierSlot.forEquipmentSlot(equipmentType.getEquipmentSlot());
        Identifier lv3 = Identifier.ofVanilla("armor." + equipmentType.getName());
        lv.add(EntityAttributes.ARMOR, new EntityAttributeModifier(lv3, i, EntityAttributeModifier.Operation.ADD_VALUE), lv2);
        lv.add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(lv3, this.toughness, EntityAttributeModifier.Operation.ADD_VALUE), lv2);
        if (this.knockbackResistance > 0.0f) {
            lv.add(EntityAttributes.KNOCKBACK_RESISTANCE, new EntityAttributeModifier(lv3, this.knockbackResistance, EntityAttributeModifier.Operation.ADD_VALUE), lv2);
        }
        return lv.build();
    }
}

