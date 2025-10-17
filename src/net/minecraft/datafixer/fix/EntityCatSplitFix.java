/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import net.minecraft.datafixer.fix.EntitySimpleTransformFix;

public class EntityCatSplitFix
extends EntitySimpleTransformFix {
    public EntityCatSplitFix(Schema schema, boolean bl) {
        super("EntityCatSplitFix", schema, bl);
    }

    @Override
    protected Pair<String, Dynamic<?>> transform(String choice, Dynamic<?> entityDynamic) {
        if (Objects.equals("minecraft:ocelot", choice)) {
            int i = entityDynamic.get("CatType").asInt(0);
            if (i == 0) {
                String string2 = entityDynamic.get("Owner").asString("");
                String string3 = entityDynamic.get("OwnerUUID").asString("");
                if (string2.length() > 0 || string3.length() > 0) {
                    entityDynamic.set("Trusting", entityDynamic.createBoolean(true));
                }
            } else if (i > 0 && i < 4) {
                entityDynamic = entityDynamic.set("CatType", entityDynamic.createInt(i));
                entityDynamic = entityDynamic.set("OwnerUUID", entityDynamic.createString(entityDynamic.get("OwnerUUID").asString("")));
                return Pair.of("minecraft:cat", entityDynamic);
            }
        }
        return Pair.of(choice, entityDynamic);
    }
}

