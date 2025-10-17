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

public class ItemCustomNameToComponentFix
extends DataFix {
    public ItemCustomNameToComponentFix(Schema schema) {
        super(schema, false);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
        Type<?> type2 = this.getInputSchema().getType(TypeReferences.TEXT_COMPONENT);
        OpticFinder<?> opticFinder = type.findField("tag");
        OpticFinder<?> opticFinder2 = opticFinder.type().findField("display");
        OpticFinder<?> opticFinder3 = opticFinder2.type().findField("Name");
        OpticFinder<?> opticFinder4 = DSL.typeFinder(type2);
        return this.fixTypeEverywhereTyped("ItemCustomNameToComponentFix", type, typed2 -> typed2.updateTyped(opticFinder, typed -> typed.updateTyped(opticFinder2, typed2 -> typed2.updateTyped(opticFinder3, typed -> typed.update(opticFinder4, pair -> pair.mapSecond(TextFixes::text))))));
    }
}

