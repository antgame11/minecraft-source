/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.Predicate;
import net.minecraft.datafixer.fix.ItemNbtFix;

public abstract class SimpleItemNbtFix
extends ItemNbtFix {
    public SimpleItemNbtFix(Schema schema, String string, Predicate<String> predicate) {
        super(schema, string, predicate);
    }

    protected abstract <T> Dynamic<T> fixNbt(Dynamic<T> var1);

    @Override
    protected final Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), this::fixNbt);
    }
}

