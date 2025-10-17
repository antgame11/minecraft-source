/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;

public class AreaEffectCloudPotionFix
extends ChoiceFix {
    public AreaEffectCloudPotionFix(Schema outputSchema) {
        super(outputSchema, false, "AreaEffectCloudPotionFix", TypeReferences.ENTITY, "minecraft:area_effect_cloud");
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), this::update);
    }

    private <T> Dynamic<T> update(Dynamic<T> areaEffectCloudDynamic) {
        Optional<Dynamic<T>> optional = areaEffectCloudDynamic.get("Color").result();
        Optional<Dynamic<T>> optional2 = areaEffectCloudDynamic.get("effects").result();
        Optional<Dynamic<T>> optional3 = areaEffectCloudDynamic.get("Potion").result();
        areaEffectCloudDynamic = areaEffectCloudDynamic.remove("Color").remove("effects").remove("Potion");
        if (optional.isEmpty() && optional2.isEmpty() && optional3.isEmpty()) {
            return areaEffectCloudDynamic;
        }
        Dynamic dynamic2 = areaEffectCloudDynamic.emptyMap();
        if (optional.isPresent()) {
            dynamic2 = dynamic2.set("custom_color", optional.get());
        }
        if (optional2.isPresent()) {
            dynamic2 = dynamic2.set("custom_effects", optional2.get());
        }
        if (optional3.isPresent()) {
            dynamic2 = dynamic2.set("potion", optional3.get());
        }
        return areaEffectCloudDynamic.set("potion_contents", dynamic2);
    }
}

