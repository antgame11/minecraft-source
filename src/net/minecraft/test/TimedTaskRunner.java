/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.test.GameTestException;
import net.minecraft.test.GameTestState;
import net.minecraft.test.TestException;
import net.minecraft.test.TimedTask;
import net.minecraft.text.Text;

public class TimedTaskRunner {
    final GameTestState test;
    private final List<TimedTask> tasks = Lists.newArrayList();
    private int tick;

    TimedTaskRunner(GameTestState gameTest) {
        this.test = gameTest;
        this.tick = gameTest.getTick();
    }

    public TimedTaskRunner createAndAdd(Runnable task) {
        this.tasks.add(TimedTask.create(task));
        return this;
    }

    public TimedTaskRunner createAndAdd(long duration, Runnable task) {
        this.tasks.add(TimedTask.create(duration, task));
        return this;
    }

    public TimedTaskRunner expectMinDuration(int minDuration) {
        return this.expectMinDurationAndRun(minDuration, () -> {});
    }

    public TimedTaskRunner createAndAddReported(Runnable task) {
        this.tasks.add(TimedTask.create(() -> this.tryRun(task)));
        return this;
    }

    public TimedTaskRunner expectMinDurationAndRun(int minDuration, Runnable task) {
        this.tasks.add(TimedTask.create(() -> {
            if (this.test.getTick() < this.tick + minDuration) {
                throw new GameTestException(Text.translatable("test.error.sequence.not_completed"), this.test.getTick());
            }
            this.tryRun(task);
        }));
        return this;
    }

    public TimedTaskRunner expectMinDurationOrRun(int minDuration, Runnable task) {
        this.tasks.add(TimedTask.create(() -> {
            if (this.test.getTick() < this.tick + minDuration) {
                this.tryRun(task);
                throw new GameTestException(Text.translatable("test.error.sequence.not_completed"), this.test.getTick());
            }
        }));
        return this;
    }

    public void completeIfSuccessful() {
        this.tasks.add(TimedTask.create(this.test::completeIfSuccessful));
    }

    public void fail(Supplier<TestException> exceptionSupplier) {
        this.tasks.add(TimedTask.create(() -> this.test.fail((TestException)exceptionSupplier.get())));
    }

    public Trigger createAndAddTrigger() {
        Trigger lv = new Trigger();
        this.tasks.add(TimedTask.create(() -> lv.trigger(this.test.getTick())));
        return lv;
    }

    public void runSilently(int tick) {
        try {
            this.runTasks(tick);
        } catch (GameTestException gameTestException) {
            // empty catch block
        }
    }

    public void runReported(int tick) {
        try {
            this.runTasks(tick);
        } catch (GameTestException lv) {
            this.test.fail(lv);
        }
    }

    private void tryRun(Runnable task) {
        try {
            task.run();
        } catch (GameTestException lv) {
            this.test.fail(lv);
        }
    }

    private void runTasks(int tick) {
        Iterator<TimedTask> iterator = this.tasks.iterator();
        while (iterator.hasNext()) {
            TimedTask lv = iterator.next();
            lv.task.run();
            iterator.remove();
            int j = tick - this.tick;
            int k = this.tick;
            this.tick = tick;
            if (lv.duration == null || lv.duration == (long)j) continue;
            this.test.fail(new GameTestException(Text.translatable("test.error.sequence.invalid_tick", (long)k + lv.duration), tick));
            break;
        }
    }

    public class Trigger {
        private static final int UNTRIGGERED_TICK = -1;
        private int triggeredTick = -1;

        void trigger(int tick) {
            if (this.triggeredTick != -1) {
                throw new IllegalStateException("Condition already triggered at " + this.triggeredTick);
            }
            this.triggeredTick = tick;
        }

        public void checkTrigger() {
            int i = TimedTaskRunner.this.test.getTick();
            if (this.triggeredTick != i) {
                if (this.triggeredTick == -1) {
                    throw new GameTestException(Text.translatable("test.error.sequence.condition_not_triggered"), i);
                }
                throw new GameTestException(Text.translatable("test.error.sequence.condition_already_triggered", this.triggeredTick), i);
            }
        }
    }
}

