/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;

public class BlockEntityFurnaceBurnTimeFix
extends ChoiceFix {
    public BlockEntityFurnaceBurnTimeFix(Schema outputSchema, String blockEntityId) {
        super(outputSchema, false, "BlockEntityFurnaceBurnTimeFix" + blockEntityId, TypeReferences.BLOCK_ENTITY, blockEntityId);
    }

    public Dynamic<?> fix(Dynamic<?> dynamic) {
        dynamic = dynamic.renameField("CookTime", "cooking_time_spent");
        dynamic = dynamic.renameField("CookTimeTotal", "cooking_total_time");
        dynamic = dynamic.renameField("BurnTime", "lit_time_remaining");
        dynamic = dynamic.setFieldIfPresent("lit_total_time", dynamic.get("lit_time_remaining").result());
        return dynamic;
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), this::fix);
    }
}

