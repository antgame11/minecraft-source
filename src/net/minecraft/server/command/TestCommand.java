/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.TestInstanceBlockEntity;
import net.minecraft.command.ArgumentGetter;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.command.argument.RegistrySelectorArgumentType;
import net.minecraft.network.packet.s2c.play.GameTestHighlightPosS2CPacket;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TestFinder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BatchListener;
import net.minecraft.test.Batches;
import net.minecraft.test.GameTestBatch;
import net.minecraft.test.GameTestState;
import net.minecraft.test.RuntimeTestInstances;
import net.minecraft.test.TestAttemptConfig;
import net.minecraft.test.TestInstance;
import net.minecraft.test.TestInstanceBlockFinder;
import net.minecraft.test.TestInstanceFinder;
import net.minecraft.test.TestInstanceUtil;
import net.minecraft.test.TestListener;
import net.minecraft.test.TestManager;
import net.minecraft.test.TestRunContext;
import net.minecraft.test.TestSet;
import net.minecraft.test.TestStructurePlacer;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import org.apache.commons.lang3.mutable.MutableInt;

public class TestCommand {
    public static final int field_33180 = 15;
    public static final int field_33181 = 250;
    public static final int field_53735 = 10;
    public static final int field_53736 = 100;
    private static final int field_33178 = 250;
    private static final int field_33179 = 1024;
    private static final int field_33182 = 3;
    private static final int field_33184 = 5;
    private static final int field_33185 = 5;
    private static final int field_33186 = 5;
    private static final SimpleCommandExceptionType NO_TESTS_TO_CLEAR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.test.clear.error.no_tests"));
    private static final SimpleCommandExceptionType NO_TESTS_TO_RESET_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.test.reset.error.no_tests"));
    private static final SimpleCommandExceptionType TEST_INSTANCE_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.test.error.test_instance_not_found"));
    private static final SimpleCommandExceptionType EXPORT_STRUCTURES_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(Text.literal("Could not find any structures to export"));
    private static final SimpleCommandExceptionType NO_TEST_INSTANCES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.test.error.no_test_instances"));
    private static final Dynamic3CommandExceptionType NO_TEST_CONTAINING_POS_EXCEPTION = new Dynamic3CommandExceptionType((x, y, z) -> Text.stringifiedTranslatable("commands.test.error.no_test_containing_pos", x, y, z));
    private static final DynamicCommandExceptionType TOO_LARGE_EXCEPTION = new DynamicCommandExceptionType(maxSize -> Text.stringifiedTranslatable("commands.test.error.too_large", maxSize));

    private static int reset(TestFinder finder) throws CommandSyntaxException {
        TestCommand.stop();
        int i = TestCommand.stream(finder.getCommandSource(), TestAttemptConfig.once(), finder).map(state -> TestCommand.reset(finder.getCommandSource(), state)).toList().size();
        if (i == 0) {
            throw NO_TESTS_TO_CLEAR_EXCEPTION.create();
        }
        finder.getCommandSource().sendFeedback(() -> Text.translatable("commands.test.reset.success", i), true);
        return i;
    }

    private static int clear(TestFinder finder) throws CommandSyntaxException {
        TestCommand.stop();
        ServerCommandSource lv = finder.getCommandSource();
        ServerWorld lv2 = lv.getWorld();
        List list = finder.findTestPos().flatMap(pos -> lv2.getBlockEntity((BlockPos)pos, BlockEntityType.TEST_INSTANCE_BLOCK).stream()).toList();
        for (TestInstanceBlockEntity lv3 : list) {
            TestInstanceUtil.clearArea(lv3.getBlockBox(), lv2);
            lv3.clearBarriers();
            lv2.breakBlock(lv3.getPos(), false);
        }
        if (list.isEmpty()) {
            throw NO_TESTS_TO_CLEAR_EXCEPTION.create();
        }
        lv.sendFeedback(() -> Text.translatable("commands.test.clear.success", list.size()), true);
        return list.size();
    }

    private static int export(TestFinder finder) throws CommandSyntaxException {
        ServerCommandSource lv = finder.getCommandSource();
        ServerWorld lv2 = lv.getWorld();
        int i = 0;
        boolean bl = true;
        Iterator iterator = finder.findTestPos().iterator();
        while (iterator.hasNext()) {
            BlockPos lv3 = (BlockPos)iterator.next();
            BlockEntity blockEntity = lv2.getBlockEntity(lv3);
            if (blockEntity instanceof TestInstanceBlockEntity) {
                TestInstanceBlockEntity lv4 = (TestInstanceBlockEntity)blockEntity;
                if (!lv4.export(lv::sendMessage)) {
                    bl = false;
                }
                ++i;
                continue;
            }
            throw TEST_INSTANCE_NOT_FOUND_EXCEPTION.create();
        }
        if (i == 0) {
            throw EXPORT_STRUCTURES_NOT_FOUND_EXCEPTION.create();
        }
        String string = "Exported " + i + " structures";
        finder.getCommandSource().sendFeedback(() -> Text.literal(string), true);
        return bl ? 0 : 1;
    }

    private static int start(TestFinder finder) {
        TestCommand.stop();
        ServerCommandSource lv = finder.getCommandSource();
        ServerWorld lv2 = lv.getWorld();
        BlockPos lv3 = TestCommand.getStructurePos(lv);
        List<GameTestState> collection = Stream.concat(TestCommand.stream(lv, TestAttemptConfig.once(), finder), TestCommand.stream(lv, TestAttemptConfig.once(), finder, 0)).toList();
        RuntimeTestInstances.clear();
        ArrayList<GameTestBatch> collection2 = new ArrayList<GameTestBatch>();
        for (GameTestState lv4 : collection) {
            for (BlockRotation lv5 : BlockRotation.values()) {
                ArrayList<GameTestState> collection3 = new ArrayList<GameTestState>();
                for (int i = 0; i < 100; ++i) {
                    GameTestState lv6 = new GameTestState(lv4.getInstanceEntry(), lv5, lv2, new TestAttemptConfig(1, true));
                    lv6.setTestBlockPos(lv4.getPos());
                    collection3.add(lv6);
                }
                GameTestBatch lv7 = Batches.create(collection3, lv4.getInstance().getEnvironment(), lv5.ordinal());
                collection2.add(lv7);
            }
        }
        TestStructurePlacer lv8 = new TestStructurePlacer(lv3, 10, true);
        TestRunContext lv9 = TestRunContext.Builder.of(collection2, lv2).batcher(Batches.batcher(100)).initialSpawner(lv8).reuseSpawner(lv8).stopAfterFailure().clearBetweenBatches().build();
        return TestCommand.start(lv, lv9);
    }

    private static int start(TestFinder finder, TestAttemptConfig config, int rotationSteps, int testsPerRow) {
        TestCommand.stop();
        ServerCommandSource lv = finder.getCommandSource();
        ServerWorld lv2 = lv.getWorld();
        BlockPos lv3 = TestCommand.getStructurePos(lv);
        List<GameTestState> collection = Stream.concat(TestCommand.stream(lv, config, finder), TestCommand.stream(lv, config, finder, rotationSteps)).toList();
        if (collection.isEmpty()) {
            lv.sendFeedback(() -> Text.translatable("commands.test.no_tests"), false);
            return 0;
        }
        RuntimeTestInstances.clear();
        lv.sendFeedback(() -> Text.translatable("commands.test.run.running", collection.size()), false);
        TestRunContext lv4 = TestRunContext.Builder.ofStates(collection, lv2).initialSpawner(new TestStructurePlacer(lv3, testsPerRow, false)).build();
        return TestCommand.start(lv, lv4);
    }

    private static int locate(TestFinder finder) throws CommandSyntaxException {
        finder.getCommandSource().sendMessage(Text.translatable("commands.test.locate.started"));
        MutableInt mutableInt = new MutableInt(0);
        BlockPos lv = BlockPos.ofFloored(finder.getCommandSource().getPosition());
        finder.findTestPos().forEach(pos -> {
            BlockEntity lv = finder.getCommandSource().getWorld().getBlockEntity((BlockPos)pos);
            if (!(lv instanceof TestInstanceBlockEntity)) {
                return;
            }
            TestInstanceBlockEntity lv2 = (TestInstanceBlockEntity)lv;
            Direction lv3 = lv2.getRotation().rotate(Direction.NORTH);
            BlockPos lv4 = lv2.getPos().offset(lv3, 2);
            int i = (int)lv3.getOpposite().getPositiveHorizontalDegrees();
            String string = String.format(Locale.ROOT, "/tp @s %d %d %d %d 0", lv4.getX(), lv4.getY(), lv4.getZ(), i);
            int j = lv.getX() - pos.getX();
            int k = lv.getZ() - pos.getZ();
            int l = MathHelper.floor(MathHelper.sqrt(j * j + k * k));
            MutableText lv5 = Texts.bracketed(Text.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ())).styled(style -> style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent.SuggestCommand(string)).withHoverEvent(new HoverEvent.ShowText(Text.translatable("chat.coordinates.tooltip"))));
            finder.getCommandSource().sendFeedback(() -> Text.translatable("commands.test.locate.found", lv5, l), false);
            mutableInt.increment();
        });
        int i = mutableInt.intValue();
        if (i == 0) {
            throw NO_TEST_INSTANCES_EXCEPTION.create();
        }
        finder.getCommandSource().sendFeedback(() -> Text.translatable("commands.test.locate.done", i), true);
        return i;
    }

    private static ArgumentBuilder<ServerCommandSource, ?> testAttemptConfig(ArgumentBuilder<ServerCommandSource, ?> builder, ArgumentGetter<CommandContext<ServerCommandSource>, TestFinder> finderGetter, Function<ArgumentBuilder<ServerCommandSource, ?>, ArgumentBuilder<ServerCommandSource, ?>> extraConfigAdder) {
        return ((ArgumentBuilder)builder.executes(context -> TestCommand.start((TestFinder)finderGetter.apply(context), TestAttemptConfig.once(), 0, 8))).then(((RequiredArgumentBuilder)CommandManager.argument("numberOfTimes", IntegerArgumentType.integer(0)).executes(context -> TestCommand.start((TestFinder)finderGetter.apply(context), new TestAttemptConfig(IntegerArgumentType.getInteger(context, "numberOfTimes"), false), 0, 8))).then(extraConfigAdder.apply((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("untilFailed", BoolArgumentType.bool()).executes(context -> TestCommand.start((TestFinder)finderGetter.apply(context), new TestAttemptConfig(IntegerArgumentType.getInteger(context, "numberOfTimes"), BoolArgumentType.getBool(context, "untilFailed")), 0, 8)))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> testAttemptConfig(ArgumentBuilder<ServerCommandSource, ?> builder, ArgumentGetter<CommandContext<ServerCommandSource>, TestFinder> finderGetter) {
        return TestCommand.testAttemptConfig(builder, finderGetter, extraConfigAdder -> extraConfigAdder);
    }

    private static ArgumentBuilder<ServerCommandSource, ?> testAttemptAndPlacementConfig(ArgumentBuilder<ServerCommandSource, ?> builder, ArgumentGetter<CommandContext<ServerCommandSource>, TestFinder> finderGetter) {
        return TestCommand.testAttemptConfig(builder, finderGetter, builder2 -> builder2.then(((RequiredArgumentBuilder)CommandManager.argument("rotationSteps", IntegerArgumentType.integer()).executes(context -> TestCommand.start((TestFinder)finderGetter.apply(context), new TestAttemptConfig(IntegerArgumentType.getInteger(context, "numberOfTimes"), BoolArgumentType.getBool(context, "untilFailed")), IntegerArgumentType.getInteger(context, "rotationSteps"), 8))).then(CommandManager.argument("testsPerRow", IntegerArgumentType.integer()).executes(context -> TestCommand.start((TestFinder)finderGetter.apply(context), new TestAttemptConfig(IntegerArgumentType.getInteger(context, "numberOfTimes"), BoolArgumentType.getBool(context, "untilFailed")), IntegerArgumentType.getInteger(context, "rotationSteps"), IntegerArgumentType.getInteger(context, "testsPerRow"))))));
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        ArgumentBuilder<ServerCommandSource, ?> argumentBuilder = TestCommand.testAttemptAndPlacementConfig(CommandManager.argument("onlyRequiredTests", BoolArgumentType.bool()), context -> TestFinder.builder().failed((CommandContext<ServerCommandSource>)context, BoolArgumentType.getBool(context, "onlyRequiredTests")));
        LiteralArgumentBuilder literalArgumentBuilder = (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("test").requires(CommandManager.requirePermissionLevel(2))).then(CommandManager.literal("run").then(TestCommand.testAttemptAndPlacementConfig(CommandManager.argument("tests", RegistrySelectorArgumentType.selector(registryAccess, RegistryKeys.TEST_INSTANCE)), context -> TestFinder.builder().selector((CommandContext<ServerCommandSource>)context, RegistrySelectorArgumentType.getEntries(context, "tests")))))).then(CommandManager.literal("runmultiple").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("tests", RegistrySelectorArgumentType.selector(registryAccess, RegistryKeys.TEST_INSTANCE)).executes(context -> TestCommand.start(TestFinder.builder().selector(context, RegistrySelectorArgumentType.getEntries(context, "tests")), TestAttemptConfig.once(), 0, 8))).then(CommandManager.argument("amount", IntegerArgumentType.integer()).executes(context -> TestCommand.start(TestFinder.builder().repeat(IntegerArgumentType.getInteger(context, "amount")).selector(context, RegistrySelectorArgumentType.getEntries(context, "tests")), TestAttemptConfig.once(), 0, 8)))))).then(TestCommand.testAttemptConfig(CommandManager.literal("runthese"), TestFinder.builder()::allStructures))).then(TestCommand.testAttemptConfig(CommandManager.literal("runclosest"), TestFinder.builder()::nearest))).then(TestCommand.testAttemptConfig(CommandManager.literal("runthat"), TestFinder.builder()::targeted))).then(TestCommand.testAttemptAndPlacementConfig(CommandManager.literal("runfailed").then(argumentBuilder), TestFinder.builder()::failed))).then(CommandManager.literal("verify").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("tests", RegistrySelectorArgumentType.selector(registryAccess, RegistryKeys.TEST_INSTANCE)).executes(context -> TestCommand.start(TestFinder.builder().selector(context, RegistrySelectorArgumentType.getEntries(context, "tests"))))))).then(CommandManager.literal("locate").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("tests", RegistrySelectorArgumentType.selector(registryAccess, RegistryKeys.TEST_INSTANCE)).executes(context -> TestCommand.locate(TestFinder.builder().selector(context, RegistrySelectorArgumentType.getEntries(context, "tests"))))))).then(CommandManager.literal("resetclosest").executes(context -> TestCommand.reset(TestFinder.builder().nearest(context))))).then(CommandManager.literal("resetthese").executes(context -> TestCommand.reset(TestFinder.builder().allStructures(context))))).then(CommandManager.literal("resetthat").executes(context -> TestCommand.reset(TestFinder.builder().targeted(context))))).then(CommandManager.literal("clearthat").executes(context -> TestCommand.clear(TestFinder.builder().targeted(context))))).then(CommandManager.literal("clearthese").executes(context -> TestCommand.clear(TestFinder.builder().allStructures(context))))).then(((LiteralArgumentBuilder)CommandManager.literal("clearall").executes(context -> TestCommand.clear(TestFinder.builder().surface(context, 250)))).then(CommandManager.argument("radius", IntegerArgumentType.integer()).executes(context -> TestCommand.clear(TestFinder.builder().surface(context, MathHelper.clamp(IntegerArgumentType.getInteger(context, "radius"), 0, 1024))))))).then(CommandManager.literal("stop").executes(context -> TestCommand.stop()))).then(((LiteralArgumentBuilder)CommandManager.literal("pos").executes(context -> TestCommand.executePos((ServerCommandSource)context.getSource(), "pos"))).then(CommandManager.argument("var", StringArgumentType.word()).executes(context -> TestCommand.executePos((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "var")))))).then(CommandManager.literal("create").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("id", IdentifierArgumentType.identifier()).suggests(TestCommand::suggestTestFunctions).executes(context -> TestCommand.executeCreate((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier(context, "id"), 5, 5, 5))).then(((RequiredArgumentBuilder)CommandManager.argument("width", IntegerArgumentType.integer()).executes(context -> TestCommand.executeCreate((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier(context, "id"), IntegerArgumentType.getInteger(context, "width"), IntegerArgumentType.getInteger(context, "width"), IntegerArgumentType.getInteger(context, "width")))).then(CommandManager.argument("height", IntegerArgumentType.integer()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("depth", IntegerArgumentType.integer()).executes(context -> TestCommand.executeCreate((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier(context, "id"), IntegerArgumentType.getInteger(context, "width"), IntegerArgumentType.getInteger(context, "height"), IntegerArgumentType.getInteger(context, "depth"))))))));
        if (SharedConstants.isDevelopment) {
            literalArgumentBuilder = (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder.then(CommandManager.literal("export").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("test", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.TEST_INSTANCE)).executes(context -> TestCommand.executeExport((ServerCommandSource)context.getSource(), RegistryEntryReferenceArgumentType.getRegistryEntry(context, "test", RegistryKeys.TEST_INSTANCE)))))).then(CommandManager.literal("exportclosest").executes(context -> TestCommand.export(TestFinder.builder().nearest(context))))).then(CommandManager.literal("exportthese").executes(context -> TestCommand.export(TestFinder.builder().allStructures(context))))).then(CommandManager.literal("exportthat").executes(context -> TestCommand.export(TestFinder.builder().targeted(context))));
        }
        dispatcher.register(literalArgumentBuilder);
    }

    public static CompletableFuture<Suggestions> suggestTestFunctions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        Stream<String> stream = context.getSource().getRegistryManager().getOrThrow(RegistryKeys.TEST_FUNCTION).streamEntries().map(RegistryEntry::getIdAsString);
        return CommandSource.suggestMatching(stream, builder);
    }

    private static int reset(ServerCommandSource source, GameTestState state) {
        TestInstanceBlockEntity lv = state.getTestInstanceBlockEntity();
        lv.reset(source::sendMessage);
        return 1;
    }

    private static Stream<GameTestState> stream(ServerCommandSource source, TestAttemptConfig config, TestInstanceBlockFinder finder) {
        return finder.findTestPos().map(pos -> TestCommand.find(pos, source, config)).flatMap(Optional::stream);
    }

    private static Stream<GameTestState> stream(ServerCommandSource source, TestAttemptConfig config, TestInstanceFinder finder, int rotationSteps) {
        return finder.findTests().filter(instance -> TestCommand.checkStructure(source, ((TestInstance)instance.value()).getStructure())).map(instance -> new GameTestState((RegistryEntry.Reference<TestInstance>)instance, TestInstanceUtil.getRotation(rotationSteps), source.getWorld(), config));
    }

    private static Optional<GameTestState> find(BlockPos pos, ServerCommandSource source, TestAttemptConfig config) {
        ServerWorld lv = source.getWorld();
        BlockEntity blockEntity = lv.getBlockEntity(pos);
        if (!(blockEntity instanceof TestInstanceBlockEntity)) {
            source.sendError(Text.translatable("commands.test.error.test_instance_not_found.position", pos.getX(), pos.getY(), pos.getZ()));
            return Optional.empty();
        }
        TestInstanceBlockEntity lv2 = (TestInstanceBlockEntity)blockEntity;
        Optional optional = lv2.getTestKey().flatMap(((Registry)source.getRegistryManager().getOrThrow(RegistryKeys.TEST_INSTANCE))::getOptional);
        if (optional.isEmpty()) {
            source.sendError(Text.translatable("commands.test.error.non_existant_test", lv2.getTestName()));
            return Optional.empty();
        }
        RegistryEntry.Reference lv3 = (RegistryEntry.Reference)optional.get();
        GameTestState lv4 = new GameTestState(lv3, lv2.getRotation(), lv, config);
        lv4.setTestBlockPos(pos);
        if (!TestCommand.checkStructure(source, lv4.getStructure())) {
            return Optional.empty();
        }
        return Optional.of(lv4);
    }

    private static int executeCreate(ServerCommandSource source, Identifier id, int x, int y, int z) throws CommandSyntaxException {
        if (x > 48 || y > 48 || z > 48) {
            throw TOO_LARGE_EXCEPTION.create(48);
        }
        ServerWorld lv = source.getWorld();
        BlockPos lv2 = TestCommand.getStructurePos(source);
        TestInstanceBlockEntity lv3 = TestInstanceUtil.createTestInstanceBlockEntity(id, lv2, new Vec3i(x, y, z), BlockRotation.NONE, lv);
        BlockPos lv4 = lv3.getStructurePos();
        BlockPos lv5 = lv4.add(x - 1, 0, z - 1);
        BlockPos.stream(lv4, lv5).forEach(pos -> lv.setBlockState((BlockPos)pos, Blocks.BEDROCK.getDefaultState()));
        source.sendFeedback(() -> Text.translatable("commands.test.create.success", lv3.getTestName()), true);
        return 1;
    }

    private static int executePos(ServerCommandSource source, String variableName) throws CommandSyntaxException {
        ServerWorld lv4;
        ServerPlayerEntity lv = source.getPlayerOrThrow();
        BlockHitResult lv2 = (BlockHitResult)lv.raycast(10.0, 1.0f, false);
        BlockPos lv3 = lv2.getBlockPos();
        Optional<BlockPos> optional = TestInstanceUtil.findContainingTestInstanceBlock(lv3, 15, lv4 = source.getWorld());
        if (optional.isEmpty()) {
            optional = TestInstanceUtil.findContainingTestInstanceBlock(lv3, 250, lv4);
        }
        if (optional.isEmpty()) {
            throw NO_TEST_CONTAINING_POS_EXCEPTION.create(lv3.getX(), lv3.getY(), lv3.getZ());
        }
        BlockEntity blockEntity = lv4.getBlockEntity(optional.get());
        if (!(blockEntity instanceof TestInstanceBlockEntity)) {
            throw TEST_INSTANCE_NOT_FOUND_EXCEPTION.create();
        }
        TestInstanceBlockEntity lv5 = (TestInstanceBlockEntity)blockEntity;
        BlockPos lv6 = lv5.getStructurePos();
        BlockPos lv7 = lv3.subtract(lv6);
        String string2 = lv7.getX() + ", " + lv7.getY() + ", " + lv7.getZ();
        String string3 = lv5.getTestName().getString();
        MutableText lv8 = Text.translatable("commands.test.coordinates", lv7.getX(), lv7.getY(), lv7.getZ()).setStyle(Style.EMPTY.withBold(true).withColor(Formatting.GREEN).withHoverEvent(new HoverEvent.ShowText(Text.translatable("commands.test.coordinates.copy"))).withClickEvent(new ClickEvent.CopyToClipboard("final BlockPos " + variableName + " = new BlockPos(" + string2 + ");")));
        source.sendFeedback(() -> Text.translatable("commands.test.relative_position", string3, lv8), false);
        lv.networkHandler.sendPacket(new GameTestHighlightPosS2CPacket(lv3, lv7));
        return 1;
    }

    private static int stop() {
        TestManager.INSTANCE.clear();
        return 1;
    }

    public static int start(ServerCommandSource source, TestRunContext context) {
        context.addBatchListener(new ReportingBatchListener(source));
        TestSet lv = new TestSet(context.getStates());
        lv.addListener(new Listener(source, lv));
        lv.addListener(state -> RuntimeTestInstances.add(state.getInstanceEntry()));
        context.start();
        return 1;
    }

    private static int executeExport(ServerCommandSource source, RegistryEntry<TestInstance> instance) {
        if (!TestInstanceBlockEntity.exportData(source.getWorld(), instance.value().getStructure(), source::sendMessage)) {
            return 0;
        }
        return 1;
    }

    private static boolean checkStructure(ServerCommandSource source, Identifier templateId) {
        if (source.getWorld().getStructureTemplateManager().getTemplate(templateId).isEmpty()) {
            source.sendError(Text.translatable("commands.test.error.structure_not_found", Text.of(templateId)));
            return false;
        }
        return true;
    }

    private static BlockPos getStructurePos(ServerCommandSource source) {
        BlockPos lv = BlockPos.ofFloored(source.getPosition());
        int i = source.getWorld().getTopPosition(Heightmap.Type.WORLD_SURFACE, lv).getY();
        return new BlockPos(lv.getX(), i, lv.getZ() + 3);
    }

    record ReportingBatchListener(ServerCommandSource source) implements BatchListener
    {
        @Override
        public void onStarted(GameTestBatch batch) {
            this.source.sendFeedback(() -> Text.translatable("commands.test.batch.starting", batch.environment().getIdAsString(), batch.index()), true);
        }

        @Override
        public void onFinished(GameTestBatch batch) {
        }
    }

    public record Listener(ServerCommandSource source, TestSet tests) implements TestListener
    {
        @Override
        public void onStarted(GameTestState test) {
        }

        @Override
        public void onPassed(GameTestState test, TestRunContext context) {
            this.onFinished();
        }

        @Override
        public void onFailed(GameTestState test, TestRunContext context) {
            this.onFinished();
        }

        @Override
        public void onRetry(GameTestState lastState, GameTestState nextState, TestRunContext context) {
            this.tests.add(nextState);
        }

        private void onFinished() {
            if (this.tests.isDone()) {
                this.source.sendFeedback(() -> Text.translatable("commands.test.summary", this.tests.getTestCount()).formatted(Formatting.WHITE), true);
                if (this.tests.failed()) {
                    this.source.sendError(Text.translatable("commands.test.summary.failed", this.tests.getFailedRequiredTestCount()));
                } else {
                    this.source.sendFeedback(() -> Text.translatable("commands.test.summary.all_required_passed").formatted(Formatting.GREEN), true);
                }
                if (this.tests.hasFailedOptionalTests()) {
                    this.source.sendMessage(Text.translatable("commands.test.summary.optional_failed", this.tests.getFailedOptionalTestCount()));
                }
            }
        }
    }
}

