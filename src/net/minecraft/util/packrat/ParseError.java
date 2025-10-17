/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.packrat;

import net.minecraft.util.packrat.Suggestable;

public record ParseError<S>(int cursor, Suggestable<S> suggestions, Object reason) {
}

