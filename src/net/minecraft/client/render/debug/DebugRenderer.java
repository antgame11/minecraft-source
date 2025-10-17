/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.debug.BeeDebugRenderer;
import net.minecraft.client.render.debug.BlockOutlineDebugRenderer;
import net.minecraft.client.render.debug.BrainDebugRenderer;
import net.minecraft.client.render.debug.BreezeDebugRenderer;
import net.minecraft.client.render.debug.ChunkBorderDebugRenderer;
import net.minecraft.client.render.debug.ChunkDebugRenderer;
import net.minecraft.client.render.debug.ChunkLoadingDebugRenderer;
import net.minecraft.client.render.debug.CollisionDebugRenderer;
import net.minecraft.client.render.debug.EntityBlockIntersectionsDebugRenderer;
import net.minecraft.client.render.debug.GameEventDebugRenderer;
import net.minecraft.client.render.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.render.debug.HeightmapDebugRenderer;
import net.minecraft.client.render.debug.LightDebugRenderer;
import net.minecraft.client.render.debug.NeighborUpdateDebugRenderer;
import net.minecraft.client.render.debug.OctreeDebugRenderer;
import net.minecraft.client.render.debug.PathfindingDebugRenderer;
import net.minecraft.client.render.debug.PoiDebugRenderer;
import net.minecraft.client.render.debug.RaidCenterDebugRenderer;
import net.minecraft.client.render.debug.RedstoneUpdateOrderDebugRenderer;
import net.minecraft.client.render.debug.SkyLightDebugRenderer;
import net.minecraft.client.render.debug.StructureDebugRenderer;
import net.minecraft.client.render.debug.SupportingBlockDebugRenderer;
import net.minecraft.client.render.debug.VillageSectionsDebugRenderer;
import net.minecraft.client.render.debug.WaterDebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.LightType;
import net.minecraft.world.debug.DebugDataStore;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DebugRenderer {
    private final List<Renderer> debugRenderers = new ArrayList<Renderer>();
    private final List<Renderer> lateDebugRenderers = new ArrayList<Renderer>();
    private long currentVersion;

    public DebugRenderer() {
        this.initRenderers();
    }

    public void initRenderers() {
        MinecraftClient lv = MinecraftClient.getInstance();
        this.debugRenderers.clear();
        this.lateDebugRenderers.clear();
        if (lv.debugHudEntryList.isEntryVisible(DebugHudEntries.CHUNK_BORDERS) && !lv.hasReducedDebugInfo()) {
            this.debugRenderers.add(new ChunkBorderDebugRenderer(lv));
        }
        if (lv.debugHudEntryList.isEntryVisible(DebugHudEntries.CHUNK_SECTION_OCTREE)) {
            this.debugRenderers.add(new OctreeDebugRenderer(lv));
        }
        if (SharedConstants.PATHFINDING) {
            this.debugRenderers.add(new PathfindingDebugRenderer());
        }
        if (SharedConstants.WATER) {
            this.debugRenderers.add(new WaterDebugRenderer(lv));
        }
        if (SharedConstants.HEIGHTMAP) {
            this.debugRenderers.add(new HeightmapDebugRenderer(lv));
        }
        if (SharedConstants.COLLISION) {
            this.debugRenderers.add(new CollisionDebugRenderer(lv));
        }
        if (SharedConstants.SUPPORT_BLOCKS) {
            this.debugRenderers.add(new SupportingBlockDebugRenderer(lv));
        }
        if (SharedConstants.NEIGHBORSUPDATE) {
            this.debugRenderers.add(new NeighborUpdateDebugRenderer());
        }
        if (SharedConstants.EXPERIMENTAL_REDSTONEWIRE_UPDATE_ORDER) {
            this.debugRenderers.add(new RedstoneUpdateOrderDebugRenderer());
        }
        if (SharedConstants.STRUCTURES) {
            this.debugRenderers.add(new StructureDebugRenderer());
        }
        if (SharedConstants.LIGHT) {
            this.debugRenderers.add(new SkyLightDebugRenderer(lv));
        }
        if (SharedConstants.SOLID_FACE) {
            this.debugRenderers.add(new BlockOutlineDebugRenderer(lv));
        }
        if (SharedConstants.VILLAGE_SECTIONS) {
            this.debugRenderers.add(new VillageSectionsDebugRenderer());
        }
        if (SharedConstants.BRAIN) {
            this.debugRenderers.add(new BrainDebugRenderer(lv));
        }
        if (SharedConstants.POI) {
            this.debugRenderers.add(new PoiDebugRenderer(new BrainDebugRenderer(lv)));
        }
        if (SharedConstants.BEES) {
            this.debugRenderers.add(new BeeDebugRenderer(lv));
        }
        if (SharedConstants.RAIDS) {
            this.debugRenderers.add(new RaidCenterDebugRenderer(lv));
        }
        if (SharedConstants.GOAL_SELECTOR) {
            this.debugRenderers.add(new GoalSelectorDebugRenderer(lv));
        }
        if (SharedConstants.CHUNKS) {
            this.debugRenderers.add(new ChunkLoadingDebugRenderer(lv));
        }
        if (SharedConstants.GAME_EVENT_LISTENERS) {
            this.debugRenderers.add(new GameEventDebugRenderer());
        }
        if (SharedConstants.SKY_LIGHT_SECTIONS) {
            this.debugRenderers.add(new LightDebugRenderer(lv, LightType.SKY));
        }
        if (SharedConstants.BREEZE_MOB) {
            this.debugRenderers.add(new BreezeDebugRenderer(lv));
        }
        if (SharedConstants.ENTITY_BLOCK_INTERSECTION) {
            this.debugRenderers.add(new EntityBlockIntersectionsDebugRenderer());
        }
        this.lateDebugRenderers.add(new ChunkDebugRenderer(lv));
    }

    public void render(MatrixStack matrices, Frustum frustum, VertexConsumerProvider.Immediate vertexConsumers, double cameraX, double cameraY, double cameraZ, boolean lateDebug) {
        MinecraftClient lv = MinecraftClient.getInstance();
        DebugDataStore lv2 = lv.getNetworkHandler().getDebugDataStore();
        if (lv.debugHudEntryList.getVersion() != this.currentVersion) {
            this.currentVersion = lv.debugHudEntryList.getVersion();
            this.initRenderers();
        }
        List<Renderer> list = lateDebug ? this.lateDebugRenderers : this.debugRenderers;
        for (Renderer lv3 : list) {
            lv3.render(matrices, vertexConsumers, cameraX, cameraY, cameraZ, lv2, frustum);
        }
    }

    public static Optional<Entity> getTargetedEntity(@Nullable Entity entity, int maxDistance) {
        int j;
        Box lv4;
        Vec3d lv2;
        Vec3d lv3;
        if (entity == null) {
            return Optional.empty();
        }
        Vec3d lv = entity.getEyePos();
        EntityHitResult lv5 = ProjectileUtil.raycast(entity, lv, lv3 = lv.add(lv2 = entity.getRotationVec(1.0f).multiply(maxDistance)), lv4 = entity.getBoundingBox().stretch(lv2).expand(1.0), EntityPredicates.CAN_HIT, j = maxDistance * maxDistance);
        if (lv5 == null) {
            return Optional.empty();
        }
        if (lv.squaredDistanceTo(lv5.getPos()) > (double)j) {
            return Optional.empty();
        }
        return Optional.of(lv5.getEntity());
    }

    public static void drawBlockBox(MatrixStack matrices, VertexConsumerProvider vertexConsumers, BlockPos pos, float red, float green, float blue, float alpha) {
        DebugRenderer.drawBox(matrices, vertexConsumers, pos, pos.add(1, 1, 1), red, green, blue, alpha);
    }

    public static void drawBox(MatrixStack matrices, VertexConsumerProvider vertexConsumers, BlockPos pos1, BlockPos pos2, float red, float green, float blue, float alpha) {
        Camera lv = MinecraftClient.getInstance().gameRenderer.getCamera();
        if (!lv.isReady()) {
            return;
        }
        Vec3d lv2 = lv.getPos().negate();
        Box lv3 = Box.enclosing(pos1, pos2).offset(lv2);
        DebugRenderer.drawBox(matrices, vertexConsumers, lv3, red, green, blue, alpha);
    }

    public static void drawBox(MatrixStack matrices, VertexConsumerProvider vertexConsumers, BlockPos pos, float expand, float red, float green, float blue, float alpha) {
        Camera lv = MinecraftClient.getInstance().gameRenderer.getCamera();
        if (!lv.isReady()) {
            return;
        }
        Vec3d lv2 = lv.getPos().negate();
        Box lv3 = new Box(pos).offset(lv2).expand(expand);
        DebugRenderer.drawBox(matrices, vertexConsumers, lv3, red, green, blue, alpha);
    }

    public static void drawBox(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Box box, float red, float green, float blue, float alpha) {
        DebugRenderer.drawBox(matrices, vertexConsumers, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha);
    }

    public static void drawBox(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float red, float green, float blue, float alpha) {
        VertexConsumer lv = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());
        VertexRendering.drawFilledBox(matrices, lv, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
    }

    public static void drawFloatingText(MatrixStack matrices, VertexConsumerProvider vertexConsumers, String string, BlockPos pos, int lineNumber, int color, float size) {
        double d = 1.3;
        double e = 0.2;
        double g = (double)pos.getX() + 0.5;
        double h = (double)pos.getY() + 1.3 + (double)lineNumber * 0.2;
        double k = (double)pos.getZ() + 0.5;
        DebugRenderer.drawString(matrices, vertexConsumers, string, g, h, k, color, size, true, 0.0f, true);
    }

    public static void drawLargeFloatingText(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity pos, int lineNumber, String string, int color, float size) {
        double d = 2.4;
        double e = 0.25;
        double g = (double)pos.getBlockX() + 0.5;
        double h = pos.getY() + 2.4 + (double)lineNumber * 0.25;
        double k = (double)pos.getBlockZ() + 0.5;
        float l = 0.5f;
        DebugRenderer.drawString(matrices, vertexConsumers, string, g, h, k, color, size, false, 0.5f, true);
    }

    public static void drawString(MatrixStack matrices, VertexConsumerProvider vertexConsumers, String string, int x, int y, int z, int color) {
        DebugRenderer.drawString(matrices, vertexConsumers, string, (double)x + 0.5, (double)y + 0.5, (double)z + 0.5, color);
    }

    public static void drawString(MatrixStack matrices, VertexConsumerProvider vertexConsumers, String string, double x, double y, double z, int color) {
        DebugRenderer.drawString(matrices, vertexConsumers, string, x, y, z, color, 0.02f);
    }

    public static void drawString(MatrixStack matrices, VertexConsumerProvider vertexConsumers, String string, double x, double y, double z, int color, float size) {
        DebugRenderer.drawString(matrices, vertexConsumers, string, x, y, z, color, size, true, 0.0f, false);
    }

    public static void drawString(MatrixStack matrices, VertexConsumerProvider vertexConsumers, String string, double x, double y, double z, int color, float size, boolean center, float offset, boolean visibleThroughObjects) {
        MinecraftClient lv = MinecraftClient.getInstance();
        Camera lv2 = lv.gameRenderer.getCamera();
        if (!lv2.isReady() || lv.getEntityRenderDispatcher().gameOptions == null) {
            return;
        }
        TextRenderer lv3 = lv.textRenderer;
        double j = lv2.getPos().x;
        double k = lv2.getPos().y;
        double l = lv2.getPos().z;
        matrices.push();
        matrices.translate((float)(x - j), (float)(y - k) + 0.07f, (float)(z - l));
        matrices.multiply(lv2.getRotation());
        matrices.scale(size, -size, size);
        float m = center ? (float)(-lv3.getWidth(string)) / 2.0f : 0.0f;
        lv3.draw(string, m -= offset / size, 0.0f, color, false, matrices.peek().getPositionMatrix(), vertexConsumers, visibleThroughObjects ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        matrices.pop();
    }

    private static Vec3d hueToRgb(float hue) {
        float g = 5.99999f;
        int i = (int)(MathHelper.clamp(hue, 0.0f, 1.0f) * 5.99999f);
        float h = hue * 5.99999f - (float)i;
        return switch (i) {
            case 0 -> new Vec3d(1.0, h, 0.0);
            case 1 -> new Vec3d(1.0f - h, 1.0, 0.0);
            case 2 -> new Vec3d(0.0, 1.0, h);
            case 3 -> new Vec3d(0.0, 1.0 - (double)h, 1.0);
            case 4 -> new Vec3d(h, 0.0, 1.0);
            case 5 -> new Vec3d(1.0, 0.0, 1.0 - (double)h);
            default -> throw new IllegalStateException("Unexpected value: " + i);
        };
    }

    private static Vec3d shiftHue(float r, float g, float b, float dHue) {
        Vec3d lv = DebugRenderer.hueToRgb(dHue).multiply(r);
        Vec3d lv2 = DebugRenderer.hueToRgb((dHue + 0.33333334f) % 1.0f).multiply(g);
        Vec3d lv3 = DebugRenderer.hueToRgb((dHue + 0.6666667f) % 1.0f).multiply(b);
        Vec3d lv4 = lv.add(lv2).add(lv3);
        double d = Math.max(Math.max(1.0, lv4.x), Math.max(lv4.y, lv4.z));
        return new Vec3d(lv4.x / d, lv4.y / d, lv4.z / d);
    }

    public static void drawVoxelShapeOutlines(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape shape, double offsetX, double offsetY, double offsetZ, float red, float green, float blue, float alpha, boolean bl) {
        List<Box> list = shape.getBoundingBoxes();
        if (list.isEmpty()) {
            return;
        }
        int k = bl ? list.size() : list.size() * 8;
        VertexRendering.drawOutline(matrices, vertexConsumer, VoxelShapes.cuboid(list.get(0)), offsetX, offsetY, offsetZ, ColorHelper.fromFloats(alpha, red, green, blue));
        for (int l = 1; l < list.size(); ++l) {
            Box lv = list.get(l);
            float m = (float)l / (float)k;
            Vec3d lv2 = DebugRenderer.shiftHue(red, green, blue, m);
            VertexRendering.drawOutline(matrices, vertexConsumer, VoxelShapes.cuboid(lv), offsetX, offsetY, offsetZ, ColorHelper.fromFloats(alpha, (float)lv2.x, (float)lv2.y, (float)lv2.z));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Renderer {
        public void render(MatrixStack var1, VertexConsumerProvider var2, double var3, double var5, double var7, DebugDataStore var9, Frustum var10);
    }
}

