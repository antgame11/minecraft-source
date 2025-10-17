/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;

public class TextComponentStringyFlagsFix
extends DataFix {
    public TextComponentStringyFlagsFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.TEXT_COMPONENT);
        return this.fixTypeEverywhere("TextComponentStringyFlagsFix", type, dynamicOps -> pair -> pair.mapSecond(either -> either.mapRight(pair -> pair.mapSecond(pair2 -> pair2.mapSecond(pair -> pair.mapSecond(dynamic -> dynamic.update("bold", TextComponentStringyFlagsFix::method_66136).update("italic", TextComponentStringyFlagsFix::method_66136).update("underlined", TextComponentStringyFlagsFix::method_66136).update("strikethrough", TextComponentStringyFlagsFix::method_66136).update("obfuscated", TextComponentStringyFlagsFix::method_66136)))))));
    }

    private static <T> Dynamic<T> method_66136(Dynamic<T> dynamic) {
        Optional<String> optional = dynamic.asString().result();
        if (optional.isPresent()) {
            return dynamic.createBoolean(Boolean.parseBoolean(optional.get()));
        }
        return dynamic;
    }
}

