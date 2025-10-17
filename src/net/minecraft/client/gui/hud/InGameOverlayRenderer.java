/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.hud;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class InGameOverlayRenderer {
    private static final Identifier UNDERWATER_TEXTURE = Identifier.ofVanilla("textures/misc/underwater.png");
    private final MinecraftClient client;
    private final SpriteHolder spriteHolder;
    private final VertexConsumerProvider vertexConsumers;
    public static final int field_59969 = 40;
    @Nullable
    private ItemStack floatingItem;
    private int floatingItemTimer;
    private float floatingItemOffsetX;
    private float floatingItemOffsetY;

    public InGameOverlayRenderer(MinecraftClient client, SpriteHolder spriteHolder, VertexConsumerProvider vertexConsumers) {
        this.client = client;
        this.spriteHolder = spriteHolder;
        this.vertexConsumers = vertexConsumers;
    }

    public void tickFloatingItemTimer() {
        if (this.floatingItemTimer > 0) {
            --this.floatingItemTimer;
            if (this.floatingItemTimer == 0) {
                this.floatingItem = null;
            }
        }
    }

    public void renderOverlays(boolean sleeping, float tickProgress, OrderedRenderCommandQueue queue) {
        MatrixStack lv = new MatrixStack();
        ClientPlayerEntity lv2 = this.client.player;
        if (this.client.options.getPerspective().isFirstPerson() && !sleeping) {
            BlockState lv3;
            if (!lv2.noClip && (lv3 = InGameOverlayRenderer.getInWallBlockState(lv2)) != null) {
                InGameOverlayRenderer.renderInWallOverlay(this.client.getBlockRenderManager().getModels().getModelParticleSprite(lv3), lv, this.vertexConsumers);
            }
            if (!this.client.player.isSpectator()) {
                if (this.client.player.isSubmergedIn(FluidTags.WATER)) {
                    InGameOverlayRenderer.renderUnderwaterOverlay(this.client, lv, this.vertexConsumers);
                }
                if (this.client.player.isOnFire()) {
                    Sprite lv4 = this.spriteHolder.getSprite(ModelBaker.FIRE_1);
                    InGameOverlayRenderer.renderFireOverlay(lv, this.vertexConsumers, lv4);
                }
            }
        }
        if (!this.client.options.hudHidden) {
            this.renderFloatingItem(lv, tickProgress, queue);
        }
    }

    private void renderFloatingItem(MatrixStack matrices, float tickProgress, OrderedRenderCommandQueue queue) {
        if (this.floatingItem == null || this.floatingItemTimer <= 0) {
            return;
        }
        int i = 40 - this.floatingItemTimer;
        float g = ((float)i + tickProgress) / 40.0f;
        float h = g * g;
        float j = g * h;
        float k = 10.25f * j * h - 24.95f * h * h + 25.5f * j - 13.8f * h + 4.0f * g;
        float l = k * (float)Math.PI;
        float m = (float)this.client.getWindow().getFramebufferWidth() / (float)this.client.getWindow().getFramebufferHeight();
        float n = this.floatingItemOffsetX * 0.3f * m;
        float o = this.floatingItemOffsetY * 0.3f;
        matrices.push();
        matrices.translate(n * MathHelper.abs(MathHelper.sin(l * 2.0f)), o * MathHelper.abs(MathHelper.sin(l * 2.0f)), -10.0f + 9.0f * MathHelper.sin(l));
        float p = 0.8f;
        matrices.scale(0.8f, 0.8f, 0.8f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(900.0f * MathHelper.abs(MathHelper.sin(l))));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0f * MathHelper.cos(g * 8.0f)));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(6.0f * MathHelper.cos(g * 8.0f)));
        this.client.gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ITEMS_3D);
        ItemRenderState lv = new ItemRenderState();
        this.client.getItemModelManager().clearAndUpdate(lv, this.floatingItem, ItemDisplayContext.FIXED, this.client.world, null, 0);
        lv.render(matrices, queue, 0xF000F0, OverlayTexture.DEFAULT_UV, 0);
        matrices.pop();
    }

    public void clearFloatingItem() {
        this.floatingItem = null;
    }

    public void setFloatingItem(ItemStack stack, Random random) {
        this.floatingItem = stack;
        this.floatingItemTimer = 40;
        this.floatingItemOffsetX = random.nextFloat() * 2.0f - 1.0f;
        this.floatingItemOffsetY = random.nextFloat() * 2.0f - 1.0f;
    }

    @Nullable
    private static BlockState getInWallBlockState(PlayerEntity player) {
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (int i = 0; i < 8; ++i) {
            double d = player.getX() + (double)(((float)((i >> 0) % 2) - 0.5f) * player.getWidth() * 0.8f);
            double e = player.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5f) * 0.1f * player.getScale());
            double f = player.getZ() + (double)(((float)((i >> 2) % 2) - 0.5f) * player.getWidth() * 0.8f);
            lv.set(d, e, f);
            BlockState lv2 = player.getEntityWorld().getBlockState(lv);
            if (lv2.getRenderType() == BlockRenderType.INVISIBLE || !lv2.shouldBlockVision(player.getEntityWorld(), lv)) continue;
            return lv2;
        }
        return null;
    }

    private static void renderInWallOverlay(Sprite sprite, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        float f = 0.1f;
        int i = ColorHelper.fromFloats(1.0f, 0.1f, 0.1f, 0.1f);
        float g = -1.0f;
        float h = 1.0f;
        float j = -1.0f;
        float k = 1.0f;
        float l = -0.5f;
        float m = sprite.getMinU();
        float n = sprite.getMaxU();
        float o = sprite.getMinV();
        float p = sprite.getMaxV();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        VertexConsumer lv = vertexConsumers.getBuffer(RenderLayer.getBlockScreenEffect(sprite.getAtlasId()));
        lv.vertex(matrix4f, -1.0f, -1.0f, -0.5f).texture(n, p).color(i);
        lv.vertex(matrix4f, 1.0f, -1.0f, -0.5f).texture(m, p).color(i);
        lv.vertex(matrix4f, 1.0f, 1.0f, -0.5f).texture(m, o).color(i);
        lv.vertex(matrix4f, -1.0f, 1.0f, -0.5f).texture(n, o).color(i);
    }

    private static void renderUnderwaterOverlay(MinecraftClient client, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        BlockPos lv = BlockPos.ofFloored(client.player.getX(), client.player.getEyeY(), client.player.getZ());
        float f = LightmapTextureManager.getBrightness(client.player.getEntityWorld().getDimension(), client.player.getEntityWorld().getLightLevel(lv));
        int i = ColorHelper.fromFloats(0.1f, f, f, f);
        float g = 4.0f;
        float h = -1.0f;
        float j = 1.0f;
        float k = -1.0f;
        float l = 1.0f;
        float m = -0.5f;
        float n = -client.player.getYaw() / 64.0f;
        float o = client.player.getPitch() / 64.0f;
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        VertexConsumer lv2 = vertexConsumers.getBuffer(RenderLayer.getBlockScreenEffect(UNDERWATER_TEXTURE));
        lv2.vertex(matrix4f, -1.0f, -1.0f, -0.5f).texture(4.0f + n, 4.0f + o).color(i);
        lv2.vertex(matrix4f, 1.0f, -1.0f, -0.5f).texture(0.0f + n, 4.0f + o).color(i);
        lv2.vertex(matrix4f, 1.0f, 1.0f, -0.5f).texture(0.0f + n, 0.0f + o).color(i);
        lv2.vertex(matrix4f, -1.0f, 1.0f, -0.5f).texture(4.0f + n, 0.0f + o).color(i);
    }

    private static void renderFireOverlay(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Sprite sprite) {
        VertexConsumer lv = vertexConsumers.getBuffer(RenderLayer.getFireScreenEffect(sprite.getAtlasId()));
        float f = sprite.getMinU();
        float g = sprite.getMaxU();
        float h = (f + g) / 2.0f;
        float i = sprite.getMinV();
        float j = sprite.getMaxV();
        float k = (i + j) / 2.0f;
        float l = sprite.getUvScaleDelta();
        float m = MathHelper.lerp(l, f, h);
        float n = MathHelper.lerp(l, g, h);
        float o = MathHelper.lerp(l, i, k);
        float p = MathHelper.lerp(l, j, k);
        float q = 1.0f;
        for (int r = 0; r < 2; ++r) {
            matrices.push();
            float s = -0.5f;
            float t = 0.5f;
            float u = -0.5f;
            float v = 0.5f;
            float w = -0.5f;
            matrices.translate((float)(-(r * 2 - 1)) * 0.24f, -0.3f, 0.0f);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(r * 2 - 1) * 10.0f));
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            lv.vertex(matrix4f, -0.5f, -0.5f, -0.5f).texture(n, p).color(1.0f, 1.0f, 1.0f, 0.9f);
            lv.vertex(matrix4f, 0.5f, -0.5f, -0.5f).texture(m, p).color(1.0f, 1.0f, 1.0f, 0.9f);
            lv.vertex(matrix4f, 0.5f, 0.5f, -0.5f).texture(m, o).color(1.0f, 1.0f, 1.0f, 0.9f);
            lv.vertex(matrix4f, -0.5f, 0.5f, -0.5f).texture(n, o).color(1.0f, 1.0f, 1.0f, 0.9f);
            matrices.pop();
        }
    }
}

