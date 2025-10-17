/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.TestInstanceBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestAttemptConfig;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestException;
import net.minecraft.test.TestInstance;
import net.minecraft.test.TestListener;
import net.minecraft.test.TestRunContext;
import net.minecraft.test.TickLimitExceededException;
import net.minecraft.test.TimedTaskRunner;
import net.minecraft.test.UnknownTestException;
import net.minecraft.text.Text;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.tick.WorldTickScheduler;
import org.jetbrains.annotations.Nullable;

public class GameTestState {
    private final RegistryEntry.Reference<TestInstance> instanceEntry;
    @Nullable
    private BlockPos testBlockPos;
    private final ServerWorld world;
    private final Collection<TestListener> listeners = Lists.newArrayList();
    private final int tickLimit;
    private final Collection<TimedTaskRunner> timedTaskRunners = Lists.newCopyOnWriteArrayList();
    private final Object2LongMap<Runnable> ticksByRunnables = new Object2LongOpenHashMap<Runnable>();
    private boolean initialized;
    private boolean tickedOnce;
    private int tick;
    private boolean started;
    private final TestAttemptConfig testAttemptConfig;
    private final Stopwatch stopwatch = Stopwatch.createUnstarted();
    private boolean completed;
    private final BlockRotation rotation;
    @Nullable
    private TestException exception;
    @Nullable
    private TestInstanceBlockEntity blockEntity;

    public GameTestState(RegistryEntry.Reference<TestInstance> instanceEntry, BlockRotation rotation, ServerWorld world, TestAttemptConfig testAttemptConfig) {
        this.instanceEntry = instanceEntry;
        this.world = world;
        this.testAttemptConfig = testAttemptConfig;
        this.tickLimit = instanceEntry.value().getMaxTicks();
        this.rotation = rotation;
    }

    public void setTestBlockPos(@Nullable BlockPos testBlockPos) {
        this.testBlockPos = testBlockPos;
    }

    public GameTestState startCountdown(int additionalExpectedStopTime) {
        this.tick = -(this.instanceEntry.value().getSetupTicks() + additionalExpectedStopTime + 1);
        return this;
    }

    public void initializeImmediately() {
        if (this.initialized) {
            return;
        }
        TestInstanceBlockEntity lv = this.getTestInstanceBlockEntity();
        if (!lv.placeStructure()) {
            this.fail(Text.translatable("test.error.structure.failure", lv.getTestName().getString()));
        }
        this.initialized = true;
        lv.placeBarriers();
        BlockBox lv2 = lv.getBlockBox();
        ((WorldTickScheduler)this.world.getBlockTickScheduler()).clearNextTicks(lv2);
        this.world.clearUpdatesInArea(lv2);
        this.listeners.forEach(listener -> listener.onStarted(this));
    }

    public void tick(TestRunContext context) {
        if (this.isCompleted()) {
            return;
        }
        if (!this.initialized) {
            this.fail(Text.translatable("test.error.ticking_without_structure"));
        }
        if (this.blockEntity == null) {
            this.fail(Text.translatable("test.error.missing_block_entity"));
        }
        if (this.exception != null) {
            this.complete();
        }
        if (!this.tickedOnce) {
            if (!this.blockEntity.getBlockBox().streamChunkPos().allMatch(this.world::shouldTickTestAt)) {
                return;
            }
        }
        this.tickedOnce = true;
        this.tickTests();
        if (this.isCompleted()) {
            if (this.exception != null) {
                this.listeners.forEach(listener -> listener.onFailed(this, context));
            } else {
                this.listeners.forEach(listener -> listener.onPassed(this, context));
            }
        }
    }

    private void tickTests() {
        ++this.tick;
        if (this.tick < 0) {
            return;
        }
        if (!this.started) {
            this.start();
        }
        Iterator objectIterator = this.ticksByRunnables.object2LongEntrySet().iterator();
        while (objectIterator.hasNext()) {
            Object2LongMap.Entry entry = (Object2LongMap.Entry)objectIterator.next();
            if (entry.getLongValue() > (long)this.tick) continue;
            try {
                ((Runnable)entry.getKey()).run();
            } catch (TestException lv) {
                this.fail(lv);
            } catch (Exception exception) {
                this.fail(new UnknownTestException(exception));
            }
            objectIterator.remove();
        }
        if (this.tick > this.tickLimit) {
            if (this.timedTaskRunners.isEmpty()) {
                this.fail(new TickLimitExceededException(Text.translatable("test.error.timeout.no_result", this.instanceEntry.value().getMaxTicks())));
            } else {
                this.timedTaskRunners.forEach(runner -> runner.runReported(this.tick));
                if (this.exception == null) {
                    this.fail(new TickLimitExceededException(Text.translatable("test.error.timeout.no_sequences_finished", this.instanceEntry.value().getMaxTicks())));
                }
            }
        } else {
            this.timedTaskRunners.forEach(runner -> runner.runSilently(this.tick));
        }
    }

    private void start() {
        if (this.started) {
            return;
        }
        this.started = true;
        this.getTestInstanceBlockEntity().setRunning();
        try {
            this.instanceEntry.value().start(new TestContext(this));
        } catch (TestException lv) {
            this.fail(lv);
        } catch (Exception exception) {
            this.fail(new UnknownTestException(exception));
        }
    }

    public void runAtTick(long tick, Runnable runnable) {
        this.ticksByRunnables.put(runnable, tick);
    }

    public Identifier getId() {
        return this.instanceEntry.registryKey().getValue();
    }

    @Nullable
    public BlockPos getPos() {
        return this.testBlockPos;
    }

    public BlockPos getOrigin() {
        return this.blockEntity.getStartPos();
    }

    public Box getBoundingBox() {
        TestInstanceBlockEntity lv = this.getTestInstanceBlockEntity();
        return lv.getBox();
    }

    public TestInstanceBlockEntity getTestInstanceBlockEntity() {
        if (this.blockEntity == null) {
            if (this.testBlockPos == null) {
                throw new IllegalStateException("This GameTestInfo has no position");
            }
            BlockEntity blockEntity = this.world.getBlockEntity(this.testBlockPos);
            if (blockEntity instanceof TestInstanceBlockEntity) {
                TestInstanceBlockEntity lv;
                this.blockEntity = lv = (TestInstanceBlockEntity)blockEntity;
            }
            if (this.blockEntity == null) {
                throw new IllegalStateException("Could not find a test instance block entity at the given coordinate " + String.valueOf(this.testBlockPos));
            }
        }
        return this.blockEntity;
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public boolean isPassed() {
        return this.completed && this.exception == null;
    }

    public boolean isFailed() {
        return this.exception != null;
    }

    public boolean isStarted() {
        return this.started;
    }

    public boolean isCompleted() {
        return this.completed;
    }

    public long getElapsedMilliseconds() {
        return this.stopwatch.elapsed(TimeUnit.MILLISECONDS);
    }

    private void complete() {
        if (!this.completed) {
            this.completed = true;
            if (this.stopwatch.isRunning()) {
                this.stopwatch.stop();
            }
        }
    }

    public void completeIfSuccessful() {
        if (this.exception == null) {
            this.complete();
            Box lv = this.getBoundingBox();
            List<Entity> list = this.getWorld().getEntitiesByClass(Entity.class, lv.expand(1.0), entity -> !(entity instanceof PlayerEntity));
            list.forEach(entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        }
    }

    public void fail(Text message) {
        this.fail(new GameTestException(message, this.tick));
    }

    public void fail(TestException exception) {
        this.exception = exception;
    }

    @Nullable
    public TestException getThrowable() {
        return this.exception;
    }

    public String toString() {
        return this.getId().toString();
    }

    public void addListener(TestListener listener) {
        this.listeners.add(listener);
    }

    @Nullable
    public GameTestState init() {
        TestInstanceBlockEntity lv = this.placeTestInstance(Objects.requireNonNull(this.testBlockPos), this.rotation, this.world);
        if (lv != null) {
            this.blockEntity = lv;
            this.initializeImmediately();
            return this;
        }
        return null;
    }

    @Nullable
    private TestInstanceBlockEntity placeTestInstance(BlockPos pos, BlockRotation rotation, ServerWorld world) {
        world.setBlockState(pos, Blocks.TEST_INSTANCE_BLOCK.getDefaultState());
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof TestInstanceBlockEntity) {
            TestInstanceBlockEntity lv = (TestInstanceBlockEntity)blockEntity;
            RegistryKey<TestInstance> lv2 = this.getInstanceEntry().registryKey();
            Vec3i lv3 = TestInstanceBlockEntity.getStructureSize(world, lv2).orElse(new Vec3i(1, 1, 1));
            lv.setData(new TestInstanceBlockEntity.Data(Optional.of(lv2), lv3, rotation, false, TestInstanceBlockEntity.Status.CLEARED, Optional.empty()));
            return lv;
        }
        return null;
    }

    int getTick() {
        return this.tick;
    }

    TimedTaskRunner createTimedTaskRunner() {
        TimedTaskRunner lv = new TimedTaskRunner(this);
        this.timedTaskRunners.add(lv);
        return lv;
    }

    public boolean isRequired() {
        return this.instanceEntry.value().isRequired();
    }

    public boolean isOptional() {
        return !this.instanceEntry.value().isRequired();
    }

    public Identifier getStructure() {
        return this.instanceEntry.value().getStructure();
    }

    public BlockRotation getRotation() {
        return this.instanceEntry.value().getData().rotation().rotate(this.rotation);
    }

    public TestInstance getInstance() {
        return this.instanceEntry.value();
    }

    public RegistryEntry.Reference<TestInstance> getInstanceEntry() {
        return this.instanceEntry;
    }

    public int getTickLimit() {
        return this.tickLimit;
    }

    public boolean isFlaky() {
        return this.instanceEntry.value().getMaxAttempts() > 1;
    }

    public int getMaxAttempts() {
        return this.instanceEntry.value().getMaxAttempts();
    }

    public int getRequiredSuccesses() {
        return this.instanceEntry.value().getRequiredSuccesses();
    }

    public TestAttemptConfig getTestAttemptConfig() {
        return this.testAttemptConfig;
    }

    public Stream<TestListener> streamListeners() {
        return this.listeners.stream();
    }

    public GameTestState copy() {
        GameTestState lv = new GameTestState(this.instanceEntry, this.rotation, this.world, this.getTestAttemptConfig());
        if (this.testBlockPos != null) {
            lv.setTestBlockPos(this.testBlockPos);
        }
        return lv;
    }
}

