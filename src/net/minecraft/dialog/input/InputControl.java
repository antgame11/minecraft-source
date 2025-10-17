/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.dialog.input;

import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registries;

public interface InputControl {
    public static final MapCodec<InputControl> CODEC = Registries.INPUT_CONTROL_TYPE.getCodec().dispatchMap(InputControl::getCodec, mapCodec -> mapCodec);

    public MapCodec<? extends InputControl> getCodec();
}

