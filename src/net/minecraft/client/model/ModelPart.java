/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public final class ModelPart {
    public static final float field_37937 = 1.0f;
    public float originX;
    public float originY;
    public float originZ;
    public float pitch;
    public float yaw;
    public float roll;
    public float xScale = 1.0f;
    public float yScale = 1.0f;
    public float zScale = 1.0f;
    public boolean visible = true;
    public boolean hidden;
    private final List<Cuboid> cuboids;
    private final Map<String, ModelPart> children;
    private ModelTransform defaultTransform = ModelTransform.NONE;

    public ModelPart(List<Cuboid> cuboids, Map<String, ModelPart> children) {
        this.cuboids = cuboids;
        this.children = children;
    }

    public ModelTransform getTransform() {
        return ModelTransform.of(this.originX, this.originY, this.originZ, this.pitch, this.yaw, this.roll);
    }

    public ModelTransform getDefaultTransform() {
        return this.defaultTransform;
    }

    public void setDefaultTransform(ModelTransform transform) {
        this.defaultTransform = transform;
    }

    public void resetTransform() {
        this.setTransform(this.defaultTransform);
    }

    public void setTransform(ModelTransform transform) {
        this.originX = transform.x();
        this.originY = transform.y();
        this.originZ = transform.z();
        this.pitch = transform.pitch();
        this.yaw = transform.yaw();
        this.roll = transform.roll();
        this.xScale = transform.xScale();
        this.yScale = transform.yScale();
        this.zScale = transform.zScale();
    }

    public boolean hasChild(String child) {
        return this.children.containsKey(child);
    }

    public ModelPart getChild(String name) {
        ModelPart lv = this.children.get(name);
        if (lv == null) {
            throw new NoSuchElementException("Can't find part " + name);
        }
        return lv;
    }

    public void setOrigin(float x, float y, float z) {
        this.originX = x;
        this.originY = y;
        this.originZ = z;
    }

    public void setAngles(float pitch, float yaw, float roll) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }

    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        this.render(matrices, vertices, light, overlay, -1);
    }

    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        if (!this.visible) {
            return;
        }
        if (this.cuboids.isEmpty() && this.children.isEmpty()) {
            return;
        }
        matrices.push();
        this.applyTransform(matrices);
        if (!this.hidden) {
            this.renderCuboids(matrices.peek(), vertices, light, overlay, color);
        }
        for (ModelPart lv : this.children.values()) {
            lv.render(matrices, vertices, light, overlay, color);
        }
        matrices.pop();
    }

    public void rotate(Quaternionf quaternion) {
        Matrix3f matrix3f = new Matrix3f().rotationZYX(this.roll, this.yaw, this.pitch);
        Matrix3f matrix3f2 = matrix3f.rotate(quaternion);
        Vector3f vector3f = matrix3f2.getEulerAnglesZYX(new Vector3f());
        this.setAngles(vector3f.x, vector3f.y, vector3f.z);
    }

    public void collectVertices(MatrixStack matrices, Set<Vector3f> vertices) {
        this.forEachCuboid(matrices, (matrix, path, index, cuboid) -> {
            for (Quad lv : cuboid.sides) {
                for (Vertex lv2 : lv.vertices()) {
                    float f = lv2.worldX();
                    float g = lv2.worldY();
                    float h = lv2.worldZ();
                    Vector3f vector3f = matrix.getPositionMatrix().transformPosition(f, g, h, new Vector3f());
                    vertices.add(vector3f);
                }
            }
        });
    }

    public void forEachCuboid(MatrixStack matrices, CuboidConsumer consumer) {
        this.forEachCuboid(matrices, consumer, "");
    }

    private void forEachCuboid(MatrixStack matrices, CuboidConsumer consumer, String path) {
        if (this.cuboids.isEmpty() && this.children.isEmpty()) {
            return;
        }
        matrices.push();
        this.applyTransform(matrices);
        MatrixStack.Entry lv = matrices.peek();
        for (int i = 0; i < this.cuboids.size(); ++i) {
            consumer.accept(lv, path, i, this.cuboids.get(i));
        }
        String string2 = path + "/";
        this.children.forEach((name, part) -> part.forEachCuboid(matrices, consumer, string2 + name));
        matrices.pop();
    }

    public void applyTransform(MatrixStack matrices) {
        matrices.translate(this.originX / 16.0f, this.originY / 16.0f, this.originZ / 16.0f);
        if (this.pitch != 0.0f || this.yaw != 0.0f || this.roll != 0.0f) {
            matrices.multiply(new Quaternionf().rotationZYX(this.roll, this.yaw, this.pitch));
        }
        if (this.xScale != 1.0f || this.yScale != 1.0f || this.zScale != 1.0f) {
            matrices.scale(this.xScale, this.yScale, this.zScale);
        }
    }

    private void renderCuboids(MatrixStack.Entry entry, VertexConsumer vertexConsumer, int light, int overlay, int color) {
        for (Cuboid lv : this.cuboids) {
            lv.renderCuboid(entry, vertexConsumer, light, overlay, color);
        }
    }

    public Cuboid getRandomCuboid(Random random) {
        return this.cuboids.get(random.nextInt(this.cuboids.size()));
    }

    public boolean isEmpty() {
        return this.cuboids.isEmpty();
    }

    public void moveOrigin(Vector3f vec3f) {
        this.originX += vec3f.x();
        this.originY += vec3f.y();
        this.originZ += vec3f.z();
    }

    public void rotate(Vector3f vec3f) {
        this.pitch += vec3f.x();
        this.yaw += vec3f.y();
        this.roll += vec3f.z();
    }

    public void scale(Vector3f vec3f) {
        this.xScale += vec3f.x();
        this.yScale += vec3f.y();
        this.zScale += vec3f.z();
    }

    public List<ModelPart> traverse() {
        ArrayList<ModelPart> list = new ArrayList<ModelPart>();
        list.add(this);
        this.forEachChild((key, part) -> list.add((ModelPart)part));
        return List.copyOf(list);
    }

    public Function<String, ModelPart> createPartGetter() {
        HashMap<String, ModelPart> map = new HashMap<String, ModelPart>();
        map.put("root", this);
        this.forEachChild(map::putIfAbsent);
        return map::get;
    }

    private void forEachChild(BiConsumer<String, ModelPart> partBiConsumer) {
        for (Map.Entry<String, ModelPart> entry : this.children.entrySet()) {
            partBiConsumer.accept(entry.getKey(), entry.getValue());
        }
        for (ModelPart lv : this.children.values()) {
            lv.forEachChild(partBiConsumer);
        }
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface CuboidConsumer {
        public void accept(MatrixStack.Entry var1, String var2, int var3, Cuboid var4);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Cuboid {
        public final Quad[] sides;
        public final float minX;
        public final float minY;
        public final float minZ;
        public final float maxX;
        public final float maxY;
        public final float maxZ;

        public Cuboid(int u, int v, float x, float y, float z, float sizeX, float sizeY, float sizeZ, float extraX, float extraY, float extraZ, boolean mirror, float textureWidth, float textureHeight, Set<Direction> sides) {
            this.minX = x;
            this.minY = y;
            this.minZ = z;
            this.maxX = x + sizeX;
            this.maxY = y + sizeY;
            this.maxZ = z + sizeZ;
            this.sides = new Quad[sides.size()];
            float s = x + sizeX;
            float t = y + sizeY;
            float u2 = z + sizeZ;
            x -= extraX;
            y -= extraY;
            z -= extraZ;
            s += extraX;
            t += extraY;
            u2 += extraZ;
            if (mirror) {
                float v2 = s;
                s = x;
                x = v2;
            }
            Vertex lv = new Vertex(x, y, z, 0.0f, 0.0f);
            Vertex lv2 = new Vertex(s, y, z, 0.0f, 8.0f);
            Vertex lv3 = new Vertex(s, t, z, 8.0f, 8.0f);
            Vertex lv4 = new Vertex(x, t, z, 8.0f, 0.0f);
            Vertex lv5 = new Vertex(x, y, u2, 0.0f, 0.0f);
            Vertex lv6 = new Vertex(s, y, u2, 0.0f, 8.0f);
            Vertex lv7 = new Vertex(s, t, u2, 8.0f, 8.0f);
            Vertex lv8 = new Vertex(x, t, u2, 8.0f, 0.0f);
            float w = u;
            float x2 = (float)u + sizeZ;
            float y2 = (float)u + sizeZ + sizeX;
            float z2 = (float)u + sizeZ + sizeX + sizeX;
            float aa = (float)u + sizeZ + sizeX + sizeZ;
            float ab = (float)u + sizeZ + sizeX + sizeZ + sizeX;
            float ac = v;
            float ad = (float)v + sizeZ;
            float ae = (float)v + sizeZ + sizeY;
            int af = 0;
            if (sides.contains(Direction.DOWN)) {
                this.sides[af++] = new Quad(new Vertex[]{lv6, lv5, lv, lv2}, x2, ac, y2, ad, textureWidth, textureHeight, mirror, Direction.DOWN);
            }
            if (sides.contains(Direction.UP)) {
                this.sides[af++] = new Quad(new Vertex[]{lv3, lv4, lv8, lv7}, y2, ad, z2, ac, textureWidth, textureHeight, mirror, Direction.UP);
            }
            if (sides.contains(Direction.WEST)) {
                this.sides[af++] = new Quad(new Vertex[]{lv, lv5, lv8, lv4}, w, ad, x2, ae, textureWidth, textureHeight, mirror, Direction.WEST);
            }
            if (sides.contains(Direction.NORTH)) {
                this.sides[af++] = new Quad(new Vertex[]{lv2, lv, lv4, lv3}, x2, ad, y2, ae, textureWidth, textureHeight, mirror, Direction.NORTH);
            }
            if (sides.contains(Direction.EAST)) {
                this.sides[af++] = new Quad(new Vertex[]{lv6, lv2, lv3, lv7}, y2, ad, aa, ae, textureWidth, textureHeight, mirror, Direction.EAST);
            }
            if (sides.contains(Direction.SOUTH)) {
                this.sides[af] = new Quad(new Vertex[]{lv5, lv6, lv7, lv8}, aa, ad, ab, ae, textureWidth, textureHeight, mirror, Direction.SOUTH);
            }
        }

        public void renderCuboid(MatrixStack.Entry entry, VertexConsumer vertexConsumer, int light, int overlay, int color) {
            Matrix4f matrix4f = entry.getPositionMatrix();
            Vector3f vector3f = new Vector3f();
            for (Quad lv : this.sides) {
                Vector3f vector3f2 = entry.transformNormal(lv.direction, vector3f);
                float f = vector3f2.x();
                float g = vector3f2.y();
                float h = vector3f2.z();
                for (Vertex lv2 : lv.vertices) {
                    float l = lv2.worldX();
                    float m = lv2.worldY();
                    float n = lv2.worldZ();
                    Vector3f vector3f3 = matrix4f.transformPosition(l, m, n, vector3f);
                    vertexConsumer.vertex(vector3f3.x(), vector3f3.y(), vector3f3.z(), color, lv2.u, lv2.v, overlay, light, f, g, h);
                }
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Quad(Vertex[] vertices, Vector3fc direction) {
        public Quad(Vertex[] vertices, float u1, float v1, float u2, float v2, float squishU, float squishV, boolean flip, Direction direction) {
            this(vertices, (flip ? Quad.getMirrorDirection(direction) : direction).getFloatVector());
            float l = 0.0f / squishU;
            float m = 0.0f / squishV;
            vertices[0] = vertices[0].remap(u2 / squishU - l, v1 / squishV + m);
            vertices[1] = vertices[1].remap(u1 / squishU + l, v1 / squishV + m);
            vertices[2] = vertices[2].remap(u1 / squishU + l, v2 / squishV - m);
            vertices[3] = vertices[3].remap(u2 / squishU - l, v2 / squishV - m);
            if (flip) {
                int n = vertices.length;
                for (int o = 0; o < n / 2; ++o) {
                    Vertex lv = vertices[o];
                    vertices[o] = vertices[n - 1 - o];
                    vertices[n - 1 - o] = lv;
                }
            }
        }

        private static Direction getMirrorDirection(Direction direction) {
            return direction.getAxis() == Direction.Axis.X ? direction.getOpposite() : direction;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Vertex(float x, float y, float z, float u, float v) {
        public static final float SCALE_FACTOR = 16.0f;

        public Vertex remap(float u, float v) {
            return new Vertex(this.x, this.y, this.z, u, v);
        }

        public float worldX() {
            return this.x / 16.0f;
        }

        public float worldY() {
            return this.y / 16.0f;
        }

        public float worldZ() {
            return this.z / 16.0f;
        }
    }
}

