/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.TextFixes;

public class DisplayNameFix
extends DataFix {
    private final String name;
    private final DSL.TypeReference typeReference;

    public DisplayNameFix(Schema outputSchema, String name, DSL.TypeReference typeReference) {
        super(outputSchema, false);
        this.name = name;
        this.typeReference = typeReference;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(this.typeReference);
        OpticFinder<?> opticFinder = type.findField("DisplayName");
        OpticFinder<?> opticFinder2 = DSL.typeFinder(this.getInputSchema().getType(TypeReferences.TEXT_COMPONENT));
        return this.fixTypeEverywhereTyped(this.name, type, typed2 -> typed2.updateTyped(opticFinder, typed -> typed.update(opticFinder2, pair -> pair.mapSecond(TextFixes::text))));
    }
}

