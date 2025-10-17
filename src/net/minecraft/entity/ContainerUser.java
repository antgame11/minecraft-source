/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import net.minecraft.block.entity.ViewerCountManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

public interface ContainerUser {
    public boolean isViewingContainerAt(ViewerCountManager var1, BlockPos var2);

    public double getContainerInteractionRange();

    default public LivingEntity asLivingEntity() {
        if (this instanceof LivingEntity) {
            return (LivingEntity)((Object)this);
        }
        throw new IllegalStateException("A container user must be a LivingEntity");
    }
}

