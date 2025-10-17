/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.dialog.type;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.dialog.DialogActionButtonData;
import net.minecraft.dialog.DialogCommonData;
import net.minecraft.dialog.type.ColumnsDialog;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.dynamic.Codecs;

public record DialogListDialog(DialogCommonData common, RegistryEntryList<Dialog> dialogs, Optional<DialogActionButtonData> exitAction, int columns, int buttonWidth) implements ColumnsDialog
{
    public static final MapCodec<DialogListDialog> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(DialogCommonData.CODEC.forGetter(DialogListDialog::common), ((MapCodec)Dialog.ENTRY_LIST_CODEC.fieldOf("dialogs")).forGetter(DialogListDialog::dialogs), DialogActionButtonData.CODEC.optionalFieldOf("exit_action").forGetter(DialogListDialog::exitAction), Codecs.POSITIVE_INT.optionalFieldOf("columns", 2).forGetter(DialogListDialog::columns), WIDTH_CODEC.optionalFieldOf("button_width", 150).forGetter(DialogListDialog::buttonWidth)).apply((Applicative<DialogListDialog, ?>)instance, DialogListDialog::new));

    public MapCodec<DialogListDialog> getCodec() {
        return CODEC;
    }
}

