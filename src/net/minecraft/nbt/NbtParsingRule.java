/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.nbt;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.packrat.ParsingRule;
import net.minecraft.util.packrat.ParsingState;
import org.jetbrains.annotations.Nullable;

public class NbtParsingRule<T>
implements ParsingRule<StringReader, Dynamic<?>> {
    private final StringNbtReader<T> nbtReader;

    public NbtParsingRule(DynamicOps<T> ops) {
        this.nbtReader = StringNbtReader.fromOps(ops);
    }

    @Override
    @Nullable
    public Dynamic<T> parse(ParsingState<StringReader> arg) {
        arg.getReader().skipWhitespace();
        int i = arg.getCursor();
        try {
            return new Dynamic<T>(this.nbtReader.getOps(), this.nbtReader.readAsArgument(arg.getReader()));
        } catch (Exception exception) {
            arg.getErrors().add(i, exception);
            return null;
        }
    }

    @Override
    @Nullable
    public /* synthetic */ Object parse(ParsingState state) {
        return this.parse(state);
    }
}

