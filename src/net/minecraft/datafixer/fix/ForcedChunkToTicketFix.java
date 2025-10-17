/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.datafixer.TypeReferences;

public class ForcedChunkToTicketFix
extends DataFix {
    public ForcedChunkToTicketFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("ForcedChunkToTicketFix", this.getInputSchema().getType(TypeReferences.TICKETS_SAVED_DATA), typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.update("data", dynamic22 -> dynamic22.renameAndFixField("Forced", "tickets", dynamic2 -> dynamic2.createList(dynamic2.asLongStream().mapToObj(l -> dynamic.emptyMap().set("type", dynamic.createString("minecraft:forced")).set("level", dynamic.createInt(31)).set("ticks_left", dynamic.createLong(0L)).set("chunk_pos", dynamic.createLong(l))))))));
    }
}

