/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.text.Text;

public record CoordinateArgument(boolean relative, double value) {
    private static final char TILDE = '~';
    public static final SimpleCommandExceptionType MISSING_COORDINATE = new SimpleCommandExceptionType(Text.translatable("argument.pos.missing.double"));
    public static final SimpleCommandExceptionType MISSING_BLOCK_POSITION = new SimpleCommandExceptionType(Text.translatable("argument.pos.missing.int"));

    public double toAbsoluteCoordinate(double offset) {
        if (this.relative) {
            return this.value + offset;
        }
        return this.value;
    }

    public static CoordinateArgument parse(StringReader reader, boolean centerIntegers) throws CommandSyntaxException {
        if (reader.canRead() && reader.peek() == '^') {
            throw Vec3ArgumentType.MIXED_COORDINATE_EXCEPTION.createWithContext(reader);
        }
        if (!reader.canRead()) {
            throw MISSING_COORDINATE.createWithContext(reader);
        }
        boolean bl2 = CoordinateArgument.isRelative(reader);
        int i = reader.getCursor();
        double d = reader.canRead() && reader.peek() != ' ' ? reader.readDouble() : 0.0;
        String string = reader.getString().substring(i, reader.getCursor());
        if (bl2 && string.isEmpty()) {
            return new CoordinateArgument(true, 0.0);
        }
        if (!string.contains(".") && !bl2 && centerIntegers) {
            d += 0.5;
        }
        return new CoordinateArgument(bl2, d);
    }

    public static CoordinateArgument parse(StringReader reader) throws CommandSyntaxException {
        if (reader.canRead() && reader.peek() == '^') {
            throw Vec3ArgumentType.MIXED_COORDINATE_EXCEPTION.createWithContext(reader);
        }
        if (!reader.canRead()) {
            throw MISSING_BLOCK_POSITION.createWithContext(reader);
        }
        boolean bl = CoordinateArgument.isRelative(reader);
        double d = reader.canRead() && reader.peek() != ' ' ? (bl ? reader.readDouble() : (double)reader.readInt()) : 0.0;
        return new CoordinateArgument(bl, d);
    }

    public static boolean isRelative(StringReader reader) {
        boolean bl;
        if (reader.peek() == '~') {
            bl = true;
            reader.skip();
        } else {
            bl = false;
        }
        return bl;
    }

    public boolean isRelative() {
        return this.relative;
    }
}

