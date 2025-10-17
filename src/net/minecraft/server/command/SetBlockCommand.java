/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class SetBlockCommand {
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.setblock.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        Predicate<CachedBlockPosition> predicate = pos -> pos.getWorld().isAir(pos.getBlockPos());
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("setblock").requires(CommandManager.requirePermissionLevel(2))).then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("block", BlockStateArgumentType.blockState(commandRegistryAccess)).executes(context -> SetBlockCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), BlockStateArgumentType.getBlockState(context, "block"), Mode.REPLACE, null, false))).then(CommandManager.literal("destroy").executes(context -> SetBlockCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), BlockStateArgumentType.getBlockState(context, "block"), Mode.DESTROY, null, false)))).then(CommandManager.literal("keep").executes(context -> SetBlockCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), BlockStateArgumentType.getBlockState(context, "block"), Mode.REPLACE, predicate, false)))).then(CommandManager.literal("replace").executes(context -> SetBlockCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), BlockStateArgumentType.getBlockState(context, "block"), Mode.REPLACE, null, false)))).then(CommandManager.literal("strict").executes(context -> SetBlockCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), BlockStateArgumentType.getBlockState(context, "block"), Mode.REPLACE, null, true))))));
    }

    private static int execute(ServerCommandSource source, BlockPos pos, BlockStateArgument block, Mode mode, @Nullable Predicate<CachedBlockPosition> condition, boolean strict) throws CommandSyntaxException {
        boolean bl2;
        ServerWorld lv = source.getWorld();
        if (lv.isDebugWorld()) {
            throw FAILED_EXCEPTION.create();
        }
        if (condition != null && !condition.test(new CachedBlockPosition(lv, pos, true))) {
            throw FAILED_EXCEPTION.create();
        }
        if (mode == Mode.DESTROY) {
            lv.breakBlock(pos, true);
            bl2 = !block.getBlockState().isAir() || !lv.getBlockState(pos).isAir();
        } else {
            bl2 = true;
        }
        BlockState lv2 = lv.getBlockState(pos);
        if (bl2 && !block.setBlockState(lv, pos, Block.NOTIFY_LISTENERS | (strict ? Block.FORCE_STATE_AND_SKIP_CALLBACKS_AND_DROPS : Block.SKIP_BLOCK_ENTITY_REPLACED_CALLBACK))) {
            throw FAILED_EXCEPTION.create();
        }
        if (!strict) {
            lv.onStateReplacedWithCommands(pos, lv2);
        }
        source.sendFeedback(() -> Text.translatable("commands.setblock.success", pos.getX(), pos.getY(), pos.getZ()), true);
        return 1;
    }

    public static enum Mode {
        REPLACE,
        DESTROY;

    }
}

