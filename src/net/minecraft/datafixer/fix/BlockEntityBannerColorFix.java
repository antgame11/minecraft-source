/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;

public class BlockEntityBannerColorFix
extends ChoiceFix {
    public BlockEntityBannerColorFix(Schema schema, boolean bl) {
        super(schema, bl, "BlockEntityBannerColorFix", TypeReferences.BLOCK_ENTITY, "minecraft:banner");
    }

    public Dynamic<?> fixBannerColor(Dynamic<?> bannerDynamic) {
        bannerDynamic = bannerDynamic.update("Base", baseDynamic -> baseDynamic.createInt(15 - baseDynamic.asInt(0)));
        bannerDynamic = bannerDynamic.update("Patterns", patternsDynamic -> DataFixUtils.orElse(patternsDynamic.asStreamOpt().map(stream -> stream.map(patternDynamic -> patternDynamic.update("Color", colorDynamic -> colorDynamic.createInt(15 - colorDynamic.asInt(0))))).map(patternsDynamic::createList).result(), patternsDynamic));
        return bannerDynamic;
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), this::fixBannerColor);
    }
}

