/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

@FunctionalInterface
public interface ArgumentGetter<T, R> {
    public R apply(T var1) throws CommandSyntaxException;
}

