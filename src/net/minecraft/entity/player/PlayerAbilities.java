/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.player;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class PlayerAbilities {
    private static final boolean DEFAULT_INVULNERABLE = false;
    private static final boolean DEFAULT_FLYING = false;
    private static final boolean DEFAULT_ALLOW_FLYING = false;
    private static final boolean DEFAULT_CREATIVE_MODE = false;
    private static final boolean DEFAULT_ALLOW_MODIFY_WORLD = true;
    private static final float DEFAULT_FLY_SPEED = 0.05f;
    private static final float DEFAULT_WALK_SPEED = 0.1f;
    public boolean invulnerable;
    public boolean flying;
    public boolean allowFlying;
    public boolean creativeMode;
    public boolean allowModifyWorld = true;
    private float flySpeed = 0.05f;
    private float walkSpeed = 0.1f;

    public float getFlySpeed() {
        return this.flySpeed;
    }

    public void setFlySpeed(float flySpeed) {
        this.flySpeed = flySpeed;
    }

    public float getWalkSpeed() {
        return this.walkSpeed;
    }

    public void setWalkSpeed(float walkSpeed) {
        this.walkSpeed = walkSpeed;
    }

    public Packed pack() {
        return new Packed(this.invulnerable, this.flying, this.allowFlying, this.creativeMode, this.allowModifyWorld, this.flySpeed, this.walkSpeed);
    }

    public void unpack(Packed packed) {
        this.invulnerable = packed.invulnerable;
        this.flying = packed.flying;
        this.allowFlying = packed.mayFly;
        this.creativeMode = packed.instabuild;
        this.allowModifyWorld = packed.mayBuild;
        this.flySpeed = packed.flyingSpeed;
        this.walkSpeed = packed.walkingSpeed;
    }

    public record Packed(boolean invulnerable, boolean flying, boolean mayFly, boolean instabuild, boolean mayBuild, float flyingSpeed, float walkingSpeed) {
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.BOOL.fieldOf("invulnerable")).orElse(false).forGetter(Packed::invulnerable), ((MapCodec)Codec.BOOL.fieldOf("flying")).orElse(false).forGetter(Packed::flying), ((MapCodec)Codec.BOOL.fieldOf("mayfly")).orElse(false).forGetter(Packed::mayFly), ((MapCodec)Codec.BOOL.fieldOf("instabuild")).orElse(false).forGetter(Packed::instabuild), ((MapCodec)Codec.BOOL.fieldOf("mayBuild")).orElse(true).forGetter(Packed::mayBuild), ((MapCodec)Codec.FLOAT.fieldOf("flySpeed")).orElse(Float.valueOf(0.05f)).forGetter(Packed::flyingSpeed), ((MapCodec)Codec.FLOAT.fieldOf("walkSpeed")).orElse(Float.valueOf(0.1f)).forGetter(Packed::walkingSpeed)).apply((Applicative<Packed, ?>)instance, Packed::new));
    }
}

