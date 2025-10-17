/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gl;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.GlImportProcessor;
import net.minecraft.client.gl.PostEffectPipeline;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.ProjectionMatrix2;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.path.PathUtil;
import net.minecraft.util.profiler.Profiler;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ShaderLoader
extends SinglePreparationResourceReloader<Definitions>
implements AutoCloseable {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final int field_53936 = 32768;
    public static final String SHADERS_PATH = "shaders";
    private static final String INCLUDE_PATH = "shaders/include/";
    private static final ResourceFinder POST_EFFECT_FINDER = ResourceFinder.json("post_effect");
    final TextureManager textureManager;
    private final Consumer<Exception> onError;
    private Cache cache = new Cache(Definitions.EMPTY);
    final ProjectionMatrix2 projectionMatrix = new ProjectionMatrix2("post", 0.1f, 1000.0f, false);

    public ShaderLoader(TextureManager textureManager, Consumer<Exception> onError) {
        this.textureManager = textureManager;
        this.onError = onError;
    }

    @Override
    protected Definitions prepare(ResourceManager arg, Profiler arg2) {
        ImmutableMap.Builder<ShaderSourceKey, String> builder = ImmutableMap.builder();
        Map<Identifier, Resource> map = arg.findResources(SHADERS_PATH, ShaderLoader::isShaderSource);
        for (Map.Entry<Identifier, Resource> entry : map.entrySet()) {
            Identifier lv = entry.getKey();
            ShaderType shaderType = ShaderType.byLocation(lv);
            if (shaderType == null) continue;
            ShaderLoader.loadShaderSource(lv, entry.getValue(), shaderType, map, builder);
        }
        ImmutableMap.Builder<Identifier, PostEffectPipeline> builder2 = ImmutableMap.builder();
        for (Map.Entry<Identifier, Resource> entry2 : POST_EFFECT_FINDER.findResources(arg).entrySet()) {
            ShaderLoader.loadPostEffect(entry2.getKey(), entry2.getValue(), builder2);
        }
        return new Definitions(builder.build(), builder2.build());
    }

    private static void loadShaderSource(Identifier id, Resource resource, ShaderType type, Map<Identifier, Resource> allResources, ImmutableMap.Builder<ShaderSourceKey, String> builder) {
        Identifier lv = type.idConverter().toResourceId(id);
        GlImportProcessor lv2 = ShaderLoader.createImportProcessor(allResources, id);
        try (BufferedReader reader = resource.getReader();){
            String string = IOUtils.toString(reader);
            builder.put(new ShaderSourceKey(lv, type), String.join((CharSequence)"", lv2.readSource(string)));
        } catch (IOException iOException) {
            LOGGER.error("Failed to load shader source at {}", (Object)id, (Object)iOException);
        }
    }

    private static GlImportProcessor createImportProcessor(final Map<Identifier, Resource> allResources, Identifier id) {
        final Identifier lv = id.withPath(PathUtil::getPosixFullPath);
        return new GlImportProcessor(){
            private final Set<Identifier> processed = new ObjectArraySet<Identifier>();

            @Override
            public String loadImport(boolean inline, String name) {
                String string;
                block11: {
                    Identifier lv3;
                    try {
                        lv3 = inline ? lv.withPath(path -> PathUtil.normalizeToPosix(path + name)) : Identifier.of(name).withPrefixedPath(ShaderLoader.INCLUDE_PATH);
                    } catch (InvalidIdentifierException lv2) {
                        LOGGER.error("Malformed GLSL import {}: {}", (Object)name, (Object)lv2.getMessage());
                        return "#error " + lv2.getMessage();
                    }
                    if (!this.processed.add(lv3)) {
                        return null;
                    }
                    BufferedReader reader = ((Resource)allResources.get(lv3)).getReader();
                    try {
                        string = IOUtils.toString(reader);
                        if (reader == null) break block11;
                    } catch (Throwable throwable) {
                        try {
                            if (reader != null) {
                                try {
                                    ((Reader)reader).close();
                                } catch (Throwable throwable2) {
                                    throwable.addSuppressed(throwable2);
                                }
                            }
                            throw throwable;
                        } catch (IOException iOException) {
                            LOGGER.error("Could not open GLSL import {}: {}", (Object)lv3, (Object)iOException.getMessage());
                            return "#error " + iOException.getMessage();
                        }
                    }
                    ((Reader)reader).close();
                }
                return string;
            }
        };
    }

    private static void loadPostEffect(Identifier id, Resource resource, ImmutableMap.Builder<Identifier, PostEffectPipeline> builder) {
        Identifier lv = POST_EFFECT_FINDER.toResourceId(id);
        try (BufferedReader reader = resource.getReader();){
            JsonElement jsonElement = StrictJsonParser.parse(reader);
            builder.put(lv, (PostEffectPipeline)PostEffectPipeline.CODEC.parse(JsonOps.INSTANCE, jsonElement).getOrThrow(JsonSyntaxException::new));
        } catch (JsonParseException | IOException exception) {
            LOGGER.error("Failed to parse post chain at {}", (Object)id, (Object)exception);
        }
    }

    private static boolean isShaderSource(Identifier id) {
        return ShaderType.byLocation(id) != null || id.getPath().endsWith(".glsl");
    }

    @Override
    protected void apply(Definitions arg, ResourceManager arg2, Profiler arg3) {
        Cache lv = new Cache(arg);
        HashSet<RenderPipeline> set = new HashSet<RenderPipeline>(RenderPipelines.getAll());
        ArrayList<Identifier> list = new ArrayList<Identifier>();
        GpuDevice gpuDevice = RenderSystem.getDevice();
        gpuDevice.clearPipelineCache();
        for (RenderPipeline renderPipeline : set) {
            CompiledRenderPipeline compiledRenderPipeline = gpuDevice.precompilePipeline(renderPipeline, lv::getSource);
            if (compiledRenderPipeline.isValid()) continue;
            list.add(renderPipeline.getLocation());
        }
        if (!list.isEmpty()) {
            gpuDevice.clearPipelineCache();
            throw new RuntimeException("Failed to load required shader programs:\n" + list.stream().map(id -> " - " + String.valueOf(id)).collect(Collectors.joining("\n")));
        }
        this.cache.close();
        this.cache = lv;
    }

    @Override
    public String getName() {
        return "Shader Loader";
    }

    private void handleError(Exception exception) {
        if (this.cache.errorHandled) {
            return;
        }
        this.onError.accept(exception);
        this.cache.errorHandled = true;
    }

    @Nullable
    public PostEffectProcessor loadPostEffect(Identifier id, Set<Identifier> availableExternalTargets) {
        try {
            return this.cache.getOrLoadProcessor(id, availableExternalTargets);
        } catch (LoadException lv) {
            LOGGER.error("Failed to load post chain: {}", (Object)id, (Object)lv);
            this.cache.postEffectProcessors.put(id, Optional.empty());
            this.handleError(lv);
            return null;
        }
    }

    @Override
    public void close() {
        this.cache.close();
        this.projectionMatrix.close();
    }

    public String getSource(Identifier id, ShaderType type) {
        return this.cache.getSource(id, type);
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager manager, Profiler profiler) {
        return this.prepare(manager, profiler);
    }

    @Environment(value=EnvType.CLIENT)
    class Cache
    implements AutoCloseable {
        private final Definitions definitions;
        final Map<Identifier, Optional<PostEffectProcessor>> postEffectProcessors = new HashMap<Identifier, Optional<PostEffectProcessor>>();
        boolean errorHandled;

        Cache(Definitions definitions) {
            this.definitions = definitions;
        }

        @Nullable
        public PostEffectProcessor getOrLoadProcessor(Identifier id, Set<Identifier> availableExternalTargets) throws LoadException {
            Optional<PostEffectProcessor> optional = this.postEffectProcessors.get(id);
            if (optional != null) {
                return optional.orElse(null);
            }
            PostEffectProcessor lv = this.loadProcessor(id, availableExternalTargets);
            this.postEffectProcessors.put(id, Optional.of(lv));
            return lv;
        }

        private PostEffectProcessor loadProcessor(Identifier id, Set<Identifier> availableExternalTargets) throws LoadException {
            PostEffectPipeline lv = this.definitions.postChains.get(id);
            if (lv == null) {
                throw new LoadException("Could not find post chain with id: " + String.valueOf(id));
            }
            return PostEffectProcessor.parseEffect(lv, ShaderLoader.this.textureManager, availableExternalTargets, id, ShaderLoader.this.projectionMatrix);
        }

        @Override
        public void close() {
            this.postEffectProcessors.values().forEach(processor -> processor.ifPresent(PostEffectProcessor::close));
            this.postEffectProcessors.clear();
        }

        public String getSource(Identifier id, ShaderType type) {
            return this.definitions.shaderSources.get(new ShaderSourceKey(id, type));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Definitions(Map<ShaderSourceKey, String> shaderSources, Map<Identifier, PostEffectPipeline> postChains) {
        public static final Definitions EMPTY = new Definitions(Map.of(), Map.of());
    }

    @Environment(value=EnvType.CLIENT)
    record ShaderSourceKey(Identifier id, ShaderType type) {
        @Override
        public String toString() {
            return String.valueOf(this.id) + " (" + String.valueOf((Object)this.type) + ")";
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class LoadException
    extends Exception {
        public LoadException(String message) {
            super(message);
        }
    }
}

