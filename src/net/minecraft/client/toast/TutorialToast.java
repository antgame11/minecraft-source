/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.toast;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TutorialToast
implements Toast {
    private static final Identifier TEXTURE = Identifier.ofVanilla("toast/tutorial");
    public static final int PROGRESS_BAR_WIDTH = 154;
    public static final int PROGRESS_BAR_HEIGHT = 1;
    public static final int PROGRESS_BAR_X = 3;
    public static final int field_55091 = 4;
    private static final int field_55092 = 7;
    private static final int field_55093 = 3;
    private static final int field_55094 = 11;
    private static final int field_55095 = 30;
    private static final int field_55096 = 126;
    private final Type type;
    private final List<OrderedText> text;
    private Toast.Visibility visibility = Toast.Visibility.SHOW;
    private long lastTime;
    private float lastProgress;
    private float progress;
    private final boolean hasProgressBar;
    private final int displayDuration;

    public TutorialToast(TextRenderer textRenderer, Type type, Text title, @Nullable Text description, boolean hasProgressBar, int displayDuration) {
        this.type = type;
        this.text = new ArrayList<OrderedText>(2);
        this.text.addAll(textRenderer.wrapLines(title.copy().withColor(Colors.PURPLE), 126));
        if (description != null) {
            this.text.addAll(textRenderer.wrapLines(description, 126));
        }
        this.hasProgressBar = hasProgressBar;
        this.displayDuration = displayDuration;
    }

    public TutorialToast(TextRenderer textRenderer, Type type, Text title, @Nullable Text description, boolean hasProgressBar) {
        this(textRenderer, type, title, description, hasProgressBar, 0);
    }

    @Override
    public Toast.Visibility getVisibility() {
        return this.visibility;
    }

    @Override
    public void update(ToastManager manager, long time) {
        if (this.displayDuration > 0) {
            this.lastProgress = this.progress = Math.min((float)time / (float)this.displayDuration, 1.0f);
            this.lastTime = time;
            if (time > (long)this.displayDuration) {
                this.hide();
            }
        } else if (this.hasProgressBar) {
            this.lastProgress = MathHelper.clampedLerp(this.lastProgress, this.progress, (float)(time - this.lastTime) / 100.0f);
            this.lastTime = time;
        }
    }

    @Override
    public int getHeight() {
        return 7 + this.getTextHeight() + 3;
    }

    private int getTextHeight() {
        return Math.max(this.text.size(), 2) * 11;
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
        int m;
        int i = this.getHeight();
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, this.getWidth(), i);
        this.type.drawIcon(context, 6, 6);
        int j = this.text.size() * 11;
        int k = 7 + (this.getTextHeight() - j) / 2;
        for (m = 0; m < this.text.size(); ++m) {
            context.drawText(textRenderer, this.text.get(m), 30, k + m * 11, -16777216, false);
        }
        if (this.hasProgressBar) {
            m = i - 4;
            context.fill(3, m, 157, m + 1, Colors.WHITE);
            int n = this.progress >= this.lastProgress ? -16755456 : -11206656;
            context.fill(3, m, (int)(3.0f + 154.0f * this.lastProgress), m + 1, n);
        }
    }

    public void hide() {
        this.visibility = Toast.Visibility.HIDE;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Type {
        MOVEMENT_KEYS(Identifier.ofVanilla("toast/movement_keys")),
        MOUSE(Identifier.ofVanilla("toast/mouse")),
        TREE(Identifier.ofVanilla("toast/tree")),
        RECIPE_BOOK(Identifier.ofVanilla("toast/recipe_book")),
        WOODEN_PLANKS(Identifier.ofVanilla("toast/wooden_planks")),
        SOCIAL_INTERACTIONS(Identifier.ofVanilla("toast/social_interactions")),
        RIGHT_CLICK(Identifier.ofVanilla("toast/right_click"));

        private final Identifier texture;

        private Type(Identifier texture) {
            this.texture = texture;
        }

        public void drawIcon(DrawContext context, int x, int y) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, this.texture, x, y, 20, 20);
        }
    }
}

