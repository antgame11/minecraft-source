/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.resource.waypoint;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public record WaypointStyleAsset(int nearDistance, int farDistance, List<Identifier> sprites, List<Identifier> spriteLocations) {
    @VisibleForTesting
    public static final String field_62050 = "hud/locator_bar_dot/";
    public static final int DEFAULT_NEAR_DISTANCE = 128;
    public static final int DEFAULT_FAR_DISTANCE = 332;
    private static final Codec<Integer> DISTANCE_CODEC = Codec.intRange(0, 60000000);
    public static final Codec<WaypointStyleAsset> CODEC = RecordCodecBuilder.create(instance -> instance.group(DISTANCE_CODEC.optionalFieldOf("near_distance", 128).forGetter(WaypointStyleAsset::nearDistance), DISTANCE_CODEC.optionalFieldOf("far_distance", 332).forGetter(WaypointStyleAsset::farDistance), ((MapCodec)Codecs.nonEmptyList(Identifier.CODEC.listOf()).fieldOf("sprites")).forGetter(WaypointStyleAsset::sprites)).apply((Applicative<WaypointStyleAsset, ?>)instance, WaypointStyleAsset::new)).validate(WaypointStyleAsset::validate);

    public WaypointStyleAsset(int nearDistance, int farDistance, List<Identifier> sprites) {
        this(nearDistance, farDistance, sprites, sprites.stream().map(id -> id.withPrefixedPath(field_62050)).toList());
    }

    @VisibleForTesting
    public DataResult<WaypointStyleAsset> validate() {
        if (this.sprites.isEmpty()) {
            return DataResult.error(() -> "Must have at least one sprite icon");
        }
        if (this.nearDistance <= 0) {
            return DataResult.error(() -> "Near distance (" + this.nearDistance + ") must be greater than zero");
        }
        if (this.nearDistance >= this.farDistance) {
            return DataResult.error(() -> "Far distance (" + this.farDistance + ") cannot be closer or equal to near distance (" + this.nearDistance + ")");
        }
        return DataResult.success(this);
    }

    public Identifier getSpriteForDistance(float distance) {
        if (distance < (float)this.nearDistance) {
            return this.spriteLocations.getFirst();
        }
        if (distance >= (float)this.farDistance) {
            return this.spriteLocations.getLast();
        }
        if (this.spriteLocations.size() == 1) {
            return this.spriteLocations.getFirst();
        }
        if (this.spriteLocations.size() == 3) {
            return this.spriteLocations.get(1);
        }
        int i = MathHelper.lerp((distance - (float)this.nearDistance) / (float)(this.farDistance - this.nearDistance), 1, this.spriteLocations.size() - 1);
        return this.spriteLocations.get(i);
    }
}

