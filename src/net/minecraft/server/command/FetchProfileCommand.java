/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.nbt.NbtOps;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.GameProfileResolver;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.Texts;
import net.minecraft.text.object.PlayerTextObjectContents;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

public class FetchProfileCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("fetchprofile").requires(CommandManager.requirePermissionLevel(2))).then(CommandManager.literal("name").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("name", StringArgumentType.greedyString()).executes(context -> FetchProfileCommand.executeName((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "name")))))).then(CommandManager.literal("id").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("id", UuidArgumentType.uuid()).executes(context -> FetchProfileCommand.executeId((ServerCommandSource)context.getSource(), UuidArgumentType.getUuid(context, "id"))))));
    }

    private static void sendResult(ServerCommandSource source, GameProfile profile, String successText, Text inputText) {
        ProfileComponent lv = ProfileComponent.ofStatic(profile);
        ProfileComponent.CODEC.encodeStart(NbtOps.INSTANCE, lv).ifSuccess(profileComponent -> {
            String string2 = profileComponent.toString();
            MutableText lv = Text.object(new PlayerTextObjectContents(lv, true));
            TextCodecs.CODEC.encodeStart(NbtOps.INSTANCE, lv).ifSuccess(headText -> {
                String string3 = headText.toString();
                source.sendFeedback(() -> {
                    MutableText lv = Texts.join(List.of(Text.translatable("commands.fetchprofile.copy_component").styled(style -> style.withClickEvent(new ClickEvent.CopyToClipboard(string2))), Text.translatable("commands.fetchprofile.give_item").styled(style -> style.withClickEvent(new ClickEvent.RunCommand("give @s minecraft:player_head[profile=" + string2 + "]"))), Text.translatable("commands.fetchprofile.summon_mannequin").styled(style -> style.withClickEvent(new ClickEvent.RunCommand("summon minecraft:mannequin ~ ~ ~ {profile:" + string2 + "}"))), Text.translatable("commands.fetchprofile.copy_text", lv.formatted(Formatting.WHITE)).styled(style -> style.withClickEvent(new ClickEvent.CopyToClipboard(string3)))), ScreenTexts.SPACE, style -> Texts.bracketed(style.formatted(Formatting.GREEN)));
                    return Text.translatable(successText, inputText, lv);
                }, false);
            }).ifError(error -> source.sendError(Text.translatable("commands.fetchprofile.failed_to_serialize", error.message())));
        }).ifError(error -> source.sendError(Text.translatable("commands.fetchprofile.failed_to_serialize", error.message())));
    }

    private static int executeName(ServerCommandSource source, String name) {
        MinecraftServer minecraftServer = source.getServer();
        GameProfileResolver lv = minecraftServer.getApiServices().profileResolver();
        Util.getDownloadWorkerExecutor().execute(() -> {
            MutableText lv = Text.literal(name);
            Optional<GameProfile> optional = lv.getProfileByName(name);
            minecraftServer.execute(() -> optional.ifPresentOrElse(profile -> FetchProfileCommand.sendResult(source, profile, "commands.fetchprofile.name.success", lv), () -> source.sendError(Text.translatable("commands.fetchprofile.name.failure", lv))));
        });
        return 1;
    }

    private static int executeId(ServerCommandSource source, UUID id) {
        MinecraftServer minecraftServer = source.getServer();
        GameProfileResolver lv = minecraftServer.getApiServices().profileResolver();
        Util.getDownloadWorkerExecutor().execute(() -> {
            Text lv = Text.of(id);
            Optional<GameProfile> optional = lv.getProfileById(id);
            minecraftServer.execute(() -> optional.ifPresentOrElse(profile -> FetchProfileCommand.sendResult(source, profile, "commands.fetchprofile.id.success", lv), () -> source.sendError(Text.translatable("commands.fetchprofile.id.failure", lv))));
        });
        return 1;
    }
}

