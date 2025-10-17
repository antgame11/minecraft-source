/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.network;

import com.mojang.logging.LogUtils;
import java.lang.runtime.SwitchBootstraps;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.SpawnLocating;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.chunk.ChunkLoadProgress;
import net.minecraft.world.chunk.ChunkLoadingCounter;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class PrepareSpawnTask
implements ServerPlayerConfigurationTask {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final ServerPlayerConfigurationTask.Key KEY = new ServerPlayerConfigurationTask.Key("prepare_spawn");
    public static final int CHUNK_LOAD_RADIUS = 3;
    final MinecraftServer server;
    final PlayerConfigEntry player;
    final ChunkLoadProgress chunkLoadProgress;
    @Nullable
    private Stage stage;

    public PrepareSpawnTask(MinecraftServer server, PlayerConfigEntry player) {
        this.server = server;
        this.player = player;
        this.chunkLoadProgress = server.getChunkLoadProgress();
    }

    @Override
    public void sendPacket(Consumer<Packet<?>> sender) {
        try (ErrorReporter.Logging lv = new ErrorReporter.Logging(LOGGER);){
            Optional<ReadView> optional = this.server.getPlayerManager().loadPlayerData(this.player).map(nbt -> NbtReadView.create(lv, this.server.getRegistryManager(), nbt));
            ServerPlayerEntity.SavePos lv2 = optional.flatMap(view -> view.read(ServerPlayerEntity.SavePos.CODEC)).orElse(ServerPlayerEntity.SavePos.EMPTY);
            WorldProperties.SpawnPoint lv3 = this.server.getSaveProperties().getMainWorldProperties().getSpawnPoint();
            ServerWorld lv4 = lv2.dimension().map(this.server::getWorld).orElseGet(() -> {
                ServerWorld lv = this.server.getWorld(lv3.getDimension());
                return lv != null ? lv : this.server.getOverworld();
            });
            CompletableFuture completableFuture = lv2.position().map(CompletableFuture::completedFuture).orElseGet(() -> SpawnLocating.locateSpawnPos(lv4, lv3.getPos()));
            Vec2f lv5 = lv2.rotation().orElse(new Vec2f(lv3.yaw(), lv3.pitch()));
            this.stage = new LoadPlayerChunks(lv4, completableFuture, lv5);
        }
    }

    @Override
    public boolean hasFinished() {
        Stage stage = this.stage;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{LoadPlayerChunks.class, PlayerSpawn.class}, (Object)stage, n)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                LoadPlayerChunks lv = (LoadPlayerChunks)stage;
                PlayerSpawn lv2 = lv.tryFinish();
                if (lv2 != null) {
                    this.stage = lv2;
                    yield true;
                }
                yield false;
            }
            case 1 -> {
                PlayerSpawn lv2 = (PlayerSpawn)stage;
                yield true;
            }
            case -1 -> false;
        };
    }

    public ServerPlayerEntity onReady(ClientConnection connection, ConnectedClientData clientData) {
        Stage stage = this.stage;
        if (stage instanceof PlayerSpawn) {
            PlayerSpawn lv = (PlayerSpawn)stage;
            return lv.onReady(connection, clientData);
        }
        throw new IllegalStateException("Player spawn was not ready");
    }

    public void tick() {
        Stage stage = this.stage;
        if (stage instanceof PlayerSpawn) {
            PlayerSpawn lv = (PlayerSpawn)stage;
            lv.tick();
        }
    }

    public void onDisconnected() {
        Stage stage = this.stage;
        if (stage instanceof LoadPlayerChunks) {
            LoadPlayerChunks lv = (LoadPlayerChunks)stage;
            lv.cancel();
        }
        this.stage = null;
    }

    @Override
    public ServerPlayerConfigurationTask.Key getKey() {
        return KEY;
    }

    final class LoadPlayerChunks
    implements Stage {
        private final ServerWorld world;
        private final CompletableFuture<Vec3d> spawnPos;
        private final Vec2f rotation;
        @Nullable
        private CompletableFuture<?> chunkLoadingFuture;
        private final ChunkLoadingCounter chunkCounter = new ChunkLoadingCounter();

        LoadPlayerChunks(ServerWorld world, CompletableFuture<Vec3d> spawnPos, Vec2f rotation) {
            this.world = world;
            this.spawnPos = spawnPos;
            this.rotation = rotation;
        }

        public void cancel() {
            this.spawnPos.cancel(false);
        }

        @Nullable
        public PlayerSpawn tryFinish() {
            if (!this.spawnPos.isDone()) {
                return null;
            }
            Vec3d lv = this.spawnPos.join();
            if (this.chunkLoadingFuture == null) {
                ChunkPos lv2 = new ChunkPos(BlockPos.ofFloored(lv));
                this.chunkCounter.load(this.world, () -> {
                    this.chunkLoadingFuture = this.world.getChunkManager().addChunkLoadingTicket(ChunkTicketType.PLAYER_SPAWN, lv2, 3);
                });
                PrepareSpawnTask.this.chunkLoadProgress.init(ChunkLoadProgress.Stage.LOAD_PLAYER_CHUNKS, this.chunkCounter.getTotalChunks());
                PrepareSpawnTask.this.chunkLoadProgress.initSpawnPos(this.world.getRegistryKey(), lv2);
            }
            PrepareSpawnTask.this.chunkLoadProgress.progress(ChunkLoadProgress.Stage.LOAD_PLAYER_CHUNKS, this.chunkCounter.getFullChunks(), this.chunkCounter.getTotalChunks());
            if (!this.chunkLoadingFuture.isDone()) {
                return null;
            }
            PrepareSpawnTask.this.chunkLoadProgress.finish(ChunkLoadProgress.Stage.LOAD_PLAYER_CHUNKS);
            return new PlayerSpawn(this.world, lv, this.rotation);
        }
    }

    static sealed interface Stage
    permits LoadPlayerChunks, PlayerSpawn {
    }

    final class PlayerSpawn
    implements Stage {
        private final ServerWorld world;
        private final Vec3d spawnPos;
        private final Vec2f rotation;

        PlayerSpawn(ServerWorld world, Vec3d spawnPos, Vec2f rotation) {
            this.world = world;
            this.spawnPos = spawnPos;
            this.rotation = rotation;
        }

        public void tick() {
            this.world.getChunkManager().addTicket(ChunkTicketType.PLAYER_SPAWN, new ChunkPos(BlockPos.ofFloored(this.spawnPos)), 3);
        }

        public ServerPlayerEntity onReady(ClientConnection connection, ConnectedClientData clientData) {
            ChunkPos lv = new ChunkPos(BlockPos.ofFloored(this.spawnPos));
            this.world.loadChunks(lv, 3);
            ServerPlayerEntity lv2 = new ServerPlayerEntity(PrepareSpawnTask.this.server, this.world, clientData.gameProfile(), clientData.syncedOptions());
            try (ErrorReporter.Logging lv3 = new ErrorReporter.Logging(lv2.getErrorReporterContext(), LOGGER);){
                Optional<ReadView> optional = PrepareSpawnTask.this.server.getPlayerManager().loadPlayerData(PrepareSpawnTask.this.player).map(playerData -> NbtReadView.create(lv3, PrepareSpawnTask.this.server.getRegistryManager(), playerData));
                optional.ifPresent(lv2::readData);
                lv2.refreshPositionAndAngles(this.spawnPos, this.rotation.x, this.rotation.y);
                PrepareSpawnTask.this.server.getPlayerManager().onPlayerConnect(connection, lv2, clientData);
                optional.ifPresent(playerData -> {
                    lv2.readEnderPearls((ReadView)playerData);
                    lv2.readRootVehicle((ReadView)playerData);
                });
                ServerPlayerEntity serverPlayerEntity = lv2;
                return serverPlayerEntity;
            }
        }
    }
}

