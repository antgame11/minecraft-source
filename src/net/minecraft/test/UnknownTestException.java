/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import net.minecraft.test.TestException;
import net.minecraft.text.Text;

public class UnknownTestException
extends TestException {
    private final Throwable throwable;

    public UnknownTestException(Throwable throwable) {
        super(throwable.getMessage());
        this.throwable = throwable;
    }

    @Override
    public Text getText() {
        return Text.translatable("test.error.unknown", this.throwable.getMessage());
    }
}

