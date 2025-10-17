/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import net.minecraft.datafixer.FixUtil;
import net.minecraft.datafixer.TypeReferences;

public class MapBannerBlockPosFormatFix
extends DataFix {
    public MapBannerBlockPosFormatFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.SAVED_DATA_MAP_DATA);
        OpticFinder<?> opticFinder = type.findField("data");
        OpticFinder<?> opticFinder2 = opticFinder.type().findField("banners");
        OpticFinder opticFinder3 = DSL.typeFinder(((List.ListType)opticFinder2.type()).getElement());
        return this.fixTypeEverywhereTyped("MapBannerBlockPosFormatFix", type, typed2 -> typed2.updateTyped(opticFinder, typed -> typed.updateTyped(opticFinder2, typed2 -> typed2.updateTyped(opticFinder3, typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.update("Pos", FixUtil::fixBlockPos))))));
    }
}

