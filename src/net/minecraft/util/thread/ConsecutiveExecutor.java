/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.thread;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.SampleType;
import net.minecraft.util.profiler.Sampler;
import net.minecraft.util.thread.ExecutorSampling;
import net.minecraft.util.thread.SampleableExecutor;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.util.thread.TaskQueue;
import org.slf4j.Logger;

public abstract class ConsecutiveExecutor<T extends Runnable>
implements SampleableExecutor,
TaskExecutor<T>,
Runnable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final AtomicReference<Status> status = new AtomicReference<Status>(Status.SLEEPING);
    private final TaskQueue<T> queue;
    private final Executor executor;
    private final String name;

    public ConsecutiveExecutor(TaskQueue<T> queue, Executor executor, String name) {
        this.executor = executor;
        this.queue = queue;
        this.name = name;
        ExecutorSampling.INSTANCE.add(this);
    }

    private boolean canRun() {
        return !this.isClosed() && !this.queue.isEmpty();
    }

    @Override
    public void close() {
        this.status.set(Status.CLOSED);
    }

    private boolean runOnce() {
        if (!this.isRunning()) {
            return false;
        }
        Runnable runnable = this.queue.poll();
        if (runnable == null) {
            return false;
        }
        Util.runInNamedZone(runnable, this.name);
        return true;
    }

    @Override
    public void run() {
        try {
            this.runOnce();
        } finally {
            this.sleep();
            this.scheduleSelf();
        }
    }

    public void runAll() {
        try {
            while (this.runOnce()) {
            }
        } finally {
            this.sleep();
            this.scheduleSelf();
        }
    }

    @Override
    public void send(T runnable) {
        this.queue.add(runnable);
        this.scheduleSelf();
    }

    private void scheduleSelf() {
        if (this.canRun() && this.wakeUp()) {
            try {
                this.executor.execute(this);
            } catch (RejectedExecutionException rejectedExecutionException) {
                try {
                    this.executor.execute(this);
                } catch (RejectedExecutionException rejectedExecutionException2) {
                    LOGGER.error("Could not schedule ConsecutiveExecutor", rejectedExecutionException2);
                }
            }
        }
    }

    public int queueSize() {
        return this.queue.getSize();
    }

    public boolean hasQueuedTasks() {
        return this.isRunning() && !this.queue.isEmpty();
    }

    public String toString() {
        return this.name + " " + String.valueOf((Object)this.status.get()) + " " + this.queue.isEmpty();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<Sampler> createSamplers() {
        return ImmutableList.of(Sampler.create(this.name + "-queue-size", SampleType.CONSECUTIVE_EXECUTORS, this::queueSize));
    }

    private boolean wakeUp() {
        return this.status.compareAndSet(Status.SLEEPING, Status.RUNNING);
    }

    private void sleep() {
        this.status.compareAndSet(Status.RUNNING, Status.SLEEPING);
    }

    private boolean isRunning() {
        return this.status.get() == Status.RUNNING;
    }

    private boolean isClosed() {
        return this.status.get() == Status.CLOSED;
    }

    static enum Status {
        SLEEPING,
        RUNNING,
        CLOSED;

    }
}

