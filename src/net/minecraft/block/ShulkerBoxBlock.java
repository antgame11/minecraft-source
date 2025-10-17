/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ShulkerBoxBlock
extends BlockWithEntity {
    public static final MapCodec<ShulkerBoxBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(DyeColor.CODEC.optionalFieldOf("color").forGetter(block -> Optional.ofNullable(block.color)), ShulkerBoxBlock.createSettingsCodec()).apply((Applicative<ShulkerBoxBlock, ?>)instance, (color, settings) -> new ShulkerBoxBlock(color.orElse(null), (AbstractBlock.Settings)settings)));
    public static final Map<Direction, VoxelShape> SHAPES_BY_DIRECTION = VoxelShapes.createFacingShapeMap(Block.createCuboidZShape(16.0, 0.0, 1.0));
    public static final EnumProperty<Direction> FACING = FacingBlock.FACING;
    public static final Identifier CONTENTS_DYNAMIC_DROP_ID = Identifier.ofVanilla("contents");
    @Nullable
    private final DyeColor color;

    public MapCodec<ShulkerBoxBlock> getCodec() {
        return CODEC;
    }

    public ShulkerBoxBlock(@Nullable DyeColor color, AbstractBlock.Settings settings) {
        super(settings);
        this.color = color;
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.UP));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ShulkerBoxBlockEntity(this.color, pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return ShulkerBoxBlock.validateTicker(type, BlockEntityType.SHULKER_BOX, ShulkerBoxBlockEntity::tick);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world instanceof ServerWorld) {
            ShulkerBoxBlockEntity lv2;
            ServerWorld lv = (ServerWorld)world;
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ShulkerBoxBlockEntity && ShulkerBoxBlock.canOpen(state, world, pos, lv2 = (ShulkerBoxBlockEntity)blockEntity)) {
                player.openHandledScreen(lv2);
                player.incrementStat(Stats.OPEN_SHULKER_BOX);
                PiglinBrain.onGuardedBlockInteracted(lv, player, true);
            }
        }
        return ActionResult.SUCCESS;
    }

    private static boolean canOpen(BlockState state, World world, BlockPos pos, ShulkerBoxBlockEntity entity) {
        if (entity.getAnimationStage() != ShulkerBoxBlockEntity.AnimationStage.CLOSED) {
            return true;
        }
        Box lv = ShulkerEntity.calculateBoundingBox(1.0f, state.get(FACING), 0.0f, 0.5f, pos.toBottomCenterPos()).contract(1.0E-6);
        return world.isSpaceEmpty(lv);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(FACING, ctx.getSide());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof ShulkerBoxBlockEntity) {
            ShulkerBoxBlockEntity lv2 = (ShulkerBoxBlockEntity)lv;
            if (!world.isClient() && player.shouldSkipBlockDrops() && !lv2.isEmpty()) {
                ItemStack lv3 = ShulkerBoxBlock.getItemStack(this.getColor());
                lv3.applyComponentsFrom(lv.createComponentMap());
                ItemEntity lv4 = new ItemEntity(world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, lv3);
                lv4.setToDefaultPickupDelay();
                world.spawnEntity(lv4);
            } else {
                lv2.generateLoot(player);
            }
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected List<ItemStack> getDroppedStacks(BlockState state, LootWorldContext.Builder builder) {
        BlockEntity lv = builder.getOptional(LootContextParameters.BLOCK_ENTITY);
        if (lv instanceof ShulkerBoxBlockEntity) {
            ShulkerBoxBlockEntity lv2 = (ShulkerBoxBlockEntity)lv;
            builder = builder.addDynamicDrop(CONTENTS_DYNAMIC_DROP_ID, lootConsumer -> {
                for (int i = 0; i < lv2.size(); ++i) {
                    lootConsumer.accept(lv2.getStack(i));
                }
            });
        }
        return super.getDroppedStacks(state, builder);
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        ItemScatterer.onStateReplaced(state, world, pos);
    }

    @Override
    protected VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        ShulkerBoxBlockEntity lv2;
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof ShulkerBoxBlockEntity && !(lv2 = (ShulkerBoxBlockEntity)lv).suffocates()) {
            return SHAPES_BY_DIRECTION.get(state.get(FACING).getOpposite());
        }
        return VoxelShapes.fullCube();
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof ShulkerBoxBlockEntity) {
            ShulkerBoxBlockEntity lv2 = (ShulkerBoxBlockEntity)lv;
            return VoxelShapes.cuboid(lv2.getBoundingBox(state));
        }
        return VoxelShapes.fullCube();
    }

    @Override
    protected boolean isTransparent(BlockState state) {
        return false;
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    public static Block get(@Nullable DyeColor dyeColor) {
        if (dyeColor == null) {
            return Blocks.SHULKER_BOX;
        }
        return switch (dyeColor) {
            default -> throw new MatchException(null, null);
            case DyeColor.WHITE -> Blocks.WHITE_SHULKER_BOX;
            case DyeColor.ORANGE -> Blocks.ORANGE_SHULKER_BOX;
            case DyeColor.MAGENTA -> Blocks.MAGENTA_SHULKER_BOX;
            case DyeColor.LIGHT_BLUE -> Blocks.LIGHT_BLUE_SHULKER_BOX;
            case DyeColor.YELLOW -> Blocks.YELLOW_SHULKER_BOX;
            case DyeColor.LIME -> Blocks.LIME_SHULKER_BOX;
            case DyeColor.PINK -> Blocks.PINK_SHULKER_BOX;
            case DyeColor.GRAY -> Blocks.GRAY_SHULKER_BOX;
            case DyeColor.LIGHT_GRAY -> Blocks.LIGHT_GRAY_SHULKER_BOX;
            case DyeColor.CYAN -> Blocks.CYAN_SHULKER_BOX;
            case DyeColor.BLUE -> Blocks.BLUE_SHULKER_BOX;
            case DyeColor.BROWN -> Blocks.BROWN_SHULKER_BOX;
            case DyeColor.GREEN -> Blocks.GREEN_SHULKER_BOX;
            case DyeColor.RED -> Blocks.RED_SHULKER_BOX;
            case DyeColor.BLACK -> Blocks.BLACK_SHULKER_BOX;
            case DyeColor.PURPLE -> Blocks.PURPLE_SHULKER_BOX;
        };
    }

    @Nullable
    public DyeColor getColor() {
        return this.color;
    }

    public static ItemStack getItemStack(@Nullable DyeColor color) {
        return new ItemStack(ShulkerBoxBlock.get(color));
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }
}

