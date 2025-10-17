/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.input;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.input.AbstractInput;

@Environment(value=EnvType.CLIENT)
public record KeyInput(int key, int scancode, int modifiers) implements AbstractInput
{
    @Override
    public int getKeycode() {
        return this.key;
    }
}

