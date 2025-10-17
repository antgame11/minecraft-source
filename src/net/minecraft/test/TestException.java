/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import net.minecraft.text.Text;

public abstract class TestException
extends RuntimeException {
    public TestException(String message) {
        super(message);
    }

    public abstract Text getText();
}

