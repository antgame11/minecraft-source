/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.datafixer.fix.ComponentFix;
import org.jetbrains.annotations.Nullable;

public class InvalidLockComponentPredicateFix
extends ComponentFix {
    private static final Optional<String> DOUBLE_QUOTES = Optional.of("\"\"");

    public InvalidLockComponentPredicateFix(Schema outputSchema) {
        super(outputSchema, "InvalidLockComponentPredicateFix", "minecraft:lock");
    }

    @Override
    @Nullable
    protected <T> Dynamic<T> fixComponent(Dynamic<T> dynamic) {
        return InvalidLockComponentPredicateFix.validateLock(dynamic);
    }

    @Nullable
    public static <T> Dynamic<T> validateLock(Dynamic<T> dynamic) {
        return InvalidLockComponentPredicateFix.isLockInvalid(dynamic) ? null : dynamic;
    }

    private static <T> boolean isLockInvalid(Dynamic<T> dynamic) {
        return InvalidLockComponentPredicateFix.hasMatchingKey(dynamic, "components", componentsDynamic -> InvalidLockComponentPredicateFix.hasMatchingKey(componentsDynamic, "minecraft:custom_name", customNameDynamic -> customNameDynamic.asString().result().equals(DOUBLE_QUOTES)));
    }

    private static <T> boolean hasMatchingKey(Dynamic<T> dynamic, String key, Predicate<Dynamic<T>> predicate) {
        Optional<Map<Dynamic<T>, Dynamic<T>>> optional = dynamic.getMapValues().result();
        if (optional.isEmpty() || optional.get().size() != 1) {
            return false;
        }
        return dynamic.get(key).result().filter(predicate).isPresent();
    }
}

