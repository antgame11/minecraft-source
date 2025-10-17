/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.item.ItemAsset;
import net.minecraft.client.item.ItemAssetsLoader;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.block.entity.LoadedBlockEntityModels;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.model.BakedSimpleModel;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.BlockStatesLoader;
import net.minecraft.client.render.model.ErrorCollectingSpriteGetter;
import net.minecraft.client.render.model.MissingModel;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.render.model.ModelGrouper;
import net.minecraft.client.render.model.ReferencedModelsCollector;
import net.minecraft.client.render.model.SimpleModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.GeneratedItemModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.AtlasManager;
import net.minecraft.client.texture.PlayerSkinCache;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Atlases;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.util.profiler.ScopedProfiler;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class BakedModelManager
implements ResourceReloader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceFinder MODELS_FINDER = ResourceFinder.json("models");
    private Map<Identifier, ItemModel> bakedItemModels = Map.of();
    private Map<Identifier, ItemAsset.Properties> itemProperties = Map.of();
    private final AtlasManager field_61870;
    private final PlayerSkinCache field_62266;
    private final BlockModels blockModelCache;
    private final BlockColors colorMap;
    private LoadedEntityModels entityModels = LoadedEntityModels.EMPTY;
    private LoadedBlockEntityModels blockEntityModels = LoadedBlockEntityModels.EMPTY;
    private ModelBaker.BlockItemModels missingModels;
    private Object2IntMap<BlockState> modelGroups = Object2IntMaps.emptyMap();

    public BakedModelManager(BlockColors arg, AtlasManager arg2, PlayerSkinCache arg3) {
        this.colorMap = arg;
        this.field_61870 = arg2;
        this.field_62266 = arg3;
        this.blockModelCache = new BlockModels(this);
    }

    public BlockStateModel getMissingModel() {
        return this.missingModels.block();
    }

    public ItemModel getItemModel(Identifier id) {
        return this.bakedItemModels.getOrDefault(id, this.missingModels.item());
    }

    public ItemAsset.Properties getItemProperties(Identifier id) {
        return this.itemProperties.getOrDefault(id, ItemAsset.Properties.DEFAULT);
    }

    public BlockModels getBlockModels() {
        return this.blockModelCache;
    }

    @Override
    public final CompletableFuture<Void> reload(ResourceReloader.Store store, Executor prepareExecutor, ResourceReloader.Synchronizer reloadSynchronizer, Executor applyExecutor) {
        ResourceManager lv = store.getResourceManager();
        CompletableFuture<LoadedEntityModels> completableFuture = CompletableFuture.supplyAsync(LoadedEntityModels::copy, prepareExecutor);
        CompletionStage completableFuture2 = completableFuture.thenApplyAsync(arg -> LoadedBlockEntityModels.fromModels(new SpecialModelRenderer.BakeContext.Simple((LoadedEntityModels)arg, this.field_61870, this.field_62266)), prepareExecutor);
        CompletableFuture<Map<Identifier, UnbakedModel>> completableFuture3 = BakedModelManager.reloadModels(lv, prepareExecutor);
        CompletableFuture<BlockStatesLoader.LoadedModels> completableFuture4 = BlockStatesLoader.load(lv, prepareExecutor);
        CompletableFuture<ItemAssetsLoader.Result> completableFuture5 = ItemAssetsLoader.load(lv, prepareExecutor);
        CompletionStage completableFuture6 = CompletableFuture.allOf(completableFuture3, completableFuture4, completableFuture5).thenApplyAsync(async -> BakedModelManager.collect((Map)completableFuture3.join(), (BlockStatesLoader.LoadedModels)completableFuture4.join(), (ItemAssetsLoader.Result)completableFuture5.join()), prepareExecutor);
        CompletionStage completableFuture7 = completableFuture4.thenApplyAsync(definition -> BakedModelManager.group(this.colorMap, definition), prepareExecutor);
        CompletableFuture<SpriteLoader.StitchResult> completableFuture8 = store.getOrThrow(AtlasManager.stitchKey).getPreparations(Atlases.BLOCKS);
        return ((CompletableFuture)((CompletableFuture)CompletableFuture.allOf(new CompletableFuture[]{completableFuture8, completableFuture6, completableFuture7, completableFuture4, completableFuture5, completableFuture, completableFuture2, completableFuture3}).thenComposeAsync(void_ -> {
            SpriteLoader.StitchResult lv = (SpriteLoader.StitchResult)completableFuture8.join();
            Models lv2 = (Models)((CompletableFuture)completableFuture6).join();
            Object2IntMap object2IntMap = (Object2IntMap)((CompletableFuture)completableFuture7).join();
            Sets.SetView set = Sets.difference(((Map)completableFuture3.join()).keySet(), lv2.models.keySet());
            if (!set.isEmpty()) {
                LOGGER.debug("Unreferenced models: \n{}", (Object)set.stream().sorted().map(id -> "\t" + String.valueOf(id) + "\n").collect(Collectors.joining()));
            }
            ModelBaker lv3 = new ModelBaker((LoadedEntityModels)completableFuture.join(), this.field_61870, this.field_62266, ((BlockStatesLoader.LoadedModels)completableFuture4.join()).models(), ((ItemAssetsLoader.Result)completableFuture5.join()).contents(), lv2.models(), lv2.missing());
            return BakedModelManager.bake(lv, lv3, object2IntMap, (LoadedEntityModels)completableFuture.join(), (LoadedBlockEntityModels)((CompletableFuture)completableFuture2).join(), prepareExecutor);
        }, prepareExecutor)).thenCompose(reloadSynchronizer::whenPrepared)).thenAcceptAsync(this::upload, applyExecutor);
    }

    private static CompletableFuture<Map<Identifier, UnbakedModel>> reloadModels(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> MODELS_FINDER.findResources(resourceManager), executor).thenCompose(models2 -> {
            ArrayList<CompletableFuture<Pair>> list = new ArrayList<CompletableFuture<Pair>>(models2.size());
            for (Map.Entry entry : models2.entrySet()) {
                list.add(CompletableFuture.supplyAsync(() -> {
                    Pair<Identifier, JsonUnbakedModel> pair;
                    block8: {
                        Identifier lv = MODELS_FINDER.toResourceId((Identifier)entry.getKey());
                        BufferedReader reader = ((Resource)entry.getValue()).getReader();
                        try {
                            pair = Pair.of(lv, JsonUnbakedModel.deserialize(reader));
                            if (reader == null) break block8;
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
                            } catch (Exception exception) {
                                LOGGER.error("Failed to load model {}", entry.getKey(), (Object)exception);
                                return null;
                            }
                        }
                        ((Reader)reader).close();
                    }
                    return pair;
                }, executor));
            }
            return Util.combineSafe(list).thenApply(models -> models.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond)));
        });
    }

    private static Models collect(Map<Identifier, UnbakedModel> modelMap, BlockStatesLoader.LoadedModels stateDefinition, ItemAssetsLoader.Result result) {
        try (ScopedProfiler lv = Profilers.get().scoped("dependencies");){
            ReferencedModelsCollector lv2 = new ReferencedModelsCollector(modelMap, MissingModel.create());
            lv2.addSpecialModel(GeneratedItemModel.GENERATED, new GeneratedItemModel());
            stateDefinition.models().values().forEach(lv2::resolve);
            result.contents().values().forEach(asset -> lv2.resolve(asset.model()));
            Models models = new Models(lv2.getMissingModel(), lv2.collectModels());
            return models;
        }
    }

    private static CompletableFuture<BakingResult> bake(final SpriteLoader.StitchResult arg, ModelBaker baker, Object2IntMap<BlockState> blockStates, LoadedEntityModels entityModels, LoadedBlockEntityModels blockEntityModels, Executor executor) {
        final Multimap multimap = Multimaps.synchronizedMultimap(HashMultimap.create());
        final Multimap multimap2 = Multimaps.synchronizedMultimap(HashMultimap.create());
        return baker.bake(new ErrorCollectingSpriteGetter(){
            private final Sprite missingSprite;
            {
                this.missingSprite = arg.missing();
            }

            @Override
            public Sprite get(SpriteIdentifier id, SimpleModel model) {
                Sprite lv;
                if (id.getAtlasId().equals(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE) && (lv = arg.getSprite(id.getTextureId())) != null) {
                    return lv;
                }
                multimap.put(model.name(), id);
                return this.missingSprite;
            }

            @Override
            public Sprite getMissing(String name, SimpleModel model) {
                multimap2.put(model.name(), name);
                return this.missingSprite;
            }
        }, executor).thenApply(arg3 -> {
            multimap.asMap().forEach((modelName, sprites) -> LOGGER.warn("Missing textures in model {}:\n{}", modelName, (Object)sprites.stream().sorted(SpriteIdentifier.COMPARATOR).map(spriteId -> "    " + String.valueOf(spriteId.getAtlasId()) + ":" + String.valueOf(spriteId.getTextureId())).collect(Collectors.joining("\n"))));
            multimap2.asMap().forEach((modelName, textureIds) -> LOGGER.warn("Missing texture references in model {}:\n{}", modelName, (Object)textureIds.stream().sorted().map(string -> "    " + string).collect(Collectors.joining("\n"))));
            Map<BlockState, BlockStateModel> map = BakedModelManager.toStateMap(arg3.blockStateModels(), arg3.missingModels().block());
            return new BakingResult((ModelBaker.BakedModels)arg3, blockStates, map, entityModels, blockEntityModels);
        });
    }

    private static Map<BlockState, BlockStateModel> toStateMap(Map<BlockState, BlockStateModel> blockStateModels, BlockStateModel missingModel) {
        try (ScopedProfiler lv = Profilers.get().scoped("block state dispatch");){
            IdentityHashMap<BlockState, BlockStateModel> map2 = new IdentityHashMap<BlockState, BlockStateModel>(blockStateModels);
            for (Block lv2 : Registries.BLOCK) {
                lv2.getStateManager().getStates().forEach(state -> {
                    if (blockStateModels.putIfAbsent((BlockState)state, missingModel) == null) {
                        LOGGER.warn("Missing model for variant: '{}'", state);
                    }
                });
            }
            IdentityHashMap<BlockState, BlockStateModel> identityHashMap = map2;
            return identityHashMap;
        }
    }

    private static Object2IntMap<BlockState> group(BlockColors colors, BlockStatesLoader.LoadedModels definition) {
        try (ScopedProfiler lv = Profilers.get().scoped("block groups");){
            Object2IntMap<BlockState> object2IntMap = ModelGrouper.group(colors, definition);
            return object2IntMap;
        }
    }

    private void upload(BakingResult bakingResult) {
        ModelBaker.BakedModels lv = bakingResult.bakedModels;
        this.bakedItemModels = lv.itemStackModels();
        this.itemProperties = lv.itemProperties();
        this.modelGroups = bakingResult.modelGroups;
        this.missingModels = lv.missingModels();
        this.blockModelCache.setModels(bakingResult.modelCache);
        this.blockEntityModels = bakingResult.specialBlockModelRenderer;
        this.entityModels = bakingResult.entityModelSet;
    }

    public boolean shouldRerender(BlockState from, BlockState to) {
        int j;
        if (from == to) {
            return false;
        }
        int i = this.modelGroups.getInt(from);
        if (i != -1 && i == (j = this.modelGroups.getInt(to))) {
            FluidState lv2;
            FluidState lv = from.getFluidState();
            return lv != (lv2 = to.getFluidState());
        }
        return true;
    }

    public Supplier<LoadedBlockEntityModels> getBlockEntityModelsSupplier() {
        return () -> this.blockEntityModels;
    }

    public Supplier<LoadedEntityModels> getEntityModelsSupplier() {
        return () -> this.entityModels;
    }

    @Environment(value=EnvType.CLIENT)
    record Models(BakedSimpleModel missing, Map<Identifier, BakedSimpleModel> models) {
    }

    @Environment(value=EnvType.CLIENT)
    record BakingResult(ModelBaker.BakedModels bakedModels, Object2IntMap<BlockState> modelGroups, Map<BlockState, BlockStateModel> modelCache, LoadedEntityModels entityModelSet, LoadedBlockEntityModels specialBlockModelRenderer) {
    }
}

