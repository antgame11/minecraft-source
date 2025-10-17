/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import net.minecraft.test.GameTestState;
import net.minecraft.test.TestRunContext;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class TestManager {
    public static final TestManager INSTANCE = new TestManager();
    private static final Logger field_61884 = LogUtils.getLogger();
    private final Collection<GameTestState> tests = Lists.newCopyOnWriteArrayList();
    @Nullable
    private TestRunContext runContext;
    private State state = State.IDLE;

    private TestManager() {
    }

    public void start(GameTestState test) {
        this.tests.add(test);
    }

    public void clear() {
        if (this.state != State.IDLE) {
            this.state = State.HALTING;
            return;
        }
        this.tests.clear();
        if (this.runContext != null) {
            this.runContext.clear();
            this.runContext = null;
        }
    }

    public void setRunContext(TestRunContext runContext) {
        if (this.runContext != null) {
            Util.logErrorOrPause("The runner was already set in GameTestTicker");
        }
        this.runContext = runContext;
    }

    public void tick() {
        if (this.runContext == null) {
            return;
        }
        this.state = State.RUNNING;
        this.tests.forEach(test -> test.tick(this.runContext));
        this.tests.removeIf(GameTestState::isCompleted);
        State lv = this.state;
        this.state = State.IDLE;
        if (lv == State.HALTING) {
            this.clear();
        }
    }

    static enum State {
        IDLE,
        RUNNING,
        HALTING;

    }
}

