/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.debug;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;

@Environment(value=EnvType.CLIENT)
public class GameTestDebugRenderer {
    private static final int field_62972 = 10000;
    private static final float MARKER_BOX_SIZE = 0.02f;
    private final Map<BlockPos, Marker> markers = Maps.newHashMap();

    public void addMarker(BlockPos absolutePos, BlockPos relativePos) {
        String string = relativePos.toShortString();
        this.markers.put(absolutePos, new Marker(-2147418368, string, Util.getMeasuringTimeMs() + 10000L));
    }

    public void clear() {
        this.markers.clear();
    }

    public void renderMarkers(MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        long l = Util.getMeasuringTimeMs();
        this.markers.entrySet().removeIf(entry -> l > ((Marker)entry.getValue()).removalTime);
        this.markers.forEach((pos, marker) -> this.renderMarker(matrices, vertexConsumers, (BlockPos)pos, (Marker)marker));
    }

    private void renderMarker(MatrixStack matrices, VertexConsumerProvider vertexConsumers, BlockPos pos, Marker marker) {
        DebugRenderer.drawBox(matrices, vertexConsumers, pos, 0.02f, marker.getRed(), marker.getGreen(), marker.getBlue(), marker.getAlpha() * 0.75f);
        if (!marker.message.isEmpty()) {
            double d = (double)pos.getX() + 0.5;
            double e = (double)pos.getY() + 1.2;
            double f = (double)pos.getZ() + 0.5;
            DebugRenderer.drawString(matrices, vertexConsumers, marker.message, d, e, f, Colors.WHITE, 0.01f, true, 0.0f, true);
        }
    }

    @Environment(value=EnvType.CLIENT)
    record Marker(int color, String message, long removalTime) {
        public float getRed() {
            return ColorHelper.getRedFloat(this.color);
        }

        public float getGreen() {
            return ColorHelper.getGreenFloat(this.color);
        }

        public float getBlue() {
            return ColorHelper.getBlueFloat(this.color);
        }

        public float getAlpha() {
            return ColorHelper.getAlphaFloat(this.color);
        }
    }
}

