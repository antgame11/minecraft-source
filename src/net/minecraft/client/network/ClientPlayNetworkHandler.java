/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.lang.ref.WeakReference;
import java.time.Instant;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.DemoScreen;
import net.minecraft.client.gui.screen.ReconfiguringScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.screen.dialog.DialogNetworkAccess;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.screen.ingame.CommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.screen.ingame.HorseScreen;
import net.minecraft.client.gui.screen.ingame.TestInstanceBlockScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ChunkBatchSizeCalculator;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientDebugSubscriptionManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.DataQueryHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.network.PingMeasurer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.recipebook.ClientRecipeManager;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.sound.AbstractBeeSoundInstance;
import net.minecraft.client.sound.AggressiveBeeSoundInstance;
import net.minecraft.client.sound.GuardianAttackSoundInstance;
import net.minecraft.client.sound.MovingMinecartSoundInstance;
import net.minecraft.client.sound.PassiveBeeSoundInstance;
import net.minecraft.client.sound.SnifferDigSoundInstance;
import net.minecraft.client.toast.RecipeToast;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.world.ClientChunkLoadProgress;
import net.minecraft.client.world.ClientWaypointHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.world.DataCache;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.SignedArgumentList;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPosition;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.TrackedPosition;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.SnifferEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.ExperimentalMinecartController;
import net.minecraft.entity.vehicle.MinecartController;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.FuelRegistry;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.encryption.ClientPlayerSession;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.encryption.PlayerKeyPair;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.message.ArgumentSignatureDataMap;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageChain;
import net.minecraft.network.message.MessageLink;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.MessageSignatureStorage;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.ClientOptionsC2SPacket;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.c2s.play.AcknowledgeChunksC2SPacket;
import net.minecraft.network.packet.c2s.play.AcknowledgeReconfigurationC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.c2s.play.MessageAcknowledgmentC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerLoadedC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerSessionC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.common.SynchronizeTagsS2CPacket;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockValueDebugS2CPacket;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ChatSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkBiomeDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkRenderDistanceCenterS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkSentS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkValueDebugS2CPacket;
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.network.packet.s2c.play.CommonPlayerSpawnInfo;
import net.minecraft.network.packet.s2c.play.CooldownUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CraftFailedResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.DamageTiltS2CPacket;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.DebugSampleS2CPacket;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.EndCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.EnterCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.EnterReconfigurationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityValueDebugS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EventDebugS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.GameTestHighlightPosS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.LightData;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.LookAtS2CPacket;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.MoveMinecartAlongTrackS2CPacket;
import net.minecraft.network.packet.s2c.play.NbtQueryResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenHorseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenWrittenBookS2CPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerActionResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRotationS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.network.packet.s2c.play.ProfilelessChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ProjectilePowerS2CPacket;
import net.minecraft.network.packet.s2c.play.RecipeBookAddS2CPacket;
import net.minecraft.network.packet.s2c.play.RecipeBookRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.RecipeBookSettingsS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreResetS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SelectAdvancementTabS2CPacket;
import net.minecraft.network.packet.s2c.play.ServerMetadataS2CPacket;
import net.minecraft.network.packet.s2c.play.SetCameraEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.SetCursorItemS2CPacket;
import net.minecraft.network.packet.s2c.play.SetPlayerInventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.minecraft.network.packet.s2c.play.SimulationDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.StartChunkSendS2CPacket;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.network.packet.s2c.play.TestInstanceBlockStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.TickStepS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateTickRateS2CPacket;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.network.packet.s2c.play.WaypointS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderCenterChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInitializeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInterpolateSizeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderSizeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningBlocksChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningTimeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.network.state.ConfigurationStates;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.NetworkRecipeId;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.display.CuttingRecipeDisplay;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.SerializableRegistries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagPacketSerializer;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.sync.ComponentChangesHash;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.storage.NbtReadView;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.HashCodeOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.debug.DebugDataStore;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.tick.TickManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ClientPlayNetworkHandler
extends ClientCommonNetworkHandler
implements ClientPlayPacketListener,
TickablePacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Text UNSECURE_SERVER_TOAST_TITLE = Text.translatable("multiplayer.unsecureserver.toast.title");
    private static final Text UNSECURE_SERVER_TOAST_TEXT = Text.translatable("multiplayer.unsecureserver.toast");
    private static final Text INVALID_PACKET_TEXT = Text.translatable("multiplayer.disconnect.invalid_packet");
    private static final Text RECONFIGURING_TEXT = Text.translatable("connect.reconfiguring");
    private static final Text BAD_CHAT_INDEX_TEXT = Text.translatable("multiplayer.disconnect.bad_chat_index");
    private static final Text CONFIRM_COMMAND_TITLE_TEXT = Text.translatable("multiplayer.confirm_command.title");
    private static final Text CONFIRM_RUN_COMMAND_TEXT = Text.translatable("multiplayer.confirm_command.run_command");
    private static final Text CONFIRM_SUGGEST_COMMAND_TEXT = Text.translatable("multiplayer.confirm_command.suggest_command");
    private static final int ACKNOWLEDGMENT_BATCH_SIZE = 64;
    public static final int field_54852 = 64;
    private static final CommandTreeS2CPacket.NodeFactory<ClientCommandSource> COMMAND_NODE_FACTORY = new CommandTreeS2CPacket.NodeFactory<ClientCommandSource>(){

        @Override
        public ArgumentBuilder<ClientCommandSource, ?> literal(String name) {
            return LiteralArgumentBuilder.literal(name);
        }

        @Override
        public ArgumentBuilder<ClientCommandSource, ?> argument(String name, ArgumentType<?> type, @Nullable Identifier suggestionProviderId) {
            RequiredArgumentBuilder requiredArgumentBuilder = RequiredArgumentBuilder.argument(name, type);
            if (suggestionProviderId != null) {
                requiredArgumentBuilder.suggests(SuggestionProviders.byId(suggestionProviderId));
            }
            return requiredArgumentBuilder;
        }

        @Override
        public ArgumentBuilder<ClientCommandSource, ?> modifyNode(ArgumentBuilder<ClientCommandSource, ?> arg, boolean disableExecution, boolean requireTrusted) {
            if (disableExecution) {
                arg.executes(context -> 0);
            }
            if (requireTrusted) {
                arg.requires(ClientCommandSource::isTrusted);
            }
            return arg;
        }
    };
    private final GameProfile profile;
    private ClientWorld world;
    private ClientWorld.Properties worldProperties;
    private final Map<UUID, PlayerListEntry> playerListEntries = Maps.newHashMap();
    private final Set<PlayerListEntry> listedPlayerListEntries = new ReferenceOpenHashSet<PlayerListEntry>();
    private final ClientAdvancementManager advancementHandler;
    private final ClientCommandSource commandSource;
    private final ClientCommandSource restrictedCommandSource;
    private final DataQueryHandler dataQueryHandler = new DataQueryHandler(this);
    private int chunkLoadDistance = 3;
    private int simulationDistance = 3;
    private final Random random = Random.createThreadSafe();
    private CommandDispatcher<ClientCommandSource> commandDispatcher = new CommandDispatcher();
    private ClientRecipeManager recipeManager = new ClientRecipeManager(Map.of(), CuttingRecipeDisplay.Grouping.empty());
    private final UUID sessionId = UUID.randomUUID();
    private Set<RegistryKey<World>> worldKeys;
    private final DynamicRegistryManager.Immutable combinedDynamicRegistries;
    private final FeatureSet enabledFeatures;
    private final BrewingRecipeRegistry brewingRecipeRegistry;
    private FuelRegistry fuelRegistry;
    private final ComponentChangesHash.ComponentHasher componentHasher;
    private OptionalInt removedPlayerVehicleId = OptionalInt.empty();
    @Nullable
    private ClientPlayerSession session;
    private MessageChain.Packer messagePacker = MessageChain.Packer.NONE;
    private int globalChatMessageIndex;
    private LastSeenMessagesCollector lastSeenMessagesCollector = new LastSeenMessagesCollector(20);
    private MessageSignatureStorage signatureStorage = MessageSignatureStorage.create();
    @Nullable
    private CompletableFuture<Optional<PlayerKeyPair>> profileKeyPairFuture;
    @Nullable
    private SyncedClientOptions syncedOptions;
    private final ChunkBatchSizeCalculator chunkBatchSizeCalculator = new ChunkBatchSizeCalculator();
    private final PingMeasurer pingMeasurer;
    private final ClientDebugSubscriptionManager debugSubscriptionManager;
    @Nullable
    private ClientChunkLoadProgress chunkLoadProgress;
    private boolean secureChatEnforced;
    private volatile boolean worldCleared;
    private final Scoreboard scoreboard = new Scoreboard();
    private final ClientWaypointHandler waypointHandler = new ClientWaypointHandler();
    private final SearchManager searchManager = new SearchManager();
    private final List<WeakReference<DataCache<?, ?>>> cachedData = new ArrayList();

    public ClientPlayNetworkHandler(MinecraftClient client, ClientConnection arg2, ClientConnectionState arg3) {
        super(client, arg2, arg3);
        this.profile = arg3.localGameProfile();
        this.combinedDynamicRegistries = arg3.receivedRegistries();
        RegistryOps<HashCode> lv = this.combinedDynamicRegistries.getOps(HashCodeOps.INSTANCE);
        this.componentHasher = component -> ((HashCode)component.encode(lv).getOrThrow(error -> new IllegalArgumentException("Failed to hash " + String.valueOf(component) + ": " + error))).asInt();
        this.enabledFeatures = arg3.enabledFeatures();
        this.advancementHandler = new ClientAdvancementManager(client, this.worldSession);
        this.commandSource = new ClientCommandSource(this, client, true);
        this.restrictedCommandSource = new ClientCommandSource(this, client, false);
        this.pingMeasurer = new PingMeasurer(this, client.getDebugHud().getPingLog());
        this.debugSubscriptionManager = new ClientDebugSubscriptionManager(this, client.getDebugHud());
        if (arg3.chatState() != null) {
            client.inGameHud.getChatHud().restoreChatState(arg3.chatState());
        }
        this.brewingRecipeRegistry = BrewingRecipeRegistry.create(this.enabledFeatures);
        this.fuelRegistry = FuelRegistry.createDefault(arg3.receivedRegistries(), this.enabledFeatures);
        this.chunkLoadProgress = arg3.chunkLoadProgress();
    }

    public ClientCommandSource getCommandSource() {
        return this.commandSource;
    }

    public void unloadWorld() {
        this.worldCleared = true;
        this.clearWorld();
        this.worldSession.onUnload();
    }

    public void clearWorld() {
        this.clearCachedData();
        this.world = null;
        this.chunkLoadProgress = null;
    }

    private void clearCachedData() {
        for (WeakReference<DataCache<?, ?>> weakReference : this.cachedData) {
            DataCache lv = (DataCache)weakReference.get();
            if (lv == null) continue;
            lv.clean();
        }
        this.cachedData.clear();
    }

    public RecipeManager getRecipeManager() {
        return this.recipeManager;
    }

    @Override
    public void onGameJoin(GameJoinS2CPacket packet) {
        ClientWorld.Properties lv4;
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.client.interactionManager = new ClientPlayerInteractionManager(this.client, this);
        CommonPlayerSpawnInfo lv = packet.commonPlayerSpawnInfo();
        ArrayList<RegistryKey<World>> list = Lists.newArrayList(packet.dimensionIds());
        Collections.shuffle(list);
        this.worldKeys = Sets.newLinkedHashSet(list);
        RegistryKey<World> lv2 = lv.dimension();
        RegistryEntry<DimensionType> lv3 = lv.dimensionType();
        this.chunkLoadDistance = packet.viewDistance();
        this.simulationDistance = packet.simulationDistance();
        boolean bl = lv.isDebug();
        boolean bl2 = lv.isFlat();
        int i = lv.seaLevel();
        this.worldProperties = lv4 = new ClientWorld.Properties(Difficulty.NORMAL, packet.hardcore(), bl2);
        this.world = new ClientWorld(this, lv4, lv2, lv3, this.chunkLoadDistance, this.simulationDistance, this.client.worldRenderer, bl, lv.seed(), i);
        this.client.joinWorld(this.world);
        if (this.client.player == null) {
            this.client.player = this.client.interactionManager.createPlayer(this.world, new StatHandler(), new ClientRecipeBook());
            this.client.player.setYaw(-180.0f);
            if (this.client.getServer() != null) {
                this.client.getServer().setLocalPlayerUuid(this.client.player.getUuid());
            }
        }
        this.debugSubscriptionManager.clearAllSubscriptions();
        this.client.worldRenderer.debugRenderer.initRenderers();
        this.client.player.init();
        this.client.player.setId(packet.playerEntityId());
        this.world.addEntity(this.client.player);
        this.client.player.input = new KeyboardInput(this.client.options);
        this.client.interactionManager.copyAbilities(this.client.player);
        this.client.setCameraEntity(this.client.player);
        this.startWorldLoading(this.client.player, this.world, LevelLoadingScreen.WorldEntryReason.OTHER);
        this.client.player.setReducedDebugInfo(packet.reducedDebugInfo());
        this.client.player.setShowsDeathScreen(packet.showDeathScreen());
        this.client.player.setLimitedCraftingEnabled(packet.doLimitedCrafting());
        this.client.player.setLastDeathPos(lv.lastDeathLocation());
        this.client.player.setPortalCooldown(lv.portalCooldown());
        this.client.interactionManager.setGameModes(lv.gameMode(), lv.lastGameMode());
        this.client.options.setServerViewDistance(packet.viewDistance());
        this.session = null;
        this.messagePacker = MessageChain.Packer.NONE;
        this.globalChatMessageIndex = 0;
        this.lastSeenMessagesCollector = new LastSeenMessagesCollector(20);
        this.signatureStorage = MessageSignatureStorage.create();
        if (this.connection.isEncrypted()) {
            this.fetchProfileKey();
        }
        this.worldSession.setGameMode(lv.gameMode(), packet.hardcore());
        this.client.getQuickPlayLogger().save(this.client);
        this.secureChatEnforced = packet.enforcesSecureChat();
        if (this.serverInfo != null && !this.seenInsecureChatWarning && !this.isSecureChatEnforced()) {
            SystemToast lv5 = SystemToast.create(this.client, SystemToast.Type.UNSECURE_SERVER_WARNING, UNSECURE_SERVER_TOAST_TITLE, UNSECURE_SERVER_TOAST_TEXT);
            this.client.getToastManager().add(lv5);
            this.seenInsecureChatWarning = true;
        }
    }

    @Override
    public void onEntitySpawn(EntitySpawnS2CPacket packet) {
        PlayerEntity lv2;
        UUID uUID;
        PlayerListEntry lv3;
        Entity lv;
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        if (this.removedPlayerVehicleId.isPresent() && this.removedPlayerVehicleId.getAsInt() == packet.getEntityId()) {
            this.removedPlayerVehicleId = OptionalInt.empty();
        }
        if ((lv = this.createEntity(packet)) != null) {
            lv.onSpawnPacket(packet);
            this.world.addEntity(lv);
            this.playSpawnSound(lv);
        } else {
            LOGGER.warn("Skipping Entity with id {}", (Object)packet.getEntityType());
        }
        if (lv instanceof PlayerEntity && (lv3 = this.playerListEntries.get(uUID = (lv2 = (PlayerEntity)lv).getUuid())) != null) {
            this.seenPlayers.put(uUID, lv3);
        }
    }

    @Nullable
    private Entity createEntity(EntitySpawnS2CPacket packet) {
        EntityType<?> lv = packet.getEntityType();
        if (lv == EntityType.PLAYER) {
            PlayerListEntry lv2 = this.getPlayerListEntry(packet.getUuid());
            if (lv2 == null) {
                LOGGER.warn("Server attempted to add player prior to sending player info (Player id {})", (Object)packet.getUuid());
                return null;
            }
            return new OtherClientPlayerEntity(this.world, lv2.getProfile());
        }
        return lv.create(this.world, SpawnReason.LOAD);
    }

    private void playSpawnSound(Entity entity) {
        if (entity instanceof AbstractMinecartEntity) {
            AbstractMinecartEntity lv = (AbstractMinecartEntity)entity;
            this.client.getSoundManager().play(new MovingMinecartSoundInstance(lv));
        } else if (entity instanceof BeeEntity) {
            BeeEntity lv2 = (BeeEntity)entity;
            boolean bl = lv2.hasAngerTime();
            AbstractBeeSoundInstance lv3 = bl ? new AggressiveBeeSoundInstance(lv2) : new PassiveBeeSoundInstance(lv2);
            this.client.getSoundManager().playNextTick(lv3);
        }
    }

    @Override
    public void onEntityVelocityUpdate(EntityVelocityUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = this.world.getEntityById(packet.getEntityId());
        if (lv == null) {
            return;
        }
        lv.setVelocityClient(packet.getVelocity());
    }

    @Override
    public void onEntityTrackerUpdate(EntityTrackerUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = this.world.getEntityById(packet.id());
        if (lv != null) {
            lv.getDataTracker().writeUpdatedEntries(packet.trackedValues());
        }
    }

    @Override
    public void onEntityPositionSync(EntityPositionSyncS2CPacket packet) {
        boolean bl;
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = this.world.getEntityById(packet.id());
        if (lv == null) {
            return;
        }
        Vec3d lv2 = packet.values().position();
        lv.getTrackedPosition().setPos(lv2);
        if (lv.isLogicalSideForUpdatingMovement()) {
            return;
        }
        float f = packet.values().yaw();
        float g = packet.values().pitch();
        boolean bl2 = bl = lv.getEntityPos().squaredDistanceTo(lv2) > 4096.0;
        if (this.world.hasEntity(lv) && !bl) {
            lv.updateTrackedPositionAndAngles(lv2, f, g);
        } else {
            lv.refreshPositionAndAngles(lv2, f, g);
        }
        if (!lv.isInterpolating() && lv.hasPassengerDeep(this.client.player)) {
            lv.updatePassengerPosition(this.client.player);
            this.client.player.resetPosition();
        }
        lv.setOnGround(packet.onGround());
    }

    @Override
    public void onEntityPosition(EntityPositionS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = this.world.getEntityById(packet.entityId());
        if (lv == null) {
            if (this.removedPlayerVehicleId.isPresent() && this.removedPlayerVehicleId.getAsInt() == packet.entityId()) {
                LOGGER.debug("Trying to teleport entity with id {}, that was formerly player vehicle, applying teleport to player instead", (Object)packet.entityId());
                ClientPlayNetworkHandler.setPosition(packet.change(), packet.relatives(), this.client.player, false);
                this.connection.send(new PlayerMoveC2SPacket.Full(this.client.player.getX(), this.client.player.getY(), this.client.player.getZ(), this.client.player.getYaw(), this.client.player.getPitch(), false, false));
            }
            return;
        }
        boolean bl = packet.relatives().contains((Object)PositionFlag.X) || packet.relatives().contains((Object)PositionFlag.Y) || packet.relatives().contains((Object)PositionFlag.Z);
        boolean bl2 = this.world.hasEntity(lv) || !lv.isLogicalSideForUpdatingMovement() || bl;
        boolean bl3 = ClientPlayNetworkHandler.setPosition(packet.change(), packet.relatives(), lv, bl2);
        lv.setOnGround(packet.onGround());
        if (!bl3 && lv.hasPassengerDeep(this.client.player)) {
            lv.updatePassengerPosition(this.client.player);
            this.client.player.resetPosition();
            if (lv.isLogicalSideForUpdatingMovement()) {
                this.connection.send(VehicleMoveC2SPacket.fromVehicle(lv));
            }
        }
    }

    @Override
    public void onUpdateTickRate(UpdateTickRateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        if (this.client.world == null) {
            return;
        }
        TickManager lv = this.client.world.getTickManager();
        lv.setTickRate(packet.tickRate());
        lv.setFrozen(packet.isFrozen());
    }

    @Override
    public void onTickStep(TickStepS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        if (this.client.world == null) {
            return;
        }
        TickManager lv = this.client.world.getTickManager();
        lv.setStepTicks(packet.tickSteps());
    }

    @Override
    public void onUpdateSelectedSlot(UpdateSelectedSlotS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        if (PlayerInventory.isValidHotbarIndex(packet.slot())) {
            this.client.player.getInventory().setSelectedSlot(packet.slot());
        }
    }

    @Override
    public void onEntity(EntityS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = packet.getEntity(this.world);
        if (lv == null) {
            return;
        }
        if (lv.isLogicalSideForUpdatingMovement()) {
            TrackedPosition lv2 = lv.getTrackedPosition();
            Vec3d lv3 = lv2.withDelta(packet.getDeltaX(), packet.getDeltaY(), packet.getDeltaZ());
            lv2.setPos(lv3);
            return;
        }
        if (packet.isPositionChanged()) {
            TrackedPosition lv2 = lv.getTrackedPosition();
            Vec3d lv3 = lv2.withDelta(packet.getDeltaX(), packet.getDeltaY(), packet.getDeltaZ());
            lv2.setPos(lv3);
            if (packet.hasRotation()) {
                lv.updateTrackedPositionAndAngles(lv3, packet.getYaw(), packet.getPitch());
            } else {
                lv.updateTrackedPosition(lv3);
            }
        } else if (packet.hasRotation()) {
            lv.updateTrackedAngles(packet.getYaw(), packet.getPitch());
        }
        lv.setOnGround(packet.isOnGround());
    }

    @Override
    public void onMoveMinecartAlongTrack(MoveMinecartAlongTrackS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = packet.getEntity(this.world);
        if (!(lv instanceof AbstractMinecartEntity)) {
            return;
        }
        AbstractMinecartEntity lv2 = (AbstractMinecartEntity)lv;
        MinecartController minecartController = lv2.getController();
        if (minecartController instanceof ExperimentalMinecartController) {
            ExperimentalMinecartController lv3 = (ExperimentalMinecartController)minecartController;
            lv3.stagingLerpSteps.addAll(packet.lerpSteps());
        }
    }

    @Override
    public void onEntitySetHeadYaw(EntitySetHeadYawS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = packet.getEntity(this.world);
        if (lv == null) {
            return;
        }
        lv.updateTrackedHeadRotation(packet.getHeadYaw(), 3);
    }

    @Override
    public void onEntitiesDestroy(EntitiesDestroyS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        packet.getEntityIds().forEach(id -> {
            Entity lv = this.world.getEntityById(id);
            if (lv == null) {
                return;
            }
            if (lv.hasPassengerDeep(this.client.player)) {
                LOGGER.debug("Remove entity {}:{} that has player as passenger", (Object)lv.getType(), (Object)id);
                this.removedPlayerVehicleId = OptionalInt.of(id);
            }
            this.world.removeEntity(id, Entity.RemovalReason.DISCARDED);
            this.debugSubscriptionManager.removeEntity(lv);
        });
    }

    @Override
    public void onPlayerPositionLook(PlayerPositionLookS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        ClientPlayerEntity lv = this.client.player;
        if (!lv.hasVehicle()) {
            ClientPlayNetworkHandler.setPosition(packet.change(), packet.relatives(), lv, false);
        }
        this.connection.send(new TeleportConfirmC2SPacket(packet.teleportId()));
        this.connection.send(new PlayerMoveC2SPacket.Full(lv.getX(), lv.getY(), lv.getZ(), lv.getYaw(), lv.getPitch(), false, false));
    }

    private static boolean setPosition(EntityPosition pos, Set<PositionFlag> flags, Entity entity, boolean bl) {
        boolean bl2;
        EntityPosition lv = EntityPosition.fromEntity(entity);
        EntityPosition lv2 = EntityPosition.apply(lv, pos, flags);
        boolean bl3 = bl2 = lv.position().squaredDistanceTo(lv2.position()) > 4096.0;
        if (bl && !bl2) {
            entity.updateTrackedPositionAndAngles(lv2.position(), lv2.yaw(), lv2.pitch());
            entity.setVelocity(lv2.deltaMovement());
            return true;
        }
        entity.setPosition(lv2.position());
        entity.setVelocity(lv2.deltaMovement());
        entity.setYaw(lv2.yaw());
        entity.setPitch(lv2.pitch());
        EntityPosition lv3 = new EntityPosition(entity.getLastRenderPos(), Vec3d.ZERO, entity.lastYaw, entity.lastPitch);
        EntityPosition lv4 = EntityPosition.apply(lv3, pos, flags);
        entity.setLastPositionAndAngles(lv4.position(), lv4.yaw(), lv4.pitch());
        return false;
    }

    @Override
    public void onPlayerRotation(PlayerRotationS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        ClientPlayerEntity lv = this.client.player;
        Set<PositionFlag> set = PositionFlag.ofRot(packet.relativeYaw(), packet.relativePitch());
        EntityPosition lv2 = EntityPosition.fromEntity(lv);
        EntityPosition lv3 = EntityPosition.apply(lv2, lv2.withRotation(packet.yaw(), packet.pitch()), set);
        lv.setYaw(lv3.yaw());
        lv.setPitch(lv3.pitch());
        lv.updateLastAngles();
        this.connection.send(new PlayerMoveC2SPacket.LookAndOnGround(lv.getYaw(), lv.getPitch(), false, false));
    }

    @Override
    public void onChunkDeltaUpdate(ChunkDeltaUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        packet.visitUpdates((pos, state) -> this.world.handleBlockUpdate((BlockPos)pos, (BlockState)state, Block.NOTIFY_ALL | Block.FORCE_STATE));
    }

    @Override
    public void onChunkData(ChunkDataS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        int i = packet.getChunkX();
        int j = packet.getChunkZ();
        this.loadChunk(i, j, packet.getChunkData());
        LightData lv = packet.getLightData();
        this.world.enqueueChunkUpdate(() -> {
            this.readLightData(i, j, lv, false);
            WorldChunk lv = this.world.getChunkManager().getWorldChunk(i, j, false);
            if (lv != null) {
                this.scheduleRenderChunk(lv, i, j);
                this.client.worldRenderer.scheduleNeighborUpdates(lv.getPos());
            }
        });
    }

    @Override
    public void onChunkBiomeData(ChunkBiomeDataS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        for (ChunkBiomeDataS2CPacket.Serialized lv : packet.chunkBiomeData()) {
            this.world.getChunkManager().onChunkBiomeData(lv.pos().x, lv.pos().z, lv.toReadingBuf());
        }
        for (ChunkBiomeDataS2CPacket.Serialized lv : packet.chunkBiomeData()) {
            this.world.resetChunkColor(new ChunkPos(lv.pos().x, lv.pos().z));
        }
        for (ChunkBiomeDataS2CPacket.Serialized lv : packet.chunkBiomeData()) {
            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    for (int k = this.world.getBottomSectionCoord(); k <= this.world.getTopSectionCoord(); ++k) {
                        this.client.worldRenderer.scheduleChunkRender(lv.pos().x + i, k, lv.pos().z + j);
                    }
                }
            }
        }
    }

    private void loadChunk(int x, int z, ChunkData chunkData) {
        this.world.getChunkManager().loadChunkFromPacket(x, z, chunkData.getSectionsDataBuf(), chunkData.getHeightmap(), chunkData.getBlockEntities(x, z));
    }

    private void scheduleRenderChunk(WorldChunk chunk, int x, int z) {
        LightingProvider lv = this.world.getChunkManager().getLightingProvider();
        ChunkSection[] lvs = chunk.getSectionArray();
        ChunkPos lv2 = chunk.getPos();
        for (int k = 0; k < lvs.length; ++k) {
            ChunkSection lv3 = lvs[k];
            int l = this.world.sectionIndexToCoord(k);
            lv.setSectionStatus(ChunkSectionPos.from(lv2, l), lv3.isEmpty());
        }
        this.world.scheduleChunkRenders(x - 1, this.world.getBottomSectionCoord(), z - 1, x + 1, this.world.getTopSectionCoord(), z + 1);
    }

    @Override
    public void onUnloadChunk(UnloadChunkS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.world.getChunkManager().unload(packet.pos());
        this.debugSubscriptionManager.removeChunk(packet.pos());
        this.unloadChunk(packet);
    }

    private void unloadChunk(UnloadChunkS2CPacket packet) {
        ChunkPos lv = packet.pos();
        this.world.enqueueChunkUpdate(() -> {
            int i;
            LightingProvider lv = this.world.getLightingProvider();
            lv.setColumnEnabled(lv, false);
            for (i = lv.getBottomY(); i < lv.getTopY(); ++i) {
                ChunkSectionPos lv2 = ChunkSectionPos.from(lv, i);
                lv.enqueueSectionData(LightType.BLOCK, lv2, null);
                lv.enqueueSectionData(LightType.SKY, lv2, null);
            }
            for (i = this.world.getBottomSectionCoord(); i <= this.world.getTopSectionCoord(); ++i) {
                lv.setSectionStatus(ChunkSectionPos.from(lv, i), true);
            }
        });
    }

    @Override
    public void onBlockUpdate(BlockUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.world.handleBlockUpdate(packet.getPos(), packet.getState(), Block.NOTIFY_ALL | Block.FORCE_STATE);
    }

    @Override
    public void onEnterReconfiguration(EnterReconfigurationS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.client.getMessageHandler().processAll();
        this.sendAcknowledgment();
        ChatHud.ChatState lv = this.client.inGameHud.getChatHud().toChatState();
        this.client.enterReconfiguration(new ReconfiguringScreen(RECONFIGURING_TEXT, this.connection));
        this.connection.transitionInbound(ConfigurationStates.S2C, new ClientConfigurationNetworkHandler(this.client, this.connection, new ClientConnectionState(new ClientChunkLoadProgress(), this.profile, this.worldSession, this.combinedDynamicRegistries, this.enabledFeatures, this.brand, this.serverInfo, this.postDisconnectScreen, this.serverCookies, lv, this.customReportDetails, this.getServerLinks(), this.seenPlayers, this.seenInsecureChatWarning)));
        this.sendPacket(AcknowledgeReconfigurationC2SPacket.INSTANCE);
        this.connection.transitionOutbound(ConfigurationStates.C2S);
    }

    @Override
    public void onItemPickupAnimation(ItemPickupAnimationS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = this.world.getEntityById(packet.getEntityId());
        LivingEntity lv2 = (LivingEntity)this.world.getEntityById(packet.getCollectorEntityId());
        if (lv2 == null) {
            lv2 = this.client.player;
        }
        if (lv != null) {
            if (lv instanceof ExperienceOrbEntity) {
                this.world.playSoundClient(lv.getX(), lv.getY(), lv.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.1f, (this.random.nextFloat() - this.random.nextFloat()) * 0.35f + 0.9f, false);
            } else {
                this.world.playSoundClient(lv.getX(), lv.getY(), lv.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f, (this.random.nextFloat() - this.random.nextFloat()) * 1.4f + 2.0f, false);
            }
            EntityRenderState lv3 = this.client.getEntityRenderDispatcher().getAndUpdateRenderState(lv, 1.0f);
            this.client.particleManager.addParticle(new ItemPickupParticle(this.world, lv3, lv2, lv.getVelocity()));
            if (lv instanceof ItemEntity) {
                ItemEntity lv4 = (ItemEntity)lv;
                ItemStack lv5 = lv4.getStack();
                if (!lv5.isEmpty()) {
                    lv5.decrement(packet.getStackAmount());
                }
                if (lv5.isEmpty()) {
                    this.world.removeEntity(packet.getEntityId(), Entity.RemovalReason.DISCARDED);
                }
            } else if (!(lv instanceof ExperienceOrbEntity)) {
                this.world.removeEntity(packet.getEntityId(), Entity.RemovalReason.DISCARDED);
            }
        }
    }

    @Override
    public void onGameMessage(GameMessageS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.client.getMessageHandler().onGameMessage(packet.content(), packet.overlay());
    }

    @Override
    public void onChatMessage(ChatMessageS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        int i = this.globalChatMessageIndex++;
        if (packet.globalIndex() != i) {
            LOGGER.error("Missing or out-of-order chat message from server, expected index {} but got {}", (Object)i, (Object)packet.globalIndex());
            this.connection.disconnect(BAD_CHAT_INDEX_TEXT);
            return;
        }
        Optional<MessageBody> optional = packet.body().toBody(this.signatureStorage);
        if (optional.isEmpty()) {
            LOGGER.error("Message from player with ID {} referenced unrecognized signature id", (Object)packet.sender());
            this.connection.disconnect(INVALID_PACKET_TEXT);
            return;
        }
        this.signatureStorage.add(optional.get(), packet.signature());
        UUID uUID = packet.sender();
        PlayerListEntry lv = this.getPlayerListEntry(uUID);
        if (lv == null) {
            LOGGER.error("Received player chat packet for unknown player with ID: {}", (Object)uUID);
            this.client.getMessageHandler().onUnverifiedMessage(uUID, packet.signature(), packet.serializedParameters());
            return;
        }
        PublicPlayerSession lv2 = lv.getSession();
        MessageLink lv3 = lv2 != null ? new MessageLink(packet.index(), uUID, lv2.sessionId()) : MessageLink.of(uUID);
        SignedMessage lv4 = new SignedMessage(lv3, packet.signature(), optional.get(), packet.unsignedContent(), packet.filterMask());
        lv4 = lv.getMessageVerifier().ensureVerified(lv4);
        if (lv4 != null) {
            this.client.getMessageHandler().onChatMessage(lv4, lv.getProfile(), packet.serializedParameters());
        } else {
            this.client.getMessageHandler().onUnverifiedMessage(uUID, packet.signature(), packet.serializedParameters());
        }
    }

    @Override
    public void onProfilelessChatMessage(ProfilelessChatMessageS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.client.getMessageHandler().onProfilelessMessage(packet.message(), packet.chatType());
    }

    @Override
    public void onRemoveMessage(RemoveMessageS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Optional<MessageSignatureData> optional = packet.messageSignature().getSignature(this.signatureStorage);
        if (optional.isEmpty()) {
            this.connection.disconnect(INVALID_PACKET_TEXT);
            return;
        }
        this.lastSeenMessagesCollector.remove(optional.get());
        if (!this.client.getMessageHandler().removeDelayedMessage(optional.get())) {
            this.client.inGameHud.getChatHud().removeMessage(optional.get());
        }
    }

    @Override
    public void onEntityAnimation(EntityAnimationS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = this.world.getEntityById(packet.getEntityId());
        if (lv == null) {
            return;
        }
        if (packet.getAnimationId() == 0) {
            LivingEntity lv2 = (LivingEntity)lv;
            lv2.swingHand(Hand.MAIN_HAND);
        } else if (packet.getAnimationId() == EntityAnimationS2CPacket.SWING_OFF_HAND) {
            LivingEntity lv2 = (LivingEntity)lv;
            lv2.swingHand(Hand.OFF_HAND);
        } else if (packet.getAnimationId() == EntityAnimationS2CPacket.WAKE_UP) {
            PlayerEntity lv3 = (PlayerEntity)lv;
            lv3.wakeUp(false, false);
        } else if (packet.getAnimationId() == EntityAnimationS2CPacket.CRIT) {
            this.client.particleManager.addEmitter(lv, ParticleTypes.CRIT);
        } else if (packet.getAnimationId() == EntityAnimationS2CPacket.ENCHANTED_HIT) {
            this.client.particleManager.addEmitter(lv, ParticleTypes.ENCHANTED_HIT);
        }
    }

    @Override
    public void onDamageTilt(DamageTiltS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = this.world.getEntityById(packet.id());
        if (lv == null) {
            return;
        }
        lv.animateDamage(packet.yaw());
    }

    @Override
    public void onWorldTimeUpdate(WorldTimeUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.world.setTime(packet.time(), packet.timeOfDay(), packet.tickDayTime());
        this.worldSession.setTick(packet.time());
    }

    @Override
    public void onPlayerSpawnPosition(PlayerSpawnPositionS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.client.world.setSpawnPoint(packet.respawnData());
    }

    @Override
    public void onEntityPassengersSet(EntityPassengersSetS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = this.world.getEntityById(packet.getEntityId());
        if (lv == null) {
            LOGGER.warn("Received passengers for unknown entity");
            return;
        }
        boolean bl = lv.hasPassengerDeep(this.client.player);
        lv.removeAllPassengers();
        for (int i : packet.getPassengerIds()) {
            Entity lv2 = this.world.getEntityById(i);
            if (lv2 == null) continue;
            lv2.startRiding(lv, true, false);
            if (lv2 != this.client.player) continue;
            this.removedPlayerVehicleId = OptionalInt.empty();
            if (bl) continue;
            if (lv instanceof AbstractBoatEntity) {
                this.client.player.lastYaw = lv.getYaw();
                this.client.player.setYaw(lv.getYaw());
                this.client.player.setHeadYaw(lv.getYaw());
            }
            MutableText lv3 = Text.translatable("mount.onboard", this.client.options.sneakKey.getBoundKeyLocalizedText());
            this.client.inGameHud.setOverlayMessage(lv3, false);
            this.client.getNarratorManager().narrateSystemImmediately(lv3);
        }
    }

    @Override
    public void onEntityAttach(EntityAttachS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = this.world.getEntityById(packet.getAttachedEntityId());
        if (lv instanceof Leashable) {
            Leashable lv2 = (Leashable)((Object)lv);
            lv2.setUnresolvedLeashHolderId(packet.getHoldingEntityId());
        }
    }

    private static ItemStack getActiveDeathProtector(PlayerEntity player) {
        for (Hand lv : Hand.values()) {
            ItemStack lv2 = player.getStackInHand(lv);
            if (!lv2.contains(DataComponentTypes.DEATH_PROTECTION)) continue;
            return lv2;
        }
        return new ItemStack(Items.TOTEM_OF_UNDYING);
    }

    @Override
    public void onEntityStatus(EntityStatusS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = packet.getEntity(this.world);
        if (lv != null) {
            switch (packet.getStatus()) {
                case 63: {
                    this.client.getSoundManager().play(new SnifferDigSoundInstance((SnifferEntity)lv));
                    break;
                }
                case 21: {
                    this.client.getSoundManager().play(new GuardianAttackSoundInstance((GuardianEntity)lv));
                    break;
                }
                case 35: {
                    int i = 40;
                    this.client.particleManager.addEmitter(lv, ParticleTypes.TOTEM_OF_UNDYING, 30);
                    this.world.playSoundClient(lv.getX(), lv.getY(), lv.getZ(), SoundEvents.ITEM_TOTEM_USE, lv.getSoundCategory(), 1.0f, 1.0f, false);
                    if (lv != this.client.player) break;
                    this.client.gameRenderer.showFloatingItem(ClientPlayNetworkHandler.getActiveDeathProtector(this.client.player));
                    break;
                }
                default: {
                    lv.handleStatus(packet.getStatus());
                }
            }
        }
    }

    @Override
    public void onEntityDamage(EntityDamageS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = this.world.getEntityById(packet.entityId());
        if (lv == null) {
            return;
        }
        lv.onDamaged(packet.createDamageSource(this.world));
    }

    @Override
    public void onHealthUpdate(HealthUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.client.player.updateHealth(packet.getHealth());
        this.client.player.getHungerManager().setFoodLevel(packet.getFood());
        this.client.player.getHungerManager().setSaturationLevel(packet.getSaturation());
    }

    @Override
    public void onExperienceBarUpdate(ExperienceBarUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.client.player.setExperience(packet.getBarProgress(), packet.getExperienceLevel(), packet.getExperience());
    }

    @Override
    public void onPlayerRespawn(PlayerRespawnS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        CommonPlayerSpawnInfo lv = packet.commonPlayerSpawnInfo();
        RegistryKey<World> lv2 = lv.dimension();
        RegistryEntry<DimensionType> lv3 = lv.dimensionType();
        ClientPlayerEntity lv4 = this.client.player;
        RegistryKey<World> lv5 = lv4.getEntityWorld().getRegistryKey();
        boolean bl = lv2 != lv5;
        LevelLoadingScreen.WorldEntryReason lv6 = this.getWorldEntryReason(lv4.isDead(), lv2, lv5);
        if (bl) {
            ClientWorld.Properties lv7;
            Map<MapIdComponent, MapState> map = this.world.getMapStates();
            boolean bl2 = lv.isDebug();
            boolean bl3 = lv.isFlat();
            int i = lv.seaLevel();
            this.worldProperties = lv7 = new ClientWorld.Properties(this.worldProperties.getDifficulty(), this.worldProperties.isHardcore(), bl3);
            this.world = new ClientWorld(this, lv7, lv2, lv3, this.chunkLoadDistance, this.simulationDistance, this.client.worldRenderer, bl2, lv.seed(), i);
            this.world.putMapStates(map);
            this.client.joinWorld(this.world);
            this.debugSubscriptionManager.clearValues();
        }
        this.client.setCameraEntity(null);
        if (lv4.shouldCloseHandledScreenOnRespawn()) {
            lv4.closeHandledScreen();
        }
        ClientPlayerEntity lv8 = packet.hasFlag(PlayerRespawnS2CPacket.KEEP_TRACKED_DATA) ? this.client.interactionManager.createPlayer(this.world, lv4.getStatHandler(), lv4.getRecipeBook(), lv4.getLastPlayerInput(), lv4.isSprinting()) : this.client.interactionManager.createPlayer(this.world, lv4.getStatHandler(), lv4.getRecipeBook());
        this.startWorldLoading(lv8, this.world, lv6);
        lv8.setId(lv4.getId());
        this.client.player = lv8;
        if (bl) {
            this.client.getMusicTracker().stop();
        }
        this.client.setCameraEntity(lv8);
        if (packet.hasFlag(PlayerRespawnS2CPacket.KEEP_TRACKED_DATA)) {
            List<DataTracker.SerializedEntry<?>> list = lv4.getDataTracker().getChangedEntries();
            if (list != null) {
                lv8.getDataTracker().writeUpdatedEntries(list);
            }
            lv8.setVelocity(lv4.getVelocity());
            lv8.setYaw(lv4.getYaw());
            lv8.setPitch(lv4.getPitch());
        } else {
            lv8.init();
            lv8.setYaw(-180.0f);
        }
        if (packet.hasFlag(PlayerRespawnS2CPacket.KEEP_ATTRIBUTES)) {
            lv8.getAttributes().setFrom(lv4.getAttributes());
        } else {
            lv8.getAttributes().setBaseFrom(lv4.getAttributes());
        }
        this.world.addEntity(lv8);
        lv8.input = new KeyboardInput(this.client.options);
        this.client.interactionManager.copyAbilities(lv8);
        lv8.setReducedDebugInfo(lv4.hasReducedDebugInfo());
        lv8.setShowsDeathScreen(lv4.showsDeathScreen());
        lv8.setLastDeathPos(lv.lastDeathLocation());
        lv8.setPortalCooldown(lv.portalCooldown());
        lv8.nauseaIntensity = lv4.nauseaIntensity;
        lv8.lastNauseaIntensity = lv4.lastNauseaIntensity;
        if (this.client.currentScreen instanceof DeathScreen || this.client.currentScreen instanceof DeathScreen.TitleScreenConfirmScreen) {
            this.client.setScreen(null);
        }
        this.client.interactionManager.setGameModes(lv.gameMode(), lv.lastGameMode());
    }

    private LevelLoadingScreen.WorldEntryReason getWorldEntryReason(boolean dead, RegistryKey<World> from, RegistryKey<World> to) {
        LevelLoadingScreen.WorldEntryReason lv = LevelLoadingScreen.WorldEntryReason.OTHER;
        if (!dead) {
            if (from == World.NETHER || to == World.NETHER) {
                lv = LevelLoadingScreen.WorldEntryReason.NETHER_PORTAL;
            } else if (from == World.END || to == World.END) {
                lv = LevelLoadingScreen.WorldEntryReason.END_PORTAL;
            }
        }
        return lv;
    }

    @Override
    public void onExplosion(ExplosionS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Vec3d lv = packet.center();
        this.client.world.playSoundClient(lv.getX(), lv.getY(), lv.getZ(), packet.explosionSound().value(), SoundCategory.BLOCKS, 4.0f, (1.0f + (this.client.world.random.nextFloat() - this.client.world.random.nextFloat()) * 0.2f) * 0.7f, false);
        this.client.world.addParticleClient(packet.explosionParticle(), lv.getX(), lv.getY(), lv.getZ(), 1.0, 0.0, 0.0);
        this.client.world.addBlockParticleEffects(lv, packet.radius(), packet.blockCount(), packet.blockParticles());
        packet.playerKnockback().ifPresent(this.client.player::addVelocityInternal);
    }

    @Override
    public void onOpenHorseScreen(OpenHorseScreenS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = this.world.getEntityById(packet.getHorseId());
        if (lv instanceof AbstractHorseEntity) {
            AbstractHorseEntity lv2 = (AbstractHorseEntity)lv;
            ClientPlayerEntity lv3 = this.client.player;
            int i = packet.getSlotColumnCount();
            SimpleInventory lv4 = new SimpleInventory(AbstractHorseEntity.getInventorySize(i));
            HorseScreenHandler lv5 = new HorseScreenHandler(packet.getSyncId(), lv3.getInventory(), lv4, lv2, i);
            lv3.currentScreenHandler = lv5;
            this.client.setScreen(new HorseScreen(lv5, lv3.getInventory(), lv2, i));
        }
    }

    @Override
    public void onOpenScreen(OpenScreenS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        HandledScreens.open(packet.getScreenHandlerType(), this.client, packet.getSyncId(), packet.getName());
    }

    @Override
    public void onScreenHandlerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet) {
        CreativeInventoryScreen lv3;
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        ClientPlayerEntity lv = this.client.player;
        ItemStack lv2 = packet.getStack();
        int i = packet.getSlot();
        this.client.getTutorialManager().onSlotUpdate(lv2);
        Screen screen = this.client.currentScreen;
        boolean bl = screen instanceof CreativeInventoryScreen ? !(lv3 = (CreativeInventoryScreen)screen).isInventoryTabSelected() : false;
        if (packet.getSyncId() == 0) {
            ItemStack lv4;
            if (PlayerScreenHandler.isInHotbar(i) && !lv2.isEmpty() && ((lv4 = lv.playerScreenHandler.getSlot(i).getStack()).isEmpty() || lv4.getCount() < lv2.getCount())) {
                lv2.setBobbingAnimationTime(5);
            }
            lv.playerScreenHandler.setStackInSlot(i, packet.getRevision(), lv2);
        } else if (!(packet.getSyncId() != lv.currentScreenHandler.syncId || packet.getSyncId() == 0 && bl)) {
            lv.currentScreenHandler.setStackInSlot(i, packet.getRevision(), lv2);
        }
        if (this.client.currentScreen instanceof CreativeInventoryScreen) {
            lv.playerScreenHandler.setReceivedStack(i, lv2);
            lv.playerScreenHandler.sendContentUpdates();
        }
    }

    @Override
    public void onSetCursorItem(SetCursorItemS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.client.getTutorialManager().onSlotUpdate(packet.contents());
        if (!(this.client.currentScreen instanceof CreativeInventoryScreen)) {
            this.client.player.currentScreenHandler.setCursorStack(packet.contents());
        }
    }

    @Override
    public void onSetPlayerInventory(SetPlayerInventoryS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.client.getTutorialManager().onSlotUpdate(packet.contents());
        this.client.player.getInventory().setStack(packet.slot(), packet.contents());
    }

    @Override
    public void onInventory(InventoryS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        ClientPlayerEntity lv = this.client.player;
        if (packet.syncId() == 0) {
            lv.playerScreenHandler.updateSlotStacks(packet.revision(), packet.contents(), packet.cursorStack());
        } else if (packet.syncId() == lv.currentScreenHandler.syncId) {
            lv.currentScreenHandler.updateSlotStacks(packet.revision(), packet.contents(), packet.cursorStack());
        }
    }

    @Override
    public void onSignEditorOpen(SignEditorOpenS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        BlockPos lv = packet.getPos();
        BlockEntity blockEntity = this.world.getBlockEntity(lv);
        if (blockEntity instanceof SignBlockEntity) {
            SignBlockEntity lv2 = (SignBlockEntity)blockEntity;
            this.client.player.openEditSignScreen(lv2, packet.isFront());
        } else {
            LOGGER.warn("Ignoring openTextEdit on an invalid entity: {} at pos {}", (Object)this.world.getBlockEntity(lv), (Object)lv);
        }
    }

    @Override
    public void onBlockEntityUpdate(BlockEntityUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        BlockPos lv = packet.getPos();
        this.client.world.getBlockEntity(lv, packet.getBlockEntityType()).ifPresent(blockEntity -> {
            try (ErrorReporter.Logging lv = new ErrorReporter.Logging(blockEntity.getReporterContext(), LOGGER);){
                blockEntity.read(NbtReadView.create(lv, this.combinedDynamicRegistries, packet.getNbt()));
            }
            if (blockEntity instanceof CommandBlockBlockEntity && this.client.currentScreen instanceof CommandBlockScreen) {
                ((CommandBlockScreen)this.client.currentScreen).updateCommandBlock();
            }
        });
    }

    @Override
    public void onScreenHandlerPropertyUpdate(ScreenHandlerPropertyUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        ClientPlayerEntity lv = this.client.player;
        if (lv.currentScreenHandler.syncId == packet.getSyncId()) {
            lv.currentScreenHandler.setProperty(packet.getPropertyId(), packet.getValue());
        }
    }

    @Override
    public void onEntityEquipmentUpdate(EntityEquipmentUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = this.world.getEntityById(packet.getEntityId());
        if (lv instanceof LivingEntity) {
            LivingEntity lv2 = (LivingEntity)lv;
            packet.getEquipmentList().forEach(pair -> lv2.equipStack((EquipmentSlot)pair.getFirst(), (ItemStack)pair.getSecond()));
        }
    }

    @Override
    public void onCloseScreen(CloseScreenS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.client.player.closeScreen();
    }

    @Override
    public void onBlockEvent(BlockEventS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.client.world.addSyncedBlockEvent(packet.getPos(), packet.getBlock(), packet.getType(), packet.getData());
    }

    @Override
    public void onBlockBreakingProgress(BlockBreakingProgressS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.client.world.setBlockBreakingInfo(packet.getEntityId(), packet.getPos(), packet.getProgress());
    }

    @Override
    public void onGameStateChange(GameStateChangeS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        ClientPlayerEntity lv = this.client.player;
        GameStateChangeS2CPacket.Reason lv2 = packet.getReason();
        float f = packet.getValue();
        int i = MathHelper.floor(f + 0.5f);
        if (lv2 == GameStateChangeS2CPacket.NO_RESPAWN_BLOCK) {
            ((PlayerEntity)lv).sendMessage(Text.translatable("block.minecraft.spawn.not_valid"), false);
        } else if (lv2 == GameStateChangeS2CPacket.RAIN_STARTED) {
            this.world.getLevelProperties().setRaining(true);
            this.world.setRainGradient(0.0f);
        } else if (lv2 == GameStateChangeS2CPacket.RAIN_STOPPED) {
            this.world.getLevelProperties().setRaining(false);
            this.world.setRainGradient(1.0f);
        } else if (lv2 == GameStateChangeS2CPacket.GAME_MODE_CHANGED) {
            this.client.interactionManager.setGameMode(GameMode.byIndex(i));
        } else if (lv2 == GameStateChangeS2CPacket.GAME_WON) {
            this.client.setScreen(new CreditsScreen(true, () -> {
                this.client.player.networkHandler.sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
                this.client.setScreen(null);
            }));
        } else if (lv2 == GameStateChangeS2CPacket.DEMO_MESSAGE_SHOWN) {
            GameOptions lv3 = this.client.options;
            MutableText lv4 = null;
            if (f == (float)GameStateChangeS2CPacket.DEMO_OPEN_SCREEN) {
                this.client.setScreen(new DemoScreen());
            } else if (f == (float)GameStateChangeS2CPacket.DEMO_MOVEMENT_HELP) {
                lv4 = Text.translatable("demo.help.movement", lv3.forwardKey.getBoundKeyLocalizedText(), lv3.leftKey.getBoundKeyLocalizedText(), lv3.backKey.getBoundKeyLocalizedText(), lv3.rightKey.getBoundKeyLocalizedText());
            } else if (f == (float)GameStateChangeS2CPacket.DEMO_JUMP_HELP) {
                lv4 = Text.translatable("demo.help.jump", lv3.jumpKey.getBoundKeyLocalizedText());
            } else if (f == (float)GameStateChangeS2CPacket.DEMO_INVENTORY_HELP) {
                lv4 = Text.translatable("demo.help.inventory", lv3.inventoryKey.getBoundKeyLocalizedText());
            } else if (f == (float)GameStateChangeS2CPacket.DEMO_EXPIRY_NOTICE) {
                lv4 = Text.translatable("demo.day.6", lv3.screenshotKey.getBoundKeyLocalizedText());
            }
            if (lv4 != null) {
                this.client.inGameHud.getChatHud().addMessage(lv4);
                this.client.getNarratorManager().narrateSystemMessage(lv4);
            }
        } else if (lv2 == GameStateChangeS2CPacket.PROJECTILE_HIT_PLAYER) {
            this.world.playSound((Entity)lv, lv.getX(), lv.getEyeY(), lv.getZ(), SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.18f, 0.45f);
        } else if (lv2 == GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED) {
            this.world.setRainGradient(f);
        } else if (lv2 == GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED) {
            this.world.setThunderGradient(f);
        } else if (lv2 == GameStateChangeS2CPacket.PUFFERFISH_STING) {
            this.world.playSound((Entity)lv, lv.getX(), lv.getY(), lv.getZ(), SoundEvents.ENTITY_PUFFER_FISH_STING, SoundCategory.NEUTRAL, 1.0f, 1.0f);
        } else if (lv2 == GameStateChangeS2CPacket.ELDER_GUARDIAN_EFFECT) {
            this.world.addParticleClient(ParticleTypes.ELDER_GUARDIAN, lv.getX(), lv.getY(), lv.getZ(), 0.0, 0.0, 0.0);
            if (i == 1) {
                this.world.playSound((Entity)lv, lv.getX(), lv.getY(), lv.getZ(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.HOSTILE, 1.0f, 1.0f);
            }
        } else if (lv2 == GameStateChangeS2CPacket.IMMEDIATE_RESPAWN) {
            this.client.player.setShowsDeathScreen(f == (float)GameStateChangeS2CPacket.DEMO_OPEN_SCREEN);
        } else if (lv2 == GameStateChangeS2CPacket.LIMITED_CRAFTING_TOGGLED) {
            this.client.player.setLimitedCraftingEnabled(f == 1.0f);
        } else if (lv2 == GameStateChangeS2CPacket.INITIAL_CHUNKS_COMING && this.chunkLoadProgress != null) {
            this.chunkLoadProgress.initialChunksComing();
        }
    }

    private void startWorldLoading(ClientPlayerEntity player, ClientWorld world, LevelLoadingScreen.WorldEntryReason reason) {
        if (this.chunkLoadProgress == null) {
            this.chunkLoadProgress = new ClientChunkLoadProgress();
        }
        this.chunkLoadProgress.startWorldLoading(player, world, this.client.worldRenderer);
        Screen screen = this.client.currentScreen;
        if (screen instanceof LevelLoadingScreen) {
            LevelLoadingScreen lv = (LevelLoadingScreen)screen;
            lv.init(this.chunkLoadProgress, reason);
        } else {
            this.client.inGameHud.getChatHud().setScreen();
            this.client.setScreenAndRender(new LevelLoadingScreen(this.chunkLoadProgress, reason));
        }
    }

    @Override
    public void onMapUpdate(MapUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        MapIdComponent lv = packet.mapId();
        MapState lv2 = this.client.world.getMapState(lv);
        if (lv2 == null) {
            lv2 = MapState.of(packet.scale(), packet.locked(), this.client.world.getRegistryKey());
            this.client.world.putClientsideMapState(lv, lv2);
        }
        packet.apply(lv2);
        this.client.getMapTextureManager().setNeedsUpdate(lv, lv2);
    }

    @Override
    public void onWorldEvent(WorldEventS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        if (packet.isGlobal()) {
            this.client.world.syncGlobalEvent(packet.getEventId(), packet.getPos(), packet.getData());
        } else {
            this.client.world.syncWorldEvent(packet.getEventId(), packet.getPos(), packet.getData());
        }
    }

    @Override
    public void onAdvancements(AdvancementUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.advancementHandler.onAdvancements(packet);
    }

    @Override
    public void onSelectAdvancementTab(SelectAdvancementTabS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Identifier lv = packet.getTabId();
        if (lv == null) {
            this.advancementHandler.selectTab(null, false);
        } else {
            AdvancementEntry lv2 = this.advancementHandler.get(lv);
            this.advancementHandler.selectTab(lv2, false);
        }
    }

    @Override
    public void onCommandTree(CommandTreeS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.commandDispatcher = new CommandDispatcher<ClientCommandSource>(packet.getCommandTree(CommandRegistryAccess.of(this.combinedDynamicRegistries, this.enabledFeatures), COMMAND_NODE_FACTORY));
    }

    @Override
    public void onStopSound(StopSoundS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.client.getSoundManager().stopSounds(packet.getSoundId(), packet.getCategory());
    }

    @Override
    public void onCommandSuggestions(CommandSuggestionsS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.commandSource.onCommandSuggestions(packet.id(), packet.getSuggestions());
    }

    @Override
    public void onSynchronizeRecipes(SynchronizeRecipesS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.recipeManager = new ClientRecipeManager(packet.itemSets(), packet.stonecutterRecipes());
    }

    @Override
    public void onLookAt(LookAtS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Vec3d lv = packet.getTargetPosition(this.world);
        if (lv != null) {
            this.client.player.lookAt(packet.getSelfAnchor(), lv);
        }
    }

    @Override
    public void onNbtQueryResponse(NbtQueryResponseS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        if (!this.dataQueryHandler.handleQueryResponse(packet.getTransactionId(), packet.getNbt())) {
            LOGGER.debug("Got unhandled response to tag query {}", (Object)packet.getTransactionId());
        }
    }

    @Override
    public void onStatistics(StatisticsS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        for (Object2IntMap.Entry screen : packet.stats().object2IntEntrySet()) {
            Stat lv = (Stat)screen.getKey();
            int i = screen.getIntValue();
            this.client.player.getStatHandler().setStat(this.client.player, lv, i);
        }
        Screen screen = this.client.currentScreen;
        if (screen instanceof StatsScreen) {
            StatsScreen lv2 = (StatsScreen)screen;
            lv2.onStatsReady();
        }
    }

    @Override
    public void onRecipeBookAdd(RecipeBookAddS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        ClientRecipeBook lv = this.client.player.getRecipeBook();
        if (packet.replace()) {
            lv.clear();
        }
        for (RecipeBookAddS2CPacket.Entry lv2 : packet.entries()) {
            lv.add(lv2.contents());
            if (lv2.isHighlighted()) {
                lv.markHighlighted(lv2.contents().id());
            }
            if (!lv2.shouldShowNotification()) continue;
            RecipeToast.show(this.client.getToastManager(), lv2.contents().display());
        }
        this.refreshRecipeBook(lv);
    }

    @Override
    public void onRecipeBookRemove(RecipeBookRemoveS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        ClientRecipeBook lv = this.client.player.getRecipeBook();
        for (NetworkRecipeId lv2 : packet.recipes()) {
            lv.remove(lv2);
        }
        this.refreshRecipeBook(lv);
    }

    @Override
    public void onRecipeBookSettings(RecipeBookSettingsS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        ClientRecipeBook lv = this.client.player.getRecipeBook();
        lv.setOptions(packet.bookSettings());
        this.refreshRecipeBook(lv);
    }

    private void refreshRecipeBook(ClientRecipeBook recipeBook) {
        recipeBook.refresh();
        this.searchManager.addRecipeOutputReloader(recipeBook, this.world);
        Screen screen = this.client.currentScreen;
        if (screen instanceof RecipeBookProvider) {
            RecipeBookProvider lv = (RecipeBookProvider)((Object)screen);
            lv.refreshRecipeBook();
        }
    }

    @Override
    public void onEntityStatusEffect(EntityStatusEffectS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = this.world.getEntityById(packet.getEntityId());
        if (!(lv instanceof LivingEntity)) {
            return;
        }
        RegistryEntry<StatusEffect> lv2 = packet.getEffectId();
        StatusEffectInstance lv3 = new StatusEffectInstance(lv2, packet.getDuration(), packet.getAmplifier(), packet.isAmbient(), packet.shouldShowParticles(), packet.shouldShowIcon(), null);
        if (!packet.keepFading()) {
            lv3.skipFading();
        }
        ((LivingEntity)lv).setStatusEffect(lv3, null);
    }

    private <T> Registry.PendingTagLoad<T> startTagReload(RegistryKey<? extends Registry<? extends T>> registryRef, TagPacketSerializer.Serialized serialized) {
        RegistryWrapper.Impl lv = this.combinedDynamicRegistries.getOrThrow((RegistryKey)registryRef);
        return lv.startTagReload(serialized.toRegistryTags(lv));
    }

    @Override
    public void onSynchronizeTags(SynchronizeTagsS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        ArrayList list = new ArrayList(packet.getGroups().size());
        boolean bl = this.connection.isLocal();
        packet.getGroups().forEach((registryRef, serialized) -> {
            if (!bl || SerializableRegistries.isSynced(registryRef)) {
                list.add(this.startTagReload((RegistryKey)registryRef, (TagPacketSerializer.Serialized)serialized));
            }
        });
        list.forEach(Registry.PendingTagLoad::apply);
        this.fuelRegistry = FuelRegistry.createDefault(this.combinedDynamicRegistries, this.enabledFeatures);
        List<ItemStack> list2 = List.copyOf(ItemGroups.getSearchGroup().getDisplayStacks());
        this.searchManager.addItemTagReloader(list2);
    }

    @Override
    public void onEndCombat(EndCombatS2CPacket packet) {
    }

    @Override
    public void onEnterCombat(EnterCombatS2CPacket packet) {
    }

    @Override
    public void onDeathMessage(DeathMessageS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = this.world.getEntityById(packet.playerId());
        if (lv == this.client.player) {
            if (this.client.player.showsDeathScreen()) {
                this.client.setScreen(new DeathScreen(packet.message(), this.world.getLevelProperties().isHardcore()));
            } else {
                this.client.player.requestRespawn();
            }
        }
    }

    @Override
    public void onDifficulty(DifficultyS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.worldProperties.setDifficulty(packet.difficulty());
        this.worldProperties.setDifficultyLocked(packet.difficultyLocked());
    }

    @Override
    public void onSetCameraEntity(SetCameraEntityS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = packet.getEntity(this.world);
        if (lv != null) {
            this.client.setCameraEntity(lv);
        }
    }

    @Override
    public void onWorldBorderInitialize(WorldBorderInitializeS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        WorldBorder lv = this.world.getWorldBorder();
        lv.setCenter(packet.getCenterX(), packet.getCenterZ());
        long l = packet.getSizeLerpTime();
        if (l > 0L) {
            lv.interpolateSize(packet.getSize(), packet.getSizeLerpTarget(), l);
        } else {
            lv.setSize(packet.getSizeLerpTarget());
        }
        lv.setMaxRadius(packet.getMaxRadius());
        lv.setWarningBlocks(packet.getWarningBlocks());
        lv.setWarningTime(packet.getWarningTime());
    }

    @Override
    public void onWorldBorderCenterChanged(WorldBorderCenterChangedS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.world.getWorldBorder().setCenter(packet.getCenterX(), packet.getCenterZ());
    }

    @Override
    public void onWorldBorderInterpolateSize(WorldBorderInterpolateSizeS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.world.getWorldBorder().interpolateSize(packet.getSize(), packet.getSizeLerpTarget(), packet.getSizeLerpTime());
    }

    @Override
    public void onWorldBorderSizeChanged(WorldBorderSizeChangedS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.world.getWorldBorder().setSize(packet.getSizeLerpTarget());
    }

    @Override
    public void onWorldBorderWarningBlocksChanged(WorldBorderWarningBlocksChangedS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.world.getWorldBorder().setWarningBlocks(packet.getWarningBlocks());
    }

    @Override
    public void onWorldBorderWarningTimeChanged(WorldBorderWarningTimeChangedS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.world.getWorldBorder().setWarningTime(packet.getWarningTime());
    }

    @Override
    public void onTitleClear(ClearTitleS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.client.inGameHud.clearTitle();
        if (packet.shouldReset()) {
            this.client.inGameHud.setDefaultTitleFade();
        }
    }

    @Override
    public void onServerMetadata(ServerMetadataS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        if (this.serverInfo == null) {
            return;
        }
        this.serverInfo.label = packet.description();
        packet.favicon().map(ServerInfo::validateFavicon).ifPresent(this.serverInfo::setFavicon);
        ServerList.updateServerListEntry(this.serverInfo);
    }

    @Override
    public void onChatSuggestions(ChatSuggestionsS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.commandSource.onChatSuggestions(packet.action(), packet.entries());
    }

    @Override
    public void onOverlayMessage(OverlayMessageS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.client.inGameHud.setOverlayMessage(packet.text(), false);
    }

    @Override
    public void onTitle(TitleS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.client.inGameHud.setTitle(packet.text());
    }

    @Override
    public void onSubtitle(SubtitleS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.client.inGameHud.setSubtitle(packet.text());
    }

    @Override
    public void onTitleFade(TitleFadeS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.client.inGameHud.setTitleTicks(packet.getFadeInTicks(), packet.getStayTicks(), packet.getFadeOutTicks());
    }

    @Override
    public void onPlayerListHeader(PlayerListHeaderS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.client.inGameHud.getPlayerListHud().setHeader(packet.header().getString().isEmpty() ? null : packet.header());
        this.client.inGameHud.getPlayerListHud().setFooter(packet.footer().getString().isEmpty() ? null : packet.footer());
    }

    @Override
    public void onRemoveEntityStatusEffect(RemoveEntityStatusEffectS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity entity = packet.getEntity(this.world);
        if (entity instanceof LivingEntity) {
            LivingEntity lv = (LivingEntity)entity;
            lv.removeStatusEffectInternal(packet.effect());
        }
    }

    @Override
    public void onPlayerRemove(PlayerRemoveS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        for (UUID uUID : packet.profileIds()) {
            this.client.getSocialInteractionsManager().setPlayerOffline(uUID);
            PlayerListEntry lv = this.playerListEntries.remove(uUID);
            if (lv == null) continue;
            this.listedPlayerListEntries.remove(lv);
        }
    }

    @Override
    public void onPlayerList(PlayerListS2CPacket packet) {
        PlayerListEntry lv2;
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        for (PlayerListS2CPacket.Entry lv : packet.getPlayerAdditionEntries()) {
            lv2 = new PlayerListEntry(Objects.requireNonNull(lv.profile()), this.isSecureChatEnforced());
            if (this.playerListEntries.putIfAbsent(lv.profileId(), lv2) != null) continue;
            this.client.getSocialInteractionsManager().setPlayerOnline(lv2);
        }
        for (PlayerListS2CPacket.Entry lv : packet.getEntries()) {
            lv2 = this.playerListEntries.get(lv.profileId());
            if (lv2 == null) {
                LOGGER.warn("Ignoring player info update for unknown player {} ({})", (Object)lv.profileId(), (Object)packet.getActions());
                continue;
            }
            for (PlayerListS2CPacket.Action lv3 : packet.getActions()) {
                this.handlePlayerListAction(lv3, lv, lv2);
            }
        }
    }

    private void handlePlayerListAction(PlayerListS2CPacket.Action action, PlayerListS2CPacket.Entry receivedEntry, PlayerListEntry currentEntry) {
        switch (action) {
            case INITIALIZE_CHAT: {
                this.setPublicSession(receivedEntry, currentEntry);
                break;
            }
            case UPDATE_GAME_MODE: {
                if (currentEntry.getGameMode() != receivedEntry.gameMode() && this.client.player != null && this.client.player.getUuid().equals(receivedEntry.profileId())) {
                    this.client.player.onGameModeChanged(receivedEntry.gameMode());
                }
                currentEntry.setGameMode(receivedEntry.gameMode());
                break;
            }
            case UPDATE_LISTED: {
                if (receivedEntry.listed()) {
                    this.listedPlayerListEntries.add(currentEntry);
                    break;
                }
                this.listedPlayerListEntries.remove(currentEntry);
                break;
            }
            case UPDATE_LATENCY: {
                currentEntry.setLatency(receivedEntry.latency());
                break;
            }
            case UPDATE_DISPLAY_NAME: {
                currentEntry.setDisplayName(receivedEntry.displayName());
                break;
            }
            case UPDATE_HAT: {
                currentEntry.setShowHat(receivedEntry.showHat());
                break;
            }
            case UPDATE_LIST_ORDER: {
                currentEntry.setListOrder(receivedEntry.listOrder());
            }
        }
    }

    private void setPublicSession(PlayerListS2CPacket.Entry receivedEntry, PlayerListEntry currentEntry) {
        GameProfile gameProfile = currentEntry.getProfile();
        SignatureVerifier lv = this.client.getApiServices().serviceSignatureVerifier();
        if (lv == null) {
            LOGGER.warn("Ignoring chat session from {} due to missing Services public key", (Object)gameProfile.name());
            currentEntry.resetSession(this.isSecureChatEnforced());
            return;
        }
        PublicPlayerSession.Serialized lv2 = receivedEntry.chatSession();
        if (lv2 != null) {
            try {
                PublicPlayerSession lv3 = lv2.toSession(gameProfile, lv);
                currentEntry.setSession(lv3);
            } catch (PlayerPublicKey.PublicKeyException lv4) {
                LOGGER.error("Failed to validate profile key for player: '{}'", (Object)gameProfile.name(), (Object)lv4);
                currentEntry.resetSession(this.isSecureChatEnforced());
            }
        } else {
            currentEntry.resetSession(this.isSecureChatEnforced());
        }
    }

    private boolean isSecureChatEnforced() {
        return this.client.getApiServices().providesProfileKeys() && this.secureChatEnforced;
    }

    @Override
    public void onPlayerAbilities(PlayerAbilitiesS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        ClientPlayerEntity lv = this.client.player;
        lv.getAbilities().flying = packet.isFlying();
        lv.getAbilities().creativeMode = packet.isCreativeMode();
        lv.getAbilities().invulnerable = packet.isInvulnerable();
        lv.getAbilities().allowFlying = packet.allowFlying();
        lv.getAbilities().setFlySpeed(packet.getFlySpeed());
        lv.getAbilities().setWalkSpeed(packet.getWalkSpeed());
    }

    @Override
    public void onPlaySound(PlaySoundS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.client.world.playSound((Entity)this.client.player, packet.getX(), packet.getY(), packet.getZ(), packet.getSound(), packet.getCategory(), packet.getVolume(), packet.getPitch(), packet.getSeed());
    }

    @Override
    public void onPlaySoundFromEntity(PlaySoundFromEntityS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = this.world.getEntityById(packet.getEntityId());
        if (lv == null) {
            return;
        }
        this.client.world.playSoundFromEntity(this.client.player, lv, packet.getSound(), packet.getCategory(), packet.getVolume(), packet.getPitch(), packet.getSeed());
    }

    @Override
    public void onBossBar(BossBarS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.client.inGameHud.getBossBarHud().handlePacket(packet);
    }

    @Override
    public void onCooldownUpdate(CooldownUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        if (packet.cooldown() == 0) {
            this.client.player.getItemCooldownManager().remove(packet.cooldownGroup());
        } else {
            this.client.player.getItemCooldownManager().set(packet.cooldownGroup(), packet.cooldown());
        }
    }

    @Override
    public void onVehicleMove(VehicleMoveS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = this.client.player.getRootVehicle();
        if (lv != this.client.player && lv.isLogicalSideForUpdatingMovement()) {
            Vec3d lv3;
            Vec3d lv2 = packet.position();
            if (lv2.distanceTo(lv3 = lv.isInterpolating() ? lv.getInterpolator().getLerpedPos() : lv.getEntityPos()) > (double)1.0E-5f) {
                if (lv.isInterpolating()) {
                    lv.getInterpolator().clear();
                }
                lv.updatePositionAndAngles(lv2.getX(), lv2.getY(), lv2.getZ(), packet.yaw(), packet.pitch());
            }
            this.connection.send(VehicleMoveC2SPacket.fromVehicle(lv));
        }
    }

    @Override
    public void onOpenWrittenBook(OpenWrittenBookS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        ItemStack lv = this.client.player.getStackInHand(packet.getHand());
        BookScreen.Contents lv2 = BookScreen.Contents.create(lv);
        if (lv2 != null) {
            this.client.setScreen(new BookScreen(lv2));
        }
    }

    @Override
    public void onCustomPayload(CustomPayload payload) {
        this.warnOnUnknownPayload(payload);
    }

    private void warnOnUnknownPayload(CustomPayload payload) {
        LOGGER.warn("Unknown custom packet payload: {}", (Object)payload.getId().id());
    }

    @Override
    public void onScoreboardObjectiveUpdate(ScoreboardObjectiveUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        String string = packet.getName();
        if (packet.getMode() == 0) {
            this.scoreboard.addObjective(string, ScoreboardCriterion.DUMMY, packet.getDisplayName(), packet.getType(), false, packet.getNumberFormat().orElse(null));
        } else {
            ScoreboardObjective lv = this.scoreboard.getNullableObjective(string);
            if (lv != null) {
                if (packet.getMode() == ScoreboardObjectiveUpdateS2CPacket.REMOVE_MODE) {
                    this.scoreboard.removeObjective(lv);
                } else if (packet.getMode() == ScoreboardObjectiveUpdateS2CPacket.UPDATE_MODE) {
                    lv.setRenderType(packet.getType());
                    lv.setDisplayName(packet.getDisplayName());
                    lv.setNumberFormat(packet.getNumberFormat().orElse(null));
                }
            }
        }
    }

    @Override
    public void onScoreboardScoreUpdate(ScoreboardScoreUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        String string = packet.objectiveName();
        ScoreHolder lv = ScoreHolder.fromName(packet.scoreHolderName());
        ScoreboardObjective lv2 = this.scoreboard.getNullableObjective(string);
        if (lv2 != null) {
            ScoreAccess lv3 = this.scoreboard.getOrCreateScore(lv, lv2, true);
            lv3.setScore(packet.score());
            lv3.setDisplayText(packet.display().orElse(null));
            lv3.setNumberFormat(packet.numberFormat().orElse(null));
        } else {
            LOGGER.warn("Received packet for unknown scoreboard objective: {}", (Object)string);
        }
    }

    @Override
    public void onScoreboardScoreReset(ScoreboardScoreResetS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        String string = packet.objectiveName();
        ScoreHolder lv = ScoreHolder.fromName(packet.scoreHolderName());
        if (string == null) {
            this.scoreboard.removeScores(lv);
        } else {
            ScoreboardObjective lv2 = this.scoreboard.getNullableObjective(string);
            if (lv2 != null) {
                this.scoreboard.removeScore(lv, lv2);
            } else {
                LOGGER.warn("Received packet for unknown scoreboard objective: {}", (Object)string);
            }
        }
    }

    @Override
    public void onScoreboardDisplay(ScoreboardDisplayS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        String string = packet.getName();
        ScoreboardObjective lv = string == null ? null : this.scoreboard.getNullableObjective(string);
        this.scoreboard.setObjectiveSlot(packet.getSlot(), lv);
    }

    @Override
    public void onTeam(TeamS2CPacket packet) {
        Team lv2;
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        TeamS2CPacket.Operation lv = packet.getTeamOperation();
        if (lv == TeamS2CPacket.Operation.ADD) {
            lv2 = this.scoreboard.addTeam(packet.getTeamName());
        } else {
            lv2 = this.scoreboard.getTeam(packet.getTeamName());
            if (lv2 == null) {
                LOGGER.warn("Received packet for unknown team {}: team action: {}, player action: {}", new Object[]{packet.getTeamName(), packet.getTeamOperation(), packet.getPlayerListOperation()});
                return;
            }
        }
        Optional<TeamS2CPacket.SerializableTeam> optional = packet.getTeam();
        optional.ifPresent(team -> {
            lv2.setDisplayName(team.getDisplayName());
            lv2.setColor(team.getColor());
            lv2.setFriendlyFlagsBitwise(team.getFriendlyFlagsBitwise());
            lv2.setNameTagVisibilityRule(team.getNameTagVisibilityRule());
            lv2.setCollisionRule(team.getCollisionRule());
            lv2.setPrefix(team.getPrefix());
            lv2.setSuffix(team.getSuffix());
        });
        TeamS2CPacket.Operation lv3 = packet.getPlayerListOperation();
        if (lv3 == TeamS2CPacket.Operation.ADD) {
            for (String string : packet.getPlayerNames()) {
                this.scoreboard.addScoreHolderToTeam(string, lv2);
            }
        } else if (lv3 == TeamS2CPacket.Operation.REMOVE) {
            for (String string : packet.getPlayerNames()) {
                this.scoreboard.removeScoreHolderFromTeam(string, lv2);
            }
        }
        if (lv == TeamS2CPacket.Operation.REMOVE) {
            this.scoreboard.removeTeam(lv2);
        }
    }

    @Override
    public void onParticle(ParticleS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        if (packet.getCount() == 0) {
            double d = packet.getSpeed() * packet.getOffsetX();
            double e = packet.getSpeed() * packet.getOffsetY();
            double f = packet.getSpeed() * packet.getOffsetZ();
            try {
                this.world.addParticleClient(packet.getParameters(), packet.shouldForceSpawn(), packet.isImportant(), packet.getX(), packet.getY(), packet.getZ(), d, e, f);
            } catch (Throwable throwable) {
                LOGGER.warn("Could not spawn particle effect {}", (Object)packet.getParameters());
            }
        } else {
            for (int i = 0; i < packet.getCount(); ++i) {
                double g = this.random.nextGaussian() * (double)packet.getOffsetX();
                double h = this.random.nextGaussian() * (double)packet.getOffsetY();
                double j = this.random.nextGaussian() * (double)packet.getOffsetZ();
                double k = this.random.nextGaussian() * (double)packet.getSpeed();
                double l = this.random.nextGaussian() * (double)packet.getSpeed();
                double m = this.random.nextGaussian() * (double)packet.getSpeed();
                try {
                    this.world.addParticleClient(packet.getParameters(), packet.shouldForceSpawn(), packet.isImportant(), packet.getX() + g, packet.getY() + h, packet.getZ() + j, k, l, m);
                    continue;
                } catch (Throwable throwable2) {
                    LOGGER.warn("Could not spawn particle effect {}", (Object)packet.getParameters());
                    return;
                }
            }
        }
    }

    @Override
    public void onEntityAttributes(EntityAttributesS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = this.world.getEntityById(packet.getEntityId());
        if (lv == null) {
            return;
        }
        if (!(lv instanceof LivingEntity)) {
            throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + String.valueOf(lv) + ")");
        }
        AttributeContainer lv2 = ((LivingEntity)lv).getAttributes();
        for (EntityAttributesS2CPacket.Entry lv3 : packet.getEntries()) {
            EntityAttributeInstance lv4 = lv2.getCustomInstance(lv3.attribute());
            if (lv4 == null) {
                LOGGER.warn("Entity {} does not have attribute {}", (Object)lv, (Object)lv3.attribute().getIdAsString());
                continue;
            }
            lv4.setBaseValue(lv3.base());
            lv4.clearModifiers();
            for (EntityAttributeModifier lv5 : lv3.modifiers()) {
                lv4.addTemporaryModifier(lv5);
            }
        }
    }

    @Override
    public void onCraftFailedResponse(CraftFailedResponseS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        ScreenHandler lv = this.client.player.currentScreenHandler;
        if (lv.syncId != packet.syncId()) {
            return;
        }
        Screen screen = this.client.currentScreen;
        if (screen instanceof RecipeBookProvider) {
            RecipeBookProvider lv2 = (RecipeBookProvider)((Object)screen);
            lv2.onCraftFailed(packet.recipeDisplay());
        }
    }

    @Override
    public void onLightUpdate(LightUpdateS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        int i = packet.getChunkX();
        int j = packet.getChunkZ();
        LightData lv = packet.getData();
        this.world.enqueueChunkUpdate(() -> this.readLightData(i, j, lv, true));
    }

    private void readLightData(int x, int z, LightData data, boolean scheduleBlockRenders) {
        LightingProvider lv = this.world.getChunkManager().getLightingProvider();
        BitSet bitSet = data.getInitedSky();
        BitSet bitSet2 = data.getUninitedSky();
        Iterator<byte[]> iterator = data.getSkyNibbles().iterator();
        this.updateLighting(x, z, lv, LightType.SKY, bitSet, bitSet2, iterator, scheduleBlockRenders);
        BitSet bitSet3 = data.getInitedBlock();
        BitSet bitSet4 = data.getUninitedBlock();
        Iterator<byte[]> iterator2 = data.getBlockNibbles().iterator();
        this.updateLighting(x, z, lv, LightType.BLOCK, bitSet3, bitSet4, iterator2, scheduleBlockRenders);
        lv.setColumnEnabled(new ChunkPos(x, z), true);
    }

    @Override
    public void onSetTradeOffers(SetTradeOffersS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        ScreenHandler lv = this.client.player.currentScreenHandler;
        if (packet.getSyncId() == lv.syncId && lv instanceof MerchantScreenHandler) {
            MerchantScreenHandler lv2 = (MerchantScreenHandler)lv;
            lv2.setOffers(packet.getOffers());
            lv2.setExperienceFromServer(packet.getExperience());
            lv2.setLevelProgress(packet.getLevelProgress());
            lv2.setLeveled(packet.isLeveled());
            lv2.setCanRefreshTrades(packet.isRefreshable());
        }
    }

    @Override
    public void onChunkLoadDistance(ChunkLoadDistanceS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.chunkLoadDistance = packet.getDistance();
        this.client.options.setServerViewDistance(this.chunkLoadDistance);
        this.world.getChunkManager().updateLoadDistance(packet.getDistance());
    }

    @Override
    public void onSimulationDistance(SimulationDistanceS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.simulationDistance = packet.simulationDistance();
        this.world.setSimulationDistance(this.simulationDistance);
    }

    @Override
    public void onChunkRenderDistanceCenter(ChunkRenderDistanceCenterS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.world.getChunkManager().setChunkMapCenter(packet.getChunkX(), packet.getChunkZ());
    }

    @Override
    public void onPlayerActionResponse(PlayerActionResponseS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.world.handlePlayerActionResponse(packet.sequence());
    }

    @Override
    public void onBundle(BundleS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        for (Packet<ClientPlayNetworkHandler> packet2 : packet.getPackets()) {
            packet2.apply(this);
        }
    }

    @Override
    public void onProjectilePower(ProjectilePowerS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = this.world.getEntityById(packet.getEntityId());
        if (lv instanceof ExplosiveProjectileEntity) {
            ExplosiveProjectileEntity lv2 = (ExplosiveProjectileEntity)lv;
            lv2.accelerationPower = packet.getAccelerationPower();
        }
    }

    @Override
    public void onStartChunkSend(StartChunkSendS2CPacket packet) {
        this.chunkBatchSizeCalculator.onStartChunkSend();
    }

    @Override
    public void onChunkSent(ChunkSentS2CPacket packet) {
        this.chunkBatchSizeCalculator.onChunkSent(packet.batchSize());
        this.sendPacket(new AcknowledgeChunksC2SPacket(this.chunkBatchSizeCalculator.getDesiredChunksPerTick()));
    }

    @Override
    public void onDebugSample(DebugSampleS2CPacket packet) {
        this.client.getDebugHud().set(packet.sample(), packet.debugSampleType());
    }

    @Override
    public void onPingResult(PingResultS2CPacket packet) {
        this.pingMeasurer.onPingResult(packet);
    }

    @Override
    public void onTestInstanceBlockStatus(TestInstanceBlockStatusS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Screen screen = this.client.currentScreen;
        if (screen instanceof TestInstanceBlockScreen) {
            TestInstanceBlockScreen lv = (TestInstanceBlockScreen)screen;
            lv.handleStatus(packet.status(), packet.size());
        }
    }

    @Override
    public void onWaypoint(WaypointS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        packet.apply(this.waypointHandler);
    }

    @Override
    public void onChunkValueDebug(ChunkValueDebugS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.debugSubscriptionManager.updateChunk(this.world.getTime(), packet.chunkPos(), packet.update());
    }

    @Override
    public void onBlockValueDebug(BlockValueDebugS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.debugSubscriptionManager.updateBlock(this.world.getTime(), packet.blockPos(), packet.update());
    }

    @Override
    public void onEntityValueDebug(EntityValueDebugS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        Entity lv = this.world.getEntityById(packet.entityId());
        if (lv != null) {
            this.debugSubscriptionManager.updateEntity(this.world.getTime(), lv, packet.update());
        }
    }

    @Override
    public void onEventDebug(EventDebugS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.debugSubscriptionManager.addEvent(this.world.getTime(), packet.event());
    }

    @Override
    public void onGameTestHighlightPos(GameTestHighlightPosS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client.getPacketApplyBatcher());
        this.client.worldRenderer.gameTestDebugRenderer.addMarker(packet.absolutePos(), packet.relativePos());
    }

    private void updateLighting(int chunkX, int chunkZ, LightingProvider provider, LightType type, BitSet inited, BitSet uninited, Iterator<byte[]> nibbles, boolean scheduleBlockRenders) {
        for (int k = 0; k < provider.getHeight(); ++k) {
            int l = provider.getBottomY() + k;
            boolean bl2 = inited.get(k);
            boolean bl3 = uninited.get(k);
            if (!bl2 && !bl3) continue;
            provider.enqueueSectionData(type, ChunkSectionPos.from(chunkX, l, chunkZ), bl2 ? new ChunkNibbleArray((byte[])nibbles.next().clone()) : new ChunkNibbleArray());
            if (!scheduleBlockRenders) continue;
            this.world.scheduleBlockRenders(chunkX, l, chunkZ);
        }
    }

    public ClientConnection getConnection() {
        return this.connection;
    }

    @Override
    public boolean isConnectionOpen() {
        return this.connection.isOpen() && !this.worldCleared;
    }

    public Collection<PlayerListEntry> getListedPlayerListEntries() {
        return this.listedPlayerListEntries;
    }

    public Collection<PlayerListEntry> getPlayerList() {
        return this.playerListEntries.values();
    }

    public Collection<UUID> getPlayerUuids() {
        return this.playerListEntries.keySet();
    }

    @Nullable
    public PlayerListEntry getPlayerListEntry(UUID uuid) {
        return this.playerListEntries.get(uuid);
    }

    @Nullable
    public PlayerListEntry getPlayerListEntry(String profileName) {
        for (PlayerListEntry lv : this.playerListEntries.values()) {
            if (!lv.getProfile().name().equals(profileName)) continue;
            return lv;
        }
        return null;
    }

    public Map<UUID, PlayerListEntry> getSeenPlayers() {
        return this.seenPlayers;
    }

    @Nullable
    public PlayerListEntry getCaseInsensitivePlayerInfo(String name) {
        for (PlayerListEntry lv : this.playerListEntries.values()) {
            if (!lv.getProfile().name().equalsIgnoreCase(name)) continue;
            return lv;
        }
        return null;
    }

    public GameProfile getProfile() {
        return this.profile;
    }

    public ClientAdvancementManager getAdvancementHandler() {
        return this.advancementHandler;
    }

    public CommandDispatcher<ClientCommandSource> getCommandDispatcher() {
        return this.commandDispatcher;
    }

    public ClientWorld getWorld() {
        return this.world;
    }

    public DataQueryHandler getDataQueryHandler() {
        return this.dataQueryHandler;
    }

    public UUID getSessionId() {
        return this.sessionId;
    }

    public Set<RegistryKey<World>> getWorldKeys() {
        return this.worldKeys;
    }

    public DynamicRegistryManager.Immutable getRegistryManager() {
        return this.combinedDynamicRegistries;
    }

    public void acknowledge(MessageSignatureData signature, boolean displayed) {
        if (this.lastSeenMessagesCollector.add(signature, displayed) && this.lastSeenMessagesCollector.getMessageCount() > 64) {
            this.sendAcknowledgment();
        }
    }

    private void sendAcknowledgment() {
        int i = this.lastSeenMessagesCollector.resetMessageCount();
        if (i > 0) {
            this.sendPacket(new MessageAcknowledgmentC2SPacket(i));
        }
    }

    public void sendChatMessage(String content) {
        Instant instant = Instant.now();
        long l = NetworkEncryptionUtils.SecureRandomUtil.nextLong();
        LastSeenMessagesCollector.LastSeenMessages lv = this.lastSeenMessagesCollector.collect();
        MessageSignatureData lv2 = this.messagePacker.pack(new MessageBody(content, instant, l, lv.lastSeen()));
        this.sendPacket(new ChatMessageC2SPacket(content, instant, l, lv2, lv.update()));
    }

    public void sendChatCommand(String command) {
        SignedArgumentList<ClientCommandSource> lv = SignedArgumentList.of(this.commandDispatcher.parse(command, this.commandSource));
        if (lv.arguments().isEmpty()) {
            this.sendPacket(new CommandExecutionC2SPacket(command));
            return;
        }
        Instant instant = Instant.now();
        long l = NetworkEncryptionUtils.SecureRandomUtil.nextLong();
        LastSeenMessagesCollector.LastSeenMessages lv2 = this.lastSeenMessagesCollector.collect();
        ArgumentSignatureDataMap lv3 = ArgumentSignatureDataMap.sign(lv, value -> {
            MessageBody lv = new MessageBody(value, instant, l, lv2.lastSeen());
            return this.messagePacker.pack(lv);
        });
        this.sendPacket(new ChatCommandSignedC2SPacket(command, instant, l, lv3, lv2.update()));
    }

    public void runClickEventCommand(String command, @Nullable Screen afterActionScreen) {
        switch (this.parseCommand(command).ordinal()) {
            case 0: {
                this.sendPacket(new CommandExecutionC2SPacket(command));
                this.client.setScreen(afterActionScreen);
                break;
            }
            case 1: {
                this.openConfirmRunCommandScreen(command, "multiplayer.confirm_command.parse_errors", afterActionScreen);
                break;
            }
            case 3: {
                this.openConfirmRunCommandScreen(command, "multiplayer.confirm_command.permissions_required", afterActionScreen);
                break;
            }
            case 2: {
                this.suggestCommand(command, "multiplayer.confirm_command.signature_required", afterActionScreen);
            }
        }
    }

    private CommandRunResult parseCommand(String command) {
        ParseResults<ClientCommandSource> parseResults = this.commandDispatcher.parse(command, this.commandSource);
        if (!ClientPlayNetworkHandler.validate(parseResults)) {
            return CommandRunResult.PARSE_ERRORS;
        }
        if (SignedArgumentList.isNotEmpty(parseResults)) {
            return CommandRunResult.SIGNATURE_REQUIRED;
        }
        ParseResults<ClientCommandSource> parseResults2 = this.commandDispatcher.parse(command, this.restrictedCommandSource);
        if (!ClientPlayNetworkHandler.validate(parseResults2)) {
            return CommandRunResult.PERMISSIONS_REQUIRED;
        }
        return CommandRunResult.NO_ISSUES;
    }

    private static boolean validate(ParseResults<?> parseResults) {
        return !parseResults.getReader().canRead() && parseResults.getExceptions().isEmpty() && parseResults.getContext().getLastChild().getCommand() != null;
    }

    private void openConfirmCommandScreen(String command, String message, Text yesText, Runnable action) {
        Screen lv = this.client.currentScreen;
        this.client.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                action.run();
            } else {
                this.client.setScreen(lv);
            }
        }, CONFIRM_COMMAND_TITLE_TEXT, Text.translatable(message, Text.literal(command).formatted(Formatting.YELLOW)), yesText, lv != null ? ScreenTexts.BACK : ScreenTexts.CANCEL));
    }

    private void openConfirmRunCommandScreen(String command, String message, @Nullable Screen screenAfterRun) {
        this.openConfirmCommandScreen(command, message, CONFIRM_RUN_COMMAND_TEXT, () -> {
            this.sendPacket(new CommandExecutionC2SPacket(command));
            this.client.setScreen(screenAfterRun);
        });
    }

    private void suggestCommand(String command, String message, @Nullable Screen afterActionScreen) {
        boolean bl = afterActionScreen == null && this.client.getChatRestriction().allowsChat(this.client.isInSingleplayer());
        this.openConfirmCommandScreen(command, message, bl ? CONFIRM_SUGGEST_COMMAND_TEXT : ScreenTexts.COPY, () -> {
            if (bl) {
                this.client.openChatScreen(ChatHud.ChatMethod.COMMAND);
                Screen lv = this.client.currentScreen;
                if (lv instanceof ChatScreen) {
                    ChatScreen lv2 = (ChatScreen)lv;
                    lv2.insertText(command, false);
                }
            } else {
                this.client.keyboard.setClipboard("/" + command);
                this.client.setScreen(afterActionScreen);
            }
        });
    }

    public void syncOptions(SyncedClientOptions syncedOptions) {
        if (!syncedOptions.equals(this.syncedOptions)) {
            this.sendPacket(new ClientOptionsC2SPacket(syncedOptions));
            this.syncedOptions = syncedOptions;
        }
    }

    @Override
    public void tick() {
        if (this.session != null && this.client.getProfileKeys().isExpired()) {
            this.fetchProfileKey();
        }
        if (this.profileKeyPairFuture != null && this.profileKeyPairFuture.isDone()) {
            this.profileKeyPairFuture.join().ifPresent(this::updateKeyPair);
            this.profileKeyPairFuture = null;
        }
        this.sendQueuedPackets();
        if (this.client.getDebugHud().shouldShowPacketSizeAndPingCharts()) {
            this.pingMeasurer.ping();
        }
        if (this.world != null) {
            this.debugSubscriptionManager.startTick(this.world.getTime());
        }
        this.worldSession.tick();
        if (this.chunkLoadProgress != null) {
            this.chunkLoadProgress.tick();
            if (this.chunkLoadProgress.isDone()) {
                this.setPlayerLoaded();
                this.chunkLoadProgress = null;
            }
        }
    }

    private void setPlayerLoaded() {
        if (!this.client.player.isLoaded()) {
            this.connection.send(new PlayerLoadedC2SPacket());
            this.client.player.setLoaded(true);
        }
    }

    public void fetchProfileKey() {
        this.profileKeyPairFuture = this.client.getProfileKeys().fetchKeyPair();
    }

    private void updateKeyPair(PlayerKeyPair keyPair) {
        if (!this.client.uuidEquals(this.profile.id())) {
            return;
        }
        if (this.session != null && this.session.keyPair().equals(keyPair)) {
            return;
        }
        this.session = ClientPlayerSession.create(keyPair);
        this.messagePacker = this.session.createPacker(this.profile.id());
        this.sendPacket(new PlayerSessionC2SPacket(this.session.toPublicSession().toSerialized()));
    }

    @Override
    protected DialogNetworkAccess createDialogNetworkAccess() {
        return new ClientCommonNetworkHandler.CommonDialogNetworkAccess(){

            @Override
            public void runClickEventCommand(String command, @Nullable Screen afterActionScreen) {
                ClientPlayNetworkHandler.this.runClickEventCommand(command, afterActionScreen);
            }
        };
    }

    @Nullable
    public ServerInfo getServerInfo() {
        return this.serverInfo;
    }

    public FeatureSet getEnabledFeatures() {
        return this.enabledFeatures;
    }

    public boolean hasFeature(FeatureSet feature) {
        return feature.isSubsetOf(this.getEnabledFeatures());
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public BrewingRecipeRegistry getBrewingRecipeRegistry() {
        return this.brewingRecipeRegistry;
    }

    public FuelRegistry getFuelRegistry() {
        return this.fuelRegistry;
    }

    public void refreshSearchManager() {
        this.searchManager.refresh();
    }

    public SearchManager getSearchManager() {
        return this.searchManager;
    }

    public void registerForCleaning(DataCache<?, ?> dataCache) {
        this.cachedData.add(new WeakReference(dataCache));
    }

    public ComponentChangesHash.ComponentHasher getComponentHasher() {
        return this.componentHasher;
    }

    public ClientWaypointHandler getWaypointHandler() {
        return this.waypointHandler;
    }

    public DebugDataStore getDebugDataStore() {
        return this.debugSubscriptionManager.createDebugDataStore(this.world);
    }

    @Environment(value=EnvType.CLIENT)
    static enum CommandRunResult {
        NO_ISSUES,
        PARSE_ERRORS,
        SIGNATURE_REQUIRED,
        PERMISSIONS_REQUIRED;

    }
}

