/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.timer;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.timer.Timer;
import net.minecraft.world.timer.TimerCallback;

public record FunctionTimerCallback(Identifier name) implements TimerCallback<MinecraftServer>
{
    public static final MapCodec<FunctionTimerCallback> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("Name")).forGetter(FunctionTimerCallback::name)).apply((Applicative<FunctionTimerCallback, ?>)instance, FunctionTimerCallback::new));

    @Override
    public void call(MinecraftServer minecraftServer, Timer<MinecraftServer> arg, long l) {
        CommandFunctionManager lv = minecraftServer.getCommandFunctionManager();
        lv.getFunction(this.name).ifPresent(function -> lv.execute((CommandFunction<ServerCommandSource>)function, lv.getScheduledCommandSource()));
    }

    @Override
    public MapCodec<FunctionTimerCallback> getCodec() {
        return CODEC;
    }
}

