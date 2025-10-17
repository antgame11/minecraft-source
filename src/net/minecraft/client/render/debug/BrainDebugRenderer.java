/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.debug;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.debug.DebugDataStore;
import net.minecraft.world.debug.DebugSubscriptionTypes;
import net.minecraft.world.debug.data.BrainDebugData;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BrainDebugRenderer
implements DebugRenderer.Renderer {
    private static final boolean field_32874 = true;
    private static final boolean field_32875 = false;
    private static final boolean field_32876 = false;
    private static final boolean field_32877 = false;
    private static final boolean field_32878 = false;
    private static final boolean field_32879 = false;
    private static final boolean field_32881 = false;
    private static final boolean field_32882 = true;
    private static final boolean field_38346 = false;
    private static final boolean field_32883 = true;
    private static final boolean field_32884 = true;
    private static final boolean field_32885 = true;
    private static final boolean field_32886 = true;
    private static final boolean field_32887 = true;
    private static final boolean field_32888 = true;
    private static final boolean field_32889 = true;
    private static final boolean field_32891 = true;
    private static final boolean field_32892 = true;
    private static final boolean field_38347 = true;
    private static final int POI_RANGE = 30;
    private static final int TARGET_ENTITY_RANGE = 8;
    private static final float DEFAULT_DRAWN_STRING_SIZE = 0.02f;
    private static final int AQUA = -16711681;
    private static final int GRAY = -3355444;
    private static final int PINK = -98404;
    private static final int ORANGE = -23296;
    private final MinecraftClient client;
    @Nullable
    private UUID targetedEntity;

    public BrainDebugRenderer(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ, DebugDataStore store, Frustum frustum) {
        this.draw(matrices, vertexConsumers, cameraX, cameraY, cameraZ, store);
        if (!this.client.player.isSpectator()) {
            this.updateTargetedEntity();
        }
    }

    private void draw(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double x, double y, double z, DebugDataStore store) {
        store.forEachEntityData(DebugSubscriptionTypes.BRAINS, (entity, brainData) -> {
            if (this.client.player.isInRange((Entity)entity, 30.0)) {
                this.drawBrain(matrices, vertexConsumers, (Entity)entity, (BrainDebugData)brainData, x, y, z);
            }
        });
    }

    private void drawBrain(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity, BrainDebugData brainData, double d, double e, double f) {
        boolean bl = this.isTargeted(entity);
        int i = 0;
        DebugRenderer.drawLargeFloatingText(matrices, vertexConsumers, entity, i, brainData.name(), -1, 0.03f);
        ++i;
        if (bl) {
            DebugRenderer.drawLargeFloatingText(matrices, vertexConsumers, entity, i, brainData.profession() + " " + brainData.xp() + " xp", -1, 0.02f);
            ++i;
        }
        if (bl) {
            int j = brainData.health() < brainData.maxHealth() ? -23296 : -1;
            DebugRenderer.drawLargeFloatingText(matrices, vertexConsumers, entity, i, "health: " + String.format(Locale.ROOT, "%.1f", Float.valueOf(brainData.health())) + " / " + String.format(Locale.ROOT, "%.1f", Float.valueOf(brainData.maxHealth())), j, 0.02f);
            ++i;
        }
        if (bl && !brainData.inventory().equals("")) {
            DebugRenderer.drawLargeFloatingText(matrices, vertexConsumers, entity, i, brainData.inventory(), -98404, 0.02f);
            ++i;
        }
        if (bl) {
            for (String string : brainData.behaviors()) {
                DebugRenderer.drawLargeFloatingText(matrices, vertexConsumers, entity, i, string, -16711681, 0.02f);
                ++i;
            }
        }
        if (bl) {
            for (String string : brainData.activities()) {
                DebugRenderer.drawLargeFloatingText(matrices, vertexConsumers, entity, i, string, -16711936, 0.02f);
                ++i;
            }
        }
        if (brainData.wantsGolem()) {
            DebugRenderer.drawLargeFloatingText(matrices, vertexConsumers, entity, i, "Wants Golem", -23296, 0.02f);
            ++i;
        }
        if (bl && brainData.angerLevel() != -1) {
            DebugRenderer.drawLargeFloatingText(matrices, vertexConsumers, entity, i, "Anger Level: " + brainData.angerLevel(), -98404, 0.02f);
            ++i;
        }
        if (bl) {
            for (String string : brainData.gossips()) {
                if (string.startsWith(brainData.name())) {
                    DebugRenderer.drawLargeFloatingText(matrices, vertexConsumers, entity, i, string, -1, 0.02f);
                } else {
                    DebugRenderer.drawLargeFloatingText(matrices, vertexConsumers, entity, i, string, -23296, 0.02f);
                }
                ++i;
            }
        }
        if (bl) {
            for (String string : Lists.reverse(brainData.memories())) {
                DebugRenderer.drawLargeFloatingText(matrices, vertexConsumers, entity, i, string, -3355444, 0.02f);
                ++i;
            }
        }
    }

    private boolean isTargeted(Entity entity) {
        return Objects.equals(this.targetedEntity, entity.getUuid());
    }

    public Map<BlockPos, List<String>> getGhostPointsOfInterest(DebugDataStore store) {
        HashMap<BlockPos, List<String>> map = Maps.newHashMap();
        store.forEachEntityData(DebugSubscriptionTypes.BRAINS, (entity, data) -> {
            for (BlockPos lv : Iterables.concat(data.pois(), data.potentialPois())) {
                map.computeIfAbsent(lv, pos -> Lists.newArrayList()).add(data.name());
            }
        });
        return map;
    }

    private void updateTargetedEntity() {
        DebugRenderer.getTargetedEntity(this.client.getCameraEntity(), 8).ifPresent(entity -> {
            this.targetedEntity = entity.getUuid();
        });
    }
}

