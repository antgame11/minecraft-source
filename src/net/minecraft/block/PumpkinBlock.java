/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class PumpkinBlock
extends Block {
    public static final MapCodec<PumpkinBlock> CODEC = PumpkinBlock.createCodec(PumpkinBlock::new);

    public MapCodec<PumpkinBlock> getCodec() {
        return CODEC;
    }

    protected PumpkinBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack2, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!stack2.isOf(Items.SHEARS)) {
            return super.onUseWithItem(stack2, state, world, pos, player, hand, hit);
        }
        if (!(world instanceof ServerWorld)) {
            return ActionResult.SUCCESS;
        }
        ServerWorld lv = (ServerWorld)world;
        Direction lv2 = hit.getSide();
        Direction lv3 = lv2.getAxis() == Direction.Axis.Y ? player.getHorizontalFacing().getOpposite() : lv2;
        PumpkinBlock.generateBlockInteractLoot(lv, LootTables.PUMPKIN_CARVE, state, world.getBlockEntity(pos), stack2, player, (worldx, stack) -> {
            ItemEntity lv = new ItemEntity(world, (double)pos.getX() + 0.5 + (double)lv3.getOffsetX() * 0.65, (double)pos.getY() + 0.1, (double)pos.getZ() + 0.5 + (double)lv3.getOffsetZ() * 0.65, (ItemStack)stack);
            lv.setVelocity(0.05 * (double)lv3.getOffsetX() + arg.random.nextDouble() * 0.02, 0.05, 0.05 * (double)lv3.getOffsetZ() + arg.random.nextDouble() * 0.02);
            world.spawnEntity(lv);
        });
        world.playSound(null, pos, SoundEvents.BLOCK_PUMPKIN_CARVE, SoundCategory.BLOCKS, 1.0f, 1.0f);
        world.setBlockState(pos, (BlockState)Blocks.CARVED_PUMPKIN.getDefaultState().with(CarvedPumpkinBlock.FACING, lv3), Block.NOTIFY_ALL_AND_REDRAW);
        stack2.damage(1, (LivingEntity)player, hand.getEquipmentSlot());
        world.emitGameEvent((Entity)player, GameEvent.SHEAR, pos);
        player.incrementStat(Stats.USED.getOrCreateStat(Items.SHEARS));
        return ActionResult.SUCCESS;
    }
}

