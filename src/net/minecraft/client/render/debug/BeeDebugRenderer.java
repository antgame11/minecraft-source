/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.debug;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.NameGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.debug.DebugDataStore;
import net.minecraft.world.debug.DebugSubscriptionTypes;
import net.minecraft.world.debug.data.BeeDebugData;
import net.minecraft.world.debug.data.BeeHiveDebugData;
import net.minecraft.world.debug.data.GoalSelectorDebugData;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BeeDebugRenderer
implements DebugRenderer.Renderer {
    private static final boolean field_32841 = true;
    private static final boolean field_32842 = true;
    private static final boolean field_32843 = true;
    private static final boolean field_32844 = true;
    private static final boolean field_32845 = true;
    private static final boolean field_32847 = true;
    private static final boolean field_32848 = true;
    private static final boolean field_32849 = true;
    private static final boolean field_32850 = true;
    private static final boolean field_32851 = true;
    private static final boolean field_32853 = true;
    private static final boolean field_32854 = true;
    private static final int HIVE_RANGE = 30;
    private static final int BEE_RANGE = 30;
    private static final int TARGET_ENTITY_RANGE = 8;
    private static final float DEFAULT_DRAWN_STRING_SIZE = 0.02f;
    private static final int ORANGE = -23296;
    private static final int GRAY = -3355444;
    private static final int PINK = -98404;
    private final MinecraftClient client;
    @Nullable
    private UUID targetedEntity;

    public BeeDebugRenderer(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ, DebugDataStore store, Frustum frustum) {
        this.render(matrices, vertexConsumers, store);
        if (!this.client.player.isSpectator()) {
            this.updateTargetedEntity();
        }
    }

    private void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, DebugDataStore dataStore) {
        BlockPos lv = this.getCameraPos().getBlockPos();
        dataStore.forEachEntityData(DebugSubscriptionTypes.BEES, (entity, data) -> {
            if (this.client.player.isInRange((Entity)entity, 30.0)) {
                GoalSelectorDebugData lv = dataStore.getEntityData(DebugSubscriptionTypes.GOAL_SELECTORS, (Entity)entity);
                this.drawBee(matrices, vertexConsumers, (Entity)entity, (BeeDebugData)data, lv);
            }
        });
        this.drawFlowers(matrices, vertexConsumers, dataStore);
        Map<BlockPos, Set<UUID>> map = this.getBlacklistingBees(dataStore);
        dataStore.forEachBlockData(DebugSubscriptionTypes.BEE_HIVES, (pos, data) -> {
            if (lv.isWithinDistance((Vec3i)pos, 30.0)) {
                BeeDebugRenderer.drawHive(matrices, vertexConsumers, pos);
                Set<UUID> set = map.getOrDefault(pos, Set.of());
                this.drawHiveInfo(matrices, vertexConsumers, (BlockPos)pos, (BeeHiveDebugData)data, (Collection<UUID>)set, dataStore);
            }
        });
        this.getBeesByHive(dataStore).forEach((hive, bees) -> {
            if (lv.isWithinDistance((Vec3i)hive, 30.0)) {
                this.drawHiveBees(matrices, vertexConsumers, (BlockPos)hive, (List<String>)bees);
            }
        });
    }

    private Map<BlockPos, Set<UUID>> getBlacklistingBees(DebugDataStore dataStore) {
        HashMap<BlockPos, Set<UUID>> map = new HashMap<BlockPos, Set<UUID>>();
        dataStore.forEachEntityData(DebugSubscriptionTypes.BEES, (entity, data) -> {
            for (BlockPos lv : data.blacklistedHives()) {
                map.computeIfAbsent(lv, pos -> new HashSet()).add(entity.getUuid());
            }
        });
        return map;
    }

    private void drawFlowers(MatrixStack matrices, VertexConsumerProvider vertexConsumers, DebugDataStore dataStore) {
        HashMap<BlockPos, Set> map = new HashMap<BlockPos, Set>();
        dataStore.forEachEntityData(DebugSubscriptionTypes.BEES, (entity, data) -> {
            if (data.flowerPos().isPresent()) {
                map.computeIfAbsent(data.flowerPos().get(), flower -> new HashSet()).add(entity.getUuid());
            }
        });
        map.forEach((flowerPos, bees) -> {
            Set set2 = bees.stream().map(NameGenerator::name).collect(Collectors.toSet());
            int i = 1;
            DebugRenderer.drawFloatingText(matrices, vertexConsumers, set2.toString(), flowerPos, i++, -256, 0.02f);
            DebugRenderer.drawFloatingText(matrices, vertexConsumers, "Flower", flowerPos, i++, -1, 0.02f);
            float f = 0.05f;
            DebugRenderer.drawBox(matrices, vertexConsumers, flowerPos, 0.05f, 0.8f, 0.8f, 0.0f, 0.3f);
        });
    }

    private static String toString(Collection<UUID> bees) {
        if (bees.isEmpty()) {
            return "-";
        }
        if (bees.size() > 3) {
            return bees.size() + " bees";
        }
        return bees.stream().map(NameGenerator::name).collect(Collectors.toSet()).toString();
    }

    private static void drawHive(MatrixStack matrices, VertexConsumerProvider vertexConsumers, BlockPos pos) {
        float f = 0.05f;
        DebugRenderer.drawBox(matrices, vertexConsumers, pos, 0.05f, 0.2f, 0.2f, 1.0f, 0.3f);
    }

    private void drawHiveBees(MatrixStack matrices, VertexConsumerProvider vertexConsumers, BlockPos pos, List<String> bees) {
        float f = 0.05f;
        DebugRenderer.drawBox(matrices, vertexConsumers, pos, 0.05f, 0.2f, 0.2f, 1.0f, 0.3f);
        DebugRenderer.drawFloatingText(matrices, vertexConsumers, bees.toString(), pos, 0, -256, 0.02f);
        DebugRenderer.drawFloatingText(matrices, vertexConsumers, "Ghost Hive", pos, 1, -65536, 0.02f);
    }

    private void drawHiveInfo(MatrixStack matrices, VertexConsumerProvider vertexConsumers, BlockPos pos, BeeHiveDebugData data, Collection<UUID> blacklistingBees, DebugDataStore dataStore) {
        int i = 0;
        if (!blacklistingBees.isEmpty()) {
            BeeDebugRenderer.drawString(matrices, vertexConsumers, "Blacklisted by " + BeeDebugRenderer.toString(blacklistingBees), pos, data, i++, -65536);
        }
        BeeDebugRenderer.drawString(matrices, vertexConsumers, "Out: " + BeeDebugRenderer.toString(this.getBeesForHive(pos, dataStore)), pos, data, i++, -3355444);
        if (data.occupantCount() == 0) {
            BeeDebugRenderer.drawString(matrices, vertexConsumers, "In: -", pos, data, i++, -256);
        } else if (data.occupantCount() == 1) {
            BeeDebugRenderer.drawString(matrices, vertexConsumers, "In: 1 bee", pos, data, i++, -256);
        } else {
            BeeDebugRenderer.drawString(matrices, vertexConsumers, "In: " + data.occupantCount() + " bees", pos, data, i++, -256);
        }
        BeeDebugRenderer.drawString(matrices, vertexConsumers, "Honey: " + data.honeyLevel(), pos, data, i++, -23296);
        BeeDebugRenderer.drawString(matrices, vertexConsumers, data.type().getName().getString() + (data.sedated() ? " (sedated)" : ""), pos, data, i++, -1);
    }

    private void drawBee(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity bee, BeeDebugData data, @Nullable GoalSelectorDebugData goalData) {
        boolean bl = this.isTargeted(bee);
        int i = 0;
        DebugRenderer.drawLargeFloatingText(matrices, vertexConsumers, bee, i++, data.toString(), -1, 0.03f);
        if (data.hivePos().isEmpty()) {
            DebugRenderer.drawLargeFloatingText(matrices, vertexConsumers, bee, i++, "No hive", -98404, 0.02f);
        } else {
            DebugRenderer.drawLargeFloatingText(matrices, vertexConsumers, bee, i++, "Hive: " + this.getPositionString(bee, data.hivePos().get()), -256, 0.02f);
        }
        if (data.flowerPos().isEmpty()) {
            DebugRenderer.drawLargeFloatingText(matrices, vertexConsumers, bee, i++, "No flower", -98404, 0.02f);
        } else {
            DebugRenderer.drawLargeFloatingText(matrices, vertexConsumers, bee, i++, "Flower: " + this.getPositionString(bee, data.flowerPos().get()), -256, 0.02f);
        }
        if (goalData != null) {
            for (GoalSelectorDebugData.Goal lv : goalData.goals()) {
                if (!lv.isRunning()) continue;
                DebugRenderer.drawLargeFloatingText(matrices, vertexConsumers, bee, i++, lv.name(), -16711936, 0.02f);
            }
        }
        if (data.travelTicks() > 0) {
            int j = data.travelTicks() < 2400 ? -3355444 : -23296;
            DebugRenderer.drawLargeFloatingText(matrices, vertexConsumers, bee, i++, "Travelling: " + data.travelTicks() + " ticks", j, 0.02f);
        }
    }

    private static void drawString(MatrixStack matrices, VertexConsumerProvider vertexConsumers, String string, BlockPos pos, BeeHiveDebugData arg4, int lineNumber, int color) {
        DebugRenderer.drawFloatingText(matrices, vertexConsumers, string, pos, lineNumber, color, 0.02f);
    }

    private Camera getCameraPos() {
        return this.client.gameRenderer.getCamera();
    }

    private String getPositionString(Entity bee, BlockPos pos) {
        double d = pos.getSquaredDistance(bee.getEntityPos());
        double e = (double)Math.round(d * 10.0) / 10.0;
        return pos.toShortString() + " (dist " + e + ")";
    }

    private boolean isTargeted(Entity bee) {
        return Objects.equals(this.targetedEntity, bee.getUuid());
    }

    private Collection<UUID> getBeesForHive(BlockPos pos, DebugDataStore dataStore) {
        HashSet<UUID> set = new HashSet<UUID>();
        dataStore.forEachEntityData(DebugSubscriptionTypes.BEES, (entity, data) -> {
            if (data.hivePosEquals(pos)) {
                set.add(entity.getUuid());
            }
        });
        return set;
    }

    private Map<BlockPos, List<String>> getBeesByHive(DebugDataStore dataStore) {
        HashMap<BlockPos, List<String>> map = new HashMap<BlockPos, List<String>>();
        dataStore.forEachEntityData(DebugSubscriptionTypes.BEES, (entity, data) -> {
            if (data.hivePos().isPresent() && dataStore.getBlockData(DebugSubscriptionTypes.BEE_HIVES, data.hivePos().get()) == null) {
                map.computeIfAbsent(data.hivePos().get(), hive -> Lists.newArrayList()).add(NameGenerator.name(entity));
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

