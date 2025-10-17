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

public class Schema4302
extends IdentifierNormalizingSchema {
    public Schema4302(int i, Schema schema) {
        super(i, schema);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);
        schema.registerSimple(map, "minecraft:test_block");
        schema.register(map, "minecraft:test_instance_block", () -> DSL.optionalFields("data", DSL.optionalFields("error_message", TypeReferences.TEXT_COMPONENT.in(schema)), "errors", DSL.list(DSL.optionalFields("text", TypeReferences.TEXT_COMPONENT.in(schema)))));
        return map;
    }
}

