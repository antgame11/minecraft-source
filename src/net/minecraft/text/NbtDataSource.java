/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;

public interface NbtDataSource {
    public Stream<NbtCompound> get(ServerCommandSource var1) throws CommandSyntaxException;

    public MapCodec<? extends NbtDataSource> getCodec();
}

