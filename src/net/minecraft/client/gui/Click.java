/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.client.input.MouseInput;

@Environment(value=EnvType.CLIENT)
public record Click(double x, double y, MouseInput buttonInfo) implements AbstractInput
{
    @Override
    public int getKeycode() {
        return this.button();
    }

    public int button() {
        return this.buttonInfo().button();
    }

    @Override
    public int modifiers() {
        return this.buttonInfo().modifiers();
    }
}

