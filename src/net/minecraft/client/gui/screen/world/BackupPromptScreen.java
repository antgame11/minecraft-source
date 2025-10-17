/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.world;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

@Environment(value=EnvType.CLIENT)
public class BackupPromptScreen
extends Screen {
    private static final Text SKIP_BUTTON_TEXT = Text.translatable("selectWorld.backupJoinSkipButton");
    public static final Text CONFIRM_BUTTON_TEXT = Text.translatable("selectWorld.backupJoinConfirmButton");
    private final Runnable onCancel;
    protected final Callback callback;
    private final Text subtitle;
    private final boolean showEraseCacheCheckbox;
    private MultilineText wrappedText = MultilineText.EMPTY;
    final Text firstButtonText;
    protected int field_32236;
    private CheckboxWidget eraseCacheCheckbox;

    public BackupPromptScreen(Runnable onCancel, Callback callback, Text title, Text subtitle, boolean showEraseCacheCheckbox) {
        this(onCancel, callback, title, subtitle, CONFIRM_BUTTON_TEXT, showEraseCacheCheckbox);
    }

    public BackupPromptScreen(Runnable onCancel, Callback callback, Text title, Text subtitle, Text firstButtonText, boolean showEraseCacheCheckbox) {
        super(title);
        this.onCancel = onCancel;
        this.callback = callback;
        this.subtitle = subtitle;
        this.showEraseCacheCheckbox = showEraseCacheCheckbox;
        this.firstButtonText = firstButtonText;
    }

    @Override
    protected void init() {
        super.init();
        this.wrappedText = MultilineText.create(this.textRenderer, this.subtitle, this.width - 50);
        int i = (this.wrappedText.getLineCount() + 1) * this.textRenderer.fontHeight;
        this.eraseCacheCheckbox = CheckboxWidget.builder(Text.translatable("selectWorld.backupEraseCache"), this.textRenderer).pos(this.width / 2 - 155 + 80, 76 + i).build();
        if (this.showEraseCacheCheckbox) {
            this.addDrawableChild(this.eraseCacheCheckbox);
        }
        this.addDrawableChild(ButtonWidget.builder(this.firstButtonText, button -> this.callback.proceed(true, this.eraseCacheCheckbox.isChecked())).dimensions(this.width / 2 - 155, 100 + i, 150, 20).build());
        this.addDrawableChild(ButtonWidget.builder(SKIP_BUTTON_TEXT, button -> this.callback.proceed(false, this.eraseCacheCheckbox.isChecked())).dimensions(this.width / 2 - 155 + 160, 100 + i, 150, 20).build());
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.onCancel.run()).dimensions(this.width / 2 - 155 + 80, 124 + i, 150, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 50, Colors.WHITE);
        this.wrappedText.draw(context, MultilineText.Alignment.CENTER, this.width / 2, 70, this.textRenderer.fontHeight, true, -1);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == InputUtil.GLFW_KEY_ESCAPE) {
            this.onCancel.run();
            return true;
        }
        return super.keyPressed(input);
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Callback {
        public void proceed(boolean var1, boolean var2);
    }
}

