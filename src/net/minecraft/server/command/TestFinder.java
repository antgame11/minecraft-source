/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.test.RuntimeTestInstances;
import net.minecraft.test.TestInstance;
import net.minecraft.test.TestInstanceBlockFinder;
import net.minecraft.test.TestInstanceFinder;
import net.minecraft.test.TestInstanceUtil;
import net.minecraft.util.math.BlockPos;

public class TestFinder
implements TestInstanceFinder,
TestInstanceBlockFinder {
    static final TestInstanceFinder NOOP_TEST_FUNCTION_FINDER = Stream::empty;
    static final TestInstanceBlockFinder NOOP_TEST_INSTANCE_BLOCK_FINDER = Stream::empty;
    private final TestInstanceFinder instanceFinder;
    private final TestInstanceBlockFinder blockFinder;
    private final ServerCommandSource commandSource;

    @Override
    public Stream<BlockPos> findTestPos() {
        return this.blockFinder.findTestPos();
    }

    public static Builder builder() {
        return new Builder();
    }

    TestFinder(ServerCommandSource commandSource, TestInstanceFinder instanceFinder, TestInstanceBlockFinder blockFinder) {
        this.commandSource = commandSource;
        this.instanceFinder = instanceFinder;
        this.blockFinder = blockFinder;
    }

    public ServerCommandSource getCommandSource() {
        return this.commandSource;
    }

    @Override
    public Stream<RegistryEntry.Reference<TestInstance>> findTests() {
        return this.instanceFinder.findTests();
    }

    public static class Builder {
        private final UnaryOperator<Supplier<Stream<RegistryEntry.Reference<TestInstance>>>> testInstanceFinderMapper;
        private final UnaryOperator<Supplier<Stream<BlockPos>>> testInstanceBlockFinderMapper;

        public Builder() {
            this.testInstanceFinderMapper = finder -> finder;
            this.testInstanceBlockFinderMapper = finder -> finder;
        }

        private Builder(UnaryOperator<Supplier<Stream<RegistryEntry.Reference<TestInstance>>>> testInstanceFinderMapper, UnaryOperator<Supplier<Stream<BlockPos>>> testInstanceBlockFinderMapper) {
            this.testInstanceFinderMapper = testInstanceFinderMapper;
            this.testInstanceBlockFinderMapper = testInstanceBlockFinderMapper;
        }

        public Builder repeat(int count) {
            return new Builder(Builder.repeating(count), Builder.repeating(count));
        }

        private static <Q> UnaryOperator<Supplier<Stream<Q>>> repeating(int count) {
            return supplier -> {
                LinkedList list = new LinkedList();
                List list2 = ((Stream)supplier.get()).toList();
                for (int j = 0; j < count; ++j) {
                    list.addAll(list2);
                }
                return list::stream;
            };
        }

        private TestFinder build(ServerCommandSource source, TestInstanceFinder instanceFinder, TestInstanceBlockFinder blockFinder) {
            return new TestFinder(source, ((Supplier)((Supplier)this.testInstanceFinderMapper.apply(instanceFinder::findTests)))::get, ((Supplier)((Supplier)this.testInstanceBlockFinderMapper.apply(blockFinder::findTestPos)))::get);
        }

        public TestFinder surface(CommandContext<ServerCommandSource> context, int radius) {
            ServerCommandSource lv = context.getSource();
            BlockPos lv2 = BlockPos.ofFloored(lv.getPosition());
            return this.build(lv, NOOP_TEST_FUNCTION_FINDER, () -> TestInstanceUtil.findTestInstanceBlocks(lv2, radius, lv.getWorld()));
        }

        public TestFinder nearest(CommandContext<ServerCommandSource> context) {
            ServerCommandSource lv = context.getSource();
            BlockPos lv2 = BlockPos.ofFloored(lv.getPosition());
            return this.build(lv, NOOP_TEST_FUNCTION_FINDER, () -> TestInstanceUtil.findNearestTestInstanceBlock(lv2, 15, lv.getWorld()).stream());
        }

        public TestFinder allStructures(CommandContext<ServerCommandSource> context) {
            ServerCommandSource lv = context.getSource();
            BlockPos lv2 = BlockPos.ofFloored(lv.getPosition());
            return this.build(lv, NOOP_TEST_FUNCTION_FINDER, () -> TestInstanceUtil.findTestInstanceBlocks(lv2, 250, lv.getWorld()));
        }

        public TestFinder targeted(CommandContext<ServerCommandSource> context) {
            ServerCommandSource lv = context.getSource();
            return this.build(lv, NOOP_TEST_FUNCTION_FINDER, () -> TestInstanceUtil.findTargetedTestInstanceBlock(BlockPos.ofFloored(lv.getPosition()), lv.getPlayer().getCameraEntity(), lv.getWorld()));
        }

        public TestFinder failed(CommandContext<ServerCommandSource> context, boolean onlyRequired) {
            return this.build(context.getSource(), () -> RuntimeTestInstances.stream().filter(instance -> !onlyRequired || ((TestInstance)instance.value()).isRequired()), NOOP_TEST_INSTANCE_BLOCK_FINDER);
        }

        public TestFinder selector(CommandContext<ServerCommandSource> context, Collection<RegistryEntry.Reference<TestInstance>> selected) {
            return this.build(context.getSource(), selected::stream, NOOP_TEST_INSTANCE_BLOCK_FINDER);
        }

        public TestFinder failed(CommandContext<ServerCommandSource> context) {
            return this.failed(context, false);
        }
    }
}

