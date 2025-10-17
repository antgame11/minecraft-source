/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.toast;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface Toast {
    public static final Object TYPE = new Object();
    public static final int BASE_WIDTH = 160;
    public static final int BASE_HEIGHT = 32;

    public Visibility getVisibility();

    public void update(ToastManager var1, long var2);

    @Nullable
    default public SoundEvent getSoundEvent() {
        return null;
    }

    public void draw(DrawContext var1, TextRenderer var2, long var3);

    default public Object getType() {
        return TYPE;
    }

    default public float getXPos(int scaledWindowWidth, float visibleWidthPortion) {
        return (float)scaledWindowWidth - (float)this.getWidth() * visibleWidthPortion;
    }

    default public float getYPos(int topIndex) {
        return topIndex * this.getHeight();
    }

    default public int getWidth() {
        return 160;
    }

    default public int getHeight() {
        return 32;
    }

    default public int getRequiredSpaceCount() {
        return MathHelper.ceilDiv(this.getHeight(), 32);
    }

    default public void onFinishedRendering() {
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Visibility {
        SHOW(SoundEvents.UI_TOAST_IN),
        HIDE(SoundEvents.UI_TOAST_OUT);

        private final SoundEvent sound;

        private Visibility(SoundEvent sound) {
            this.sound = sound;
        }

        public void playSound(SoundManager soundManager) {
            soundManager.play(PositionedSoundInstance.master(this.sound, 1.0f, 1.0f));
        }
    }
}

