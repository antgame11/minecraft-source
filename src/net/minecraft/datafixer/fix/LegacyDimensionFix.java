/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class LegacyDimensionFix
extends DataFix {
    public LegacyDimensionFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    public TypeRewriteRule makeRule() {
        TypeRewriteRule typeRewriteRule = this.fixTypeEverywhereTyped("PlayerLegacyDimensionFix", this.getInputSchema().getType(TypeReferences.PLAYER), typed -> typed.update(DSL.remainderFinder(), this::fixPlayer));
        Type<?> type = this.getInputSchema().getType(TypeReferences.SAVED_DATA_MAP_DATA);
        OpticFinder<?> opticFinder = type.findField("data");
        TypeRewriteRule typeRewriteRule2 = this.fixTypeEverywhereTyped("MapLegacyDimensionFix", type, typed2 -> typed2.updateTyped(opticFinder, typed -> typed.update(DSL.remainderFinder(), this::fixMap)));
        return TypeRewriteRule.seq(typeRewriteRule, typeRewriteRule2);
    }

    private <T> Dynamic<T> fixMap(Dynamic<T> dynamic) {
        return dynamic.update("dimension", this::fix);
    }

    private <T> Dynamic<T> fixPlayer(Dynamic<T> dynamic) {
        return dynamic.update("Dimension", this::fix);
    }

    private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
        return DataFixUtils.orElse(dynamic.asNumber().result().map(id -> switch (id.intValue()) {
            case -1 -> dynamic.createString("minecraft:the_nether");
            case 1 -> dynamic.createString("minecraft:the_end");
            default -> dynamic.createString("minecraft:overworld");
        }), dynamic);
    }
}

