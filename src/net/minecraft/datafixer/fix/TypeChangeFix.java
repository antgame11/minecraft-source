/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.datafixer.FixUtil;
import net.minecraft.datafixer.fix.ChoiceFix;

public class TypeChangeFix
extends ChoiceFix {
    public TypeChangeFix(Schema outputSchema, String name, DSL.TypeReference type, String choiceName) {
        super(outputSchema, true, name, type, choiceName);
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        Type<?> type = this.getOutputSchema().getChoiceType(this.type, this.choiceName);
        return FixUtil.withType(type, inputTyped);
    }
}

