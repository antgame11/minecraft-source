/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.AbstractUuidFix;
import org.slf4j.Logger;

public class WorldUuidFix
extends AbstractUuidFix {
    private static final Logger LOGGER = LogUtils.getLogger();

    public WorldUuidFix(Schema outputSchema) {
        super(outputSchema, TypeReferences.LEVEL);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(this.typeReference);
        OpticFinder<?> opticFinder = type.findField("CustomBossEvents");
        OpticFinder<Pair<Either<?, Unit>, Dynamic<?>>> opticFinder2 = DSL.typeFinder(DSL.and(DSL.optional(DSL.field("Name", this.getInputSchema().getTypeRaw(TypeReferences.TEXT_COMPONENT))), DSL.remainderType()));
        return this.fixTypeEverywhereTyped("LevelUUIDFix", type, typed -> typed.update(DSL.remainderFinder(), dynamic -> {
            dynamic = this.fixDragonUuid((Dynamic<?>)dynamic);
            dynamic = this.fixWanderingTraderId((Dynamic<?>)dynamic);
            return dynamic;
        }).updateTyped(opticFinder, typed2 -> typed2.updateTyped(opticFinder2, typed -> typed.update(DSL.remainderFinder(), this::fixCustomBossEvents))));
    }

    private Dynamic<?> fixWanderingTraderId(Dynamic<?> levelDynamic) {
        return WorldUuidFix.updateStringUuid(levelDynamic, "WanderingTraderId", "WanderingTraderId").orElse(levelDynamic);
    }

    private Dynamic<?> fixDragonUuid(Dynamic<?> levelDynamic) {
        return levelDynamic.update("DimensionData", dimensionDataDynamic -> dimensionDataDynamic.updateMapValues(entry -> entry.mapSecond(dimensionDataValueDynamic -> dimensionDataValueDynamic.update("DragonFight", dragonFightDynamic -> WorldUuidFix.updateRegularMostLeast(dragonFightDynamic, "DragonUUID", "Dragon").orElse((Dynamic<?>)dragonFightDynamic)))));
    }

    private Dynamic<?> fixCustomBossEvents(Dynamic<?> levelDynamic) {
        return levelDynamic.update("Players", dynamic22 -> levelDynamic.createList(dynamic22.asStream().map(dynamic -> WorldUuidFix.createArrayFromCompoundUuid(dynamic).orElseGet(() -> {
            LOGGER.warn("CustomBossEvents contains invalid UUIDs.");
            return dynamic;
        }))));
    }
}

