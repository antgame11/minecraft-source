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

public class EntityArmorStandSilentFix
extends ChoiceFix {
    public EntityArmorStandSilentFix(Schema schema, boolean bl) {
        super(schema, bl, "EntityArmorStandSilentFix", TypeReferences.ENTITY, "ArmorStand");
    }

    public Dynamic<?> fixSilent(Dynamic<?> armorStandDynamic) {
        if (armorStandDynamic.get("Silent").asBoolean(false) && !armorStandDynamic.get("Marker").asBoolean(false)) {
            return armorStandDynamic.remove("Silent");
        }
        return armorStandDynamic;
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), this::fixSilent);
    }
}

