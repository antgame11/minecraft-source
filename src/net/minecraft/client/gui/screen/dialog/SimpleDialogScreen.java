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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.dialog.DialogControls;
import net.minecraft.client.gui.screen.dialog.DialogNetworkAccess;
import net.minecraft.client.gui.screen.dialog.DialogScreen;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.dialog.DialogActionButtonData;
import net.minecraft.dialog.type.SimpleDialog;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SimpleDialogScreen<T extends SimpleDialog>
extends DialogScreen<T> {
    public SimpleDialogScreen(@Nullable Screen parent, T dialog, DialogNetworkAccess networkAccess) {
        super(parent, dialog, networkAccess);
    }

    @Override
    protected void initHeaderAndFooter(ThreePartsLayoutWidget arg, DialogControls arg2, T arg3, DialogNetworkAccess arg4) {
        super.initHeaderAndFooter(arg, arg2, arg3, arg4);
        DirectionalLayoutWidget lv = DirectionalLayoutWidget.horizontal().spacing(8);
        for (DialogActionButtonData lv2 : arg3.getButtons()) {
            lv.add(arg2.createButton(lv2).build());
        }
        arg.addFooter(lv);
    }
}

