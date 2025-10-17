/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item.property.numeric;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HeldItemContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class NeedleAngleState {
    private final boolean wobble;

    protected NeedleAngleState(boolean wobble) {
        this.wobble = wobble;
    }

    public float getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable HeldItemContext pos, int seed) {
        World world2;
        if (pos == null) {
            pos = stack.getHolder();
        }
        if (pos == null) {
            return 0.0f;
        }
        if (world == null && pos != null && (world2 = pos.getEntityWorld()) instanceof ClientWorld) {
            ClientWorld lv;
            world = lv = (ClientWorld)world2;
        }
        if (world == null) {
            return 0.0f;
        }
        return this.getAngle(stack, world, seed, pos);
    }

    protected abstract float getAngle(ItemStack var1, ClientWorld var2, int var3, @Nullable HeldItemContext var4);

    protected boolean hasWobble() {
        return this.wobble;
    }

    protected Angler createAngler(float speedMultiplier) {
        return this.wobble ? NeedleAngleState.createWobblyAngler(speedMultiplier) : NeedleAngleState.createInstantAngler();
    }

    public static Angler createWobblyAngler(final float speedMultiplier) {
        return new Angler(){
            private float angle;
            private float speed;
            private long lastUpdateTime;

            @Override
            public float getAngle() {
                return this.angle;
            }

            @Override
            public boolean shouldUpdate(long time) {
                return this.lastUpdateTime != time;
            }

            @Override
            public void update(long time, float target) {
                this.lastUpdateTime = time;
                float g = MathHelper.floorMod(target - this.angle + 0.5f, 1.0f) - 0.5f;
                this.speed += g * 0.1f;
                this.speed *= speedMultiplier;
                this.angle = MathHelper.floorMod(this.angle + this.speed, 1.0f);
            }
        };
    }

    public static Angler createInstantAngler() {
        return new Angler(){
            private float angle;

            @Override
            public float getAngle() {
                return this.angle;
            }

            @Override
            public boolean shouldUpdate(long time) {
                return true;
            }

            @Override
            public void update(long time, float target) {
                this.angle = target;
            }
        };
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Angler {
        public float getAngle();

        public boolean shouldUpdate(long var1);

        public void update(long var1, float var3);
    }
}

