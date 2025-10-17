/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class Schema1458
extends IdentifierNormalizingSchema {
    public Schema1458(int i, Schema schema) {
        super(i, schema);
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        super.registerTypes(schema, map, map2);
        schema.registerType(true, TypeReferences.ENTITY, () -> DSL.and(TypeReferences.ENTITY_EQUIPMENT.in(schema), DSL.optionalFields("CustomName", TypeReferences.TEXT_COMPONENT.in(schema), DSL.taggedChoiceLazy("id", Schema1458.getIdentifierType(), map))));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);
        schema.register(map, "minecraft:beacon", () -> Schema1458.customName(schema));
        schema.register(map, "minecraft:banner", () -> Schema1458.customName(schema));
        schema.register(map, "minecraft:brewing_stand", () -> Schema1458.itemsAndCustomName(schema));
        schema.register(map, "minecraft:chest", () -> Schema1458.itemsAndCustomName(schema));
        schema.register(map, "minecraft:trapped_chest", () -> Schema1458.itemsAndCustomName(schema));
        schema.register(map, "minecraft:dispenser", () -> Schema1458.itemsAndCustomName(schema));
        schema.register(map, "minecraft:dropper", () -> Schema1458.itemsAndCustomName(schema));
        schema.register(map, "minecraft:enchanting_table", () -> Schema1458.customName(schema));
        schema.register(map, "minecraft:furnace", () -> Schema1458.itemsAndCustomName(schema));
        schema.register(map, "minecraft:hopper", () -> Schema1458.itemsAndCustomName(schema));
        schema.register(map, "minecraft:shulker_box", () -> Schema1458.itemsAndCustomName(schema));
        return map;
    }

    public static TypeTemplate itemsAndCustomName(Schema schema) {
        return DSL.optionalFields("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "CustomName", TypeReferences.TEXT_COMPONENT.in(schema));
    }

    public static TypeTemplate customName(Schema schema) {
        return DSL.optionalFields("CustomName", TypeReferences.TEXT_COMPONENT.in(schema));
    }
}

