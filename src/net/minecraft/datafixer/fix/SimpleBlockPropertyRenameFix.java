/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.UnaryOperator;
import net.minecraft.datafixer.fix.BlockPropertyRenameFix;

public class SimpleBlockPropertyRenameFix
extends BlockPropertyRenameFix {
    private final String targetId;
    private final String oldPropertyName;
    private final String newPropertyName;
    private final UnaryOperator<String> valueConverter;

    public SimpleBlockPropertyRenameFix(Schema outputSchema, String name, String targetId, String oldPropertyName, String newPropertyName, UnaryOperator<String> valueConverter) {
        super(outputSchema, name);
        this.targetId = targetId;
        this.oldPropertyName = oldPropertyName;
        this.newPropertyName = newPropertyName;
        this.valueConverter = valueConverter;
    }

    @Override
    protected boolean shouldFix(String id) {
        return id.equals(this.targetId);
    }

    @Override
    protected <T> Dynamic<T> fix(String id, Dynamic<T> properties) {
        return properties.renameAndFixField(this.oldPropertyName, this.newPropertyName, value -> value.createString((String)this.valueConverter.apply(value.asString(""))));
    }
}

