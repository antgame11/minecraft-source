/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item.map;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;

public record MapFrameMarker(BlockPos pos, int rotation, int entityId) {
    public static final Codec<MapFrameMarker> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockPos.CODEC.fieldOf("pos")).forGetter(MapFrameMarker::pos), ((MapCodec)Codec.INT.fieldOf("rotation")).forGetter(MapFrameMarker::rotation), ((MapCodec)Codec.INT.fieldOf("entity_id")).forGetter(MapFrameMarker::entityId)).apply((Applicative<MapFrameMarker, ?>)instance, MapFrameMarker::new));

    public String getKey() {
        return MapFrameMarker.getKey(this.pos);
    }

    public static String getKey(BlockPos pos) {
        return "frame-" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
}

