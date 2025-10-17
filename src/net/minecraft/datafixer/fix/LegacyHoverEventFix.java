/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.google.gson.JsonElement;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import java.lang.invoke.CallSite;
import java.util.Map;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;

public class LegacyHoverEventFix
extends DataFix {
    public LegacyHoverEventFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.TEXT_COMPONENT).findFieldType("hoverEvent");
        return this.method_66084(this.getInputSchema().getTypeRaw(TypeReferences.TEXT_COMPONENT), type);
    }

    private <C, H extends Pair<String, ?>> TypeRewriteRule method_66084(Type<C> type, Type<H> type2) {
        Type<Pair<String, Either<Either<String, C>, Pair<Either<C, Unit>, Pair<Either<C, Unit>, Pair<Either<H, Unit>, Dynamic<?>>>>>>> type3 = DSL.named(TypeReferences.TEXT_COMPONENT.typeName(), DSL.or(DSL.or(DSL.string(), DSL.list(type)), DSL.and(DSL.optional(DSL.field("extra", DSL.list(type))), DSL.optional(DSL.field("separator", type)), DSL.optional(DSL.field("hoverEvent", type2)), DSL.remainderType())));
        if (!type3.equals(this.getInputSchema().getType(TypeReferences.TEXT_COMPONENT))) {
            throw new IllegalStateException("Text component type did not match, expected " + String.valueOf(type3) + " but got " + String.valueOf(this.getInputSchema().getType(TypeReferences.TEXT_COMPONENT)));
        }
        return this.fixTypeEverywhere("LegacyHoverEventFix", type3, dynamicOps -> pair -> pair.mapSecond(either -> either.mapRight(pair -> pair.mapSecond(pair2 -> pair2.mapSecond(pair -> {
            Dynamic dynamic = (Dynamic)pair.getSecond();
            Optional optional = dynamic.get("hoverEvent").result();
            if (optional.isEmpty()) {
                return pair;
            }
            Optional optional2 = optional.get().get("value").result();
            if (optional2.isEmpty()) {
                return pair;
            }
            String string = ((Either)pair.getFirst()).left().map(Pair::getFirst).orElse("");
            Pair pair2 = (Pair)this.method_66089(type2, string, optional.get());
            return pair.mapFirst(either -> Either.left(pair2));
        })))));
    }

    private <H> H method_66089(Type<H> type, String string, Dynamic<?> dynamic) {
        if ("show_text".equals(string)) {
            return LegacyHoverEventFix.method_66087(type, dynamic);
        }
        return LegacyHoverEventFix.method_66092(type, dynamic);
    }

    private static <H> H method_66087(Type<H> type, Dynamic<?> dynamic) {
        Dynamic<?> dynamic2 = dynamic.renameField("value", "contents");
        return Util.readTyped(type, dynamic2).getValue();
    }

    private static <H> H method_66092(Type<H> type, Dynamic<?> dynamic) {
        JsonElement jsonElement = dynamic.convert(JsonOps.INSTANCE).getValue();
        Dynamic<Map<String, Map<String, CallSite>>> dynamic2 = new Dynamic<Map<String, Map<String, CallSite>>>(JavaOps.INSTANCE, Map.of("action", "show_text", "contents", Map.of("text", "Legacy hoverEvent: " + JsonHelper.toSortedString(jsonElement))));
        return Util.readTyped(type, dynamic2).getValue();
    }
}

