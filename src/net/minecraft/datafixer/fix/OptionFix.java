/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.datafixer.TypeReferences;

public class OptionFix
extends DataFix {
    private final String name;
    private final String oldName;
    private final String newName;

    public OptionFix(Schema outputSchema, boolean changesType, String name, String oldName, String newName) {
        super(outputSchema, changesType);
        this.name = name;
        this.oldName = oldName;
        this.newName = newName;
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(TypeReferences.OPTIONS), optionsTyped -> optionsTyped.update(DSL.remainderFinder(), optionsDynamic -> optionsDynamic.renameField(this.oldName, this.newName)));
    }
}

