/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.dialog.type;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.dialog.DialogActionButtonData;
import net.minecraft.dialog.type.Dialog;

public interface SimpleDialog
extends Dialog {
    public MapCodec<? extends SimpleDialog> getCodec();

    public List<DialogActionButtonData> getButtons();
}

