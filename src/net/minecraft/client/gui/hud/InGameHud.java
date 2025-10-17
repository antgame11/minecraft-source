/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.hud;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.gui.hud.SpectatorHud;
import net.minecraft.client.gui.hud.SubtitlesHud;
import net.minecraft.client.gui.hud.bar.Bar;
import net.minecraft.client.gui.hud.bar.ExperienceBar;
import net.minecraft.client.gui.hud.bar.JumpBar;
import net.minecraft.client.gui.hud.bar.LocatorBar;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Nullables;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.GameMode;
import net.minecraft.world.border.WorldBorder;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class InGameHud {
    private static final Identifier CROSSHAIR_TEXTURE = Identifier.ofVanilla("hud/crosshair");
    private static final Identifier CROSSHAIR_ATTACK_INDICATOR_FULL_TEXTURE = Identifier.ofVanilla("hud/crosshair_attack_indicator_full");
    private static final Identifier CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_TEXTURE = Identifier.ofVanilla("hud/crosshair_attack_indicator_background");
    private static final Identifier CROSSHAIR_ATTACK_INDICATOR_PROGRESS_TEXTURE = Identifier.ofVanilla("hud/crosshair_attack_indicator_progress");
    private static final Identifier EFFECT_BACKGROUND_AMBIENT_TEXTURE = Identifier.ofVanilla("hud/effect_background_ambient");
    private static final Identifier EFFECT_BACKGROUND_TEXTURE = Identifier.ofVanilla("hud/effect_background");
    private static final Identifier HOTBAR_TEXTURE = Identifier.ofVanilla("hud/hotbar");
    private static final Identifier HOTBAR_SELECTION_TEXTURE = Identifier.ofVanilla("hud/hotbar_selection");
    private static final Identifier HOTBAR_OFFHAND_LEFT_TEXTURE = Identifier.ofVanilla("hud/hotbar_offhand_left");
    private static final Identifier HOTBAR_OFFHAND_RIGHT_TEXTURE = Identifier.ofVanilla("hud/hotbar_offhand_right");
    private static final Identifier HOTBAR_ATTACK_INDICATOR_BACKGROUND_TEXTURE = Identifier.ofVanilla("hud/hotbar_attack_indicator_background");
    private static final Identifier HOTBAR_ATTACK_INDICATOR_PROGRESS_TEXTURE = Identifier.ofVanilla("hud/hotbar_attack_indicator_progress");
    private static final Identifier ARMOR_EMPTY_TEXTURE = Identifier.ofVanilla("hud/armor_empty");
    private static final Identifier ARMOR_HALF_TEXTURE = Identifier.ofVanilla("hud/armor_half");
    private static final Identifier ARMOR_FULL_TEXTURE = Identifier.ofVanilla("hud/armor_full");
    private static final Identifier FOOD_EMPTY_HUNGER_TEXTURE = Identifier.ofVanilla("hud/food_empty_hunger");
    private static final Identifier FOOD_HALF_HUNGER_TEXTURE = Identifier.ofVanilla("hud/food_half_hunger");
    private static final Identifier FOOD_FULL_HUNGER_TEXTURE = Identifier.ofVanilla("hud/food_full_hunger");
    private static final Identifier FOOD_EMPTY_TEXTURE = Identifier.ofVanilla("hud/food_empty");
    private static final Identifier FOOD_HALF_TEXTURE = Identifier.ofVanilla("hud/food_half");
    private static final Identifier FOOD_FULL_TEXTURE = Identifier.ofVanilla("hud/food_full");
    private static final Identifier AIR_TEXTURE = Identifier.ofVanilla("hud/air");
    private static final Identifier AIR_BURSTING_TEXTURE = Identifier.ofVanilla("hud/air_bursting");
    private static final Identifier AIR_EMPTY_TEXTURE = Identifier.ofVanilla("hud/air_empty");
    private static final Identifier VEHICLE_CONTAINER_HEART_TEXTURE = Identifier.ofVanilla("hud/heart/vehicle_container");
    private static final Identifier VEHICLE_FULL_HEART_TEXTURE = Identifier.ofVanilla("hud/heart/vehicle_full");
    private static final Identifier VEHICLE_HALF_HEART_TEXTURE = Identifier.ofVanilla("hud/heart/vehicle_half");
    private static final Identifier VIGNETTE_TEXTURE = Identifier.ofVanilla("textures/misc/vignette.png");
    public static final Identifier NAUSEA_TEXTURE = Identifier.ofVanilla("textures/misc/nausea.png");
    private static final Identifier SPYGLASS_SCOPE = Identifier.ofVanilla("textures/misc/spyglass_scope.png");
    private static final Identifier POWDER_SNOW_OUTLINE = Identifier.ofVanilla("textures/misc/powder_snow_outline.png");
    private static final Comparator<ScoreboardEntry> SCOREBOARD_ENTRY_COMPARATOR = Comparator.comparing(ScoreboardEntry::value).reversed().thenComparing(ScoreboardEntry::owner, String.CASE_INSENSITIVE_ORDER);
    private static final Text DEMO_EXPIRED_MESSAGE = Text.translatable("demo.demoExpired");
    private static final Text SAVING_LEVEL_TEXT = Text.translatable("menu.savingLevel");
    private static final float field_32168 = 5.0f;
    private static final int field_59816 = 100;
    private static final int field_32169 = 10;
    private static final int field_32170 = 10;
    private static final String SCOREBOARD_JOINER = ": ";
    private static final float field_32172 = 0.2f;
    private static final int field_33942 = 9;
    private static final int field_33943 = 8;
    private static final int field_54914 = 10;
    private static final int field_54915 = 9;
    private static final int field_54916 = 8;
    private static final int field_54917 = 2;
    private static final int SUBMERGED_IN_WATER_AIR_BUBBLE_DELAY = 1;
    private static final float field_54920 = 0.5f;
    private static final float field_54921 = 0.1f;
    private static final float field_54922 = 1.0f;
    private static final float field_54923 = 0.1f;
    private static final int field_54924 = 3;
    private static final int field_54925 = 5;
    private static final float field_35431 = 0.2f;
    private static final int field_52769 = 5;
    private static final int field_52770 = 5;
    private final Random random = Random.create();
    private final MinecraftClient client;
    private final ChatHud chatHud;
    private int ticks;
    @Nullable
    private Text overlayMessage;
    private int overlayRemaining;
    private boolean overlayTinted;
    private boolean canShowChatDisabledScreen;
    public float vignetteDarkness = 1.0f;
    private int heldItemTooltipFade;
    private ItemStack currentStack = ItemStack.EMPTY;
    private final DebugHud debugHud;
    private final SubtitlesHud subtitlesHud;
    private final SpectatorHud spectatorHud;
    private final PlayerListHud playerListHud;
    private final BossBarHud bossBarHud;
    private int titleRemainTicks;
    @Nullable
    private Text title;
    @Nullable
    private Text subtitle;
    private int titleFadeInTicks;
    private int titleStayTicks;
    private int titleFadeOutTicks;
    private int lastHealthValue;
    private int renderHealthValue;
    private long lastHealthCheckTime;
    private long heartJumpEndTick;
    private int lastBurstBubble;
    @Nullable
    private Runnable deferredSubtitleRenderer;
    private float autosaveIndicatorAlpha;
    private float lastAutosaveIndicatorAlpha;
    private Pair<BarType, Bar> currentBar = Pair.of(BarType.EMPTY, Bar.EMPTY);
    private final Map<BarType, Supplier<Bar>> bars;
    private float spyglassScale;

    public InGameHud(MinecraftClient client) {
        this.client = client;
        this.debugHud = new DebugHud(client);
        this.spectatorHud = new SpectatorHud(client);
        this.chatHud = new ChatHud(client);
        this.playerListHud = new PlayerListHud(client, this);
        this.bossBarHud = new BossBarHud(client);
        this.subtitlesHud = new SubtitlesHud(client);
        this.bars = ImmutableMap.of(BarType.EMPTY, () -> Bar.EMPTY, BarType.EXPERIENCE, () -> new ExperienceBar(client), BarType.LOCATOR, () -> new LocatorBar(client), BarType.JUMPABLE_VEHICLE, () -> new JumpBar(client));
        this.setDefaultTitleFade();
    }

    public void setDefaultTitleFade() {
        this.titleFadeInTicks = 10;
        this.titleStayTicks = 70;
        this.titleFadeOutTicks = 20;
    }

    public void render(DrawContext context, RenderTickCounter tickCounter) {
        if (this.client.currentScreen instanceof LevelLoadingScreen) {
            return;
        }
        if (!this.client.options.hudHidden) {
            this.renderMiscOverlays(context, tickCounter);
            this.renderCrosshair(context, tickCounter);
            context.createNewRootLayer();
            this.renderMainHud(context, tickCounter);
            this.renderStatusEffectOverlay(context, tickCounter);
            this.renderBossBarHud(context, tickCounter);
        }
        this.renderSleepOverlay(context, tickCounter);
        if (!this.client.options.hudHidden) {
            this.renderDemoTimer(context, tickCounter);
            this.renderScoreboardSidebar(context, tickCounter);
            this.renderOverlayMessage(context, tickCounter);
            this.renderTitleAndSubtitle(context, tickCounter);
            this.renderChat(context, tickCounter);
            this.renderPlayerList(context, tickCounter);
            this.renderSubtitlesHud(context, this.client.currentScreen == null || this.client.currentScreen.deferSubtitles());
        } else if (this.client.currentScreen != null && this.client.currentScreen.deferSubtitles()) {
            this.renderSubtitlesHud(context, true);
        }
    }

    private void renderBossBarHud(DrawContext context, RenderTickCounter tickCounter) {
        this.bossBarHud.render(context);
    }

    public void renderDebugHud(DrawContext context) {
        this.debugHud.render(context);
    }

    private void renderSubtitlesHud(DrawContext context, boolean defer) {
        if (defer) {
            this.deferredSubtitleRenderer = () -> this.subtitlesHud.render(context);
        } else {
            this.deferredSubtitleRenderer = null;
            this.subtitlesHud.render(context);
        }
    }

    public void renderDeferredSubtitles() {
        if (this.deferredSubtitleRenderer != null) {
            this.deferredSubtitleRenderer.run();
            this.deferredSubtitleRenderer = null;
        }
    }

    private void renderMiscOverlays(DrawContext context, RenderTickCounter tickCounter) {
        float j;
        if (MinecraftClient.isFancyGraphicsOrBetter()) {
            this.renderVignetteOverlay(context, this.client.getCameraEntity());
        }
        ClientPlayerEntity lv = this.client.player;
        float f = tickCounter.getDynamicDeltaTicks();
        this.spyglassScale = MathHelper.lerp(0.5f * f, this.spyglassScale, 1.125f);
        if (this.client.options.getPerspective().isFirstPerson()) {
            if (lv.isUsingSpyglass()) {
                this.renderSpyglassOverlay(context, this.spyglassScale);
            } else {
                this.spyglassScale = 0.5f;
                for (EquipmentSlot lv2 : EquipmentSlot.values()) {
                    ItemStack lv3 = lv.getEquippedStack(lv2);
                    EquippableComponent lv4 = lv3.get(DataComponentTypes.EQUIPPABLE);
                    if (lv4 == null || lv4.slot() != lv2 || !lv4.cameraOverlay().isPresent()) continue;
                    this.renderOverlay(context, lv4.cameraOverlay().get().withPath(overlayTexture -> "textures/" + overlayTexture + ".png"), 1.0f);
                }
            }
        }
        if (lv.getFrozenTicks() > 0) {
            this.renderOverlay(context, POWDER_SNOW_OUTLINE, lv.getFreezingScale());
        }
        float g = tickCounter.getTickProgress(false);
        float h = MathHelper.lerp(g, lv.lastNauseaIntensity, lv.nauseaIntensity);
        float i = lv.getEffectFadeFactor(StatusEffects.NAUSEA, g);
        if (h > 0.0f) {
            this.renderPortalOverlay(context, h);
        } else if (i > 0.0f && (j = this.client.options.getDistortionEffectScale().getValue().floatValue()) < 1.0f) {
            float k = i * (1.0f - j);
            this.renderNauseaOverlay(context, k);
        }
    }

    private void renderSleepOverlay(DrawContext context, RenderTickCounter tickCounter) {
        if (this.client.player.getSleepTimer() <= 0) {
            return;
        }
        Profilers.get().push("sleep");
        context.createNewRootLayer();
        float f = this.client.player.getSleepTimer();
        float g = f / 100.0f;
        if (g > 1.0f) {
            g = 1.0f - (f - 100.0f) / 10.0f;
        }
        int i = (int)(220.0f * g) << 24 | 0x101020;
        context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), i);
        Profilers.get().pop();
    }

    private void renderOverlayMessage(DrawContext context, RenderTickCounter tickCounter) {
        TextRenderer lv = this.getTextRenderer();
        if (this.overlayMessage == null || this.overlayRemaining <= 0) {
            return;
        }
        Profilers.get().push("overlayMessage");
        float f = (float)this.overlayRemaining - tickCounter.getTickProgress(false);
        int i = (int)(f * 255.0f / 20.0f);
        if (i > 255) {
            i = 255;
        }
        if (i > 0) {
            context.createNewRootLayer();
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(context.getScaledWindowWidth() / 2, context.getScaledWindowHeight() - 68);
            int j = this.overlayTinted ? MathHelper.hsvToArgb(f / 50.0f, 0.7f, 0.6f, i) : ColorHelper.withAlpha(i, Colors.WHITE);
            int k = lv.getWidth(this.overlayMessage);
            context.drawTextWithBackground(lv, this.overlayMessage, -k / 2, -4, k, j);
            context.getMatrices().popMatrix();
        }
        Profilers.get().pop();
    }

    private void renderTitleAndSubtitle(DrawContext context, RenderTickCounter tickCounter) {
        if (this.title == null || this.titleRemainTicks <= 0) {
            return;
        }
        TextRenderer lv = this.getTextRenderer();
        Profilers.get().push("titleAndSubtitle");
        float f = (float)this.titleRemainTicks - tickCounter.getTickProgress(false);
        int i = 255;
        if (this.titleRemainTicks > this.titleFadeOutTicks + this.titleStayTicks) {
            float g = (float)(this.titleFadeInTicks + this.titleStayTicks + this.titleFadeOutTicks) - f;
            i = (int)(g * 255.0f / (float)this.titleFadeInTicks);
        }
        if (this.titleRemainTicks <= this.titleFadeOutTicks) {
            i = (int)(f * 255.0f / (float)this.titleFadeOutTicks);
        }
        if ((i = MathHelper.clamp(i, 0, 255)) > 0) {
            context.createNewRootLayer();
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(context.getScaledWindowWidth() / 2, context.getScaledWindowHeight() / 2);
            context.getMatrices().pushMatrix();
            context.getMatrices().scale(4.0f, 4.0f);
            int j = lv.getWidth(this.title);
            int k = ColorHelper.withAlpha(i, Colors.WHITE);
            context.drawTextWithBackground(lv, this.title, -j / 2, -10, j, k);
            context.getMatrices().popMatrix();
            if (this.subtitle != null) {
                context.getMatrices().pushMatrix();
                context.getMatrices().scale(2.0f, 2.0f);
                int l = lv.getWidth(this.subtitle);
                context.drawTextWithBackground(lv, this.subtitle, -l / 2, 5, l, k);
                context.getMatrices().popMatrix();
            }
            context.getMatrices().popMatrix();
        }
        Profilers.get().pop();
    }

    private void renderChat(DrawContext context, RenderTickCounter tickCounter) {
        if (!this.chatHud.isChatFocused()) {
            Window lv = this.client.getWindow();
            int i = MathHelper.floor(this.client.mouse.getScaledX(lv));
            int j = MathHelper.floor(this.client.mouse.getScaledY(lv));
            context.createNewRootLayer();
            this.chatHud.render(context, this.ticks, i, j, false);
        }
    }

    private void renderScoreboardSidebar(DrawContext context, RenderTickCounter tickCounter) {
        ScoreboardObjective lv5;
        ScoreboardDisplaySlot lv4;
        Scoreboard lv = this.client.world.getScoreboard();
        ScoreboardObjective lv2 = null;
        Team lv3 = lv.getScoreHolderTeam(this.client.player.getNameForScoreboard());
        if (lv3 != null && (lv4 = ScoreboardDisplaySlot.fromFormatting(lv3.getColor())) != null) {
            lv2 = lv.getObjectiveForSlot(lv4);
        }
        ScoreboardObjective scoreboardObjective = lv5 = lv2 != null ? lv2 : lv.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (lv5 != null) {
            context.createNewRootLayer();
            this.renderScoreboardSidebar(context, lv5);
        }
    }

    private void renderPlayerList(DrawContext context, RenderTickCounter tickCounter) {
        Scoreboard lv = this.client.world.getScoreboard();
        ScoreboardObjective lv2 = lv.getObjectiveForSlot(ScoreboardDisplaySlot.LIST);
        if (this.client.options.playerListKey.isPressed() && (!this.client.isInSingleplayer() || this.client.player.networkHandler.getListedPlayerListEntries().size() > 1 || lv2 != null)) {
            this.playerListHud.setVisible(true);
            context.createNewRootLayer();
            this.playerListHud.render(context, context.getScaledWindowWidth(), lv, lv2);
        } else {
            this.playerListHud.setVisible(false);
        }
    }

    private void renderCrosshair(DrawContext context, RenderTickCounter tickCounter) {
        GameOptions lv = this.client.options;
        if (!lv.getPerspective().isFirstPerson()) {
            return;
        }
        if (this.client.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR && !this.shouldRenderSpectatorCrosshair(this.client.crosshairTarget)) {
            return;
        }
        if (!this.client.debugHudEntryList.isEntryVisible(DebugHudEntries.THREE_DIMENSIONAL_CROSSHAIR)) {
            context.createNewRootLayer();
            int i = 15;
            context.drawGuiTexture(RenderPipelines.CROSSHAIR, CROSSHAIR_TEXTURE, (context.getScaledWindowWidth() - 15) / 2, (context.getScaledWindowHeight() - 15) / 2, 15, 15);
            if (this.client.options.getAttackIndicator().getValue() == AttackIndicator.CROSSHAIR) {
                float f = this.client.player.getAttackCooldownProgress(0.0f);
                boolean bl = false;
                if (this.client.targetedEntity != null && this.client.targetedEntity instanceof LivingEntity && f >= 1.0f) {
                    bl = this.client.player.getAttackCooldownProgressPerTick() > 5.0f;
                    bl &= this.client.targetedEntity.isAlive();
                }
                int j = context.getScaledWindowHeight() / 2 - 7 + 16;
                int k = context.getScaledWindowWidth() / 2 - 8;
                if (bl) {
                    context.drawGuiTexture(RenderPipelines.CROSSHAIR, CROSSHAIR_ATTACK_INDICATOR_FULL_TEXTURE, k, j, 16, 16);
                } else if (f < 1.0f) {
                    int l = (int)(f * 17.0f);
                    context.drawGuiTexture(RenderPipelines.CROSSHAIR, CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_TEXTURE, k, j, 16, 4);
                    context.drawGuiTexture(RenderPipelines.CROSSHAIR, CROSSHAIR_ATTACK_INDICATOR_PROGRESS_TEXTURE, 16, 4, 0, 0, k, j, l, 4);
                }
            }
        }
    }

    private boolean shouldRenderSpectatorCrosshair(@Nullable HitResult hitResult) {
        if (hitResult == null) {
            return false;
        }
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult)hitResult).getEntity() instanceof NamedScreenHandlerFactory;
        }
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            ClientWorld lv2 = this.client.world;
            BlockPos lv = ((BlockHitResult)hitResult).getBlockPos();
            return lv2.getBlockState(lv).createScreenHandlerFactory(lv2, lv) != null;
        }
        return false;
    }

    private void renderStatusEffectOverlay(DrawContext context, RenderTickCounter tickCounter) {
        Collection<StatusEffectInstance> collection = this.client.player.getStatusEffects();
        if (collection.isEmpty() || this.client.currentScreen != null && this.client.currentScreen.showsStatusEffects()) {
            return;
        }
        int i = 0;
        int j = 0;
        for (StatusEffectInstance lv : Ordering.natural().reverse().sortedCopy(collection)) {
            RegistryEntry<StatusEffect> lv2 = lv.getEffectType();
            if (!lv.shouldShowIcon()) continue;
            int k = context.getScaledWindowWidth();
            int l = 1;
            if (this.client.isDemo()) {
                l += 15;
            }
            if (lv2.value().isBeneficial()) {
                k -= 25 * ++i;
            } else {
                k -= 25 * ++j;
                l += 26;
            }
            float f = 1.0f;
            if (lv.isAmbient()) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, EFFECT_BACKGROUND_AMBIENT_TEXTURE, k, l, 24, 24);
            } else {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, EFFECT_BACKGROUND_TEXTURE, k, l, 24, 24);
                if (lv.isDurationBelow(200)) {
                    int m = lv.getDuration();
                    int n = 10 - m / 20;
                    f = MathHelper.clamp((float)m / 10.0f / 5.0f * 0.5f, 0.0f, 0.5f) + MathHelper.cos((float)m * (float)Math.PI / 5.0f) * MathHelper.clamp((float)n / 10.0f * 0.25f, 0.0f, 0.25f);
                    f = MathHelper.clamp(f, 0.0f, 1.0f);
                }
            }
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, InGameHud.getEffectTexture(lv2), k + 3, l + 3, 18, 18, ColorHelper.getWhite(f));
        }
    }

    public static Identifier getEffectTexture(RegistryEntry<StatusEffect> effect) {
        return effect.getKey().map(RegistryKey::getValue).map(id -> id.withPrefixedPath("mob_effect/")).orElseGet(MissingSprite::getMissingSpriteId);
    }

    private void renderMainHud(DrawContext context, RenderTickCounter tickCounter) {
        if (this.client.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR) {
            this.spectatorHud.renderSpectatorMenu(context);
        } else {
            this.renderHotbar(context, tickCounter);
        }
        if (this.client.interactionManager.hasStatusBars()) {
            this.renderStatusBars(context);
        }
        this.renderMountHealth(context);
        BarType lv = this.getCurrentBarType();
        if (lv != this.currentBar.getKey()) {
            this.currentBar = Pair.of(lv, this.bars.get((Object)lv).get());
        }
        this.currentBar.getValue().renderBar(context, tickCounter);
        if (this.client.interactionManager.hasExperienceBar() && this.client.player.experienceLevel > 0) {
            Bar.drawExperienceLevel(context, this.client.textRenderer, this.client.player.experienceLevel);
        }
        this.currentBar.getValue().renderAddons(context, tickCounter);
        if (this.client.interactionManager.getCurrentGameMode() != GameMode.SPECTATOR) {
            this.renderHeldItemTooltip(context);
        } else if (this.client.player.isSpectator()) {
            this.spectatorHud.render(context);
        }
    }

    private void renderHotbar(DrawContext context, RenderTickCounter tickCounter) {
        float f;
        int o;
        int n;
        int m;
        PlayerEntity lv = this.getCameraPlayer();
        if (lv == null) {
            return;
        }
        ItemStack lv2 = lv.getOffHandStack();
        Arm lv3 = lv.getMainArm().getOpposite();
        int i = context.getScaledWindowWidth() / 2;
        int j = 182;
        int k = 91;
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR_TEXTURE, i - 91, context.getScaledWindowHeight() - 22, 182, 22);
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION_TEXTURE, i - 91 - 1 + lv.getInventory().getSelectedSlot() * 20, context.getScaledWindowHeight() - 22 - 1, 24, 23);
        if (!lv2.isEmpty()) {
            if (lv3 == Arm.LEFT) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_TEXTURE, i - 91 - 29, context.getScaledWindowHeight() - 23, 29, 24);
            } else {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_RIGHT_TEXTURE, i + 91, context.getScaledWindowHeight() - 23, 29, 24);
            }
        }
        int l = 1;
        for (m = 0; m < 9; ++m) {
            n = i - 90 + m * 20 + 2;
            o = context.getScaledWindowHeight() - 16 - 3;
            this.renderHotbarItem(context, n, o, tickCounter, lv, lv.getInventory().getStack(m), l++);
        }
        if (!lv2.isEmpty()) {
            m = context.getScaledWindowHeight() - 16 - 3;
            if (lv3 == Arm.LEFT) {
                this.renderHotbarItem(context, i - 91 - 26, m, tickCounter, lv, lv2, l++);
            } else {
                this.renderHotbarItem(context, i + 91 + 10, m, tickCounter, lv, lv2, l++);
            }
        }
        if (this.client.options.getAttackIndicator().getValue() == AttackIndicator.HOTBAR && (f = this.client.player.getAttackCooldownProgress(0.0f)) < 1.0f) {
            n = context.getScaledWindowHeight() - 20;
            o = i + 91 + 6;
            if (lv3 == Arm.RIGHT) {
                o = i - 91 - 22;
            }
            int p = (int)(f * 19.0f);
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR_ATTACK_INDICATOR_BACKGROUND_TEXTURE, o, n, 18, 18);
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR_ATTACK_INDICATOR_PROGRESS_TEXTURE, 18, 18, 0, 18 - p, o, n + 18 - p, 18, p);
        }
    }

    private void renderHeldItemTooltip(DrawContext context) {
        Profilers.get().push("selectedItemName");
        if (this.heldItemTooltipFade > 0 && !this.currentStack.isEmpty()) {
            int l;
            MutableText lv = Text.empty().append(this.currentStack.getName()).formatted(this.currentStack.getRarity().getFormatting());
            if (this.currentStack.contains(DataComponentTypes.CUSTOM_NAME)) {
                lv.formatted(Formatting.ITALIC);
            }
            int i = this.getTextRenderer().getWidth(lv);
            int j = (context.getScaledWindowWidth() - i) / 2;
            int k = context.getScaledWindowHeight() - 59;
            if (!this.client.interactionManager.hasStatusBars()) {
                k += 14;
            }
            if ((l = (int)((float)this.heldItemTooltipFade * 256.0f / 10.0f)) > 255) {
                l = 255;
            }
            if (l > 0) {
                context.drawTextWithBackground(this.getTextRenderer(), lv, j, k, i, ColorHelper.withAlpha(l, Colors.WHITE));
            }
        }
        Profilers.get().pop();
    }

    private void renderDemoTimer(DrawContext context, RenderTickCounter tickCounter) {
        if (!this.client.isDemo()) {
            return;
        }
        Profilers.get().push("demo");
        context.createNewRootLayer();
        Text lv = this.client.world.getTime() >= 120500L ? DEMO_EXPIRED_MESSAGE : Text.translatable("demo.remainingTime", StringHelper.formatTicks((int)(120500L - this.client.world.getTime()), this.client.world.getTickManager().getTickRate()));
        int i = this.getTextRenderer().getWidth(lv);
        int j = context.getScaledWindowWidth() - i - 10;
        int k = 5;
        context.drawTextWithBackground(this.getTextRenderer(), lv, j, 5, i, -1);
        Profilers.get().pop();
    }

    private void renderScoreboardSidebar(DrawContext context, ScoreboardObjective objective) {
        int i;
        Scoreboard lv = objective.getScoreboard();
        NumberFormat lv2 = objective.getNumberFormatOr(StyledNumberFormat.RED);
        @Environment(value=EnvType.CLIENT)
        record SidebarEntry(Text name, Text score, int scoreWidth) {
        }
        SidebarEntry[] lvs = (SidebarEntry[])lv.getScoreboardEntries(objective).stream().filter(score -> !score.hidden()).sorted(SCOREBOARD_ENTRY_COMPARATOR).limit(15L).map(scoreboardEntry -> {
            Team lv = lv.getScoreHolderTeam(scoreboardEntry.owner());
            Text lv2 = scoreboardEntry.name();
            MutableText lv3 = Team.decorateName(lv, lv2);
            MutableText lv4 = scoreboardEntry.formatted(lv2);
            int i = this.getTextRenderer().getWidth(lv4);
            return new SidebarEntry(lv3, lv4, i);
        }).toArray(size -> new SidebarEntry[size]);
        Text lv3 = objective.getDisplayName();
        int j = i = this.getTextRenderer().getWidth(lv3);
        int k = this.getTextRenderer().getWidth(SCOREBOARD_JOINER);
        for (SidebarEntry lv4 : lvs) {
            j = Math.max(j, this.getTextRenderer().getWidth(lv4.name) + (lv4.scoreWidth > 0 ? k + lv4.scoreWidth : 0));
        }
        int l = j;
        int m = lvs.length;
        int n = m * this.getTextRenderer().fontHeight;
        int o = context.getScaledWindowHeight() / 2 + n / 3;
        int p = 3;
        int q = context.getScaledWindowWidth() - l - 3;
        int r = context.getScaledWindowWidth() - 3 + 2;
        int s = this.client.options.getTextBackgroundColor(0.3f);
        int t = this.client.options.getTextBackgroundColor(0.4f);
        int u = o - m * this.getTextRenderer().fontHeight;
        context.fill(q - 2, u - this.getTextRenderer().fontHeight - 1, r, u - 1, t);
        context.fill(q - 2, u - 1, r, o, s);
        context.drawText(this.getTextRenderer(), lv3, q + l / 2 - i / 2, u - this.getTextRenderer().fontHeight, Colors.WHITE, false);
        for (int v = 0; v < m; ++v) {
            SidebarEntry lv5 = lvs[v];
            int w = o - (m - v) * this.getTextRenderer().fontHeight;
            context.drawText(this.getTextRenderer(), lv5.name, q, w, Colors.WHITE, false);
            context.drawText(this.getTextRenderer(), lv5.score, r - lv5.scoreWidth, w, Colors.WHITE, false);
        }
    }

    @Nullable
    private PlayerEntity getCameraPlayer() {
        PlayerEntity lv;
        Entity entity = this.client.getCameraEntity();
        return entity instanceof PlayerEntity ? (lv = (PlayerEntity)entity) : null;
    }

    @Nullable
    private LivingEntity getRiddenEntity() {
        PlayerEntity lv = this.getCameraPlayer();
        if (lv != null) {
            Entity lv2 = lv.getVehicle();
            if (lv2 == null) {
                return null;
            }
            if (lv2 instanceof LivingEntity) {
                return (LivingEntity)lv2;
            }
        }
        return null;
    }

    private int getHeartCount(@Nullable LivingEntity entity) {
        if (entity == null || !entity.isLiving()) {
            return 0;
        }
        float f = entity.getMaxHealth();
        int i = (int)(f + 0.5f) / 2;
        if (i > 30) {
            i = 30;
        }
        return i;
    }

    private int getHeartRows(int heartCount) {
        return (int)Math.ceil((double)heartCount / 10.0);
    }

    private void renderStatusBars(DrawContext context) {
        PlayerEntity lv = this.getCameraPlayer();
        if (lv == null) {
            return;
        }
        int i = MathHelper.ceil(lv.getHealth());
        boolean bl = this.heartJumpEndTick > (long)this.ticks && (this.heartJumpEndTick - (long)this.ticks) / 3L % 2L == 1L;
        long l = Util.getMeasuringTimeMs();
        if (i < this.lastHealthValue && lv.timeUntilRegen > 0) {
            this.lastHealthCheckTime = l;
            this.heartJumpEndTick = this.ticks + 20;
        } else if (i > this.lastHealthValue && lv.timeUntilRegen > 0) {
            this.lastHealthCheckTime = l;
            this.heartJumpEndTick = this.ticks + 10;
        }
        if (l - this.lastHealthCheckTime > 1000L) {
            this.renderHealthValue = i;
            this.lastHealthCheckTime = l;
        }
        this.lastHealthValue = i;
        int j = this.renderHealthValue;
        this.random.setSeed(this.ticks * 312871);
        int k = context.getScaledWindowWidth() / 2 - 91;
        int m = context.getScaledWindowWidth() / 2 + 91;
        int n = context.getScaledWindowHeight() - 39;
        float f = Math.max((float)lv.getAttributeValue(EntityAttributes.MAX_HEALTH), (float)Math.max(j, i));
        int o = MathHelper.ceil(lv.getAbsorptionAmount());
        int p = MathHelper.ceil((f + (float)o) / 2.0f / 10.0f);
        int q = Math.max(10 - (p - 2), 3);
        int r = n - 10;
        int s = -1;
        if (lv.hasStatusEffect(StatusEffects.REGENERATION)) {
            s = this.ticks % MathHelper.ceil(f + 5.0f);
        }
        Profilers.get().push("armor");
        InGameHud.renderArmor(context, lv, n, p, q, k);
        Profilers.get().swap("health");
        this.renderHealthBar(context, lv, k, n, q, s, f, i, j, o, bl);
        LivingEntity lv2 = this.getRiddenEntity();
        int t = this.getHeartCount(lv2);
        if (t == 0) {
            Profilers.get().swap("food");
            this.renderFood(context, lv, n, m);
            r -= 10;
        }
        Profilers.get().swap("air");
        this.renderAirBubbles(context, lv, t, r, m);
        Profilers.get().pop();
    }

    private static void renderArmor(DrawContext context, PlayerEntity player, int y, int j, int healthBarLines, int x) {
        int m = player.getArmor();
        if (m <= 0) {
            return;
        }
        int n = y - (j - 1) * healthBarLines - 10;
        for (int o = 0; o < 10; ++o) {
            int p = x + o * 8;
            if (o * 2 + 1 < m) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ARMOR_FULL_TEXTURE, p, n, 9, 9);
            }
            if (o * 2 + 1 == m) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ARMOR_HALF_TEXTURE, p, n, 9, 9);
            }
            if (o * 2 + 1 <= m) continue;
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ARMOR_EMPTY_TEXTURE, p, n, 9, 9);
        }
    }

    private void renderHealthBar(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking) {
        HeartType lv = HeartType.fromPlayerState(player);
        boolean bl2 = player.getEntityWorld().getLevelProperties().isHardcore();
        int p = MathHelper.ceil((double)maxHealth / 2.0);
        int q = MathHelper.ceil((double)absorption / 2.0);
        int r = p * 2;
        for (int s = p + q - 1; s >= 0; --s) {
            boolean bl5;
            int y2;
            boolean bl3;
            int t = s / 10;
            int u = s % 10;
            int v = x + u * 8;
            int w = y - t * lines;
            if (lastHealth + absorption <= 4) {
                w += this.random.nextInt(2);
            }
            if (s < p && s == regeneratingHeartIndex) {
                w -= 2;
            }
            this.drawHeart(context, HeartType.CONTAINER, v, w, bl2, blinking, false);
            int x2 = s * 2;
            boolean bl = bl3 = s >= p;
            if (bl3 && (y2 = x2 - r) < absorption) {
                boolean bl4 = y2 + 1 == absorption;
                this.drawHeart(context, lv == HeartType.WITHERED ? lv : HeartType.ABSORBING, v, w, bl2, false, bl4);
            }
            if (blinking && x2 < health) {
                bl5 = x2 + 1 == health;
                this.drawHeart(context, lv, v, w, bl2, true, bl5);
            }
            if (x2 >= lastHealth) continue;
            bl5 = x2 + 1 == lastHealth;
            this.drawHeart(context, lv, v, w, bl2, false, bl5);
        }
    }

    private void drawHeart(DrawContext context, HeartType type, int x, int y, boolean hardcore, boolean blinking, boolean half) {
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, type.getTexture(hardcore, half, blinking), x, y, 9, 9);
    }

    private void renderAirBubbles(DrawContext context, PlayerEntity player, int heartCount, int top, int left) {
        int l = player.getMaxAir();
        int m = Math.clamp((long)player.getAir(), 0, l);
        boolean bl = player.isSubmergedIn(FluidTags.WATER);
        if (bl || m < l) {
            boolean bl2;
            top = this.getAirBubbleY(heartCount, top);
            int n = InGameHud.getAirBubbles(m, l, -2);
            int o = InGameHud.getAirBubbles(m, l, 0);
            int p = 10 - InGameHud.getAirBubbles(m, l, InGameHud.getAirBubbleDelay(m, bl));
            boolean bl3 = bl2 = n != o;
            if (!bl) {
                this.lastBurstBubble = 0;
            }
            for (int q = 1; q <= 10; ++q) {
                int r = left - (q - 1) * 8 - 9;
                if (q <= n) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, AIR_TEXTURE, r, top, 9, 9);
                    continue;
                }
                if (bl2 && q == o && bl) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, AIR_BURSTING_TEXTURE, r, top, 9, 9);
                    this.playBurstSound(q, player, p);
                    continue;
                }
                if (q <= 10 - p) continue;
                int s = p == 10 && this.ticks % 2 == 0 ? this.random.nextInt(2) : 0;
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, AIR_EMPTY_TEXTURE, r, top + s, 9, 9);
            }
        }
    }

    private int getAirBubbleY(int heartCount, int top) {
        int k = this.getHeartRows(heartCount) - 1;
        return top -= k * 10;
    }

    private static int getAirBubbles(int air, int maxAir, int delay) {
        return MathHelper.ceil((float)((air + delay) * 10) / (float)maxAir);
    }

    private static int getAirBubbleDelay(int air, boolean submergedInWater) {
        return air == 0 || !submergedInWater ? 0 : 1;
    }

    private void playBurstSound(int bubble, PlayerEntity player, int burstBubbles) {
        if (this.lastBurstBubble != bubble) {
            float f = 0.5f + 0.1f * (float)Math.max(0, burstBubbles - 3 + 1);
            float g = 1.0f + 0.1f * (float)Math.max(0, burstBubbles - 5 + 1);
            player.playSound(SoundEvents.UI_HUD_BUBBLE_POP, f, g);
            this.lastBurstBubble = bubble;
        }
    }

    private void renderFood(DrawContext context, PlayerEntity player, int top, int right) {
        HungerManager lv = player.getHungerManager();
        int k = lv.getFoodLevel();
        for (int l = 0; l < 10; ++l) {
            Identifier lv4;
            Identifier lv3;
            Identifier lv2;
            int m = top;
            if (player.hasStatusEffect(StatusEffects.HUNGER)) {
                lv2 = FOOD_EMPTY_HUNGER_TEXTURE;
                lv3 = FOOD_HALF_HUNGER_TEXTURE;
                lv4 = FOOD_FULL_HUNGER_TEXTURE;
            } else {
                lv2 = FOOD_EMPTY_TEXTURE;
                lv3 = FOOD_HALF_TEXTURE;
                lv4 = FOOD_FULL_TEXTURE;
            }
            if (player.getHungerManager().getSaturationLevel() <= 0.0f && this.ticks % (k * 3 + 1) == 0) {
                m += this.random.nextInt(3) - 1;
            }
            int n = right - l * 8 - 9;
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv2, n, m, 9, 9);
            if (l * 2 + 1 < k) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv4, n, m, 9, 9);
            }
            if (l * 2 + 1 != k) continue;
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv3, n, m, 9, 9);
        }
    }

    private void renderMountHealth(DrawContext context) {
        LivingEntity lv = this.getRiddenEntity();
        if (lv == null) {
            return;
        }
        int i = this.getHeartCount(lv);
        if (i == 0) {
            return;
        }
        int j = (int)Math.ceil(lv.getHealth());
        Profilers.get().swap("mountHealth");
        int k = context.getScaledWindowHeight() - 39;
        int l = context.getScaledWindowWidth() / 2 + 91;
        int m = k;
        int n = 0;
        while (i > 0) {
            int o = Math.min(i, 10);
            i -= o;
            for (int p = 0; p < o; ++p) {
                int q = l - p * 8 - 9;
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, VEHICLE_CONTAINER_HEART_TEXTURE, q, m, 9, 9);
                if (p * 2 + 1 + n < j) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, VEHICLE_FULL_HEART_TEXTURE, q, m, 9, 9);
                }
                if (p * 2 + 1 + n != j) continue;
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, VEHICLE_HALF_HEART_TEXTURE, q, m, 9, 9);
            }
            m -= 10;
            n += 20;
        }
    }

    private void renderOverlay(DrawContext context, Identifier texture, float opacity) {
        int i = ColorHelper.getWhite(opacity);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, 0, 0, 0.0f, 0.0f, context.getScaledWindowWidth(), context.getScaledWindowHeight(), context.getScaledWindowWidth(), context.getScaledWindowHeight(), i);
    }

    private void renderSpyglassOverlay(DrawContext context, float scale) {
        float g;
        float h = g = (float)Math.min(context.getScaledWindowWidth(), context.getScaledWindowHeight());
        float i = Math.min((float)context.getScaledWindowWidth() / g, (float)context.getScaledWindowHeight() / h) * scale;
        int j = MathHelper.floor(g * i);
        int k = MathHelper.floor(h * i);
        int l = (context.getScaledWindowWidth() - j) / 2;
        int m = (context.getScaledWindowHeight() - k) / 2;
        int n = l + j;
        int o = m + k;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, SPYGLASS_SCOPE, l, m, 0.0f, 0.0f, j, k, j, k);
        context.fill(RenderPipelines.GUI, 0, o, context.getScaledWindowWidth(), context.getScaledWindowHeight(), Colors.BLACK);
        context.fill(RenderPipelines.GUI, 0, 0, context.getScaledWindowWidth(), m, Colors.BLACK);
        context.fill(RenderPipelines.GUI, 0, m, l, o, Colors.BLACK);
        context.fill(RenderPipelines.GUI, n, m, context.getScaledWindowWidth(), o, Colors.BLACK);
    }

    private void updateVignetteDarkness(Entity entity) {
        BlockPos lv = BlockPos.ofFloored(entity.getX(), entity.getEyeY(), entity.getZ());
        float f = LightmapTextureManager.getBrightness(entity.getEntityWorld().getDimension(), entity.getEntityWorld().getLightLevel(lv));
        float g = MathHelper.clamp(1.0f - f, 0.0f, 1.0f);
        this.vignetteDarkness += (g - this.vignetteDarkness) * 0.01f;
    }

    private void renderVignetteOverlay(DrawContext context, @Nullable Entity entity) {
        int i;
        WorldBorder lv = this.client.world.getWorldBorder();
        float f = 0.0f;
        if (entity != null) {
            float g = (float)lv.getDistanceInsideBorder(entity);
            double d = Math.min(lv.getShrinkingSpeed() * (double)lv.getWarningTime() * 1000.0, Math.abs(lv.getSizeLerpTarget() - lv.getSize()));
            double e = Math.max((double)lv.getWarningBlocks(), d);
            if ((double)g < e) {
                f = 1.0f - (float)((double)g / e);
            }
        }
        if (f > 0.0f) {
            f = MathHelper.clamp(f, 0.0f, 1.0f);
            i = ColorHelper.fromFloats(1.0f, 0.0f, f, f);
        } else {
            float h = this.vignetteDarkness;
            h = MathHelper.clamp(h, 0.0f, 1.0f);
            i = ColorHelper.fromFloats(1.0f, h, h, h);
        }
        context.drawTexture(RenderPipelines.VIGNETTE, VIGNETTE_TEXTURE, 0, 0, 0.0f, 0.0f, context.getScaledWindowWidth(), context.getScaledWindowHeight(), context.getScaledWindowWidth(), context.getScaledWindowHeight(), i);
    }

    private void renderPortalOverlay(DrawContext context, float nauseaStrength) {
        if (nauseaStrength < 1.0f) {
            nauseaStrength *= nauseaStrength;
            nauseaStrength *= nauseaStrength;
            nauseaStrength = nauseaStrength * 0.8f + 0.2f;
        }
        int i = ColorHelper.getWhite(nauseaStrength);
        Sprite lv = this.client.getBlockRenderManager().getModels().getModelParticleSprite(Blocks.NETHER_PORTAL.getDefaultState());
        context.drawSpriteStretched(RenderPipelines.GUI_TEXTURED, lv, 0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), i);
    }

    private void renderNauseaOverlay(DrawContext context, float nauseaStrength) {
        int i = context.getScaledWindowWidth();
        int j = context.getScaledWindowHeight();
        context.getMatrices().pushMatrix();
        float g = MathHelper.lerp(nauseaStrength, 2.0f, 1.0f);
        context.getMatrices().translate((float)i / 2.0f, (float)j / 2.0f);
        context.getMatrices().scale(g, g);
        context.getMatrices().translate((float)(-i) / 2.0f, (float)(-j) / 2.0f);
        float h = 0.2f * nauseaStrength;
        float k = 0.4f * nauseaStrength;
        float l = 0.2f * nauseaStrength;
        context.drawTexture(RenderPipelines.GUI_NAUSEA_OVERLAY, NAUSEA_TEXTURE, 0, 0, 0.0f, 0.0f, i, j, i, j, ColorHelper.fromFloats(1.0f, h, k, l));
        context.getMatrices().popMatrix();
    }

    private void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed) {
        if (stack.isEmpty()) {
            return;
        }
        float f = (float)stack.getBobbingAnimationTime() - tickCounter.getTickProgress(false);
        if (f > 0.0f) {
            float g = 1.0f + f / 5.0f;
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(x + 8, y + 12);
            context.getMatrices().scale(1.0f / g, (g + 1.0f) / 2.0f);
            context.getMatrices().translate(-(x + 8), -(y + 12));
        }
        context.drawItem(player, stack, x, y, seed);
        if (f > 0.0f) {
            context.getMatrices().popMatrix();
        }
        context.drawStackOverlay(this.client.textRenderer, stack, x, y);
    }

    public void tick(boolean paused) {
        this.tickAutosaveIndicator();
        if (!paused) {
            this.tick();
        }
    }

    private void tick() {
        if (this.overlayRemaining > 0) {
            --this.overlayRemaining;
        }
        if (this.titleRemainTicks > 0) {
            --this.titleRemainTicks;
            if (this.titleRemainTicks <= 0) {
                this.title = null;
                this.subtitle = null;
            }
        }
        ++this.ticks;
        Entity lv = this.client.getCameraEntity();
        if (lv != null) {
            this.updateVignetteDarkness(lv);
        }
        if (this.client.player != null) {
            ItemStack lv2 = this.client.player.getInventory().getSelectedStack();
            if (lv2.isEmpty()) {
                this.heldItemTooltipFade = 0;
            } else if (this.currentStack.isEmpty() || !lv2.isOf(this.currentStack.getItem()) || !lv2.getName().equals(this.currentStack.getName())) {
                this.heldItemTooltipFade = (int)(40.0 * this.client.options.getNotificationDisplayTime().getValue());
            } else if (this.heldItemTooltipFade > 0) {
                --this.heldItemTooltipFade;
            }
            this.currentStack = lv2;
        }
        this.chatHud.tickRemovalQueueIfExists();
    }

    private void tickAutosaveIndicator() {
        IntegratedServer minecraftServer = this.client.getServer();
        boolean bl = minecraftServer != null && minecraftServer.isSaving();
        this.lastAutosaveIndicatorAlpha = this.autosaveIndicatorAlpha;
        this.autosaveIndicatorAlpha = MathHelper.lerp(0.2f, this.autosaveIndicatorAlpha, bl ? 1.0f : 0.0f);
    }

    public void setRecordPlayingOverlay(Text description) {
        MutableText lv = Text.translatable("record.nowPlaying", description);
        this.setOverlayMessage(lv, true);
        this.client.getNarratorManager().narrateSystemImmediately(lv);
    }

    public void setOverlayMessage(Text message, boolean tinted) {
        this.setCanShowChatDisabledScreen(false);
        this.overlayMessage = message;
        this.overlayRemaining = 60;
        this.overlayTinted = tinted;
    }

    public void setCanShowChatDisabledScreen(boolean canShowChatDisabledScreen) {
        this.canShowChatDisabledScreen = canShowChatDisabledScreen;
    }

    public boolean shouldShowChatDisabledScreen() {
        return this.canShowChatDisabledScreen && this.overlayRemaining > 0;
    }

    public void setTitleTicks(int fadeInTicks, int stayTicks, int fadeOutTicks) {
        if (fadeInTicks >= 0) {
            this.titleFadeInTicks = fadeInTicks;
        }
        if (stayTicks >= 0) {
            this.titleStayTicks = stayTicks;
        }
        if (fadeOutTicks >= 0) {
            this.titleFadeOutTicks = fadeOutTicks;
        }
        if (this.titleRemainTicks > 0) {
            this.titleRemainTicks = this.titleFadeInTicks + this.titleStayTicks + this.titleFadeOutTicks;
        }
    }

    public void setSubtitle(Text subtitle) {
        this.subtitle = subtitle;
    }

    public void setTitle(Text title) {
        this.title = title;
        this.titleRemainTicks = this.titleFadeInTicks + this.titleStayTicks + this.titleFadeOutTicks;
    }

    public void clearTitle() {
        this.title = null;
        this.subtitle = null;
        this.titleRemainTicks = 0;
    }

    public ChatHud getChatHud() {
        return this.chatHud;
    }

    public int getTicks() {
        return this.ticks;
    }

    public TextRenderer getTextRenderer() {
        return this.client.textRenderer;
    }

    public SpectatorHud getSpectatorHud() {
        return this.spectatorHud;
    }

    public PlayerListHud getPlayerListHud() {
        return this.playerListHud;
    }

    public void clear() {
        this.playerListHud.clear();
        this.bossBarHud.clear();
        this.client.getToastManager().clear();
        this.debugHud.clear();
        this.chatHud.clear(true);
        this.clearTitle();
        this.setDefaultTitleFade();
    }

    public BossBarHud getBossBarHud() {
        return this.bossBarHud;
    }

    public DebugHud getDebugHud() {
        return this.debugHud;
    }

    public void resetDebugHudChunk() {
        this.debugHud.resetChunk();
    }

    public void renderAutosaveIndicator(DrawContext context, RenderTickCounter tickCounter) {
        int i;
        if (this.client.options.getShowAutosaveIndicator().getValue().booleanValue() && (this.autosaveIndicatorAlpha > 0.0f || this.lastAutosaveIndicatorAlpha > 0.0f) && (i = MathHelper.floor(255.0f * MathHelper.clamp(MathHelper.lerp(tickCounter.getFixedDeltaTicks(), this.lastAutosaveIndicatorAlpha, this.autosaveIndicatorAlpha), 0.0f, 1.0f))) > 0) {
            TextRenderer lv = this.getTextRenderer();
            int j = lv.getWidth(SAVING_LEVEL_TEXT);
            int k = ColorHelper.withAlpha(i, Colors.WHITE);
            int l = context.getScaledWindowWidth() - j - 5;
            int m = context.getScaledWindowHeight() - lv.fontHeight - 5;
            context.createNewRootLayer();
            context.drawTextWithBackground(lv, SAVING_LEVEL_TEXT, l, m, j, k);
        }
    }

    private boolean shouldShowExperienceBar() {
        return this.client.player.experienceBarDisplayStartTime + 100 > this.client.player.age;
    }

    private boolean shouldShowJumpBar() {
        return this.client.player.getMountJumpStrength() > 0.0f || Nullables.mapOrElse(this.client.player.getJumpingMount(), JumpingMount::getJumpCooldown, 0) > 0;
    }

    private BarType getCurrentBarType() {
        boolean bl = this.client.player.networkHandler.getWaypointHandler().hasWaypoint();
        boolean bl2 = this.client.player.getJumpingMount() != null;
        boolean bl3 = this.client.interactionManager.hasExperienceBar();
        if (bl) {
            if (bl2 && this.shouldShowJumpBar()) {
                return BarType.JUMPABLE_VEHICLE;
            }
            if (bl3 && this.shouldShowExperienceBar()) {
                return BarType.EXPERIENCE;
            }
            return BarType.LOCATOR;
        }
        if (bl2) {
            return BarType.JUMPABLE_VEHICLE;
        }
        if (bl3) {
            return BarType.EXPERIENCE;
        }
        return BarType.EMPTY;
    }

    @Environment(value=EnvType.CLIENT)
    static enum BarType {
        EMPTY,
        EXPERIENCE,
        LOCATOR,
        JUMPABLE_VEHICLE;

    }

    @Environment(value=EnvType.CLIENT)
    static enum HeartType {
        CONTAINER(Identifier.ofVanilla("hud/heart/container"), Identifier.ofVanilla("hud/heart/container_blinking"), Identifier.ofVanilla("hud/heart/container"), Identifier.ofVanilla("hud/heart/container_blinking"), Identifier.ofVanilla("hud/heart/container_hardcore"), Identifier.ofVanilla("hud/heart/container_hardcore_blinking"), Identifier.ofVanilla("hud/heart/container_hardcore"), Identifier.ofVanilla("hud/heart/container_hardcore_blinking")),
        NORMAL(Identifier.ofVanilla("hud/heart/full"), Identifier.ofVanilla("hud/heart/full_blinking"), Identifier.ofVanilla("hud/heart/half"), Identifier.ofVanilla("hud/heart/half_blinking"), Identifier.ofVanilla("hud/heart/hardcore_full"), Identifier.ofVanilla("hud/heart/hardcore_full_blinking"), Identifier.ofVanilla("hud/heart/hardcore_half"), Identifier.ofVanilla("hud/heart/hardcore_half_blinking")),
        POISONED(Identifier.ofVanilla("hud/heart/poisoned_full"), Identifier.ofVanilla("hud/heart/poisoned_full_blinking"), Identifier.ofVanilla("hud/heart/poisoned_half"), Identifier.ofVanilla("hud/heart/poisoned_half_blinking"), Identifier.ofVanilla("hud/heart/poisoned_hardcore_full"), Identifier.ofVanilla("hud/heart/poisoned_hardcore_full_blinking"), Identifier.ofVanilla("hud/heart/poisoned_hardcore_half"), Identifier.ofVanilla("hud/heart/poisoned_hardcore_half_blinking")),
        WITHERED(Identifier.ofVanilla("hud/heart/withered_full"), Identifier.ofVanilla("hud/heart/withered_full_blinking"), Identifier.ofVanilla("hud/heart/withered_half"), Identifier.ofVanilla("hud/heart/withered_half_blinking"), Identifier.ofVanilla("hud/heart/withered_hardcore_full"), Identifier.ofVanilla("hud/heart/withered_hardcore_full_blinking"), Identifier.ofVanilla("hud/heart/withered_hardcore_half"), Identifier.ofVanilla("hud/heart/withered_hardcore_half_blinking")),
        ABSORBING(Identifier.ofVanilla("hud/heart/absorbing_full"), Identifier.ofVanilla("hud/heart/absorbing_full_blinking"), Identifier.ofVanilla("hud/heart/absorbing_half"), Identifier.ofVanilla("hud/heart/absorbing_half_blinking"), Identifier.ofVanilla("hud/heart/absorbing_hardcore_full"), Identifier.ofVanilla("hud/heart/absorbing_hardcore_full_blinking"), Identifier.ofVanilla("hud/heart/absorbing_hardcore_half"), Identifier.ofVanilla("hud/heart/absorbing_hardcore_half_blinking")),
        FROZEN(Identifier.ofVanilla("hud/heart/frozen_full"), Identifier.ofVanilla("hud/heart/frozen_full_blinking"), Identifier.ofVanilla("hud/heart/frozen_half"), Identifier.ofVanilla("hud/heart/frozen_half_blinking"), Identifier.ofVanilla("hud/heart/frozen_hardcore_full"), Identifier.ofVanilla("hud/heart/frozen_hardcore_full_blinking"), Identifier.ofVanilla("hud/heart/frozen_hardcore_half"), Identifier.ofVanilla("hud/heart/frozen_hardcore_half_blinking"));

        private final Identifier fullTexture;
        private final Identifier fullBlinkingTexture;
        private final Identifier halfTexture;
        private final Identifier halfBlinkingTexture;
        private final Identifier hardcoreFullTexture;
        private final Identifier hardcoreFullBlinkingTexture;
        private final Identifier hardcoreHalfTexture;
        private final Identifier hardcoreHalfBlinkingTexture;

        private HeartType(Identifier fullTexture, Identifier fullBlinkingTexture, Identifier halfTexture, Identifier halfBlinkingTexture, Identifier hardcoreFullTexture, Identifier hardcoreFullBlinkingTexture, Identifier hardcoreHalfTexture, Identifier hardcoreHalfBlinkingTexture) {
            this.fullTexture = fullTexture;
            this.fullBlinkingTexture = fullBlinkingTexture;
            this.halfTexture = halfTexture;
            this.halfBlinkingTexture = halfBlinkingTexture;
            this.hardcoreFullTexture = hardcoreFullTexture;
            this.hardcoreFullBlinkingTexture = hardcoreFullBlinkingTexture;
            this.hardcoreHalfTexture = hardcoreHalfTexture;
            this.hardcoreHalfBlinkingTexture = hardcoreHalfBlinkingTexture;
        }

        public Identifier getTexture(boolean hardcore, boolean half, boolean blinking) {
            if (!hardcore) {
                if (half) {
                    return blinking ? this.halfBlinkingTexture : this.halfTexture;
                }
                return blinking ? this.fullBlinkingTexture : this.fullTexture;
            }
            if (half) {
                return blinking ? this.hardcoreHalfBlinkingTexture : this.hardcoreHalfTexture;
            }
            return blinking ? this.hardcoreFullBlinkingTexture : this.hardcoreFullTexture;
        }

        static HeartType fromPlayerState(PlayerEntity player) {
            HeartType lv = player.hasStatusEffect(StatusEffects.POISON) ? POISONED : (player.hasStatusEffect(StatusEffects.WITHER) ? WITHERED : (player.isFrozen() ? FROZEN : NORMAL));
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Renderable {
        public void render(DrawContext var1, RenderTickCounter var2);
    }
}

