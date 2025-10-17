/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.ArgumentGetter;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.component.ComponentMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.tick.WorldTickScheduler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class CloneCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final SimpleCommandExceptionType OVERLAP_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.clone.overlap"));
    private static final Dynamic2CommandExceptionType TOO_BIG_EXCEPTION = new Dynamic2CommandExceptionType((maxCount, count) -> Text.stringifiedTranslatable("commands.clone.toobig", maxCount, count));
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.clone.failed"));
    public static final Predicate<CachedBlockPosition> IS_AIR_PREDICATE = pos -> !pos.getBlockState().isAir();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("clone").requires(CommandManager.requirePermissionLevel(2))).then(CloneCommand.createSourceArgs(commandRegistryAccess, context -> ((ServerCommandSource)context.getSource()).getWorld()))).then(CommandManager.literal("from").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("sourceDimension", DimensionArgumentType.dimension()).then(CloneCommand.createSourceArgs(commandRegistryAccess, context -> DimensionArgumentType.getDimensionArgument(context, "sourceDimension"))))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> createSourceArgs(CommandRegistryAccess commandRegistryAccess, ArgumentGetter<CommandContext<ServerCommandSource>, ServerWorld> worldGetter) {
        return CommandManager.argument("begin", BlockPosArgumentType.blockPos()).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("end", BlockPosArgumentType.blockPos()).then(CloneCommand.createDestinationArgs(commandRegistryAccess, worldGetter, context -> ((ServerCommandSource)context.getSource()).getWorld()))).then(CommandManager.literal("to").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("targetDimension", DimensionArgumentType.dimension()).then(CloneCommand.createDestinationArgs(commandRegistryAccess, worldGetter, context -> DimensionArgumentType.getDimensionArgument(context, "targetDimension"))))));
    }

    private static DimensionalPos createDimensionalPos(CommandContext<ServerCommandSource> context, ServerWorld world, String name) throws CommandSyntaxException {
        BlockPos lv = BlockPosArgumentType.getLoadedBlockPos(context, world, name);
        return new DimensionalPos(world, lv);
    }

    private static ArgumentBuilder<ServerCommandSource, ?> createDestinationArgs(CommandRegistryAccess registries, ArgumentGetter<CommandContext<ServerCommandSource>, ServerWorld> currentWorldGetter, ArgumentGetter<CommandContext<ServerCommandSource>, ServerWorld> targetWorldGetter) {
        ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> lv = context -> CloneCommand.createDimensionalPos(context, (ServerWorld)currentWorldGetter.apply((CommandContext<ServerCommandSource>)context), "begin");
        ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> lv2 = context -> CloneCommand.createDimensionalPos(context, (ServerWorld)currentWorldGetter.apply((CommandContext<ServerCommandSource>)context), "end");
        ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> lv3 = context -> CloneCommand.createDimensionalPos(context, (ServerWorld)targetWorldGetter.apply((CommandContext<ServerCommandSource>)context), "destination");
        return CloneCommand.appendMode(registries, lv, lv2, lv3, false, CommandManager.argument("destination", BlockPosArgumentType.blockPos())).then(CloneCommand.appendMode(registries, lv, lv2, lv3, true, CommandManager.literal("strict")));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> appendMode(CommandRegistryAccess registries, ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> beginPosGetter, ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> endPosGetter, ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> destinationPosGetter, boolean strict, ArgumentBuilder<ServerCommandSource, ?> builder) {
        return ((ArgumentBuilder)((ArgumentBuilder)((ArgumentBuilder)builder.executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), (DimensionalPos)beginPosGetter.apply(context), (DimensionalPos)endPosGetter.apply(context), (DimensionalPos)destinationPosGetter.apply(context), pos -> true, Mode.NORMAL, strict))).then(CloneCommand.createModeArgs(beginPosGetter, endPosGetter, destinationPosGetter, context -> pos -> true, strict, CommandManager.literal("replace")))).then(CloneCommand.createModeArgs(beginPosGetter, endPosGetter, destinationPosGetter, context -> IS_AIR_PREDICATE, strict, CommandManager.literal("masked")))).then(CommandManager.literal("filtered").then(CloneCommand.createModeArgs(beginPosGetter, endPosGetter, destinationPosGetter, context -> BlockPredicateArgumentType.getBlockPredicate(context, "filter"), strict, CommandManager.argument("filter", BlockPredicateArgumentType.blockPredicate(registries)))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> createModeArgs(ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> beginPosGetter, ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> endPosGetter, ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> destinationPosGetter, ArgumentGetter<CommandContext<ServerCommandSource>, Predicate<CachedBlockPosition>> filterGetter, boolean strict, ArgumentBuilder<ServerCommandSource, ?> builder) {
        return ((ArgumentBuilder)((ArgumentBuilder)((ArgumentBuilder)builder.executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), (DimensionalPos)beginPosGetter.apply(context), (DimensionalPos)endPosGetter.apply(context), (DimensionalPos)destinationPosGetter.apply(context), (Predicate)filterGetter.apply(context), Mode.NORMAL, strict))).then(CommandManager.literal("force").executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), (DimensionalPos)beginPosGetter.apply(context), (DimensionalPos)endPosGetter.apply(context), (DimensionalPos)destinationPosGetter.apply(context), (Predicate)filterGetter.apply(context), Mode.FORCE, strict)))).then(CommandManager.literal("move").executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), (DimensionalPos)beginPosGetter.apply(context), (DimensionalPos)endPosGetter.apply(context), (DimensionalPos)destinationPosGetter.apply(context), (Predicate)filterGetter.apply(context), Mode.MOVE, strict)))).then(CommandManager.literal("normal").executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), (DimensionalPos)beginPosGetter.apply(context), (DimensionalPos)endPosGetter.apply(context), (DimensionalPos)destinationPosGetter.apply(context), (Predicate)filterGetter.apply(context), Mode.NORMAL, strict)));
    }

    private static int execute(ServerCommandSource source, DimensionalPos begin, DimensionalPos end, DimensionalPos destination, Predicate<CachedBlockPosition> filter, Mode mode, boolean strict) throws CommandSyntaxException {
        int j;
        BlockPos lv = begin.position();
        BlockPos lv2 = end.position();
        BlockBox lv3 = BlockBox.create(lv, lv2);
        BlockPos lv4 = destination.position();
        BlockPos lv5 = lv4.add(lv3.getDimensions());
        BlockBox lv6 = BlockBox.create(lv4, lv5);
        ServerWorld lv7 = begin.dimension();
        ServerWorld lv8 = destination.dimension();
        if (!mode.allowsOverlap() && lv7 == lv8 && lv6.intersects(lv3)) {
            throw OVERLAP_EXCEPTION.create();
        }
        int i = lv3.getBlockCountX() * lv3.getBlockCountY() * lv3.getBlockCountZ();
        if (i > (j = source.getWorld().getGameRules().getInt(GameRules.COMMAND_MODIFICATION_BLOCK_LIMIT))) {
            throw TOO_BIG_EXCEPTION.create(j, i);
        }
        if (!lv7.isRegionLoaded(lv, lv2) || !lv8.isRegionLoaded(lv4, lv5)) {
            throw BlockPosArgumentType.UNLOADED_EXCEPTION.create();
        }
        if (lv8.isDebugWorld()) {
            throw FAILED_EXCEPTION.create();
        }
        ArrayList<BlockInfo> list = Lists.newArrayList();
        ArrayList<BlockInfo> list2 = Lists.newArrayList();
        ArrayList<BlockInfo> list3 = Lists.newArrayList();
        LinkedList<BlockPos> deque = Lists.newLinkedList();
        int k = 0;
        try (ErrorReporter.Logging lv9 = new ErrorReporter.Logging(LOGGER);){
            int m;
            int l;
            BlockPos lv10 = new BlockPos(lv6.getMinX() - lv3.getMinX(), lv6.getMinY() - lv3.getMinY(), lv6.getMinZ() - lv3.getMinZ());
            for (l = lv3.getMinZ(); l <= lv3.getMaxZ(); ++l) {
                for (m = lv3.getMinY(); m <= lv3.getMaxY(); ++m) {
                    for (int n = lv3.getMinX(); n <= lv3.getMaxX(); ++n) {
                        BlockPos lv11 = new BlockPos(n, m, l);
                        BlockPos lv12 = lv11.add(lv10);
                        CachedBlockPosition lv13 = new CachedBlockPosition(lv7, lv11, false);
                        BlockState lv14 = lv13.getBlockState();
                        if (!filter.test(lv13)) continue;
                        BlockEntity lv15 = lv7.getBlockEntity(lv11);
                        if (lv15 != null) {
                            NbtWriteView lv16 = NbtWriteView.create(lv9.makeChild(lv15.getReporterContext()), source.getRegistryManager());
                            lv15.writeComponentlessData(lv16);
                            BlockEntityInfo lv17 = new BlockEntityInfo(lv16.getNbt(), lv15.getComponents());
                            list2.add(new BlockInfo(lv12, lv14, lv17, lv8.getBlockState(lv12)));
                            deque.addLast(lv11);
                            continue;
                        }
                        if (lv14.isOpaqueFullCube() || lv14.isFullCube(lv7, lv11)) {
                            list.add(new BlockInfo(lv12, lv14, null, lv8.getBlockState(lv12)));
                            deque.addLast(lv11);
                            continue;
                        }
                        list3.add(new BlockInfo(lv12, lv14, null, lv8.getBlockState(lv12)));
                        deque.addFirst(lv11);
                    }
                }
            }
            l = 2 | (strict ? 816 : 0);
            if (mode == Mode.MOVE) {
                for (BlockPos lv18 : deque) {
                    lv7.setBlockState(lv18, Blocks.BARRIER.getDefaultState(), l | Block.FORCE_STATE_AND_SKIP_CALLBACKS_AND_DROPS);
                }
                m = strict ? l : Block.NOTIFY_ALL;
                for (BlockPos lv11 : deque) {
                    lv7.setBlockState(lv11, Blocks.AIR.getDefaultState(), m);
                }
            }
            ArrayList<BlockInfo> list4 = Lists.newArrayList();
            list4.addAll(list);
            list4.addAll(list2);
            list4.addAll(list3);
            List<BlockInfo> list5 = Lists.reverse(list4);
            for (BlockInfo lv19 : list5) {
                lv8.setBlockState(lv19.pos, Blocks.BARRIER.getDefaultState(), l | Block.FORCE_STATE_AND_SKIP_CALLBACKS_AND_DROPS);
            }
            for (BlockInfo lv19 : list4) {
                if (!lv8.setBlockState(lv19.pos, lv19.state, l)) continue;
                ++k;
            }
            for (BlockInfo lv19 : list2) {
                BlockEntity lv20 = lv8.getBlockEntity(lv19.pos);
                if (lv19.blockEntityInfo != null && lv20 != null) {
                    lv20.readComponentlessData(NbtReadView.create(lv9.makeChild(lv20.getReporterContext()), lv8.getRegistryManager(), lv19.blockEntityInfo.nbt));
                    lv20.setComponents(lv19.blockEntityInfo.components);
                    lv20.markDirty();
                }
                lv8.setBlockState(lv19.pos, lv19.state, l);
            }
            if (!strict) {
                for (BlockInfo lv19 : list5) {
                    lv8.onStateReplacedWithCommands(lv19.pos, lv19.previousStateAtDestination);
                }
            }
            ((WorldTickScheduler)lv8.getBlockTickScheduler()).scheduleTicks(lv7.getBlockTickScheduler(), lv3, lv10);
        }
        if (k == 0) {
            throw FAILED_EXCEPTION.create();
        }
        int o = k;
        source.sendFeedback(() -> Text.translatable("commands.clone.success", o), true);
        return k;
    }

    record DimensionalPos(ServerWorld dimension, BlockPos position) {
    }

    static enum Mode {
        FORCE(true),
        MOVE(true),
        NORMAL(false);

        private final boolean allowsOverlap;

        private Mode(boolean allowsOverlap) {
            this.allowsOverlap = allowsOverlap;
        }

        public boolean allowsOverlap() {
            return this.allowsOverlap;
        }
    }

    record BlockEntityInfo(NbtCompound nbt, ComponentMap components) {
    }

    record BlockInfo(BlockPos pos, BlockState state, @Nullable BlockEntityInfo blockEntityInfo, BlockState previousStateAtDestination) {
        @Nullable
        public BlockEntityInfo blockEntityInfo() {
            return this.blockEntityInfo;
        }
    }
}

