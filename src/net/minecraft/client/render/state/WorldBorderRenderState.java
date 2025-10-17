/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.state;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Direction;

@Environment(value=EnvType.CLIENT)
public class WorldBorderRenderState {
    public double minX;
    public double maxX;
    public double minZ;
    public double maxZ;
    public int tint;
    public double alpha;

    public List<Distance> nearestBorder(double x, double z) {
        Distance[] lvs = new Distance[]{new Distance(Direction.NORTH, z - this.minZ), new Distance(Direction.SOUTH, this.maxZ - z), new Distance(Direction.WEST, x - this.minX), new Distance(Direction.EAST, this.maxX - x)};
        return Arrays.stream(lvs).sorted(Comparator.comparingDouble(d -> d.value)).toList();
    }

    public void clear() {
        this.alpha = 0.0;
    }

    @Environment(value=EnvType.CLIENT)
    public record Distance(Direction direction, double value) {
    }
}

