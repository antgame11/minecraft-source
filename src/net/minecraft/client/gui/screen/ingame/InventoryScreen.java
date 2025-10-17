/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.ingame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import net.minecraft.client.gui.screen.recipebook.CraftingRecipeBookWidget;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class InventoryScreen
extends RecipeBookScreen<PlayerScreenHandler> {
    private float mouseX;
    private float mouseY;
    private boolean mouseDown;
    private final StatusEffectsDisplay statusEffectsDisplay;

    public InventoryScreen(PlayerEntity player) {
        super(player.playerScreenHandler, new CraftingRecipeBookWidget(player.playerScreenHandler), player.getInventory(), Text.translatable("container.crafting"));
        this.titleX = 97;
        this.statusEffectsDisplay = new StatusEffectsDisplay(this);
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        if (this.client.player.isInCreativeMode()) {
            this.client.setScreen(new CreativeInventoryScreen(this.client.player, this.client.player.networkHandler.getEnabledFeatures(), this.client.options.getOperatorItemsTab().getValue()));
        }
    }

    @Override
    protected void init() {
        if (this.client.player.isInCreativeMode()) {
            this.client.setScreen(new CreativeInventoryScreen(this.client.player, this.client.player.networkHandler.getEnabledFeatures(), this.client.options.getOperatorItemsTab().getValue()));
            return;
        }
        super.init();
    }

    @Override
    protected ScreenPos getRecipeBookButtonPos() {
        return new ScreenPos(this.x + 104, this.height / 2 - 22);
    }

    @Override
    protected void onRecipeBookToggled() {
        this.mouseDown = true;
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, Colors.DARK_GRAY, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        this.statusEffectsDisplay.drawStatusEffects(context, mouseX, mouseY);
        super.render(context, mouseX, mouseY, deltaTicks);
        this.statusEffectsDisplay.drawStatusEffectTooltip(context, mouseX, mouseY);
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    @Override
    public boolean showsStatusEffects() {
        return this.statusEffectsDisplay.shouldHideStatusEffectHud();
    }

    @Override
    protected boolean shouldAddPaddingToGhostResult() {
        return false;
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        int k = this.x;
        int l = this.y;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, k, l, 0.0f, 0.0f, this.backgroundWidth, this.backgroundHeight, 256, 256);
        InventoryScreen.drawEntity(context, k + 26, l + 8, k + 75, l + 78, 30, 0.0625f, this.mouseX, this.mouseY, (LivingEntity)this.client.player);
    }

    public static void drawEntity(DrawContext context, int x1, int y1, int x2, int y2, int size, float scale, float mouseX, float mouseY, LivingEntity entity) {
        float n = (float)(x1 + x2) / 2.0f;
        float o = (float)(y1 + y2) / 2.0f;
        context.enableScissor(x1, y1, x2, y2);
        float p = (float)Math.atan((n - mouseX) / 40.0f);
        float q = (float)Math.atan((o - mouseY) / 40.0f);
        Quaternionf quaternionf = new Quaternionf().rotateZ((float)Math.PI);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(q * 20.0f * ((float)Math.PI / 180));
        quaternionf.mul(quaternionf2);
        float r = entity.bodyYaw;
        float s = entity.getYaw();
        float t = entity.getPitch();
        float u = entity.lastHeadYaw;
        float v = entity.headYaw;
        entity.bodyYaw = 180.0f + p * 20.0f;
        entity.setYaw(180.0f + p * 40.0f);
        entity.setPitch(-q * 20.0f);
        entity.headYaw = entity.getYaw();
        entity.lastHeadYaw = entity.getYaw();
        float w = entity.getScale();
        Vector3f vector3f = new Vector3f(0.0f, entity.getHeight() / 2.0f + scale * w, 0.0f);
        float x = (float)size / w;
        InventoryScreen.drawEntity(context, x1, y1, x2, y2, x, vector3f, quaternionf, quaternionf2, entity);
        entity.bodyYaw = r;
        entity.setYaw(s);
        entity.setPitch(t);
        entity.lastHeadYaw = u;
        entity.headYaw = v;
        context.disableScissor();
    }

    public static void drawEntity(DrawContext drawer, int x1, int y1, int x2, int y2, float scale, Vector3f translation, Quaternionf rotation, @Nullable Quaternionf overrideCameraAngle, LivingEntity entity) {
        EntityRenderManager lv = MinecraftClient.getInstance().getEntityRenderDispatcher();
        EntityRenderer<?, LivingEntity> lv2 = lv.getRenderer(entity);
        LivingEntity lv3 = lv2.getAndUpdateRenderState(entity, 1.0f);
        ((EntityRenderState)((Object)lv3)).light = 0xF000F0;
        ((EntityRenderState)((Object)lv3)).hitbox = null;
        ((EntityRenderState)((Object)lv3)).shadowPieces.clear();
        ((EntityRenderState)((Object)lv3)).outlineColor = 0;
        drawer.addEntity((EntityRenderState)((Object)lv3), scale, translation, rotation, overrideCameraAngle, x1, y1, x2, y2);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (this.mouseDown) {
            this.mouseDown = false;
            return true;
        }
        return super.mouseReleased(click);
    }
}

