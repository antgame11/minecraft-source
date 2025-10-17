/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.Streams;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.TextFixes;
import net.minecraft.datafixer.fix.UpdateSignTextFormatFix;
import net.minecraft.util.Util;

public class DropInvalidSignDatafixDataFix
extends DataFix {
    private final String field_55637;

    public DropInvalidSignDatafixDataFix(Schema outputSchema, String name) {
        super(outputSchema, false);
        this.field_55637 = name;
    }

    private <T> Dynamic<T> dropInvalidDatafixData(Dynamic<T> dynamic) {
        dynamic = dynamic.update("front_text", DropInvalidSignDatafixDataFix::dropInvalidDatafixDataOnSide);
        dynamic = dynamic.update("back_text", DropInvalidSignDatafixDataFix::dropInvalidDatafixDataOnSide);
        for (String string : UpdateSignTextFormatFix.field_55629) {
            dynamic = dynamic.remove(string);
        }
        return dynamic;
    }

    private static <T> Dynamic<T> dropInvalidDatafixDataOnSide(Dynamic<T> textData) {
        Optional<Stream<Dynamic<T>>> optional = textData.get("filtered_messages").asStreamOpt().result();
        if (optional.isEmpty()) {
            return textData;
        }
        Dynamic dynamic2 = TextFixes.empty(textData.getOps());
        List<Dynamic> list = textData.get("messages").asStreamOpt().result().orElse(Stream.of(new Dynamic[0])).toList();
        List<Dynamic> list2 = Streams.mapWithIndex(optional.get(), (message, index) -> {
            Dynamic dynamic3 = index < (long)list.size() ? (Dynamic)list.get((int)index) : dynamic2;
            return message.equals(dynamic2) ? dynamic3 : message;
        }).toList();
        if (list2.equals(list)) {
            return textData.remove("filtered_messages");
        }
        return textData.set("filtered_messages", textData.createList(list2.stream()));
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.BLOCK_ENTITY);
        Type<?> type2 = this.getInputSchema().getChoiceType(TypeReferences.BLOCK_ENTITY, this.field_55637);
        OpticFinder<?> opticFinder = DSL.namedChoice(this.field_55637, type2);
        return this.fixTypeEverywhereTyped("DropInvalidSignDataFix for " + this.field_55637, type, typed2 -> typed2.updateTyped(opticFinder, type2, typed -> {
            boolean bl = typed.get(DSL.remainderFinder()).get("_filtered_correct").asBoolean(false);
            if (bl) {
                return typed.update(DSL.remainderFinder(), dynamic -> dynamic.remove("_filtered_correct"));
            }
            return Util.apply(typed, type2, this::dropInvalidDatafixData);
        }));
    }
}

