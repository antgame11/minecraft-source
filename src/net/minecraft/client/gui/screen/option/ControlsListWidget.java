/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.option;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.NarratedMultilineTextWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.ArrayUtils;

@Environment(value=EnvType.CLIENT)
public class ControlsListWidget
extends ElementListWidget<Entry> {
    private static final int field_49533 = 20;
    final KeybindsScreen parent;
    private int maxKeyNameLength;

    public ControlsListWidget(KeybindsScreen parent, MinecraftClient client) {
        super(client, parent.width, parent.layout.getContentHeight(), parent.layout.getHeaderHeight(), 20);
        this.parent = parent;
        Object[] lvs = ArrayUtils.clone(client.options.allKeys);
        Arrays.sort(lvs);
        KeyBinding.Category lv = null;
        for (Object lv2 : lvs) {
            MutableText lv4;
            int i;
            KeyBinding.Category lv3 = ((KeyBinding)lv2).getCategory();
            if (lv3 != lv) {
                lv = lv3;
                this.addEntry(new CategoryEntry(lv3));
            }
            if ((i = client.textRenderer.getWidth(lv4 = Text.translatable(((KeyBinding)lv2).getId()))) > this.maxKeyNameLength) {
                this.maxKeyNameLength = i;
            }
            this.addEntry(new KeyBindingEntry((KeyBinding)lv2, lv4));
        }
    }

    public void update() {
        KeyBinding.updateKeysByCode();
        this.updateChildren();
    }

    public void updateChildren() {
        this.children().forEach(Entry::update);
    }

    @Override
    public int getRowWidth() {
        return 340;
    }

    @Environment(value=EnvType.CLIENT)
    public class CategoryEntry
    extends Entry {
        private final NarratedMultilineTextWidget field_62179;

        public CategoryEntry(KeyBinding.Category arg2) {
            this.field_62179 = new NarratedMultilineTextWidget(ControlsListWidget.this.getRowWidth(), arg2.getLabel(), ((ControlsListWidget)ControlsListWidget.this).client.textRenderer, false, NarratedMultilineTextWidget.BackgroundRendering.ON_FOCUS, 4);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            this.field_62179.setPosition(ControlsListWidget.this.width / 2 - this.field_62179.getWidth() / 2, this.getContentBottomEnd() - ((ControlsListWidget)ControlsListWidget.this).client.textRenderer.fontHeight - 1);
            this.field_62179.render(context, mouseX, mouseY, deltaTicks);
        }

        @Override
        public List<? extends Element> children() {
            return List.of(this.field_62179);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of(this.field_62179);
        }

        @Override
        protected void update() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    public class KeyBindingEntry
    extends Entry {
        private static final Text RESET_TEXT = Text.translatable("controls.reset");
        private static final int field_49535 = 10;
        private final KeyBinding binding;
        private final Text bindingName;
        private final ButtonWidget editButton;
        private final ButtonWidget resetButton;
        private boolean duplicate = false;

        KeyBindingEntry(KeyBinding binding, Text bindingName) {
            this.binding = binding;
            this.bindingName = bindingName;
            this.editButton = ButtonWidget.builder(bindingName, button -> {
                ControlsListWidget.this.parent.selectedKeyBinding = binding;
                ControlsListWidget.this.update();
            }).dimensions(0, 0, 75, 20).narrationSupplier(textSupplier -> {
                if (binding.isUnbound()) {
                    return Text.translatable("narrator.controls.unbound", bindingName);
                }
                return Text.translatable("narrator.controls.bound", bindingName, textSupplier.get());
            }).build();
            this.resetButton = ButtonWidget.builder(RESET_TEXT, button -> {
                binding.setBoundKey(binding.getDefaultKey());
                ControlsListWidget.this.update();
            }).dimensions(0, 0, 50, 20).narrationSupplier(textSupplier -> Text.translatable("narrator.controls.reset", bindingName)).build();
            this.update();
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            int k = ControlsListWidget.this.getScrollbarX() - this.resetButton.getWidth() - 10;
            int l = this.getContentY() - 2;
            this.resetButton.setPosition(k, l);
            this.resetButton.render(context, mouseX, mouseY, deltaTicks);
            int m = k - 5 - this.editButton.getWidth();
            this.editButton.setPosition(m, l);
            this.editButton.render(context, mouseX, mouseY, deltaTicks);
            context.drawTextWithShadow(((ControlsListWidget)ControlsListWidget.this).client.textRenderer, this.bindingName, this.getContentX(), this.getContentMiddleY() - ((ControlsListWidget)ControlsListWidget.this).client.textRenderer.fontHeight / 2, Colors.WHITE);
            if (this.duplicate) {
                int n = 3;
                int o = this.editButton.getX() - 6;
                context.fill(o, this.getContentY() - 1, o + 3, this.getContentBottomEnd(), Colors.YELLOW);
            }
        }

        @Override
        public List<? extends Element> children() {
            return ImmutableList.of(this.editButton, this.resetButton);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return ImmutableList.of(this.editButton, this.resetButton);
        }

        @Override
        protected void update() {
            this.editButton.setMessage(this.binding.getBoundKeyLocalizedText());
            this.resetButton.active = !this.binding.isDefault();
            this.duplicate = false;
            MutableText lv = Text.empty();
            if (!this.binding.isUnbound()) {
                for (KeyBinding lv2 : ((ControlsListWidget)ControlsListWidget.this).client.options.allKeys) {
                    if (lv2 == this.binding || !this.binding.equals(lv2) || lv2.isDefault() && this.binding.isDefault()) continue;
                    if (this.duplicate) {
                        lv.append(", ");
                    }
                    this.duplicate = true;
                    lv.append(Text.translatable(lv2.getId()));
                }
            }
            if (this.duplicate) {
                this.editButton.setMessage(Text.literal("[ ").append(this.editButton.getMessage().copy().formatted(Formatting.WHITE)).append(" ]").formatted(Formatting.YELLOW));
                this.editButton.setTooltip(Tooltip.of(Text.translatable("controls.keybinds.duplicateKeybinds", lv)));
            } else {
                this.editButton.setTooltip(null);
            }
            if (ControlsListWidget.this.parent.selectedKeyBinding == this.binding) {
                this.editButton.setMessage(Text.literal("> ").append(this.editButton.getMessage().copy().formatted(Formatting.WHITE, Formatting.UNDERLINE)).append(" <").formatted(Formatting.YELLOW));
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class Entry
    extends ElementListWidget.Entry<Entry> {
        abstract void update();
    }
}

