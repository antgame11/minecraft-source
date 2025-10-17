/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.context.CommandContext;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.command.argument.ParserBackedArgumentType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.SnbtParsing;
import net.minecraft.util.packrat.Parser;

public class NbtElementArgumentType
extends ParserBackedArgumentType<NbtElement> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0", "0b", "0l", "0.0", "\"foo\"", "{foo=bar}", "[0]");
    private static final Parser<NbtElement> PARSER = SnbtParsing.createParser(NbtOps.INSTANCE);

    private NbtElementArgumentType() {
        super(PARSER);
    }

    public static NbtElementArgumentType nbtElement() {
        return new NbtElementArgumentType();
    }

    public static <S> NbtElement getNbtElement(CommandContext<S> context, String name) {
        return context.getArgument(name, NbtElement.class);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

