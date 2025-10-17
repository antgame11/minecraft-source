/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import java.util.function.ToIntFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.loot.LootTables;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public interface CaveVines {
    public static final VoxelShape SHAPE = Block.createColumnShape(14.0, 0.0, 16.0);
    public static final BooleanProperty BERRIES = Properties.BERRIES;

    public static ActionResult pickBerries(Entity picker, BlockState state, World world, BlockPos pos) {
        if (state.get(BERRIES).booleanValue()) {
            if (world instanceof ServerWorld) {
                ServerWorld lv = (ServerWorld)world;
                Block.generateBlockInteractLoot(lv, LootTables.CAVE_VINE_HARVEST, state, world.getBlockEntity(pos), null, picker, (worldx, stack) -> Block.dropStack((World)worldx, pos, stack));
                float f = MathHelper.nextBetween(lv.random, 0.8f, 1.2f);
                lv.playSound(null, pos, SoundEvents.BLOCK_CAVE_VINES_PICK_BERRIES, SoundCategory.BLOCKS, 1.0f, f);
                BlockState lv2 = (BlockState)state.with(BERRIES, false);
                lv.setBlockState(pos, lv2, Block.NOTIFY_LISTENERS);
                lv.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(picker, lv2));
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    public static boolean hasBerries(BlockState state) {
        return state.contains(BERRIES) && state.get(BERRIES) != false;
    }

    public static ToIntFunction<BlockState> getLuminanceSupplier(int luminance) {
        return state -> state.get(Properties.BERRIES) != false ? luminance : 0;
    }
}

