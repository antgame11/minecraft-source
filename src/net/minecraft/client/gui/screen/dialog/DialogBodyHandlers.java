/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.dialog;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.dialog.DialogBodyHandler;
import net.minecraft.client.gui.screen.dialog.DialogScreen;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ItemStackWidget;
import net.minecraft.client.gui.widget.NarratedMultilineTextWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.dialog.body.DialogBody;
import net.minecraft.dialog.body.ItemDialogBody;
import net.minecraft.dialog.body.PlainMessageDialogBody;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class DialogBodyHandlers {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<MapCodec<? extends DialogBody>, DialogBodyHandler<?>> DIALOG_BODY_HANDLERS = new HashMap();

    private static <B extends DialogBody> void register(MapCodec<B> dialogBodyCodec, DialogBodyHandler<? super B> dialogBodyHandler) {
        DIALOG_BODY_HANDLERS.put(dialogBodyCodec, dialogBodyHandler);
    }

    @Nullable
    private static <B extends DialogBody> DialogBodyHandler<B> getHandler(B dialogBody) {
        return DIALOG_BODY_HANDLERS.get(dialogBody.getTypeCodec());
    }

    @Nullable
    public static <B extends DialogBody> Widget createWidget(DialogScreen<?> dialogScreen, B dialogBody) {
        DialogBodyHandler<B> lv = DialogBodyHandlers.getHandler(dialogBody);
        if (lv == null) {
            LOGGER.warn("Unrecognized dialog body {}", (Object)dialogBody);
            return null;
        }
        return lv.createWidget(dialogScreen, dialogBody);
    }

    public static void bootstrap() {
        DialogBodyHandlers.register(PlainMessageDialogBody.CODEC, new PlainMessageDialogBodyHandler());
        DialogBodyHandlers.register(ItemDialogBody.CODEC, new ItemDialogBodyHandler());
    }

    static void runActionFromStyle(DialogScreen<?> dialogScreen, @Nullable Style style) {
        ClickEvent lv;
        if (style != null && (lv = style.getClickEvent()) != null) {
            dialogScreen.runAction(Optional.of(lv));
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class PlainMessageDialogBodyHandler
    implements DialogBodyHandler<PlainMessageDialogBody> {
        PlainMessageDialogBodyHandler() {
        }

        @Override
        public Widget createWidget(DialogScreen<?> arg, PlainMessageDialogBody arg2) {
            return new NarratedMultilineTextWidget(arg2.width(), arg2.contents(), arg.getTextRenderer(), false, NarratedMultilineTextWidget.BackgroundRendering.NEVER, 4).setStyleConfig(true, style -> DialogBodyHandlers.runActionFromStyle(arg, style)).setCentered(true);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ItemDialogBodyHandler
    implements DialogBodyHandler<ItemDialogBody> {
        ItemDialogBodyHandler() {
        }

        @Override
        public Widget createWidget(DialogScreen<?> arg, ItemDialogBody arg22) {
            if (arg22.description().isPresent()) {
                PlainMessageDialogBody lv = arg22.description().get();
                DirectionalLayoutWidget lv2 = DirectionalLayoutWidget.horizontal().spacing(2);
                lv2.getMainPositioner().alignVerticalCenter();
                ItemStackWidget lv3 = new ItemStackWidget(MinecraftClient.getInstance(), 0, 0, arg22.width(), arg22.height(), ScreenTexts.EMPTY, arg22.item(), arg22.showDecorations(), arg22.showTooltip());
                lv2.add(lv3);
                lv2.add(new NarratedMultilineTextWidget(lv.width(), lv.contents(), arg.getTextRenderer(), false, NarratedMultilineTextWidget.BackgroundRendering.NEVER, 4).setStyleConfig(true, arg2 -> DialogBodyHandlers.runActionFromStyle(arg, arg2)));
                return lv2;
            }
            return new ItemStackWidget(MinecraftClient.getInstance(), 0, 0, arg22.width(), arg22.height(), arg22.item().getName(), arg22.item(), arg22.showDecorations(), arg22.showTooltip());
        }
    }
}

