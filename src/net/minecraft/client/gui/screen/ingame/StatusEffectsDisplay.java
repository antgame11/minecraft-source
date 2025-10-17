/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.ingame;

import com.google.common.collect.Ordering;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class StatusEffectsDisplay {
    private static final Identifier EFFECT_BACKGROUND_LARGE_TEXTURE = Identifier.ofVanilla("container/inventory/effect_background_large");
    private static final Identifier EFFECT_BACKGROUND_SMALL_TEXTURE = Identifier.ofVanilla("container/inventory/effect_background_small");
    private final HandledScreen<?> parent;
    private final MinecraftClient client;
    @Nullable
    private StatusEffectInstance hoveredStatusEffect;

    public StatusEffectsDisplay(HandledScreen<?> parent) {
        this.parent = parent;
        this.client = MinecraftClient.getInstance();
    }

    public boolean shouldHideStatusEffectHud() {
        int i = this.parent.x + this.parent.backgroundWidth + 2;
        int j = this.parent.width - i;
        return j >= 32;
    }

    public void drawStatusEffects(DrawContext context, int mouseX, int mouseY) {
        this.hoveredStatusEffect = null;
        int k = this.parent.x + this.parent.backgroundWidth + 2;
        int l = this.parent.width - k;
        Collection<StatusEffectInstance> collection = this.client.player.getStatusEffects();
        if (collection.isEmpty() || l < 32) {
            return;
        }
        boolean bl = l >= 120;
        int m = 33;
        if (collection.size() > 5) {
            m = 132 / (collection.size() - 1);
        }
        List<StatusEffectInstance> iterable = Ordering.natural().sortedCopy(collection);
        this.drawStatusEffectBackgrounds(context, k, m, iterable, bl);
        this.drawStatusEffectSprites(context, k, m, iterable, bl);
        if (bl) {
            this.drawStatusEffectDescriptions(context, k, m, iterable);
        } else if (mouseX >= k && mouseX <= k + 33) {
            int n = this.parent.y;
            for (StatusEffectInstance lv : iterable) {
                if (mouseY >= n && mouseY <= n + m) {
                    this.hoveredStatusEffect = lv;
                }
                n += m;
            }
        }
    }

    public void drawStatusEffectTooltip(DrawContext context, int mouseX, int mouseY) {
        if (this.hoveredStatusEffect != null) {
            List<Text> list = List.of(this.getStatusEffectDescription(this.hoveredStatusEffect), StatusEffectUtil.getDurationText(this.hoveredStatusEffect, 1.0f, this.client.world.getTickManager().getTickRate()));
            context.drawTooltip(this.parent.getTextRenderer(), list, Optional.empty(), mouseX, mouseY);
        }
    }

    private void drawStatusEffectBackgrounds(DrawContext context, int x, int height, Iterable<StatusEffectInstance> statusEffects, boolean wide) {
        int k = this.parent.y;
        for (StatusEffectInstance lv : statusEffects) {
            if (wide) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, EFFECT_BACKGROUND_LARGE_TEXTURE, x, k, 120, 32);
            } else {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, EFFECT_BACKGROUND_SMALL_TEXTURE, x, k, 32, 32);
            }
            k += height;
        }
    }

    private void drawStatusEffectSprites(DrawContext context, int x, int height, Iterable<StatusEffectInstance> statusEffects, boolean wide) {
        int k = this.parent.y;
        for (StatusEffectInstance lv : statusEffects) {
            RegistryEntry<StatusEffect> lv2 = lv.getEffectType();
            Identifier lv3 = InGameHud.getEffectTexture(lv2);
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv3, x + (wide ? 6 : 7), k + 7, 18, 18);
            k += height;
        }
    }

    private void drawStatusEffectDescriptions(DrawContext context, int x, int height, Iterable<StatusEffectInstance> statusEffects) {
        int k = this.parent.y;
        for (StatusEffectInstance lv : statusEffects) {
            Text lv2 = this.getStatusEffectDescription(lv);
            context.drawTextWithShadow(this.parent.getTextRenderer(), lv2, x + 10 + 18, k + 6, Colors.WHITE);
            Text lv3 = StatusEffectUtil.getDurationText(lv, 1.0f, this.client.world.getTickManager().getTickRate());
            context.drawTextWithShadow(this.parent.getTextRenderer(), lv3, x + 10 + 18, k + 6 + 10, -8421505);
            k += height;
        }
    }

    private Text getStatusEffectDescription(StatusEffectInstance statusEffect) {
        MutableText lv = statusEffect.getEffectType().value().getName().copy();
        if (statusEffect.getAmplifier() >= 1 && statusEffect.getAmplifier() <= 9) {
            lv.append(ScreenTexts.SPACE).append(Text.translatable("enchantment.level." + (statusEffect.getAmplifier() + 1)));
        }
        return lv;
    }
}

