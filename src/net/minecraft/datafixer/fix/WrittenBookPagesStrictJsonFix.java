/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ItemNbtFix;
import net.minecraft.datafixer.fix.TextFixes;

public class WrittenBookPagesStrictJsonFix
extends ItemNbtFix {
    public WrittenBookPagesStrictJsonFix(Schema outputSchema) {
        super(outputSchema, "WrittenBookPagesStrictJsonFix", string -> string.equals("minecraft:written_book"));
    }

    @Override
    protected Typed<?> fix(Typed<?> typed2) {
        Type<?> type = this.getInputSchema().getType(TypeReferences.TEXT_COMPONENT);
        Type<?> type2 = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
        OpticFinder<?> opticFinder = type2.findField("tag");
        OpticFinder<?> opticFinder2 = opticFinder.type().findField("pages");
        OpticFinder<?> opticFinder3 = DSL.typeFinder(type);
        return typed2.updateTyped(opticFinder2, typed -> typed.update(opticFinder3, pair -> pair.mapSecond(TextFixes::parseLenientJson)));
    }
}

