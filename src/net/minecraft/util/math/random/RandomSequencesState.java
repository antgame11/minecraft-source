/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math.random;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSequence;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

public class RandomSequencesState
extends PersistentState {
    public static final PersistentStateType<RandomSequencesState> STATE_TYPE = new PersistentStateType<RandomSequencesState>("random_sequences", state -> new RandomSequencesState(state.worldSeed()), state -> RandomSequencesState.createCodec(state.worldSeed()), DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES);
    private final long seed;
    private int salt;
    private boolean includeWorldSeed = true;
    private boolean includeSequenceId = true;
    private final Map<Identifier, RandomSequence> sequences = new Object2ObjectOpenHashMap<Identifier, RandomSequence>();

    public RandomSequencesState(long seed) {
        this.seed = seed;
    }

    private RandomSequencesState(long seed, int salt, boolean includeWorldSeed, boolean includeSequenceId, Map<Identifier, RandomSequence> sequences) {
        this.seed = seed;
        this.salt = salt;
        this.includeWorldSeed = includeWorldSeed;
        this.includeSequenceId = includeSequenceId;
        this.sequences.putAll(sequences);
    }

    public static Codec<RandomSequencesState> createCodec(long seed) {
        return RecordCodecBuilder.create(instance -> instance.group(RecordCodecBuilder.point(seed), ((MapCodec)Codec.INT.fieldOf("salt")).forGetter(state -> state.salt), Codec.BOOL.optionalFieldOf("include_world_seed", true).forGetter(state -> state.includeWorldSeed), Codec.BOOL.optionalFieldOf("include_sequence_id", true).forGetter(state -> state.includeSequenceId), ((MapCodec)Codec.unboundedMap(Identifier.CODEC, RandomSequence.CODEC).fieldOf("sequences")).forGetter(state -> state.sequences)).apply((Applicative<RandomSequencesState, ?>)instance, RandomSequencesState::new));
    }

    public Random getOrCreate(Identifier id) {
        Random lv = this.sequences.computeIfAbsent(id, this::createSequence).getSource();
        return new WrappedRandom(lv);
    }

    private RandomSequence createSequence(Identifier id) {
        return this.createSequence(id, this.salt, this.includeWorldSeed, this.includeSequenceId);
    }

    private RandomSequence createSequence(Identifier id, int salt, boolean includeWorldSeed, boolean includeSequenceId) {
        long l = (includeWorldSeed ? this.seed : 0L) ^ (long)salt;
        return new RandomSequence(l, includeSequenceId ? Optional.of(id) : Optional.empty());
    }

    public void forEachSequence(BiConsumer<Identifier, RandomSequence> consumer) {
        this.sequences.forEach(consumer);
    }

    public void setDefaultParameters(int salt, boolean includeWorldSeed, boolean includeSequenceId) {
        this.salt = salt;
        this.includeWorldSeed = includeWorldSeed;
        this.includeSequenceId = includeSequenceId;
    }

    public int resetAll() {
        int i = this.sequences.size();
        this.sequences.clear();
        return i;
    }

    public void reset(Identifier id) {
        this.sequences.put(id, this.createSequence(id));
    }

    public void reset(Identifier id, int salt, boolean includeWorldSeed, boolean includeSequenceId) {
        this.sequences.put(id, this.createSequence(id, salt, includeWorldSeed, includeSequenceId));
    }

    class WrappedRandom
    implements Random {
        private final Random random;

        WrappedRandom(Random random) {
            this.random = random;
        }

        @Override
        public Random split() {
            RandomSequencesState.this.markDirty();
            return this.random.split();
        }

        @Override
        public RandomSplitter nextSplitter() {
            RandomSequencesState.this.markDirty();
            return this.random.nextSplitter();
        }

        @Override
        public void setSeed(long seed) {
            RandomSequencesState.this.markDirty();
            this.random.setSeed(seed);
        }

        @Override
        public int nextInt() {
            RandomSequencesState.this.markDirty();
            return this.random.nextInt();
        }

        @Override
        public int nextInt(int bound) {
            RandomSequencesState.this.markDirty();
            return this.random.nextInt(bound);
        }

        @Override
        public long nextLong() {
            RandomSequencesState.this.markDirty();
            return this.random.nextLong();
        }

        @Override
        public boolean nextBoolean() {
            RandomSequencesState.this.markDirty();
            return this.random.nextBoolean();
        }

        @Override
        public float nextFloat() {
            RandomSequencesState.this.markDirty();
            return this.random.nextFloat();
        }

        @Override
        public double nextDouble() {
            RandomSequencesState.this.markDirty();
            return this.random.nextDouble();
        }

        @Override
        public double nextGaussian() {
            RandomSequencesState.this.markDirty();
            return this.random.nextGaussian();
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof WrappedRandom) {
                WrappedRandom lv = (WrappedRandom)o;
                return this.random.equals(lv.random);
            }
            return false;
        }
    }
}

