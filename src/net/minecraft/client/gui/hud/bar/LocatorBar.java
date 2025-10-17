/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.hud.bar;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.bar.Bar;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.resource.waypoint.WaypointStyleAsset;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.tick.TickManager;
import net.minecraft.world.waypoint.EntityTickProgress;
import net.minecraft.world.waypoint.TrackedWaypoint;
import net.minecraft.world.waypoint.Waypoint;

@Environment(value=EnvType.CLIENT)
public class LocatorBar
implements Bar {
    private static final Identifier BACKGROUND = Identifier.ofVanilla("hud/locator_bar_background");
    private static final Identifier ARROW_UP = Identifier.ofVanilla("hud/locator_bar_arrow_up");
    private static final Identifier ARROW_DOWN = Identifier.ofVanilla("hud/locator_bar_arrow_down");
    private static final int field_59852 = 9;
    private static final int field_59854 = 60;
    private static final int field_59855 = 7;
    private static final int field_59856 = 5;
    private static final int field_60309 = 1;
    private static final int field_60453 = 1;
    private final MinecraftClient client;

    public LocatorBar(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void renderBar(DrawContext context, RenderTickCounter tickCounter) {
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.getCenterX(this.client.getWindow()), this.getCenterY(this.client.getWindow()), 182, 5);
    }

    @Override
    public void renderAddons(DrawContext context, RenderTickCounter tickCounter) {
        int i = this.getCenterY(this.client.getWindow());
        Entity lv = this.client.getCameraEntity();
        if (lv == null) {
            return;
        }
        World lv2 = lv.getEntityWorld();
        TickManager lv3 = lv2.getTickManager();
        EntityTickProgress lv4 = entity -> tickCounter.getTickProgress(!lv3.shouldSkipTick(entity));
        this.client.player.networkHandler.getWaypointHandler().forEachWaypoint(lv, waypoint -> {
            if (waypoint.getSource().left().map(uuid -> uuid.equals(lv.getUuid())).orElse(false).booleanValue()) {
                return;
            }
            double d = waypoint.getRelativeYaw(lv2, this.client.gameRenderer.getCamera(), lv4);
            if (d <= -60.0 || d > 60.0) {
                return;
            }
            int j = MathHelper.ceil((float)(context.getScaledWindowWidth() - 9) / 2.0f);
            Waypoint.Config lv = waypoint.getConfig();
            WaypointStyleAsset lv2 = this.client.getWaypointStyleAssetManager().get(lv.style);
            float f = MathHelper.sqrt((float)waypoint.squaredDistanceTo(lv));
            Identifier lv3 = lv2.getSpriteForDistance(f);
            int k = lv.color.orElseGet(() -> waypoint.getSource().map(uuid -> ColorHelper.withBrightness(ColorHelper.withAlpha(255, uuid.hashCode()), 0.9f), name -> ColorHelper.withBrightness(ColorHelper.withAlpha(255, name.hashCode()), 0.9f)));
            int l = MathHelper.floor(d * 173.0 / 2.0 / 60.0);
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv3, j + l, i - 2, 9, 9, k);
            TrackedWaypoint.Pitch lv4 = waypoint.getPitch(lv2, this.client.gameRenderer, lv4);
            if (lv4 != TrackedWaypoint.Pitch.NONE) {
                Identifier lv5;
                int m;
                if (lv4 == TrackedWaypoint.Pitch.DOWN) {
                    m = 6;
                    lv5 = ARROW_DOWN;
                } else {
                    m = -6;
                    lv5 = ARROW_UP;
                }
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv5, j + l + 1, i + m, 7, 5);
            }
        });
    }
}

