/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityDebugInfo;
import net.minecraft.client.render.entity.state.EntityHitbox;
import net.minecraft.client.render.entity.state.EntityHitboxAndView;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.ExperimentalMinecartController;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class EntityRenderer<T extends Entity, S extends EntityRenderState> {
    private static final float field_61797 = 0.5f;
    private static final float field_61798 = 32.0f;
    public static final float field_32921 = 0.025f;
    protected final EntityRenderManager dispatcher;
    private final TextRenderer textRenderer;
    protected float shadowRadius;
    protected float shadowOpacity = 1.0f;

    protected EntityRenderer(EntityRendererFactory.Context context) {
        this.dispatcher = context.getRenderDispatcher();
        this.textRenderer = context.getTextRenderer();
    }

    public final int getLight(T entity, float tickProgress) {
        BlockPos lv = BlockPos.ofFloored(((Entity)entity).getClientCameraPosVec(tickProgress));
        return LightmapTextureManager.pack(this.getBlockLight(entity, lv), this.getSkyLight(entity, lv));
    }

    protected int getSkyLight(T entity, BlockPos pos) {
        return ((Entity)entity).getEntityWorld().getLightLevel(LightType.SKY, pos);
    }

    protected int getBlockLight(T entity, BlockPos pos) {
        if (((Entity)entity).isOnFire()) {
            return 15;
        }
        return ((Entity)entity).getEntityWorld().getLightLevel(LightType.BLOCK, pos);
    }

    public boolean shouldRender(T entity, Frustum frustum, double x, double y, double z) {
        Leashable lv2;
        Entity lv3;
        if (!((Entity)entity).shouldRender(x, y, z)) {
            return false;
        }
        if (!this.canBeCulled(entity)) {
            return true;
        }
        Box lv = this.getBoundingBox(entity).expand(0.5);
        if (lv.isNaN() || lv.getAverageSideLength() == 0.0) {
            lv = new Box(((Entity)entity).getX() - 2.0, ((Entity)entity).getY() - 2.0, ((Entity)entity).getZ() - 2.0, ((Entity)entity).getX() + 2.0, ((Entity)entity).getY() + 2.0, ((Entity)entity).getZ() + 2.0);
        }
        if (frustum.isVisible(lv)) {
            return true;
        }
        if (entity instanceof Leashable && (lv3 = (lv2 = (Leashable)entity).getLeashHolder()) != null) {
            Box lv4 = this.dispatcher.getRenderer(lv3).getBoundingBox(lv3);
            return frustum.isVisible(lv4) || frustum.isVisible(lv.union(lv4));
        }
        return false;
    }

    protected Box getBoundingBox(T entity) {
        return ((Entity)entity).getBoundingBox();
    }

    protected boolean canBeCulled(T entity) {
        return true;
    }

    public Vec3d getPositionOffset(S state) {
        if (((EntityRenderState)state).positionOffset != null) {
            return ((EntityRenderState)state).positionOffset;
        }
        return Vec3d.ZERO;
    }

    public void render(S renderState, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (((EntityRenderState)renderState).leashDatas != null) {
            for (EntityRenderState.LeashData lv : ((EntityRenderState)renderState).leashDatas) {
                queue.submitLeash(matrices, lv);
            }
        }
        this.renderLabelIfPresent(renderState, matrices, queue, cameraState);
    }

    protected boolean hasLabel(T entity, double squaredDistanceToCamera) {
        return ((Entity)entity).shouldRenderName() || ((Entity)entity).hasCustomName() && entity == this.dispatcher.targetedEntity;
    }

    public TextRenderer getTextRenderer() {
        return this.textRenderer;
    }

    protected void renderLabelIfPresent(S state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState arg4) {
        if (((EntityRenderState)state).displayName != null) {
            queue.submitLabel(matrices, ((EntityRenderState)state).nameLabelPos, 0, ((EntityRenderState)state).displayName, !((EntityRenderState)state).sneaking, ((EntityRenderState)state).light, ((EntityRenderState)state).squaredDistanceToCamera, arg4);
        }
    }

    @Nullable
    protected Text getDisplayName(T entity) {
        return ((Entity)entity).getDisplayName();
    }

    protected float getShadowRadius(S state) {
        return this.shadowRadius;
    }

    protected float getShadowOpacity(S state) {
        return this.shadowOpacity;
    }

    public abstract S createRenderState();

    public final S getAndUpdateRenderState(T entity, float tickProgress) {
        S lv = this.createRenderState();
        this.updateRenderState(entity, lv, tickProgress);
        this.updateShadow(entity, lv);
        return lv;
    }

    public void updateRenderState(T entity, S state, float tickProgress) {
        Leashable lv4;
        Entity entity2;
        ExperimentalMinecartController lv2;
        AbstractMinecartEntity lv;
        Object object;
        ((EntityRenderState)state).entityType = ((Entity)entity).getType();
        ((EntityRenderState)state).x = MathHelper.lerp((double)tickProgress, ((Entity)entity).lastRenderX, ((Entity)entity).getX());
        ((EntityRenderState)state).y = MathHelper.lerp((double)tickProgress, ((Entity)entity).lastRenderY, ((Entity)entity).getY());
        ((EntityRenderState)state).z = MathHelper.lerp((double)tickProgress, ((Entity)entity).lastRenderZ, ((Entity)entity).getZ());
        ((EntityRenderState)state).invisible = ((Entity)entity).isInvisible();
        ((EntityRenderState)state).age = (float)((Entity)entity).age + tickProgress;
        ((EntityRenderState)state).width = ((Entity)entity).getWidth();
        ((EntityRenderState)state).height = ((Entity)entity).getHeight();
        ((EntityRenderState)state).standingEyeHeight = ((Entity)entity).getStandingEyeHeight();
        if (((Entity)entity).hasVehicle() && (object = ((Entity)entity).getVehicle()) instanceof AbstractMinecartEntity && (object = (lv = (AbstractMinecartEntity)object).getController()) instanceof ExperimentalMinecartController && (lv2 = (ExperimentalMinecartController)object).hasCurrentLerpSteps()) {
            double d = MathHelper.lerp((double)tickProgress, lv.lastRenderX, lv.getX());
            double e = MathHelper.lerp((double)tickProgress, lv.lastRenderY, lv.getY());
            double g = MathHelper.lerp((double)tickProgress, lv.lastRenderZ, lv.getZ());
            ((EntityRenderState)state).positionOffset = lv2.getLerpedPosition(tickProgress).subtract(new Vec3d(d, e, g));
        } else {
            ((EntityRenderState)state).positionOffset = null;
        }
        if (this.dispatcher.camera != null) {
            boolean bl;
            ((EntityRenderState)state).squaredDistanceToCamera = this.dispatcher.getSquaredDistanceToCamera((Entity)entity);
            boolean bl2 = bl = ((EntityRenderState)state).squaredDistanceToCamera < 4096.0 && this.hasLabel(entity, ((EntityRenderState)state).squaredDistanceToCamera);
            if (bl) {
                ((EntityRenderState)state).displayName = this.getDisplayName(entity);
                ((EntityRenderState)state).nameLabelPos = ((Entity)entity).getAttachments().getPointNullable(EntityAttachmentType.NAME_TAG, 0, ((Entity)entity).getLerpedYaw(tickProgress));
            } else {
                ((EntityRenderState)state).displayName = null;
            }
        }
        ((EntityRenderState)state).sneaking = ((Entity)entity).isSneaky();
        World lv3 = ((Entity)entity).getEntityWorld();
        if (entity instanceof Leashable && (entity2 = (lv4 = (Leashable)entity).getLeashHolder()) instanceof Entity) {
            int m;
            Entity lv5 = entity2;
            float h = ((Entity)entity).lerpYaw(tickProgress) * ((float)Math.PI / 180);
            Vec3d lv6 = lv4.getLeashOffset(tickProgress);
            BlockPos lv7 = BlockPos.ofFloored(((Entity)entity).getCameraPosVec(tickProgress));
            BlockPos lv8 = BlockPos.ofFloored(lv5.getCameraPosVec(tickProgress));
            int i = this.getBlockLight(entity, lv7);
            int j = this.dispatcher.getRenderer(lv5).getBlockLight(lv5, lv8);
            int k = lv3.getLightLevel(LightType.SKY, lv7);
            int l = lv3.getLightLevel(LightType.SKY, lv8);
            boolean bl2 = lv5.hasQuadLeashAttachmentPoints() && lv4.canUseQuadLeashAttachmentPoint();
            int n = m = bl2 ? 4 : 1;
            if (((EntityRenderState)state).leashDatas == null || ((EntityRenderState)state).leashDatas.size() != m) {
                ((EntityRenderState)state).leashDatas = new ArrayList<EntityRenderState.LeashData>(m);
                for (int n2 = 0; n2 < m; ++n2) {
                    ((EntityRenderState)state).leashDatas.add(new EntityRenderState.LeashData());
                }
            }
            if (bl2) {
                float o = lv5.lerpYaw(tickProgress) * ((float)Math.PI / 180);
                Vec3d lv9 = lv5.getLerpedPos(tickProgress);
                Vec3d[] lvs = lv4.getQuadLeashOffsets();
                Vec3d[] lvs2 = lv5.getHeldQuadLeashOffsets();
                for (int p = 0; p < m; ++p) {
                    EntityRenderState.LeashData lv10 = ((EntityRenderState)state).leashDatas.get(p);
                    lv10.offset = lvs[p].rotateY(-h);
                    lv10.startPos = ((Entity)entity).getLerpedPos(tickProgress).add(lv10.offset);
                    lv10.endPos = lv9.add(lvs2[p].rotateY(-o));
                    lv10.leashedEntityBlockLight = i;
                    lv10.leashHolderBlockLight = j;
                    lv10.leashedEntitySkyLight = k;
                    lv10.leashHolderSkyLight = l;
                    lv10.slack = false;
                }
            } else {
                Vec3d lv11 = lv6.rotateY(-h);
                EntityRenderState.LeashData lv12 = ((EntityRenderState)state).leashDatas.getFirst();
                lv12.offset = lv11;
                lv12.startPos = ((Entity)entity).getLerpedPos(tickProgress).add(lv11);
                lv12.endPos = lv5.getLeashPos(tickProgress);
                lv12.leashedEntityBlockLight = i;
                lv12.leashHolderBlockLight = j;
                lv12.leashedEntitySkyLight = k;
                lv12.leashHolderSkyLight = l;
            }
        } else {
            ((EntityRenderState)state).leashDatas = null;
        }
        ((EntityRenderState)state).onFire = ((Entity)entity).doesRenderOnFire();
        MinecraftClient lv13 = MinecraftClient.getInstance();
        boolean bl3 = lv13.hasOutline((Entity)entity);
        int n = ((EntityRenderState)state).outlineColor = bl3 ? ColorHelper.fullAlpha(((Entity)entity).getTeamColorValue()) : 0;
        if (lv13.debugHudEntryList.isEntryVisible(DebugHudEntries.ENTITY_HITBOXES) && !((EntityRenderState)state).invisible && !lv13.hasReducedDebugInfo()) {
            this.updateDebugState(entity, state, tickProgress);
        } else {
            ((EntityRenderState)state).hitbox = null;
            ((EntityRenderState)state).debugInfo = null;
        }
        ((EntityRenderState)state).light = this.getLight(entity, tickProgress);
    }

    protected void updateShadow(T entity, S renderState) {
        MinecraftClient lv = MinecraftClient.getInstance();
        World lv2 = ((Entity)entity).getEntityWorld();
        this.updateShadow(renderState, lv, lv2);
    }

    private void updateShadow(S renderState, MinecraftClient client, World world) {
        ((EntityRenderState)renderState).shadowPieces.clear();
        if (client.options.getEntityShadows().getValue().booleanValue() && !((EntityRenderState)renderState).invisible) {
            double d;
            float g;
            float f;
            ((EntityRenderState)renderState).shadowRadius = f = Math.min(this.getShadowRadius(renderState), 32.0f);
            if (f > 0.0f && (g = (float)((1.0 - (d = ((EntityRenderState)renderState).squaredDistanceToCamera) / 256.0) * (double)this.getShadowOpacity(renderState))) > 0.0f) {
                int i = MathHelper.floor(((EntityRenderState)renderState).x - (double)f);
                int j = MathHelper.floor(((EntityRenderState)renderState).x + (double)f);
                int k = MathHelper.floor(((EntityRenderState)renderState).z - (double)f);
                int l = MathHelper.floor(((EntityRenderState)renderState).z + (double)f);
                float h = Math.min(g / 0.5f - 1.0f, f);
                int m = MathHelper.floor(((EntityRenderState)renderState).y - (double)h);
                int n = MathHelper.floor(((EntityRenderState)renderState).y);
                BlockPos.Mutable lv = new BlockPos.Mutable();
                for (int o = k; o <= l; ++o) {
                    for (int p = i; p <= j; ++p) {
                        lv.set(p, 0, o);
                        Chunk lv2 = world.getChunk(lv);
                        for (int q = m; q <= n; ++q) {
                            lv.setY(q);
                            this.addShadowPiece(renderState, world, g, lv, lv2);
                        }
                    }
                }
            }
        } else {
            ((EntityRenderState)renderState).shadowRadius = 0.0f;
        }
    }

    private void addShadowPiece(S renderState, World world, float shadowOpacity, BlockPos.Mutable pos, Chunk chunk) {
        float g = shadowOpacity - (float)(((EntityRenderState)renderState).y - (double)pos.getY()) * 0.5f;
        Vec3i lv = pos.down();
        BlockState lv2 = chunk.getBlockState((BlockPos)lv);
        if (lv2.getRenderType() == BlockRenderType.INVISIBLE) {
            return;
        }
        int i = world.getLightLevel(pos);
        if (i <= 3) {
            return;
        }
        if (!lv2.isFullCube(chunk, (BlockPos)lv)) {
            return;
        }
        VoxelShape lv3 = lv2.getOutlineShape(chunk, (BlockPos)lv);
        if (lv3.isEmpty()) {
            return;
        }
        float h = MathHelper.clamp(g * 0.5f * LightmapTextureManager.getBrightness(world.getDimension(), i), 0.0f, 1.0f);
        float j = (float)((double)pos.getX() - ((EntityRenderState)renderState).x);
        float k = (float)((double)pos.getY() - ((EntityRenderState)renderState).y);
        float l = (float)((double)pos.getZ() - ((EntityRenderState)renderState).z);
        ((EntityRenderState)renderState).shadowPieces.add(new EntityRenderState.ShadowPiece(j, k, l, lv3, h));
    }

    private void updateDebugState(T entity, S state, float tickProgress) {
        ((EntityRenderState)state).hitbox = this.createHitbox(entity, tickProgress, false);
        if (SharedConstants.SHOW_LOCAL_SERVER_ENTITY_HIT_BOXES) {
            Entity lv = EntityRenderer.getServerEntity(entity);
            if (lv != null) {
                Vec3d lv2 = lv.getVelocity();
                ((EntityRenderState)state).debugInfo = new EntityDebugInfo(false, lv.getX(), lv.getY(), lv.getZ(), lv2.x, lv2.y, lv2.z, lv.getStandingEyeHeight(), this.createHitbox(lv, 1.0f, true));
            } else {
                ((EntityRenderState)state).debugInfo = new EntityDebugInfo(true);
            }
        } else {
            ((EntityRenderState)state).debugInfo = null;
        }
    }

    private EntityHitboxAndView createHitbox(T entity, float tickProgress, boolean green) {
        ImmutableList.Builder<EntityHitbox> builder = new ImmutableList.Builder<EntityHitbox>();
        Box lv = ((Entity)entity).getBoundingBox();
        EntityHitbox lv2 = green ? new EntityHitbox(lv.minX - ((Entity)entity).getX(), lv.minY - ((Entity)entity).getY(), lv.minZ - ((Entity)entity).getZ(), lv.maxX - ((Entity)entity).getX(), lv.maxY - ((Entity)entity).getY(), lv.maxZ - ((Entity)entity).getZ(), 0.0f, 1.0f, 0.0f) : new EntityHitbox(lv.minX - ((Entity)entity).getX(), lv.minY - ((Entity)entity).getY(), lv.minZ - ((Entity)entity).getZ(), lv.maxX - ((Entity)entity).getX(), lv.maxY - ((Entity)entity).getY(), lv.maxZ - ((Entity)entity).getZ(), 1.0f, 1.0f, 1.0f);
        builder.add((Object)lv2);
        Entity lv3 = ((Entity)entity).getVehicle();
        if (lv3 != null) {
            float g = Math.min(lv3.getWidth(), ((Entity)entity).getWidth()) / 2.0f;
            float h = 0.0625f;
            Vec3d lv4 = lv3.getPassengerRidingPos((Entity)entity).subtract(((Entity)entity).getEntityPos());
            EntityHitbox lv5 = new EntityHitbox(lv4.x - (double)g, lv4.y, lv4.z - (double)g, lv4.x + (double)g, lv4.y + 0.0625, lv4.z + (double)g, 1.0f, 1.0f, 0.0f);
            builder.add((Object)lv5);
        }
        this.appendHitboxes(entity, builder, tickProgress);
        Vec3d lv6 = ((Entity)entity).getRotationVec(tickProgress);
        return new EntityHitboxAndView(lv6.x, lv6.y, lv6.z, (ImmutableList<EntityHitbox>)builder.build());
    }

    protected void appendHitboxes(T entity, ImmutableList.Builder<EntityHitbox> builder, float tickProgress) {
    }

    @Nullable
    private static Entity getServerEntity(Entity clientEntity) {
        ServerWorld lv2;
        IntegratedServer lv = MinecraftClient.getInstance().getServer();
        if (lv != null && (lv2 = lv.getWorld(clientEntity.getEntityWorld().getRegistryKey())) != null) {
            return lv2.getEntityById(clientEntity.getId());
        }
        return null;
    }
}

