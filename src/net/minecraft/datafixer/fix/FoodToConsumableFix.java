/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;

public class FoodToConsumableFix
extends DataFix {
    public FoodToConsumableFix(Schema schema) {
        super(schema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.writeFixAndRead("Food to consumable fix", this.getInputSchema().getType(TypeReferences.DATA_COMPONENTS), this.getOutputSchema().getType(TypeReferences.DATA_COMPONENTS), dynamic2 -> {
            Optional optional = dynamic2.get("minecraft:food").result();
            if (optional.isPresent()) {
                float f = optional.get().get("eat_seconds").asFloat(1.6f);
                Stream<Dynamic<Dynamic>> stream = optional.get().get("effects").asStream();
                Stream<Dynamic> stream2 = stream.map(dynamic -> dynamic.emptyMap().set("type", dynamic.createString("minecraft:apply_effects")).set("effects", dynamic.createList(dynamic.get("effect").result().stream())).set("probability", dynamic.createFloat(dynamic.get("probability").asFloat(1.0f))));
                dynamic2 = Dynamic.copyField(optional.get(), "using_converts_to", dynamic2, "minecraft:use_remainder");
                dynamic2 = dynamic2.set("minecraft:food", optional.get().remove("eat_seconds").remove("effects").remove("using_converts_to"));
                dynamic2 = dynamic2.set("minecraft:consumable", dynamic2.emptyMap().set("consume_seconds", dynamic2.createFloat(f)).set("on_consume_effects", dynamic2.createList(stream2)));
                return dynamic2;
            }
            return dynamic2;
        });
    }
}

