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
public enum DebugProfileType implements StringIdentifiable
{
    DEFAULT("default", "debug.options.profile.default"),
    PERFORMANCE("performance", "debug.options.profile.performance");

    public static final StringIdentifiable.EnumCodec<DebugProfileType> CODEC;
    private final String id;
    private final String translationKey;

    private DebugProfileType(String id, String translationKey) {
        this.id = id;
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return this.translationKey;
    }

    @Override
    public String asString() {
        return this.id;
    }

    static {
        CODEC = StringIdentifiable.createCodec(DebugProfileType::values);
    }
}

