/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ReloadCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.path.PathUtil;
import org.slf4j.Logger;

public class DatapackCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DynamicCommandExceptionType UNKNOWN_DATAPACK_EXCEPTION = new DynamicCommandExceptionType(name -> Text.stringifiedTranslatable("commands.datapack.unknown", name));
    private static final DynamicCommandExceptionType ALREADY_ENABLED_EXCEPTION = new DynamicCommandExceptionType(name -> Text.stringifiedTranslatable("commands.datapack.enable.failed", name));
    private static final DynamicCommandExceptionType ALREADY_DISABLED_EXCEPTION = new DynamicCommandExceptionType(name -> Text.stringifiedTranslatable("commands.datapack.disable.failed", name));
    private static final DynamicCommandExceptionType CANNOT_DISABLE_FEATURE_EXCEPTION = new DynamicCommandExceptionType(name -> Text.stringifiedTranslatable("commands.datapack.disable.failed.feature", name));
    private static final Dynamic2CommandExceptionType NO_FLAGS_EXCEPTION = new Dynamic2CommandExceptionType((name, flags) -> Text.stringifiedTranslatable("commands.datapack.enable.failed.no_flags", name, flags));
    private static final DynamicCommandExceptionType INVALID_NAME_EXCEPTION = new DynamicCommandExceptionType(name -> Text.stringifiedTranslatable("commands.datapack.create.invalid_name", name));
    private static final DynamicCommandExceptionType INVALID_FULL_NAME_EXCEPTION = new DynamicCommandExceptionType(name -> Text.stringifiedTranslatable("commands.datapack.create.invalid_full_name", name));
    private static final DynamicCommandExceptionType ALREADY_EXISTS_EXCEPTION = new DynamicCommandExceptionType(name -> Text.stringifiedTranslatable("commands.datapack.create.already_exists", name));
    private static final Dynamic2CommandExceptionType METADATA_ENCODE_FAILURE_EXCEPTION = new Dynamic2CommandExceptionType((name, message) -> Text.stringifiedTranslatable("commands.datapack.create.metadata_encode_failure", name, message));
    private static final DynamicCommandExceptionType IO_FAILURE_EXCEPTION = new DynamicCommandExceptionType(name -> Text.stringifiedTranslatable("commands.datapack.create.io_failure", name));
    private static final SuggestionProvider<ServerCommandSource> ENABLED_CONTAINERS_SUGGESTION_PROVIDER = (context, builder) -> CommandSource.suggestMatching(((ServerCommandSource)context.getSource()).getServer().getDataPackManager().getEnabledIds().stream().map(StringArgumentType::escapeIfRequired), builder);
    private static final SuggestionProvider<ServerCommandSource> DISABLED_CONTAINERS_SUGGESTION_PROVIDER = (context, builder) -> {
        ResourcePackManager lv = ((ServerCommandSource)context.getSource()).getServer().getDataPackManager();
        Collection<String> collection = lv.getEnabledIds();
        FeatureSet lv2 = ((ServerCommandSource)context.getSource()).getEnabledFeatures();
        return CommandSource.suggestMatching(lv.getProfiles().stream().filter(profile -> profile.getRequestedFeatures().isSubsetOf(lv2)).map(ResourcePackProfile::getId).filter(name -> !collection.contains(name)).map(StringArgumentType::escapeIfRequired), builder);
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("datapack").requires(CommandManager.requirePermissionLevel(2))).then(CommandManager.literal("enable").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("name", StringArgumentType.string()).suggests(DISABLED_CONTAINERS_SUGGESTION_PROVIDER).executes(context -> DatapackCommand.executeEnable((ServerCommandSource)context.getSource(), DatapackCommand.getPackContainer(context, "name", true), (profiles, profile) -> profile.getInitialPosition().insert(profiles, profile, ResourcePackProfile::getPosition, false)))).then(CommandManager.literal("after").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("existing", StringArgumentType.string()).suggests(ENABLED_CONTAINERS_SUGGESTION_PROVIDER).executes(context -> DatapackCommand.executeEnable((ServerCommandSource)context.getSource(), DatapackCommand.getPackContainer(context, "name", true), (profiles, profile) -> profiles.add(profiles.indexOf(DatapackCommand.getPackContainer(context, "existing", false)) + 1, profile)))))).then(CommandManager.literal("before").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("existing", StringArgumentType.string()).suggests(ENABLED_CONTAINERS_SUGGESTION_PROVIDER).executes(context -> DatapackCommand.executeEnable((ServerCommandSource)context.getSource(), DatapackCommand.getPackContainer(context, "name", true), (profiles, profile) -> profiles.add(profiles.indexOf(DatapackCommand.getPackContainer(context, "existing", false)), profile)))))).then(CommandManager.literal("last").executes(context -> DatapackCommand.executeEnable((ServerCommandSource)context.getSource(), DatapackCommand.getPackContainer(context, "name", true), List::add)))).then(CommandManager.literal("first").executes(context -> DatapackCommand.executeEnable((ServerCommandSource)context.getSource(), DatapackCommand.getPackContainer(context, "name", true), (profiles, profile) -> profiles.add(0, profile))))))).then(CommandManager.literal("disable").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("name", StringArgumentType.string()).suggests(ENABLED_CONTAINERS_SUGGESTION_PROVIDER).executes(context -> DatapackCommand.executeDisable((ServerCommandSource)context.getSource(), DatapackCommand.getPackContainer(context, "name", false)))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("list").executes(context -> DatapackCommand.executeList((ServerCommandSource)context.getSource()))).then(CommandManager.literal("available").executes(context -> DatapackCommand.executeListAvailable((ServerCommandSource)context.getSource())))).then(CommandManager.literal("enabled").executes(context -> DatapackCommand.executeListEnabled((ServerCommandSource)context.getSource()))))).then(((LiteralArgumentBuilder)CommandManager.literal("create").requires(CommandManager.requirePermissionLevel(4))).then(CommandManager.argument("id", StringArgumentType.string()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("description", TextArgumentType.text(registryAccess)).executes(context -> DatapackCommand.executeCreate((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "id"), TextArgumentType.parseTextArgument(context, "description")))))));
    }

    private static int executeCreate(ServerCommandSource source, String id, Text description) throws CommandSyntaxException {
        Path path = source.getServer().getSavePath(WorldSavePath.DATAPACKS);
        if (!PathUtil.isFileNameValid(id)) {
            throw INVALID_NAME_EXCEPTION.create(id);
        }
        if (!PathUtil.isNotReservedWindowsName(id)) {
            throw INVALID_FULL_NAME_EXCEPTION.create(id);
        }
        Path path2 = path.resolve(id);
        if (Files.exists(path2, new LinkOption[0])) {
            throw ALREADY_EXISTS_EXCEPTION.create(id);
        }
        PackResourceMetadata lv = new PackResourceMetadata(description, SharedConstants.getGameVersion().packVersion(ResourceType.SERVER_DATA).majorRange());
        DataResult<JsonElement> dataResult = PackResourceMetadata.SERVER_DATA_SERIALIZER.codec().encodeStart(JsonOps.INSTANCE, lv);
        Optional<DataResult.Error<JsonElement>> optional = dataResult.error();
        if (optional.isPresent()) {
            throw METADATA_ENCODE_FAILURE_EXCEPTION.create(id, optional.get().message());
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(PackResourceMetadata.SERVER_DATA_SERIALIZER.name(), dataResult.getOrThrow());
        try {
            Files.createDirectory(path2, new FileAttribute[0]);
            Files.createDirectory(path2.resolve(ResourceType.SERVER_DATA.getDirectory()), new FileAttribute[0]);
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path2.resolve("pack.mcmeta"), StandardCharsets.UTF_8, new OpenOption[0]);
                 JsonWriter jsonWriter = new JsonWriter(bufferedWriter);){
                jsonWriter.setSerializeNulls(false);
                jsonWriter.setIndent("  ");
                JsonHelper.writeSorted(jsonWriter, jsonObject, null);
            }
        } catch (IOException iOException) {
            LOGGER.warn("Failed to create pack at {}", (Object)path.toAbsolutePath(), (Object)iOException);
            throw IO_FAILURE_EXCEPTION.create(id);
        }
        source.sendFeedback(() -> Text.translatable("commands.datapack.create.success", id), true);
        return 1;
    }

    private static int executeEnable(ServerCommandSource source, ResourcePackProfile container, PackAdder packAdder) throws CommandSyntaxException {
        ResourcePackManager lv = source.getServer().getDataPackManager();
        ArrayList<ResourcePackProfile> list = Lists.newArrayList(lv.getEnabledProfiles());
        packAdder.apply(list, container);
        source.sendFeedback(() -> Text.translatable("commands.datapack.modify.enable", container.getInformationText(true)), true);
        ReloadCommand.tryReloadDataPacks(list.stream().map(ResourcePackProfile::getId).collect(Collectors.toList()), source);
        return list.size();
    }

    private static int executeDisable(ServerCommandSource source, ResourcePackProfile container) {
        ResourcePackManager lv = source.getServer().getDataPackManager();
        ArrayList<ResourcePackProfile> list = Lists.newArrayList(lv.getEnabledProfiles());
        list.remove(container);
        source.sendFeedback(() -> Text.translatable("commands.datapack.modify.disable", container.getInformationText(true)), true);
        ReloadCommand.tryReloadDataPacks(list.stream().map(ResourcePackProfile::getId).collect(Collectors.toList()), source);
        return list.size();
    }

    private static int executeList(ServerCommandSource source) {
        return DatapackCommand.executeListEnabled(source) + DatapackCommand.executeListAvailable(source);
    }

    private static int executeListAvailable(ServerCommandSource source) {
        ResourcePackManager lv = source.getServer().getDataPackManager();
        lv.scanPacks();
        Collection<ResourcePackProfile> collection = lv.getEnabledProfiles();
        Collection<ResourcePackProfile> collection2 = lv.getProfiles();
        FeatureSet lv2 = source.getEnabledFeatures();
        List<ResourcePackProfile> list = collection2.stream().filter(profile -> !collection.contains(profile) && profile.getRequestedFeatures().isSubsetOf(lv2)).toList();
        if (list.isEmpty()) {
            source.sendFeedback(() -> Text.translatable("commands.datapack.list.available.none"), false);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.datapack.list.available.success", list.size(), Texts.join(list, profile -> profile.getInformationText(false))), false);
        }
        return list.size();
    }

    private static int executeListEnabled(ServerCommandSource source) {
        ResourcePackManager lv = source.getServer().getDataPackManager();
        lv.scanPacks();
        Collection<ResourcePackProfile> collection = lv.getEnabledProfiles();
        if (collection.isEmpty()) {
            source.sendFeedback(() -> Text.translatable("commands.datapack.list.enabled.none"), false);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.datapack.list.enabled.success", collection.size(), Texts.join(collection, profile -> profile.getInformationText(true))), false);
        }
        return collection.size();
    }

    private static ResourcePackProfile getPackContainer(CommandContext<ServerCommandSource> context, String name, boolean enable) throws CommandSyntaxException {
        String string2 = StringArgumentType.getString(context, name);
        ResourcePackManager lv = context.getSource().getServer().getDataPackManager();
        ResourcePackProfile lv2 = lv.getProfile(string2);
        if (lv2 == null) {
            throw UNKNOWN_DATAPACK_EXCEPTION.create(string2);
        }
        boolean bl2 = lv.getEnabledProfiles().contains(lv2);
        if (enable && bl2) {
            throw ALREADY_ENABLED_EXCEPTION.create(string2);
        }
        if (!enable && !bl2) {
            throw ALREADY_DISABLED_EXCEPTION.create(string2);
        }
        FeatureSet lv3 = context.getSource().getEnabledFeatures();
        FeatureSet lv4 = lv2.getRequestedFeatures();
        if (!enable && !lv4.isEmpty() && lv2.getSource() == ResourcePackSource.FEATURE) {
            throw CANNOT_DISABLE_FEATURE_EXCEPTION.create(string2);
        }
        if (!lv4.isSubsetOf(lv3)) {
            throw NO_FLAGS_EXCEPTION.create(string2, FeatureFlags.printMissingFlags(lv3, lv4));
        }
        return lv2;
    }

    static interface PackAdder {
        public void apply(List<ResourcePackProfile> var1, ResourcePackProfile var2) throws CommandSyntaxException;
    }
}

