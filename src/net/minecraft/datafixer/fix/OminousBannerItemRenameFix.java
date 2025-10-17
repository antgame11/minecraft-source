/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.fix.ItemNbtFix;
import net.minecraft.util.Util;

public class OminousBannerItemRenameFix
extends ItemNbtFix {
    public OminousBannerItemRenameFix(Schema outputSchema) {
        super(outputSchema, "OminousBannerRenameFix", itemId -> itemId.equals("minecraft:white_banner"));
    }

    private <T> Dynamic<T> fixBannerNbt(Dynamic<T> nbt) {
        return nbt.update("display", display -> display.update("Name", name -> {
            Optional<String> optional = name.asString().result();
            if (optional.isPresent()) {
                return name.createString(optional.get().replace("\"translate\":\"block.minecraft.illager_banner\"", "\"translate\":\"block.minecraft.ominous_banner\""));
            }
            return name;
        }));
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return Util.apply(typed, typed.getType(), this::fixBannerNbt);
    }
}

