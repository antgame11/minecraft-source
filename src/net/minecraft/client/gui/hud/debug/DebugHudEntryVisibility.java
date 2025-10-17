/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.hud.debug;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.StringIdentifiable;

@Environment(value=EnvType.CLIENT)
public enum DebugHudEntryVisibility implements StringIdentifiable
{
    ALWAYS_ON("alwaysOn"),
    IN_F3("inF3"),
    NEVER("never");

    public static final StringIdentifiable.EnumCodec<DebugHudEntryVisibility> CODEC;
    private final String id;

    private DebugHudEntryVisibility(String id) {
        this.id = id;
    }

    @Override
    public String asString() {
        return this.id;
    }

    static {
        CODEC = StringIdentifiable.createCodec(DebugHudEntryVisibility::values);
    }
}

