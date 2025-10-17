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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.dialog.InputControlHandler;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.LayoutWidgets;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.dialog.action.DialogAction;
import net.minecraft.dialog.input.BooleanInputControl;
import net.minecraft.dialog.input.InputControl;
import net.minecraft.dialog.input.NumberRangeInputControl;
import net.minecraft.dialog.input.SingleOptionInputControl;
import net.minecraft.dialog.input.TextInputControl;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class InputControlHandlers {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<MapCodec<? extends InputControl>, InputControlHandler<?>> INPUT_CONTROL_HANDLERS = new HashMap();

    private static <T extends InputControl> void register(MapCodec<T> inputControlCodec, InputControlHandler<? super T> inputControlHandler) {
        INPUT_CONTROL_HANDLERS.put(inputControlCodec, inputControlHandler);
    }

    @Nullable
    private static <T extends InputControl> InputControlHandler<T> getHandler(T inputControl) {
        return INPUT_CONTROL_HANDLERS.get(inputControl.getCodec());
    }

    public static <T extends InputControl> void addControl(T inputControl, Screen screen, InputControlHandler.Output output) {
        InputControlHandler<T> lv = InputControlHandlers.getHandler(inputControl);
        if (lv == null) {
            LOGGER.warn("Unrecognized input control {}", (Object)inputControl);
            return;
        }
        lv.addControl(inputControl, screen, output);
    }

    public static void bootstrap() {
        InputControlHandlers.register(TextInputControl.CODEC, new TextInputControlHandler());
        InputControlHandlers.register(SingleOptionInputControl.CODEC, new SimpleOptionInputControlHandler());
        InputControlHandlers.register(BooleanInputControl.CODEC, new BooleanInputControlHandler());
        InputControlHandlers.register(NumberRangeInputControl.CODEC, new NumberRangeInputControlHandler());
    }

    @Environment(value=EnvType.CLIENT)
    static class TextInputControlHandler
    implements InputControlHandler<TextInputControl> {
        TextInputControlHandler() {
        }

        @Override
        public void addControl(TextInputControl arg, Screen arg2, InputControlHandler.Output arg3) {
            Supplier<String> supplier;
            ClickableWidget lv4;
            TextRenderer lv = arg2.getTextRenderer();
            if (arg.multiline().isPresent()) {
                TextInputControl.Multiline lv2 = arg.multiline().get();
                int i = lv2.height().orElseGet(() -> {
                    int i = lv2.maxLines().orElse(4);
                    return Math.min(arg2.fontHeight * i + 8, 512);
                });
                EditBoxWidget lv3 = EditBoxWidget.builder().build(lv, arg.width(), i, ScreenTexts.EMPTY);
                lv3.setMaxLength(arg.maxLength());
                lv2.maxLines().ifPresent(lv3::setMaxLines);
                lv3.setText(arg.initial());
                lv4 = lv3;
                supplier = lv3::getText;
            } else {
                TextFieldWidget lv5 = new TextFieldWidget(lv, arg.width(), 20, arg.label());
                lv5.setMaxLength(arg.maxLength());
                lv5.setText(arg.initial());
                lv4 = lv5;
                supplier = lv5::getText;
            }
            TextFieldWidget lv6 = arg.labelVisible() ? LayoutWidgets.createLabeledWidget(lv, lv4, arg.label()) : lv4;
            arg3.accept(lv6, new DialogAction.ValueGetter(){

                @Override
                public String get() {
                    return NbtString.escapeUnquoted((String)supplier.get());
                }

                @Override
                public NbtElement getAsNbt() {
                    return NbtString.of((String)supplier.get());
                }
            });
        }

        @Override
        public /* synthetic */ void addControl(InputControl arg, Screen arg2, InputControlHandler.Output arg3) {
            this.addControl((TextInputControl)arg, arg2, arg3);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class SimpleOptionInputControlHandler
    implements InputControlHandler<SingleOptionInputControl> {
        SimpleOptionInputControlHandler() {
        }

        @Override
        public void addControl(SingleOptionInputControl arg, Screen arg2, InputControlHandler.Output arg3) {
            CyclingButtonWidget.Builder<SingleOptionInputControl.Entry> lv = CyclingButtonWidget.builder(SingleOptionInputControl.Entry::getDisplay).values((Collection<SingleOptionInputControl.Entry>)arg.entries()).optionTextOmitted(!arg.labelVisible());
            Optional<SingleOptionInputControl.Entry> optional = arg.getInitialEntry();
            if (optional.isPresent()) {
                lv = lv.initially(optional.get());
            }
            CyclingButtonWidget<SingleOptionInputControl.Entry> lv2 = lv.build(0, 0, arg.width(), 20, arg.label());
            arg3.accept(lv2, DialogAction.ValueGetter.of(() -> ((SingleOptionInputControl.Entry)lv2.getValue()).id()));
        }

        @Override
        public /* synthetic */ void addControl(InputControl arg, Screen arg2, InputControlHandler.Output arg3) {
            this.addControl((SingleOptionInputControl)arg, arg2, arg3);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class BooleanInputControlHandler
    implements InputControlHandler<BooleanInputControl> {
        BooleanInputControlHandler() {
        }

        @Override
        public void addControl(final BooleanInputControl arg, Screen arg2, InputControlHandler.Output arg3) {
            TextRenderer lv = arg2.getTextRenderer();
            final CheckboxWidget lv2 = CheckboxWidget.builder(arg.label(), lv).checked(arg.initial()).build();
            arg3.accept(lv2, new DialogAction.ValueGetter(){

                @Override
                public String get() {
                    return lv2.isChecked() ? arg.onTrue() : arg.onFalse();
                }

                @Override
                public NbtElement getAsNbt() {
                    return NbtByte.of(lv2.isChecked());
                }
            });
        }

        @Override
        public /* synthetic */ void addControl(InputControl arg, Screen arg2, InputControlHandler.Output arg3) {
            this.addControl((BooleanInputControl)arg, arg2, arg3);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class NumberRangeInputControlHandler
    implements InputControlHandler<NumberRangeInputControl> {
        NumberRangeInputControlHandler() {
        }

        @Override
        public void addControl(NumberRangeInputControl arg, Screen arg2, InputControlHandler.Output arg3) {
            float f = arg.rangeInfo().getInitialSliderProgress();
            final RangeSliderWidget lv = new RangeSliderWidget(arg, f);
            arg3.accept(lv, new DialogAction.ValueGetter(){

                @Override
                public String get() {
                    return lv.getLabel();
                }

                @Override
                public NbtElement getAsNbt() {
                    return NbtFloat.of(lv.getActualValue());
                }
            });
        }

        @Override
        public /* synthetic */ void addControl(InputControl arg, Screen arg2, InputControlHandler.Output arg3) {
            this.addControl((NumberRangeInputControl)arg, arg2, arg3);
        }

        @Environment(value=EnvType.CLIENT)
        static class RangeSliderWidget
        extends SliderWidget {
            private final NumberRangeInputControl inputControl;

            RangeSliderWidget(NumberRangeInputControl inputControl, double value) {
                super(0, 0, inputControl.width(), 20, RangeSliderWidget.getFormattedLabel(inputControl, value), value);
                this.inputControl = inputControl;
            }

            @Override
            protected void updateMessage() {
                this.setMessage(RangeSliderWidget.getFormattedLabel(this.inputControl, this.value));
            }

            @Override
            protected void applyValue() {
            }

            public String getLabel() {
                return RangeSliderWidget.getLabel(this.inputControl, this.value);
            }

            public float getActualValue() {
                return RangeSliderWidget.getActualValue(this.inputControl, this.value);
            }

            private static float getActualValue(NumberRangeInputControl inputControl, double sliderProgress) {
                return inputControl.rangeInfo().sliderProgressToValue((float)sliderProgress);
            }

            private static String getLabel(NumberRangeInputControl inputControl, double sliderProgress) {
                return RangeSliderWidget.valueToString(RangeSliderWidget.getActualValue(inputControl, sliderProgress));
            }

            private static Text getFormattedLabel(NumberRangeInputControl inputControl, double sliderProgress) {
                return inputControl.getFormattedLabel(RangeSliderWidget.getLabel(inputControl, sliderProgress));
            }

            private static String valueToString(float value) {
                int i = (int)value;
                if ((float)i == value) {
                    return Integer.toString(i);
                }
                return Float.toString(value);
            }
        }
    }
}

