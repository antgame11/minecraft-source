/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import org.jetbrains.annotations.Nullable;

public class DataCommandStorage {
    private static final String COMMAND_STORAGE_PREFIX = "command_storage_";
    private final Map<String, PersistentState> storages = new HashMap<String, PersistentState>();
    private final PersistentStateManager stateManager;

    public DataCommandStorage(PersistentStateManager stateManager) {
        this.stateManager = stateManager;
    }

    public NbtCompound get(Identifier id) {
        PersistentState lv = this.getStorage(id.getNamespace());
        if (lv != null) {
            return lv.get(id.getPath());
        }
        return new NbtCompound();
    }

    @Nullable
    private PersistentState getStorage(String namespace) {
        PersistentState lv = this.storages.get(namespace);
        if (lv != null) {
            return lv;
        }
        PersistentState lv2 = this.stateManager.get(PersistentState.createStateType(namespace));
        if (lv2 != null) {
            this.storages.put(namespace, lv2);
        }
        return lv2;
    }

    private PersistentState getOrCreateStorage(String namespace) {
        PersistentState lv = this.storages.get(namespace);
        if (lv != null) {
            return lv;
        }
        PersistentState lv2 = this.stateManager.getOrCreate(PersistentState.createStateType(namespace));
        this.storages.put(namespace, lv2);
        return lv2;
    }

    public void set(Identifier id, NbtCompound nbt) {
        this.getOrCreateStorage(id.getNamespace()).set(id.getPath(), nbt);
    }

    public Stream<Identifier> getIds() {
        return this.storages.entrySet().stream().flatMap(entry -> ((PersistentState)entry.getValue()).getIds((String)entry.getKey()));
    }

    static String getSaveKey(String namespace) {
        return COMMAND_STORAGE_PREFIX + namespace;
    }

    static class PersistentState
    extends net.minecraft.world.PersistentState {
        public static final Codec<PersistentState> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.unboundedMap(Codecs.IDENTIFIER_PATH, NbtCompound.CODEC).fieldOf("contents")).forGetter(state -> state.map)).apply((Applicative<PersistentState, ?>)instance, PersistentState::new));
        private final Map<String, NbtCompound> map;

        private PersistentState(Map<String, NbtCompound> map) {
            this.map = new HashMap<String, NbtCompound>(map);
        }

        private PersistentState() {
            this(new HashMap<String, NbtCompound>());
        }

        public static PersistentStateType<PersistentState> createStateType(String id) {
            return new PersistentStateType<PersistentState>(DataCommandStorage.getSaveKey(id), PersistentState::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
        }

        public NbtCompound get(String name) {
            NbtCompound lv = this.map.get(name);
            return lv != null ? lv : new NbtCompound();
        }

        public void set(String name, NbtCompound nbt) {
            if (nbt.isEmpty()) {
                this.map.remove(name);
            } else {
                this.map.put(name, nbt);
            }
            this.markDirty();
        }

        public Stream<Identifier> getIds(String namespace) {
            return this.map.keySet().stream().map(key -> Identifier.of(namespace, key));
        }
    }
}

