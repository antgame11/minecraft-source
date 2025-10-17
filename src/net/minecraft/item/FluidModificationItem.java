/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface FluidModificationItem {
    default public void onEmptied(@Nullable LivingEntity user, World world, ItemStack stack, BlockPos pos) {
    }

    public boolean placeFluid(@Nullable LivingEntity var1, World var2, BlockPos var3, @Nullable BlockHitResult var4);
}

