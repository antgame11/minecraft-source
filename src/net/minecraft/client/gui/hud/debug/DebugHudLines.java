/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.hud.debug;

import java.util.Collection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public interface DebugHudLines {
    public void addPriorityLine(String var1);

    public void addLine(String var1);

    public void addLinesToSection(Identifier var1, Collection<String> var2);

    public void addLineToSection(Identifier var1, String var2);
}

