/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.OperatorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.TestInstanceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TestInstanceBlock
extends BlockWithEntity
implements OperatorBlock {
    public static final MapCodec<TestInstanceBlock> CODEC = TestInstanceBlock.createCodec(TestInstanceBlock::new);

    public TestInstanceBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TestInstanceBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (!(lv instanceof TestInstanceBlockEntity)) {
            return ActionResult.PASS;
        }
        TestInstanceBlockEntity lv2 = (TestInstanceBlockEntity)lv;
        if (!player.isCreativeLevelTwoOp()) {
            return ActionResult.PASS;
        }
        if (player.getEntityWorld().isClient()) {
            player.openTestInstanceBlockScreen(lv2);
        }
        return ActionResult.SUCCESS;
    }

    protected MapCodec<TestInstanceBlock> getCodec() {
        return CODEC;
    }
}

