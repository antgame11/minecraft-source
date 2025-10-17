/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import net.minecraft.datafixer.fix.BlockPropertyRenameFix;

public class JigsawRotationFix
extends BlockPropertyRenameFix {
    private static final Map<String, String> ORIENTATION_UPDATES = ImmutableMap.builder().put("down", "down_south").put("up", "up_north").put("north", "north_up").put("south", "south_up").put("west", "west_up").put("east", "east_up").build();

    public JigsawRotationFix(Schema outputSchema) {
        super(outputSchema, "jigsaw_rotation_fix");
    }

    @Override
    protected boolean shouldFix(String id) {
        return id.equals("minecraft:jigsaw");
    }

    @Override
    protected <T> Dynamic<T> fix(String id, Dynamic<T> properties) {
        String string2 = properties.get("facing").asString("north");
        return properties.remove("facing").set("orientation", properties.createString(ORIENTATION_UPDATES.getOrDefault(string2, string2)));
    }
}

