/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.MapRenderState;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.map.MapState;
import net.minecraft.util.Arm;
import net.minecraft.util.Colors;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

@Environment(value=EnvType.CLIENT)
public class HeldItemRenderer {
    private static final RenderLayer MAP_BACKGROUND = RenderLayer.getText(Identifier.ofVanilla("textures/map/map_background.png"));
    private static final RenderLayer MAP_BACKGROUND_CHECKERBOARD = RenderLayer.getText(Identifier.ofVanilla("textures/map/map_background_checkerboard.png"));
    private static final float field_32735 = -0.4f;
    private static final float field_32736 = 0.2f;
    private static final float field_32737 = -0.2f;
    private static final float field_32738 = -0.6f;
    private static final float EQUIP_OFFSET_TRANSLATE_X = 0.56f;
    private static final float EQUIP_OFFSET_TRANSLATE_Y = -0.52f;
    private static final float EQUIP_OFFSET_TRANSLATE_Z = -0.72f;
    private static final float field_32742 = 45.0f;
    private static final float field_32743 = -80.0f;
    private static final float field_32744 = -20.0f;
    private static final float field_32745 = -20.0f;
    private static final float EAT_OR_DRINK_X_ANGLE_MULTIPLIER = 10.0f;
    private static final float EAT_OR_DRINK_Y_ANGLE_MULTIPLIER = 90.0f;
    private static final float EAT_OR_DRINK_Z_ANGLE_MULTIPLIER = 30.0f;
    private static final float field_32749 = 0.6f;
    private static final float field_32750 = -0.5f;
    private static final float field_32751 = 0.0f;
    private static final double field_32752 = 27.0;
    private static final float field_32753 = 0.8f;
    private static final float field_32754 = 0.1f;
    private static final float field_32755 = -0.3f;
    private static final float field_32756 = 0.4f;
    private static final float field_32757 = -0.4f;
    private static final float ARM_HOLDING_ITEM_SECOND_Y_ANGLE_MULTIPLIER = 70.0f;
    private static final float ARM_HOLDING_ITEM_FIRST_Z_ANGLE_MULTIPLIER = -20.0f;
    private static final float field_32690 = -0.6f;
    private static final float field_32691 = 0.8f;
    private static final float field_32692 = 0.8f;
    private static final float field_32693 = -0.75f;
    private static final float field_32694 = -0.9f;
    private static final float field_32695 = 45.0f;
    private static final float field_32696 = -1.0f;
    private static final float field_32697 = 3.6f;
    private static final float field_32698 = 3.5f;
    private static final float ARM_HOLDING_ITEM_TRANSLATE_X = 5.6f;
    private static final int ARM_HOLDING_ITEM_X_ANGLE_MULTIPLIER = 200;
    private static final int ARM_HOLDING_ITEM_THIRD_Y_ANGLE_MULTIPLIER = -135;
    private static final int ARM_HOLDING_ITEM_SECOND_Z_ANGLE_MULTIPLIER = 120;
    private static final float field_32703 = -0.4f;
    private static final float field_32704 = -0.2f;
    private static final float field_32705 = 0.0f;
    private static final float field_32706 = 0.04f;
    private static final float field_32707 = -0.72f;
    private static final float field_32708 = -1.2f;
    private static final float field_32709 = -0.5f;
    private static final float field_32710 = 45.0f;
    private static final float field_32711 = -85.0f;
    private static final float ARM_X_ANGLE_MULTIPLIER = 45.0f;
    private static final float ARM_Y_ANGLE_MULTIPLIER = 92.0f;
    private static final float ARM_Z_ANGLE_MULTIPLIER = -41.0f;
    private static final float ARM_TRANSLATE_X = 0.3f;
    private static final float ARM_TRANSLATE_Y = -1.1f;
    private static final float ARM_TRANSLATE_Z = 0.45f;
    private static final float field_32718 = 20.0f;
    private static final float FIRST_PERSON_MAP_FIRST_SCALE = 0.38f;
    private static final float FIRST_PERSON_MAP_TRANSLATE_X = -0.5f;
    private static final float FIRST_PERSON_MAP_TRANSLATE_Y = -0.5f;
    private static final float FIRST_PERSON_MAP_TRANSLATE_Z = 0.0f;
    private static final float FIRST_PERSON_MAP_SECOND_SCALE = 0.0078125f;
    private static final int field_32724 = 7;
    private static final int field_32725 = 128;
    private static final int field_32726 = 128;
    private static final float field_32727 = 0.0f;
    private static final float field_32728 = 0.0f;
    private static final float field_32729 = 0.04f;
    private static final float field_32730 = 0.0f;
    private static final float field_32731 = 0.004f;
    private static final float field_32732 = 0.0f;
    private static final float field_32733 = 0.2f;
    private static final float field_32734 = 0.1f;
    private final MinecraftClient client;
    private final MapRenderState mapRenderState = new MapRenderState();
    private ItemStack mainHand = ItemStack.EMPTY;
    private ItemStack offHand = ItemStack.EMPTY;
    private float equipProgressMainHand;
    private float lastEquipProgressMainHand;
    private float equipProgressOffHand;
    private float lastEquipProgressOffHand;
    private final EntityRenderManager entityRenderDispatcher;
    private final ItemRenderer itemRenderer;
    private final ItemModelManager itemModelManager;

    public HeldItemRenderer(MinecraftClient client, EntityRenderManager entityRenderDispatcher, ItemRenderer itemRenderer, ItemModelManager itemModelManager) {
        this.client = client;
        this.entityRenderDispatcher = entityRenderDispatcher;
        this.itemRenderer = itemRenderer;
        this.itemModelManager = itemModelManager;
    }

    public void renderItem(LivingEntity entity, ItemStack stack, ItemDisplayContext renderMode, MatrixStack matrices, OrderedRenderCommandQueue arg5, int light) {
        if (stack.isEmpty()) {
            return;
        }
        ItemRenderState lv = new ItemRenderState();
        this.itemModelManager.clearAndUpdate(lv, stack, renderMode, entity.getEntityWorld(), entity, entity.getId() + renderMode.ordinal());
        lv.render(matrices, arg5, light, OverlayTexture.DEFAULT_UV, 0);
    }

    private float getMapAngle(float tickProgress) {
        float g = 1.0f - tickProgress / 45.0f + 0.1f;
        g = MathHelper.clamp(g, 0.0f, 1.0f);
        g = -MathHelper.cos(g * (float)Math.PI) * 0.5f + 0.5f;
        return g;
    }

    private void renderArm(MatrixStack matrices, OrderedRenderCommandQueue arg2, int light, Arm arm) {
        PlayerEntityRenderer<AbstractClientPlayerEntity> lv = this.entityRenderDispatcher.getPlayerRenderer(this.client.player);
        matrices.push();
        float f = arm == Arm.RIGHT ? 1.0f : -1.0f;
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(92.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(45.0f));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * -41.0f));
        matrices.translate(f * 0.3f, -1.1f, 0.45f);
        Identifier lv2 = this.client.player.getSkin().body().texturePath();
        if (arm == Arm.RIGHT) {
            lv.renderRightArm(matrices, arg2, light, lv2, this.client.player.isModelPartVisible(PlayerModelPart.RIGHT_SLEEVE));
        } else {
            lv.renderLeftArm(matrices, arg2, light, lv2, this.client.player.isModelPartVisible(PlayerModelPart.LEFT_SLEEVE));
        }
        matrices.pop();
    }

    private void renderMapInOneHand(MatrixStack matrices, OrderedRenderCommandQueue arg2, int light, float equipProgress, Arm arm, float swingProgress, ItemStack stack) {
        float h = arm == Arm.RIGHT ? 1.0f : -1.0f;
        matrices.translate(h * 0.125f, -0.125f, 0.0f);
        if (!this.client.player.isInvisible()) {
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(h * 10.0f));
            this.renderArmHoldingItem(matrices, arg2, light, equipProgress, swingProgress, arm);
            matrices.pop();
        }
        matrices.push();
        matrices.translate(h * 0.51f, -0.08f + equipProgress * -1.2f, -0.75f);
        float j = MathHelper.sqrt(swingProgress);
        float k = MathHelper.sin(j * (float)Math.PI);
        float l = -0.5f * k;
        float m = 0.4f * MathHelper.sin(j * ((float)Math.PI * 2));
        float n = -0.3f * MathHelper.sin(swingProgress * (float)Math.PI);
        matrices.translate(h * l, m - 0.3f * k, n);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(k * -45.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(h * k * -30.0f));
        this.renderFirstPersonMap(matrices, arg2, light, stack);
        matrices.pop();
    }

    private void renderMapInBothHands(MatrixStack matrices, OrderedRenderCommandQueue arg2, int light, float pitch, float equipProgress, float swingProgress) {
        float j = MathHelper.sqrt(swingProgress);
        float k = -0.2f * MathHelper.sin(swingProgress * (float)Math.PI);
        float l = -0.4f * MathHelper.sin(j * (float)Math.PI);
        matrices.translate(0.0f, -k / 2.0f, l);
        float m = this.getMapAngle(pitch);
        matrices.translate(0.0f, 0.04f + equipProgress * -1.2f + m * -0.5f, -0.72f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m * -85.0f));
        if (!this.client.player.isInvisible()) {
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0f));
            this.renderArm(matrices, arg2, light, Arm.RIGHT);
            this.renderArm(matrices, arg2, light, Arm.LEFT);
            matrices.pop();
        }
        float n = MathHelper.sin(j * (float)Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(n * 20.0f));
        matrices.scale(2.0f, 2.0f, 2.0f);
        this.renderFirstPersonMap(matrices, arg2, light, this.mainHand);
    }

    private void renderFirstPersonMap(MatrixStack matrices, OrderedRenderCommandQueue queue, int swingProgress, ItemStack stack) {
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
        matrices.scale(0.38f, 0.38f, 0.38f);
        matrices.translate(-0.5f, -0.5f, 0.0f);
        matrices.scale(0.0078125f, 0.0078125f, 0.0078125f);
        MapIdComponent lv = stack.get(DataComponentTypes.MAP_ID);
        MapState lv2 = FilledMapItem.getMapState(lv, (World)this.client.world);
        RenderLayer lv3 = lv2 == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD;
        queue.submitCustom(matrices, lv3, (matricesEntry, vertexConsumer) -> {
            vertexConsumer.vertex(matricesEntry, -7.0f, 135.0f, 0.0f).color(Colors.WHITE).texture(0.0f, 1.0f).light(swingProgress);
            vertexConsumer.vertex(matricesEntry, 135.0f, 135.0f, 0.0f).color(Colors.WHITE).texture(1.0f, 1.0f).light(swingProgress);
            vertexConsumer.vertex(matricesEntry, 135.0f, -7.0f, 0.0f).color(Colors.WHITE).texture(1.0f, 0.0f).light(swingProgress);
            vertexConsumer.vertex(matricesEntry, -7.0f, -7.0f, 0.0f).color(Colors.WHITE).texture(0.0f, 0.0f).light(swingProgress);
        });
        if (lv2 != null) {
            MapRenderer lv4 = this.client.getMapRenderer();
            lv4.update(lv, lv2, this.mapRenderState);
            lv4.draw(this.mapRenderState, matrices, queue, false, swingProgress);
        }
    }

    private void renderArmHoldingItem(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, float equipProgress, float swingProgress, Arm arm) {
        boolean bl = arm != Arm.LEFT;
        float h = bl ? 1.0f : -1.0f;
        float j = MathHelper.sqrt(swingProgress);
        float k = -0.3f * MathHelper.sin(j * (float)Math.PI);
        float l = 0.4f * MathHelper.sin(j * ((float)Math.PI * 2));
        float m = -0.4f * MathHelper.sin(swingProgress * (float)Math.PI);
        matrices.translate(h * (k + 0.64000005f), l + -0.6f + equipProgress * -0.6f, m + -0.71999997f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(h * 45.0f));
        float n = MathHelper.sin(swingProgress * swingProgress * (float)Math.PI);
        float o = MathHelper.sin(j * (float)Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(h * o * 70.0f));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(h * n * -20.0f));
        ClientPlayerEntity lv = this.client.player;
        matrices.translate(h * -1.0f, 3.6f, 3.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(h * 120.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(200.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(h * -135.0f));
        matrices.translate(h * 5.6f, 0.0f, 0.0f);
        PlayerEntityRenderer<AbstractClientPlayerEntity> lv2 = this.entityRenderDispatcher.getPlayerRenderer(lv);
        Identifier lv3 = lv.getSkin().body().texturePath();
        if (bl) {
            lv2.renderRightArm(matrices, queue, light, lv3, lv.isModelPartVisible(PlayerModelPart.RIGHT_SLEEVE));
        } else {
            lv2.renderLeftArm(matrices, queue, light, lv3, lv.isModelPartVisible(PlayerModelPart.LEFT_SLEEVE));
        }
    }

    private void applyEatOrDrinkTransformation(MatrixStack matrices, float tickProgress, Arm arm, ItemStack stack, PlayerEntity player) {
        float i;
        float g = (float)player.getItemUseTimeLeft() - tickProgress + 1.0f;
        float h = g / (float)stack.getMaxUseTime(player);
        if (h < 0.8f) {
            i = MathHelper.abs(MathHelper.cos(g / 4.0f * (float)Math.PI) * 0.1f);
            matrices.translate(0.0f, i, 0.0f);
        }
        i = 1.0f - (float)Math.pow(h, 27.0);
        int j = arm == Arm.RIGHT ? 1 : -1;
        matrices.translate(i * 0.6f * (float)j, i * -0.5f, i * 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)j * i * 90.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(i * 10.0f));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)j * i * 30.0f));
    }

    private void applyBrushTransformation(MatrixStack matrices, float tickProgress, Arm arm, ItemStack stack, PlayerEntity player, float equipProgress) {
        this.applyEquipOffset(matrices, arm, equipProgress);
        float h = player.getItemUseTimeLeft() % 10;
        float i = h - tickProgress + 1.0f;
        float j = 1.0f - i / 10.0f;
        float k = -90.0f;
        float l = 60.0f;
        float m = 150.0f;
        float n = -15.0f;
        int o = 2;
        float p = -15.0f + 75.0f * MathHelper.cos(j * 2.0f * (float)Math.PI);
        if (arm != Arm.RIGHT) {
            matrices.translate(0.1, 0.83, 0.35);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0f));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(p));
            matrices.translate(-0.3, 0.22, 0.35);
        } else {
            matrices.translate(-0.25, 0.22, 0.35);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0f));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(0.0f));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(p));
        }
    }

    private void applySwingOffset(MatrixStack matrices, Arm arm, float swingProgress) {
        int i = arm == Arm.RIGHT ? 1 : -1;
        float g = MathHelper.sin(swingProgress * swingProgress * (float)Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * (45.0f + g * -20.0f)));
        float h = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float)Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)i * h * -20.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(h * -80.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * -45.0f));
    }

    private void applyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress) {
        int i = arm == Arm.RIGHT ? 1 : -1;
        matrices.translate((float)i * 0.56f, -0.52f + equipProgress * -0.6f, -0.72f);
    }

    public void renderItem(float tickProgress, MatrixStack matrices, OrderedRenderCommandQueue arg2, ClientPlayerEntity player, int light) {
        float m;
        float l;
        float g = player.getHandSwingProgress(tickProgress);
        Hand lv = MoreObjects.firstNonNull(player.preferredHand, Hand.MAIN_HAND);
        float h = player.getLerpedPitch(tickProgress);
        HandRenderType lv2 = HeldItemRenderer.getHandRenderType(player);
        float j = MathHelper.lerp(tickProgress, player.lastRenderPitch, player.renderPitch);
        float k = MathHelper.lerp(tickProgress, player.lastRenderYaw, player.renderYaw);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((player.getPitch(tickProgress) - j) * 0.1f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((player.getYaw(tickProgress) - k) * 0.1f));
        if (lv2.renderMainHand) {
            l = lv == Hand.MAIN_HAND ? g : 0.0f;
            m = 1.0f - MathHelper.lerp(tickProgress, this.lastEquipProgressMainHand, this.equipProgressMainHand);
            this.renderFirstPersonItem(player, tickProgress, h, Hand.MAIN_HAND, l, this.mainHand, m, matrices, arg2, light);
        }
        if (lv2.renderOffHand) {
            l = lv == Hand.OFF_HAND ? g : 0.0f;
            m = 1.0f - MathHelper.lerp(tickProgress, this.lastEquipProgressOffHand, this.equipProgressOffHand);
            this.renderFirstPersonItem(player, tickProgress, h, Hand.OFF_HAND, l, this.offHand, m, matrices, arg2, light);
        }
        this.client.gameRenderer.getEntityRenderDispatcher().render();
        this.client.getBufferBuilders().getEntityVertexConsumers().draw();
    }

    @VisibleForTesting
    static HandRenderType getHandRenderType(ClientPlayerEntity player) {
        boolean bl2;
        ItemStack lv = player.getMainHandStack();
        ItemStack lv2 = player.getOffHandStack();
        boolean bl = lv.isOf(Items.BOW) || lv2.isOf(Items.BOW);
        boolean bl3 = bl2 = lv.isOf(Items.CROSSBOW) || lv2.isOf(Items.CROSSBOW);
        if (!bl && !bl2) {
            return HandRenderType.RENDER_BOTH_HANDS;
        }
        if (player.isUsingItem()) {
            return HeldItemRenderer.getUsingItemHandRenderType(player);
        }
        if (HeldItemRenderer.isChargedCrossbow(lv)) {
            return HandRenderType.RENDER_MAIN_HAND_ONLY;
        }
        return HandRenderType.RENDER_BOTH_HANDS;
    }

    private static HandRenderType getUsingItemHandRenderType(ClientPlayerEntity player) {
        ItemStack lv = player.getActiveItem();
        Hand lv2 = player.getActiveHand();
        if (lv.isOf(Items.BOW) || lv.isOf(Items.CROSSBOW)) {
            return HandRenderType.shouldOnlyRender(lv2);
        }
        return lv2 == Hand.MAIN_HAND && HeldItemRenderer.isChargedCrossbow(player.getOffHandStack()) ? HandRenderType.RENDER_MAIN_HAND_ONLY : HandRenderType.RENDER_BOTH_HANDS;
    }

    private static boolean isChargedCrossbow(ItemStack stack) {
        return stack.isOf(Items.CROSSBOW) && CrossbowItem.isCharged(stack);
    }

    private void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, OrderedRenderCommandQueue arg5, int light) {
        if (player.isUsingSpyglass()) {
            return;
        }
        boolean bl = hand == Hand.MAIN_HAND;
        Arm lv = bl ? player.getMainArm() : player.getMainArm().getOpposite();
        matrices.push();
        if (item.isEmpty()) {
            if (bl && !player.isInvisible()) {
                this.renderArmHoldingItem(matrices, arg5, light, equipProgress, swingProgress, lv);
            }
        } else if (item.contains(DataComponentTypes.MAP_ID)) {
            if (bl && this.offHand.isEmpty()) {
                this.renderMapInBothHands(matrices, arg5, light, pitch, equipProgress, swingProgress);
            } else {
                this.renderMapInOneHand(matrices, arg5, light, equipProgress, lv, swingProgress, item);
            }
        } else if (item.isOf(Items.CROSSBOW)) {
            int k;
            boolean bl2 = CrossbowItem.isCharged(item);
            boolean bl3 = lv == Arm.RIGHT;
            int n = k = bl3 ? 1 : -1;
            if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand && !bl2) {
                this.applyEquipOffset(matrices, lv, equipProgress);
                matrices.translate((float)k * -0.4785682f, -0.094387f, 0.05731531f);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-11.935f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)k * 65.3f));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)k * -9.785f));
                float l = (float)item.getMaxUseTime(player) - ((float)player.getItemUseTimeLeft() - tickProgress + 1.0f);
                float m = l / (float)CrossbowItem.getPullTime(item, player);
                if (m > 1.0f) {
                    m = 1.0f;
                }
                if (m > 0.1f) {
                    float n2 = MathHelper.sin((l - 0.1f) * 1.3f);
                    float o = m - 0.1f;
                    float p = n2 * o;
                    matrices.translate(p * 0.0f, p * 0.004f, p * 0.0f);
                }
                matrices.translate(m * 0.0f, m * 0.0f, m * 0.04f);
                matrices.scale(1.0f, 1.0f, 1.0f + m * 0.2f);
                matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)k * 45.0f));
            } else {
                this.swingArm(swingProgress, equipProgress, matrices, k, lv);
                if (bl2 && swingProgress < 0.001f && bl) {
                    matrices.translate((float)k * -0.641864f, 0.0f, 0.0f);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)k * 10.0f));
                }
            }
            this.renderItem(player, item, bl3 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, matrices, arg5, light);
        } else {
            int q;
            boolean bl2 = lv == Arm.RIGHT;
            int n = q = bl2 ? 1 : -1;
            if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
                switch (item.getUseAction()) {
                    case NONE: {
                        this.applyEquipOffset(matrices, lv, equipProgress);
                        break;
                    }
                    case EAT: 
                    case DRINK: {
                        this.applyEatOrDrinkTransformation(matrices, tickProgress, lv, item, player);
                        this.applyEquipOffset(matrices, lv, equipProgress);
                        break;
                    }
                    case BLOCK: {
                        this.applyEquipOffset(matrices, lv, equipProgress);
                        if (item.getItem() instanceof ShieldItem) break;
                        matrices.translate((float)q * -0.14142136f, 0.08f, 0.14142136f);
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25f));
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)q * 13.365f));
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)q * 78.05f));
                        break;
                    }
                    case BOW: {
                        this.applyEquipOffset(matrices, lv, equipProgress);
                        matrices.translate((float)q * -0.2785682f, 0.18344387f, 0.15731531f);
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-13.935f));
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)q * 35.3f));
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)q * -9.785f));
                        float r = (float)item.getMaxUseTime(player) - ((float)player.getItemUseTimeLeft() - tickProgress + 1.0f);
                        float l = r / 20.0f;
                        l = (l * l + l * 2.0f) / 3.0f;
                        if (l > 1.0f) {
                            l = 1.0f;
                        }
                        if (l > 0.1f) {
                            float m = MathHelper.sin((r - 0.1f) * 1.3f);
                            float n3 = l - 0.1f;
                            float o = m * n3;
                            matrices.translate(o * 0.0f, o * 0.004f, o * 0.0f);
                        }
                        matrices.translate(l * 0.0f, l * 0.0f, l * 0.04f);
                        matrices.scale(1.0f, 1.0f, 1.0f + l * 0.2f);
                        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)q * 45.0f));
                        break;
                    }
                    case SPEAR: {
                        this.applyEquipOffset(matrices, lv, equipProgress);
                        matrices.translate((float)q * -0.5f, 0.7f, 0.1f);
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-55.0f));
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)q * 35.3f));
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)q * -9.785f));
                        float r = (float)item.getMaxUseTime(player) - ((float)player.getItemUseTimeLeft() - tickProgress + 1.0f);
                        float l = r / 10.0f;
                        if (l > 1.0f) {
                            l = 1.0f;
                        }
                        if (l > 0.1f) {
                            float m = MathHelper.sin((r - 0.1f) * 1.3f);
                            float n4 = l - 0.1f;
                            float o = m * n4;
                            matrices.translate(o * 0.0f, o * 0.004f, o * 0.0f);
                        }
                        matrices.translate(0.0f, 0.0f, l * 0.2f);
                        matrices.scale(1.0f, 1.0f, 1.0f + l * 0.2f);
                        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)q * 45.0f));
                        break;
                    }
                    case BRUSH: {
                        this.applyBrushTransformation(matrices, tickProgress, lv, item, player, equipProgress);
                        break;
                    }
                    case BUNDLE: {
                        this.swingArm(swingProgress, equipProgress, matrices, q, lv);
                    }
                }
            } else if (player.isUsingRiptide()) {
                this.applyEquipOffset(matrices, lv, equipProgress);
                matrices.translate((float)q * -0.4f, 0.8f, 0.3f);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)q * 65.0f));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)q * -85.0f));
            } else {
                this.swingArm(swingProgress, equipProgress, matrices, q, lv);
            }
            this.renderItem(player, item, bl2 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, matrices, arg5, light);
        }
        matrices.pop();
    }

    private void swingArm(float swingProgress, float equipProgress, MatrixStack matrices, int armX, Arm arm) {
        float h = -0.4f * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float)Math.PI);
        float j = 0.2f * MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float)Math.PI * 2));
        float k = -0.2f * MathHelper.sin(swingProgress * (float)Math.PI);
        matrices.translate((float)armX * h, j, k);
        this.applyEquipOffset(matrices, arm, equipProgress);
        this.applySwingOffset(matrices, arm, swingProgress);
    }

    private boolean shouldSkipHandAnimationOnSwap(ItemStack from, ItemStack to) {
        if (ItemStack.areEqual(from, to)) {
            return true;
        }
        return !this.itemModelManager.hasHandAnimationOnSwap(to);
    }

    public void updateHeldItems() {
        this.lastEquipProgressMainHand = this.equipProgressMainHand;
        this.lastEquipProgressOffHand = this.equipProgressOffHand;
        ClientPlayerEntity lv = this.client.player;
        ItemStack lv2 = lv.getMainHandStack();
        ItemStack lv3 = lv.getOffHandStack();
        if (this.shouldSkipHandAnimationOnSwap(this.mainHand, lv2)) {
            this.mainHand = lv2;
        }
        if (this.shouldSkipHandAnimationOnSwap(this.offHand, lv3)) {
            this.offHand = lv3;
        }
        if (lv.isRiding()) {
            this.equipProgressMainHand = MathHelper.clamp(this.equipProgressMainHand - 0.4f, 0.0f, 1.0f);
            this.equipProgressOffHand = MathHelper.clamp(this.equipProgressOffHand - 0.4f, 0.0f, 1.0f);
        } else {
            float f = lv.getAttackCooldownProgress(1.0f);
            float g = this.mainHand != lv2 ? 0.0f : f * f * f;
            float h = this.offHand != lv3 ? 0.0f : 1.0f;
            this.equipProgressMainHand += MathHelper.clamp(g - this.equipProgressMainHand, -0.4f, 0.4f);
            this.equipProgressOffHand += MathHelper.clamp(h - this.equipProgressOffHand, -0.4f, 0.4f);
        }
        if (this.equipProgressMainHand < 0.1f) {
            this.mainHand = lv2;
        }
        if (this.equipProgressOffHand < 0.1f) {
            this.offHand = lv3;
        }
    }

    public void resetEquipProgress(Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            this.equipProgressMainHand = 0.0f;
        } else {
            this.equipProgressOffHand = 0.0f;
        }
    }

    @Environment(value=EnvType.CLIENT)
    @VisibleForTesting
    static enum HandRenderType {
        RENDER_BOTH_HANDS(true, true),
        RENDER_MAIN_HAND_ONLY(true, false),
        RENDER_OFF_HAND_ONLY(false, true);

        final boolean renderMainHand;
        final boolean renderOffHand;

        private HandRenderType(boolean renderMainHand, boolean renderOffHand) {
            this.renderMainHand = renderMainHand;
            this.renderOffHand = renderOffHand;
        }

        public static HandRenderType shouldOnlyRender(Hand hand) {
            return hand == Hand.MAIN_HAND ? RENDER_MAIN_HAND_ONLY : RENDER_OFF_HAND_ONLY;
        }
    }
}

