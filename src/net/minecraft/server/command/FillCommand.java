/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.ArgumentGetter;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;

public class FillCommand {
    private static final Dynamic2CommandExceptionType TOO_BIG_EXCEPTION = new Dynamic2CommandExceptionType((maxCount, count) -> Text.stringifiedTranslatable("commands.fill.toobig", maxCount, count));
    static final BlockStateArgument AIR_BLOCK_ARGUMENT = new BlockStateArgument(Blocks.AIR.getDefaultState(), Collections.emptySet(), null);
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.fill.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("fill").requires(CommandManager.requirePermissionLevel(2))).then(CommandManager.argument("from", BlockPosArgumentType.blockPos()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("to", BlockPosArgumentType.blockPos()).then((ArgumentBuilder<ServerCommandSource, ?>)((ArgumentBuilder)FillCommand.buildModeTree(commandRegistryAccess, CommandManager.argument("block", BlockStateArgumentType.blockState(commandRegistryAccess)), context -> BlockPosArgumentType.getLoadedBlockPos(context, "from"), context -> BlockPosArgumentType.getLoadedBlockPos(context, "to"), context -> BlockStateArgumentType.getBlockState(context, "block"), context -> null).then((ArgumentBuilder<ServerCommandSource, ?>)((LiteralArgumentBuilder)CommandManager.literal("replace").executes(context -> FillCommand.execute((ServerCommandSource)context.getSource(), BlockBox.create(BlockPosArgumentType.getLoadedBlockPos(context, "from"), BlockPosArgumentType.getLoadedBlockPos(context, "to")), BlockStateArgumentType.getBlockState(context, "block"), Mode.REPLACE, null, false))).then(FillCommand.buildModeTree(commandRegistryAccess, CommandManager.argument("filter", BlockPredicateArgumentType.blockPredicate(commandRegistryAccess)), context -> BlockPosArgumentType.getLoadedBlockPos(context, "from"), context -> BlockPosArgumentType.getLoadedBlockPos(context, "to"), context -> BlockStateArgumentType.getBlockState(context, "block"), context -> BlockPredicateArgumentType.getBlockPredicate(context, "filter"))))).then(CommandManager.literal("keep").executes(context -> FillCommand.execute((ServerCommandSource)context.getSource(), BlockBox.create(BlockPosArgumentType.getLoadedBlockPos(context, "from"), BlockPosArgumentType.getLoadedBlockPos(context, "to")), BlockStateArgumentType.getBlockState(context, "block"), Mode.REPLACE, pos -> pos.getWorld().isAir(pos.getBlockPos()), false)))))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> buildModeTree(CommandRegistryAccess registries, ArgumentBuilder<ServerCommandSource, ?> argumentBuilder, ArgumentGetter<CommandContext<ServerCommandSource>, BlockPos> from, ArgumentGetter<CommandContext<ServerCommandSource>, BlockPos> to, ArgumentGetter<CommandContext<ServerCommandSource>, BlockStateArgument> state, OptionalArgumentResolver<CommandContext<ServerCommandSource>, Predicate<CachedBlockPosition>> filter) {
        return ((ArgumentBuilder)((ArgumentBuilder)((ArgumentBuilder)((ArgumentBuilder)argumentBuilder.executes(context -> FillCommand.execute((ServerCommandSource)context.getSource(), BlockBox.create((Vec3i)from.apply(context), (Vec3i)to.apply(context)), (BlockStateArgument)state.apply(context), Mode.REPLACE, (Predicate)filter.apply(context), false))).then(CommandManager.literal("outline").executes(context -> FillCommand.execute((ServerCommandSource)context.getSource(), BlockBox.create((Vec3i)from.apply(context), (Vec3i)to.apply(context)), (BlockStateArgument)state.apply(context), Mode.OUTLINE, (Predicate)filter.apply(context), false)))).then(CommandManager.literal("hollow").executes(context -> FillCommand.execute((ServerCommandSource)context.getSource(), BlockBox.create((Vec3i)from.apply(context), (Vec3i)to.apply(context)), (BlockStateArgument)state.apply(context), Mode.HOLLOW, (Predicate)filter.apply(context), false)))).then(CommandManager.literal("destroy").executes(context -> FillCommand.execute((ServerCommandSource)context.getSource(), BlockBox.create((Vec3i)from.apply(context), (Vec3i)to.apply(context)), (BlockStateArgument)state.apply(context), Mode.DESTROY, (Predicate)filter.apply(context), false)))).then(CommandManager.literal("strict").executes(context -> FillCommand.execute((ServerCommandSource)context.getSource(), BlockBox.create((Vec3i)from.apply(context), (Vec3i)to.apply(context)), (BlockStateArgument)state.apply(context), Mode.REPLACE, (Predicate)filter.apply(context), true)));
    }

    private static int execute(ServerCommandSource source, BlockBox range, BlockStateArgument block, Mode mode, @Nullable Predicate<CachedBlockPosition> filter, boolean strict) throws CommandSyntaxException {
        int j;
        int i = range.getBlockCountX() * range.getBlockCountY() * range.getBlockCountZ();
        if (i > (j = source.getWorld().getGameRules().getInt(GameRules.COMMAND_MODIFICATION_BLOCK_LIMIT))) {
            throw TOO_BIG_EXCEPTION.create(j, i);
        }
        record Replaced(BlockPos pos, BlockState oldState) {
        }
        ArrayList<Replaced> list = Lists.newArrayList();
        ServerWorld lv = source.getWorld();
        if (lv.isDebugWorld()) {
            throw FAILED_EXCEPTION.create();
        }
        int k = 0;
        for (BlockPos lv2 : BlockPos.iterate(range.getMinX(), range.getMinY(), range.getMinZ(), range.getMaxX(), range.getMaxY(), range.getMaxZ())) {
            BlockStateArgument lv4;
            if (filter != null && !filter.test(new CachedBlockPosition(lv, lv2, true))) continue;
            BlockState lv3 = lv.getBlockState(lv2);
            boolean bl2 = false;
            if (mode.postProcessor.affect(lv, lv2)) {
                bl2 = true;
            }
            if ((lv4 = mode.filter.filter(range, lv2, block, lv)) == null) {
                if (!bl2) continue;
                ++k;
                continue;
            }
            if (!lv4.setBlockState(lv, lv2, Block.NOTIFY_LISTENERS | (strict ? Block.FORCE_STATE_AND_SKIP_CALLBACKS_AND_DROPS : Block.SKIP_BLOCK_ENTITY_REPLACED_CALLBACK))) {
                if (!bl2) continue;
                ++k;
                continue;
            }
            if (!strict) {
                list.add(new Replaced(lv2.toImmutable(), lv3));
            }
            ++k;
        }
        for (Replaced lv5 : list) {
            lv.onStateReplacedWithCommands(lv5.pos, lv5.oldState);
        }
        if (k == 0) {
            throw FAILED_EXCEPTION.create();
        }
        int l = k;
        source.sendFeedback(() -> Text.translatable("commands.fill.success", l), true);
        return k;
    }

    @FunctionalInterface
    static interface OptionalArgumentResolver<T, R> {
        @Nullable
        public R apply(T var1) throws CommandSyntaxException;
    }

    static enum Mode {
        REPLACE(PostProcessor.EMPTY, Filter.IDENTITY),
        OUTLINE(PostProcessor.EMPTY, (range, pos, block, world) -> {
            if (pos.getX() == range.getMinX() || pos.getX() == range.getMaxX() || pos.getY() == range.getMinY() || pos.getY() == range.getMaxY() || pos.getZ() == range.getMinZ() || pos.getZ() == range.getMaxZ()) {
                return block;
            }
            return null;
        }),
        HOLLOW(PostProcessor.EMPTY, (range, pos, block, world) -> {
            if (pos.getX() == range.getMinX() || pos.getX() == range.getMaxX() || pos.getY() == range.getMinY() || pos.getY() == range.getMaxY() || pos.getZ() == range.getMinZ() || pos.getZ() == range.getMaxZ()) {
                return block;
            }
            return AIR_BLOCK_ARGUMENT;
        }),
        DESTROY((world, pos) -> world.breakBlock(pos, true), Filter.IDENTITY);

        public final Filter filter;
        public final PostProcessor postProcessor;

        private Mode(PostProcessor postProcessor, Filter filter) {
            this.postProcessor = postProcessor;
            this.filter = filter;
        }
    }

    @FunctionalInterface
    public static interface PostProcessor {
        public static final PostProcessor EMPTY = (world, pos) -> false;

        public boolean affect(ServerWorld var1, BlockPos var2);
    }

    @FunctionalInterface
    public static interface Filter {
        public static final Filter IDENTITY = (box, pos, block, world) -> block;

        @Nullable
        public BlockStateArgument filter(BlockBox var1, BlockPos var2, BlockStateArgument var3, ServerWorld var4);
    }
}

