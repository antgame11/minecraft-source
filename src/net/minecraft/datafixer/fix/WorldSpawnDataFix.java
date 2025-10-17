/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.stream.IntStream;
import net.minecraft.datafixer.TypeReferences;

public class WorldSpawnDataFix
extends DataFix {
    public WorldSpawnDataFix(Schema schema) {
        super(schema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("WorldSpawnDataFix", this.getInputSchema().getType(TypeReferences.LEVEL), typed -> typed.update(DSL.remainderFinder(), dynamic -> {
            int i = dynamic.get("SpawnX").asInt(0);
            int j = dynamic.get("SpawnY").asInt(0);
            int k = dynamic.get("SpawnZ").asInt(0);
            float f = dynamic.get("SpawnAngle").asFloat(0.0f);
            Dynamic dynamic2 = dynamic.emptyMap().set("dimension", dynamic.createString("minecraft:overworld")).set("pos", dynamic.createIntList(IntStream.of(i, j, k))).set("yaw", dynamic.createFloat(f)).set("pitch", dynamic.createFloat(0.0f));
            dynamic = dynamic.remove("SpawnX").remove("SpawnY").remove("SpawnZ").remove("SpawnAngle");
            return dynamic.set("spawn", dynamic2);
        }));
    }
}

