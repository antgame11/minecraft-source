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
import net.minecraft.dialog.DialogCommonData;
import net.minecraft.dialog.type.ColumnsDialog;
import net.minecraft.util.dynamic.Codecs;

public record MultiActionDialog(DialogCommonData common, List<DialogActionButtonData> actions, Optional<DialogActionButtonData> exitAction, int columns) implements ColumnsDialog
{
    public static final MapCodec<MultiActionDialog> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(DialogCommonData.CODEC.forGetter(MultiActionDialog::common), ((MapCodec)Codecs.nonEmptyList(DialogActionButtonData.CODEC.listOf()).fieldOf("actions")).forGetter(MultiActionDialog::actions), DialogActionButtonData.CODEC.optionalFieldOf("exit_action").forGetter(MultiActionDialog::exitAction), Codecs.POSITIVE_INT.optionalFieldOf("columns", 2).forGetter(MultiActionDialog::columns)).apply((Applicative<MultiActionDialog, ?>)instance, MultiActionDialog::new));

    public MapCodec<MultiActionDialog> getCodec() {
        return CODEC;
    }
}

