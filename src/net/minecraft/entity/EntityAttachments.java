/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class EntityAttachments {
    private final Map<EntityAttachmentType, List<Vec3d>> points;

    EntityAttachments(Map<EntityAttachmentType, List<Vec3d>> points) {
        this.points = points;
    }

    public static EntityAttachments of(float width, float height) {
        return EntityAttachments.builder().build(width, height);
    }

    public static Builder builder() {
        return new Builder();
    }

    public EntityAttachments scale(float xScale, float yScale, float zScale) {
        return new EntityAttachments(Util.mapEnum(EntityAttachmentType.class, type -> {
            ArrayList<Vec3d> list = new ArrayList<Vec3d>();
            for (Vec3d lv : this.points.get(type)) {
                list.add(lv.multiply(xScale, yScale, zScale));
            }
            return list;
        }));
    }

    @Nullable
    public Vec3d getPointNullable(EntityAttachmentType type, int index, float yaw) {
        List<Vec3d> list = this.points.get((Object)type);
        if (index < 0 || index >= list.size()) {
            return null;
        }
        return EntityAttachments.rotatePoint(list.get(index), yaw);
    }

    public Vec3d getPoint(EntityAttachmentType type, int index, float yaw) {
        Vec3d lv = this.getPointNullable(type, index, yaw);
        if (lv == null) {
            throw new IllegalStateException("Had no attachment point of type: " + String.valueOf((Object)type) + " for index: " + index);
        }
        return lv;
    }

    public Vec3d getPointOrDefault(EntityAttachmentType type) {
        List<Vec3d> list = this.points.get((Object)type);
        if (list == null || list.isEmpty()) {
            throw new IllegalStateException("No attachment points of type: PASSENGER");
        }
        Vec3d lv = Vec3d.ZERO;
        for (Vec3d lv2 : list) {
            lv = lv.add(lv2);
        }
        return lv.multiply(1.0f / (float)list.size());
    }

    public Vec3d getPointOrDefault(EntityAttachmentType type, int index, float yaw) {
        List<Vec3d> list = this.points.get((Object)type);
        if (list.isEmpty()) {
            throw new IllegalStateException("Had no attachment points of type: " + String.valueOf((Object)type));
        }
        Vec3d lv = list.get(MathHelper.clamp(index, 0, list.size() - 1));
        return EntityAttachments.rotatePoint(lv, yaw);
    }

    private static Vec3d rotatePoint(Vec3d point, float yaw) {
        return point.rotateY(-yaw * ((float)Math.PI / 180));
    }

    public static class Builder {
        private final Map<EntityAttachmentType, List<Vec3d>> points = new EnumMap<EntityAttachmentType, List<Vec3d>>(EntityAttachmentType.class);

        Builder() {
        }

        public Builder add(EntityAttachmentType type, float x, float y, float z) {
            return this.add(type, new Vec3d(x, y, z));
        }

        public Builder add(EntityAttachmentType type, Vec3d point) {
            this.points.computeIfAbsent(type, list -> new ArrayList(1)).add(point);
            return this;
        }

        public EntityAttachments build(float width, float height) {
            Map<EntityAttachmentType, List<Vec3d>> map = Util.mapEnum(EntityAttachmentType.class, type -> {
                List<Vec3d> list = this.points.get(type);
                return list == null ? type.createPoint(width, height) : List.copyOf(list);
            });
            return new EntityAttachments(map);
        }
    }
}

