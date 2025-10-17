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

public class Schema4300
extends IdentifierNormalizingSchema {
    public Schema4300(int i, Schema schema) {
        super(i, schema);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
        schema.register(map, "minecraft:llama", (String string) -> Schema4300.method_66634(schema));
        schema.register(map, "minecraft:trader_llama", (String string) -> Schema4300.method_66634(schema));
        schema.register(map, "minecraft:donkey", (String string) -> Schema4300.method_66634(schema));
        schema.register(map, "minecraft:mule", (String string) -> Schema4300.method_66634(schema));
        schema.registerSimple(map, "minecraft:horse");
        schema.registerSimple(map, "minecraft:skeleton_horse");
        schema.registerSimple(map, "minecraft:zombie_horse");
        return map;
    }

    private static TypeTemplate method_66634(Schema schema) {
        return DSL.optionalFields("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)));
    }
}

