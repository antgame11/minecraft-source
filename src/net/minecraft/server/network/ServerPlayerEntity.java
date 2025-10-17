/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.network;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.HashCode;
import com.google.common.net.InetAddresses;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.SculkShriekerWarningManager;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.component.Component;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPosition;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.s2c.common.ShowDialogS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.CommonPlayerSpawnInfo;
import net.minecraft.network.packet.s2c.play.DamageTiltS2CPacket;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.EndCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.EnterCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.LookAtS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenHorseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenWrittenBookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRotationS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ServerMetadataS2CPacket;
import net.minecraft.network.packet.s2c.play.SetCameraEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.SetCursorItemS2CPacket;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerSyncHandler;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.sync.TrackedSlot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ChunkFilter;
import net.minecraft.server.network.ServerItemCooldownManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.network.ServerRecipeBook;
import net.minecraft.server.network.ServerWaypointHandler;
import net.minecraft.server.network.SpawnLocating;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.dynamic.HashCodeOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.CollisionView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.debug.DebugSubscriptionType;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ServerPlayerEntity
extends PlayerEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int field_29769 = 32;
    private static final int field_29770 = 10;
    private static final int field_46928 = 25;
    public static final double field_54046 = 1.0;
    public static final double field_54047 = 3.0;
    public static final int field_54207 = 2;
    public static final String ENDER_PEARLS_KEY = "ender_pearls";
    public static final String ENDER_PEARLS_DIMENSION_KEY = "ender_pearl_dimension";
    public static final String DIMENSION_KEY = "Dimension";
    private static final EntityAttributeModifier CREATIVE_BLOCK_INTERACTION_RANGE_MODIFIER = new EntityAttributeModifier(Identifier.ofVanilla("creative_mode_block_range"), 0.5, EntityAttributeModifier.Operation.ADD_VALUE);
    private static final EntityAttributeModifier CREATIVE_ENTITY_INTERACTION_RANGE_MODIFIER = new EntityAttributeModifier(Identifier.ofVanilla("creative_mode_entity_range"), 2.0, EntityAttributeModifier.Operation.ADD_VALUE);
    private static final Text SET_SPAWN_TEXT = Text.translatable("block.minecraft.set_spawn");
    private static final EntityAttributeModifier WAYPOINT_TRANSMIT_RANGE_CROUCH_MODIFIER = new EntityAttributeModifier(Identifier.ofVanilla("waypoint_transmit_range_crouch"), -1.0, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    private static final boolean DEFAULT_SEEN_CREDITS = false;
    private static final boolean DEFAULT_SPAWN_EXTRA_PARTICLES_ON_FALL = false;
    public ServerPlayNetworkHandler networkHandler;
    private final MinecraftServer server;
    public final ServerPlayerInteractionManager interactionManager;
    private final PlayerAdvancementTracker advancementTracker;
    private final ServerStatHandler statHandler;
    private float lastHealthScore = Float.MIN_VALUE;
    private int lastFoodScore = Integer.MIN_VALUE;
    private int lastAirScore = Integer.MIN_VALUE;
    private int lastArmorScore = Integer.MIN_VALUE;
    private int lastLevelScore = Integer.MIN_VALUE;
    private int lastExperienceScore = Integer.MIN_VALUE;
    private float syncedHealth = -1.0E8f;
    private int syncedFoodLevel = -99999999;
    private boolean syncedSaturationIsZero = true;
    private int syncedExperience = -99999999;
    private ChatVisibility clientChatVisibility = ChatVisibility.FULL;
    private ParticlesMode particlesMode = ParticlesMode.ALL;
    private boolean clientChatColorsEnabled = true;
    private long lastActionTime = Util.getMeasuringTimeMs();
    @Nullable
    private Entity cameraEntity;
    private boolean inTeleportationState;
    public boolean seenCredits = false;
    private final ServerRecipeBook recipeBook;
    @Nullable
    private Vec3d levitationStartPos;
    private int levitationStartTick;
    private boolean disconnected;
    private int viewDistance = 2;
    private String language = "en_us";
    @Nullable
    private Vec3d fallStartPos;
    @Nullable
    private Vec3d enteredNetherPos;
    @Nullable
    private Vec3d vehicleInLavaRidingPos;
    private ChunkSectionPos watchedSection = ChunkSectionPos.from(0, 0, 0);
    private ChunkFilter chunkFilter = ChunkFilter.IGNORE_ALL;
    @Nullable
    private Respawn respawn;
    private final TextStream textStream;
    private boolean filterText;
    private boolean allowServerListing;
    private boolean spawnExtraParticlesOnFall = false;
    private SculkShriekerWarningManager sculkShriekerWarningManager = new SculkShriekerWarningManager();
    @Nullable
    private BlockPos startRaidPos;
    private Vec3d movement = Vec3d.ZERO;
    private PlayerInput playerInput = PlayerInput.DEFAULT;
    private final Set<EnderPearlEntity> enderPearls = new HashSet<EnderPearlEntity>();
    private long shoulderMountTime;
    private NbtCompound leftShoulderNbt = new NbtCompound();
    private NbtCompound rightShoulderNbt = new NbtCompound();
    private final ScreenHandlerSyncHandler screenHandlerSyncHandler = new ScreenHandlerSyncHandler(){
        private final LoadingCache<Component<?>, Integer> componentHashCache = CacheBuilder.newBuilder().maximumSize(256L).build(new CacheLoader<Component<?>, Integer>(){
            private final DynamicOps<HashCode> hashOps;
            {
                this.hashOps = ServerPlayerEntity.this.getRegistryManager().getOps(HashCodeOps.INSTANCE);
            }

            @Override
            public Integer load(Component<?> arg) {
                return arg.encode(this.hashOps).getOrThrow(error -> new IllegalArgumentException("Failed to hash " + String.valueOf(arg) + ": " + error)).asInt();
            }

            @Override
            public /* synthetic */ Object load(Object component) throws Exception {
                return this.load((Component)component);
            }
        });

        @Override
        public void updateState(ScreenHandler handler, List<ItemStack> stacks, ItemStack cursorStack, int[] properties) {
            ServerPlayerEntity.this.networkHandler.sendPacket(new InventoryS2CPacket(handler.syncId, handler.nextRevision(), stacks, cursorStack));
            for (int i = 0; i < properties.length; ++i) {
                this.sendPropertyUpdate(handler, i, properties[i]);
            }
        }

        @Override
        public void updateSlot(ScreenHandler handler, int slot, ItemStack stack) {
            ServerPlayerEntity.this.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), slot, stack));
        }

        @Override
        public void updateCursorStack(ScreenHandler handler, ItemStack stack) {
            ServerPlayerEntity.this.networkHandler.sendPacket(new SetCursorItemS2CPacket(stack));
        }

        @Override
        public void updateProperty(ScreenHandler handler, int property, int value) {
            this.sendPropertyUpdate(handler, property, value);
        }

        private void sendPropertyUpdate(ScreenHandler handler, int property, int value) {
            ServerPlayerEntity.this.networkHandler.sendPacket(new ScreenHandlerPropertyUpdateS2CPacket(handler.syncId, property, value));
        }

        @Override
        public TrackedSlot createTrackedSlot() {
            return new TrackedSlot.Impl(this.componentHashCache::getUnchecked);
        }
    };
    private final ScreenHandlerListener screenHandlerListener = new ScreenHandlerListener(){

        @Override
        public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
            Slot lv = handler.getSlot(slotId);
            if (lv instanceof CraftingResultSlot) {
                return;
            }
            if (lv.inventory == ServerPlayerEntity.this.getInventory()) {
                Criteria.INVENTORY_CHANGED.trigger(ServerPlayerEntity.this, ServerPlayerEntity.this.getInventory(), stack);
            }
        }

        @Override
        public void onPropertyUpdate(ScreenHandler handler, int property, int value) {
        }
    };
    @Nullable
    private PublicPlayerSession session;
    @Nullable
    public final Object field_49777;
    private final CommandOutput commandOutput = new CommandOutput(){

        @Override
        public boolean shouldReceiveFeedback() {
            return ServerPlayerEntity.this.getEntityWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK);
        }

        @Override
        public boolean shouldTrackOutput() {
            return true;
        }

        @Override
        public boolean shouldBroadcastConsoleToOps() {
            return true;
        }

        @Override
        public void sendMessage(Text message) {
            ServerPlayerEntity.this.sendMessage(message);
        }
    };
    private Set<DebugSubscriptionType<?>> subscribedTypes = Set.of();
    private int screenHandlerSyncId;
    public boolean notInAnyWorld;

    public ServerPlayerEntity(MinecraftServer server, ServerWorld world, GameProfile profile, SyncedClientOptions clientOptions) {
        super(world, profile);
        this.server = server;
        this.textStream = server.createFilterer(this);
        this.interactionManager = server.getPlayerInteractionManager(this);
        this.interactionManager.setGameMode(this.getServerGameMode(null), null);
        this.recipeBook = new ServerRecipeBook((key, adder) -> server.getRecipeManager().forEachRecipeDisplay(key, adder));
        this.statHandler = server.getPlayerManager().createStatHandler(this);
        this.advancementTracker = server.getPlayerManager().getAdvancementTracker(this);
        this.setClientOptions(clientOptions);
        this.field_49777 = null;
    }

    @Override
    public BlockPos getWorldSpawnPos(ServerWorld world, BlockPos basePos) {
        CompletableFuture<Vec3d> completableFuture = SpawnLocating.locateSpawnPos(world, basePos);
        this.server.runTasks(completableFuture::isDone);
        return BlockPos.ofFloored(completableFuture.join());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.sculkShriekerWarningManager = view.read("warden_spawn_tracker", SculkShriekerWarningManager.CODEC).orElseGet(SculkShriekerWarningManager::new);
        this.enteredNetherPos = view.read("entered_nether_pos", Vec3d.CODEC).orElse(null);
        this.seenCredits = view.getBoolean("seenCredits", false);
        view.read("recipeBook", ServerRecipeBook.Packed.CODEC).ifPresent(recipeBook -> this.recipeBook.unpack((ServerRecipeBook.Packed)recipeBook, recipeKey -> this.server.getRecipeManager().get((RegistryKey<Recipe<?>>)recipeKey).isPresent()));
        if (this.isSleeping()) {
            this.wakeUp();
        }
        this.respawn = view.read("respawn", Respawn.CODEC).orElse(null);
        this.spawnExtraParticlesOnFall = view.getBoolean("spawn_extra_particles_on_fall", false);
        this.startRaidPos = view.read("raid_omen_position", BlockPos.CODEC).orElse(null);
        this.interactionManager.setGameMode(this.getServerGameMode(ServerPlayerEntity.gameModeFromData(view, "playerGameType")), ServerPlayerEntity.gameModeFromData(view, "previousPlayerGameType"));
        this.setLeftShoulderNbt(view.read("ShoulderEntityLeft", NbtCompound.CODEC).orElseGet(NbtCompound::new));
        this.setRightShoulderNbt(view.read("ShoulderEntityRight", NbtCompound.CODEC).orElseGet(NbtCompound::new));
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.put("warden_spawn_tracker", SculkShriekerWarningManager.CODEC, this.sculkShriekerWarningManager);
        this.writeGameModeData(view);
        view.putBoolean("seenCredits", this.seenCredits);
        view.putNullable("entered_nether_pos", Vec3d.CODEC, this.enteredNetherPos);
        this.writeRootVehicle(view);
        view.put("recipeBook", ServerRecipeBook.Packed.CODEC, this.recipeBook.pack());
        view.putString(DIMENSION_KEY, this.getEntityWorld().getRegistryKey().getValue().toString());
        view.putNullable("respawn", Respawn.CODEC, this.respawn);
        view.putBoolean("spawn_extra_particles_on_fall", this.spawnExtraParticlesOnFall);
        view.putNullable("raid_omen_position", BlockPos.CODEC, this.startRaidPos);
        this.writeEnderPearls(view);
        if (!this.getLeftShoulderNbt().isEmpty()) {
            view.put("ShoulderEntityLeft", NbtCompound.CODEC, this.getLeftShoulderNbt());
        }
        if (!this.getRightShoulderNbt().isEmpty()) {
            view.put("ShoulderEntityRight", NbtCompound.CODEC, this.getRightShoulderNbt());
        }
    }

    private void writeRootVehicle(WriteView view) {
        Entity lv = this.getRootVehicle();
        Entity lv2 = this.getVehicle();
        if (lv2 != null && lv != this && lv.hasPlayerRider()) {
            WriteView lv3 = view.get("RootVehicle");
            lv3.put("Attach", Uuids.INT_STREAM_CODEC, lv2.getUuid());
            lv.saveData(lv3.get("Entity"));
        }
    }

    public void readRootVehicle(ReadView view) {
        Optional<ReadView> optional = view.getOptionalReadView("RootVehicle");
        if (optional.isEmpty()) {
            return;
        }
        ServerWorld lv = this.getEntityWorld();
        Entity lv2 = EntityType.loadEntityWithPassengers(optional.get().getReadView("Entity"), (World)lv, SpawnReason.LOAD, entity -> {
            if (!lv.tryLoadEntity((Entity)entity)) {
                return null;
            }
            return entity;
        });
        if (lv2 == null) {
            return;
        }
        UUID uUID = optional.get().read("Attach", Uuids.INT_STREAM_CODEC).orElse(null);
        if (lv2.getUuid().equals(uUID)) {
            this.startRiding(lv2, true, false);
        } else {
            for (Entity lv3 : lv2.getPassengersDeep()) {
                if (!lv3.getUuid().equals(uUID)) continue;
                this.startRiding(lv3, true, false);
                break;
            }
        }
        if (!this.hasVehicle()) {
            LOGGER.warn("Couldn't reattach entity to player");
            lv2.discard();
            for (Entity lv3 : lv2.getPassengersDeep()) {
                lv3.discard();
            }
        }
    }

    private void writeEnderPearls(WriteView view) {
        if (!this.enderPearls.isEmpty()) {
            WriteView.ListView lv = view.getList(ENDER_PEARLS_KEY);
            for (EnderPearlEntity lv2 : this.enderPearls) {
                if (lv2.isRemoved()) {
                    LOGGER.warn("Trying to save removed ender pearl, skipping");
                    continue;
                }
                WriteView lv3 = lv.add();
                lv2.saveData(lv3);
                lv3.put(ENDER_PEARLS_DIMENSION_KEY, World.CODEC, lv2.getEntityWorld().getRegistryKey());
            }
        }
    }

    public void readEnderPearls(ReadView view) {
        view.getListReadView(ENDER_PEARLS_KEY).forEach(this::readEnderPearl);
    }

    private void readEnderPearl(ReadView view) {
        Optional<RegistryKey<World>> optional = view.read(ENDER_PEARLS_DIMENSION_KEY, World.CODEC);
        if (optional.isEmpty()) {
            return;
        }
        ServerWorld lv = this.getEntityWorld().getServer().getWorld(optional.get());
        if (lv != null) {
            Entity lv2 = EntityType.loadEntityWithPassengers(view, (World)lv, SpawnReason.LOAD, enderPearl -> {
                if (!lv.tryLoadEntity((Entity)enderPearl)) {
                    return null;
                }
                return enderPearl;
            });
            if (lv2 != null) {
                ServerPlayerEntity.addEnderPearlTicket(lv, lv2.getChunkPos());
            } else {
                LOGGER.warn("Failed to spawn player ender pearl in level ({}), skipping", (Object)optional.get());
            }
        } else {
            LOGGER.warn("Trying to load ender pearl without level ({}) being loaded, skipping", (Object)optional.get());
        }
    }

    public void setExperiencePoints(int points) {
        float f = this.getNextLevelExperience();
        float g = (f - 1.0f) / f;
        this.experienceProgress = MathHelper.clamp((float)points / f, 0.0f, g);
        this.syncedExperience = -1;
    }

    public void setExperienceLevel(int level) {
        this.experienceLevel = level;
        this.syncedExperience = -1;
    }

    @Override
    public void addExperienceLevels(int levels) {
        super.addExperienceLevels(levels);
        this.syncedExperience = -1;
    }

    @Override
    public void applyEnchantmentCosts(ItemStack enchantedItem, int experienceLevels) {
        super.applyEnchantmentCosts(enchantedItem, experienceLevels);
        this.syncedExperience = -1;
    }

    private void onScreenHandlerOpened(ScreenHandler screenHandler) {
        screenHandler.addListener(this.screenHandlerListener);
        screenHandler.updateSyncHandler(this.screenHandlerSyncHandler);
    }

    public void onSpawn() {
        this.onScreenHandlerOpened(this.playerScreenHandler);
    }

    @Override
    public void enterCombat() {
        super.enterCombat();
        this.networkHandler.sendPacket(EnterCombatS2CPacket.INSTANCE);
    }

    @Override
    public void endCombat() {
        super.endCombat();
        this.networkHandler.sendPacket(new EndCombatS2CPacket(this.getDamageTracker()));
    }

    @Override
    public void onBlockCollision(BlockState state) {
        Criteria.ENTER_BLOCK.trigger(this, state);
    }

    @Override
    protected ItemCooldownManager createCooldownManager() {
        return new ServerItemCooldownManager(this);
    }

    @Override
    public void tick() {
        Entity lv;
        this.tickLoaded();
        this.interactionManager.update();
        this.sculkShriekerWarningManager.tick();
        if (this.timeUntilRegen > 0) {
            --this.timeUntilRegen;
        }
        this.currentScreenHandler.sendContentUpdates();
        if (!this.currentScreenHandler.canUse(this)) {
            this.closeHandledScreen();
            this.currentScreenHandler = this.playerScreenHandler;
        }
        if ((lv = this.getCameraEntity()) != this) {
            if (lv.isAlive()) {
                this.updatePositionAndAngles(lv.getX(), lv.getY(), lv.getZ(), lv.getYaw(), lv.getPitch());
                this.getEntityWorld().getChunkManager().updatePosition(this);
                if (this.shouldDismount()) {
                    this.setCameraEntity(this);
                }
            } else {
                this.setCameraEntity(this);
            }
        }
        Criteria.TICK.trigger(this);
        if (this.levitationStartPos != null) {
            Criteria.LEVITATION.trigger(this, this.levitationStartPos, this.age - this.levitationStartTick);
        }
        this.tickFallStartPos();
        this.tickVehicleInLavaRiding();
        this.updateCreativeInteractionRangeModifiers();
        this.advancementTracker.sendUpdate(this, true);
    }

    private void updateCreativeInteractionRangeModifiers() {
        EntityAttributeInstance lv3;
        EntityAttributeInstance lv2;
        EntityAttributeInstance lv = this.getAttributeInstance(EntityAttributes.BLOCK_INTERACTION_RANGE);
        if (lv != null) {
            if (this.isCreative()) {
                lv.updateModifier(CREATIVE_BLOCK_INTERACTION_RANGE_MODIFIER);
            } else {
                lv.removeModifier(CREATIVE_BLOCK_INTERACTION_RANGE_MODIFIER);
            }
        }
        if ((lv2 = this.getAttributeInstance(EntityAttributes.ENTITY_INTERACTION_RANGE)) != null) {
            if (this.isCreative()) {
                lv2.updateModifier(CREATIVE_ENTITY_INTERACTION_RANGE_MODIFIER);
            } else {
                lv2.removeModifier(CREATIVE_ENTITY_INTERACTION_RANGE_MODIFIER);
            }
        }
        if ((lv3 = this.getAttributeInstance(EntityAttributes.WAYPOINT_TRANSMIT_RANGE)) != null) {
            if (this.isInSneakingPose()) {
                lv3.updateModifier(WAYPOINT_TRANSMIT_RANGE_CROUCH_MODIFIER);
            } else {
                lv3.removeModifier(WAYPOINT_TRANSMIT_RANGE_CROUCH_MODIFIER);
            }
        }
    }

    public void playerTick() {
        try {
            if (!this.isSpectator() || !this.isRegionUnloaded()) {
                super.tick();
                if (!this.currentScreenHandler.canUse(this)) {
                    this.closeHandledScreen();
                    this.currentScreenHandler = this.playerScreenHandler;
                }
                this.hungerManager.update(this);
                this.incrementStat(Stats.PLAY_TIME);
                this.incrementStat(Stats.TOTAL_WORLD_TIME);
                if (this.isAlive()) {
                    this.incrementStat(Stats.TIME_SINCE_DEATH);
                }
                if (this.isSneaky()) {
                    this.incrementStat(Stats.SNEAK_TIME);
                }
                if (!this.isSleeping()) {
                    this.incrementStat(Stats.TIME_SINCE_REST);
                }
            }
            for (int i = 0; i < this.getInventory().size(); ++i) {
                ItemStack lv = this.getInventory().getStack(i);
                if (lv.isEmpty()) continue;
                this.sendMapPacket(lv);
            }
            if (this.getHealth() != this.syncedHealth || this.syncedFoodLevel != this.hungerManager.getFoodLevel() || this.hungerManager.getSaturationLevel() == 0.0f != this.syncedSaturationIsZero) {
                this.networkHandler.sendPacket(new HealthUpdateS2CPacket(this.getHealth(), this.hungerManager.getFoodLevel(), this.hungerManager.getSaturationLevel()));
                this.syncedHealth = this.getHealth();
                this.syncedFoodLevel = this.hungerManager.getFoodLevel();
                boolean bl = this.syncedSaturationIsZero = this.hungerManager.getSaturationLevel() == 0.0f;
            }
            if (this.getHealth() + this.getAbsorptionAmount() != this.lastHealthScore) {
                this.lastHealthScore = this.getHealth() + this.getAbsorptionAmount();
                this.updateScores(ScoreboardCriterion.HEALTH, MathHelper.ceil(this.lastHealthScore));
            }
            if (this.hungerManager.getFoodLevel() != this.lastFoodScore) {
                this.lastFoodScore = this.hungerManager.getFoodLevel();
                this.updateScores(ScoreboardCriterion.FOOD, MathHelper.ceil(this.lastFoodScore));
            }
            if (this.getAir() != this.lastAirScore) {
                this.lastAirScore = this.getAir();
                this.updateScores(ScoreboardCriterion.AIR, MathHelper.ceil(this.lastAirScore));
            }
            if (this.getArmor() != this.lastArmorScore) {
                this.lastArmorScore = this.getArmor();
                this.updateScores(ScoreboardCriterion.ARMOR, MathHelper.ceil(this.lastArmorScore));
            }
            if (this.totalExperience != this.lastExperienceScore) {
                this.lastExperienceScore = this.totalExperience;
                this.updateScores(ScoreboardCriterion.XP, MathHelper.ceil(this.lastExperienceScore));
            }
            if (this.experienceLevel != this.lastLevelScore) {
                this.lastLevelScore = this.experienceLevel;
                this.updateScores(ScoreboardCriterion.LEVEL, MathHelper.ceil(this.lastLevelScore));
            }
            if (this.totalExperience != this.syncedExperience) {
                this.syncedExperience = this.totalExperience;
                this.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(this.experienceProgress, this.totalExperience, this.experienceLevel));
            }
            if (this.age % 20 == 0) {
                Criteria.LOCATION.trigger(this);
            }
        } catch (Throwable throwable) {
            CrashReport lv2 = CrashReport.create(throwable, "Ticking player");
            CrashReportSection lv3 = lv2.addElement("Player being ticked");
            this.populateCrashReport(lv3);
            throw new CrashException(lv2);
        }
    }

    private void sendMapPacket(ItemStack stack) {
        Packet<?> lv3;
        MapIdComponent lv = stack.get(DataComponentTypes.MAP_ID);
        MapState lv2 = FilledMapItem.getMapState(lv, (World)this.getEntityWorld());
        if (lv2 != null && (lv3 = lv2.getPlayerMarkerPacket(lv, this)) != null) {
            this.networkHandler.sendPacket(lv3);
        }
    }

    @Override
    protected void tickHunger() {
        if (this.getEntityWorld().getDifficulty() == Difficulty.PEACEFUL && this.getEntityWorld().getGameRules().getBoolean(GameRules.NATURAL_REGENERATION)) {
            if (this.age % 20 == 0) {
                float f;
                if (this.getHealth() < this.getMaxHealth()) {
                    this.heal(1.0f);
                }
                if ((f = this.hungerManager.getSaturationLevel()) < 20.0f) {
                    this.hungerManager.setSaturationLevel(f + 1.0f);
                }
            }
            if (this.age % 10 == 0 && this.hungerManager.isNotFull()) {
                this.hungerManager.setFoodLevel(this.hungerManager.getFoodLevel() + 1);
            }
        }
    }

    @Override
    public void handleShoulderEntities() {
        this.playShoulderEntitySound(this.getLeftShoulderNbt());
        this.playShoulderEntitySound(this.getRightShoulderNbt());
        if (this.fallDistance > 0.5 || this.isTouchingWater() || this.getAbilities().flying || this.isSleeping() || this.inPowderSnow) {
            this.dropShoulderEntities();
        }
    }

    private void playShoulderEntitySound(NbtCompound nbt) {
        EntityType lv;
        if (nbt.isEmpty() || nbt.getBoolean("Silent", false)) {
            return;
        }
        if (this.random.nextInt(200) == 0 && (lv = (EntityType)nbt.get("id", EntityType.CODEC).orElse(null)) == EntityType.PARROT && !ParrotEntity.imitateNearbyMob(this.getEntityWorld(), this)) {
            this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), ParrotEntity.getRandomSound(this.getEntityWorld(), this.random), this.getSoundCategory(), 1.0f, ParrotEntity.getSoundPitch(this.random));
        }
    }

    public boolean mountOntoShoulder(NbtCompound shoulderNbt) {
        if (this.hasVehicle() || !this.isOnGround() || this.isTouchingWater() || this.inPowderSnow) {
            return false;
        }
        if (this.getLeftShoulderNbt().isEmpty()) {
            this.setLeftShoulderNbt(shoulderNbt);
            this.shoulderMountTime = this.getEntityWorld().getTime();
            return true;
        }
        if (this.getRightShoulderNbt().isEmpty()) {
            this.setRightShoulderNbt(shoulderNbt);
            this.shoulderMountTime = this.getEntityWorld().getTime();
            return true;
        }
        return false;
    }

    @Override
    protected void dropShoulderEntities() {
        if (this.shoulderMountTime + 20L < this.getEntityWorld().getTime()) {
            this.spawnShoulderEntity(this.getLeftShoulderNbt());
            this.setLeftShoulderNbt(new NbtCompound());
            this.spawnShoulderEntity(this.getRightShoulderNbt());
            this.setRightShoulderNbt(new NbtCompound());
        }
    }

    private void spawnShoulderEntity(NbtCompound nbt) {
        ServerWorld serverWorld = this.getEntityWorld();
        if (serverWorld instanceof ServerWorld) {
            ServerWorld lv = serverWorld;
            if (!nbt.isEmpty()) {
                try (ErrorReporter.Logging lv2 = new ErrorReporter.Logging(this.getErrorReporterContext(), LOGGER);){
                    EntityType.getEntityFromData(NbtReadView.create(lv2.makeChild(() -> ".shoulder"), lv.getRegistryManager(), nbt), lv, SpawnReason.LOAD).ifPresent(entity -> {
                        if (entity instanceof TameableEntity) {
                            TameableEntity lv = (TameableEntity)entity;
                            lv.setOwner(this);
                        }
                        entity.setPosition(this.getX(), this.getY() + (double)0.7f, this.getZ());
                        lv.tryLoadEntity((Entity)entity);
                    });
                }
            }
        }
    }

    @Override
    public void onLanding() {
        if (this.getHealth() > 0.0f && this.fallStartPos != null) {
            Criteria.FALL_FROM_HEIGHT.trigger(this, this.fallStartPos);
        }
        this.fallStartPos = null;
        super.onLanding();
    }

    public void tickFallStartPos() {
        if (this.fallDistance > 0.0 && this.fallStartPos == null) {
            this.fallStartPos = this.getEntityPos();
            if (this.currentExplosionImpactPos != null && this.currentExplosionImpactPos.y <= this.fallStartPos.y) {
                Criteria.FALL_AFTER_EXPLOSION.trigger(this, this.currentExplosionImpactPos, this.explodedBy);
            }
        }
    }

    public void tickVehicleInLavaRiding() {
        if (this.getVehicle() != null && this.getVehicle().isInLava()) {
            if (this.vehicleInLavaRidingPos == null) {
                this.vehicleInLavaRidingPos = this.getEntityPos();
            } else {
                Criteria.RIDE_ENTITY_IN_LAVA.trigger(this, this.vehicleInLavaRidingPos);
            }
        }
        if (!(this.vehicleInLavaRidingPos == null || this.getVehicle() != null && this.getVehicle().isInLava())) {
            this.vehicleInLavaRidingPos = null;
        }
    }

    private void updateScores(ScoreboardCriterion criterion, int score) {
        this.getEntityWorld().getScoreboard().forEachScore(criterion, this, innerScore -> innerScore.setScore(score));
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        this.emitGameEvent(GameEvent.ENTITY_DIE);
        boolean bl = this.getEntityWorld().getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES);
        if (bl) {
            Text lv = this.getDamageTracker().getDeathMessage();
            this.networkHandler.send(new DeathMessageS2CPacket(this.getId(), lv), PacketCallbacks.of(() -> {
                int i = 256;
                String string = lv.asTruncatedString(256);
                MutableText lv = Text.translatable("death.attack.message_too_long", Text.literal(string).formatted(Formatting.YELLOW));
                MutableText lv2 = Text.translatable("death.attack.even_more_magic", this.getDisplayName()).styled(style -> style.withHoverEvent(new HoverEvent.ShowText(lv)));
                return new DeathMessageS2CPacket(this.getId(), lv2);
            }));
            Team lv2 = this.getScoreboardTeam();
            if (lv2 == null || ((AbstractTeam)lv2).getDeathMessageVisibilityRule() == AbstractTeam.VisibilityRule.ALWAYS) {
                this.server.getPlayerManager().broadcast(lv, false);
            } else if (((AbstractTeam)lv2).getDeathMessageVisibilityRule() == AbstractTeam.VisibilityRule.HIDE_FOR_OTHER_TEAMS) {
                this.server.getPlayerManager().sendToTeam(this, lv);
            } else if (((AbstractTeam)lv2).getDeathMessageVisibilityRule() == AbstractTeam.VisibilityRule.HIDE_FOR_OWN_TEAM) {
                this.server.getPlayerManager().sendToOtherTeams(this, lv);
            }
        } else {
            this.networkHandler.sendPacket(new DeathMessageS2CPacket(this.getId(), ScreenTexts.EMPTY));
        }
        this.dropShoulderEntities();
        if (this.getEntityWorld().getGameRules().getBoolean(GameRules.FORGIVE_DEAD_PLAYERS)) {
            this.forgiveMobAnger();
        }
        if (!this.isSpectator()) {
            this.drop(this.getEntityWorld(), damageSource);
        }
        this.getEntityWorld().getScoreboard().forEachScore(ScoreboardCriterion.DEATH_COUNT, this, ScoreAccess::incrementScore);
        LivingEntity lv3 = this.getPrimeAdversary();
        if (lv3 != null) {
            this.incrementStat(Stats.KILLED_BY.getOrCreateStat(lv3.getType()));
            lv3.updateKilledAdvancementCriterion(this, damageSource);
            this.onKilledBy(lv3);
        }
        this.getEntityWorld().sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);
        this.incrementStat(Stats.DEATHS);
        this.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_DEATH));
        this.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST));
        this.extinguish();
        this.setFrozenTicks(0);
        this.setOnFire(false);
        this.getDamageTracker().update();
        this.setLastDeathPos(Optional.of(GlobalPos.create(this.getEntityWorld().getRegistryKey(), this.getBlockPos())));
        this.setLoaded(false);
    }

    private void forgiveMobAnger() {
        Box lv = new Box(this.getBlockPos()).expand(32.0, 10.0, 32.0);
        this.getEntityWorld().getEntitiesByClass(MobEntity.class, lv, EntityPredicates.EXCEPT_SPECTATOR).stream().filter(entity -> entity instanceof Angerable).forEach(entity -> ((Angerable)((Object)entity)).forgive(this.getEntityWorld(), this));
    }

    @Override
    public void updateKilledAdvancementCriterion(Entity entityKilled, DamageSource damageSource) {
        if (entityKilled == this) {
            return;
        }
        super.updateKilledAdvancementCriterion(entityKilled, damageSource);
        ServerScoreboard lv = this.getEntityWorld().getScoreboard();
        lv.forEachScore(ScoreboardCriterion.TOTAL_KILL_COUNT, this, ScoreAccess::incrementScore);
        if (entityKilled instanceof PlayerEntity) {
            this.incrementStat(Stats.PLAYER_KILLS);
            lv.forEachScore(ScoreboardCriterion.PLAYER_KILL_COUNT, this, ScoreAccess::incrementScore);
        } else {
            this.incrementStat(Stats.MOB_KILLS);
        }
        this.updateScoreboardScore(this, entityKilled, ScoreboardCriterion.TEAM_KILLS);
        this.updateScoreboardScore(entityKilled, this, ScoreboardCriterion.KILLED_BY_TEAMS);
        Criteria.PLAYER_KILLED_ENTITY.trigger(this, entityKilled, damageSource);
    }

    private void updateScoreboardScore(ScoreHolder targetScoreHolder, ScoreHolder aboutScoreHolder, ScoreboardCriterion[] criterions) {
        int i;
        ServerScoreboard lv = this.getEntityWorld().getScoreboard();
        Team lv2 = lv.getScoreHolderTeam(aboutScoreHolder.getNameForScoreboard());
        if (lv2 != null && (i = lv2.getColor().getColorIndex()) >= 0 && i < criterions.length) {
            lv.forEachScore(criterions[i], targetScoreHolder, ScoreAccess::incrementScore);
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        PlayerEntity lv5;
        PersistentProjectileEntity lv3;
        Entity lv4;
        PlayerEntity lv2;
        if (this.isInvulnerableTo(world, source)) {
            return false;
        }
        Entity lv = source.getAttacker();
        if (lv instanceof PlayerEntity && !this.shouldDamagePlayer(lv2 = (PlayerEntity)lv)) {
            return false;
        }
        if (lv instanceof PersistentProjectileEntity && (lv4 = (lv3 = (PersistentProjectileEntity)lv).getOwner()) instanceof PlayerEntity && !this.shouldDamagePlayer(lv5 = (PlayerEntity)lv4)) {
            return false;
        }
        return super.damage(world, source, amount);
    }

    @Override
    public boolean shouldDamagePlayer(PlayerEntity player) {
        if (!this.isPvpEnabled()) {
            return false;
        }
        return super.shouldDamagePlayer(player);
    }

    private boolean isPvpEnabled() {
        return this.server.isPvpEnabled();
    }

    public TeleportTarget getRespawnTarget(boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition) {
        Respawn lv = this.getRespawn();
        ServerWorld lv2 = this.server.getWorld(Respawn.getDimension(lv));
        if (lv2 != null && lv != null) {
            Optional<RespawnPos> optional = ServerPlayerEntity.findRespawnPosition(lv2, lv, alive);
            if (optional.isPresent()) {
                RespawnPos lv3 = optional.get();
                return new TeleportTarget(lv2, lv3.pos(), Vec3d.ZERO, lv3.yaw(), lv3.pitch(), postDimensionTransition);
            }
            return TeleportTarget.missingSpawnBlock(this, postDimensionTransition);
        }
        return TeleportTarget.noRespawnPointSet(this, postDimensionTransition);
    }

    public boolean canReceiveWaypoints() {
        return this.getAttributeValue(EntityAttributes.WAYPOINT_RECEIVE_RANGE) > 0.0;
    }

    @Override
    protected void updateAttribute(RegistryEntry<EntityAttribute> attribute) {
        if (attribute.matches(EntityAttributes.WAYPOINT_RECEIVE_RANGE)) {
            ServerWaypointHandler lv = this.getEntityWorld().getWaypointHandler();
            if (this.getAttributes().getValue(attribute) > 0.0) {
                lv.addPlayer(this);
            } else {
                lv.removePlayer(this);
            }
        }
        super.updateAttribute(attribute);
    }

    private static Optional<RespawnPos> findRespawnPosition(ServerWorld world, Respawn respawn, boolean bl) {
        WorldProperties.SpawnPoint lv = respawn.respawnData;
        BlockPos lv2 = lv.getPos();
        float f = lv.yaw();
        float g = lv.pitch();
        boolean bl2 = respawn.forced;
        BlockState lv3 = world.getBlockState(lv2);
        Block lv4 = lv3.getBlock();
        if (lv4 instanceof RespawnAnchorBlock && (bl2 || lv3.get(RespawnAnchorBlock.CHARGES) > 0) && RespawnAnchorBlock.isNether(world)) {
            Optional<Vec3d> optional = RespawnAnchorBlock.findRespawnPosition(EntityType.PLAYER, world, lv2);
            if (!bl2 && bl && optional.isPresent()) {
                world.setBlockState(lv2, (BlockState)lv3.with(RespawnAnchorBlock.CHARGES, lv3.get(RespawnAnchorBlock.CHARGES) - 1), Block.NOTIFY_ALL);
            }
            return optional.map(respawnPos -> RespawnPos.fromCurrentPos(respawnPos, lv2, 0.0f));
        }
        if (lv4 instanceof BedBlock && BedBlock.isBedWorking(world)) {
            return BedBlock.findWakeUpPosition(EntityType.PLAYER, (CollisionView)world, lv2, (Direction)lv3.get(BedBlock.FACING), f).map(respawnPos -> RespawnPos.fromCurrentPos(respawnPos, lv2, 0.0f));
        }
        if (!bl2) {
            return Optional.empty();
        }
        boolean bl3 = lv4.canMobSpawnInside(lv3);
        BlockState lv5 = world.getBlockState(lv2.up());
        boolean bl4 = lv5.getBlock().canMobSpawnInside(lv5);
        if (bl3 && bl4) {
            return Optional.of(new RespawnPos(new Vec3d((double)lv2.getX() + 0.5, (double)lv2.getY() + 0.1, (double)lv2.getZ() + 0.5), f, g));
        }
        return Optional.empty();
    }

    public void detachForDimensionChange() {
        this.detach();
        this.getEntityWorld().removePlayer(this, Entity.RemovalReason.CHANGED_DIMENSION);
        if (!this.notInAnyWorld) {
            this.notInAnyWorld = true;
            this.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_WON, GameStateChangeS2CPacket.DEMO_OPEN_SCREEN));
            this.seenCredits = true;
        }
    }

    @Override
    @Nullable
    public ServerPlayerEntity teleportTo(TeleportTarget arg) {
        if (this.isRemoved()) {
            return null;
        }
        if (arg.missingRespawnBlock()) {
            this.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.NO_RESPAWN_BLOCK, GameStateChangeS2CPacket.DEMO_OPEN_SCREEN));
        }
        ServerWorld lv = arg.world();
        ServerWorld lv2 = this.getEntityWorld();
        RegistryKey<World> lv3 = lv2.getRegistryKey();
        if (!arg.asPassenger()) {
            this.dismountVehicle();
        }
        if (lv.getRegistryKey() == lv3) {
            this.networkHandler.requestTeleport(EntityPosition.fromTeleportTarget(arg), arg.relatives());
            this.networkHandler.syncWithPlayerPosition();
            arg.postTeleportTransition().onTransition(this);
            return this;
        }
        this.inTeleportationState = true;
        WorldProperties lv4 = lv.getLevelProperties();
        this.networkHandler.sendPacket(new PlayerRespawnS2CPacket(this.createCommonPlayerSpawnInfo(lv), PlayerRespawnS2CPacket.KEEP_ALL));
        this.networkHandler.sendPacket(new DifficultyS2CPacket(lv4.getDifficulty(), lv4.isDifficultyLocked()));
        PlayerManager lv5 = this.server.getPlayerManager();
        lv5.sendCommandTree(this);
        lv2.removePlayer(this, Entity.RemovalReason.CHANGED_DIMENSION);
        this.unsetRemoved();
        Profiler lv6 = Profilers.get();
        lv6.push("moving");
        if (lv3 == World.OVERWORLD && lv.getRegistryKey() == World.NETHER) {
            this.enteredNetherPos = this.getEntityPos();
        }
        lv6.pop();
        lv6.push("placing");
        this.setServerWorld(lv);
        this.networkHandler.requestTeleport(EntityPosition.fromTeleportTarget(arg), arg.relatives());
        this.networkHandler.syncWithPlayerPosition();
        lv.onDimensionChanged(this);
        lv6.pop();
        this.worldChanged(lv2);
        this.clearActiveItem();
        this.networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(this.getAbilities()));
        lv5.sendWorldInfo(this, lv);
        lv5.sendPlayerStatus(this);
        lv5.sendStatusEffects(this);
        arg.postTeleportTransition().onTransition(this);
        this.syncedExperience = -1;
        this.syncedHealth = -1.0f;
        this.syncedFoodLevel = -1;
        this.teleportSpectatingPlayers(arg, lv2);
        return this;
    }

    @Override
    public void rotate(float yaw, boolean relativeYaw, float pitch, boolean relativePitch) {
        super.rotate(yaw, relativeYaw, pitch, relativePitch);
        this.networkHandler.sendPacket(new PlayerRotationS2CPacket(yaw, relativeYaw, pitch, relativePitch));
    }

    private void worldChanged(ServerWorld origin) {
        RegistryKey<World> lv = origin.getRegistryKey();
        RegistryKey<World> lv2 = this.getEntityWorld().getRegistryKey();
        Criteria.CHANGED_DIMENSION.trigger(this, lv, lv2);
        if (lv == World.NETHER && lv2 == World.OVERWORLD && this.enteredNetherPos != null) {
            Criteria.NETHER_TRAVEL.trigger(this, this.enteredNetherPos);
        }
        if (lv2 != World.NETHER) {
            this.enteredNetherPos = null;
        }
    }

    @Override
    public boolean canBeSpectated(ServerPlayerEntity spectator) {
        if (spectator.isSpectator()) {
            return this.getCameraEntity() == this;
        }
        if (this.isSpectator()) {
            return false;
        }
        return super.canBeSpectated(spectator);
    }

    @Override
    public void sendPickup(Entity item, int count) {
        super.sendPickup(item, count);
        this.currentScreenHandler.sendContentUpdates();
    }

    @Override
    public Either<PlayerEntity.SleepFailureReason, Unit> trySleep(BlockPos pos) {
        Direction lv = this.getEntityWorld().getBlockState(pos).get(HorizontalFacingBlock.FACING);
        if (this.isSleeping() || !this.isAlive()) {
            return Either.left(PlayerEntity.SleepFailureReason.OTHER_PROBLEM);
        }
        if (!this.getEntityWorld().getDimension().natural()) {
            return Either.left(PlayerEntity.SleepFailureReason.NOT_POSSIBLE_HERE);
        }
        if (!this.isBedWithinRange(pos, lv)) {
            return Either.left(PlayerEntity.SleepFailureReason.TOO_FAR_AWAY);
        }
        if (this.isBedObstructed(pos, lv)) {
            return Either.left(PlayerEntity.SleepFailureReason.OBSTRUCTED);
        }
        this.setSpawnPoint(new Respawn(WorldProperties.SpawnPoint.create(this.getEntityWorld().getRegistryKey(), pos, this.getYaw(), this.getPitch()), false), true);
        if (this.getEntityWorld().isDay()) {
            return Either.left(PlayerEntity.SleepFailureReason.NOT_POSSIBLE_NOW);
        }
        if (!this.isCreative()) {
            double d = 8.0;
            double e = 5.0;
            Vec3d lv2 = Vec3d.ofBottomCenter(pos);
            List<HostileEntity> list = this.getEntityWorld().getEntitiesByClass(HostileEntity.class, new Box(lv2.getX() - 8.0, lv2.getY() - 5.0, lv2.getZ() - 8.0, lv2.getX() + 8.0, lv2.getY() + 5.0, lv2.getZ() + 8.0), entity -> entity.isAngryAt(this.getEntityWorld(), this));
            if (!list.isEmpty()) {
                return Either.left(PlayerEntity.SleepFailureReason.NOT_SAFE);
            }
        }
        Either<PlayerEntity.SleepFailureReason, Unit> either = super.trySleep(pos).ifRight(unit -> {
            this.incrementStat(Stats.SLEEP_IN_BED);
            Criteria.SLEPT_IN_BED.trigger(this);
        });
        if (!this.getEntityWorld().isSleepingEnabled()) {
            this.sendMessage(Text.translatable("sleep.not_possible"), true);
        }
        this.getEntityWorld().updateSleepingPlayers();
        return either;
    }

    @Override
    public void sleep(BlockPos pos) {
        this.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST));
        super.sleep(pos);
    }

    private boolean isBedWithinRange(BlockPos pos, Direction direction) {
        return this.isBedWithinRange(pos) || this.isBedWithinRange(pos.offset(direction.getOpposite()));
    }

    private boolean isBedWithinRange(BlockPos pos) {
        Vec3d lv = Vec3d.ofBottomCenter(pos);
        return Math.abs(this.getX() - lv.getX()) <= 3.0 && Math.abs(this.getY() - lv.getY()) <= 2.0 && Math.abs(this.getZ() - lv.getZ()) <= 3.0;
    }

    private boolean isBedObstructed(BlockPos pos, Direction direction) {
        BlockPos lv = pos.up();
        return !this.doesNotSuffocate(lv) || !this.doesNotSuffocate(lv.offset(direction.getOpposite()));
    }

    @Override
    public void wakeUp(boolean skipSleepTimer, boolean updateSleepingPlayers) {
        if (this.isSleeping()) {
            this.getEntityWorld().getChunkManager().sendToNearbyPlayers(this, new EntityAnimationS2CPacket(this, EntityAnimationS2CPacket.WAKE_UP));
        }
        super.wakeUp(skipSleepTimer, updateSleepingPlayers);
        if (this.networkHandler != null) {
            this.networkHandler.requestTeleport(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
        }
    }

    @Override
    public boolean isInvulnerableTo(ServerWorld world, DamageSource source) {
        return super.isInvulnerableTo(world, source) || this.isInTeleportationState() && !source.isOf(DamageTypes.ENDER_PEARL) || !this.isLoaded();
    }

    @Override
    protected void applyMovementEffects(ServerWorld world, BlockPos pos) {
        if (!this.isSpectator()) {
            super.applyMovementEffects(world, pos);
        }
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
        if (this.spawnExtraParticlesOnFall && onGround && this.fallDistance > 0.0) {
            Vec3d lv = landedPosition.toCenterPos().add(0.0, 0.5, 0.0);
            int i = (int)MathHelper.clamp(50.0 * this.fallDistance, 0.0, 200.0);
            this.getEntityWorld().spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), lv.x, lv.y, lv.z, i, 0.3f, 0.3f, 0.3f, 0.15f);
            this.spawnExtraParticlesOnFall = false;
        }
        super.fall(heightDifference, onGround, state, landedPosition);
    }

    @Override
    public void onExplodedBy(@Nullable Entity entity) {
        super.onExplodedBy(entity);
        this.currentExplosionImpactPos = this.getEntityPos();
        this.explodedBy = entity;
        this.setIgnoreFallDamageFromCurrentExplosion(entity != null && entity.getType() == EntityType.WIND_CHARGE);
    }

    @Override
    protected void tickCramming() {
        if (this.getEntityWorld().getTickManager().shouldTick()) {
            super.tickCramming();
        }
    }

    @Override
    public void openEditSignScreen(SignBlockEntity sign, boolean front) {
        this.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.getEntityWorld(), sign.getPos()));
        this.networkHandler.sendPacket(new SignEditorOpenS2CPacket(sign.getPos(), front));
    }

    @Override
    public void openDialog(RegistryEntry<Dialog> dialog) {
        this.networkHandler.sendPacket(new ShowDialogS2CPacket(dialog));
    }

    private void incrementScreenHandlerSyncId() {
        this.screenHandlerSyncId = this.screenHandlerSyncId % 100 + 1;
    }

    @Override
    public OptionalInt openHandledScreen(@Nullable NamedScreenHandlerFactory factory) {
        if (factory == null) {
            return OptionalInt.empty();
        }
        if (this.currentScreenHandler != this.playerScreenHandler) {
            this.closeHandledScreen();
        }
        this.incrementScreenHandlerSyncId();
        ScreenHandler lv = factory.createMenu(this.screenHandlerSyncId, this.getInventory(), this);
        if (lv == null) {
            if (this.isSpectator()) {
                this.sendMessage(Text.translatable("container.spectatorCantOpen").formatted(Formatting.RED), true);
            }
            return OptionalInt.empty();
        }
        this.networkHandler.sendPacket(new OpenScreenS2CPacket(lv.syncId, lv.getType(), factory.getDisplayName()));
        this.onScreenHandlerOpened(lv);
        this.currentScreenHandler = lv;
        return OptionalInt.of(this.screenHandlerSyncId);
    }

    @Override
    public void sendTradeOffers(int syncId, TradeOfferList offers, int levelProgress, int experience, boolean leveled, boolean refreshable) {
        this.networkHandler.sendPacket(new SetTradeOffersS2CPacket(syncId, offers, levelProgress, experience, leveled, refreshable));
    }

    @Override
    public void openHorseInventory(AbstractHorseEntity horse, Inventory inventory) {
        if (this.currentScreenHandler != this.playerScreenHandler) {
            this.closeHandledScreen();
        }
        this.incrementScreenHandlerSyncId();
        int i = horse.getInventoryColumns();
        this.networkHandler.sendPacket(new OpenHorseScreenS2CPacket(this.screenHandlerSyncId, i, horse.getId()));
        this.currentScreenHandler = new HorseScreenHandler(this.screenHandlerSyncId, this.getInventory(), inventory, horse, i);
        this.onScreenHandlerOpened(this.currentScreenHandler);
    }

    @Override
    public void useBook(ItemStack book, Hand hand) {
        if (book.contains(DataComponentTypes.WRITTEN_BOOK_CONTENT)) {
            if (WrittenBookContentComponent.resolveInStack(book, this.getCommandSource(), this)) {
                this.currentScreenHandler.sendContentUpdates();
            }
            this.networkHandler.sendPacket(new OpenWrittenBookS2CPacket(hand));
        }
    }

    @Override
    public void openCommandBlockScreen(CommandBlockBlockEntity commandBlock) {
        this.networkHandler.sendPacket(BlockEntityUpdateS2CPacket.create(commandBlock, BlockEntity::createComponentlessNbt));
    }

    @Override
    public void closeHandledScreen() {
        this.networkHandler.sendPacket(new CloseScreenS2CPacket(this.currentScreenHandler.syncId));
        this.onHandledScreenClosed();
    }

    @Override
    public void onHandledScreenClosed() {
        this.currentScreenHandler.onClosed(this);
        this.playerScreenHandler.copySharedSlots(this.currentScreenHandler);
        this.currentScreenHandler = this.playerScreenHandler;
    }

    @Override
    public void tickRiding() {
        double d = this.getX();
        double e = this.getY();
        double f = this.getZ();
        super.tickRiding();
        this.increaseRidingMotionStats(this.getX() - d, this.getY() - e, this.getZ() - f);
    }

    public void increaseTravelMotionStats(double deltaX, double deltaY, double deltaZ) {
        if (this.hasVehicle() || ServerPlayerEntity.isZero(deltaX, deltaY, deltaZ)) {
            return;
        }
        if (this.isSwimming()) {
            int i = Math.round((float)Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) * 100.0f);
            if (i > 0) {
                this.increaseStat(Stats.SWIM_ONE_CM, i);
                this.addExhaustion(0.01f * (float)i * 0.01f);
            }
        } else if (this.isSubmergedIn(FluidTags.WATER)) {
            int i = Math.round((float)Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) * 100.0f);
            if (i > 0) {
                this.increaseStat(Stats.WALK_UNDER_WATER_ONE_CM, i);
                this.addExhaustion(0.01f * (float)i * 0.01f);
            }
        } else if (this.isTouchingWater()) {
            int i = Math.round((float)Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) * 100.0f);
            if (i > 0) {
                this.increaseStat(Stats.WALK_ON_WATER_ONE_CM, i);
                this.addExhaustion(0.01f * (float)i * 0.01f);
            }
        } else if (this.isClimbing()) {
            if (deltaY > 0.0) {
                this.increaseStat(Stats.CLIMB_ONE_CM, (int)Math.round(deltaY * 100.0));
            }
        } else if (this.isOnGround()) {
            int i = Math.round((float)Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) * 100.0f);
            if (i > 0) {
                if (this.isSprinting()) {
                    this.increaseStat(Stats.SPRINT_ONE_CM, i);
                    this.addExhaustion(0.1f * (float)i * 0.01f);
                } else if (this.isInSneakingPose()) {
                    this.increaseStat(Stats.CROUCH_ONE_CM, i);
                    this.addExhaustion(0.0f * (float)i * 0.01f);
                } else {
                    this.increaseStat(Stats.WALK_ONE_CM, i);
                    this.addExhaustion(0.0f * (float)i * 0.01f);
                }
            }
        } else if (this.isGliding()) {
            int i = Math.round((float)Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) * 100.0f);
            this.increaseStat(Stats.AVIATE_ONE_CM, i);
        } else {
            int i = Math.round((float)Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) * 100.0f);
            if (i > 25) {
                this.increaseStat(Stats.FLY_ONE_CM, i);
            }
        }
    }

    private void increaseRidingMotionStats(double deltaX, double deltaY, double deltaZ) {
        if (!this.hasVehicle() || ServerPlayerEntity.isZero(deltaX, deltaY, deltaZ)) {
            return;
        }
        int i = Math.round((float)Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) * 100.0f);
        Entity lv = this.getVehicle();
        if (lv instanceof AbstractMinecartEntity) {
            this.increaseStat(Stats.MINECART_ONE_CM, i);
        } else if (lv instanceof AbstractBoatEntity) {
            this.increaseStat(Stats.BOAT_ONE_CM, i);
        } else if (lv instanceof PigEntity) {
            this.increaseStat(Stats.PIG_ONE_CM, i);
        } else if (lv instanceof AbstractHorseEntity) {
            this.increaseStat(Stats.HORSE_ONE_CM, i);
        } else if (lv instanceof StriderEntity) {
            this.increaseStat(Stats.STRIDER_ONE_CM, i);
        } else if (lv instanceof HappyGhastEntity) {
            this.increaseStat(Stats.HAPPY_GHAST_ONE_CM, i);
        }
    }

    private static boolean isZero(double deltaX, double deltaY, double deltaZ) {
        return deltaX == 0.0 && deltaY == 0.0 && deltaZ == 0.0;
    }

    @Override
    public void increaseStat(Stat<?> stat, int amount) {
        this.statHandler.increaseStat(this, stat, amount);
        this.getEntityWorld().getScoreboard().forEachScore(stat, this, score -> score.incrementScore(amount));
    }

    @Override
    public void resetStat(Stat<?> stat) {
        this.statHandler.setStat(this, stat, 0);
        this.getEntityWorld().getScoreboard().forEachScore(stat, this, ScoreAccess::resetScore);
    }

    @Override
    public int unlockRecipes(Collection<RecipeEntry<?>> recipes) {
        return this.recipeBook.unlockRecipes(recipes, this);
    }

    @Override
    public void onRecipeCrafted(RecipeEntry<?> recipe, List<ItemStack> ingredients) {
        Criteria.RECIPE_CRAFTED.trigger(this, recipe.id(), ingredients);
    }

    @Override
    public void unlockRecipes(List<RegistryKey<Recipe<?>>> recipes) {
        List<RecipeEntry<?>> list2 = recipes.stream().flatMap(recipeKey -> this.server.getRecipeManager().get((RegistryKey<Recipe<?>>)recipeKey).stream()).collect(Collectors.toList());
        this.unlockRecipes((Collection<RecipeEntry<?>>)list2);
    }

    @Override
    public int lockRecipes(Collection<RecipeEntry<?>> recipes) {
        return this.recipeBook.lockRecipes(recipes, this);
    }

    @Override
    public void jump() {
        super.jump();
        this.incrementStat(Stats.JUMP);
        if (this.isSprinting()) {
            this.addExhaustion(0.2f);
        } else {
            this.addExhaustion(0.05f);
        }
    }

    @Override
    public void addExperience(int experience) {
        super.addExperience(experience);
        this.syncedExperience = -1;
    }

    public void onDisconnect() {
        this.disconnected = true;
        this.removeAllPassengers();
        if (this.isSleeping()) {
            this.wakeUp(true, false);
        }
    }

    public boolean isDisconnected() {
        return this.disconnected;
    }

    public void markHealthDirty() {
        this.syncedHealth = -1.0E8f;
    }

    @Override
    public void sendMessage(Text message, boolean overlay) {
        this.sendMessageToClient(message, overlay);
    }

    @Override
    protected void consumeItem() {
        if (!this.activeItemStack.isEmpty() && this.isUsingItem()) {
            this.networkHandler.sendPacket(new EntityStatusS2CPacket(this, EntityStatuses.CONSUME_ITEM));
            super.consumeItem();
        }
    }

    @Override
    public void lookAt(EntityAnchorArgumentType.EntityAnchor anchorPoint, Vec3d target) {
        super.lookAt(anchorPoint, target);
        this.networkHandler.sendPacket(new LookAtS2CPacket(anchorPoint, target.x, target.y, target.z));
    }

    public void lookAtEntity(EntityAnchorArgumentType.EntityAnchor anchorPoint, Entity targetEntity, EntityAnchorArgumentType.EntityAnchor targetAnchor) {
        Vec3d lv = targetAnchor.positionAt(targetEntity);
        super.lookAt(anchorPoint, lv);
        this.networkHandler.sendPacket(new LookAtS2CPacket(anchorPoint, targetEntity, targetAnchor));
    }

    public void copyFrom(ServerPlayerEntity oldPlayer, boolean alive) {
        this.sculkShriekerWarningManager = oldPlayer.sculkShriekerWarningManager;
        this.session = oldPlayer.session;
        this.interactionManager.setGameMode(oldPlayer.interactionManager.getGameMode(), oldPlayer.interactionManager.getPreviousGameMode());
        this.sendAbilitiesUpdate();
        if (alive) {
            this.getAttributes().setBaseFrom(oldPlayer.getAttributes());
            this.getAttributes().addPersistentModifiersFrom(oldPlayer.getAttributes());
            this.setHealth(oldPlayer.getHealth());
            this.hungerManager = oldPlayer.hungerManager;
            for (StatusEffectInstance lv : oldPlayer.getStatusEffects()) {
                this.addStatusEffect(new StatusEffectInstance(lv));
            }
            this.getInventory().clone(oldPlayer.getInventory());
            this.experienceLevel = oldPlayer.experienceLevel;
            this.totalExperience = oldPlayer.totalExperience;
            this.experienceProgress = oldPlayer.experienceProgress;
            this.setScore(oldPlayer.getScore());
            this.portalManager = oldPlayer.portalManager;
        } else {
            this.getAttributes().setBaseFrom(oldPlayer.getAttributes());
            this.setHealth(this.getMaxHealth());
            if (this.getEntityWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || oldPlayer.isSpectator()) {
                this.getInventory().clone(oldPlayer.getInventory());
                this.experienceLevel = oldPlayer.experienceLevel;
                this.totalExperience = oldPlayer.totalExperience;
                this.experienceProgress = oldPlayer.experienceProgress;
                this.setScore(oldPlayer.getScore());
            }
        }
        this.enchantingTableSeed = oldPlayer.enchantingTableSeed;
        this.enderChestInventory = oldPlayer.enderChestInventory;
        this.getDataTracker().set(PLAYER_MODE_CUSTOMIZATION_ID, (Byte)oldPlayer.getDataTracker().get(PLAYER_MODE_CUSTOMIZATION_ID));
        this.syncedExperience = -1;
        this.syncedHealth = -1.0f;
        this.syncedFoodLevel = -1;
        this.recipeBook.copyFrom(oldPlayer.recipeBook);
        this.seenCredits = oldPlayer.seenCredits;
        this.enteredNetherPos = oldPlayer.enteredNetherPos;
        this.chunkFilter = oldPlayer.chunkFilter;
        this.subscribedTypes = oldPlayer.subscribedTypes;
        this.setLeftShoulderNbt(oldPlayer.getLeftShoulderNbt());
        this.setRightShoulderNbt(oldPlayer.getRightShoulderNbt());
        this.setLastDeathPos(oldPlayer.getLastDeathPos());
    }

    @Override
    protected void onStatusEffectApplied(StatusEffectInstance effect, @Nullable Entity source) {
        super.onStatusEffectApplied(effect, source);
        this.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(this.getId(), effect, true));
        if (effect.equals(StatusEffects.LEVITATION)) {
            this.levitationStartTick = this.age;
            this.levitationStartPos = this.getEntityPos();
        }
        Criteria.EFFECTS_CHANGED.trigger(this, source);
    }

    @Override
    protected void onStatusEffectUpgraded(StatusEffectInstance effect, boolean reapplyEffect, @Nullable Entity source) {
        super.onStatusEffectUpgraded(effect, reapplyEffect, source);
        this.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(this.getId(), effect, false));
        Criteria.EFFECTS_CHANGED.trigger(this, source);
    }

    @Override
    protected void onStatusEffectsRemoved(Collection<StatusEffectInstance> effects) {
        super.onStatusEffectsRemoved(effects);
        for (StatusEffectInstance lv : effects) {
            this.networkHandler.sendPacket(new RemoveEntityStatusEffectS2CPacket(this.getId(), lv.getEffectType()));
            if (!lv.equals(StatusEffects.LEVITATION)) continue;
            this.levitationStartPos = null;
        }
        Criteria.EFFECTS_CHANGED.trigger(this, (Entity)null);
    }

    @Override
    public void requestTeleport(double destX, double destY, double destZ) {
        this.networkHandler.requestTeleport(new EntityPosition(new Vec3d(destX, destY, destZ), Vec3d.ZERO, 0.0f, 0.0f), PositionFlag.combine(PositionFlag.DELTA, PositionFlag.ROT));
    }

    @Override
    public void requestTeleportOffset(double offsetX, double offsetY, double offsetZ) {
        this.networkHandler.requestTeleport(new EntityPosition(new Vec3d(offsetX, offsetY, offsetZ), Vec3d.ZERO, 0.0f, 0.0f), PositionFlag.VALUES);
    }

    @Override
    public boolean teleport(ServerWorld world, double destX, double destY, double destZ, Set<PositionFlag> flags, float yaw, float pitch, boolean resetCamera) {
        boolean bl2;
        if (this.isSleeping()) {
            this.wakeUp(true, true);
        }
        if (resetCamera) {
            this.setCameraEntity(this);
        }
        if (bl2 = super.teleport(world, destX, destY, destZ, flags, yaw, pitch, resetCamera)) {
            this.setHeadYaw(flags.contains((Object)PositionFlag.Y_ROT) ? this.getHeadYaw() + yaw : yaw);
        }
        return bl2;
    }

    @Override
    public void refreshPositionAfterTeleport(double x, double y, double z) {
        super.refreshPositionAfterTeleport(x, y, z);
        this.networkHandler.syncWithPlayerPosition();
    }

    @Override
    public void addCritParticles(Entity target) {
        this.getEntityWorld().getChunkManager().sendToNearbyPlayers(this, new EntityAnimationS2CPacket(target, EntityAnimationS2CPacket.CRIT));
    }

    @Override
    public void addEnchantedHitParticles(Entity target) {
        this.getEntityWorld().getChunkManager().sendToNearbyPlayers(this, new EntityAnimationS2CPacket(target, EntityAnimationS2CPacket.ENCHANTED_HIT));
    }

    @Override
    public void sendAbilitiesUpdate() {
        if (this.networkHandler == null) {
            return;
        }
        this.networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(this.getAbilities()));
        this.updatePotionVisibility();
    }

    @Override
    public ServerWorld getEntityWorld() {
        return (ServerWorld)super.getEntityWorld();
    }

    public boolean changeGameMode(GameMode gameMode) {
        boolean bl = this.isSpectator();
        if (!this.interactionManager.changeGameMode(gameMode)) {
            return false;
        }
        this.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_MODE_CHANGED, gameMode.getIndex()));
        if (gameMode == GameMode.SPECTATOR) {
            this.dropShoulderEntities();
            this.stopRiding();
            EnchantmentHelper.removeLocationBasedEffects(this);
        } else {
            this.setCameraEntity(this);
            if (bl) {
                EnchantmentHelper.applyLocationBasedEffects(this.getEntityWorld(), this);
            }
        }
        this.sendAbilitiesUpdate();
        this.markEffectsDirty();
        return true;
    }

    @Override
    @NotNull
    public GameMode getGameMode() {
        return this.interactionManager.getGameMode();
    }

    public CommandOutput getCommandOutput() {
        return this.commandOutput;
    }

    public ServerCommandSource getCommandSource() {
        return new ServerCommandSource(this.getCommandOutput(), this.getEntityPos(), this.getRotationClient(), this.getEntityWorld(), this.getPermissionLevel(), this.getStringifiedName(), this.getDisplayName(), this.server, this);
    }

    public void sendMessage(Text message) {
        this.sendMessageToClient(message, false);
    }

    public void sendMessageToClient(Text message, boolean overlay) {
        if (!this.acceptsMessage(overlay)) {
            return;
        }
        this.networkHandler.send(new GameMessageS2CPacket(message, overlay), PacketCallbacks.of(() -> {
            if (this.acceptsMessage(false)) {
                int i = 256;
                String string = message.asTruncatedString(256);
                MutableText lv = Text.literal(string).formatted(Formatting.YELLOW);
                return new GameMessageS2CPacket(Text.translatable("multiplayer.message_not_delivered", lv).formatted(Formatting.RED), false);
            }
            return null;
        }));
    }

    public void sendChatMessage(SentMessage message, boolean filterMaskEnabled, MessageType.Parameters params) {
        if (this.acceptsChatMessage()) {
            message.send(this, filterMaskEnabled, params);
        }
    }

    public String getIp() {
        SocketAddress socketAddress = this.networkHandler.getConnectionAddress();
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress)socketAddress;
            return InetAddresses.toAddrString(inetSocketAddress.getAddress());
        }
        return "<unknown>";
    }

    public void setClientOptions(SyncedClientOptions clientOptions) {
        this.language = clientOptions.language();
        this.viewDistance = clientOptions.viewDistance();
        this.clientChatVisibility = clientOptions.chatVisibility();
        this.clientChatColorsEnabled = clientOptions.chatColorsEnabled();
        this.filterText = clientOptions.filtersText();
        this.allowServerListing = clientOptions.allowsServerListing();
        this.particlesMode = clientOptions.particleStatus();
        this.getDataTracker().set(PLAYER_MODE_CUSTOMIZATION_ID, (byte)clientOptions.playerModelParts());
        this.getDataTracker().set(MAIN_ARM_ID, (byte)clientOptions.mainArm().getId());
    }

    public SyncedClientOptions getClientOptions() {
        byte i = (Byte)this.getDataTracker().get(PLAYER_MODE_CUSTOMIZATION_ID);
        Arm lv = Arm.BY_ID.apply(((Byte)this.getDataTracker().get(MAIN_ARM_ID)).byteValue());
        return new SyncedClientOptions(this.language, this.viewDistance, this.clientChatVisibility, this.clientChatColorsEnabled, i, lv, this.filterText, this.allowServerListing, this.particlesMode);
    }

    public boolean areClientChatColorsEnabled() {
        return this.clientChatColorsEnabled;
    }

    public ChatVisibility getClientChatVisibility() {
        return this.clientChatVisibility;
    }

    private boolean acceptsMessage(boolean overlay) {
        if (this.clientChatVisibility == ChatVisibility.HIDDEN) {
            return overlay;
        }
        return true;
    }

    private boolean acceptsChatMessage() {
        return this.clientChatVisibility == ChatVisibility.FULL;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public void sendServerMetadata(ServerMetadata metadata) {
        this.networkHandler.sendPacket(new ServerMetadataS2CPacket(metadata.description(), metadata.favicon().map(ServerMetadata.Favicon::iconBytes)));
    }

    @Override
    public int getPermissionLevel() {
        return this.server.getPermissionLevel(this.getPlayerConfigEntry());
    }

    public void updateLastActionTime() {
        this.lastActionTime = Util.getMeasuringTimeMs();
    }

    public ServerStatHandler getStatHandler() {
        return this.statHandler;
    }

    public ServerRecipeBook getRecipeBook() {
        return this.recipeBook;
    }

    @Override
    protected void updatePotionVisibility() {
        if (this.isSpectator()) {
            this.clearPotionSwirls();
            this.setInvisible(true);
        } else {
            super.updatePotionVisibility();
        }
    }

    public Entity getCameraEntity() {
        return this.cameraEntity == null ? this : this.cameraEntity;
    }

    public void setCameraEntity(@Nullable Entity entity) {
        Entity lv = this.getCameraEntity();
        Entity entity2 = this.cameraEntity = entity == null ? this : entity;
        if (lv != this.cameraEntity) {
            World world = this.cameraEntity.getEntityWorld();
            if (world instanceof ServerWorld) {
                ServerWorld lv2 = (ServerWorld)world;
                this.teleport(lv2, this.cameraEntity.getX(), this.cameraEntity.getY(), this.cameraEntity.getZ(), Set.of(), this.getYaw(), this.getPitch(), false);
            }
            if (entity != null) {
                this.getEntityWorld().getChunkManager().updatePosition(this);
            }
            this.networkHandler.sendPacket(new SetCameraEntityS2CPacket(this.cameraEntity));
            this.networkHandler.syncWithPlayerPosition();
        }
    }

    @Override
    protected void tickPortalCooldown() {
        if (!this.inTeleportationState) {
            super.tickPortalCooldown();
        }
    }

    @Override
    public void attack(Entity target) {
        if (this.isSpectator()) {
            this.setCameraEntity(target);
        } else {
            super.attack(target);
        }
    }

    public long getLastActionTime() {
        return this.lastActionTime;
    }

    @Nullable
    public Text getPlayerListName() {
        return null;
    }

    public int getPlayerListOrder() {
        return 0;
    }

    @Override
    public void swingHand(Hand hand) {
        super.swingHand(hand);
        this.resetLastAttackedTicks();
    }

    public boolean isInTeleportationState() {
        return this.inTeleportationState;
    }

    public void onTeleportationDone() {
        this.inTeleportationState = false;
    }

    public PlayerAdvancementTracker getAdvancementTracker() {
        return this.advancementTracker;
    }

    @Nullable
    public Respawn getRespawn() {
        return this.respawn;
    }

    public void setSpawnPointFrom(ServerPlayerEntity player) {
        this.setSpawnPoint(player.respawn, false);
    }

    public void setSpawnPoint(@Nullable Respawn respawn, boolean sendMessage) {
        if (sendMessage && respawn != null && !respawn.posEquals(this.respawn)) {
            this.sendMessage(SET_SPAWN_TEXT);
        }
        this.respawn = respawn;
    }

    public ChunkSectionPos getWatchedSection() {
        return this.watchedSection;
    }

    public void setWatchedSection(ChunkSectionPos section) {
        this.watchedSection = section;
    }

    public ChunkFilter getChunkFilter() {
        return this.chunkFilter;
    }

    public void setChunkFilter(ChunkFilter chunkFilter) {
        this.chunkFilter = chunkFilter;
    }

    @Override
    public void playSoundToPlayer(SoundEvent sound, SoundCategory category, float volume, float pitch) {
        this.networkHandler.sendPacket(new PlaySoundS2CPacket(Registries.SOUND_EVENT.getEntry(sound), category, this.getX(), this.getY(), this.getZ(), volume, pitch, this.random.nextLong()));
    }

    @Override
    public ItemEntity dropItem(ItemStack stack, boolean dropAtSelf, boolean retainOwnership) {
        ItemEntity lv = super.dropItem(stack, dropAtSelf, retainOwnership);
        if (retainOwnership) {
            ItemStack lv2;
            ItemStack itemStack = lv2 = lv != null ? lv.getStack() : ItemStack.EMPTY;
            if (!lv2.isEmpty()) {
                this.increaseStat(Stats.DROPPED.getOrCreateStat(lv2.getItem()), stack.getCount());
                this.incrementStat(Stats.DROP);
            }
        }
        return lv;
    }

    public TextStream getTextStream() {
        return this.textStream;
    }

    public void setServerWorld(ServerWorld world) {
        this.setWorld(world);
        this.interactionManager.setWorld(world);
    }

    @Nullable
    private static GameMode gameModeFromData(ReadView view, String key) {
        return view.read(key, GameMode.INDEX_CODEC).orElse(null);
    }

    private GameMode getServerGameMode(@Nullable GameMode backupGameMode) {
        GameMode lv = this.server.getForcedGameMode();
        if (lv != null) {
            return lv;
        }
        return backupGameMode != null ? backupGameMode : this.server.getDefaultGameMode();
    }

    private void writeGameModeData(WriteView view) {
        view.put("playerGameType", GameMode.INDEX_CODEC, this.interactionManager.getGameMode());
        GameMode lv = this.interactionManager.getPreviousGameMode();
        view.putNullable("previousPlayerGameType", GameMode.INDEX_CODEC, lv);
    }

    @Override
    public boolean shouldFilterText() {
        return this.filterText;
    }

    public boolean shouldFilterMessagesSentTo(ServerPlayerEntity player) {
        if (player == this) {
            return false;
        }
        return this.filterText || player.filterText;
    }

    @Override
    public boolean canModifyAt(ServerWorld world, BlockPos pos) {
        return super.canModifyAt(world, pos) && world.canEntityModifyAt(this, pos);
    }

    @Override
    protected void tickItemStackUsage(ItemStack stack) {
        Criteria.USING_ITEM.trigger(this, stack);
        super.tickItemStackUsage(stack);
    }

    public boolean dropSelectedItem(boolean entireStack) {
        PlayerInventory lv = this.getInventory();
        ItemStack lv2 = lv.dropSelectedItem(entireStack);
        this.currentScreenHandler.getSlotIndex(lv, lv.getSelectedSlot()).ifPresent(index -> this.currentScreenHandler.setReceivedStack(index, lv.getSelectedStack()));
        return this.dropItem(lv2, false, true) != null;
    }

    @Override
    public void giveOrDropStack(ItemStack stack) {
        if (!this.getInventory().insertStack(stack)) {
            this.dropItem(stack, false);
        }
    }

    public boolean allowsServerListing() {
        return this.allowServerListing;
    }

    @Override
    public Optional<SculkShriekerWarningManager> getSculkShriekerWarningManager() {
        return Optional.of(this.sculkShriekerWarningManager);
    }

    public void setSpawnExtraParticlesOnFall(boolean spawnExtraParticlesOnFall) {
        this.spawnExtraParticlesOnFall = spawnExtraParticlesOnFall;
    }

    @Override
    public void triggerItemPickedUpByEntityCriteria(ItemEntity item) {
        super.triggerItemPickedUpByEntityCriteria(item);
        Entity lv = item.getOwner();
        if (lv != null) {
            Criteria.THROWN_ITEM_PICKED_UP_BY_PLAYER.trigger(this, item.getStack(), lv);
        }
    }

    public void setSession(PublicPlayerSession session) {
        this.session = session;
    }

    @Nullable
    public PublicPlayerSession getSession() {
        if (this.session != null && this.session.isKeyExpired()) {
            return null;
        }
        return this.session;
    }

    @Override
    public void tiltScreen(double deltaX, double deltaZ) {
        this.damageTiltYaw = (float)(MathHelper.atan2(deltaZ, deltaX) * 57.2957763671875 - (double)this.getYaw());
        this.networkHandler.sendPacket(new DamageTiltS2CPacket(this));
    }

    @Override
    public boolean startRiding(Entity entity, boolean force, boolean emitEvent) {
        if (super.startRiding(entity, force, emitEvent)) {
            entity.updatePassengerPosition(this);
            this.networkHandler.requestTeleport(new EntityPosition(this.getEntityPos(), Vec3d.ZERO, 0.0f, 0.0f), PositionFlag.ROT);
            if (entity instanceof LivingEntity) {
                LivingEntity lv = (LivingEntity)entity;
                this.server.getPlayerManager().sendStatusEffects(lv, this.networkHandler);
            }
            this.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(entity));
            return true;
        }
        return false;
    }

    @Override
    public void dismountVehicle() {
        Entity lv = this.getVehicle();
        super.dismountVehicle();
        if (lv instanceof LivingEntity) {
            LivingEntity lv2 = (LivingEntity)lv;
            for (StatusEffectInstance lv3 : lv2.getStatusEffects()) {
                this.networkHandler.sendPacket(new RemoveEntityStatusEffectS2CPacket(lv.getId(), lv3.getEffectType()));
            }
        }
        if (lv != null) {
            this.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(lv));
        }
    }

    public CommonPlayerSpawnInfo createCommonPlayerSpawnInfo(ServerWorld world) {
        return new CommonPlayerSpawnInfo(world.getDimensionEntry(), world.getRegistryKey(), BiomeAccess.hashSeed(world.getSeed()), this.interactionManager.getGameMode(), this.interactionManager.getPreviousGameMode(), world.isDebugWorld(), world.isFlat(), this.getLastDeathPos(), this.getPortalCooldown(), world.getSeaLevel());
    }

    public void setStartRaidPos(BlockPos startRaidPos) {
        this.startRaidPos = startRaidPos;
    }

    public void clearStartRaidPos() {
        this.startRaidPos = null;
    }

    @Nullable
    public BlockPos getStartRaidPos() {
        return this.startRaidPos;
    }

    @Override
    public Vec3d getMovement() {
        Entity lv = this.getVehicle();
        if (lv != null && lv.getControllingPassenger() != this) {
            return lv.getMovement();
        }
        return this.movement;
    }

    public void setMovement(Vec3d movement) {
        this.movement = movement;
    }

    @Override
    protected float getDamageAgainst(Entity target, float baseDamage, DamageSource damageSource) {
        return EnchantmentHelper.getDamage(this.getEntityWorld(), this.getWeaponStack(), target, damageSource, baseDamage);
    }

    @Override
    public void sendEquipmentBreakStatus(Item item, EquipmentSlot slot) {
        super.sendEquipmentBreakStatus(item, slot);
        this.incrementStat(Stats.BROKEN.getOrCreateStat(item));
    }

    public PlayerInput getPlayerInput() {
        return this.playerInput;
    }

    public void setPlayerInput(PlayerInput playerInput) {
        this.playerInput = playerInput;
    }

    public Vec3d getInputVelocityForMinecart() {
        float f;
        float f2 = this.playerInput.left() == this.playerInput.right() ? 0.0f : (f = this.playerInput.left() ? 1.0f : -1.0f);
        float g = this.playerInput.forward() == this.playerInput.backward() ? 0.0f : (this.playerInput.forward() ? 1.0f : -1.0f);
        return ServerPlayerEntity.movementInputToVelocity(new Vec3d(f, 0.0, g), 1.0f, this.getYaw());
    }

    public void addEnderPearl(EnderPearlEntity enderPearl) {
        this.enderPearls.add(enderPearl);
    }

    public void removeEnderPearl(EnderPearlEntity enderPearl) {
        this.enderPearls.remove(enderPearl);
    }

    public Set<EnderPearlEntity> getEnderPearls() {
        return this.enderPearls;
    }

    public NbtCompound getLeftShoulderNbt() {
        return this.leftShoulderNbt;
    }

    protected void setLeftShoulderNbt(NbtCompound leftShoulderNbt) {
        this.leftShoulderNbt = leftShoulderNbt;
        this.setLeftShoulderParrotVariant(ServerPlayerEntity.readParrotVariant(leftShoulderNbt));
    }

    public NbtCompound getRightShoulderNbt() {
        return this.rightShoulderNbt;
    }

    protected void setRightShoulderNbt(NbtCompound rightShoulderNbt) {
        this.rightShoulderNbt = rightShoulderNbt;
        this.setRightShoulderParrotVariant(ServerPlayerEntity.readParrotVariant(rightShoulderNbt));
    }

    public long handleThrownEnderPearl(EnderPearlEntity enderPearl) {
        World world = enderPearl.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            ChunkPos lv2 = enderPearl.getChunkPos();
            this.addEnderPearl(enderPearl);
            lv.resetIdleTimeout();
            return ServerPlayerEntity.addEnderPearlTicket(lv, lv2) - 1L;
        }
        return 0L;
    }

    public static long addEnderPearlTicket(ServerWorld world, ChunkPos chunkPos) {
        world.getChunkManager().addTicket(ChunkTicketType.ENDER_PEARL, chunkPos, 2);
        return ChunkTicketType.ENDER_PEARL.expiryTicks();
    }

    public void setSubscribedTypes(Set<DebugSubscriptionType<?>> subscribedTypes) {
        this.subscribedTypes = Set.copyOf(subscribedTypes);
    }

    public Set<DebugSubscriptionType<?>> getSubscribedTypes() {
        if (!this.server.getSubscriberTracker().canSubscribe(this)) {
            return Set.of();
        }
        return this.subscribedTypes;
    }

    @Override
    public /* synthetic */ World getEntityWorld() {
        return this.getEntityWorld();
    }

    @Override
    @Nullable
    public /* synthetic */ Entity teleportTo(TeleportTarget teleportTarget) {
        return this.teleportTo(teleportTarget);
    }

    public record Respawn(WorldProperties.SpawnPoint respawnData, boolean forced) {
        public static final Codec<Respawn> CODEC = RecordCodecBuilder.create(instance -> instance.group(WorldProperties.SpawnPoint.MAP_CODEC.forGetter(Respawn::respawnData), Codec.BOOL.optionalFieldOf("forced", false).forGetter(Respawn::forced)).apply((Applicative<Respawn, ?>)instance, Respawn::new));

        static RegistryKey<World> getDimension(@Nullable Respawn respawn) {
            return respawn != null ? respawn.respawnData().getDimension() : World.OVERWORLD;
        }

        public boolean posEquals(@Nullable Respawn respawn) {
            return respawn != null && this.respawnData.globalPos().equals(respawn.respawnData.globalPos());
        }
    }

    record RespawnPos(Vec3d pos, float yaw, float pitch) {
        public static RespawnPos fromCurrentPos(Vec3d respawnPos, BlockPos currentPos, float f) {
            return new RespawnPos(respawnPos, RespawnPos.getYaw(respawnPos, currentPos), f);
        }

        private static float getYaw(Vec3d respawnPos, BlockPos currentPos) {
            Vec3d lv = Vec3d.ofBottomCenter(currentPos).subtract(respawnPos).normalize();
            return (float)MathHelper.wrapDegrees(MathHelper.atan2(lv.z, lv.x) * 57.2957763671875 - 90.0);
        }
    }

    public record SavePos(Optional<RegistryKey<World>> dimension, Optional<Vec3d> position, Optional<Vec2f> rotation) {
        public static final MapCodec<SavePos> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(World.CODEC.optionalFieldOf(ServerPlayerEntity.DIMENSION_KEY).forGetter(SavePos::dimension), Vec3d.CODEC.optionalFieldOf("Pos").forGetter(SavePos::position), Vec2f.CODEC.optionalFieldOf("Rotation").forGetter(SavePos::rotation)).apply((Applicative<SavePos, ?>)instance, SavePos::new));
        public static final SavePos EMPTY = new SavePos(Optional.empty(), Optional.empty(), Optional.empty());
    }
}

