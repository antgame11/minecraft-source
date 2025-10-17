/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.RenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.BlockDisplayEntityRenderState;
import net.minecraft.client.render.entity.state.DisplayEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.ItemDisplayEntityRenderState;
import net.minecraft.client.render.entity.state.TextDisplayEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@Environment(value=EnvType.CLIENT)
public abstract class DisplayEntityRenderer<T extends DisplayEntity, S, ST extends DisplayEntityRenderState>
extends EntityRenderer<T, ST> {
    private final EntityRenderManager renderDispatcher;

    protected DisplayEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
        this.renderDispatcher = arg.getRenderDispatcher();
    }

    @Override
    protected Box getBoundingBox(T arg) {
        return ((DisplayEntity)arg).getVisibilityBoundingBox();
    }

    @Override
    protected boolean canBeCulled(T arg) {
        return ((DisplayEntity)arg).shouldRender();
    }

    private static int getBrightnessOverride(DisplayEntity entity) {
        DisplayEntity.RenderState lv = entity.getRenderState();
        return lv != null ? lv.brightnessOverride() : -1;
    }

    @Override
    protected int getSkyLight(T arg, BlockPos arg2) {
        int i = DisplayEntityRenderer.getBrightnessOverride(arg);
        if (i != -1) {
            return LightmapTextureManager.getSkyLightCoordinates(i);
        }
        return super.getSkyLight(arg, arg2);
    }

    @Override
    protected int getBlockLight(T arg, BlockPos arg2) {
        int i = DisplayEntityRenderer.getBrightnessOverride(arg);
        if (i != -1) {
            return LightmapTextureManager.getBlockLightCoordinates(i);
        }
        return super.getBlockLight(arg, arg2);
    }

    @Override
    protected float getShadowRadius(ST arg) {
        DisplayEntity.RenderState lv = ((DisplayEntityRenderState)arg).displayRenderState;
        if (lv == null) {
            return 0.0f;
        }
        return lv.shadowRadius().lerp(((DisplayEntityRenderState)arg).lerpProgress);
    }

    @Override
    protected float getShadowOpacity(ST arg) {
        DisplayEntity.RenderState lv = ((DisplayEntityRenderState)arg).displayRenderState;
        if (lv == null) {
            return 0.0f;
        }
        return lv.shadowStrength().lerp(((DisplayEntityRenderState)arg).lerpProgress);
    }

    @Override
    public void render(ST arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        DisplayEntity.RenderState lv = ((DisplayEntityRenderState)arg).displayRenderState;
        if (lv == null || !((DisplayEntityRenderState)arg).canRender()) {
            return;
        }
        float f = ((DisplayEntityRenderState)arg).lerpProgress;
        super.render(arg, arg2, arg3, arg4);
        arg2.push();
        arg2.multiply(this.getBillboardRotation(lv, arg, new Quaternionf()));
        AffineTransformation lv2 = lv.transformation().interpolate(f);
        arg2.multiplyPositionMatrix(lv2.getMatrix());
        this.render(arg, arg2, arg3, ((DisplayEntityRenderState)arg).light, f);
        arg2.pop();
    }

    private Quaternionf getBillboardRotation(DisplayEntity.RenderState renderState, ST state, Quaternionf rotation) {
        return switch (renderState.billboardConstraints()) {
            default -> throw new MatchException(null, null);
            case DisplayEntity.BillboardMode.FIXED -> rotation.rotationYXZ((float)(-Math.PI) / 180 * ((DisplayEntityRenderState)state).yaw, (float)Math.PI / 180 * ((DisplayEntityRenderState)state).pitch, 0.0f);
            case DisplayEntity.BillboardMode.HORIZONTAL -> rotation.rotationYXZ((float)(-Math.PI) / 180 * ((DisplayEntityRenderState)state).yaw, (float)Math.PI / 180 * DisplayEntityRenderer.getNegatedPitch(((DisplayEntityRenderState)state).cameraPitch), 0.0f);
            case DisplayEntity.BillboardMode.VERTICAL -> rotation.rotationYXZ((float)(-Math.PI) / 180 * DisplayEntityRenderer.getBackwardsYaw(((DisplayEntityRenderState)state).cameraYaw), (float)Math.PI / 180 * ((DisplayEntityRenderState)state).pitch, 0.0f);
            case DisplayEntity.BillboardMode.CENTER -> rotation.rotationYXZ((float)(-Math.PI) / 180 * DisplayEntityRenderer.getBackwardsYaw(((DisplayEntityRenderState)state).cameraYaw), (float)Math.PI / 180 * DisplayEntityRenderer.getNegatedPitch(((DisplayEntityRenderState)state).cameraPitch), 0.0f);
        };
    }

    private static float getBackwardsYaw(float yaw) {
        return yaw - 180.0f;
    }

    private static float getNegatedPitch(float pitch) {
        return -pitch;
    }

    private static <T extends DisplayEntity> float lerpYaw(T entity, float deltaTicks) {
        return entity.getLerpedYaw(deltaTicks);
    }

    private static <T extends DisplayEntity> float lerpPitch(T entity, float deltaTicks) {
        return entity.getLerpedPitch(deltaTicks);
    }

    protected abstract void render(ST var1, MatrixStack var2, OrderedRenderCommandQueue var3, int var4, float var5);

    @Override
    public void updateRenderState(T arg, ST arg2, float f) {
        super.updateRenderState(arg, arg2, f);
        ((DisplayEntityRenderState)arg2).displayRenderState = ((DisplayEntity)arg).getRenderState();
        ((DisplayEntityRenderState)arg2).lerpProgress = ((DisplayEntity)arg).getLerpProgress(f);
        ((DisplayEntityRenderState)arg2).yaw = DisplayEntityRenderer.lerpYaw(arg, f);
        ((DisplayEntityRenderState)arg2).pitch = DisplayEntityRenderer.lerpPitch(arg, f);
        Camera lv = this.renderDispatcher.camera;
        ((DisplayEntityRenderState)arg2).cameraPitch = lv.getPitch();
        ((DisplayEntityRenderState)arg2).cameraYaw = lv.getYaw();
    }

    @Override
    protected /* synthetic */ float getShadowRadius(EntityRenderState state) {
        return this.getShadowRadius((ST)((DisplayEntityRenderState)state));
    }

    @Override
    protected /* synthetic */ int getBlockLight(Entity entity, BlockPos pos) {
        return this.getBlockLight((T)((DisplayEntity)entity), pos);
    }

    @Override
    protected /* synthetic */ int getSkyLight(Entity entity, BlockPos pos) {
        return this.getSkyLight((T)((DisplayEntity)entity), pos);
    }

    @Environment(value=EnvType.CLIENT)
    public static class TextDisplayEntityRenderer
    extends DisplayEntityRenderer<DisplayEntity.TextDisplayEntity, DisplayEntity.TextDisplayEntity.Data, TextDisplayEntityRenderState> {
        private final TextRenderer displayTextRenderer;

        protected TextDisplayEntityRenderer(EntityRendererFactory.Context arg) {
            super(arg);
            this.displayTextRenderer = arg.getTextRenderer();
        }

        @Override
        public TextDisplayEntityRenderState createRenderState() {
            return new TextDisplayEntityRenderState();
        }

        @Override
        public void updateRenderState(DisplayEntity.TextDisplayEntity arg, TextDisplayEntityRenderState arg2, float f) {
            super.updateRenderState(arg, arg2, f);
            arg2.data = arg.getData();
            arg2.textLines = arg.splitLines(this::getLines);
        }

        private DisplayEntity.TextDisplayEntity.TextLines getLines(Text text, int width) {
            List<OrderedText> list = this.displayTextRenderer.wrapLines(text, width);
            ArrayList<DisplayEntity.TextDisplayEntity.TextLine> list2 = new ArrayList<DisplayEntity.TextDisplayEntity.TextLine>(list.size());
            int j = 0;
            for (OrderedText lv : list) {
                int k = this.displayTextRenderer.getWidth(lv);
                j = Math.max(j, k);
                list2.add(new DisplayEntity.TextDisplayEntity.TextLine(lv, k));
            }
            return new DisplayEntity.TextDisplayEntity.TextLines(list2, j);
        }

        @Override
        public void render(TextDisplayEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, int i, float f) {
            int j;
            float g;
            DisplayEntity.TextDisplayEntity.Data lv = arg.data;
            byte b = lv.flags();
            boolean bl = (b & DisplayEntity.TextDisplayEntity.SEE_THROUGH_FLAG) != 0;
            boolean bl2 = (b & DisplayEntity.TextDisplayEntity.DEFAULT_BACKGROUND_FLAG) != 0;
            boolean bl3 = (b & DisplayEntity.TextDisplayEntity.SHADOW_FLAG) != 0;
            DisplayEntity.TextDisplayEntity.TextAlignment lv2 = DisplayEntity.TextDisplayEntity.getAlignment(b);
            byte c = (byte)lv.textOpacity().lerp(f);
            if (bl2) {
                g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25f);
                j = (int)(g * 255.0f) << 24;
            } else {
                j = lv.backgroundColor().lerp(f);
            }
            g = 0.0f;
            Matrix4f matrix4f = arg2.peek().getPositionMatrix();
            matrix4f.rotate((float)Math.PI, 0.0f, 1.0f, 0.0f);
            matrix4f.scale(-0.025f, -0.025f, -0.025f);
            DisplayEntity.TextDisplayEntity.TextLines lv3 = arg.textLines;
            boolean k = true;
            int l = this.displayTextRenderer.fontHeight + 1;
            int m = lv3.width();
            int n = lv3.lines().size() * l - 1;
            matrix4f.translate(1.0f - (float)m / 2.0f, -n, 0.0f);
            if (j != 0) {
                arg3.submitCustom(arg2, bl ? RenderLayer.getTextBackgroundSeeThrough() : RenderLayer.getTextBackground(), (matricesEntry, vertexConsumer) -> {
                    vertexConsumer.vertex(matricesEntry, -1.0f, -1.0f, 0.0f).color(j).light(i);
                    vertexConsumer.vertex(matricesEntry, -1.0f, (float)n, 0.0f).color(j).light(i);
                    vertexConsumer.vertex(matricesEntry, (float)m, (float)n, 0.0f).color(j).light(i);
                    vertexConsumer.vertex(matricesEntry, (float)m, -1.0f, 0.0f).color(j).light(i);
                });
            }
            RenderCommandQueue lv4 = arg3.getBatchingQueue(j != 0 ? 1 : 0);
            for (DisplayEntity.TextDisplayEntity.TextLine lv5 : lv3.lines()) {
                float h = switch (lv2) {
                    default -> throw new MatchException(null, null);
                    case DisplayEntity.TextDisplayEntity.TextAlignment.LEFT -> 0.0f;
                    case DisplayEntity.TextDisplayEntity.TextAlignment.RIGHT -> m - lv5.width();
                    case DisplayEntity.TextDisplayEntity.TextAlignment.CENTER -> (float)m / 2.0f - (float)lv5.width() / 2.0f;
                };
                lv4.submitText(arg2, h, g, lv5.contents(), bl3, bl ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.POLYGON_OFFSET, i, c << 24 | 0xFFFFFF, 0, 0);
                g += (float)l;
            }
        }

        @Override
        public /* synthetic */ EntityRenderState createRenderState() {
            return this.createRenderState();
        }

        @Override
        protected /* synthetic */ float getShadowRadius(EntityRenderState state) {
            return super.getShadowRadius((DisplayEntityRenderState)state);
        }

        @Override
        protected /* synthetic */ int getBlockLight(Entity entity, BlockPos pos) {
            return super.getBlockLight((DisplayEntity)entity, pos);
        }

        @Override
        protected /* synthetic */ int getSkyLight(Entity entity, BlockPos pos) {
            return super.getSkyLight((DisplayEntity)entity, pos);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class ItemDisplayEntityRenderer
    extends DisplayEntityRenderer<DisplayEntity.ItemDisplayEntity, DisplayEntity.ItemDisplayEntity.Data, ItemDisplayEntityRenderState> {
        private final ItemModelManager itemModelManager;

        protected ItemDisplayEntityRenderer(EntityRendererFactory.Context arg) {
            super(arg);
            this.itemModelManager = arg.getItemModelManager();
        }

        @Override
        public ItemDisplayEntityRenderState createRenderState() {
            return new ItemDisplayEntityRenderState();
        }

        @Override
        public void updateRenderState(DisplayEntity.ItemDisplayEntity arg, ItemDisplayEntityRenderState arg2, float f) {
            super.updateRenderState(arg, arg2, f);
            DisplayEntity.ItemDisplayEntity.Data lv = arg.getData();
            if (lv != null) {
                this.itemModelManager.updateForNonLivingEntity(arg2.itemRenderState, lv.itemStack(), lv.itemTransform(), arg);
            } else {
                arg2.itemRenderState.clear();
            }
        }

        @Override
        public void render(ItemDisplayEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, int i, float f) {
            if (arg.itemRenderState.isEmpty()) {
                return;
            }
            arg2.multiply(RotationAxis.POSITIVE_Y.rotation((float)Math.PI));
            arg.itemRenderState.render(arg2, arg3, i, OverlayTexture.DEFAULT_UV, arg.outlineColor);
        }

        @Override
        public /* synthetic */ EntityRenderState createRenderState() {
            return this.createRenderState();
        }

        @Override
        protected /* synthetic */ float getShadowRadius(EntityRenderState state) {
            return super.getShadowRadius((DisplayEntityRenderState)state);
        }

        @Override
        protected /* synthetic */ int getBlockLight(Entity entity, BlockPos pos) {
            return super.getBlockLight((DisplayEntity)entity, pos);
        }

        @Override
        protected /* synthetic */ int getSkyLight(Entity entity, BlockPos pos) {
            return super.getSkyLight((DisplayEntity)entity, pos);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class BlockDisplayEntityRenderer
    extends DisplayEntityRenderer<DisplayEntity.BlockDisplayEntity, DisplayEntity.BlockDisplayEntity.Data, BlockDisplayEntityRenderState> {
        protected BlockDisplayEntityRenderer(EntityRendererFactory.Context arg) {
            super(arg);
        }

        @Override
        public BlockDisplayEntityRenderState createRenderState() {
            return new BlockDisplayEntityRenderState();
        }

        @Override
        public void updateRenderState(DisplayEntity.BlockDisplayEntity arg, BlockDisplayEntityRenderState arg2, float f) {
            super.updateRenderState(arg, arg2, f);
            arg2.data = arg.getData();
        }

        @Override
        public void render(BlockDisplayEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, int i, float f) {
            arg3.submitBlock(arg2, arg.data.blockState(), i, OverlayTexture.DEFAULT_UV, arg.outlineColor);
        }

        @Override
        public /* synthetic */ EntityRenderState createRenderState() {
            return this.createRenderState();
        }

        @Override
        protected /* synthetic */ float getShadowRadius(EntityRenderState state) {
            return super.getShadowRadius((DisplayEntityRenderState)state);
        }

        @Override
        protected /* synthetic */ int getBlockLight(Entity entity, BlockPos pos) {
            return super.getBlockLight((DisplayEntity)entity, pos);
        }

        @Override
        protected /* synthetic */ int getSkyLight(Entity entity, BlockPos pos) {
            return super.getSkyLight((DisplayEntity)entity, pos);
        }
    }
}

