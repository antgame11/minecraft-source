/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package com.mojang.blaze3d.systems;

import com.google.common.primitives.Floats;
import it.unimi.dsi.fastutil.ints.IntArrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.Vec3fArray;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public interface VertexSorter {
    public static final VertexSorter BY_DISTANCE = VertexSorter.byDistance(0.0f, 0.0f, 0.0f);
    public static final VertexSorter BY_Z = VertexSorter.of(vec -> -vec.z());

    public static VertexSorter byDistance(float originX, float originY, float originZ) {
        return VertexSorter.byDistance(new Vector3f(originX, originY, originZ));
    }

    public static VertexSorter byDistance(Vector3fc origin) {
        return VertexSorter.of(origin::distanceSquared);
    }

    public static VertexSorter of(SortKeyMapper mapper) {
        return vectors -> {
            Vector3f vector3f = new Vector3f();
            float[] fs = new float[vectors.size()];
            int[] is = new int[vectors.size()];
            for (int i = 0; i < vectors.size(); ++i) {
                fs[i] = mapper.apply(vectors.get(i, vector3f));
                is[i] = i;
            }
            IntArrays.mergeSort(is, (a, b) -> Floats.compare(fs[b], fs[a]));
            return is;
        };
    }

    public int[] sort(Vec3fArray var1);

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface SortKeyMapper {
        public float apply(Vector3f var1);
    }
}

