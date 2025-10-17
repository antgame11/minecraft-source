/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.dialog;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.dialog.DialogScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.dialog.body.DialogBody;

@Environment(value=EnvType.CLIENT)
public interface DialogBodyHandler<T extends DialogBody> {
    public Widget createWidget(DialogScreen<?> var1, T var2);
}

