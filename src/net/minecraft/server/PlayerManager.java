/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.SynchronizeTagsS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.SimulationDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderCenterChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInitializeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInterpolateSizeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderSizeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningBlocksChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningTimeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.network.state.PlayStateFactories;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.registry.tag.TagPacketSerializer;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedIpList;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.OperatorList;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.Whitelist;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.management.listener.ManagementListener;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.NameToIdCache;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.path.PathUtil;
import net.minecraft.world.GameRules;
import net.minecraft.world.PlayerSaveHandler;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class PlayerManager {
    public static final File BANNED_PLAYERS_FILE = new File("banned-players.json");
    public static final File BANNED_IPS_FILE = new File("banned-ips.json");
    public static final File OPERATORS_FILE = new File("ops.json");
    public static final File WHITELIST_FILE = new File("whitelist.json");
    public static final Text FILTERED_FULL_TEXT = Text.translatable("chat.filtered_full");
    public static final Text DUPLICATE_LOGIN_TEXT = Text.translatable("multiplayer.disconnect.duplicate_login");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int LATENCY_UPDATE_INTERVAL = 600;
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
    private final MinecraftServer server;
    private final List<ServerPlayerEntity> players = Lists.newArrayList();
    private final Map<UUID, ServerPlayerEntity> playerMap = Maps.newHashMap();
    private final BannedPlayerList bannedProfiles;
    private final BannedIpList bannedIps;
    private final OperatorList ops;
    private final Whitelist whitelist;
    private final Map<UUID, ServerStatHandler> statisticsMap = Maps.newHashMap();
    private final Map<UUID, PlayerAdvancementTracker> advancementTrackers = Maps.newHashMap();
    private final PlayerSaveHandler saveHandler;
    private final CombinedDynamicRegistries<ServerDynamicRegistryType> registryManager;
    private int viewDistance;
    private int simulationDistance;
    private boolean cheatsAllowed;
    private int latencyUpdateTimer;

    public PlayerManager(MinecraftServer server, CombinedDynamicRegistries<ServerDynamicRegistryType> registryManager, PlayerSaveHandler saveHandler, ManagementListener managementListener) {
        this.server = server;
        this.registryManager = registryManager;
        this.saveHandler = saveHandler;
        this.whitelist = new Whitelist(WHITELIST_FILE, managementListener);
        this.ops = new OperatorList(OPERATORS_FILE, managementListener);
        this.bannedProfiles = new BannedPlayerList(BANNED_PLAYERS_FILE, managementListener);
        this.bannedIps = new BannedIpList(BANNED_IPS_FILE, managementListener);
    }

    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData) {
        PlayerConfigEntry lv = player.getPlayerConfigEntry();
        NameToIdCache lv2 = this.server.getApiServices().nameToIdCache();
        Optional<PlayerConfigEntry> optional = lv2.getByUuid(lv.id());
        String string = optional.map(PlayerConfigEntry::name).orElse(lv.name());
        lv2.add(lv);
        ServerWorld lv3 = player.getEntityWorld();
        String string2 = connection.getAddressAsString(this.server.shouldLogIps());
        LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {})", player.getStringifiedName(), string2, player.getId(), player.getX(), player.getY(), player.getZ());
        WorldProperties lv4 = lv3.getLevelProperties();
        ServerPlayNetworkHandler lv5 = new ServerPlayNetworkHandler(this.server, connection, player, clientData);
        connection.transitionInbound(PlayStateFactories.C2S.bind(RegistryByteBuf.makeFactory(this.server.getRegistryManager()), lv5), lv5);
        GameRules lv6 = lv3.getGameRules();
        boolean bl = lv6.getBoolean(GameRules.DO_IMMEDIATE_RESPAWN);
        boolean bl2 = lv6.getBoolean(GameRules.REDUCED_DEBUG_INFO);
        boolean bl3 = lv6.getBoolean(GameRules.DO_LIMITED_CRAFTING);
        lv5.sendPacket(new GameJoinS2CPacket(player.getId(), lv4.isHardcore(), this.server.getWorldRegistryKeys(), this.getMaxPlayerCount(), this.getViewDistance(), this.getSimulationDistance(), bl2, !bl, bl3, player.createCommonPlayerSpawnInfo(lv3), this.server.shouldEnforceSecureProfile()));
        lv5.sendPacket(new DifficultyS2CPacket(lv4.getDifficulty(), lv4.isDifficultyLocked()));
        lv5.sendPacket(new PlayerAbilitiesS2CPacket(player.getAbilities()));
        lv5.sendPacket(new UpdateSelectedSlotS2CPacket(player.getInventory().getSelectedSlot()));
        ServerRecipeManager lv7 = this.server.getRecipeManager();
        lv5.sendPacket(new SynchronizeRecipesS2CPacket(lv7.getPropertySets(), lv7.getStonecutterRecipeForSync()));
        this.sendCommandTree(player);
        player.getStatHandler().updateStatSet();
        player.getRecipeBook().sendInitRecipesPacket(player);
        this.sendScoreboard(lv3.getScoreboard(), player);
        this.server.forcePlayerSampleUpdate();
        MutableText lv8 = player.getGameProfile().name().equalsIgnoreCase(string) ? Text.translatable("multiplayer.player.joined", player.getDisplayName()) : Text.translatable("multiplayer.player.joined.renamed", player.getDisplayName(), string);
        this.broadcast(lv8.formatted(Formatting.YELLOW), false);
        lv5.requestTeleport(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
        ServerMetadata lv9 = this.server.getServerMetadata();
        if (lv9 != null && !clientData.transferred()) {
            player.sendServerMetadata(lv9);
        }
        player.networkHandler.sendPacket(PlayerListS2CPacket.entryFromPlayer(this.players));
        this.players.add(player);
        this.playerMap.put(player.getUuid(), player);
        this.sendToAll(PlayerListS2CPacket.entryFromPlayer(List.of(player)));
        this.sendWorldInfo(player, lv3);
        lv3.onPlayerConnected(player);
        this.server.getBossBarManager().onPlayerConnect(player);
        this.sendStatusEffects(player);
        player.onSpawn();
        this.server.getManagementListener().onPlayerJoined(player);
    }

    protected void sendScoreboard(ServerScoreboard scoreboard, ServerPlayerEntity player) {
        HashSet<ScoreboardObjective> set = Sets.newHashSet();
        for (Team lv : scoreboard.getTeams()) {
            player.networkHandler.sendPacket(TeamS2CPacket.updateTeam(lv, true));
        }
        for (ScoreboardDisplaySlot lv2 : ScoreboardDisplaySlot.values()) {
            ScoreboardObjective lv3 = scoreboard.getObjectiveForSlot(lv2);
            if (lv3 == null || set.contains(lv3)) continue;
            List<Packet<?>> list = scoreboard.createChangePackets(lv3);
            for (Packet<?> lv4 : list) {
                player.networkHandler.sendPacket(lv4);
            }
            set.add(lv3);
        }
    }

    public void setMainWorld(final ServerWorld world) {
        world.getWorldBorder().addListener(new WorldBorderListener(){

            @Override
            public void onSizeChange(WorldBorder border, double size) {
                PlayerManager.this.sendToDimension(new WorldBorderSizeChangedS2CPacket(border), world.getRegistryKey());
            }

            @Override
            public void onInterpolateSize(WorldBorder border, double fromSize, double toSize, long time) {
                PlayerManager.this.sendToDimension(new WorldBorderInterpolateSizeS2CPacket(border), world.getRegistryKey());
            }

            @Override
            public void onCenterChanged(WorldBorder border, double centerX, double centerZ) {
                PlayerManager.this.sendToDimension(new WorldBorderCenterChangedS2CPacket(border), world.getRegistryKey());
            }

            @Override
            public void onWarningTimeChanged(WorldBorder border, int warningTime) {
                PlayerManager.this.sendToDimension(new WorldBorderWarningTimeChangedS2CPacket(border), world.getRegistryKey());
            }

            @Override
            public void onWarningBlocksChanged(WorldBorder border, int warningBlockDistance) {
                PlayerManager.this.sendToDimension(new WorldBorderWarningBlocksChangedS2CPacket(border), world.getRegistryKey());
            }

            @Override
            public void onDamagePerBlockChanged(WorldBorder border, double damagePerBlock) {
            }

            @Override
            public void onSafeZoneChanged(WorldBorder border, double safeZoneRadius) {
            }
        });
    }

    public Optional<NbtCompound> loadPlayerData(PlayerConfigEntry player) {
        NbtCompound lv = this.server.getSaveProperties().getPlayerData();
        if (this.server.isHost(player) && lv != null) {
            LOGGER.debug("loading single player");
            return Optional.of(lv);
        }
        return this.saveHandler.loadPlayerData(player);
    }

    protected void savePlayerData(ServerPlayerEntity player) {
        PlayerAdvancementTracker lv2;
        this.saveHandler.savePlayerData(player);
        ServerStatHandler lv = this.statisticsMap.get(player.getUuid());
        if (lv != null) {
            lv.save();
        }
        if ((lv2 = this.advancementTrackers.get(player.getUuid())) != null) {
            lv2.save();
        }
    }

    public void remove(ServerPlayerEntity player) {
        Object lv2;
        ServerWorld lv = player.getEntityWorld();
        player.incrementStat(Stats.LEAVE_GAME);
        this.savePlayerData(player);
        if (player.hasVehicle() && ((Entity)(lv2 = player.getRootVehicle())).hasPlayerRider()) {
            LOGGER.debug("Removing player mount");
            player.stopRiding();
            ((Entity)lv2).streamPassengersAndSelf().forEach(entity -> entity.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER));
        }
        player.detach();
        for (EnderPearlEntity lv3 : player.getEnderPearls()) {
            lv3.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        }
        lv.removePlayer(player, Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        player.getAdvancementTracker().clearCriteria();
        this.players.remove(player);
        this.server.getBossBarManager().onPlayerDisconnect(player);
        UUID uUID = player.getUuid();
        ServerPlayerEntity lv4 = this.playerMap.get(uUID);
        if (lv4 == player) {
            this.playerMap.remove(uUID);
            this.statisticsMap.remove(uUID);
            this.advancementTrackers.remove(uUID);
            this.server.getManagementListener().onPlayerLeft(player);
        }
        this.sendToAll(new PlayerRemoveS2CPacket(List.of(player.getUuid())));
    }

    @Nullable
    public Text checkCanJoin(SocketAddress address, PlayerConfigEntry configEntry) {
        if (this.bannedProfiles.contains(configEntry)) {
            BannedPlayerEntry lv = (BannedPlayerEntry)this.bannedProfiles.get(configEntry);
            MutableText lv2 = Text.translatable("multiplayer.disconnect.banned.reason", lv.getReasonText());
            if (lv.getExpiryDate() != null) {
                lv2.append(Text.translatable("multiplayer.disconnect.banned.expiration", DATE_FORMATTER.format(lv.getExpiryDate())));
            }
            return lv2;
        }
        if (!this.isWhitelisted(configEntry)) {
            return Text.translatable("multiplayer.disconnect.not_whitelisted");
        }
        if (this.bannedIps.isBanned(address)) {
            BannedIpEntry lv3 = this.bannedIps.get(address);
            MutableText lv2 = Text.translatable("multiplayer.disconnect.banned_ip.reason", lv3.getReasonText());
            if (lv3.getExpiryDate() != null) {
                lv2.append(Text.translatable("multiplayer.disconnect.banned_ip.expiration", DATE_FORMATTER.format(lv3.getExpiryDate())));
            }
            return lv2;
        }
        if (this.players.size() >= this.getMaxPlayerCount() && !this.canBypassPlayerLimit(configEntry)) {
            return Text.translatable("multiplayer.disconnect.server_full");
        }
        return null;
    }

    public boolean disconnectDuplicateLogins(UUID uuid) {
        Set<ServerPlayerEntity> set = Sets.newIdentityHashSet();
        for (ServerPlayerEntity lv : this.players) {
            if (!lv.getUuid().equals(uuid)) continue;
            set.add(lv);
        }
        ServerPlayerEntity lv2 = this.playerMap.get(uuid);
        if (lv2 != null) {
            set.add(lv2);
        }
        for (ServerPlayerEntity lv3 : set) {
            lv3.networkHandler.disconnect(DUPLICATE_LOGIN_TEXT);
        }
        return !set.isEmpty();
    }

    public ServerPlayerEntity respawnPlayer(ServerPlayerEntity player, boolean alive, Entity.RemovalReason removalReason) {
        BlockPos lv10;
        BlockState lv11;
        WorldProperties.SpawnPoint lv8;
        ServerWorld lv9;
        TeleportTarget lv = player.getRespawnTarget(!alive, TeleportTarget.NO_OP);
        this.players.remove(player);
        player.getEntityWorld().removePlayer(player, removalReason);
        ServerWorld lv2 = lv.world();
        ServerPlayerEntity lv3 = new ServerPlayerEntity(this.server, lv2, player.getGameProfile(), player.getClientOptions());
        lv3.networkHandler = player.networkHandler;
        lv3.copyFrom(player, alive);
        lv3.setId(player.getId());
        lv3.setMainArm(player.getMainArm());
        if (!lv.missingRespawnBlock()) {
            lv3.setSpawnPointFrom(player);
        }
        for (String string : player.getCommandTags()) {
            lv3.addCommandTag(string);
        }
        Vec3d lv4 = lv.position();
        lv3.refreshPositionAndAngles(lv4.x, lv4.y, lv4.z, lv.yaw(), lv.pitch());
        if (lv.missingRespawnBlock()) {
            lv3.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.NO_RESPAWN_BLOCK, GameStateChangeS2CPacket.DEMO_OPEN_SCREEN));
        }
        byte b = alive ? PlayerRespawnS2CPacket.KEEP_ATTRIBUTES : (byte)0;
        ServerWorld lv5 = lv3.getEntityWorld();
        WorldProperties lv6 = lv5.getLevelProperties();
        lv3.networkHandler.sendPacket(new PlayerRespawnS2CPacket(lv3.createCommonPlayerSpawnInfo(lv5), b));
        lv3.networkHandler.requestTeleport(lv3.getX(), lv3.getY(), lv3.getZ(), lv3.getYaw(), lv3.getPitch());
        lv3.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(lv2.getSpawnPoint()));
        lv3.networkHandler.sendPacket(new DifficultyS2CPacket(lv6.getDifficulty(), lv6.isDifficultyLocked()));
        lv3.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(lv3.experienceProgress, lv3.totalExperience, lv3.experienceLevel));
        this.sendStatusEffects(lv3);
        this.sendWorldInfo(lv3, lv2);
        this.sendCommandTree(lv3);
        lv2.onPlayerRespawned(lv3);
        this.players.add(lv3);
        this.playerMap.put(lv3.getUuid(), lv3);
        lv3.onSpawn();
        lv3.setHealth(lv3.getHealth());
        ServerPlayerEntity.Respawn lv7 = lv3.getRespawn();
        if (!alive && lv7 != null && (lv9 = this.server.getWorld((lv8 = lv7.respawnData()).getDimension())) != null && (lv11 = lv9.getBlockState(lv10 = lv8.getPos())).isOf(Blocks.RESPAWN_ANCHOR)) {
            lv3.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.BLOCKS, lv10.getX(), lv10.getY(), lv10.getZ(), 1.0f, 1.0f, lv2.getRandom().nextLong()));
        }
        return lv3;
    }

    public void sendStatusEffects(ServerPlayerEntity player) {
        this.sendStatusEffects(player, player.networkHandler);
    }

    public void sendStatusEffects(LivingEntity entity, ServerPlayNetworkHandler networkHandler) {
        for (StatusEffectInstance lv : entity.getStatusEffects()) {
            networkHandler.sendPacket(new EntityStatusEffectS2CPacket(entity.getId(), lv, false));
        }
    }

    public void sendCommandTree(ServerPlayerEntity player) {
        int i = this.server.getPermissionLevel(player.getPlayerConfigEntry());
        this.sendCommandTree(player, i);
    }

    public void updatePlayerLatency() {
        if (++this.latencyUpdateTimer > 600) {
            this.sendToAll(new PlayerListS2CPacket(EnumSet.of(PlayerListS2CPacket.Action.UPDATE_LATENCY), this.players));
            this.latencyUpdateTimer = 0;
        }
    }

    public void sendToAll(Packet<?> packet) {
        for (ServerPlayerEntity lv : this.players) {
            lv.networkHandler.sendPacket(packet);
        }
    }

    public void sendToDimension(Packet<?> packet, RegistryKey<World> dimension) {
        for (ServerPlayerEntity lv : this.players) {
            if (lv.getEntityWorld().getRegistryKey() != dimension) continue;
            lv.networkHandler.sendPacket(packet);
        }
    }

    public void sendToTeam(PlayerEntity source, Text message) {
        Team lv = source.getScoreboardTeam();
        if (lv == null) {
            return;
        }
        Collection<String> collection = ((AbstractTeam)lv).getPlayerList();
        for (String string : collection) {
            ServerPlayerEntity lv2 = this.getPlayer(string);
            if (lv2 == null || lv2 == source) continue;
            lv2.sendMessage(message);
        }
    }

    public void sendToOtherTeams(PlayerEntity source, Text message) {
        Team lv = source.getScoreboardTeam();
        if (lv == null) {
            this.broadcast(message, false);
            return;
        }
        for (int i = 0; i < this.players.size(); ++i) {
            ServerPlayerEntity lv2 = this.players.get(i);
            if (lv2.getScoreboardTeam() == lv) continue;
            lv2.sendMessage(message);
        }
    }

    public String[] getPlayerNames() {
        String[] strings = new String[this.players.size()];
        for (int i = 0; i < this.players.size(); ++i) {
            strings[i] = this.players.get(i).getGameProfile().name();
        }
        return strings;
    }

    public BannedPlayerList getUserBanList() {
        return this.bannedProfiles;
    }

    public BannedIpList getIpBanList() {
        return this.bannedIps;
    }

    public void addToOperators(PlayerConfigEntry player) {
        this.addToOperators(player, Optional.empty(), Optional.empty());
    }

    public void addToOperators(PlayerConfigEntry player, Optional<Integer> permissionLevel, Optional<Boolean> canBypassPlayerLimit) {
        this.ops.add(new OperatorEntry(player, permissionLevel.orElse(this.server.getOpPermissionLevel()), canBypassPlayerLimit.orElse(this.ops.canBypassPlayerLimit(player))));
        ServerPlayerEntity lv = this.getPlayer(player.id());
        if (lv != null) {
            this.sendCommandTree(lv);
        }
    }

    public void removeFromOperators(PlayerConfigEntry player) {
        ServerPlayerEntity lv;
        if (this.ops.remove(player) && (lv = this.getPlayer(player.id())) != null) {
            this.sendCommandTree(lv);
        }
    }

    private void sendCommandTree(ServerPlayerEntity player, int permissionLevel) {
        if (player.networkHandler != null) {
            byte b = permissionLevel <= 0 ? (byte)24 : (permissionLevel >= 4 ? (byte)28 : (byte)((byte)(EntityStatuses.SET_OP_LEVEL_0 + permissionLevel)));
            player.networkHandler.sendPacket(new EntityStatusS2CPacket(player, b));
        }
        this.server.getCommandManager().sendCommandTree(player);
    }

    public boolean isWhitelisted(PlayerConfigEntry player) {
        return !this.isWhitelistEnabled() || this.ops.contains(player) || this.whitelist.contains(player);
    }

    public boolean isOperator(PlayerConfigEntry player) {
        return this.ops.contains(player) || this.server.isHost(player) && this.server.getSaveProperties().areCommandsAllowed() || this.cheatsAllowed;
    }

    @Nullable
    public ServerPlayerEntity getPlayer(String name) {
        int i = this.players.size();
        for (int j = 0; j < i; ++j) {
            ServerPlayerEntity lv = this.players.get(j);
            if (!lv.getGameProfile().name().equalsIgnoreCase(name)) continue;
            return lv;
        }
        return null;
    }

    public void sendToAround(@Nullable PlayerEntity player, double x, double y, double z, double distance, RegistryKey<World> worldKey, Packet<?> packet) {
        for (int i = 0; i < this.players.size(); ++i) {
            double k;
            double j;
            double h;
            ServerPlayerEntity lv = this.players.get(i);
            if (lv == player || lv.getEntityWorld().getRegistryKey() != worldKey || !((h = x - lv.getX()) * h + (j = y - lv.getY()) * j + (k = z - lv.getZ()) * k < distance * distance)) continue;
            lv.networkHandler.sendPacket(packet);
        }
    }

    public void saveAllPlayerData() {
        for (int i = 0; i < this.players.size(); ++i) {
            this.savePlayerData(this.players.get(i));
        }
    }

    public Whitelist getWhitelist() {
        return this.whitelist;
    }

    public String[] getWhitelistedNames() {
        return this.whitelist.getNames();
    }

    public OperatorList getOpList() {
        return this.ops;
    }

    public String[] getOpNames() {
        return this.ops.getNames();
    }

    public void reloadWhitelist() {
    }

    public void sendWorldInfo(ServerPlayerEntity player, ServerWorld world) {
        WorldBorder lv = world.getWorldBorder();
        player.networkHandler.sendPacket(new WorldBorderInitializeS2CPacket(lv));
        player.networkHandler.sendPacket(new WorldTimeUpdateS2CPacket(world.getTime(), world.getTimeOfDay(), world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)));
        player.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(world.getSpawnPoint()));
        if (world.isRaining()) {
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_STARTED, GameStateChangeS2CPacket.DEMO_OPEN_SCREEN));
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED, world.getRainGradient(1.0f)));
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED, world.getThunderGradient(1.0f)));
        }
        player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.INITIAL_CHUNKS_COMING, GameStateChangeS2CPacket.DEMO_OPEN_SCREEN));
        this.server.getTickManager().sendPackets(player);
    }

    public void sendPlayerStatus(ServerPlayerEntity player) {
        player.playerScreenHandler.syncState();
        player.markHealthDirty();
        player.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(player.getInventory().getSelectedSlot()));
    }

    public int getCurrentPlayerCount() {
        return this.players.size();
    }

    public int getMaxPlayerCount() {
        return this.server.getMaxPlayerCount();
    }

    public boolean isWhitelistEnabled() {
        return this.server.getUseAllowlist();
    }

    public List<ServerPlayerEntity> getPlayersByIp(String ip) {
        ArrayList<ServerPlayerEntity> list = Lists.newArrayList();
        for (ServerPlayerEntity lv : this.players) {
            if (!lv.getIp().equals(ip)) continue;
            list.add(lv);
        }
        return list;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public int getSimulationDistance() {
        return this.simulationDistance;
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    @Nullable
    public NbtCompound getUserData() {
        return null;
    }

    public void setCheatsAllowed(boolean cheatsAllowed) {
        this.cheatsAllowed = cheatsAllowed;
    }

    public void disconnectAllPlayers() {
        for (int i = 0; i < this.players.size(); ++i) {
            this.players.get((int)i).networkHandler.disconnect(Text.translatable("multiplayer.disconnect.server_shutdown"));
        }
    }

    public void broadcast(Text message, boolean overlay) {
        this.broadcast(message, (ServerPlayerEntity player) -> message, overlay);
    }

    public void broadcast(Text message, Function<ServerPlayerEntity, Text> playerMessageFactory, boolean overlay) {
        this.server.sendMessage(message);
        for (ServerPlayerEntity lv : this.players) {
            Text lv2 = playerMessageFactory.apply(lv);
            if (lv2 == null) continue;
            lv.sendMessageToClient(lv2, overlay);
        }
    }

    public void broadcast(SignedMessage message, ServerCommandSource source, MessageType.Parameters params) {
        this.broadcast(message, source::shouldFilterText, source.getPlayer(), params);
    }

    public void broadcast(SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params) {
        this.broadcast(message, sender::shouldFilterMessagesSentTo, sender, params);
    }

    private void broadcast(SignedMessage message, Predicate<ServerPlayerEntity> shouldSendFiltered, @Nullable ServerPlayerEntity sender, MessageType.Parameters params) {
        boolean bl = this.verify(message);
        this.server.logChatMessage(message.getContent(), params, bl ? null : "Not Secure");
        SentMessage lv = SentMessage.of(message);
        boolean bl2 = false;
        for (ServerPlayerEntity lv2 : this.players) {
            boolean bl3 = shouldSendFiltered.test(lv2);
            lv2.sendChatMessage(lv, bl3, params);
            bl2 |= bl3 && message.isFullyFiltered();
        }
        if (bl2 && sender != null) {
            sender.sendMessage(FILTERED_FULL_TEXT);
        }
    }

    private boolean verify(SignedMessage message) {
        return message.hasSignature() && !message.isExpiredOnServer(Instant.now());
    }

    public ServerStatHandler createStatHandler(PlayerEntity player) {
        GameProfile gameProfile = player.getGameProfile();
        UUID uUID = gameProfile.id();
        ServerStatHandler lv = this.statisticsMap.get(uUID);
        if (lv == null) {
            File file3;
            Path path;
            File file = this.server.getSavePath(WorldSavePath.STATS).toFile();
            File file2 = new File(file, String.valueOf(uUID) + ".json");
            if (!file2.exists() && PathUtil.isNormal(path = (file3 = new File(file, gameProfile.name() + ".json")).toPath()) && PathUtil.isAllowedName(path) && path.startsWith(file.getPath()) && file3.isFile()) {
                file3.renameTo(file2);
            }
            lv = new ServerStatHandler(this.server, file2);
            this.statisticsMap.put(uUID, lv);
        }
        return lv;
    }

    public PlayerAdvancementTracker getAdvancementTracker(ServerPlayerEntity player) {
        UUID uUID = player.getUuid();
        PlayerAdvancementTracker lv = this.advancementTrackers.get(uUID);
        if (lv == null) {
            Path path = this.server.getSavePath(WorldSavePath.ADVANCEMENTS).resolve(String.valueOf(uUID) + ".json");
            lv = new PlayerAdvancementTracker(this.server.getDataFixer(), this, this.server.getAdvancementLoader(), path, player);
            this.advancementTrackers.put(uUID, lv);
        }
        lv.setOwner(player);
        return lv;
    }

    public void setViewDistance(int viewDistance) {
        this.viewDistance = viewDistance;
        this.sendToAll(new ChunkLoadDistanceS2CPacket(viewDistance));
        for (ServerWorld lv : this.server.getWorlds()) {
            if (lv == null) continue;
            lv.getChunkManager().applyViewDistance(viewDistance);
        }
    }

    public void setSimulationDistance(int simulationDistance) {
        this.simulationDistance = simulationDistance;
        this.sendToAll(new SimulationDistanceS2CPacket(simulationDistance));
        for (ServerWorld lv : this.server.getWorlds()) {
            if (lv == null) continue;
            lv.getChunkManager().applySimulationDistance(simulationDistance);
        }
    }

    public List<ServerPlayerEntity> getPlayerList() {
        return this.players;
    }

    @Nullable
    public ServerPlayerEntity getPlayer(UUID uuid) {
        return this.playerMap.get(uuid);
    }

    @Nullable
    public ServerPlayerEntity isAlreadyConnected(String playerName) {
        for (ServerPlayerEntity lv : this.players) {
            if (!lv.getGameProfile().name().equalsIgnoreCase(playerName)) continue;
            return lv;
        }
        return null;
    }

    public boolean canBypassPlayerLimit(PlayerConfigEntry configEntry) {
        return false;
    }

    public void onDataPacksReloaded() {
        for (PlayerAdvancementTracker lv : this.advancementTrackers.values()) {
            lv.reload(this.server.getAdvancementLoader());
        }
        this.sendToAll(new SynchronizeTagsS2CPacket(TagPacketSerializer.serializeTags(this.registryManager)));
        ServerRecipeManager lv2 = this.server.getRecipeManager();
        SynchronizeRecipesS2CPacket lv3 = new SynchronizeRecipesS2CPacket(lv2.getPropertySets(), lv2.getStonecutterRecipeForSync());
        for (ServerPlayerEntity lv4 : this.players) {
            lv4.networkHandler.sendPacket(lv3);
            lv4.getRecipeBook().sendInitRecipesPacket(lv4);
        }
    }

    public boolean areCheatsAllowed() {
        return this.cheatsAllowed;
    }
}

