/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.AbstractPhase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class HoverPhase
extends AbstractPhase {
    @Nullable
    private Vec3d target;

    public HoverPhase(EnderDragonEntity arg) {
        super(arg);
    }

    @Override
    public void serverTick(ServerWorld world) {
        if (this.target == null) {
            this.target = this.dragon.getEntityPos();
        }
    }

    @Override
    public boolean isSittingOrHovering() {
        return true;
    }

    @Override
    public void beginPhase() {
        this.target = null;
    }

    @Override
    public float getMaxYAcceleration() {
        return 1.0f;
    }

    @Override
    @Nullable
    public Vec3d getPathTarget() {
        return this.target;
    }

    public PhaseType<HoverPhase> getType() {
        return PhaseType.HOVER;
    }
}

