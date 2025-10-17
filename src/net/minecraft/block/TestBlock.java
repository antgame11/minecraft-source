/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.OperatorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.TestBlockEntity;
import net.minecraft.block.enums.TestBlockMode;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

public class TestBlock
extends BlockWithEntity
implements OperatorBlock {
    public static final MapCodec<TestBlock> CODEC = TestBlock.createCodec(TestBlock::new);
    public static final EnumProperty<TestBlockMode> MODE = Properties.TEST_BLOCK_MODE;

    public TestBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TestBlockEntity(pos, state);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        TestBlockMode lv3;
        BlockStateComponent lv = ctx.getStack().get(DataComponentTypes.BLOCK_STATE);
        BlockState lv2 = this.getDefaultState();
        if (lv != null && (lv3 = lv.getValue(MODE)) != null) {
            lv2 = (BlockState)lv2.with(MODE, lv3);
        }
        return lv2;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(MODE);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (!(lv instanceof TestBlockEntity)) {
            return ActionResult.PASS;
        }
        TestBlockEntity lv2 = (TestBlockEntity)lv;
        if (!player.isCreativeLevelTwoOp()) {
            return ActionResult.PASS;
        }
        if (world.isClient()) {
            player.openTestBlockScreen(lv2);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        TestBlockEntity lv = TestBlock.getBlockEntityOnServer(world, pos);
        if (lv == null) {
            return;
        }
        lv.reset();
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        TestBlockEntity lv = TestBlock.getBlockEntityOnServer(world, pos);
        if (lv == null) {
            return;
        }
        if (lv.getMode() == TestBlockMode.START) {
            return;
        }
        boolean bl2 = world.isReceivingRedstonePower(pos);
        boolean bl3 = lv.isPowered();
        if (bl2 && !bl3) {
            lv.setPowered(true);
            lv.trigger();
        } else if (!bl2 && bl3) {
            lv.setPowered(false);
        }
    }

    @Nullable
    private static TestBlockEntity getBlockEntityOnServer(World world, BlockPos pos) {
        ServerWorld lv;
        BlockEntity blockEntity;
        if (world instanceof ServerWorld && (blockEntity = (lv = (ServerWorld)world).getBlockEntity(pos)) instanceof TestBlockEntity) {
            TestBlockEntity lv2 = (TestBlockEntity)blockEntity;
            return lv2;
        }
        return null;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (state.get(MODE) != TestBlockMode.START) {
            return 0;
        }
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof TestBlockEntity) {
            TestBlockEntity lv2 = (TestBlockEntity)lv;
            return lv2.isPowered() ? 15 : 0;
        }
        return 0;
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack lv = super.getPickStack(world, pos, state, includeData);
        return TestBlock.applyBlockStateToStack(lv, state.get(MODE));
    }

    public static ItemStack applyBlockStateToStack(ItemStack stack, TestBlockMode mode) {
        stack.set(DataComponentTypes.BLOCK_STATE, stack.getOrDefault(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT).with(MODE, mode));
        return stack;
    }

    protected MapCodec<TestBlock> getCodec() {
        return CODEC;
    }
}

