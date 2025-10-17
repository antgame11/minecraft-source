/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.dialog.type;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.dialog.DialogActionButtonData;
import net.minecraft.dialog.DialogButtonData;
import net.minecraft.dialog.DialogCommonData;
import net.minecraft.dialog.action.DialogAction;
import net.minecraft.dialog.type.SimpleDialog;
import net.minecraft.screen.ScreenTexts;

public record NoticeDialog(DialogCommonData common, DialogActionButtonData action) implements SimpleDialog
{
    public static final DialogActionButtonData OK_BUTTON = new DialogActionButtonData(new DialogButtonData(ScreenTexts.OK, 150), Optional.empty());
    public static final MapCodec<NoticeDialog> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(DialogCommonData.CODEC.forGetter(NoticeDialog::common), DialogActionButtonData.CODEC.optionalFieldOf("action", OK_BUTTON).forGetter(NoticeDialog::action)).apply((Applicative<NoticeDialog, ?>)instance, NoticeDialog::new));

    public MapCodec<NoticeDialog> getCodec() {
        return CODEC;
    }

    @Override
    public Optional<DialogAction> getCancelAction() {
        return this.action.action();
    }

    @Override
    public List<DialogActionButtonData> getButtons() {
        return List.of(this.action);
    }
}

