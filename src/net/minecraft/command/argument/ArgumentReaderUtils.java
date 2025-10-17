/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import net.minecraft.util.function.CharPredicate;

public class ArgumentReaderUtils {
    public static String readWhileMatching(StringReader stringReader, CharPredicate predicate) {
        int i = stringReader.getCursor();
        while (stringReader.canRead() && predicate.test(stringReader.peek())) {
            stringReader.skip();
        }
        return stringReader.getString().substring(i, stringReader.getCursor());
    }
}

