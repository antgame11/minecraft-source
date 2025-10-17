/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gl;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.lang.runtime.SwitchBootstraps;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.UniformValue;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

@Environment(value=EnvType.CLIENT)
public record PostEffectPipeline(Map<Identifier, Targets> internalTargets, List<Pass> passes) {
    public static final Codec<PostEffectPipeline> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.unboundedMap(Identifier.CODEC, Targets.CODEC).optionalFieldOf("targets", Map.of()).forGetter(PostEffectPipeline::internalTargets), Pass.CODEC.listOf().optionalFieldOf("passes", List.of()).forGetter(PostEffectPipeline::passes)).apply((Applicative<PostEffectPipeline, ?>)instance, PostEffectPipeline::new));

    @Environment(value=EnvType.CLIENT)
    public record Targets(Optional<Integer> width, Optional<Integer> height, boolean persistent, int clearColor) {
        public static final Codec<Targets> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codecs.POSITIVE_INT.optionalFieldOf("width").forGetter(Targets::width), Codecs.POSITIVE_INT.optionalFieldOf("height").forGetter(Targets::height), Codec.BOOL.optionalFieldOf("persistent", false).forGetter(Targets::persistent), Codecs.ARGB.optionalFieldOf("clear_color", 0).forGetter(Targets::clearColor)).apply((Applicative<Targets, ?>)instance, Targets::new));
    }

    @Environment(value=EnvType.CLIENT)
    public record Pass(Identifier vertexShaderId, Identifier fragmentShaderId, List<Input> inputs, Identifier outputTarget, Map<String, List<UniformValue>> uniforms) {
        private static final Codec<List<Input>> INPUTS_CODEC = Input.CODEC.listOf().validate(inputs -> {
            ObjectArraySet set = new ObjectArraySet(inputs.size());
            for (Input lv : inputs) {
                if (set.add(lv.samplerName())) continue;
                return DataResult.error(() -> "Encountered repeated sampler name: " + lv.samplerName());
            }
            return DataResult.success(inputs);
        });
        private static final Codec<Map<String, List<UniformValue>>> UNIFORMS_CODEC = Codec.unboundedMap(Codec.STRING, UniformValue.CODEC.listOf());
        public static final Codec<Pass> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("vertex_shader")).forGetter(Pass::vertexShaderId), ((MapCodec)Identifier.CODEC.fieldOf("fragment_shader")).forGetter(Pass::fragmentShaderId), INPUTS_CODEC.optionalFieldOf("inputs", List.of()).forGetter(Pass::inputs), ((MapCodec)Identifier.CODEC.fieldOf("output")).forGetter(Pass::outputTarget), UNIFORMS_CODEC.optionalFieldOf("uniforms", Map.of()).forGetter(Pass::uniforms)).apply((Applicative<Pass, ?>)instance, Pass::new));

        public Stream<Identifier> streamTargets() {
            Stream stream = this.inputs.stream().flatMap(input -> input.getTargetId().stream());
            return Stream.concat(stream, Stream.of(this.outputTarget));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record TargetSampler(String samplerName, Identifier targetId, boolean useDepthBuffer, boolean bilinear) implements Input
    {
        public static final Codec<TargetSampler> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("sampler_name")).forGetter(TargetSampler::samplerName), ((MapCodec)Identifier.CODEC.fieldOf("target")).forGetter(TargetSampler::targetId), Codec.BOOL.optionalFieldOf("use_depth_buffer", false).forGetter(TargetSampler::useDepthBuffer), Codec.BOOL.optionalFieldOf("bilinear", false).forGetter(TargetSampler::bilinear)).apply((Applicative<TargetSampler, ?>)instance, TargetSampler::new));

        @Override
        public Set<Identifier> getTargetId() {
            return Set.of(this.targetId);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record TextureSampler(String samplerName, Identifier location, int width, int height, boolean bilinear) implements Input
    {
        public static final Codec<TextureSampler> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("sampler_name")).forGetter(TextureSampler::samplerName), ((MapCodec)Identifier.CODEC.fieldOf("location")).forGetter(TextureSampler::location), ((MapCodec)Codecs.POSITIVE_INT.fieldOf("width")).forGetter(TextureSampler::width), ((MapCodec)Codecs.POSITIVE_INT.fieldOf("height")).forGetter(TextureSampler::height), Codec.BOOL.optionalFieldOf("bilinear", false).forGetter(TextureSampler::bilinear)).apply((Applicative<TextureSampler, ?>)instance, TextureSampler::new));

        @Override
        public Set<Identifier> getTargetId() {
            return Set.of();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static sealed interface Input
    permits TextureSampler, TargetSampler {
        public static final Codec<Input> CODEC = Codec.xor(TextureSampler.CODEC, TargetSampler.CODEC).xmap(either -> (Input)either.map(Function.identity(), Function.identity()), sampler -> {
            Input input = sampler;
            Objects.requireNonNull(input);
            Input lv = input;
            int i = 0;
            return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{TextureSampler.class, TargetSampler.class}, (Object)lv, i)) {
                default -> throw new MatchException(null, null);
                case 0 -> {
                    TextureSampler lv2 = (TextureSampler)lv;
                    yield Either.left(lv2);
                }
                case 1 -> {
                    TargetSampler lv3 = (TargetSampler)lv;
                    yield Either.right(lv3);
                }
            };
        });

        public String samplerName();

        public Set<Identifier> getTargetId();
    }
}

