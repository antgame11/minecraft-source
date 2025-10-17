/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import java.util.function.Consumer;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.entity.Entity;

public enum CollisionEvent {
    FREEZE(entity -> {
        entity.setInPowderSnow(true);
        if (entity.canFreeze()) {
            entity.setFrozenTicks(Math.min(entity.getMinFreezeDamageTicks(), entity.getFrozenTicks() + 1));
        }
    }),
    CLEAR_FREEZE(Entity::defrost),
    FIRE_IGNITE(AbstractFireBlock::igniteEntity),
    LAVA_IGNITE(Entity::igniteByLava),
    EXTINGUISH(Entity::extinguish);

    private final Consumer<Entity> action;

    private CollisionEvent(Consumer<Entity> action) {
        this.action = action;
    }

    public Consumer<Entity> getAction() {
        return this.action;
    }
}

