/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;

public interface TestEnvironmentDefinition {
    public static final Codec<TestEnvironmentDefinition> CODEC = Registries.TEST_ENVIRONMENT_DEFINITION_TYPE.getCodec().dispatch(TestEnvironmentDefinition::getCodec, codec -> codec);
    public static final Codec<RegistryEntry<TestEnvironmentDefinition>> ENTRY_CODEC = RegistryElementCodec.of(RegistryKeys.TEST_ENVIRONMENT, CODEC);

    public static MapCodec<? extends TestEnvironmentDefinition> registerAndGetDefault(Registry<MapCodec<? extends TestEnvironmentDefinition>> registry) {
        Registry.register(registry, "all_of", AllOf.CODEC);
        Registry.register(registry, "game_rules", GameRules.CODEC);
        Registry.register(registry, "time_of_day", TimeOfDay.CODEC);
        Registry.register(registry, "weather", Weather.CODEC);
        return Registry.register(registry, "function", Function.CODEC);
    }

    public void setup(ServerWorld var1);

    default public void teardown(ServerWorld world) {
    }

    public MapCodec<? extends TestEnvironmentDefinition> getCodec();

    public record AllOf(List<RegistryEntry<TestEnvironmentDefinition>> definitions) implements TestEnvironmentDefinition
    {
        public static final MapCodec<AllOf> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)ENTRY_CODEC.listOf().fieldOf("definitions")).forGetter(AllOf::definitions)).apply((Applicative<AllOf, ?>)instance, AllOf::new));

        public AllOf(TestEnvironmentDefinition ... definitionTypes) {
            this(Arrays.stream(definitionTypes).map(RegistryEntry::of).toList());
        }

        @Override
        public void setup(ServerWorld world) {
            this.definitions.forEach(definition -> ((TestEnvironmentDefinition)definition.value()).setup(world));
        }

        @Override
        public void teardown(ServerWorld world) {
            this.definitions.forEach(definition -> ((TestEnvironmentDefinition)definition.value()).teardown(world));
        }

        public MapCodec<AllOf> getCodec() {
            return CODEC;
        }
    }

    public record GameRules(List<RuleValue<Boolean, GameRules.BooleanRule>> boolRules, List<RuleValue<Integer, GameRules.IntRule>> intRules) implements TestEnvironmentDefinition
    {
        public static final MapCodec<GameRules> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)RuleValue.createCodec(GameRules.BooleanRule.class, Codec.BOOL).listOf().fieldOf("bool_rules")).forGetter(GameRules::boolRules), ((MapCodec)RuleValue.createCodec(GameRules.IntRule.class, Codec.INT).listOf().fieldOf("int_rules")).forGetter(GameRules::intRules)).apply((Applicative<GameRules, ?>)instance, GameRules::new));

        @Override
        public void setup(ServerWorld world) {
            net.minecraft.world.GameRules lv = world.getGameRules();
            MinecraftServer minecraftServer = world.getServer();
            for (RuleValue<Boolean, GameRules.BooleanRule> ruleValue : this.boolRules) {
                lv.get(ruleValue.key()).set(ruleValue.value(), minecraftServer);
            }
            for (RuleValue<Comparable<Boolean>, GameRules.Rule> ruleValue : this.intRules) {
                ((GameRules.IntRule)lv.get(ruleValue.key())).set((Integer)ruleValue.value(), minecraftServer);
            }
        }

        @Override
        public void teardown(ServerWorld world) {
            net.minecraft.world.GameRules lv = world.getGameRules();
            MinecraftServer minecraftServer = world.getServer();
            for (RuleValue<Boolean, GameRules.BooleanRule> ruleValue : this.boolRules) {
                lv.get(ruleValue.key()).setValue(net.minecraft.world.GameRules.getRuleType(ruleValue.key()).createRule(), minecraftServer);
            }
            for (RuleValue<Comparable<Boolean>, GameRules.Rule> ruleValue : this.intRules) {
                ((GameRules.IntRule)lv.get(ruleValue.key())).setValue((GameRules.IntRule)net.minecraft.world.GameRules.getRuleType(ruleValue.key()).createRule(), minecraftServer);
            }
        }

        public MapCodec<GameRules> getCodec() {
            return CODEC;
        }

        public static <S, T extends GameRules.Rule<T>> RuleValue<S, T> ruleValue(GameRules.Key<T> key, S value) {
            return new RuleValue<S, T>(key, value);
        }

        public record RuleValue<S, T extends GameRules.Rule<T>>(GameRules.Key<T> key, S value) {
            public static <S, T extends GameRules.Rule<T>> Codec<RuleValue<S, T>> createCodec(Class<T> ruleClass, Codec<S> valueCodec) {
                return RecordCodecBuilder.create(instance -> instance.group(((MapCodec)net.minecraft.world.GameRules.createKeyCodec(ruleClass).fieldOf("rule")).forGetter(RuleValue::key), ((MapCodec)valueCodec.fieldOf("value")).forGetter(RuleValue::value)).apply((Applicative<RuleValue, ?>)instance, RuleValue::new));
            }
        }
    }

    public record TimeOfDay(int time) implements TestEnvironmentDefinition
    {
        public static final MapCodec<TimeOfDay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codecs.NON_NEGATIVE_INT.fieldOf("time")).forGetter(TimeOfDay::time)).apply((Applicative<TimeOfDay, ?>)instance, TimeOfDay::new));

        @Override
        public void setup(ServerWorld world) {
            world.setTimeOfDay(this.time);
        }

        public MapCodec<TimeOfDay> getCodec() {
            return CODEC;
        }
    }

    public record Weather(State weather) implements TestEnvironmentDefinition
    {
        public static final MapCodec<Weather> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)State.CODEC.fieldOf("weather")).forGetter(Weather::weather)).apply((Applicative<Weather, ?>)instance, Weather::new));

        @Override
        public void setup(ServerWorld world) {
            this.weather.apply(world);
        }

        @Override
        public void teardown(ServerWorld world) {
            world.resetWeather();
        }

        public MapCodec<Weather> getCodec() {
            return CODEC;
        }

        public static enum State implements StringIdentifiable
        {
            CLEAR("clear", 100000, 0, false, false),
            RAIN("rain", 0, 100000, true, false),
            THUNDER("thunder", 0, 100000, true, true);

            public static final Codec<State> CODEC;
            private final String name;
            private final int clearDuration;
            private final int rainDuration;
            private final boolean raining;
            private final boolean thundering;

            private State(String name, int clearDuration, int rainDuration, boolean raining, boolean thundering) {
                this.name = name;
                this.clearDuration = clearDuration;
                this.rainDuration = rainDuration;
                this.raining = raining;
                this.thundering = thundering;
            }

            void apply(ServerWorld world) {
                world.setWeather(this.clearDuration, this.rainDuration, this.raining, this.thundering);
            }

            @Override
            public String asString() {
                return this.name;
            }

            static {
                CODEC = StringIdentifiable.createCodec(State::values);
            }
        }
    }

    public record Function(Optional<Identifier> setupFunction, Optional<Identifier> teardownFunction) implements TestEnvironmentDefinition
    {
        private static final Logger LOGGER = LogUtils.getLogger();
        public static final MapCodec<Function> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Identifier.CODEC.optionalFieldOf("setup").forGetter(Function::setupFunction), Identifier.CODEC.optionalFieldOf("teardown").forGetter(Function::teardownFunction)).apply((Applicative<Function, ?>)instance, Function::new));

        @Override
        public void setup(ServerWorld world) {
            this.setupFunction.ifPresent(functionId -> Function.executeFunction(world, functionId));
        }

        @Override
        public void teardown(ServerWorld world) {
            this.teardownFunction.ifPresent(functionId -> Function.executeFunction(world, functionId));
        }

        private static void executeFunction(ServerWorld world, Identifier functionId) {
            MinecraftServer minecraftServer = world.getServer();
            CommandFunctionManager lv = minecraftServer.getCommandFunctionManager();
            Optional<CommandFunction<ServerCommandSource>> optional = lv.getFunction(functionId);
            if (optional.isPresent()) {
                ServerCommandSource lv2 = minecraftServer.getCommandSource().withLevel(2).withSilent().withWorld(world);
                lv.execute(optional.get(), lv2);
            } else {
                LOGGER.error("Test Batch failed for non-existent function {}", (Object)functionId);
            }
        }

        public MapCodec<Function> getCodec() {
            return CODEC;
        }
    }
}

