/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.toast;

import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class AdvancementToast
implements Toast {
    private static final Identifier TEXTURE = Identifier.ofVanilla("toast/advancement");
    public static final int DEFAULT_DURATION_MS = 5000;
    private final AdvancementEntry advancement;
    private Toast.Visibility visibility = Toast.Visibility.HIDE;

    public AdvancementToast(AdvancementEntry advancement) {
        this.advancement = advancement;
    }

    @Override
    public Toast.Visibility getVisibility() {
        return this.visibility;
    }

    @Override
    public void update(ToastManager manager, long time) {
        AdvancementDisplay lv = this.advancement.value().display().orElse(null);
        if (lv == null) {
            this.visibility = Toast.Visibility.HIDE;
            return;
        }
        this.visibility = (double)time >= 5000.0 * manager.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    @Override
    @Nullable
    public SoundEvent getSoundEvent() {
        return this.isChallenge() ? SoundEvents.UI_TOAST_CHALLENGE_COMPLETE : null;
    }

    private boolean isChallenge() {
        Optional<AdvancementDisplay> optional = this.advancement.value().display();
        return optional.isPresent() && optional.get().getFrame().equals(AdvancementFrame.CHALLENGE);
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
        int i;
        AdvancementDisplay lv = this.advancement.value().display().orElse(null);
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, this.getWidth(), this.getHeight());
        if (lv == null) {
            return;
        }
        List<OrderedText> list = textRenderer.wrapLines(lv.getTitle(), 125);
        int n = i = lv.getFrame() == AdvancementFrame.CHALLENGE ? -30465 : Colors.YELLOW;
        if (list.size() == 1) {
            context.drawText(textRenderer, lv.getFrame().getToastText(), 30, 7, i, false);
            context.drawText(textRenderer, list.get(0), 30, 18, -1, false);
        } else {
            int j = 1500;
            float f = 300.0f;
            if (startTime < 1500L) {
                int k = MathHelper.floor(MathHelper.clamp((float)(1500L - startTime) / 300.0f, 0.0f, 1.0f) * 255.0f);
                context.drawText(textRenderer, lv.getFrame().getToastText(), 30, 11, ColorHelper.withAlpha(k, i), false);
            } else {
                int k = MathHelper.floor(MathHelper.clamp((float)(startTime - 1500L) / 300.0f, 0.0f, 1.0f) * 252.0f);
                int m = this.getHeight() / 2 - list.size() * textRenderer.fontHeight / 2;
                for (OrderedText lv2 : list) {
                    context.drawText(textRenderer, lv2, 30, m, ColorHelper.withAlpha(k, Colors.WHITE), false);
                    m += textRenderer.fontHeight;
                }
            }
        }
        context.drawItemWithoutEntity(lv.getIcon(), 8, 8);
    }
}

