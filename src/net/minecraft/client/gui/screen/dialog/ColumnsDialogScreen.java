/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.dialog;

import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.dialog.DialogControls;
import net.minecraft.client.gui.screen.dialog.DialogNetworkAccess;
import net.minecraft.client.gui.screen.dialog.DialogScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.dialog.DialogActionButtonData;
import net.minecraft.dialog.type.ColumnsDialog;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class ColumnsDialogScreen<T extends ColumnsDialog>
extends DialogScreen<T> {
    public static final int field_61004 = 5;

    public ColumnsDialogScreen(@Nullable Screen parent, T dialog, DialogNetworkAccess networkAccess) {
        super(parent, dialog, networkAccess);
    }

    @Override
    protected void initBody(DirectionalLayoutWidget arg, DialogControls arg2, T arg3, DialogNetworkAccess arg4) {
        super.initBody(arg, arg2, arg3, arg4);
        List<ButtonWidget> list = this.streamActionButtonData(arg3, arg4).map(actionButtonData -> arg2.createButton((DialogActionButtonData)actionButtonData).build()).toList();
        arg.add(ColumnsDialogScreen.createGridWidget(list, arg3.columns()));
    }

    protected abstract Stream<DialogActionButtonData> streamActionButtonData(T var1, DialogNetworkAccess var2);

    @Override
    protected void initHeaderAndFooter(ThreePartsLayoutWidget arg, DialogControls arg2, T arg3, DialogNetworkAccess arg4) {
        super.initHeaderAndFooter(arg, arg2, arg3, arg4);
        arg3.exitAction().ifPresentOrElse(actionButtonData -> arg.addFooter(arg2.createButton((DialogActionButtonData)actionButtonData).build()), () -> arg.setFooterHeight(5));
    }
}

