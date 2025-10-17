/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.goal;

import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.goal.DoorInteractGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldEvents;

public class BreakDoorGoal
extends DoorInteractGoal {
    private static final int MIN_MAX_PROGRESS = 240;
    private final Predicate<Difficulty> difficultySufficientPredicate;
    protected int breakProgress;
    protected int lastBreakProgress = -1;
    protected int maxProgress = -1;

    public BreakDoorGoal(MobEntity mob, Predicate<Difficulty> difficultySufficientPredicate) {
        super(mob);
        this.difficultySufficientPredicate = difficultySufficientPredicate;
    }

    public BreakDoorGoal(MobEntity mob, int maxProgress, Predicate<Difficulty> difficultySufficientPredicate) {
        this(mob, difficultySufficientPredicate);
        this.maxProgress = maxProgress;
    }

    protected int getMaxProgress() {
        return Math.max(240, this.maxProgress);
    }

    @Override
    public boolean canStart() {
        if (!super.canStart()) {
            return false;
        }
        if (!BreakDoorGoal.getServerWorld(this.mob).getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            return false;
        }
        return this.isDifficultySufficient(this.mob.getEntityWorld().getDifficulty()) && !this.isDoorOpen();
    }

    @Override
    public void start() {
        super.start();
        this.breakProgress = 0;
    }

    @Override
    public boolean shouldContinue() {
        return this.breakProgress <= this.getMaxProgress() && !this.isDoorOpen() && this.doorPos.isWithinDistance(this.mob.getEntityPos(), 2.0) && this.isDifficultySufficient(this.mob.getEntityWorld().getDifficulty());
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.getEntityWorld().setBlockBreakingInfo(this.mob.getId(), this.doorPos, -1);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.mob.getRandom().nextInt(20) == 0) {
            this.mob.getEntityWorld().syncWorldEvent(WorldEvents.ZOMBIE_ATTACKS_WOODEN_DOOR, this.doorPos, 0);
            if (!this.mob.handSwinging) {
                this.mob.swingHand(this.mob.getActiveHand());
            }
        }
        ++this.breakProgress;
        int i = (int)((float)this.breakProgress / (float)this.getMaxProgress() * 10.0f);
        if (i != this.lastBreakProgress) {
            this.mob.getEntityWorld().setBlockBreakingInfo(this.mob.getId(), this.doorPos, i);
            this.lastBreakProgress = i;
        }
        if (this.breakProgress == this.getMaxProgress() && this.isDifficultySufficient(this.mob.getEntityWorld().getDifficulty())) {
            this.mob.getEntityWorld().removeBlock(this.doorPos, false);
            this.mob.getEntityWorld().syncWorldEvent(WorldEvents.ZOMBIE_BREAKS_WOODEN_DOOR, this.doorPos, 0);
            this.mob.getEntityWorld().syncWorldEvent(WorldEvents.BLOCK_BROKEN, this.doorPos, Block.getRawIdFromState(this.mob.getEntityWorld().getBlockState(this.doorPos)));
        }
    }

    private boolean isDifficultySufficient(Difficulty difficulty) {
        return this.difficultySufficientPredicate.test(difficulty);
    }
}

