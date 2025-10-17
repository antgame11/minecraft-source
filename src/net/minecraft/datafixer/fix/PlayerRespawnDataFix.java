/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.datafixer.TypeReferences;

public class PlayerRespawnDataFix
extends DataFix {
    public PlayerRespawnDataFix(Schema schema) {
        super(schema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("PlayerRespawnDataFix", this.getInputSchema().getType(TypeReferences.PLAYER), typed -> typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.update("respawn", dynamic -> dynamic.set("dimension", dynamic.createString(dynamic.get("dimension").asString("minecraft:overworld"))).set("yaw", dynamic.createFloat(dynamic.get("angle").asFloat(0.0f))).set("pitch", dynamic.createFloat(0.0f)).remove("angle"))));
    }
}

