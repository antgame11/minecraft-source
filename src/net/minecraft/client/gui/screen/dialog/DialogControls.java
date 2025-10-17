/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.dialog;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.dialog.DialogScreen;
import net.minecraft.client.gui.screen.dialog.InputControlHandlers;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.dialog.DialogActionButtonData;
import net.minecraft.dialog.DialogButtonData;
import net.minecraft.dialog.action.DialogAction;
import net.minecraft.dialog.type.DialogInput;
import net.minecraft.text.ClickEvent;

@Environment(value=EnvType.CLIENT)
public class DialogControls {
    public static final Supplier<Optional<ClickEvent>> EMPTY_ACTION_CLICK_EVENT = Optional::empty;
    private final DialogScreen<?> screen;
    private final Map<String, DialogAction.ValueGetter> valueGetters = new HashMap<String, DialogAction.ValueGetter>();

    public DialogControls(DialogScreen<?> screen) {
        this.screen = screen;
    }

    public void addInput(DialogInput input, Consumer<Widget> widgetConsumer) {
        String string = input.key();
        InputControlHandlers.addControl(input.control(), this.screen, (widget, valueGetter) -> {
            this.valueGetters.put(string, valueGetter);
            widgetConsumer.accept(widget);
        });
    }

    private static ButtonWidget.Builder createButton(DialogButtonData data, ButtonWidget.PressAction pressAction) {
        ButtonWidget.Builder lv = ButtonWidget.builder(data.label(), pressAction);
        lv.width(data.width());
        if (data.tooltip().isPresent()) {
            lv = lv.tooltip(Tooltip.of(data.tooltip().get()));
        }
        return lv;
    }

    public Supplier<Optional<ClickEvent>> createClickEvent(Optional<DialogAction> action) {
        if (action.isPresent()) {
            DialogAction lv = action.get();
            return () -> lv.createClickEvent(this.valueGetters);
        }
        return EMPTY_ACTION_CLICK_EVENT;
    }

    public ButtonWidget.Builder createButton(DialogActionButtonData actionButtonData) {
        Supplier<Optional<ClickEvent>> supplier = this.createClickEvent(actionButtonData.action());
        return DialogControls.createButton(actionButtonData.data(), button -> this.screen.runAction((Optional)supplier.get()));
    }
}

