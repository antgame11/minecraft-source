/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.datafixer.TypeReferences;

public class OptionsAddTextBackgroundFix
extends DataFix {
    public OptionsAddTextBackgroundFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("OptionsAddTextBackgroundFix", this.getInputSchema().getType(TypeReferences.OPTIONS), optionsTyped -> optionsTyped.update(DSL.remainderFinder(), optionsDynamic -> DataFixUtils.orElse(optionsDynamic.get("chatOpacity").asString().map(string -> {
            double d = this.convertToTextBackgroundOpacity((String)string);
            return optionsDynamic.set("textBackgroundOpacity", optionsDynamic.createString(String.valueOf(d)));
        }).result(), optionsDynamic)));
    }

    private double convertToTextBackgroundOpacity(String chatOpacity) {
        try {
            double d = 0.9 * Double.parseDouble(chatOpacity) + 0.1;
            return d / 2.0;
        } catch (NumberFormatException numberFormatException) {
            return 0.5;
        }
    }
}

