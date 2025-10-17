/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model;

import com.mojang.logging.LogUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.ItemAsset;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.MissingItemModel;
import net.minecraft.client.render.model.BakedGeometry;
import net.minecraft.client.render.model.BakedSimpleModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.ErrorCollectingSpriteGetter;
import net.minecraft.client.render.model.GeometryBakedModel;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.ModelSettings;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.SimpleBlockStateModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.PlayerSkinCache;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.thread.AsyncHelper;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ModelBaker {
    public static final SpriteIdentifier FIRE_0 = TexturedRenderLayers.BLOCK_SPRITE_MAPPER.mapVanilla("fire_0");
    public static final SpriteIdentifier FIRE_1 = TexturedRenderLayers.BLOCK_SPRITE_MAPPER.mapVanilla("fire_1");
    public static final SpriteIdentifier LAVA_FLOW = TexturedRenderLayers.BLOCK_SPRITE_MAPPER.mapVanilla("lava_flow");
    public static final SpriteIdentifier WATER_FLOW = TexturedRenderLayers.BLOCK_SPRITE_MAPPER.mapVanilla("water_flow");
    public static final SpriteIdentifier WATER_OVERLAY = TexturedRenderLayers.BLOCK_SPRITE_MAPPER.mapVanilla("water_overlay");
    public static final SpriteIdentifier BANNER_BASE = new SpriteIdentifier(TexturedRenderLayers.BANNER_PATTERNS_ATLAS_TEXTURE, Identifier.ofVanilla("entity/banner_base"));
    public static final SpriteIdentifier SHIELD_BASE = new SpriteIdentifier(TexturedRenderLayers.SHIELD_PATTERNS_ATLAS_TEXTURE, Identifier.ofVanilla("entity/shield_base"));
    public static final SpriteIdentifier SHIELD_BASE_NO_PATTERN = new SpriteIdentifier(TexturedRenderLayers.SHIELD_PATTERNS_ATLAS_TEXTURE, Identifier.ofVanilla("entity/shield_base_nopattern"));
    public static final int MAX_BLOCK_DESTRUCTION_STAGE = 10;
    public static final List<Identifier> BLOCK_DESTRUCTION_STAGES = IntStream.range(0, 10).mapToObj(stage -> Identifier.ofVanilla("block/destroy_stage_" + stage)).collect(Collectors.toList());
    public static final List<Identifier> BLOCK_DESTRUCTION_STAGE_TEXTURES = BLOCK_DESTRUCTION_STAGES.stream().map(id -> id.withPath(path -> "textures/" + path + ".png")).collect(Collectors.toList());
    public static final List<RenderLayer> BLOCK_DESTRUCTION_RENDER_LAYERS = BLOCK_DESTRUCTION_STAGE_TEXTURES.stream().map(RenderLayer::getBlockBreaking).collect(Collectors.toList());
    static final Logger LOGGER = LogUtils.getLogger();
    private final LoadedEntityModels entityModels;
    private final SpriteHolder field_61869;
    private final PlayerSkinCache field_62265;
    private final Map<BlockState, BlockStateModel.UnbakedGrouped> blockModels;
    private final Map<Identifier, ItemAsset> itemAssets;
    final Map<Identifier, BakedSimpleModel> simpleModels;
    final BakedSimpleModel missingModel;

    public ModelBaker(LoadedEntityModels entityModels, SpriteHolder arg2, PlayerSkinCache arg3, Map<BlockState, BlockStateModel.UnbakedGrouped> map, Map<Identifier, ItemAsset> map2, Map<Identifier, BakedSimpleModel> map3, BakedSimpleModel arg4) {
        this.entityModels = entityModels;
        this.field_61869 = arg2;
        this.field_62265 = arg3;
        this.blockModels = map;
        this.itemAssets = map2;
        this.simpleModels = map3;
        this.missingModel = arg4;
    }

    public CompletableFuture<BakedModels> bake(ErrorCollectingSpriteGetter spriteGetter, Executor executor) {
        BlockItemModels lv = BlockItemModels.bake(this.missingModel, spriteGetter);
        BakerImpl lv2 = new BakerImpl(spriteGetter);
        CompletableFuture<Map<BlockState, BlockStateModel>> completableFuture = AsyncHelper.mapValues(this.blockModels, (state, unbaked) -> {
            try {
                return unbaked.bake((BlockState)state, lv2);
            } catch (Exception exception) {
                LOGGER.warn("Unable to bake model: '{}': {}", state, (Object)exception);
                return null;
            }
        }, executor);
        CompletableFuture<Map<Identifier, ItemModel>> completableFuture2 = AsyncHelper.mapValues(this.itemAssets, (state, asset) -> {
            try {
                return asset.model().bake(new ItemModel.BakeContext(lv2, this.entityModels, this.field_61869, this.field_62265, arg2.item, asset.registrySwapper()));
            } catch (Exception exception) {
                LOGGER.warn("Unable to bake item model: '{}'", state, (Object)exception);
                return null;
            }
        }, executor);
        HashMap map = new HashMap(this.itemAssets.size());
        this.itemAssets.forEach((id, asset) -> {
            ItemAsset.Properties lv = asset.properties();
            if (!lv.equals(ItemAsset.Properties.DEFAULT)) {
                map.put(id, lv);
            }
        });
        return completableFuture.thenCombine(completableFuture2, (blockStateModels, itemModels) -> new BakedModels(lv, (Map<BlockState, BlockStateModel>)blockStateModels, (Map<Identifier, ItemModel>)itemModels, map));
    }

    @Environment(value=EnvType.CLIENT)
    public record BlockItemModels(BlockStateModel block, ItemModel item) {
        public static BlockItemModels bake(BakedSimpleModel model, final ErrorCollectingSpriteGetter arg2) {
            Baker lv = new Baker(){

                @Override
                public BakedSimpleModel getModel(Identifier id) {
                    throw new IllegalStateException("Missing model can't have dependencies, but asked for " + String.valueOf(id));
                }

                @Override
                public <T> T compute(Baker.ResolvableCacheKey<T> key) {
                    return key.compute(this);
                }

                @Override
                public ErrorCollectingSpriteGetter getSpriteGetter() {
                    return arg2;
                }
            };
            ModelTextures lv2 = model.getTextures();
            boolean bl = model.getAmbientOcclusion();
            boolean bl2 = model.getGuiLight().isSide();
            ModelTransformation lv3 = model.getTransformations();
            BakedGeometry lv4 = model.bakeGeometry(lv2, lv, ModelRotation.X0_Y0);
            Sprite lv5 = model.getParticleTexture(lv2, lv);
            SimpleBlockStateModel lv6 = new SimpleBlockStateModel(new GeometryBakedModel(lv4, bl, lv5));
            MissingItemModel lv7 = new MissingItemModel(lv4.getAllQuads(), new ModelSettings(bl2, lv5, lv3));
            return new BlockItemModels(lv6, lv7);
        }
    }

    @Environment(value=EnvType.CLIENT)
    class BakerImpl
    implements Baker {
        private final ErrorCollectingSpriteGetter spriteGetter;
        private final Map<Baker.ResolvableCacheKey<Object>, Object> cache = new ConcurrentHashMap<Baker.ResolvableCacheKey<Object>, Object>();
        private final Function<Baker.ResolvableCacheKey<Object>, Object> cacheValueFunction = key -> key.compute(this);

        BakerImpl(ErrorCollectingSpriteGetter spriteGetter) {
            this.spriteGetter = spriteGetter;
        }

        @Override
        public ErrorCollectingSpriteGetter getSpriteGetter() {
            return this.spriteGetter;
        }

        @Override
        public BakedSimpleModel getModel(Identifier id) {
            BakedSimpleModel lv = ModelBaker.this.simpleModels.get(id);
            if (lv == null) {
                LOGGER.warn("Requested a model that was not discovered previously: {}", (Object)id);
                return ModelBaker.this.missingModel;
            }
            return lv;
        }

        @Override
        public <T> T compute(Baker.ResolvableCacheKey<T> key) {
            return (T)this.cache.computeIfAbsent(key, this.cacheValueFunction);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record BakedModels(BlockItemModels missingModels, Map<BlockState, BlockStateModel> blockStateModels, Map<Identifier, ItemModel> itemStackModels, Map<Identifier, ItemAsset.Properties> itemProperties) {
    }
}

