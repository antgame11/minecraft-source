/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Set;
import net.minecraft.datafixer.FixUtil;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;
import net.minecraft.util.Util;

public class SaddleEquipmentSlotFix
extends DataFix {
    private static final Set<String> FULL_SADDLE_STACK_ENTITY_IDS = Set.of("minecraft:horse", "minecraft:skeleton_horse", "minecraft:zombie_horse", "minecraft:donkey", "minecraft:mule", "minecraft:camel", "minecraft:llama", "minecraft:trader_llama");
    private static final Set<String> BOOLEAN_SADDLE_ENTITY_IDS = Set.of("minecraft:pig", "minecraft:strider");
    private static final String OLD_NBT_KEY = "Saddle";
    private static final String NEW_NBT_KEY = "saddle";

    public SaddleEquipmentSlotFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        TaggedChoice.TaggedChoiceType<?> taggedChoiceType = this.getInputSchema().findChoiceType(TypeReferences.ENTITY);
        OpticFinder<?> opticFinder = DSL.typeFinder(taggedChoiceType);
        Type<?> type = this.getInputSchema().getType(TypeReferences.ENTITY);
        Type<?> type2 = this.getOutputSchema().getType(TypeReferences.ENTITY);
        Type<?> type3 = FixUtil.withTypeChanged(type, type, type2);
        return this.fixTypeEverywhereTyped("SaddleEquipmentSlotFix", type, type2, (Typed<?> typed) -> {
            String string = typed.getOptional(opticFinder).map(Pair::getFirst).map(IdentifierNormalizingSchema::normalize).orElse("");
            Typed typed2 = FixUtil.withType(type3, typed);
            if (FULL_SADDLE_STACK_ENTITY_IDS.contains(string)) {
                return Util.apply(typed2, type2, SaddleEquipmentSlotFix::fixFullSaddleStackEntity);
            }
            if (BOOLEAN_SADDLE_ENTITY_IDS.contains(string)) {
                return Util.apply(typed2, type2, SaddleEquipmentSlotFix::fixBooleanSaddleEntity);
            }
            return FixUtil.withType(type2, typed);
        });
    }

    private static Dynamic<?> fixFullSaddleStackEntity(Dynamic<?> nbt) {
        if (nbt.get("SaddleItem").result().isEmpty()) {
            return nbt;
        }
        return SaddleEquipmentSlotFix.fixDropChances(nbt.renameField("SaddleItem", NEW_NBT_KEY));
    }

    private static Dynamic<?> fixBooleanSaddleEntity(Dynamic<?> nbt) {
        boolean bl = nbt.get(OLD_NBT_KEY).asBoolean(false);
        nbt = nbt.remove(OLD_NBT_KEY);
        if (!bl) {
            return nbt;
        }
        Dynamic dynamic2 = nbt.emptyMap().set("id", nbt.createString("minecraft:saddle")).set("count", nbt.createInt(1));
        return SaddleEquipmentSlotFix.fixDropChances(nbt.set(NEW_NBT_KEY, dynamic2));
    }

    private static Dynamic<?> fixDropChances(Dynamic<?> nbt) {
        Dynamic<?> dynamic2 = nbt.get("drop_chances").orElseEmptyMap().set(NEW_NBT_KEY, nbt.createFloat(2.0f));
        return nbt.set("drop_chances", dynamic2);
    }
}

