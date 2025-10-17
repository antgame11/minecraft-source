/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.timer;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.timer.Timer;
import net.minecraft.world.timer.TimerCallback;

public record FunctionTagTimerCallback(Identifier name) implements TimerCallback<MinecraftServer>
{
    public static final MapCodec<FunctionTagTimerCallback> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("Name")).forGetter(FunctionTagTimerCallback::name)).apply((Applicative<FunctionTagTimerCallback, ?>)instance, FunctionTagTimerCallback::new));

    @Override
    public void call(MinecraftServer minecraftServer, Timer<MinecraftServer> arg, long l) {
        CommandFunctionManager lv = minecraftServer.getCommandFunctionManager();
        List<CommandFunction<ServerCommandSource>> list = lv.getTag(this.name);
        for (CommandFunction<ServerCommandSource> lv2 : list) {
            lv.execute(lv2, lv.getScheduledCommandSource());
        }
    }

    @Override
    public MapCodec<FunctionTagTimerCallback> getCodec() {
        return CODEC;
    }
}

