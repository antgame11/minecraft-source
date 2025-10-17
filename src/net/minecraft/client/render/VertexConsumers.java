/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;

@Environment(value=EnvType.CLIENT)
public class VertexConsumers {
    public static VertexConsumer union() {
        throw new IllegalArgumentException();
    }

    public static VertexConsumer union(VertexConsumer first) {
        return first;
    }

    public static VertexConsumer union(VertexConsumer first, VertexConsumer second) {
        return new Dual(first, second);
    }

    public static VertexConsumer union(VertexConsumer ... delegates) {
        return new Union(delegates);
    }

    @Environment(value=EnvType.CLIENT)
    static class Dual
    implements VertexConsumer {
        private final VertexConsumer first;
        private final VertexConsumer second;

        public Dual(VertexConsumer first, VertexConsumer second) {
            if (first == second) {
                throw new IllegalArgumentException("Duplicate delegates");
            }
            this.first = first;
            this.second = second;
        }

        @Override
        public VertexConsumer vertex(float x, float y, float z) {
            this.first.vertex(x, y, z);
            this.second.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            this.first.color(red, green, blue, alpha);
            this.second.color(red, green, blue, alpha);
            return this;
        }

        @Override
        public VertexConsumer texture(float u, float v) {
            this.first.texture(u, v);
            this.second.texture(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlay(int u, int v) {
            this.first.overlay(u, v);
            this.second.overlay(u, v);
            return this;
        }

        @Override
        public VertexConsumer light(int u, int v) {
            this.first.light(u, v);
            this.second.light(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            this.first.normal(x, y, z);
            this.second.normal(x, y, z);
            return this;
        }

        @Override
        public void vertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
            this.first.vertex(x, y, z, color, u, v, overlay, light, normalX, normalY, normalZ);
            this.second.vertex(x, y, z, color, u, v, overlay, light, normalX, normalY, normalZ);
        }
    }

    @Environment(value=EnvType.CLIENT)
    record Union(VertexConsumer[] delegates) implements VertexConsumer
    {
        Union {
            for (int i = 0; i < delegates.length; ++i) {
                for (int j = i + 1; j < delegates.length; ++j) {
                    if (delegates[i] != delegates[j]) continue;
                    throw new IllegalArgumentException("Duplicate delegates");
                }
            }
        }

        private void delegate(Consumer<VertexConsumer> action) {
            for (VertexConsumer lv : this.delegates) {
                action.accept(lv);
            }
        }

        @Override
        public VertexConsumer vertex(float x, float y, float z) {
            this.delegate(vertexConsumer -> vertexConsumer.vertex(x, y, z));
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            this.delegate(vertexConsumer -> vertexConsumer.color(red, green, blue, alpha));
            return this;
        }

        @Override
        public VertexConsumer texture(float u, float v) {
            this.delegate(vertexConsumer -> vertexConsumer.texture(u, v));
            return this;
        }

        @Override
        public VertexConsumer overlay(int u, int v) {
            this.delegate(vertexConsumer -> vertexConsumer.overlay(u, v));
            return this;
        }

        @Override
        public VertexConsumer light(int u, int v) {
            this.delegate(vertexConsumer -> vertexConsumer.light(u, v));
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            this.delegate(vertexConsumer -> vertexConsumer.normal(x, y, z));
            return this;
        }

        @Override
        public void vertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
            this.delegate(vertexConsumer -> vertexConsumer.vertex(x, y, z, color, u, v, overlay, light, normalX, normalY, normalZ));
        }
    }
}

