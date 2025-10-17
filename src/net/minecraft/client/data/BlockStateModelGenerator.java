/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.data;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChiseledBookshelfBlock;
import net.minecraft.block.CopperGolemStatueBlock;
import net.minecraft.block.CrafterBlock;
import net.minecraft.block.CreakingHeartBlock;
import net.minecraft.block.DriedGhastBlock;
import net.minecraft.block.HangingMossBlock;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.LightBlock;
import net.minecraft.block.MultifaceBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.PaleMossCarpetBlock;
import net.minecraft.block.PitcherCropBlock;
import net.minecraft.block.PropaguleBlock;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.SnifferEggBlock;
import net.minecraft.block.TestBlock;
import net.minecraft.block.VaultBlock;
import net.minecraft.block.enums.Attachment;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.ComparatorMode;
import net.minecraft.block.enums.CreakingHeartState;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.Orientation;
import net.minecraft.block.enums.PistonType;
import net.minecraft.block.enums.RailShape;
import net.minecraft.block.enums.SculkSensorPhase;
import net.minecraft.block.enums.SideChainPart;
import net.minecraft.block.enums.SlabType;
import net.minecraft.block.enums.StairShape;
import net.minecraft.block.enums.TestBlockMode;
import net.minecraft.block.enums.Thickness;
import net.minecraft.block.enums.Tilt;
import net.minecraft.block.enums.TrialSpawnerState;
import net.minecraft.block.enums.VaultState;
import net.minecraft.block.enums.WallShape;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.client.data.BlockModelDefinitionCreator;
import net.minecraft.client.data.BlockStateVariantMap;
import net.minecraft.client.data.ItemModelOutput;
import net.minecraft.client.data.ItemModels;
import net.minecraft.client.data.Model;
import net.minecraft.client.data.ModelIds;
import net.minecraft.client.data.ModelSupplier;
import net.minecraft.client.data.Models;
import net.minecraft.client.data.MultipartBlockModelDefinitionCreator;
import net.minecraft.client.data.TextureKey;
import net.minecraft.client.data.TextureMap;
import net.minecraft.client.data.TexturedModel;
import net.minecraft.client.data.VariantsBlockModelDefinitionCreator;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.special.BannerModelRenderer;
import net.minecraft.client.render.item.model.special.BedModelRenderer;
import net.minecraft.client.render.item.model.special.ChestModelRenderer;
import net.minecraft.client.render.item.model.special.ConduitModelRenderer;
import net.minecraft.client.render.item.model.special.CopperGolemStatueModelRenderer;
import net.minecraft.client.render.item.model.special.DecoratedPotModelRenderer;
import net.minecraft.client.render.item.model.special.HeadModelRenderer;
import net.minecraft.client.render.item.model.special.PlayerHeadModelRenderer;
import net.minecraft.client.render.item.model.special.ShulkerBoxModelRenderer;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.item.tint.GrassTintSource;
import net.minecraft.client.render.item.tint.TintSource;
import net.minecraft.client.render.model.json.ModelVariant;
import net.minecraft.client.render.model.json.ModelVariantOperator;
import net.minecraft.client.render.model.json.MultipartModelCombinedCondition;
import net.minecraft.client.render.model.json.MultipartModelCondition;
import net.minecraft.client.render.model.json.MultipartModelConditionBuilder;
import net.minecraft.client.render.model.json.WeightedVariant;
import net.minecraft.data.family.BlockFamilies;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.state.State;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.math.AxisRotation;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BlockStateModelGenerator {
    final Consumer<BlockModelDefinitionCreator> blockStateCollector;
    final ItemModelOutput itemModelOutput;
    final BiConsumer<Identifier, ModelSupplier> modelCollector;
    static final List<Block> UNORIENTABLE_TRAPDOORS = List.of(Blocks.OAK_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.IRON_TRAPDOOR);
    public static final ModelVariantOperator NO_OP = variant -> variant;
    public static final ModelVariantOperator UV_LOCK = ModelVariantOperator.UV_LOCK.withValue(true);
    public static final ModelVariantOperator ROTATE_X_90 = ModelVariantOperator.ROTATION_X.withValue(AxisRotation.R90);
    public static final ModelVariantOperator ROTATE_X_180 = ModelVariantOperator.ROTATION_X.withValue(AxisRotation.R180);
    public static final ModelVariantOperator ROTATE_X_270 = ModelVariantOperator.ROTATION_X.withValue(AxisRotation.R270);
    public static final ModelVariantOperator ROTATE_Y_90 = ModelVariantOperator.ROTATION_Y.withValue(AxisRotation.R90);
    public static final ModelVariantOperator ROTATE_Y_180 = ModelVariantOperator.ROTATION_Y.withValue(AxisRotation.R180);
    public static final ModelVariantOperator ROTATE_Y_270 = ModelVariantOperator.ROTATION_Y.withValue(AxisRotation.R270);
    private static final Function<MultipartModelConditionBuilder, MultipartModelConditionBuilder> FLOWERBED_MODEL_1_CONDITION_FUNCTION = builder -> builder;
    private static final Function<MultipartModelConditionBuilder, MultipartModelConditionBuilder> FLOWERBED_MODEL_2_CONDITION_FUNCTION = builder -> builder.put(Properties.FLOWER_AMOUNT, Integer.valueOf(2), new Integer[]{3, 4});
    private static final Function<MultipartModelConditionBuilder, MultipartModelConditionBuilder> FLOWERBED_MODEL_3_CONDITION_FUNCTION = builder -> builder.put(Properties.FLOWER_AMOUNT, Integer.valueOf(3), new Integer[]{4});
    private static final Function<MultipartModelConditionBuilder, MultipartModelConditionBuilder> FLOWERBED_MODEL_4_CONDITION_FUNCTION = builder -> builder.put(Properties.FLOWER_AMOUNT, 4);
    private static final Function<MultipartModelConditionBuilder, MultipartModelConditionBuilder> LEAF_LITTER_MODEL_1_CONDITION_FUNCTION = builder -> builder.put(Properties.SEGMENT_AMOUNT, 1);
    private static final Function<MultipartModelConditionBuilder, MultipartModelConditionBuilder> LEAF_LITTER_MODEL_2_CONDITION_FUNCTION = builder -> builder.put(Properties.SEGMENT_AMOUNT, Integer.valueOf(2), new Integer[]{3});
    private static final Function<MultipartModelConditionBuilder, MultipartModelConditionBuilder> LEAF_LITTER_MODEL_3_CONDITION_FUNCTION = builder -> builder.put(Properties.SEGMENT_AMOUNT, 3);
    private static final Function<MultipartModelConditionBuilder, MultipartModelConditionBuilder> LEAF_LITTER_MODEL_4_CONDITION_FUNCTION = builder -> builder.put(Properties.SEGMENT_AMOUNT, 4);
    static final Map<Block, StateFactory> BASE_WITH_CUSTOM_GENERATOR = Map.of(Blocks.STONE, BlockStateModelGenerator::createStoneState, Blocks.DEEPSLATE, BlockStateModelGenerator::createDeepslateState, Blocks.MUD_BRICKS, BlockStateModelGenerator::createMudBrickState);
    private static final BlockStateVariantMap<ModelVariantOperator> NORTH_DEFAULT_ROTATION_OPERATIONS = BlockStateVariantMap.operations(Properties.FACING).register(Direction.DOWN, ROTATE_X_90).register(Direction.UP, ROTATE_X_270).register(Direction.NORTH, NO_OP).register(Direction.SOUTH, ROTATE_Y_180).register(Direction.WEST, ROTATE_Y_270).register(Direction.EAST, ROTATE_Y_90);
    private static final BlockStateVariantMap<ModelVariantOperator> UP_DEFAULT_ROTATION_OPERATIONS = BlockStateVariantMap.operations(Properties.FACING).register(Direction.DOWN, ROTATE_X_180).register(Direction.UP, NO_OP).register(Direction.NORTH, ROTATE_X_90).register(Direction.SOUTH, ROTATE_X_90.then(ROTATE_Y_180)).register(Direction.WEST, ROTATE_X_90.then(ROTATE_Y_270)).register(Direction.EAST, ROTATE_X_90.then(ROTATE_Y_90));
    private static final BlockStateVariantMap<ModelVariantOperator> EAST_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS = BlockStateVariantMap.operations(Properties.HORIZONTAL_FACING).register(Direction.EAST, NO_OP).register(Direction.SOUTH, ROTATE_Y_90).register(Direction.WEST, ROTATE_Y_180).register(Direction.NORTH, ROTATE_Y_270);
    private static final BlockStateVariantMap<ModelVariantOperator> SOUTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS = BlockStateVariantMap.operations(Properties.HORIZONTAL_FACING).register(Direction.SOUTH, NO_OP).register(Direction.WEST, ROTATE_Y_90).register(Direction.NORTH, ROTATE_Y_180).register(Direction.EAST, ROTATE_Y_270);
    private static final BlockStateVariantMap<ModelVariantOperator> NORTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS = BlockStateVariantMap.operations(Properties.HORIZONTAL_FACING).register(Direction.EAST, ROTATE_Y_90).register(Direction.SOUTH, ROTATE_Y_180).register(Direction.WEST, ROTATE_Y_270).register(Direction.NORTH, NO_OP);
    static final Map<Block, TexturedModel> TEXTURED_MODELS = ImmutableMap.builder().put(Blocks.SANDSTONE, TexturedModel.SIDE_TOP_BOTTOM_WALL.get(Blocks.SANDSTONE)).put(Blocks.RED_SANDSTONE, TexturedModel.SIDE_TOP_BOTTOM_WALL.get(Blocks.RED_SANDSTONE)).put(Blocks.SMOOTH_SANDSTONE, TexturedModel.getCubeAll(TextureMap.getSubId(Blocks.SANDSTONE, "_top"))).put(Blocks.SMOOTH_RED_SANDSTONE, TexturedModel.getCubeAll(TextureMap.getSubId(Blocks.RED_SANDSTONE, "_top"))).put(Blocks.CUT_SANDSTONE, TexturedModel.CUBE_COLUMN.get(Blocks.SANDSTONE).textures(textureMap -> textureMap.put(TextureKey.SIDE, TextureMap.getId(Blocks.CUT_SANDSTONE)))).put(Blocks.CUT_RED_SANDSTONE, TexturedModel.CUBE_COLUMN.get(Blocks.RED_SANDSTONE).textures(textureMap -> textureMap.put(TextureKey.SIDE, TextureMap.getId(Blocks.CUT_RED_SANDSTONE)))).put(Blocks.QUARTZ_BLOCK, TexturedModel.CUBE_COLUMN.get(Blocks.QUARTZ_BLOCK)).put(Blocks.SMOOTH_QUARTZ, TexturedModel.getCubeAll(TextureMap.getSubId(Blocks.QUARTZ_BLOCK, "_bottom"))).put(Blocks.BLACKSTONE, TexturedModel.SIDE_END_WALL.get(Blocks.BLACKSTONE)).put(Blocks.DEEPSLATE, TexturedModel.SIDE_END_WALL.get(Blocks.DEEPSLATE)).put(Blocks.CHISELED_QUARTZ_BLOCK, TexturedModel.CUBE_COLUMN.get(Blocks.CHISELED_QUARTZ_BLOCK).textures(textureMap -> textureMap.put(TextureKey.SIDE, TextureMap.getId(Blocks.CHISELED_QUARTZ_BLOCK)))).put(Blocks.CHISELED_SANDSTONE, TexturedModel.CUBE_COLUMN.get(Blocks.CHISELED_SANDSTONE).textures(textureMap -> {
        textureMap.put(TextureKey.END, TextureMap.getSubId(Blocks.SANDSTONE, "_top"));
        textureMap.put(TextureKey.SIDE, TextureMap.getId(Blocks.CHISELED_SANDSTONE));
    })).put(Blocks.CHISELED_RED_SANDSTONE, TexturedModel.CUBE_COLUMN.get(Blocks.CHISELED_RED_SANDSTONE).textures(textureMap -> {
        textureMap.put(TextureKey.END, TextureMap.getSubId(Blocks.RED_SANDSTONE, "_top"));
        textureMap.put(TextureKey.SIDE, TextureMap.getId(Blocks.CHISELED_RED_SANDSTONE));
    })).put(Blocks.CHISELED_TUFF_BRICKS, TexturedModel.SIDE_END_WALL.get(Blocks.CHISELED_TUFF_BRICKS)).put(Blocks.CHISELED_TUFF, TexturedModel.SIDE_END_WALL.get(Blocks.CHISELED_TUFF)).build();
    static final Map<BlockFamily.Variant, BiConsumer<BlockTexturePool, Block>> VARIANT_POOL_FUNCTIONS = ImmutableMap.builder().put(BlockFamily.Variant.BUTTON, BlockTexturePool::button).put(BlockFamily.Variant.DOOR, BlockTexturePool::door).put(BlockFamily.Variant.CHISELED, BlockTexturePool::block).put(BlockFamily.Variant.CRACKED, BlockTexturePool::block).put(BlockFamily.Variant.CUSTOM_FENCE, BlockTexturePool::customFence).put(BlockFamily.Variant.FENCE, BlockTexturePool::fence).put(BlockFamily.Variant.CUSTOM_FENCE_GATE, BlockTexturePool::customFenceGate).put(BlockFamily.Variant.FENCE_GATE, BlockTexturePool::fenceGate).put(BlockFamily.Variant.SIGN, BlockTexturePool::sign).put(BlockFamily.Variant.SLAB, BlockTexturePool::slab).put(BlockFamily.Variant.STAIRS, BlockTexturePool::stairs).put(BlockFamily.Variant.PRESSURE_PLATE, BlockTexturePool::pressurePlate).put(BlockFamily.Variant.TRAPDOOR, BlockTexturePool::registerTrapdoor).put(BlockFamily.Variant.WALL, BlockTexturePool::wall).build();
    private static final Map<Direction, ModelVariantOperator> CONNECTION_VARIANT_FUNCTIONS = ImmutableMap.of(Direction.NORTH, NO_OP, Direction.EAST, ROTATE_Y_90.then(UV_LOCK), Direction.SOUTH, ROTATE_Y_180.then(UV_LOCK), Direction.WEST, ROTATE_Y_270.then(UV_LOCK), Direction.UP, ROTATE_X_270.then(UV_LOCK), Direction.DOWN, ROTATE_X_90.then(UV_LOCK));
    private static final Map<ChiseledBookshelfModelCacheKey, Identifier> CHISELED_BOOKSHELF_MODEL_CACHE = new HashMap<ChiseledBookshelfModelCacheKey, Identifier>();

    static ModelVariant createModelVariant(Identifier id) {
        return new ModelVariant(id);
    }

    static WeightedVariant createWeightedVariant(ModelVariant variant) {
        return new WeightedVariant(Pool.of(variant));
    }

    private static WeightedVariant createWeightedVariant(ModelVariant ... variants) {
        return new WeightedVariant(Pool.of(Arrays.stream(variants).map(variant -> new Weighted<ModelVariant>((ModelVariant)variant, 1)).toList()));
    }

    static WeightedVariant createWeightedVariant(Identifier id) {
        return BlockStateModelGenerator.createWeightedVariant(BlockStateModelGenerator.createModelVariant(id));
    }

    private static MultipartModelConditionBuilder createMultipartConditionBuilder() {
        return new MultipartModelConditionBuilder();
    }

    @SafeVarargs
    private static <T extends Enum<T>> MultipartModelConditionBuilder createMultipartConditionBuilderWith(EnumProperty<T> property, T value, T ... values) {
        return BlockStateModelGenerator.createMultipartConditionBuilder().put(property, (Comparable)((Object)value), (Comparable[])values);
    }

    private static MultipartModelConditionBuilder createMultipartConditionBuilderWith(BooleanProperty property, boolean value) {
        return BlockStateModelGenerator.createMultipartConditionBuilder().put(property, value);
    }

    private static MultipartModelCondition or(MultipartModelConditionBuilder ... conditionBuilders) {
        return new MultipartModelCombinedCondition(MultipartModelCombinedCondition.LogicalOperator.OR, Stream.of(conditionBuilders).map(MultipartModelConditionBuilder::build).toList());
    }

    private static MultipartModelCondition and(MultipartModelConditionBuilder ... conditionBuilders) {
        return new MultipartModelCombinedCondition(MultipartModelCombinedCondition.LogicalOperator.AND, Stream.of(conditionBuilders).map(MultipartModelConditionBuilder::build).toList());
    }

    private static BlockModelDefinitionCreator createStoneState(Block block, ModelVariant variant, TextureMap textures, BiConsumer<Identifier, ModelSupplier> modelCollector) {
        ModelVariant lv = BlockStateModelGenerator.createModelVariant(Models.CUBE_MIRRORED_ALL.upload(block, textures, modelCollector));
        return VariantsBlockModelDefinitionCreator.of(block, BlockStateModelGenerator.modelWithMirroring(variant, lv));
    }

    private static BlockModelDefinitionCreator createMudBrickState(Block block, ModelVariant variant, TextureMap textures, BiConsumer<Identifier, ModelSupplier> modelCollector) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(Models.CUBE_NORTH_WEST_MIRRORED_ALL.upload(block, textures, modelCollector));
        return BlockStateModelGenerator.createSingletonBlockState(block, lv);
    }

    private static BlockModelDefinitionCreator createDeepslateState(Block block, ModelVariant variant, TextureMap textures, BiConsumer<Identifier, ModelSupplier> modelCollector) {
        ModelVariant lv = BlockStateModelGenerator.createModelVariant(Models.CUBE_COLUMN_MIRRORED.upload(block, textures, modelCollector));
        return VariantsBlockModelDefinitionCreator.of(block, BlockStateModelGenerator.modelWithMirroring(variant, lv)).apply(BlockStateModelGenerator.createAxisRotatedVariantMap());
    }

    public BlockStateModelGenerator(Consumer<BlockModelDefinitionCreator> blockStateCollector, ItemModelOutput itemModelOutput, BiConsumer<Identifier, ModelSupplier> modelCollector) {
        this.blockStateCollector = blockStateCollector;
        this.itemModelOutput = itemModelOutput;
        this.modelCollector = modelCollector;
    }

    private void registerItemModel(Item item, Identifier modelId) {
        this.itemModelOutput.accept(item, ItemModels.basic(modelId));
    }

    void registerParentedItemModel(Block block, Identifier parentModelId) {
        this.itemModelOutput.accept(block.asItem(), ItemModels.basic(parentModelId));
    }

    private void registerTintedItemModel(Block block, Identifier modelId, TintSource tint) {
        this.itemModelOutput.accept(block.asItem(), ItemModels.tinted(modelId, tint));
    }

    private Identifier uploadItemModel(Item item) {
        return Models.GENERATED.upload(ModelIds.getItemModelId(item), TextureMap.layer0(item), this.modelCollector);
    }

    Identifier uploadBlockItemModel(Item item, Block block) {
        return Models.GENERATED.upload(ModelIds.getItemModelId(item), TextureMap.layer0(block), this.modelCollector);
    }

    private Identifier uploadBlockItemModel(Item item, Block block, String textureSuffix) {
        return Models.GENERATED.upload(ModelIds.getItemModelId(item), TextureMap.layer0(TextureMap.getSubId(block, textureSuffix)), this.modelCollector);
    }

    Identifier uploadTwoLayerBlockItemModel(Item item, Block block, String layer1Suffix) {
        Identifier lv = TextureMap.getId(block);
        Identifier lv2 = TextureMap.getSubId(block, layer1Suffix);
        return Models.GENERATED_TWO_LAYERS.upload(ModelIds.getItemModelId(item), TextureMap.layered(lv, lv2), this.modelCollector);
    }

    void registerItemModel(Item item) {
        this.registerItemModel(item, this.uploadItemModel(item));
    }

    private void registerItemModel(Block block) {
        Item lv = block.asItem();
        if (lv != Items.AIR) {
            this.registerItemModel(lv, this.uploadBlockItemModel(lv, block));
        }
    }

    private void registerItemModel(Block block, String textureSuffix) {
        Item lv = block.asItem();
        if (lv != Items.AIR) {
            this.registerItemModel(lv, this.uploadBlockItemModel(lv, block, textureSuffix));
        }
    }

    private void registerTwoLayerItemModel(Block block, String layer1Suffix) {
        Item lv = block.asItem();
        if (lv != Items.AIR) {
            Identifier lv2 = this.uploadTwoLayerBlockItemModel(lv, block, layer1Suffix);
            this.registerItemModel(lv, lv2);
        }
    }

    private static WeightedVariant modelWithYRotation(ModelVariant variant) {
        return BlockStateModelGenerator.createWeightedVariant(variant, variant.with(ROTATE_Y_90), variant.with(ROTATE_Y_180), variant.with(ROTATE_Y_270));
    }

    private static WeightedVariant modelWithMirroring(ModelVariant variant, ModelVariant mirroredVariant) {
        return BlockStateModelGenerator.createWeightedVariant(variant, mirroredVariant, variant.with(ROTATE_Y_180), mirroredVariant.with(ROTATE_Y_180));
    }

    private static BlockStateVariantMap<WeightedVariant> createBooleanModelMap(BooleanProperty property, WeightedVariant trueModel, WeightedVariant falseModel) {
        return BlockStateVariantMap.models(property).register(true, trueModel).register(false, falseModel);
    }

    private void registerMirrorable(Block block) {
        ModelVariant lv = BlockStateModelGenerator.createModelVariant(TexturedModel.CUBE_ALL.upload(block, this.modelCollector));
        ModelVariant lv2 = BlockStateModelGenerator.createModelVariant(TexturedModel.CUBE_MIRRORED_ALL.upload(block, this.modelCollector));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(block, BlockStateModelGenerator.modelWithMirroring(lv, lv2)));
    }

    private void registerRotatable(Block block) {
        ModelVariant lv = BlockStateModelGenerator.createModelVariant(TexturedModel.CUBE_ALL.upload(block, this.modelCollector));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(block, BlockStateModelGenerator.modelWithYRotation(lv)));
    }

    private void registerBrushableBlock(Block block) {
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(block).with(BlockStateVariantMap.models(Properties.DUSTED).generate(dusted -> {
            String string = "_" + dusted;
            Identifier lv = TextureMap.getSubId(block, string);
            Identifier lv2 = Models.CUBE_ALL.upload(block, string, new TextureMap().put(TextureKey.ALL, lv), this.modelCollector);
            return BlockStateModelGenerator.createWeightedVariant(lv2);
        })));
        this.registerParentedItemModel(block, ModelIds.getBlockSubModelId(block, "_0"));
    }

    static BlockModelDefinitionCreator createButtonBlockState(Block buttonBlock, WeightedVariant unpressedModel, WeightedVariant pressedModel) {
        return VariantsBlockModelDefinitionCreator.of(buttonBlock).with(BlockStateVariantMap.models(Properties.POWERED).register(false, unpressedModel).register(true, pressedModel)).apply(BlockStateVariantMap.operations(Properties.BLOCK_FACE, Properties.HORIZONTAL_FACING).register(BlockFace.FLOOR, Direction.EAST, ROTATE_Y_90).register(BlockFace.FLOOR, Direction.WEST, ROTATE_Y_270).register(BlockFace.FLOOR, Direction.SOUTH, ROTATE_Y_180).register(BlockFace.FLOOR, Direction.NORTH, NO_OP).register(BlockFace.WALL, Direction.EAST, ROTATE_Y_90.then(ROTATE_X_90).then(UV_LOCK)).register(BlockFace.WALL, Direction.WEST, ROTATE_Y_270.then(ROTATE_X_90).then(UV_LOCK)).register(BlockFace.WALL, Direction.SOUTH, ROTATE_Y_180.then(ROTATE_X_90).then(UV_LOCK)).register(BlockFace.WALL, Direction.NORTH, ROTATE_X_90.then(UV_LOCK)).register(BlockFace.CEILING, Direction.EAST, ROTATE_Y_270.then(ROTATE_X_180)).register(BlockFace.CEILING, Direction.WEST, ROTATE_Y_90.then(ROTATE_X_180)).register(BlockFace.CEILING, Direction.SOUTH, ROTATE_X_180).register(BlockFace.CEILING, Direction.NORTH, ROTATE_Y_180.then(ROTATE_X_180)));
    }

    private static BlockModelDefinitionCreator createDoorBlockState(Block doorBlock, WeightedVariant bottomLeftClosedModel, WeightedVariant bottomLeftOpenModel, WeightedVariant bottomRightClosedModel, WeightedVariant bottomRightOpenModel, WeightedVariant topLeftClosedModel, WeightedVariant topLeftOpenModel, WeightedVariant topRightClosedModel, WeightedVariant topRightOpenModel) {
        return VariantsBlockModelDefinitionCreator.of(doorBlock).with(BlockStateVariantMap.models(Properties.HORIZONTAL_FACING, Properties.DOUBLE_BLOCK_HALF, Properties.DOOR_HINGE, Properties.OPEN).register(Direction.EAST, DoubleBlockHalf.LOWER, DoorHinge.LEFT, false, bottomLeftClosedModel).register(Direction.SOUTH, DoubleBlockHalf.LOWER, DoorHinge.LEFT, false, bottomLeftClosedModel.apply(ROTATE_Y_90)).register(Direction.WEST, DoubleBlockHalf.LOWER, DoorHinge.LEFT, false, bottomLeftClosedModel.apply(ROTATE_Y_180)).register(Direction.NORTH, DoubleBlockHalf.LOWER, DoorHinge.LEFT, false, bottomLeftClosedModel.apply(ROTATE_Y_270)).register(Direction.EAST, DoubleBlockHalf.LOWER, DoorHinge.RIGHT, false, bottomRightClosedModel).register(Direction.SOUTH, DoubleBlockHalf.LOWER, DoorHinge.RIGHT, false, bottomRightClosedModel.apply(ROTATE_Y_90)).register(Direction.WEST, DoubleBlockHalf.LOWER, DoorHinge.RIGHT, false, bottomRightClosedModel.apply(ROTATE_Y_180)).register(Direction.NORTH, DoubleBlockHalf.LOWER, DoorHinge.RIGHT, false, bottomRightClosedModel.apply(ROTATE_Y_270)).register(Direction.EAST, DoubleBlockHalf.LOWER, DoorHinge.LEFT, true, bottomLeftOpenModel.apply(ROTATE_Y_90)).register(Direction.SOUTH, DoubleBlockHalf.LOWER, DoorHinge.LEFT, true, bottomLeftOpenModel.apply(ROTATE_Y_180)).register(Direction.WEST, DoubleBlockHalf.LOWER, DoorHinge.LEFT, true, bottomLeftOpenModel.apply(ROTATE_Y_270)).register(Direction.NORTH, DoubleBlockHalf.LOWER, DoorHinge.LEFT, true, bottomLeftOpenModel).register(Direction.EAST, DoubleBlockHalf.LOWER, DoorHinge.RIGHT, true, bottomRightOpenModel.apply(ROTATE_Y_270)).register(Direction.SOUTH, DoubleBlockHalf.LOWER, DoorHinge.RIGHT, true, bottomRightOpenModel).register(Direction.WEST, DoubleBlockHalf.LOWER, DoorHinge.RIGHT, true, bottomRightOpenModel.apply(ROTATE_Y_90)).register(Direction.NORTH, DoubleBlockHalf.LOWER, DoorHinge.RIGHT, true, bottomRightOpenModel.apply(ROTATE_Y_180)).register(Direction.EAST, DoubleBlockHalf.UPPER, DoorHinge.LEFT, false, topLeftClosedModel).register(Direction.SOUTH, DoubleBlockHalf.UPPER, DoorHinge.LEFT, false, topLeftClosedModel.apply(ROTATE_Y_90)).register(Direction.WEST, DoubleBlockHalf.UPPER, DoorHinge.LEFT, false, topLeftClosedModel.apply(ROTATE_Y_180)).register(Direction.NORTH, DoubleBlockHalf.UPPER, DoorHinge.LEFT, false, topLeftClosedModel.apply(ROTATE_Y_270)).register(Direction.EAST, DoubleBlockHalf.UPPER, DoorHinge.RIGHT, false, topRightClosedModel).register(Direction.SOUTH, DoubleBlockHalf.UPPER, DoorHinge.RIGHT, false, topRightClosedModel.apply(ROTATE_Y_90)).register(Direction.WEST, DoubleBlockHalf.UPPER, DoorHinge.RIGHT, false, topRightClosedModel.apply(ROTATE_Y_180)).register(Direction.NORTH, DoubleBlockHalf.UPPER, DoorHinge.RIGHT, false, topRightClosedModel.apply(ROTATE_Y_270)).register(Direction.EAST, DoubleBlockHalf.UPPER, DoorHinge.LEFT, true, topLeftOpenModel.apply(ROTATE_Y_90)).register(Direction.SOUTH, DoubleBlockHalf.UPPER, DoorHinge.LEFT, true, topLeftOpenModel.apply(ROTATE_Y_180)).register(Direction.WEST, DoubleBlockHalf.UPPER, DoorHinge.LEFT, true, topLeftOpenModel.apply(ROTATE_Y_270)).register(Direction.NORTH, DoubleBlockHalf.UPPER, DoorHinge.LEFT, true, topLeftOpenModel).register(Direction.EAST, DoubleBlockHalf.UPPER, DoorHinge.RIGHT, true, topRightOpenModel.apply(ROTATE_Y_270)).register(Direction.SOUTH, DoubleBlockHalf.UPPER, DoorHinge.RIGHT, true, topRightOpenModel).register(Direction.WEST, DoubleBlockHalf.UPPER, DoorHinge.RIGHT, true, topRightOpenModel.apply(ROTATE_Y_90)).register(Direction.NORTH, DoubleBlockHalf.UPPER, DoorHinge.RIGHT, true, topRightOpenModel.apply(ROTATE_Y_180)));
    }

    static BlockModelDefinitionCreator createCustomFenceBlockState(Block customFenceBlock, WeightedVariant postModel, WeightedVariant northSideModel, WeightedVariant eastSideModel, WeightedVariant southSideModel, WeightedVariant westSideModel) {
        return MultipartBlockModelDefinitionCreator.create(customFenceBlock).with(postModel).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.NORTH, true), northSideModel).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.EAST, true), eastSideModel).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.SOUTH, true), southSideModel).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.WEST, true), westSideModel);
    }

    static BlockModelDefinitionCreator createFenceBlockState(Block fenceBlock, WeightedVariant postModel, WeightedVariant sideModel) {
        return MultipartBlockModelDefinitionCreator.create(fenceBlock).with(postModel).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.NORTH, true), sideModel.apply(UV_LOCK)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.EAST, true), sideModel.apply(ROTATE_Y_90).apply(UV_LOCK)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.SOUTH, true), sideModel.apply(ROTATE_Y_180).apply(UV_LOCK)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.WEST, true), sideModel.apply(ROTATE_Y_270).apply(UV_LOCK));
    }

    static BlockModelDefinitionCreator createWallBlockState(Block wallBlock, WeightedVariant postModel, WeightedVariant lowSideModel, WeightedVariant tallSideModel) {
        return MultipartBlockModelDefinitionCreator.create(wallBlock).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.UP, true), postModel).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.NORTH_WALL_SHAPE, WallShape.LOW), lowSideModel.apply(UV_LOCK)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.EAST_WALL_SHAPE, WallShape.LOW), lowSideModel.apply(ROTATE_Y_90).apply(UV_LOCK)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.SOUTH_WALL_SHAPE, WallShape.LOW), lowSideModel.apply(ROTATE_Y_180).apply(UV_LOCK)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.WEST_WALL_SHAPE, WallShape.LOW), lowSideModel.apply(ROTATE_Y_270).apply(UV_LOCK)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.NORTH_WALL_SHAPE, WallShape.TALL), tallSideModel.apply(UV_LOCK)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.EAST_WALL_SHAPE, WallShape.TALL), tallSideModel.apply(ROTATE_Y_90).apply(UV_LOCK)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.SOUTH_WALL_SHAPE, WallShape.TALL), tallSideModel.apply(ROTATE_Y_180).apply(UV_LOCK)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.WEST_WALL_SHAPE, WallShape.TALL), tallSideModel.apply(ROTATE_Y_270).apply(UV_LOCK));
    }

    static BlockModelDefinitionCreator createFenceGateBlockState(Block fenceGateBlock, WeightedVariant openModel, WeightedVariant closedModel, WeightedVariant openWallModel, WeightedVariant closedWallModel, boolean uvlock) {
        return VariantsBlockModelDefinitionCreator.of(fenceGateBlock).with(BlockStateVariantMap.models(Properties.IN_WALL, Properties.OPEN).register(false, false, closedModel).register(true, false, closedWallModel).register(false, true, openModel).register(true, true, openWallModel)).apply(uvlock ? UV_LOCK : NO_OP).apply(SOUTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS);
    }

    static BlockModelDefinitionCreator createStairsBlockState(Block stairsBlock, WeightedVariant innerModel, WeightedVariant straightModel, WeightedVariant outerModel) {
        return VariantsBlockModelDefinitionCreator.of(stairsBlock).with(BlockStateVariantMap.models(Properties.HORIZONTAL_FACING, Properties.BLOCK_HALF, Properties.STAIR_SHAPE).register(Direction.EAST, BlockHalf.BOTTOM, StairShape.STRAIGHT, straightModel).register(Direction.WEST, BlockHalf.BOTTOM, StairShape.STRAIGHT, straightModel.apply(ROTATE_Y_180).apply(UV_LOCK)).register(Direction.SOUTH, BlockHalf.BOTTOM, StairShape.STRAIGHT, straightModel.apply(ROTATE_Y_90).apply(UV_LOCK)).register(Direction.NORTH, BlockHalf.BOTTOM, StairShape.STRAIGHT, straightModel.apply(ROTATE_Y_270).apply(UV_LOCK)).register(Direction.EAST, BlockHalf.BOTTOM, StairShape.OUTER_RIGHT, outerModel).register(Direction.WEST, BlockHalf.BOTTOM, StairShape.OUTER_RIGHT, outerModel.apply(ROTATE_Y_180).apply(UV_LOCK)).register(Direction.SOUTH, BlockHalf.BOTTOM, StairShape.OUTER_RIGHT, outerModel.apply(ROTATE_Y_90).apply(UV_LOCK)).register(Direction.NORTH, BlockHalf.BOTTOM, StairShape.OUTER_RIGHT, outerModel.apply(ROTATE_Y_270).apply(UV_LOCK)).register(Direction.EAST, BlockHalf.BOTTOM, StairShape.OUTER_LEFT, outerModel.apply(ROTATE_Y_270).apply(UV_LOCK)).register(Direction.WEST, BlockHalf.BOTTOM, StairShape.OUTER_LEFT, outerModel.apply(ROTATE_Y_90).apply(UV_LOCK)).register(Direction.SOUTH, BlockHalf.BOTTOM, StairShape.OUTER_LEFT, outerModel).register(Direction.NORTH, BlockHalf.BOTTOM, StairShape.OUTER_LEFT, outerModel.apply(ROTATE_Y_180).apply(UV_LOCK)).register(Direction.EAST, BlockHalf.BOTTOM, StairShape.INNER_RIGHT, innerModel).register(Direction.WEST, BlockHalf.BOTTOM, StairShape.INNER_RIGHT, innerModel.apply(ROTATE_Y_180).apply(UV_LOCK)).register(Direction.SOUTH, BlockHalf.BOTTOM, StairShape.INNER_RIGHT, innerModel.apply(ROTATE_Y_90).apply(UV_LOCK)).register(Direction.NORTH, BlockHalf.BOTTOM, StairShape.INNER_RIGHT, innerModel.apply(ROTATE_Y_270).apply(UV_LOCK)).register(Direction.EAST, BlockHalf.BOTTOM, StairShape.INNER_LEFT, innerModel.apply(ROTATE_Y_270).apply(UV_LOCK)).register(Direction.WEST, BlockHalf.BOTTOM, StairShape.INNER_LEFT, innerModel.apply(ROTATE_Y_90).apply(UV_LOCK)).register(Direction.SOUTH, BlockHalf.BOTTOM, StairShape.INNER_LEFT, innerModel).register(Direction.NORTH, BlockHalf.BOTTOM, StairShape.INNER_LEFT, innerModel.apply(ROTATE_Y_180).apply(UV_LOCK)).register(Direction.EAST, BlockHalf.TOP, StairShape.STRAIGHT, straightModel.apply(ROTATE_X_180).apply(UV_LOCK)).register(Direction.WEST, BlockHalf.TOP, StairShape.STRAIGHT, straightModel.apply(ROTATE_X_180).apply(ROTATE_Y_180).apply(UV_LOCK)).register(Direction.SOUTH, BlockHalf.TOP, StairShape.STRAIGHT, straightModel.apply(ROTATE_X_180).apply(ROTATE_Y_90).apply(UV_LOCK)).register(Direction.NORTH, BlockHalf.TOP, StairShape.STRAIGHT, straightModel.apply(ROTATE_X_180).apply(ROTATE_Y_270).apply(UV_LOCK)).register(Direction.EAST, BlockHalf.TOP, StairShape.OUTER_RIGHT, outerModel.apply(ROTATE_X_180).apply(ROTATE_Y_90).apply(UV_LOCK)).register(Direction.WEST, BlockHalf.TOP, StairShape.OUTER_RIGHT, outerModel.apply(ROTATE_X_180).apply(ROTATE_Y_270).apply(UV_LOCK)).register(Direction.SOUTH, BlockHalf.TOP, StairShape.OUTER_RIGHT, outerModel.apply(ROTATE_X_180).apply(ROTATE_Y_180).apply(UV_LOCK)).register(Direction.NORTH, BlockHalf.TOP, StairShape.OUTER_RIGHT, outerModel.apply(ROTATE_X_180).apply(UV_LOCK)).register(Direction.EAST, BlockHalf.TOP, StairShape.OUTER_LEFT, outerModel.apply(ROTATE_X_180).apply(UV_LOCK)).register(Direction.WEST, BlockHalf.TOP, StairShape.OUTER_LEFT, outerModel.apply(ROTATE_X_180).apply(ROTATE_Y_180).apply(UV_LOCK)).register(Direction.SOUTH, BlockHalf.TOP, StairShape.OUTER_LEFT, outerModel.apply(ROTATE_X_180).apply(ROTATE_Y_90).apply(UV_LOCK)).register(Direction.NORTH, BlockHalf.TOP, StairShape.OUTER_LEFT, outerModel.apply(ROTATE_X_180).apply(ROTATE_Y_270).apply(UV_LOCK)).register(Direction.EAST, BlockHalf.TOP, StairShape.INNER_RIGHT, innerModel.apply(ROTATE_X_180).apply(ROTATE_Y_90).apply(UV_LOCK)).register(Direction.WEST, BlockHalf.TOP, StairShape.INNER_RIGHT, innerModel.apply(ROTATE_X_180).apply(ROTATE_Y_270).apply(UV_LOCK)).register(Direction.SOUTH, BlockHalf.TOP, StairShape.INNER_RIGHT, innerModel.apply(ROTATE_X_180).apply(ROTATE_Y_180).apply(UV_LOCK)).register(Direction.NORTH, BlockHalf.TOP, StairShape.INNER_RIGHT, innerModel.apply(ROTATE_X_180).apply(UV_LOCK)).register(Direction.EAST, BlockHalf.TOP, StairShape.INNER_LEFT, innerModel.apply(ROTATE_X_180).apply(UV_LOCK)).register(Direction.WEST, BlockHalf.TOP, StairShape.INNER_LEFT, innerModel.apply(ROTATE_X_180).apply(ROTATE_Y_180).apply(UV_LOCK)).register(Direction.SOUTH, BlockHalf.TOP, StairShape.INNER_LEFT, innerModel.apply(ROTATE_X_180).apply(ROTATE_Y_90).apply(UV_LOCK)).register(Direction.NORTH, BlockHalf.TOP, StairShape.INNER_LEFT, innerModel.apply(ROTATE_X_180).apply(ROTATE_Y_270).apply(UV_LOCK)));
    }

    private static BlockModelDefinitionCreator createOrientableTrapdoorBlockState(Block trapdoorBlock, WeightedVariant topModel, WeightedVariant bottomModel, WeightedVariant openModel) {
        return VariantsBlockModelDefinitionCreator.of(trapdoorBlock).with(BlockStateVariantMap.models(Properties.HORIZONTAL_FACING, Properties.BLOCK_HALF, Properties.OPEN).register(Direction.NORTH, BlockHalf.BOTTOM, false, bottomModel).register(Direction.SOUTH, BlockHalf.BOTTOM, false, bottomModel.apply(ROTATE_Y_180)).register(Direction.EAST, BlockHalf.BOTTOM, false, bottomModel.apply(ROTATE_Y_90)).register(Direction.WEST, BlockHalf.BOTTOM, false, bottomModel.apply(ROTATE_Y_270)).register(Direction.NORTH, BlockHalf.TOP, false, topModel).register(Direction.SOUTH, BlockHalf.TOP, false, topModel.apply(ROTATE_Y_180)).register(Direction.EAST, BlockHalf.TOP, false, topModel.apply(ROTATE_Y_90)).register(Direction.WEST, BlockHalf.TOP, false, topModel.apply(ROTATE_Y_270)).register(Direction.NORTH, BlockHalf.BOTTOM, true, openModel).register(Direction.SOUTH, BlockHalf.BOTTOM, true, openModel.apply(ROTATE_Y_180)).register(Direction.EAST, BlockHalf.BOTTOM, true, openModel.apply(ROTATE_Y_90)).register(Direction.WEST, BlockHalf.BOTTOM, true, openModel.apply(ROTATE_Y_270)).register(Direction.NORTH, BlockHalf.TOP, true, openModel.apply(ROTATE_X_180).apply(ROTATE_Y_180)).register(Direction.SOUTH, BlockHalf.TOP, true, openModel.apply(ROTATE_X_180)).register(Direction.EAST, BlockHalf.TOP, true, openModel.apply(ROTATE_X_180).apply(ROTATE_Y_270)).register(Direction.WEST, BlockHalf.TOP, true, openModel.apply(ROTATE_X_180).apply(ROTATE_Y_90)));
    }

    private static BlockModelDefinitionCreator createTrapdoorBlockState(Block trapdoorBlock, WeightedVariant topModel, WeightedVariant bottomModel, WeightedVariant openModel) {
        return VariantsBlockModelDefinitionCreator.of(trapdoorBlock).with(BlockStateVariantMap.models(Properties.HORIZONTAL_FACING, Properties.BLOCK_HALF, Properties.OPEN).register(Direction.NORTH, BlockHalf.BOTTOM, false, bottomModel).register(Direction.SOUTH, BlockHalf.BOTTOM, false, bottomModel).register(Direction.EAST, BlockHalf.BOTTOM, false, bottomModel).register(Direction.WEST, BlockHalf.BOTTOM, false, bottomModel).register(Direction.NORTH, BlockHalf.TOP, false, topModel).register(Direction.SOUTH, BlockHalf.TOP, false, topModel).register(Direction.EAST, BlockHalf.TOP, false, topModel).register(Direction.WEST, BlockHalf.TOP, false, topModel).register(Direction.NORTH, BlockHalf.BOTTOM, true, openModel).register(Direction.SOUTH, BlockHalf.BOTTOM, true, openModel.apply(ROTATE_Y_180)).register(Direction.EAST, BlockHalf.BOTTOM, true, openModel.apply(ROTATE_Y_90)).register(Direction.WEST, BlockHalf.BOTTOM, true, openModel.apply(ROTATE_Y_270)).register(Direction.NORTH, BlockHalf.TOP, true, openModel).register(Direction.SOUTH, BlockHalf.TOP, true, openModel.apply(ROTATE_Y_180)).register(Direction.EAST, BlockHalf.TOP, true, openModel.apply(ROTATE_Y_90)).register(Direction.WEST, BlockHalf.TOP, true, openModel.apply(ROTATE_Y_270)));
    }

    static VariantsBlockModelDefinitionCreator createSingletonBlockState(Block block, WeightedVariant model) {
        return VariantsBlockModelDefinitionCreator.of(block, model);
    }

    private static BlockStateVariantMap<ModelVariantOperator> createAxisRotatedVariantMap() {
        return BlockStateVariantMap.operations(Properties.AXIS).register(Direction.Axis.Y, NO_OP).register(Direction.Axis.Z, ROTATE_X_90).register(Direction.Axis.X, ROTATE_X_90.then(ROTATE_Y_90));
    }

    static BlockModelDefinitionCreator createUvLockedColumnBlockState(Block block, TextureMap textureMap, BiConsumer<Identifier, ModelSupplier> modelCollector) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(Models.CUBE_COLUMN_UV_LOCKED_X.upload(block, textureMap, modelCollector));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.CUBE_COLUMN_UV_LOCKED_Y.upload(block, textureMap, modelCollector));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(Models.CUBE_COLUMN_UV_LOCKED_Z.upload(block, textureMap, modelCollector));
        return VariantsBlockModelDefinitionCreator.of(block).with(BlockStateVariantMap.models(Properties.AXIS).register(Direction.Axis.X, lv).register(Direction.Axis.Y, lv2).register(Direction.Axis.Z, lv3));
    }

    static BlockModelDefinitionCreator createAxisRotatedBlockState(Block block, WeightedVariant model) {
        return VariantsBlockModelDefinitionCreator.of(block, model).apply(BlockStateModelGenerator.createAxisRotatedVariantMap());
    }

    private void registerAxisRotated(Block block, WeightedVariant model) {
        this.blockStateCollector.accept(BlockStateModelGenerator.createAxisRotatedBlockState(block, model));
    }

    public void registerAxisRotated(Block block, TexturedModel.Factory modelFactory) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(modelFactory.upload(block, this.modelCollector));
        this.blockStateCollector.accept(BlockStateModelGenerator.createAxisRotatedBlockState(block, lv));
    }

    private void registerNorthDefaultHorizontalRotatable(Block block, TexturedModel.Factory modelFactory) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(modelFactory.upload(block, this.modelCollector));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(block, lv).apply(NORTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS));
    }

    static BlockModelDefinitionCreator createAxisRotatedBlockState(Block block, WeightedVariant verticalModel, WeightedVariant horizontalModel) {
        return VariantsBlockModelDefinitionCreator.of(block).with(BlockStateVariantMap.models(Properties.AXIS).register(Direction.Axis.Y, verticalModel).register(Direction.Axis.Z, horizontalModel.apply(ROTATE_X_90)).register(Direction.Axis.X, horizontalModel.apply(ROTATE_X_90).apply(ROTATE_Y_90)));
    }

    private void registerAxisRotated(Block block, TexturedModel.Factory verticalModelFactory, TexturedModel.Factory horizontalModelFactory) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(verticalModelFactory.upload(block, this.modelCollector));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(horizontalModelFactory.upload(block, this.modelCollector));
        this.blockStateCollector.accept(BlockStateModelGenerator.createAxisRotatedBlockState(block, lv, lv2));
    }

    private void registerCreakingHeart(Block block) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(TexturedModel.END_FOR_TOP_CUBE_COLUMN.upload(block, this.modelCollector));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(TexturedModel.END_FOR_TOP_CUBE_COLUMN_HORIZONTAL.upload(block, this.modelCollector));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(this.createCreakingHeartModel(TexturedModel.END_FOR_TOP_CUBE_COLUMN, block, "_awake"));
        WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(this.createCreakingHeartModel(TexturedModel.END_FOR_TOP_CUBE_COLUMN_HORIZONTAL, block, "_awake"));
        WeightedVariant lv5 = BlockStateModelGenerator.createWeightedVariant(this.createCreakingHeartModel(TexturedModel.END_FOR_TOP_CUBE_COLUMN, block, "_dormant"));
        WeightedVariant lv6 = BlockStateModelGenerator.createWeightedVariant(this.createCreakingHeartModel(TexturedModel.END_FOR_TOP_CUBE_COLUMN_HORIZONTAL, block, "_dormant"));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(block).with(BlockStateVariantMap.models(Properties.AXIS, CreakingHeartBlock.ACTIVE).register(Direction.Axis.Y, CreakingHeartState.UPROOTED, lv).register(Direction.Axis.Z, CreakingHeartState.UPROOTED, lv2.apply(ROTATE_X_90)).register(Direction.Axis.X, CreakingHeartState.UPROOTED, lv2.apply(ROTATE_X_90).apply(ROTATE_Y_90)).register(Direction.Axis.Y, CreakingHeartState.DORMANT, lv5).register(Direction.Axis.Z, CreakingHeartState.DORMANT, lv6.apply(ROTATE_X_90)).register(Direction.Axis.X, CreakingHeartState.DORMANT, lv6.apply(ROTATE_X_90).apply(ROTATE_Y_90)).register(Direction.Axis.Y, CreakingHeartState.AWAKE, lv3).register(Direction.Axis.Z, CreakingHeartState.AWAKE, lv4.apply(ROTATE_X_90)).register(Direction.Axis.X, CreakingHeartState.AWAKE, lv4.apply(ROTATE_X_90).apply(ROTATE_Y_90))));
    }

    private Identifier createCreakingHeartModel(TexturedModel.Factory texturedModelFactory, Block block, String suffix) {
        return texturedModelFactory.andThen(textureMap -> textureMap.put(TextureKey.SIDE, TextureMap.getSubId(block, suffix)).put(TextureKey.END, TextureMap.getSubId(block, "_top" + suffix))).upload(block, suffix, this.modelCollector);
    }

    private Identifier createSubModel(Block block, String suffix, Model model, Function<Identifier, TextureMap> texturesFactory) {
        return model.upload(block, suffix, texturesFactory.apply(TextureMap.getSubId(block, suffix)), this.modelCollector);
    }

    static BlockModelDefinitionCreator createPressurePlateBlockState(Block pressurePlateBlock, WeightedVariant upModel, WeightedVariant downModel) {
        return VariantsBlockModelDefinitionCreator.of(pressurePlateBlock).with(BlockStateModelGenerator.createBooleanModelMap(Properties.POWERED, downModel, upModel));
    }

    static BlockModelDefinitionCreator createSlabBlockState(Block slabBlock, WeightedVariant bottomModel, WeightedVariant topModel, WeightedVariant doubleModel) {
        return VariantsBlockModelDefinitionCreator.of(slabBlock).with(BlockStateVariantMap.models(Properties.SLAB_TYPE).register(SlabType.BOTTOM, bottomModel).register(SlabType.TOP, topModel).register(SlabType.DOUBLE, doubleModel));
    }

    public void registerSimpleCubeAll(Block block) {
        this.registerSingleton(block, TexturedModel.CUBE_ALL);
    }

    public void registerSingleton(Block block, TexturedModel.Factory modelFactory) {
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(block, BlockStateModelGenerator.createWeightedVariant(modelFactory.upload(block, this.modelCollector))));
    }

    public void registerTintedBlockAndItem(Block block, TexturedModel.Factory texturedModelFactory, int tintColor) {
        Identifier lv = texturedModelFactory.upload(block, this.modelCollector);
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(block, BlockStateModelGenerator.createWeightedVariant(lv)));
        this.registerTintedItemModel(block, lv, ItemModels.constantTintSource(tintColor));
    }

    private void registerVine() {
        this.registerMultifaceBlockModel(Blocks.VINE);
        Identifier lv = this.uploadBlockItemModel(Items.VINE, Blocks.VINE);
        this.registerTintedItemModel(Blocks.VINE, lv, ItemModels.constantTintSource(-12012264));
    }

    private void registerGrassTinted(Block block) {
        Identifier lv = this.uploadBlockItemModel(block.asItem(), block);
        this.registerTintedItemModel(block, lv, new GrassTintSource());
    }

    private BlockTexturePool registerCubeAllModelTexturePool(Block block) {
        TexturedModel lv = TEXTURED_MODELS.getOrDefault(block, TexturedModel.CUBE_ALL.get(block));
        return new BlockTexturePool(lv.getTextures()).base(block, lv.getModel());
    }

    public void registerHangingSign(Block base, Block hangingSign, Block wallHangingSign) {
        WeightedVariant lv = this.uploadParticleModel(hangingSign, base);
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(hangingSign, lv));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(wallHangingSign, lv));
        this.registerItemModel(hangingSign.asItem());
    }

    void registerDoor(Block doorBlock) {
        TextureMap lv = TextureMap.topBottom(doorBlock);
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.DOOR_BOTTOM_LEFT.upload(doorBlock, lv, this.modelCollector));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(Models.DOOR_BOTTOM_LEFT_OPEN.upload(doorBlock, lv, this.modelCollector));
        WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(Models.DOOR_BOTTOM_RIGHT.upload(doorBlock, lv, this.modelCollector));
        WeightedVariant lv5 = BlockStateModelGenerator.createWeightedVariant(Models.DOOR_BOTTOM_RIGHT_OPEN.upload(doorBlock, lv, this.modelCollector));
        WeightedVariant lv6 = BlockStateModelGenerator.createWeightedVariant(Models.DOOR_TOP_LEFT.upload(doorBlock, lv, this.modelCollector));
        WeightedVariant lv7 = BlockStateModelGenerator.createWeightedVariant(Models.DOOR_TOP_LEFT_OPEN.upload(doorBlock, lv, this.modelCollector));
        WeightedVariant lv8 = BlockStateModelGenerator.createWeightedVariant(Models.DOOR_TOP_RIGHT.upload(doorBlock, lv, this.modelCollector));
        WeightedVariant lv9 = BlockStateModelGenerator.createWeightedVariant(Models.DOOR_TOP_RIGHT_OPEN.upload(doorBlock, lv, this.modelCollector));
        this.registerItemModel(doorBlock.asItem());
        this.blockStateCollector.accept(BlockStateModelGenerator.createDoorBlockState(doorBlock, lv2, lv3, lv4, lv5, lv6, lv7, lv8, lv9));
    }

    private void registerParentedDoor(Block parent, Block doorBlock) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(Models.DOOR_BOTTOM_LEFT.getBlockSubModelId(parent));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.DOOR_BOTTOM_LEFT_OPEN.getBlockSubModelId(parent));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(Models.DOOR_BOTTOM_RIGHT.getBlockSubModelId(parent));
        WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(Models.DOOR_BOTTOM_RIGHT_OPEN.getBlockSubModelId(parent));
        WeightedVariant lv5 = BlockStateModelGenerator.createWeightedVariant(Models.DOOR_TOP_LEFT.getBlockSubModelId(parent));
        WeightedVariant lv6 = BlockStateModelGenerator.createWeightedVariant(Models.DOOR_TOP_LEFT_OPEN.getBlockSubModelId(parent));
        WeightedVariant lv7 = BlockStateModelGenerator.createWeightedVariant(Models.DOOR_TOP_RIGHT.getBlockSubModelId(parent));
        WeightedVariant lv8 = BlockStateModelGenerator.createWeightedVariant(Models.DOOR_TOP_RIGHT_OPEN.getBlockSubModelId(parent));
        this.itemModelOutput.acceptAlias(parent.asItem(), doorBlock.asItem());
        this.blockStateCollector.accept(BlockStateModelGenerator.createDoorBlockState(doorBlock, lv, lv2, lv3, lv4, lv5, lv6, lv7, lv8));
    }

    void registerOrientableTrapdoor(Block trapdoorBlock) {
        TextureMap lv = TextureMap.texture(trapdoorBlock);
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_ORIENTABLE_TRAPDOOR_TOP.upload(trapdoorBlock, lv, this.modelCollector));
        Identifier lv3 = Models.TEMPLATE_ORIENTABLE_TRAPDOOR_BOTTOM.upload(trapdoorBlock, lv, this.modelCollector);
        WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_ORIENTABLE_TRAPDOOR_OPEN.upload(trapdoorBlock, lv, this.modelCollector));
        this.blockStateCollector.accept(BlockStateModelGenerator.createOrientableTrapdoorBlockState(trapdoorBlock, lv2, BlockStateModelGenerator.createWeightedVariant(lv3), lv4));
        this.registerParentedItemModel(trapdoorBlock, lv3);
    }

    void registerTrapdoor(Block trapdoorBlock) {
        TextureMap lv = TextureMap.texture(trapdoorBlock);
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_TRAPDOOR_TOP.upload(trapdoorBlock, lv, this.modelCollector));
        Identifier lv3 = Models.TEMPLATE_TRAPDOOR_BOTTOM.upload(trapdoorBlock, lv, this.modelCollector);
        WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_TRAPDOOR_OPEN.upload(trapdoorBlock, lv, this.modelCollector));
        this.blockStateCollector.accept(BlockStateModelGenerator.createTrapdoorBlockState(trapdoorBlock, lv2, BlockStateModelGenerator.createWeightedVariant(lv3), lv4));
        this.registerParentedItemModel(trapdoorBlock, lv3);
    }

    private void registerParentedTrapdoor(Block parent, Block trapdoorBlock) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_TRAPDOOR_TOP.getBlockSubModelId(parent));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_TRAPDOOR_BOTTOM.getBlockSubModelId(parent));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_TRAPDOOR_OPEN.getBlockSubModelId(parent));
        this.itemModelOutput.acceptAlias(parent.asItem(), trapdoorBlock.asItem());
        this.blockStateCollector.accept(BlockStateModelGenerator.createTrapdoorBlockState(trapdoorBlock, lv, lv2, lv3));
    }

    private void registerBigDripleaf() {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(Blocks.BIG_DRIPLEAF));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.BIG_DRIPLEAF, "_partial_tilt"));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.BIG_DRIPLEAF, "_full_tilt"));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.BIG_DRIPLEAF).with(BlockStateVariantMap.models(Properties.TILT).register(Tilt.NONE, lv).register(Tilt.UNSTABLE, lv).register(Tilt.PARTIAL, lv2).register(Tilt.FULL, lv3)).apply(NORTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS));
    }

    private LogTexturePool createLogTexturePool(Block logBlock) {
        return new LogTexturePool(TextureMap.sideAndEndForTop(logBlock));
    }

    private void registerSimpleState(Block block) {
        this.registerStateWithModelReference(block, block);
    }

    private void registerStateWithModelReference(Block block, Block modelReference) {
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(block, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(modelReference))));
    }

    private void registerTintableCross(Block block, CrossType crossType) {
        this.registerItemModel(block.asItem(), crossType.registerItemModel(this, block));
        this.registerTintableCrossBlockState(block, crossType);
    }

    private void registerTintableCross(Block block, CrossType tintType, TextureMap texture) {
        this.registerItemModel(block);
        this.registerTintableCrossBlockState(block, tintType, texture);
    }

    private void registerTintableCrossBlockState(Block block, CrossType tintType) {
        TextureMap lv = tintType.getTextureMap(block);
        this.registerTintableCrossBlockState(block, tintType, lv);
    }

    private void registerTintableCrossBlockState(Block block, CrossType tintType, TextureMap crossTexture) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(tintType.getCrossModel().upload(block, crossTexture, this.modelCollector));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(block, lv));
    }

    private void registerTintableCrossBlockStateWithStages(Block block, CrossType tintType, Property<Integer> stageProperty, int ... stages) {
        if (stageProperty.getValues().size() != stages.length) {
            throw new IllegalArgumentException("missing values for property: " + String.valueOf(stageProperty));
        }
        this.registerItemModel(block.asItem());
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(block).with(BlockStateVariantMap.models(stageProperty).generate(stage -> {
            String string = "_stage" + stages[stage];
            TextureMap lv = TextureMap.cross(TextureMap.getSubId(block, string));
            return BlockStateModelGenerator.createWeightedVariant(tintType.getCrossModel().upload(block, string, lv, this.modelCollector));
        })));
    }

    private void registerFlowerPotPlantAndItem(Block block, Block flowerPotBlock, CrossType crossType) {
        this.registerItemModel(block.asItem(), crossType.registerItemModel(this, block));
        this.registerFlowerPotPlant(block, flowerPotBlock, crossType);
    }

    private void registerFlowerPotPlant(Block plantBlock, Block flowerPotBlock, CrossType tintType) {
        this.registerTintableCrossBlockState(plantBlock, tintType);
        TextureMap lv = tintType.getFlowerPotTextureMap(plantBlock);
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(tintType.getFlowerPotCrossModel().upload(flowerPotBlock, lv, this.modelCollector));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(flowerPotBlock, lv2));
    }

    private void registerCoralFan(Block coralFanBlock, Block coralWallFanBlock) {
        TexturedModel lv = TexturedModel.CORAL_FAN.get(coralFanBlock);
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(lv.upload(coralFanBlock, this.modelCollector));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(coralFanBlock, lv2));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(Models.CORAL_WALL_FAN.upload(coralWallFanBlock, lv.getTextures(), this.modelCollector));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(coralWallFanBlock, lv3).apply(NORTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS));
        this.registerItemModel(coralFanBlock);
    }

    private void registerGourd(Block stemBlock, Block attachedStemBlock) {
        this.registerItemModel(stemBlock.asItem());
        TextureMap lv = TextureMap.stem(stemBlock);
        TextureMap lv2 = TextureMap.stemAndUpper(stemBlock, attachedStemBlock);
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(Models.STEM_FRUIT.upload(attachedStemBlock, lv2, this.modelCollector));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(attachedStemBlock, lv3).apply(BlockStateVariantMap.operations(Properties.HORIZONTAL_FACING).register(Direction.WEST, NO_OP).register(Direction.SOUTH, ROTATE_Y_270).register(Direction.NORTH, ROTATE_Y_90).register(Direction.EAST, ROTATE_Y_180)));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(stemBlock).with(BlockStateVariantMap.models(Properties.AGE_7).generate(age -> BlockStateModelGenerator.createWeightedVariant(Models.STEM_GROWTH_STAGES[age].upload(stemBlock, lv, this.modelCollector)))));
    }

    private void registerPitcherPlant() {
        Block lv = Blocks.PITCHER_PLANT;
        this.registerItemModel(lv.asItem());
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(lv, "_top"));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(lv, "_bottom"));
        this.registerDoubleBlock(lv, lv2, lv3);
    }

    private void registerPitcherCrop() {
        Block lv = Blocks.PITCHER_CROP;
        this.registerItemModel(lv.asItem());
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(lv).with(BlockStateVariantMap.models(PitcherCropBlock.AGE, Properties.DOUBLE_BLOCK_HALF).generate((age, half) -> switch (half) {
            default -> throw new MatchException(null, null);
            case DoubleBlockHalf.UPPER -> BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(lv, "_top_stage_" + age));
            case DoubleBlockHalf.LOWER -> BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(lv, "_bottom_stage_" + age));
        })));
    }

    private void registerCoral(Block coral, Block deadCoral, Block coralBlock, Block deadCoralBlock, Block coralFan, Block deadCoralFan, Block coralWallFan, Block deadCoralWallFan) {
        this.registerTintableCross(coral, CrossType.NOT_TINTED);
        this.registerTintableCross(deadCoral, CrossType.NOT_TINTED);
        this.registerSimpleCubeAll(coralBlock);
        this.registerSimpleCubeAll(deadCoralBlock);
        this.registerCoralFan(coralFan, coralWallFan);
        this.registerCoralFan(deadCoralFan, deadCoralWallFan);
    }

    private void registerDoubleBlock(Block doubleBlock, CrossType tintType) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(this.createSubModel(doubleBlock, "_top", tintType.getCrossModel(), TextureMap::cross));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(this.createSubModel(doubleBlock, "_bottom", tintType.getCrossModel(), TextureMap::cross));
        this.registerDoubleBlock(doubleBlock, lv, lv2);
    }

    private void registerDoubleBlockAndItem(Block block, CrossType crossType) {
        this.registerItemModel(block, "_top");
        this.registerDoubleBlock(block, crossType);
    }

    private void registerGrassTintedDoubleBlockAndItem(Block block) {
        Identifier lv = this.uploadBlockItemModel(block.asItem(), block, "_top");
        this.registerTintedItemModel(block, lv, new GrassTintSource());
        this.registerDoubleBlock(block, CrossType.TINTED);
    }

    private void registerSunflower() {
        this.registerItemModel(Blocks.SUNFLOWER, "_front");
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.SUNFLOWER, "_top"));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(this.createSubModel(Blocks.SUNFLOWER, "_bottom", CrossType.NOT_TINTED.getCrossModel(), TextureMap::cross));
        this.registerDoubleBlock(Blocks.SUNFLOWER, lv, lv2);
    }

    private void registerTallSeagrass() {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(this.createSubModel(Blocks.TALL_SEAGRASS, "_top", Models.TEMPLATE_SEAGRASS, TextureMap::texture));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(this.createSubModel(Blocks.TALL_SEAGRASS, "_bottom", Models.TEMPLATE_SEAGRASS, TextureMap::texture));
        this.registerDoubleBlock(Blocks.TALL_SEAGRASS, lv, lv2);
    }

    private void registerSmallDripleaf() {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.SMALL_DRIPLEAF, "_top"));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.SMALL_DRIPLEAF, "_bottom"));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.SMALL_DRIPLEAF).with(BlockStateVariantMap.models(Properties.DOUBLE_BLOCK_HALF).register(DoubleBlockHalf.LOWER, lv2).register(DoubleBlockHalf.UPPER, lv)).apply(NORTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS));
    }

    private void registerDoubleBlock(Block block, WeightedVariant upperModel, WeightedVariant lowerModel) {
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(block).with(BlockStateVariantMap.models(Properties.DOUBLE_BLOCK_HALF).register(DoubleBlockHalf.LOWER, lowerModel).register(DoubleBlockHalf.UPPER, upperModel)));
    }

    private void registerTurnableRail(Block rail) {
        TextureMap lv = TextureMap.rail(rail);
        TextureMap lv2 = TextureMap.rail(TextureMap.getSubId(rail, "_corner"));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(Models.RAIL_FLAT.upload(rail, lv, this.modelCollector));
        WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(Models.RAIL_CURVED.upload(rail, lv2, this.modelCollector));
        WeightedVariant lv5 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_RAIL_RAISED_NE.upload(rail, lv, this.modelCollector));
        WeightedVariant lv6 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_RAIL_RAISED_SW.upload(rail, lv, this.modelCollector));
        this.registerItemModel(rail);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(rail).with(BlockStateVariantMap.models(Properties.RAIL_SHAPE).register(RailShape.NORTH_SOUTH, lv3).register(RailShape.EAST_WEST, lv3.apply(ROTATE_Y_90)).register(RailShape.ASCENDING_EAST, lv5.apply(ROTATE_Y_90)).register(RailShape.ASCENDING_WEST, lv6.apply(ROTATE_Y_90)).register(RailShape.ASCENDING_NORTH, lv5).register(RailShape.ASCENDING_SOUTH, lv6).register(RailShape.SOUTH_EAST, lv4).register(RailShape.SOUTH_WEST, lv4.apply(ROTATE_Y_90)).register(RailShape.NORTH_WEST, lv4.apply(ROTATE_Y_180)).register(RailShape.NORTH_EAST, lv4.apply(ROTATE_Y_270))));
    }

    private void registerStraightRail(Block rail) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(this.createSubModel(rail, "", Models.RAIL_FLAT, TextureMap::rail));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(this.createSubModel(rail, "", Models.TEMPLATE_RAIL_RAISED_NE, TextureMap::rail));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(this.createSubModel(rail, "", Models.TEMPLATE_RAIL_RAISED_SW, TextureMap::rail));
        WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(this.createSubModel(rail, "_on", Models.RAIL_FLAT, TextureMap::rail));
        WeightedVariant lv5 = BlockStateModelGenerator.createWeightedVariant(this.createSubModel(rail, "_on", Models.TEMPLATE_RAIL_RAISED_NE, TextureMap::rail));
        WeightedVariant lv6 = BlockStateModelGenerator.createWeightedVariant(this.createSubModel(rail, "_on", Models.TEMPLATE_RAIL_RAISED_SW, TextureMap::rail));
        this.registerItemModel(rail);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(rail).with(BlockStateVariantMap.models(Properties.POWERED, Properties.STRAIGHT_RAIL_SHAPE).generate((powered, shape) -> switch (shape) {
            case RailShape.NORTH_SOUTH -> {
                if (powered.booleanValue()) {
                    yield lv4;
                }
                yield lv;
            }
            case RailShape.EAST_WEST -> (powered != false ? lv4 : lv).apply(ROTATE_Y_90);
            case RailShape.ASCENDING_EAST -> (powered != false ? lv5 : lv2).apply(ROTATE_Y_90);
            case RailShape.ASCENDING_WEST -> (powered != false ? lv6 : lv3).apply(ROTATE_Y_90);
            case RailShape.ASCENDING_NORTH -> {
                if (powered.booleanValue()) {
                    yield lv5;
                }
                yield lv2;
            }
            case RailShape.ASCENDING_SOUTH -> {
                if (powered.booleanValue()) {
                    yield lv6;
                }
                yield lv3;
            }
            default -> throw new UnsupportedOperationException("Fix you generator!");
        })));
    }

    private void registerBuiltinWithParticle(Block block, Item particleSource) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(Models.PARTICLE.upload(block, TextureMap.particle(particleSource), this.modelCollector));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(block, lv));
    }

    private void registerBuiltinWithParticle(Block block, Identifier particleSource) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(Models.PARTICLE.upload(block, TextureMap.particle(particleSource), this.modelCollector));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(block, lv));
    }

    private WeightedVariant uploadParticleModel(Block block, Block particleSource) {
        return BlockStateModelGenerator.createWeightedVariant(Models.PARTICLE.upload(block, TextureMap.particle(particleSource), this.modelCollector));
    }

    public void registerBuiltinWithParticle(Block block, Block particleSource) {
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(block, this.uploadParticleModel(block, particleSource)));
    }

    private void registerBuiltin(Block block) {
        this.registerBuiltinWithParticle(block, block);
    }

    private void registerWoolAndCarpet(Block wool, Block carpet) {
        this.registerSimpleCubeAll(wool);
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(TexturedModel.CARPET.get(wool).upload(carpet, this.modelCollector));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(carpet, lv));
    }

    private void registerLeafLitter(Block leafLitter) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(TexturedModel.TEMPLATE_LEAF_LITTER_1.upload(leafLitter, this.modelCollector));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(TexturedModel.TEMPLATE_LEAF_LITTER_2.upload(leafLitter, this.modelCollector));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(TexturedModel.TEMPLATE_LEAF_LITTER_3.upload(leafLitter, this.modelCollector));
        WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(TexturedModel.TEMPLATE_LEAF_LITTER_4.upload(leafLitter, this.modelCollector));
        this.registerItemModel(leafLitter.asItem());
        this.registerSegmentedBlock(leafLitter, lv, LEAF_LITTER_MODEL_1_CONDITION_FUNCTION, lv2, LEAF_LITTER_MODEL_2_CONDITION_FUNCTION, lv3, LEAF_LITTER_MODEL_3_CONDITION_FUNCTION, lv4, LEAF_LITTER_MODEL_4_CONDITION_FUNCTION);
    }

    private void registerFlowerbed(Block flowerbed) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(TexturedModel.FLOWERBED_1.upload(flowerbed, this.modelCollector));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(TexturedModel.FLOWERBED_2.upload(flowerbed, this.modelCollector));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(TexturedModel.FLOWERBED_3.upload(flowerbed, this.modelCollector));
        WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(TexturedModel.FLOWERBED_4.upload(flowerbed, this.modelCollector));
        this.registerItemModel(flowerbed.asItem());
        this.registerSegmentedBlock(flowerbed, lv, FLOWERBED_MODEL_1_CONDITION_FUNCTION, lv2, FLOWERBED_MODEL_2_CONDITION_FUNCTION, lv3, FLOWERBED_MODEL_3_CONDITION_FUNCTION, lv4, FLOWERBED_MODEL_4_CONDITION_FUNCTION);
    }

    private void registerSegmentedBlock(Block block, WeightedVariant model1, Function<MultipartModelConditionBuilder, MultipartModelConditionBuilder> model1ConditionFunction, WeightedVariant model2, Function<MultipartModelConditionBuilder, MultipartModelConditionBuilder> model2ConditionFunction, WeightedVariant model3, Function<MultipartModelConditionBuilder, MultipartModelConditionBuilder> model3ConditionFunction, WeightedVariant model4, Function<MultipartModelConditionBuilder, MultipartModelConditionBuilder> model4ConditionFunction) {
        this.blockStateCollector.accept(MultipartBlockModelDefinitionCreator.create(block).with(model1ConditionFunction.apply(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HORIZONTAL_FACING, Direction.NORTH)), model1).with(model1ConditionFunction.apply(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HORIZONTAL_FACING, Direction.EAST)), model1.apply(ROTATE_Y_90)).with(model1ConditionFunction.apply(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HORIZONTAL_FACING, Direction.SOUTH)), model1.apply(ROTATE_Y_180)).with(model1ConditionFunction.apply(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HORIZONTAL_FACING, Direction.WEST)), model1.apply(ROTATE_Y_270)).with(model2ConditionFunction.apply(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HORIZONTAL_FACING, Direction.NORTH)), model2).with(model2ConditionFunction.apply(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HORIZONTAL_FACING, Direction.EAST)), model2.apply(ROTATE_Y_90)).with(model2ConditionFunction.apply(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HORIZONTAL_FACING, Direction.SOUTH)), model2.apply(ROTATE_Y_180)).with(model2ConditionFunction.apply(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HORIZONTAL_FACING, Direction.WEST)), model2.apply(ROTATE_Y_270)).with(model3ConditionFunction.apply(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HORIZONTAL_FACING, Direction.NORTH)), model3).with(model3ConditionFunction.apply(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HORIZONTAL_FACING, Direction.EAST)), model3.apply(ROTATE_Y_90)).with(model3ConditionFunction.apply(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HORIZONTAL_FACING, Direction.SOUTH)), model3.apply(ROTATE_Y_180)).with(model3ConditionFunction.apply(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HORIZONTAL_FACING, Direction.WEST)), model3.apply(ROTATE_Y_270)).with(model4ConditionFunction.apply(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HORIZONTAL_FACING, Direction.NORTH)), model4).with(model4ConditionFunction.apply(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HORIZONTAL_FACING, Direction.EAST)), model4.apply(ROTATE_Y_90)).with(model4ConditionFunction.apply(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HORIZONTAL_FACING, Direction.SOUTH)), model4.apply(ROTATE_Y_180)).with(model4ConditionFunction.apply(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HORIZONTAL_FACING, Direction.WEST)), model4.apply(ROTATE_Y_270)));
    }

    private void registerRandomHorizontalRotations(TexturedModel.Factory modelFactory, Block ... blocks) {
        for (Block lv : blocks) {
            ModelVariant lv2 = BlockStateModelGenerator.createModelVariant(modelFactory.upload(lv, this.modelCollector));
            this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(lv, BlockStateModelGenerator.modelWithYRotation(lv2)));
        }
    }

    private void registerSouthDefaultHorizontalFacing(TexturedModel.Factory modelFactory, Block ... blocks) {
        for (Block lv : blocks) {
            WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(modelFactory.upload(lv, this.modelCollector));
            this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(lv, lv2).apply(SOUTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS));
        }
    }

    private void registerGlassAndPane(Block glassBlock, Block glassPane) {
        this.registerSimpleCubeAll(glassBlock);
        TextureMap lv = TextureMap.paneAndTopForEdge(glassBlock, glassPane);
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_GLASS_PANE_POST.upload(glassPane, lv, this.modelCollector));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_GLASS_PANE_SIDE.upload(glassPane, lv, this.modelCollector));
        WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_GLASS_PANE_SIDE_ALT.upload(glassPane, lv, this.modelCollector));
        WeightedVariant lv5 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_GLASS_PANE_NOSIDE.upload(glassPane, lv, this.modelCollector));
        WeightedVariant lv6 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_GLASS_PANE_NOSIDE_ALT.upload(glassPane, lv, this.modelCollector));
        Item lv7 = glassPane.asItem();
        this.registerItemModel(lv7, this.uploadBlockItemModel(lv7, glassBlock));
        this.blockStateCollector.accept(MultipartBlockModelDefinitionCreator.create(glassPane).with(lv2).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.NORTH, true), lv3).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.EAST, true), lv3.apply(ROTATE_Y_90)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.SOUTH, true), lv4).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.WEST, true), lv4.apply(ROTATE_Y_90)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.NORTH, false), lv5).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.EAST, false), lv6).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.SOUTH, false), lv6.apply(ROTATE_Y_90)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.WEST, false), lv5.apply(ROTATE_Y_270)));
    }

    private void registerCommandBlock(Block commandBlock) {
        TextureMap lv = TextureMap.sideFrontBack(commandBlock);
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_COMMAND_BLOCK.upload(commandBlock, lv, this.modelCollector));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(this.createSubModel(commandBlock, "_conditional", Models.TEMPLATE_COMMAND_BLOCK, id -> lv.copyAndAdd(TextureKey.SIDE, (Identifier)id)));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(commandBlock).with(BlockStateModelGenerator.createBooleanModelMap(Properties.CONDITIONAL, lv3, lv2)).apply(NORTH_DEFAULT_ROTATION_OPERATIONS));
    }

    private void registerAnvil(Block anvil) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(TexturedModel.TEMPLATE_ANVIL.upload(anvil, this.modelCollector));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(anvil, lv).apply(SOUTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS));
    }

    private static WeightedVariant getBambooBlockStateVariants(int age) {
        String string = "_age" + age;
        return new WeightedVariant(Pool.of(IntStream.range(1, 5).mapToObj(i -> new Weighted<ModelVariant>(BlockStateModelGenerator.createModelVariant(ModelIds.getBlockSubModelId(Blocks.BAMBOO, i + string)), 1)).collect(Collectors.toList())));
    }

    private void registerBamboo() {
        this.blockStateCollector.accept(MultipartBlockModelDefinitionCreator.create(Blocks.BAMBOO).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.AGE_1, 0), BlockStateModelGenerator.getBambooBlockStateVariants(0)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.AGE_1, 1), BlockStateModelGenerator.getBambooBlockStateVariants(1)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.BAMBOO_LEAVES, BambooLeaves.SMALL), BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.BAMBOO, "_small_leaves"))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.BAMBOO_LEAVES, BambooLeaves.LARGE), BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.BAMBOO, "_large_leaves"))));
    }

    private void registerBarrel() {
        Identifier lv = TextureMap.getSubId(Blocks.BARREL, "_top_open");
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(TexturedModel.CUBE_BOTTOM_TOP.upload(Blocks.BARREL, this.modelCollector));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(TexturedModel.CUBE_BOTTOM_TOP.get(Blocks.BARREL).textures(textureMap -> textureMap.put(TextureKey.TOP, lv)).upload(Blocks.BARREL, "_open", this.modelCollector));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.BARREL).with(BlockStateVariantMap.models(Properties.OPEN).register(false, lv2).register(true, lv3)).apply(UP_DEFAULT_ROTATION_OPERATIONS));
    }

    private static <T extends Comparable<T>> BlockStateVariantMap<WeightedVariant> createValueFencedModelMap(Property<T> property, T fence, WeightedVariant aboveFenceModel, WeightedVariant belowFenceModel) {
        return BlockStateVariantMap.models(property).generate(value -> {
            boolean bl = value.compareTo(fence) >= 0;
            return bl ? aboveFenceModel : belowFenceModel;
        });
    }

    private void registerBeehive(Block beehive, Function<Block, TextureMap> texturesFactory) {
        TextureMap lv = texturesFactory.apply(beehive).inherit(TextureKey.SIDE, TextureKey.PARTICLE);
        TextureMap lv2 = lv.copyAndAdd(TextureKey.FRONT, TextureMap.getSubId(beehive, "_front_honey"));
        Identifier lv3 = Models.ORIENTABLE_WITH_BOTTOM.upload(beehive, "_empty", lv, this.modelCollector);
        Identifier lv4 = Models.ORIENTABLE_WITH_BOTTOM.upload(beehive, "_honey", lv2, this.modelCollector);
        this.itemModelOutput.accept(beehive.asItem(), ItemModels.select(BeehiveBlock.HONEY_LEVEL, ItemModels.basic(lv3), Map.of(5, ItemModels.basic(lv4))));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(beehive).with(BlockStateModelGenerator.createValueFencedModelMap(BeehiveBlock.HONEY_LEVEL, 5, BlockStateModelGenerator.createWeightedVariant(lv4), BlockStateModelGenerator.createWeightedVariant(lv3))).apply(NORTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS));
    }

    private void registerCrop(Block crop, Property<Integer> ageProperty, int ... ageTextureIndices) {
        this.registerItemModel(crop.asItem());
        if (ageProperty.getValues().size() != ageTextureIndices.length) {
            throw new IllegalArgumentException();
        }
        Int2ObjectOpenHashMap int2ObjectMap = new Int2ObjectOpenHashMap();
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(crop).with(BlockStateVariantMap.models(ageProperty).generate(age -> {
            int i = ageTextureIndices[age];
            return BlockStateModelGenerator.createWeightedVariant(int2ObjectMap.computeIfAbsent(i, stage -> this.createSubModel(crop, "_stage" + stage, Models.CROP, TextureMap::crop)));
        })));
    }

    private void registerBell() {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.BELL, "_floor"));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.BELL, "_ceiling"));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.BELL, "_wall"));
        WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.BELL, "_between_walls"));
        this.registerItemModel(Items.BELL);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.BELL).with(BlockStateVariantMap.models(Properties.HORIZONTAL_FACING, Properties.ATTACHMENT).register(Direction.NORTH, Attachment.FLOOR, lv).register(Direction.SOUTH, Attachment.FLOOR, lv.apply(ROTATE_Y_180)).register(Direction.EAST, Attachment.FLOOR, lv.apply(ROTATE_Y_90)).register(Direction.WEST, Attachment.FLOOR, lv.apply(ROTATE_Y_270)).register(Direction.NORTH, Attachment.CEILING, lv2).register(Direction.SOUTH, Attachment.CEILING, lv2.apply(ROTATE_Y_180)).register(Direction.EAST, Attachment.CEILING, lv2.apply(ROTATE_Y_90)).register(Direction.WEST, Attachment.CEILING, lv2.apply(ROTATE_Y_270)).register(Direction.NORTH, Attachment.SINGLE_WALL, lv3.apply(ROTATE_Y_270)).register(Direction.SOUTH, Attachment.SINGLE_WALL, lv3.apply(ROTATE_Y_90)).register(Direction.EAST, Attachment.SINGLE_WALL, lv3).register(Direction.WEST, Attachment.SINGLE_WALL, lv3.apply(ROTATE_Y_180)).register(Direction.SOUTH, Attachment.DOUBLE_WALL, lv4.apply(ROTATE_Y_90)).register(Direction.NORTH, Attachment.DOUBLE_WALL, lv4.apply(ROTATE_Y_270)).register(Direction.EAST, Attachment.DOUBLE_WALL, lv4).register(Direction.WEST, Attachment.DOUBLE_WALL, lv4.apply(ROTATE_Y_180))));
    }

    private void registerGrindstone() {
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.GRINDSTONE, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(Blocks.GRINDSTONE))).apply(BlockStateVariantMap.operations(Properties.BLOCK_FACE, Properties.HORIZONTAL_FACING).register(BlockFace.FLOOR, Direction.NORTH, NO_OP).register(BlockFace.FLOOR, Direction.EAST, ROTATE_Y_90).register(BlockFace.FLOOR, Direction.SOUTH, ROTATE_Y_180).register(BlockFace.FLOOR, Direction.WEST, ROTATE_Y_270).register(BlockFace.WALL, Direction.NORTH, ROTATE_X_90).register(BlockFace.WALL, Direction.EAST, ROTATE_X_90.then(ROTATE_Y_90)).register(BlockFace.WALL, Direction.SOUTH, ROTATE_X_90.then(ROTATE_Y_180)).register(BlockFace.WALL, Direction.WEST, ROTATE_X_90.then(ROTATE_Y_270)).register(BlockFace.CEILING, Direction.SOUTH, ROTATE_X_180).register(BlockFace.CEILING, Direction.WEST, ROTATE_X_180.then(ROTATE_Y_90)).register(BlockFace.CEILING, Direction.NORTH, ROTATE_X_180.then(ROTATE_Y_180)).register(BlockFace.CEILING, Direction.EAST, ROTATE_X_180.then(ROTATE_Y_270))));
    }

    private void registerCooker(Block cooker, TexturedModel.Factory modelFactory) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(modelFactory.upload(cooker, this.modelCollector));
        Identifier lv2 = TextureMap.getSubId(cooker, "_front_on");
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(modelFactory.get(cooker).textures(textures -> textures.put(TextureKey.FRONT, lv2)).upload(cooker, "_on", this.modelCollector));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(cooker).with(BlockStateModelGenerator.createBooleanModelMap(Properties.LIT, lv3, lv)).apply(NORTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS));
    }

    private void registerCampfire(Block ... blocks) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(ModelIds.getMinecraftNamespacedBlock("campfire_off"));
        for (Block lv2 : blocks) {
            WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_CAMPFIRE.upload(lv2, TextureMap.campfire(lv2), this.modelCollector));
            this.registerItemModel(lv2.asItem());
            this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(lv2).with(BlockStateModelGenerator.createBooleanModelMap(Properties.LIT, lv3, lv)).apply(SOUTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS));
        }
    }

    private void registerAzalea(Block block) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_AZALEA.upload(block, TextureMap.sideAndTop(block), this.modelCollector));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(block, lv));
    }

    private void registerPottedAzaleaBush(Block block) {
        WeightedVariant lv = block == Blocks.POTTED_FLOWERING_AZALEA_BUSH ? BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_POTTED_FLOWERING_AZALEA_BUSH.upload(block, TextureMap.pottedAzaleaBush(block), this.modelCollector)) : BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_POTTED_AZALEA_BUSH.upload(block, TextureMap.pottedAzaleaBush(block), this.modelCollector));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(block, lv));
    }

    private void registerBookshelf() {
        TextureMap lv = TextureMap.sideEnd(TextureMap.getId(Blocks.BOOKSHELF), TextureMap.getId(Blocks.OAK_PLANKS));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.CUBE_COLUMN.upload(Blocks.BOOKSHELF, lv, this.modelCollector));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(Blocks.BOOKSHELF, lv2));
    }

    private void registerRedstone() {
        this.registerItemModel(Items.REDSTONE);
        this.blockStateCollector.accept(MultipartBlockModelDefinitionCreator.create(Blocks.REDSTONE_WIRE).with(BlockStateModelGenerator.or(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.NORTH_WIRE_CONNECTION, WireConnection.NONE).put(Properties.EAST_WIRE_CONNECTION, WireConnection.NONE).put(Properties.SOUTH_WIRE_CONNECTION, WireConnection.NONE).put(Properties.WEST_WIRE_CONNECTION, WireConnection.NONE), BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.NORTH_WIRE_CONNECTION, (Comparable)((Object)WireConnection.SIDE), (Comparable[])new WireConnection[]{WireConnection.UP}).put(Properties.EAST_WIRE_CONNECTION, (Comparable)((Object)WireConnection.SIDE), (Comparable[])new WireConnection[]{WireConnection.UP}), BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.EAST_WIRE_CONNECTION, (Comparable)((Object)WireConnection.SIDE), (Comparable[])new WireConnection[]{WireConnection.UP}).put(Properties.SOUTH_WIRE_CONNECTION, (Comparable)((Object)WireConnection.SIDE), (Comparable[])new WireConnection[]{WireConnection.UP}), BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.SOUTH_WIRE_CONNECTION, (Comparable)((Object)WireConnection.SIDE), (Comparable[])new WireConnection[]{WireConnection.UP}).put(Properties.WEST_WIRE_CONNECTION, (Comparable)((Object)WireConnection.SIDE), (Comparable[])new WireConnection[]{WireConnection.UP}), BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.WEST_WIRE_CONNECTION, (Comparable)((Object)WireConnection.SIDE), (Comparable[])new WireConnection[]{WireConnection.UP}).put(Properties.NORTH_WIRE_CONNECTION, (Comparable)((Object)WireConnection.SIDE), (Comparable[])new WireConnection[]{WireConnection.UP})), BlockStateModelGenerator.createWeightedVariant(ModelIds.getMinecraftNamespacedBlock("redstone_dust_dot"))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.NORTH_WIRE_CONNECTION, (Comparable)((Object)WireConnection.SIDE), (Comparable[])new WireConnection[]{WireConnection.UP}), BlockStateModelGenerator.createWeightedVariant(ModelIds.getMinecraftNamespacedBlock("redstone_dust_side0"))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.SOUTH_WIRE_CONNECTION, (Comparable)((Object)WireConnection.SIDE), (Comparable[])new WireConnection[]{WireConnection.UP}), BlockStateModelGenerator.createWeightedVariant(ModelIds.getMinecraftNamespacedBlock("redstone_dust_side_alt0"))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.EAST_WIRE_CONNECTION, (Comparable)((Object)WireConnection.SIDE), (Comparable[])new WireConnection[]{WireConnection.UP}), BlockStateModelGenerator.createWeightedVariant(ModelIds.getMinecraftNamespacedBlock("redstone_dust_side_alt1")).apply(ROTATE_Y_270)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.WEST_WIRE_CONNECTION, (Comparable)((Object)WireConnection.SIDE), (Comparable[])new WireConnection[]{WireConnection.UP}), BlockStateModelGenerator.createWeightedVariant(ModelIds.getMinecraftNamespacedBlock("redstone_dust_side1")).apply(ROTATE_Y_270)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.NORTH_WIRE_CONNECTION, WireConnection.UP), BlockStateModelGenerator.createWeightedVariant(ModelIds.getMinecraftNamespacedBlock("redstone_dust_up"))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.EAST_WIRE_CONNECTION, WireConnection.UP), BlockStateModelGenerator.createWeightedVariant(ModelIds.getMinecraftNamespacedBlock("redstone_dust_up")).apply(ROTATE_Y_90)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.SOUTH_WIRE_CONNECTION, WireConnection.UP), BlockStateModelGenerator.createWeightedVariant(ModelIds.getMinecraftNamespacedBlock("redstone_dust_up")).apply(ROTATE_Y_180)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.WEST_WIRE_CONNECTION, WireConnection.UP), BlockStateModelGenerator.createWeightedVariant(ModelIds.getMinecraftNamespacedBlock("redstone_dust_up")).apply(ROTATE_Y_270)));
    }

    private void registerComparator() {
        this.registerItemModel(Items.COMPARATOR);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.COMPARATOR).with(BlockStateVariantMap.models(Properties.COMPARATOR_MODE, Properties.POWERED).register(ComparatorMode.COMPARE, false, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(Blocks.COMPARATOR))).register(ComparatorMode.COMPARE, true, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.COMPARATOR, "_on"))).register(ComparatorMode.SUBTRACT, false, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.COMPARATOR, "_subtract"))).register(ComparatorMode.SUBTRACT, true, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.COMPARATOR, "_on_subtract")))).apply(SOUTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS));
    }

    private void registerSmoothStone() {
        TextureMap lv = TextureMap.all(Blocks.SMOOTH_STONE);
        TextureMap lv2 = TextureMap.sideEnd(TextureMap.getSubId(Blocks.SMOOTH_STONE_SLAB, "_side"), lv.getTexture(TextureKey.TOP));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(Models.SLAB.upload(Blocks.SMOOTH_STONE_SLAB, lv2, this.modelCollector));
        WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(Models.SLAB_TOP.upload(Blocks.SMOOTH_STONE_SLAB, lv2, this.modelCollector));
        WeightedVariant lv5 = BlockStateModelGenerator.createWeightedVariant(Models.CUBE_COLUMN.uploadWithoutVariant(Blocks.SMOOTH_STONE_SLAB, "_double", lv2, this.modelCollector));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSlabBlockState(Blocks.SMOOTH_STONE_SLAB, lv3, lv4, lv5));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(Blocks.SMOOTH_STONE, BlockStateModelGenerator.createWeightedVariant(Models.CUBE_ALL.upload(Blocks.SMOOTH_STONE, lv, this.modelCollector))));
    }

    private void registerBrewingStand() {
        this.registerItemModel(Items.BREWING_STAND);
        this.blockStateCollector.accept(MultipartBlockModelDefinitionCreator.create(Blocks.BREWING_STAND).with(BlockStateModelGenerator.createWeightedVariant(TextureMap.getId(Blocks.BREWING_STAND))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HAS_BOTTLE_0, true), BlockStateModelGenerator.createWeightedVariant(TextureMap.getSubId(Blocks.BREWING_STAND, "_bottle0"))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HAS_BOTTLE_1, true), BlockStateModelGenerator.createWeightedVariant(TextureMap.getSubId(Blocks.BREWING_STAND, "_bottle1"))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HAS_BOTTLE_2, true), BlockStateModelGenerator.createWeightedVariant(TextureMap.getSubId(Blocks.BREWING_STAND, "_bottle2"))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HAS_BOTTLE_0, false), BlockStateModelGenerator.createWeightedVariant(TextureMap.getSubId(Blocks.BREWING_STAND, "_empty0"))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HAS_BOTTLE_1, false), BlockStateModelGenerator.createWeightedVariant(TextureMap.getSubId(Blocks.BREWING_STAND, "_empty1"))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HAS_BOTTLE_2, false), BlockStateModelGenerator.createWeightedVariant(TextureMap.getSubId(Blocks.BREWING_STAND, "_empty2"))));
    }

    private void registerMushroomBlock(Block mushroomBlock) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_SINGLE_FACE.upload(mushroomBlock, TextureMap.texture(mushroomBlock), this.modelCollector));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getMinecraftNamespacedBlock("mushroom_block_inside"));
        this.blockStateCollector.accept(MultipartBlockModelDefinitionCreator.create(mushroomBlock).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.NORTH, true), lv).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.EAST, true), lv.apply(ROTATE_Y_90).apply(UV_LOCK)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.SOUTH, true), lv.apply(ROTATE_Y_180).apply(UV_LOCK)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.WEST, true), lv.apply(ROTATE_Y_270).apply(UV_LOCK)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.UP, true), lv.apply(ROTATE_X_270).apply(UV_LOCK)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.DOWN, true), lv.apply(ROTATE_X_90).apply(UV_LOCK)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.NORTH, false), lv2).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.EAST, false), lv2.apply(ROTATE_Y_90)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.SOUTH, false), lv2.apply(ROTATE_Y_180)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.WEST, false), lv2.apply(ROTATE_Y_270)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.UP, false), lv2.apply(ROTATE_X_270)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.DOWN, false), lv2.apply(ROTATE_X_90)));
        this.registerParentedItemModel(mushroomBlock, TexturedModel.CUBE_ALL.upload(mushroomBlock, "_inventory", this.modelCollector));
    }

    private void registerCake() {
        this.registerItemModel(Items.CAKE);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.CAKE).with(BlockStateVariantMap.models(Properties.BITES).register(0, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(Blocks.CAKE))).register(1, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.CAKE, "_slice1"))).register(2, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.CAKE, "_slice2"))).register(3, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.CAKE, "_slice3"))).register(4, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.CAKE, "_slice4"))).register(5, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.CAKE, "_slice5"))).register(6, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.CAKE, "_slice6")))));
    }

    private void registerCartographyTable() {
        TextureMap lv = new TextureMap().put(TextureKey.PARTICLE, TextureMap.getSubId(Blocks.CARTOGRAPHY_TABLE, "_side3")).put(TextureKey.DOWN, TextureMap.getId(Blocks.DARK_OAK_PLANKS)).put(TextureKey.UP, TextureMap.getSubId(Blocks.CARTOGRAPHY_TABLE, "_top")).put(TextureKey.NORTH, TextureMap.getSubId(Blocks.CARTOGRAPHY_TABLE, "_side3")).put(TextureKey.EAST, TextureMap.getSubId(Blocks.CARTOGRAPHY_TABLE, "_side3")).put(TextureKey.SOUTH, TextureMap.getSubId(Blocks.CARTOGRAPHY_TABLE, "_side1")).put(TextureKey.WEST, TextureMap.getSubId(Blocks.CARTOGRAPHY_TABLE, "_side2"));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(Blocks.CARTOGRAPHY_TABLE, BlockStateModelGenerator.createWeightedVariant(Models.CUBE.upload(Blocks.CARTOGRAPHY_TABLE, lv, this.modelCollector))));
    }

    private void registerSmithingTable() {
        TextureMap lv = new TextureMap().put(TextureKey.PARTICLE, TextureMap.getSubId(Blocks.SMITHING_TABLE, "_front")).put(TextureKey.DOWN, TextureMap.getSubId(Blocks.SMITHING_TABLE, "_bottom")).put(TextureKey.UP, TextureMap.getSubId(Blocks.SMITHING_TABLE, "_top")).put(TextureKey.NORTH, TextureMap.getSubId(Blocks.SMITHING_TABLE, "_front")).put(TextureKey.SOUTH, TextureMap.getSubId(Blocks.SMITHING_TABLE, "_front")).put(TextureKey.EAST, TextureMap.getSubId(Blocks.SMITHING_TABLE, "_side")).put(TextureKey.WEST, TextureMap.getSubId(Blocks.SMITHING_TABLE, "_side"));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(Blocks.SMITHING_TABLE, BlockStateModelGenerator.createWeightedVariant(Models.CUBE.upload(Blocks.SMITHING_TABLE, lv, this.modelCollector))));
    }

    private void registerCubeWithCustomTextures(Block block, Block otherTextureSource, BiFunction<Block, Block, TextureMap> texturesFactory) {
        TextureMap lv = texturesFactory.apply(block, otherTextureSource);
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(block, BlockStateModelGenerator.createWeightedVariant(Models.CUBE.upload(block, lv, this.modelCollector))));
    }

    public void registerGeneric(Block block) {
        TextureMap lv = new TextureMap().put(TextureKey.PARTICLE, TextureMap.getSubId(block, "_particle")).put(TextureKey.DOWN, TextureMap.getSubId(block, "_down")).put(TextureKey.UP, TextureMap.getSubId(block, "_up")).put(TextureKey.NORTH, TextureMap.getSubId(block, "_north")).put(TextureKey.SOUTH, TextureMap.getSubId(block, "_south")).put(TextureKey.EAST, TextureMap.getSubId(block, "_east")).put(TextureKey.WEST, TextureMap.getSubId(block, "_west"));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(block, BlockStateModelGenerator.createWeightedVariant(Models.CUBE.upload(block, lv, this.modelCollector))));
    }

    private void registerPumpkins() {
        TextureMap lv = TextureMap.sideEnd(Blocks.PUMPKIN);
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(Blocks.PUMPKIN, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(Blocks.PUMPKIN))));
        this.registerNorthDefaultHorizontalRotatable(Blocks.CARVED_PUMPKIN, lv);
        this.registerNorthDefaultHorizontalRotatable(Blocks.JACK_O_LANTERN, lv);
    }

    private void registerNorthDefaultHorizontalRotatable(Block block, TextureMap texture) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(Models.ORIENTABLE.upload(block, texture.copyAndAdd(TextureKey.FRONT, TextureMap.getId(block)), this.modelCollector));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(block, lv).apply(NORTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS));
    }

    private void registerCauldrons() {
        this.registerItemModel(Items.CAULDRON);
        this.registerSimpleState(Blocks.CAULDRON);
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(Blocks.LAVA_CAULDRON, BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_CAULDRON_FULL.upload(Blocks.LAVA_CAULDRON, TextureMap.cauldron(TextureMap.getSubId(Blocks.LAVA, "_still")), this.modelCollector))));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.WATER_CAULDRON).with(BlockStateVariantMap.models(LeveledCauldronBlock.LEVEL).register(1, BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_CAULDRON_LEVEL1.upload(Blocks.WATER_CAULDRON, "_level1", TextureMap.cauldron(TextureMap.getSubId(Blocks.WATER, "_still")), this.modelCollector))).register(2, BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_CAULDRON_LEVEL2.upload(Blocks.WATER_CAULDRON, "_level2", TextureMap.cauldron(TextureMap.getSubId(Blocks.WATER, "_still")), this.modelCollector))).register(3, BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_CAULDRON_FULL.upload(Blocks.WATER_CAULDRON, "_full", TextureMap.cauldron(TextureMap.getSubId(Blocks.WATER, "_still")), this.modelCollector)))));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.POWDER_SNOW_CAULDRON).with(BlockStateVariantMap.models(LeveledCauldronBlock.LEVEL).register(1, BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_CAULDRON_LEVEL1.upload(Blocks.POWDER_SNOW_CAULDRON, "_level1", TextureMap.cauldron(TextureMap.getId(Blocks.POWDER_SNOW)), this.modelCollector))).register(2, BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_CAULDRON_LEVEL2.upload(Blocks.POWDER_SNOW_CAULDRON, "_level2", TextureMap.cauldron(TextureMap.getId(Blocks.POWDER_SNOW)), this.modelCollector))).register(3, BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_CAULDRON_FULL.upload(Blocks.POWDER_SNOW_CAULDRON, "_full", TextureMap.cauldron(TextureMap.getId(Blocks.POWDER_SNOW)), this.modelCollector)))));
    }

    private void registerChorusFlower() {
        TextureMap lv = TextureMap.texture(Blocks.CHORUS_FLOWER);
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_CHORUS_FLOWER.upload(Blocks.CHORUS_FLOWER, lv, this.modelCollector));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(this.createSubModel(Blocks.CHORUS_FLOWER, "_dead", Models.TEMPLATE_CHORUS_FLOWER, id -> lv.copyAndAdd(TextureKey.TEXTURE, (Identifier)id)));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.CHORUS_FLOWER).with(BlockStateModelGenerator.createValueFencedModelMap(Properties.AGE_5, 5, lv3, lv2)));
    }

    private void registerCrafter() {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(Blocks.CRAFTER));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.CRAFTER, "_triggered"));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.CRAFTER, "_crafting"));
        WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.CRAFTER, "_crafting_triggered"));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.CRAFTER).with(BlockStateVariantMap.models(Properties.TRIGGERED, CrafterBlock.CRAFTING).register(false, false, lv).register(true, true, lv4).register(true, false, lv2).register(false, true, lv3)).apply(BlockStateVariantMap.operations(Properties.ORIENTATION).generate(BlockStateModelGenerator::addJigsawOrientationToVariant)));
    }

    private void registerDispenserLikeOrientable(Block block) {
        TextureMap lv = new TextureMap().put(TextureKey.TOP, TextureMap.getSubId(Blocks.FURNACE, "_top")).put(TextureKey.SIDE, TextureMap.getSubId(Blocks.FURNACE, "_side")).put(TextureKey.FRONT, TextureMap.getSubId(block, "_front"));
        TextureMap lv2 = new TextureMap().put(TextureKey.SIDE, TextureMap.getSubId(Blocks.FURNACE, "_top")).put(TextureKey.FRONT, TextureMap.getSubId(block, "_front_vertical"));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(Models.ORIENTABLE.upload(block, lv, this.modelCollector));
        WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(Models.ORIENTABLE_VERTICAL.upload(block, lv2, this.modelCollector));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(block).with(BlockStateVariantMap.models(Properties.FACING).register(Direction.DOWN, lv4.apply(ROTATE_X_180)).register(Direction.UP, lv4).register(Direction.NORTH, lv3).register(Direction.EAST, lv3.apply(ROTATE_Y_90)).register(Direction.SOUTH, lv3.apply(ROTATE_Y_180)).register(Direction.WEST, lv3.apply(ROTATE_Y_270))));
    }

    private void registerEndPortalFrame() {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(Blocks.END_PORTAL_FRAME));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.END_PORTAL_FRAME, "_filled"));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.END_PORTAL_FRAME).with(BlockStateVariantMap.models(Properties.EYE).register(false, lv).register(true, lv2)).apply(SOUTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS));
    }

    private void registerChorusPlant() {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.CHORUS_PLANT, "_side"));
        ModelVariant lv2 = BlockStateModelGenerator.createModelVariant(ModelIds.getBlockSubModelId(Blocks.CHORUS_PLANT, "_noside"));
        ModelVariant lv3 = BlockStateModelGenerator.createModelVariant(ModelIds.getBlockSubModelId(Blocks.CHORUS_PLANT, "_noside1"));
        ModelVariant lv4 = BlockStateModelGenerator.createModelVariant(ModelIds.getBlockSubModelId(Blocks.CHORUS_PLANT, "_noside2"));
        ModelVariant lv5 = BlockStateModelGenerator.createModelVariant(ModelIds.getBlockSubModelId(Blocks.CHORUS_PLANT, "_noside3"));
        ModelVariant lv6 = lv2.with(UV_LOCK);
        ModelVariant lv7 = lv3.with(UV_LOCK);
        ModelVariant lv8 = lv4.with(UV_LOCK);
        ModelVariant lv9 = lv5.with(UV_LOCK);
        this.blockStateCollector.accept(MultipartBlockModelDefinitionCreator.create(Blocks.CHORUS_PLANT).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.NORTH, true), lv).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.EAST, true), lv.apply(ROTATE_Y_90).apply(UV_LOCK)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.SOUTH, true), lv.apply(ROTATE_Y_180).apply(UV_LOCK)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.WEST, true), lv.apply(ROTATE_Y_270).apply(UV_LOCK)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.UP, true), lv.apply(ROTATE_X_270).apply(UV_LOCK)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.DOWN, true), lv.apply(ROTATE_X_90).apply(UV_LOCK)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.NORTH, false), new WeightedVariant(Pool.of(new Weighted<ModelVariant>(lv2, 2), new Weighted<ModelVariant>(lv3, 1), new Weighted<ModelVariant>(lv4, 1), new Weighted<ModelVariant>(lv5, 1)))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.EAST, false), new WeightedVariant(Pool.of(new Weighted<ModelVariant>(lv7.with(ROTATE_Y_90), 1), new Weighted<ModelVariant>(lv8.with(ROTATE_Y_90), 1), new Weighted<ModelVariant>(lv9.with(ROTATE_Y_90), 1), new Weighted<ModelVariant>(lv6.with(ROTATE_Y_90), 2)))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.SOUTH, false), new WeightedVariant(Pool.of(new Weighted<ModelVariant>(lv8.with(ROTATE_Y_180), 1), new Weighted<ModelVariant>(lv9.with(ROTATE_Y_180), 1), new Weighted<ModelVariant>(lv6.with(ROTATE_Y_180), 2), new Weighted<ModelVariant>(lv7.with(ROTATE_Y_180), 1)))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.WEST, false), new WeightedVariant(Pool.of(new Weighted<ModelVariant>(lv9.with(ROTATE_Y_270), 1), new Weighted<ModelVariant>(lv6.with(ROTATE_Y_270), 2), new Weighted<ModelVariant>(lv7.with(ROTATE_Y_270), 1), new Weighted<ModelVariant>(lv8.with(ROTATE_Y_270), 1)))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.UP, false), new WeightedVariant(Pool.of(new Weighted<ModelVariant>(lv6.with(ROTATE_X_270), 2), new Weighted<ModelVariant>(lv9.with(ROTATE_X_270), 1), new Weighted<ModelVariant>(lv7.with(ROTATE_X_270), 1), new Weighted<ModelVariant>(lv8.with(ROTATE_X_270), 1)))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.DOWN, false), new WeightedVariant(Pool.of(new Weighted<ModelVariant>(lv9.with(ROTATE_X_90), 1), new Weighted<ModelVariant>(lv8.with(ROTATE_X_90), 1), new Weighted<ModelVariant>(lv7.with(ROTATE_X_90), 1), new Weighted<ModelVariant>(lv6.with(ROTATE_X_90), 2)))));
    }

    private void registerComposter() {
        this.blockStateCollector.accept(MultipartBlockModelDefinitionCreator.create(Blocks.COMPOSTER).with(BlockStateModelGenerator.createWeightedVariant(TextureMap.getId(Blocks.COMPOSTER))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.LEVEL_8, 1), BlockStateModelGenerator.createWeightedVariant(TextureMap.getSubId(Blocks.COMPOSTER, "_contents1"))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.LEVEL_8, 2), BlockStateModelGenerator.createWeightedVariant(TextureMap.getSubId(Blocks.COMPOSTER, "_contents2"))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.LEVEL_8, 3), BlockStateModelGenerator.createWeightedVariant(TextureMap.getSubId(Blocks.COMPOSTER, "_contents3"))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.LEVEL_8, 4), BlockStateModelGenerator.createWeightedVariant(TextureMap.getSubId(Blocks.COMPOSTER, "_contents4"))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.LEVEL_8, 5), BlockStateModelGenerator.createWeightedVariant(TextureMap.getSubId(Blocks.COMPOSTER, "_contents5"))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.LEVEL_8, 6), BlockStateModelGenerator.createWeightedVariant(TextureMap.getSubId(Blocks.COMPOSTER, "_contents6"))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.LEVEL_8, 7), BlockStateModelGenerator.createWeightedVariant(TextureMap.getSubId(Blocks.COMPOSTER, "_contents7"))).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.LEVEL_8, 8), BlockStateModelGenerator.createWeightedVariant(TextureMap.getSubId(Blocks.COMPOSTER, "_contents_ready"))));
    }

    private void registerCopperBulb(Block copperBulbBlock) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(Models.CUBE_ALL.upload(copperBulbBlock, TextureMap.all(copperBulbBlock), this.modelCollector));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(this.createSubModel(copperBulbBlock, "_powered", Models.CUBE_ALL, TextureMap::all));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(this.createSubModel(copperBulbBlock, "_lit", Models.CUBE_ALL, TextureMap::all));
        WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(this.createSubModel(copperBulbBlock, "_lit_powered", Models.CUBE_ALL, TextureMap::all));
        this.blockStateCollector.accept(BlockStateModelGenerator.createCopperBulbBlockState(copperBulbBlock, lv, lv3, lv2, lv4));
    }

    private static BlockModelDefinitionCreator createCopperBulbBlockState(Block block, WeightedVariant unlitUnpoweredModel, WeightedVariant litUnpoweredModel, WeightedVariant unlitPoweredModel, WeightedVariant litPoweredModel) {
        return VariantsBlockModelDefinitionCreator.of(block).with(BlockStateVariantMap.models(Properties.LIT, Properties.POWERED).generate((lit, powered) -> {
            if (lit.booleanValue()) {
                return powered != false ? litPoweredModel : litUnpoweredModel;
            }
            return powered != false ? unlitPoweredModel : unlitUnpoweredModel;
        }));
    }

    private void registerWaxedCopperBulb(Block unwaxedCopperBulbBlock, Block waxedCopperBulbBlock) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(unwaxedCopperBulbBlock));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(unwaxedCopperBulbBlock, "_powered"));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(unwaxedCopperBulbBlock, "_lit"));
        WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(unwaxedCopperBulbBlock, "_lit_powered"));
        this.itemModelOutput.acceptAlias(unwaxedCopperBulbBlock.asItem(), waxedCopperBulbBlock.asItem());
        this.blockStateCollector.accept(BlockStateModelGenerator.createCopperBulbBlockState(waxedCopperBulbBlock, lv, lv3, lv2, lv4));
    }

    private void registerAmethyst(Block block) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(Models.CROSS.upload(block, TextureMap.cross(block), this.modelCollector));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(block, lv).apply(UP_DEFAULT_ROTATION_OPERATIONS));
    }

    private void registerAmethysts() {
        this.registerAmethyst(Blocks.SMALL_AMETHYST_BUD);
        this.registerAmethyst(Blocks.MEDIUM_AMETHYST_BUD);
        this.registerAmethyst(Blocks.LARGE_AMETHYST_BUD);
        this.registerAmethyst(Blocks.AMETHYST_CLUSTER);
    }

    private void registerPointedDripstone() {
        BlockStateVariantMap.DoubleProperty<WeightedVariant, Direction, Thickness> lv = BlockStateVariantMap.models(Properties.VERTICAL_DIRECTION, Properties.THICKNESS);
        for (Thickness lv2 : Thickness.values()) {
            lv.register(Direction.UP, lv2, this.getDripstoneVariant(Direction.UP, lv2));
        }
        for (Thickness lv2 : Thickness.values()) {
            lv.register(Direction.DOWN, lv2, this.getDripstoneVariant(Direction.DOWN, lv2));
        }
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.POINTED_DRIPSTONE).with(lv));
    }

    private WeightedVariant getDripstoneVariant(Direction direction, Thickness thickness) {
        String string = "_" + direction.asString() + "_" + thickness.asString();
        TextureMap lv = TextureMap.cross(TextureMap.getSubId(Blocks.POINTED_DRIPSTONE, string));
        return BlockStateModelGenerator.createWeightedVariant(Models.POINTED_DRIPSTONE.upload(Blocks.POINTED_DRIPSTONE, string, lv, this.modelCollector));
    }

    private void registerNetherrackBottomCustomTop(Block block) {
        TextureMap lv = new TextureMap().put(TextureKey.BOTTOM, TextureMap.getId(Blocks.NETHERRACK)).put(TextureKey.TOP, TextureMap.getId(block)).put(TextureKey.SIDE, TextureMap.getSubId(block, "_side"));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(block, BlockStateModelGenerator.createWeightedVariant(Models.CUBE_BOTTOM_TOP.upload(block, lv, this.modelCollector))));
    }

    private void registerDaylightDetector() {
        Identifier lv = TextureMap.getSubId(Blocks.DAYLIGHT_DETECTOR, "_side");
        TextureMap lv2 = new TextureMap().put(TextureKey.TOP, TextureMap.getSubId(Blocks.DAYLIGHT_DETECTOR, "_top")).put(TextureKey.SIDE, lv);
        TextureMap lv3 = new TextureMap().put(TextureKey.TOP, TextureMap.getSubId(Blocks.DAYLIGHT_DETECTOR, "_inverted_top")).put(TextureKey.SIDE, lv);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.DAYLIGHT_DETECTOR).with(BlockStateVariantMap.models(Properties.INVERTED).register(false, BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_DAYLIGHT_DETECTOR.upload(Blocks.DAYLIGHT_DETECTOR, lv2, this.modelCollector))).register(true, BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_DAYLIGHT_DETECTOR.upload(ModelIds.getBlockSubModelId(Blocks.DAYLIGHT_DETECTOR, "_inverted"), lv3, this.modelCollector)))));
    }

    private void registerRod(Block block) {
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(block, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(block))).apply(UP_DEFAULT_ROTATION_OPERATIONS));
    }

    private void registerLightningRod(Block unwaxed, Block waxed) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.LIGHTNING_ROD, "_on"));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_LIGHTNING_ROD.upload(unwaxed, TextureMap.texture(unwaxed), this.modelCollector));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(unwaxed).with(BlockStateModelGenerator.createBooleanModelMap(Properties.POWERED, lv, lv2)).apply(UP_DEFAULT_ROTATION_OPERATIONS));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(waxed).with(BlockStateModelGenerator.createBooleanModelMap(Properties.POWERED, lv, lv2)).apply(UP_DEFAULT_ROTATION_OPERATIONS));
        this.itemModelOutput.acceptAlias(unwaxed.asItem(), waxed.asItem());
    }

    private void registerFarmland() {
        TextureMap lv = new TextureMap().put(TextureKey.DIRT, TextureMap.getId(Blocks.DIRT)).put(TextureKey.TOP, TextureMap.getId(Blocks.FARMLAND));
        TextureMap lv2 = new TextureMap().put(TextureKey.DIRT, TextureMap.getId(Blocks.DIRT)).put(TextureKey.TOP, TextureMap.getSubId(Blocks.FARMLAND, "_moist"));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_FARMLAND.upload(Blocks.FARMLAND, lv, this.modelCollector));
        WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_FARMLAND.upload(TextureMap.getSubId(Blocks.FARMLAND, "_moist"), lv2, this.modelCollector));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.FARMLAND).with(BlockStateModelGenerator.createValueFencedModelMap(Properties.MOISTURE, 7, lv4, lv3)));
    }

    private WeightedVariant getFireFloorModels(Block texture) {
        return BlockStateModelGenerator.createWeightedVariant(BlockStateModelGenerator.createModelVariant(Models.TEMPLATE_FIRE_FLOOR.upload(ModelIds.getBlockSubModelId(texture, "_floor0"), TextureMap.fire0(texture), this.modelCollector)), BlockStateModelGenerator.createModelVariant(Models.TEMPLATE_FIRE_FLOOR.upload(ModelIds.getBlockSubModelId(texture, "_floor1"), TextureMap.fire1(texture), this.modelCollector)));
    }

    private WeightedVariant getFireSideModels(Block texture) {
        return BlockStateModelGenerator.createWeightedVariant(BlockStateModelGenerator.createModelVariant(Models.TEMPLATE_FIRE_SIDE.upload(ModelIds.getBlockSubModelId(texture, "_side0"), TextureMap.fire0(texture), this.modelCollector)), BlockStateModelGenerator.createModelVariant(Models.TEMPLATE_FIRE_SIDE.upload(ModelIds.getBlockSubModelId(texture, "_side1"), TextureMap.fire1(texture), this.modelCollector)), BlockStateModelGenerator.createModelVariant(Models.TEMPLATE_FIRE_SIDE_ALT.upload(ModelIds.getBlockSubModelId(texture, "_side_alt0"), TextureMap.fire0(texture), this.modelCollector)), BlockStateModelGenerator.createModelVariant(Models.TEMPLATE_FIRE_SIDE_ALT.upload(ModelIds.getBlockSubModelId(texture, "_side_alt1"), TextureMap.fire1(texture), this.modelCollector)));
    }

    private WeightedVariant getFireUpModels(Block texture) {
        return BlockStateModelGenerator.createWeightedVariant(BlockStateModelGenerator.createModelVariant(Models.TEMPLATE_FIRE_UP.upload(ModelIds.getBlockSubModelId(texture, "_up0"), TextureMap.fire0(texture), this.modelCollector)), BlockStateModelGenerator.createModelVariant(Models.TEMPLATE_FIRE_UP.upload(ModelIds.getBlockSubModelId(texture, "_up1"), TextureMap.fire1(texture), this.modelCollector)), BlockStateModelGenerator.createModelVariant(Models.TEMPLATE_FIRE_UP_ALT.upload(ModelIds.getBlockSubModelId(texture, "_up_alt0"), TextureMap.fire0(texture), this.modelCollector)), BlockStateModelGenerator.createModelVariant(Models.TEMPLATE_FIRE_UP_ALT.upload(ModelIds.getBlockSubModelId(texture, "_up_alt1"), TextureMap.fire1(texture), this.modelCollector)));
    }

    private void registerFire() {
        MultipartModelConditionBuilder lv = BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.NORTH, false).put(Properties.EAST, false).put(Properties.SOUTH, false).put(Properties.WEST, false).put(Properties.UP, false);
        WeightedVariant lv2 = this.getFireFloorModels(Blocks.FIRE);
        WeightedVariant lv3 = this.getFireSideModels(Blocks.FIRE);
        WeightedVariant lv4 = this.getFireUpModels(Blocks.FIRE);
        this.blockStateCollector.accept(MultipartBlockModelDefinitionCreator.create(Blocks.FIRE).with(lv, lv2).with(BlockStateModelGenerator.or(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.NORTH, true), lv), lv3).with(BlockStateModelGenerator.or(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.EAST, true), lv), lv3.apply(ROTATE_Y_90)).with(BlockStateModelGenerator.or(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.SOUTH, true), lv), lv3.apply(ROTATE_Y_180)).with(BlockStateModelGenerator.or(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.WEST, true), lv), lv3.apply(ROTATE_Y_270)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.UP, true), lv4));
    }

    private void registerSoulFire() {
        WeightedVariant lv = this.getFireFloorModels(Blocks.SOUL_FIRE);
        WeightedVariant lv2 = this.getFireSideModels(Blocks.SOUL_FIRE);
        this.blockStateCollector.accept(MultipartBlockModelDefinitionCreator.create(Blocks.SOUL_FIRE).with(lv).with(lv2).with(lv2.apply(ROTATE_Y_90)).with(lv2.apply(ROTATE_Y_180)).with(lv2.apply(ROTATE_Y_270)));
    }

    private void registerLantern(Block lantern) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(TexturedModel.TEMPLATE_LANTERN.upload(lantern, this.modelCollector));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(TexturedModel.TEMPLATE_HANGING_LANTERN.upload(lantern, this.modelCollector));
        this.registerItemModel(lantern.asItem());
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(lantern).with(BlockStateModelGenerator.createBooleanModelMap(Properties.HANGING, lv2, lv)));
    }

    private void registerCopperLantern(Block unwaxed, Block waxed) {
        Identifier lv = TexturedModel.TEMPLATE_LANTERN.upload(unwaxed, this.modelCollector);
        Identifier lv2 = TexturedModel.TEMPLATE_HANGING_LANTERN.upload(unwaxed, this.modelCollector);
        this.registerItemModel(unwaxed.asItem());
        this.itemModelOutput.acceptAlias(unwaxed.asItem(), waxed.asItem());
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(unwaxed).with(BlockStateModelGenerator.createBooleanModelMap(Properties.HANGING, BlockStateModelGenerator.createWeightedVariant(lv2), BlockStateModelGenerator.createWeightedVariant(lv))));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(waxed).with(BlockStateModelGenerator.createBooleanModelMap(Properties.HANGING, BlockStateModelGenerator.createWeightedVariant(lv2), BlockStateModelGenerator.createWeightedVariant(lv))));
    }

    private void registerCopperChain(Block unwaxed, Block waxed) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(TexturedModel.TEMPLATE_CHAIN.upload(unwaxed, this.modelCollector));
        this.registerAxisRotated(unwaxed, lv);
        this.registerAxisRotated(waxed, lv);
    }

    private void registerMuddyMangroveRoots() {
        TextureMap lv = TextureMap.sideEnd(TextureMap.getSubId(Blocks.MUDDY_MANGROVE_ROOTS, "_side"), TextureMap.getSubId(Blocks.MUDDY_MANGROVE_ROOTS, "_top"));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.CUBE_COLUMN.upload(Blocks.MUDDY_MANGROVE_ROOTS, lv, this.modelCollector));
        this.blockStateCollector.accept(BlockStateModelGenerator.createAxisRotatedBlockState(Blocks.MUDDY_MANGROVE_ROOTS, lv2));
    }

    private void registerMangrovePropagule() {
        this.registerItemModel(Items.MANGROVE_PROPAGULE);
        Block lv = Blocks.MANGROVE_PROPAGULE;
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(lv));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.MANGROVE_PROPAGULE).with(BlockStateVariantMap.models(PropaguleBlock.HANGING, PropaguleBlock.AGE).generate((hanging, age) -> hanging != false ? BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(lv, "_hanging_" + age)) : lv2)));
    }

    private void registerFrostedIce() {
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.FROSTED_ICE).with(BlockStateVariantMap.models(Properties.AGE_3).register(0, BlockStateModelGenerator.createWeightedVariant(this.createSubModel(Blocks.FROSTED_ICE, "_0", Models.CUBE_ALL, TextureMap::all))).register(1, BlockStateModelGenerator.createWeightedVariant(this.createSubModel(Blocks.FROSTED_ICE, "_1", Models.CUBE_ALL, TextureMap::all))).register(2, BlockStateModelGenerator.createWeightedVariant(this.createSubModel(Blocks.FROSTED_ICE, "_2", Models.CUBE_ALL, TextureMap::all))).register(3, BlockStateModelGenerator.createWeightedVariant(this.createSubModel(Blocks.FROSTED_ICE, "_3", Models.CUBE_ALL, TextureMap::all)))));
    }

    private void registerTopSoils() {
        Identifier lv = TextureMap.getId(Blocks.DIRT);
        TextureMap lv2 = new TextureMap().put(TextureKey.BOTTOM, lv).inherit(TextureKey.BOTTOM, TextureKey.PARTICLE).put(TextureKey.TOP, TextureMap.getSubId(Blocks.GRASS_BLOCK, "_top")).put(TextureKey.SIDE, TextureMap.getSubId(Blocks.GRASS_BLOCK, "_snow"));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(Models.CUBE_BOTTOM_TOP.upload(Blocks.GRASS_BLOCK, "_snow", lv2, this.modelCollector));
        Identifier lv4 = ModelIds.getBlockModelId(Blocks.GRASS_BLOCK);
        this.registerTopSoil(Blocks.GRASS_BLOCK, BlockStateModelGenerator.modelWithYRotation(BlockStateModelGenerator.createModelVariant(lv4)), lv3);
        this.registerTintedItemModel(Blocks.GRASS_BLOCK, lv4, new GrassTintSource());
        WeightedVariant lv5 = BlockStateModelGenerator.modelWithYRotation(BlockStateModelGenerator.createModelVariant(TexturedModel.CUBE_BOTTOM_TOP.get(Blocks.MYCELIUM).textures(textures -> textures.put(TextureKey.BOTTOM, lv)).upload(Blocks.MYCELIUM, this.modelCollector)));
        this.registerTopSoil(Blocks.MYCELIUM, lv5, lv3);
        WeightedVariant lv6 = BlockStateModelGenerator.modelWithYRotation(BlockStateModelGenerator.createModelVariant(TexturedModel.CUBE_BOTTOM_TOP.get(Blocks.PODZOL).textures(textures -> textures.put(TextureKey.BOTTOM, lv)).upload(Blocks.PODZOL, this.modelCollector)));
        this.registerTopSoil(Blocks.PODZOL, lv6, lv3);
    }

    private void registerTopSoil(Block topSoil, WeightedVariant regularVariant, WeightedVariant snowyVariant) {
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(topSoil).with(BlockStateVariantMap.models(Properties.SNOWY).register(true, snowyVariant).register(false, regularVariant)));
    }

    private void registerCocoa() {
        this.registerItemModel(Items.COCOA_BEANS);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.COCOA).with(BlockStateVariantMap.models(Properties.AGE_2).register(0, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.COCOA, "_stage0"))).register(1, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.COCOA, "_stage1"))).register(2, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.COCOA, "_stage2")))).apply(SOUTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS));
    }

    private void registerDirtPath() {
        ModelVariant lv = BlockStateModelGenerator.createModelVariant(ModelIds.getBlockModelId(Blocks.DIRT_PATH));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.DIRT_PATH, BlockStateModelGenerator.modelWithYRotation(lv)));
    }

    private void registerWeightedPressurePlate(Block weightedPressurePlate, Block textureSource) {
        TextureMap lv = TextureMap.texture(textureSource);
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.PRESSURE_PLATE_UP.upload(weightedPressurePlate, lv, this.modelCollector));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(Models.PRESSURE_PLATE_DOWN.upload(weightedPressurePlate, lv, this.modelCollector));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(weightedPressurePlate).with(BlockStateModelGenerator.createValueFencedModelMap(Properties.POWER, 1, lv3, lv2)));
    }

    private void registerHopper() {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(Blocks.HOPPER));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.HOPPER, "_side"));
        this.registerItemModel(Items.HOPPER);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.HOPPER).with(BlockStateVariantMap.models(Properties.HOPPER_FACING).register(Direction.DOWN, lv).register(Direction.NORTH, lv2).register(Direction.EAST, lv2.apply(ROTATE_Y_90)).register(Direction.SOUTH, lv2.apply(ROTATE_Y_180)).register(Direction.WEST, lv2.apply(ROTATE_Y_270))));
    }

    private void registerParented(Block modelSource, Block child) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(modelSource));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(child, lv));
        this.itemModelOutput.acceptAlias(modelSource.asItem(), child.asItem());
    }

    private void registerBars(Block block) {
        TextureMap lv = TextureMap.bars(block);
        this.registerBars(block, Models.TEMPLATE_BARS_POST_ENDS.upload(block, lv, this.modelCollector), Models.TEMPLATE_BARS_POST.upload(block, lv, this.modelCollector), Models.TEMPLATE_BARS_CAP.upload(block, lv, this.modelCollector), Models.TEMPLATE_BARS_CAP_ALT.upload(block, lv, this.modelCollector), Models.TEMPLATE_BARS_SIDE.upload(block, lv, this.modelCollector), Models.TEMPLATE_BARS_SIDE_ALT.upload(block, lv, this.modelCollector));
        this.registerItemModel(block);
    }

    private void registerCopperBars(Block unwaxedBlock, Block waxedBlock) {
        TextureMap lv = TextureMap.bars(unwaxedBlock);
        Identifier lv2 = Models.TEMPLATE_BARS_POST_ENDS.upload(unwaxedBlock, lv, this.modelCollector);
        Identifier lv3 = Models.TEMPLATE_BARS_POST.upload(unwaxedBlock, lv, this.modelCollector);
        Identifier lv4 = Models.TEMPLATE_BARS_CAP.upload(unwaxedBlock, lv, this.modelCollector);
        Identifier lv5 = Models.TEMPLATE_BARS_CAP_ALT.upload(unwaxedBlock, lv, this.modelCollector);
        Identifier lv6 = Models.TEMPLATE_BARS_SIDE.upload(unwaxedBlock, lv, this.modelCollector);
        Identifier lv7 = Models.TEMPLATE_BARS_SIDE_ALT.upload(unwaxedBlock, lv, this.modelCollector);
        this.registerBars(unwaxedBlock, lv2, lv3, lv4, lv5, lv6, lv7);
        this.registerBars(waxedBlock, lv2, lv3, lv4, lv5, lv6, lv7);
        this.registerItemModel(unwaxedBlock);
        this.itemModelOutput.acceptAlias(unwaxedBlock.asItem(), waxedBlock.asItem());
    }

    private void registerBars(Block block, Identifier postEndsModelId, Identifier postModelId, Identifier capModelId, Identifier capAltModelId, Identifier sideModelId, Identifier sideAltModelId) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(postEndsModelId);
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(postModelId);
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(capModelId);
        WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(capAltModelId);
        WeightedVariant lv5 = BlockStateModelGenerator.createWeightedVariant(sideModelId);
        WeightedVariant lv6 = BlockStateModelGenerator.createWeightedVariant(sideAltModelId);
        this.blockStateCollector.accept(MultipartBlockModelDefinitionCreator.create(block).with(lv).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.NORTH, false).put(Properties.EAST, false).put(Properties.SOUTH, false).put(Properties.WEST, false), lv2).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.NORTH, true).put(Properties.EAST, false).put(Properties.SOUTH, false).put(Properties.WEST, false), lv3).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.NORTH, false).put(Properties.EAST, true).put(Properties.SOUTH, false).put(Properties.WEST, false), lv3.apply(ROTATE_Y_90)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.NORTH, false).put(Properties.EAST, false).put(Properties.SOUTH, true).put(Properties.WEST, false), lv4).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.NORTH, false).put(Properties.EAST, false).put(Properties.SOUTH, false).put(Properties.WEST, true), lv4.apply(ROTATE_Y_90)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.NORTH, true), lv5).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.EAST, true), lv5.apply(ROTATE_Y_90)).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.SOUTH, true), lv6).with(BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.WEST, true), lv6.apply(ROTATE_Y_90)));
    }

    private void registerNorthDefaultHorizontalRotatable(Block block) {
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(block, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(block))).apply(NORTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS));
    }

    private void registerLever() {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(Blocks.LEVER));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.LEVER, "_on"));
        this.registerItemModel(Blocks.LEVER);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.LEVER).with(BlockStateModelGenerator.createBooleanModelMap(Properties.POWERED, lv, lv2)).apply(BlockStateVariantMap.operations(Properties.BLOCK_FACE, Properties.HORIZONTAL_FACING).register(BlockFace.CEILING, Direction.NORTH, ROTATE_X_180.then(ROTATE_Y_180)).register(BlockFace.CEILING, Direction.EAST, ROTATE_X_180.then(ROTATE_Y_270)).register(BlockFace.CEILING, Direction.SOUTH, ROTATE_X_180).register(BlockFace.CEILING, Direction.WEST, ROTATE_X_180.then(ROTATE_Y_90)).register(BlockFace.FLOOR, Direction.NORTH, NO_OP).register(BlockFace.FLOOR, Direction.EAST, ROTATE_Y_90).register(BlockFace.FLOOR, Direction.SOUTH, ROTATE_Y_180).register(BlockFace.FLOOR, Direction.WEST, ROTATE_Y_270).register(BlockFace.WALL, Direction.NORTH, ROTATE_X_90).register(BlockFace.WALL, Direction.EAST, ROTATE_X_90.then(ROTATE_Y_90)).register(BlockFace.WALL, Direction.SOUTH, ROTATE_X_90.then(ROTATE_Y_180)).register(BlockFace.WALL, Direction.WEST, ROTATE_X_90.then(ROTATE_Y_270))));
    }

    private void registerLilyPad() {
        Identifier lv = this.uploadBlockItemModel(Items.LILY_PAD, Blocks.LILY_PAD);
        this.registerTintedItemModel(Blocks.LILY_PAD, lv, ItemModels.constantTintSource(-9321636));
        ModelVariant lv2 = BlockStateModelGenerator.createModelVariant(ModelIds.getBlockModelId(Blocks.LILY_PAD));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.LILY_PAD, BlockStateModelGenerator.modelWithYRotation(lv2)));
    }

    private void registerFrogspawn() {
        this.registerItemModel(Blocks.FROGSPAWN);
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(Blocks.FROGSPAWN, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(Blocks.FROGSPAWN))));
    }

    private void registerNetherPortal() {
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.NETHER_PORTAL).with(BlockStateVariantMap.models(Properties.HORIZONTAL_AXIS).register(Direction.Axis.X, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.NETHER_PORTAL, "_ns"))).register(Direction.Axis.Z, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.NETHER_PORTAL, "_ew")))));
    }

    private void registerNetherrack() {
        ModelVariant lv = BlockStateModelGenerator.createModelVariant(TexturedModel.CUBE_ALL.upload(Blocks.NETHERRACK, this.modelCollector));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.NETHERRACK, BlockStateModelGenerator.createWeightedVariant(lv, lv.with(ROTATE_X_90), lv.with(ROTATE_X_180), lv.with(ROTATE_X_270), lv.with(ROTATE_Y_90), lv.with(ROTATE_Y_90.then(ROTATE_X_90)), lv.with(ROTATE_Y_90.then(ROTATE_X_180)), lv.with(ROTATE_Y_90.then(ROTATE_X_270)), lv.with(ROTATE_Y_180), lv.with(ROTATE_Y_180.then(ROTATE_X_90)), lv.with(ROTATE_Y_180.then(ROTATE_X_180)), lv.with(ROTATE_Y_180.then(ROTATE_X_270)), lv.with(ROTATE_Y_270), lv.with(ROTATE_Y_270.then(ROTATE_X_90)), lv.with(ROTATE_Y_270.then(ROTATE_X_180)), lv.with(ROTATE_Y_270.then(ROTATE_X_270)))));
    }

    private void registerObserver() {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(Blocks.OBSERVER));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.OBSERVER, "_on"));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.OBSERVER).with(BlockStateModelGenerator.createBooleanModelMap(Properties.POWERED, lv2, lv)).apply(NORTH_DEFAULT_ROTATION_OPERATIONS));
    }

    private void registerPistons() {
        TextureMap lv = new TextureMap().put(TextureKey.BOTTOM, TextureMap.getSubId(Blocks.PISTON, "_bottom")).put(TextureKey.SIDE, TextureMap.getSubId(Blocks.PISTON, "_side"));
        Identifier lv2 = TextureMap.getSubId(Blocks.PISTON, "_top_sticky");
        Identifier lv3 = TextureMap.getSubId(Blocks.PISTON, "_top");
        TextureMap lv4 = lv.copyAndAdd(TextureKey.PLATFORM, lv2);
        TextureMap lv5 = lv.copyAndAdd(TextureKey.PLATFORM, lv3);
        WeightedVariant lv6 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.PISTON, "_base"));
        this.registerPiston(Blocks.PISTON, lv6, lv5);
        this.registerPiston(Blocks.STICKY_PISTON, lv6, lv4);
        Identifier lv7 = Models.CUBE_BOTTOM_TOP.upload(Blocks.PISTON, "_inventory", lv.copyAndAdd(TextureKey.TOP, lv3), this.modelCollector);
        Identifier lv8 = Models.CUBE_BOTTOM_TOP.upload(Blocks.STICKY_PISTON, "_inventory", lv.copyAndAdd(TextureKey.TOP, lv2), this.modelCollector);
        this.registerParentedItemModel(Blocks.PISTON, lv7);
        this.registerParentedItemModel(Blocks.STICKY_PISTON, lv8);
    }

    private void registerPiston(Block piston, WeightedVariant arg2, TextureMap textures) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_PISTON.upload(piston, textures, this.modelCollector));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(piston).with(BlockStateModelGenerator.createBooleanModelMap(Properties.EXTENDED, arg2, lv)).apply(NORTH_DEFAULT_ROTATION_OPERATIONS));
    }

    private void registerPistonHead() {
        TextureMap lv = new TextureMap().put(TextureKey.UNSTICKY, TextureMap.getSubId(Blocks.PISTON, "_top")).put(TextureKey.SIDE, TextureMap.getSubId(Blocks.PISTON, "_side"));
        TextureMap lv2 = lv.copyAndAdd(TextureKey.PLATFORM, TextureMap.getSubId(Blocks.PISTON, "_top_sticky"));
        TextureMap lv3 = lv.copyAndAdd(TextureKey.PLATFORM, TextureMap.getSubId(Blocks.PISTON, "_top"));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.PISTON_HEAD).with(BlockStateVariantMap.models(Properties.SHORT, Properties.PISTON_TYPE).register(false, PistonType.DEFAULT, BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_PISTON_HEAD.upload(Blocks.PISTON, "_head", lv3, this.modelCollector))).register(false, PistonType.STICKY, BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_PISTON_HEAD.upload(Blocks.PISTON, "_head_sticky", lv2, this.modelCollector))).register(true, PistonType.DEFAULT, BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_PISTON_HEAD_SHORT.upload(Blocks.PISTON, "_head_short", lv3, this.modelCollector))).register(true, PistonType.STICKY, BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_PISTON_HEAD_SHORT.upload(Blocks.PISTON, "_head_short_sticky", lv2, this.modelCollector)))).apply(NORTH_DEFAULT_ROTATION_OPERATIONS));
    }

    private void registerTrialSpawner() {
        Block lv = Blocks.TRIAL_SPAWNER;
        TextureMap lv2 = TextureMap.trialSpawner(lv, "_side_inactive", "_top_inactive");
        TextureMap lv3 = TextureMap.trialSpawner(lv, "_side_active", "_top_active");
        TextureMap lv4 = TextureMap.trialSpawner(lv, "_side_active", "_top_ejecting_reward");
        TextureMap lv5 = TextureMap.trialSpawner(lv, "_side_inactive_ominous", "_top_inactive_ominous");
        TextureMap lv6 = TextureMap.trialSpawner(lv, "_side_active_ominous", "_top_active_ominous");
        TextureMap lv7 = TextureMap.trialSpawner(lv, "_side_active_ominous", "_top_ejecting_reward_ominous");
        Identifier lv8 = Models.CUBE_BOTTOM_TOP_INNER_FACES.upload(lv, lv2, this.modelCollector);
        WeightedVariant lv9 = BlockStateModelGenerator.createWeightedVariant(lv8);
        WeightedVariant lv10 = BlockStateModelGenerator.createWeightedVariant(Models.CUBE_BOTTOM_TOP_INNER_FACES.upload(lv, "_active", lv3, this.modelCollector));
        WeightedVariant lv11 = BlockStateModelGenerator.createWeightedVariant(Models.CUBE_BOTTOM_TOP_INNER_FACES.upload(lv, "_ejecting_reward", lv4, this.modelCollector));
        WeightedVariant lv12 = BlockStateModelGenerator.createWeightedVariant(Models.CUBE_BOTTOM_TOP_INNER_FACES.upload(lv, "_inactive_ominous", lv5, this.modelCollector));
        WeightedVariant lv13 = BlockStateModelGenerator.createWeightedVariant(Models.CUBE_BOTTOM_TOP_INNER_FACES.upload(lv, "_active_ominous", lv6, this.modelCollector));
        WeightedVariant lv14 = BlockStateModelGenerator.createWeightedVariant(Models.CUBE_BOTTOM_TOP_INNER_FACES.upload(lv, "_ejecting_reward_ominous", lv7, this.modelCollector));
        this.registerParentedItemModel(lv, lv8);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(lv).with(BlockStateVariantMap.models(Properties.TRIAL_SPAWNER_STATE, Properties.OMINOUS).generate((state, ominous) -> switch (state) {
            default -> throw new MatchException(null, null);
            case TrialSpawnerState.INACTIVE, TrialSpawnerState.COOLDOWN -> {
                if (ominous.booleanValue()) {
                    yield lv12;
                }
                yield lv9;
            }
            case TrialSpawnerState.WAITING_FOR_PLAYERS, TrialSpawnerState.ACTIVE, TrialSpawnerState.WAITING_FOR_REWARD_EJECTION -> {
                if (ominous.booleanValue()) {
                    yield lv13;
                }
                yield lv10;
            }
            case TrialSpawnerState.EJECTING_REWARD -> ominous != false ? lv14 : lv11;
        })));
    }

    private void registerVault() {
        Block lv = Blocks.VAULT;
        TextureMap lv2 = TextureMap.vault(lv, "_front_off", "_side_off", "_top", "_bottom");
        TextureMap lv3 = TextureMap.vault(lv, "_front_on", "_side_on", "_top", "_bottom");
        TextureMap lv4 = TextureMap.vault(lv, "_front_ejecting", "_side_on", "_top", "_bottom");
        TextureMap lv5 = TextureMap.vault(lv, "_front_ejecting", "_side_on", "_top_ejecting", "_bottom");
        Identifier lv6 = Models.TEMPLATE_VAULT.upload(lv, lv2, this.modelCollector);
        WeightedVariant lv7 = BlockStateModelGenerator.createWeightedVariant(lv6);
        WeightedVariant lv8 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_VAULT.upload(lv, "_active", lv3, this.modelCollector));
        WeightedVariant lv9 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_VAULT.upload(lv, "_unlocking", lv4, this.modelCollector));
        WeightedVariant lv10 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_VAULT.upload(lv, "_ejecting_reward", lv5, this.modelCollector));
        TextureMap lv11 = TextureMap.vault(lv, "_front_off_ominous", "_side_off_ominous", "_top_ominous", "_bottom_ominous");
        TextureMap lv12 = TextureMap.vault(lv, "_front_on_ominous", "_side_on_ominous", "_top_ominous", "_bottom_ominous");
        TextureMap lv13 = TextureMap.vault(lv, "_front_ejecting_ominous", "_side_on_ominous", "_top_ominous", "_bottom_ominous");
        TextureMap lv14 = TextureMap.vault(lv, "_front_ejecting_ominous", "_side_on_ominous", "_top_ejecting_ominous", "_bottom_ominous");
        WeightedVariant lv15 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_VAULT.upload(lv, "_ominous", lv11, this.modelCollector));
        WeightedVariant lv16 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_VAULT.upload(lv, "_active_ominous", lv12, this.modelCollector));
        WeightedVariant lv17 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_VAULT.upload(lv, "_unlocking_ominous", lv13, this.modelCollector));
        WeightedVariant lv18 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_VAULT.upload(lv, "_ejecting_reward_ominous", lv14, this.modelCollector));
        this.registerParentedItemModel(lv, lv6);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(lv).with(BlockStateVariantMap.models(VaultBlock.VAULT_STATE, VaultBlock.OMINOUS).generate((state, ominous) -> switch (state) {
            default -> throw new MatchException(null, null);
            case VaultState.INACTIVE -> {
                if (ominous.booleanValue()) {
                    yield lv15;
                }
                yield lv7;
            }
            case VaultState.ACTIVE -> {
                if (ominous.booleanValue()) {
                    yield lv16;
                }
                yield lv8;
            }
            case VaultState.UNLOCKING -> {
                if (ominous.booleanValue()) {
                    yield lv17;
                }
                yield lv9;
            }
            case VaultState.EJECTING -> ominous != false ? lv18 : lv10;
        })).apply(NORTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS));
    }

    private void registerSculkSensor() {
        Identifier lv = ModelIds.getBlockSubModelId(Blocks.SCULK_SENSOR, "_inactive");
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(lv);
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.SCULK_SENSOR, "_active"));
        this.registerParentedItemModel(Blocks.SCULK_SENSOR, lv);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.SCULK_SENSOR).with(BlockStateVariantMap.models(Properties.SCULK_SENSOR_PHASE).generate(phase -> phase == SculkSensorPhase.ACTIVE || phase == SculkSensorPhase.COOLDOWN ? lv3 : lv2)));
    }

    private void registerCalibratedSculkSensor() {
        Identifier lv = ModelIds.getBlockSubModelId(Blocks.CALIBRATED_SCULK_SENSOR, "_inactive");
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(lv);
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.CALIBRATED_SCULK_SENSOR, "_active"));
        this.registerParentedItemModel(Blocks.CALIBRATED_SCULK_SENSOR, lv);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.CALIBRATED_SCULK_SENSOR).with(BlockStateVariantMap.models(Properties.SCULK_SENSOR_PHASE).generate(phase -> phase == SculkSensorPhase.ACTIVE || phase == SculkSensorPhase.COOLDOWN ? lv3 : lv2)).apply(NORTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS));
    }

    private void registerSculkShrieker() {
        Identifier lv = Models.TEMPLATE_SCULK_SHRIEKER.upload(Blocks.SCULK_SHRIEKER, TextureMap.sculkShrieker(false), this.modelCollector);
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(lv);
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_SCULK_SHRIEKER.upload(Blocks.SCULK_SHRIEKER, "_can_summon", TextureMap.sculkShrieker(true), this.modelCollector));
        this.registerParentedItemModel(Blocks.SCULK_SHRIEKER, lv);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.SCULK_SHRIEKER).with(BlockStateModelGenerator.createBooleanModelMap(Properties.CAN_SUMMON, lv3, lv2)));
    }

    private void registerScaffolding() {
        Identifier lv = ModelIds.getBlockSubModelId(Blocks.SCAFFOLDING, "_stable");
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(lv);
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.SCAFFOLDING, "_unstable"));
        this.registerParentedItemModel(Blocks.SCAFFOLDING, lv);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.SCAFFOLDING).with(BlockStateModelGenerator.createBooleanModelMap(Properties.BOTTOM, lv3, lv2)));
    }

    private void registerCaveVines() {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(this.createSubModel(Blocks.CAVE_VINES, "", Models.CROSS, TextureMap::cross));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(this.createSubModel(Blocks.CAVE_VINES, "_lit", Models.CROSS, TextureMap::cross));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.CAVE_VINES).with(BlockStateModelGenerator.createBooleanModelMap(Properties.BERRIES, lv2, lv)));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(this.createSubModel(Blocks.CAVE_VINES_PLANT, "", Models.CROSS, TextureMap::cross));
        WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(this.createSubModel(Blocks.CAVE_VINES_PLANT, "_lit", Models.CROSS, TextureMap::cross));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.CAVE_VINES_PLANT).with(BlockStateModelGenerator.createBooleanModelMap(Properties.BERRIES, lv4, lv3)));
    }

    private void registerRedstoneLamp() {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(TexturedModel.CUBE_ALL.upload(Blocks.REDSTONE_LAMP, this.modelCollector));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(this.createSubModel(Blocks.REDSTONE_LAMP, "_on", Models.CUBE_ALL, TextureMap::all));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.REDSTONE_LAMP).with(BlockStateModelGenerator.createBooleanModelMap(Properties.LIT, lv2, lv)));
    }

    private void registerTorch(Block torch, Block wallTorch) {
        TextureMap lv = TextureMap.torch(torch);
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(torch, BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_TORCH.upload(torch, lv, this.modelCollector))));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(wallTorch, BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_TORCH_WALL.upload(wallTorch, lv, this.modelCollector))).apply(EAST_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS));
        this.registerItemModel(torch);
    }

    private void registerRedstoneTorch() {
        TextureMap lv = TextureMap.torch(Blocks.REDSTONE_TORCH);
        TextureMap lv2 = TextureMap.torch(TextureMap.getSubId(Blocks.REDSTONE_TORCH, "_off"));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_REDSTONE_TORCH.upload(Blocks.REDSTONE_TORCH, lv, this.modelCollector));
        WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_TORCH_UNLIT.upload(Blocks.REDSTONE_TORCH, "_off", lv2, this.modelCollector));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.REDSTONE_TORCH).with(BlockStateModelGenerator.createBooleanModelMap(Properties.LIT, lv3, lv4)));
        WeightedVariant lv5 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_REDSTONE_TORCH_WALL.upload(Blocks.REDSTONE_WALL_TORCH, lv, this.modelCollector));
        WeightedVariant lv6 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_TORCH_WALL_UNLIT.upload(Blocks.REDSTONE_WALL_TORCH, "_off", lv2, this.modelCollector));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.REDSTONE_WALL_TORCH).with(BlockStateModelGenerator.createBooleanModelMap(Properties.LIT, lv5, lv6)).apply(EAST_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS));
        this.registerItemModel(Blocks.REDSTONE_TORCH);
    }

    private void registerRepeater() {
        this.registerItemModel(Items.REPEATER);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.REPEATER).with(BlockStateVariantMap.models(Properties.DELAY, Properties.LOCKED, Properties.POWERED).generate((tick, locked, on) -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append('_').append(tick).append("tick");
            if (on.booleanValue()) {
                stringBuilder.append("_on");
            }
            if (locked.booleanValue()) {
                stringBuilder.append("_locked");
            }
            return BlockStateModelGenerator.createWeightedVariant(TextureMap.getSubId(Blocks.REPEATER, stringBuilder.toString()));
        })).apply(SOUTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS));
    }

    private void registerSeaPickle() {
        this.registerItemModel(Items.SEA_PICKLE);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.SEA_PICKLE).with(BlockStateVariantMap.models(Properties.PICKLES, Properties.WATERLOGGED).register(1, false, BlockStateModelGenerator.modelWithYRotation(BlockStateModelGenerator.createModelVariant(ModelIds.getMinecraftNamespacedBlock("dead_sea_pickle")))).register(2, false, BlockStateModelGenerator.modelWithYRotation(BlockStateModelGenerator.createModelVariant(ModelIds.getMinecraftNamespacedBlock("two_dead_sea_pickles")))).register(3, false, BlockStateModelGenerator.modelWithYRotation(BlockStateModelGenerator.createModelVariant(ModelIds.getMinecraftNamespacedBlock("three_dead_sea_pickles")))).register(4, false, BlockStateModelGenerator.modelWithYRotation(BlockStateModelGenerator.createModelVariant(ModelIds.getMinecraftNamespacedBlock("four_dead_sea_pickles")))).register(1, true, BlockStateModelGenerator.modelWithYRotation(BlockStateModelGenerator.createModelVariant(ModelIds.getMinecraftNamespacedBlock("sea_pickle")))).register(2, true, BlockStateModelGenerator.modelWithYRotation(BlockStateModelGenerator.createModelVariant(ModelIds.getMinecraftNamespacedBlock("two_sea_pickles")))).register(3, true, BlockStateModelGenerator.modelWithYRotation(BlockStateModelGenerator.createModelVariant(ModelIds.getMinecraftNamespacedBlock("three_sea_pickles")))).register(4, true, BlockStateModelGenerator.modelWithYRotation(BlockStateModelGenerator.createModelVariant(ModelIds.getMinecraftNamespacedBlock("four_sea_pickles"))))));
    }

    private void registerSnows() {
        TextureMap lv = TextureMap.all(Blocks.SNOW);
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.CUBE_ALL.upload(Blocks.SNOW_BLOCK, lv, this.modelCollector));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.SNOW).with(BlockStateVariantMap.models(Properties.LAYERS).generate(layers -> layers < 8 ? BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.SNOW, "_height" + layers * 2)) : lv2)));
        this.registerParentedItemModel(Blocks.SNOW, ModelIds.getBlockSubModelId(Blocks.SNOW, "_height2"));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(Blocks.SNOW_BLOCK, lv2));
    }

    private void registerStonecutter() {
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.STONECUTTER, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(Blocks.STONECUTTER))).apply(NORTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS));
    }

    private void registerStructureBlock() {
        Identifier lv = TexturedModel.CUBE_ALL.upload(Blocks.STRUCTURE_BLOCK, this.modelCollector);
        this.registerParentedItemModel(Blocks.STRUCTURE_BLOCK, lv);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.STRUCTURE_BLOCK).with(BlockStateVariantMap.models(Properties.STRUCTURE_BLOCK_MODE).generate(mode -> BlockStateModelGenerator.createWeightedVariant(this.createSubModel(Blocks.STRUCTURE_BLOCK, "_" + mode.asString(), Models.CUBE_ALL, TextureMap::all)))));
    }

    private void registerTestBlock() {
        HashMap<TestBlockMode, Identifier> map = new HashMap<TestBlockMode, Identifier>();
        for (TestBlockMode lv : TestBlockMode.values()) {
            map.put(lv, this.createSubModel(Blocks.TEST_BLOCK, "_" + lv.asString(), Models.CUBE_ALL, TextureMap::all));
        }
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.TEST_BLOCK).with(BlockStateVariantMap.models(Properties.TEST_BLOCK_MODE).generate(mode -> BlockStateModelGenerator.createWeightedVariant((Identifier)map.get(mode)))));
        this.itemModelOutput.accept(Items.TEST_BLOCK, ItemModels.select(TestBlock.MODE, ItemModels.basic((Identifier)map.get(TestBlockMode.START)), Map.of(TestBlockMode.FAIL, ItemModels.basic((Identifier)map.get(TestBlockMode.FAIL)), TestBlockMode.LOG, ItemModels.basic((Identifier)map.get(TestBlockMode.LOG)), TestBlockMode.ACCEPT, ItemModels.basic((Identifier)map.get(TestBlockMode.ACCEPT)))));
    }

    private void registerSweetBerryBush() {
        this.registerItemModel(Items.SWEET_BERRIES);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.SWEET_BERRY_BUSH).with(BlockStateVariantMap.models(Properties.AGE_3).generate(stage -> BlockStateModelGenerator.createWeightedVariant(this.createSubModel(Blocks.SWEET_BERRY_BUSH, "_stage" + stage, Models.CROSS, TextureMap::cross)))));
    }

    private void registerTripwire() {
        this.registerItemModel(Items.STRING);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.TRIPWIRE).with(BlockStateVariantMap.models(Properties.ATTACHED, Properties.EAST, Properties.NORTH, Properties.SOUTH, Properties.WEST).register(false, false, false, false, false, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_ns"))).register(false, true, false, false, false, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_n")).apply(ROTATE_Y_90)).register(false, false, true, false, false, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_n"))).register(false, false, false, true, false, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_n")).apply(ROTATE_Y_180)).register(false, false, false, false, true, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_n")).apply(ROTATE_Y_270)).register(false, true, true, false, false, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_ne"))).register(false, true, false, true, false, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_ne")).apply(ROTATE_Y_90)).register(false, false, false, true, true, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_ne")).apply(ROTATE_Y_180)).register(false, false, true, false, true, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_ne")).apply(ROTATE_Y_270)).register(false, false, true, true, false, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_ns"))).register(false, true, false, false, true, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_ns")).apply(ROTATE_Y_90)).register(false, true, true, true, false, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_nse"))).register(false, true, false, true, true, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_nse")).apply(ROTATE_Y_90)).register(false, false, true, true, true, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_nse")).apply(ROTATE_Y_180)).register(false, true, true, false, true, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_nse")).apply(ROTATE_Y_270)).register(false, true, true, true, true, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_nsew"))).register(true, false, false, false, false, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_attached_ns"))).register(true, false, true, false, false, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_attached_n"))).register(true, false, false, true, false, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_attached_n")).apply(ROTATE_Y_180)).register(true, true, false, false, false, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_attached_n")).apply(ROTATE_Y_90)).register(true, false, false, false, true, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_attached_n")).apply(ROTATE_Y_270)).register(true, true, true, false, false, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_attached_ne"))).register(true, true, false, true, false, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_attached_ne")).apply(ROTATE_Y_90)).register(true, false, false, true, true, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_attached_ne")).apply(ROTATE_Y_180)).register(true, false, true, false, true, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_attached_ne")).apply(ROTATE_Y_270)).register(true, false, true, true, false, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_attached_ns"))).register(true, true, false, false, true, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_attached_ns")).apply(ROTATE_Y_90)).register(true, true, true, true, false, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_attached_nse"))).register(true, true, false, true, true, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_attached_nse")).apply(ROTATE_Y_90)).register(true, false, true, true, true, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_attached_nse")).apply(ROTATE_Y_180)).register(true, true, true, false, true, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_attached_nse")).apply(ROTATE_Y_270)).register(true, true, true, true, true, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE, "_attached_nsew")))));
    }

    private void registerTripwireHook() {
        this.registerItemModel(Blocks.TRIPWIRE_HOOK);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.TRIPWIRE_HOOK).with(BlockStateVariantMap.models(Properties.ATTACHED, Properties.POWERED).generate((attached, on) -> BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockSubModelId(Blocks.TRIPWIRE_HOOK, (attached != false ? "_attached" : "") + (on != false ? "_on" : ""))))).apply(NORTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS));
    }

    private ModelVariant getTurtleEggModel(int eggs, String prefix, TextureMap textures) {
        return switch (eggs) {
            case 1 -> BlockStateModelGenerator.createModelVariant(Models.TEMPLATE_TURTLE_EGG.upload(ModelIds.getMinecraftNamespacedBlock(prefix + "turtle_egg"), textures, this.modelCollector));
            case 2 -> BlockStateModelGenerator.createModelVariant(Models.TEMPLATE_TWO_TURTLE_EGGS.upload(ModelIds.getMinecraftNamespacedBlock("two_" + prefix + "turtle_eggs"), textures, this.modelCollector));
            case 3 -> BlockStateModelGenerator.createModelVariant(Models.TEMPLATE_THREE_TURTLE_EGGS.upload(ModelIds.getMinecraftNamespacedBlock("three_" + prefix + "turtle_eggs"), textures, this.modelCollector));
            case 4 -> BlockStateModelGenerator.createModelVariant(Models.TEMPLATE_FOUR_TURTLE_EGGS.upload(ModelIds.getMinecraftNamespacedBlock("four_" + prefix + "turtle_eggs"), textures, this.modelCollector));
            default -> throw new UnsupportedOperationException();
        };
    }

    private ModelVariant getTurtleEggModel(int eggs, int cracks) {
        return switch (cracks) {
            case 0 -> this.getTurtleEggModel(eggs, "", TextureMap.all(TextureMap.getId(Blocks.TURTLE_EGG)));
            case 1 -> this.getTurtleEggModel(eggs, "slightly_cracked_", TextureMap.all(TextureMap.getSubId(Blocks.TURTLE_EGG, "_slightly_cracked")));
            case 2 -> this.getTurtleEggModel(eggs, "very_cracked_", TextureMap.all(TextureMap.getSubId(Blocks.TURTLE_EGG, "_very_cracked")));
            default -> throw new UnsupportedOperationException();
        };
    }

    private void registerTurtleEgg() {
        this.registerItemModel(Items.TURTLE_EGG);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.TURTLE_EGG).with(BlockStateVariantMap.models(Properties.EGGS, Properties.HATCH).generate((eggs, hatch) -> BlockStateModelGenerator.modelWithYRotation(this.getTurtleEggModel((int)eggs, (int)hatch)))));
    }

    private void registerDriedGhast() {
        Identifier lv = ModelIds.getBlockSubModelId(Blocks.DRIED_GHAST, "_hydration_0");
        this.registerParentedItemModel(Blocks.DRIED_GHAST, lv);
        Function<Integer, Identifier> function = hydration -> {
            String string = switch (hydration) {
                case 1 -> "_hydration_1";
                case 2 -> "_hydration_2";
                case 3 -> "_hydration_3";
                default -> "_hydration_0";
            };
            TextureMap lv = TextureMap.driedGhast(string);
            return Models.DRIED_GHAST.upload(Blocks.DRIED_GHAST, string, lv, this.modelCollector);
        };
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.DRIED_GHAST).with(BlockStateVariantMap.models(DriedGhastBlock.HYDRATION).generate(hydration -> BlockStateModelGenerator.createWeightedVariant((Identifier)function.apply((Integer)hydration)))).apply(NORTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS));
    }

    private void registerSnifferEgg() {
        this.registerItemModel(Items.SNIFFER_EGG);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.SNIFFER_EGG).with(BlockStateVariantMap.models(SnifferEggBlock.HATCH).generate(hatch -> {
            String string = switch (hatch) {
                case 1 -> "_slightly_cracked";
                case 2 -> "_very_cracked";
                default -> "_not_cracked";
            };
            TextureMap lv = TextureMap.snifferEgg(string);
            return BlockStateModelGenerator.createWeightedVariant(Models.SNIFFER_EGG.upload(Blocks.SNIFFER_EGG, string, lv, this.modelCollector));
        })));
    }

    private void registerMultifaceBlock(Block block) {
        this.registerItemModel(block);
        this.registerMultifaceBlockModel(block);
    }

    private void registerMultifaceBlock(Block block, Item item) {
        this.registerItemModel(item);
        this.registerMultifaceBlockModel(block);
    }

    private static <T extends Property<?>> Map<T, ModelVariantOperator> collectMultifaceOperators(State<?, ?> state, Function<Direction, T> propertyGetter) {
        ImmutableMap.Builder builder = ImmutableMap.builderWithExpectedSize(CONNECTION_VARIANT_FUNCTIONS.size());
        CONNECTION_VARIANT_FUNCTIONS.forEach((direction, operator) -> {
            Property lv = (Property)propertyGetter.apply((Direction)direction);
            if (state.contains(lv)) {
                builder.put(lv, operator);
            }
        });
        return builder.build();
    }

    private void registerMultifaceBlockModel(Block block) {
        Map<Property, ModelVariantOperator> map = BlockStateModelGenerator.collectMultifaceOperators(block.getDefaultState(), MultifaceBlock::getProperty);
        MultipartModelConditionBuilder lv = BlockStateModelGenerator.createMultipartConditionBuilder();
        map.forEach((property, operator) -> lv.put(property, false));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(block));
        MultipartBlockModelDefinitionCreator lv3 = MultipartBlockModelDefinitionCreator.create(block);
        map.forEach((property, operator) -> {
            lv3.with(BlockStateModelGenerator.createMultipartConditionBuilder().put(property, true), lv2.apply((ModelVariantOperator)operator));
            lv3.with(lv, lv2.apply((ModelVariantOperator)operator));
        });
        this.blockStateCollector.accept(lv3);
    }

    private void registerPaleMossCarpet(Block block) {
        Map<Property, ModelVariantOperator> map = BlockStateModelGenerator.collectMultifaceOperators(block.getDefaultState(), PaleMossCarpetBlock::getWallShape);
        MultipartModelConditionBuilder lv = BlockStateModelGenerator.createMultipartConditionBuilder().put(PaleMossCarpetBlock.BOTTOM, false);
        map.forEach((property, operator) -> lv.put(property, WallShape.NONE));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(TexturedModel.CARPET.upload(block, this.modelCollector));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(TexturedModel.MOSSY_CARPET_SIDE.get(block).textures(textureMap -> textureMap.put(TextureKey.SIDE, TextureMap.getSubId(block, "_side_tall"))).upload(block, "_side_tall", this.modelCollector));
        WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(TexturedModel.MOSSY_CARPET_SIDE.get(block).textures(textureMap -> textureMap.put(TextureKey.SIDE, TextureMap.getSubId(block, "_side_small"))).upload(block, "_side_small", this.modelCollector));
        MultipartBlockModelDefinitionCreator lv5 = MultipartBlockModelDefinitionCreator.create(block);
        lv5.with(BlockStateModelGenerator.createMultipartConditionBuilder().put(PaleMossCarpetBlock.BOTTOM, true), lv2);
        lv5.with(lv, lv2);
        map.forEach((property, operator) -> {
            lv5.with(BlockStateModelGenerator.createMultipartConditionBuilder().put(property, WallShape.TALL), lv3.apply((ModelVariantOperator)operator));
            lv5.with(BlockStateModelGenerator.createMultipartConditionBuilder().put(property, WallShape.LOW), lv4.apply((ModelVariantOperator)operator));
            lv5.with(lv, lv3.apply((ModelVariantOperator)operator));
        });
        this.blockStateCollector.accept(lv5);
    }

    private void registerHangingMoss(Block block) {
        this.registerItemModel(block);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(block).with(BlockStateVariantMap.models(HangingMossBlock.TIP).generate(tip -> {
            String string = tip != false ? "_tip" : "";
            TextureMap lv = TextureMap.cross(TextureMap.getSubId(block, string));
            return BlockStateModelGenerator.createWeightedVariant(CrossType.NOT_TINTED.getCrossModel().upload(block, string, lv, this.modelCollector));
        })));
    }

    private void registerSculkCatalyst() {
        Identifier lv = TextureMap.getSubId(Blocks.SCULK_CATALYST, "_bottom");
        TextureMap lv2 = new TextureMap().put(TextureKey.BOTTOM, lv).put(TextureKey.TOP, TextureMap.getSubId(Blocks.SCULK_CATALYST, "_top")).put(TextureKey.SIDE, TextureMap.getSubId(Blocks.SCULK_CATALYST, "_side"));
        TextureMap lv3 = new TextureMap().put(TextureKey.BOTTOM, lv).put(TextureKey.TOP, TextureMap.getSubId(Blocks.SCULK_CATALYST, "_top_bloom")).put(TextureKey.SIDE, TextureMap.getSubId(Blocks.SCULK_CATALYST, "_side_bloom"));
        Identifier lv4 = Models.CUBE_BOTTOM_TOP.upload(Blocks.SCULK_CATALYST, lv2, this.modelCollector);
        WeightedVariant lv5 = BlockStateModelGenerator.createWeightedVariant(lv4);
        WeightedVariant lv6 = BlockStateModelGenerator.createWeightedVariant(Models.CUBE_BOTTOM_TOP.upload(Blocks.SCULK_CATALYST, "_bloom", lv3, this.modelCollector));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.SCULK_CATALYST).with(BlockStateVariantMap.models(Properties.BLOOM).generate(bloom -> bloom != false ? lv6 : lv5)));
        this.registerParentedItemModel(Blocks.SCULK_CATALYST, lv4);
    }

    private void registerShelf(Block block, Block arg2) {
        TextureMap lv = new TextureMap().put(TextureKey.ALL, TextureMap.getId(block)).put(TextureKey.PARTICLE, TextureMap.getId(arg2));
        MultipartBlockModelDefinitionCreator lv2 = MultipartBlockModelDefinitionCreator.create(block);
        this.registerShelf(block, lv, lv2, Models.TEMPLATE_SHELF_BODY, null, null);
        this.registerShelf(block, lv, lv2, Models.TEMPLATE_SHELF_UNPOWERED, false, null);
        this.registerShelf(block, lv, lv2, Models.TEMPLATE_SHELF_UNCONNECTED, true, SideChainPart.UNCONNECTED);
        this.registerShelf(block, lv, lv2, Models.TEMPLATE_SHELF_LEFT, true, SideChainPart.LEFT);
        this.registerShelf(block, lv, lv2, Models.TEMPLATE_SHELF_CENTER, true, SideChainPart.CENTER);
        this.registerShelf(block, lv, lv2, Models.TEMPLATE_SHELF_RIGHT, true, SideChainPart.RIGHT);
        this.blockStateCollector.accept(lv2);
        this.registerParentedItemModel(block, Models.TEMPLATE_SHELF_INVENTORY.upload(block, lv, this.modelCollector));
    }

    private void registerShelf(Block block, TextureMap textureMap, MultipartBlockModelDefinitionCreator definitionCreator, Model model, @Nullable Boolean powered, @Nullable SideChainPart sideChain) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(model.upload(block, textureMap, this.modelCollector));
        BlockStateModelGenerator.forEachHorizontalDirection((facing, operator) -> definitionCreator.with(BlockStateModelGenerator.createSideChainModelCondition(facing, powered, sideChain), lv.apply((ModelVariantOperator)operator)));
    }

    private static void forEachHorizontalDirection(BiConsumer<Direction, ModelVariantOperator> biConsumer) {
        List.of(Pair.of(Direction.NORTH, NO_OP), Pair.of(Direction.EAST, ROTATE_Y_90), Pair.of(Direction.SOUTH, ROTATE_Y_180), Pair.of(Direction.WEST, ROTATE_Y_270)).forEach(pair -> {
            Direction lv = (Direction)pair.getFirst();
            ModelVariantOperator lv2 = (ModelVariantOperator)pair.getSecond();
            biConsumer.accept(lv, lv2);
        });
    }

    private static MultipartModelCondition createSideChainModelCondition(Direction facing, @Nullable Boolean powered, @Nullable SideChainPart sideChain) {
        MultipartModelConditionBuilder lv = BlockStateModelGenerator.createMultipartConditionBuilderWith(Properties.HORIZONTAL_FACING, (Enum)facing, (Enum[])new Direction[0]);
        if (powered == null) {
            return lv.build();
        }
        MultipartModelConditionBuilder lv2 = BlockStateModelGenerator.createMultipartConditionBuilderWith(Properties.POWERED, powered);
        return sideChain != null ? BlockStateModelGenerator.and(lv, lv2, BlockStateModelGenerator.createMultipartConditionBuilderWith(Properties.SIDE_CHAIN, (Enum)sideChain, (Enum[])new SideChainPart[0])) : BlockStateModelGenerator.and(lv, lv2);
    }

    private void registerChiseledBookshelf() {
        Block lv = Blocks.CHISELED_BOOKSHELF;
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(lv));
        MultipartBlockModelDefinitionCreator lv3 = MultipartBlockModelDefinitionCreator.create(lv);
        BlockStateModelGenerator.forEachHorizontalDirection((facing, operator) -> {
            MultipartModelCondition lv = BlockStateModelGenerator.createMultipartConditionBuilder().put(Properties.HORIZONTAL_FACING, facing).build();
            lv3.with(lv, lv2.apply((ModelVariantOperator)operator).apply(UV_LOCK));
            this.supplyChiseledBookshelfModels(lv3, lv, (ModelVariantOperator)operator);
        });
        this.blockStateCollector.accept(lv3);
        this.registerParentedItemModel(lv, ModelIds.getBlockSubModelId(lv, "_inventory"));
        CHISELED_BOOKSHELF_MODEL_CACHE.clear();
    }

    private void supplyChiseledBookshelfModels(MultipartBlockModelDefinitionCreator blockStateSupplier, MultipartModelCondition facingCondition, ModelVariantOperator rotation) {
        List.of(Pair.of(ChiseledBookshelfBlock.SLOT_0_OCCUPIED, Models.TEMPLATE_CHISELED_BOOKSHELF_SLOT_TOP_LEFT), Pair.of(ChiseledBookshelfBlock.SLOT_1_OCCUPIED, Models.TEMPLATE_CHISELED_BOOKSHELF_SLOT_TOP_MID), Pair.of(ChiseledBookshelfBlock.SLOT_2_OCCUPIED, Models.TEMPLATE_CHISELED_BOOKSHELF_SLOT_TOP_RIGHT), Pair.of(ChiseledBookshelfBlock.SLOT_3_OCCUPIED, Models.TEMPLATE_CHISELED_BOOKSHELF_SLOT_BOTTOM_LEFT), Pair.of(ChiseledBookshelfBlock.SLOT_4_OCCUPIED, Models.TEMPLATE_CHISELED_BOOKSHELF_SLOT_BOTTOM_MID), Pair.of(ChiseledBookshelfBlock.SLOT_5_OCCUPIED, Models.TEMPLATE_CHISELED_BOOKSHELF_SLOT_BOTTOM_RIGHT)).forEach(pair -> {
            BooleanProperty lv = (BooleanProperty)pair.getFirst();
            Model lv2 = (Model)pair.getSecond();
            this.supplyChiseledBookshelfModel(blockStateSupplier, facingCondition, rotation, lv, lv2, true);
            this.supplyChiseledBookshelfModel(blockStateSupplier, facingCondition, rotation, lv, lv2, false);
        });
    }

    private void supplyChiseledBookshelfModel(MultipartBlockModelDefinitionCreator blockStateSupplier, MultipartModelCondition facingCondition, ModelVariantOperator rotation, BooleanProperty property, Model model, boolean occupied) {
        String string = occupied ? "_occupied" : "_empty";
        TextureMap lv = new TextureMap().put(TextureKey.TEXTURE, TextureMap.getSubId(Blocks.CHISELED_BOOKSHELF, string));
        ChiseledBookshelfModelCacheKey lv2 = new ChiseledBookshelfModelCacheKey(model, string);
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(CHISELED_BOOKSHELF_MODEL_CACHE.computeIfAbsent(lv2, key -> model.upload(Blocks.CHISELED_BOOKSHELF, string, lv, this.modelCollector)));
        blockStateSupplier.with(new MultipartModelCombinedCondition(MultipartModelCombinedCondition.LogicalOperator.AND, List.of(facingCondition, BlockStateModelGenerator.createMultipartConditionBuilder().put(property, occupied).build())), lv3.apply(rotation));
    }

    private void registerMagmaBlock() {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(Models.CUBE_ALL.upload(Blocks.MAGMA_BLOCK, TextureMap.all(ModelIds.getMinecraftNamespacedBlock("magma")), this.modelCollector));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(Blocks.MAGMA_BLOCK, lv));
    }

    private void registerShulkerBox(Block shulkerBox, @Nullable DyeColor color) {
        this.registerBuiltin(shulkerBox);
        Item lv = shulkerBox.asItem();
        Identifier lv2 = Models.TEMPLATE_SHULKER_BOX.upload(lv, TextureMap.particle(shulkerBox), this.modelCollector);
        ItemModel.Unbaked lv3 = color != null ? ItemModels.special(lv2, new ShulkerBoxModelRenderer.Unbaked(color)) : ItemModels.special(lv2, new ShulkerBoxModelRenderer.Unbaked());
        this.itemModelOutput.accept(lv, lv3);
    }

    private void registerPlantPart(Block plant, Block plantStem, CrossType tintType) {
        this.registerTintableCrossBlockState(plant, tintType);
        this.registerTintableCrossBlockState(plantStem, tintType);
    }

    private void registerInfestedStone() {
        Identifier lv = ModelIds.getBlockModelId(Blocks.STONE);
        ModelVariant lv2 = BlockStateModelGenerator.createModelVariant(lv);
        ModelVariant lv3 = BlockStateModelGenerator.createModelVariant(ModelIds.getBlockSubModelId(Blocks.STONE, "_mirrored"));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.INFESTED_STONE, BlockStateModelGenerator.modelWithMirroring(lv2, lv3)));
        this.registerParentedItemModel(Blocks.INFESTED_STONE, lv);
    }

    private void registerInfestedDeepslate() {
        Identifier lv = ModelIds.getBlockModelId(Blocks.DEEPSLATE);
        ModelVariant lv2 = BlockStateModelGenerator.createModelVariant(lv);
        ModelVariant lv3 = BlockStateModelGenerator.createModelVariant(ModelIds.getBlockSubModelId(Blocks.DEEPSLATE, "_mirrored"));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.INFESTED_DEEPSLATE, BlockStateModelGenerator.modelWithMirroring(lv2, lv3)).apply(BlockStateModelGenerator.createAxisRotatedVariantMap()));
        this.registerParentedItemModel(Blocks.INFESTED_DEEPSLATE, lv);
    }

    private void registerRoots(Block root, Block pottedRoot) {
        this.registerTintableCross(root, CrossType.NOT_TINTED);
        TextureMap lv = TextureMap.plant(TextureMap.getSubId(root, "_pot"));
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(CrossType.NOT_TINTED.getFlowerPotCrossModel().upload(pottedRoot, lv, this.modelCollector));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(pottedRoot, lv2));
    }

    private void registerRespawnAnchor() {
        Identifier lv = TextureMap.getSubId(Blocks.RESPAWN_ANCHOR, "_bottom");
        Identifier lv2 = TextureMap.getSubId(Blocks.RESPAWN_ANCHOR, "_top_off");
        Identifier lv3 = TextureMap.getSubId(Blocks.RESPAWN_ANCHOR, "_top");
        Identifier[] lvs = new Identifier[5];
        for (int i = 0; i < 5; ++i) {
            TextureMap lv4 = new TextureMap().put(TextureKey.BOTTOM, lv).put(TextureKey.TOP, i == 0 ? lv2 : lv3).put(TextureKey.SIDE, TextureMap.getSubId(Blocks.RESPAWN_ANCHOR, "_side" + i));
            lvs[i] = Models.CUBE_BOTTOM_TOP.upload(Blocks.RESPAWN_ANCHOR, "_" + i, lv4, this.modelCollector);
        }
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.RESPAWN_ANCHOR).with(BlockStateVariantMap.models(Properties.CHARGES).generate(charges -> BlockStateModelGenerator.createWeightedVariant(lvs[charges]))));
        this.registerParentedItemModel(Blocks.RESPAWN_ANCHOR, lvs[0]);
    }

    private static ModelVariantOperator addJigsawOrientationToVariant(Orientation orientation) {
        return switch (orientation) {
            default -> throw new MatchException(null, null);
            case Orientation.DOWN_NORTH -> ROTATE_X_90;
            case Orientation.DOWN_SOUTH -> ROTATE_X_90.then(ROTATE_Y_180);
            case Orientation.DOWN_WEST -> ROTATE_X_90.then(ROTATE_Y_270);
            case Orientation.DOWN_EAST -> ROTATE_X_90.then(ROTATE_Y_90);
            case Orientation.UP_NORTH -> ROTATE_X_270.then(ROTATE_Y_180);
            case Orientation.UP_SOUTH -> ROTATE_X_270;
            case Orientation.UP_WEST -> ROTATE_X_270.then(ROTATE_Y_90);
            case Orientation.UP_EAST -> ROTATE_X_270.then(ROTATE_Y_270);
            case Orientation.NORTH_UP -> NO_OP;
            case Orientation.SOUTH_UP -> ROTATE_Y_180;
            case Orientation.WEST_UP -> ROTATE_Y_270;
            case Orientation.EAST_UP -> ROTATE_Y_90;
        };
    }

    private void registerJigsaw() {
        Identifier lv = TextureMap.getSubId(Blocks.JIGSAW, "_top");
        Identifier lv2 = TextureMap.getSubId(Blocks.JIGSAW, "_bottom");
        Identifier lv3 = TextureMap.getSubId(Blocks.JIGSAW, "_side");
        Identifier lv4 = TextureMap.getSubId(Blocks.JIGSAW, "_lock");
        TextureMap lv5 = new TextureMap().put(TextureKey.DOWN, lv3).put(TextureKey.WEST, lv3).put(TextureKey.EAST, lv3).put(TextureKey.PARTICLE, lv).put(TextureKey.NORTH, lv).put(TextureKey.SOUTH, lv2).put(TextureKey.UP, lv4);
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.JIGSAW, BlockStateModelGenerator.createWeightedVariant(Models.CUBE_DIRECTIONAL.upload(Blocks.JIGSAW, lv5, this.modelCollector))).apply(BlockStateVariantMap.operations(Properties.ORIENTATION).generate(BlockStateModelGenerator::addJigsawOrientationToVariant)));
    }

    private void registerPetrifiedOakSlab() {
        Block lv = Blocks.OAK_PLANKS;
        WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(lv));
        TextureMap lv3 = TextureMap.all(lv);
        Block lv4 = Blocks.PETRIFIED_OAK_SLAB;
        WeightedVariant lv5 = BlockStateModelGenerator.createWeightedVariant(Models.SLAB.upload(lv4, lv3, this.modelCollector));
        WeightedVariant lv6 = BlockStateModelGenerator.createWeightedVariant(Models.SLAB_TOP.upload(lv4, lv3, this.modelCollector));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSlabBlockState(lv4, lv5, lv6, lv2));
    }

    private void registerSkull(Block block, Block wallBlock, SkullBlock.SkullType type, Identifier baseModelId) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(ModelIds.getMinecraftNamespacedBlock("skull"));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(block, lv));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(wallBlock, lv));
        if (type == SkullBlock.Type.PLAYER) {
            this.itemModelOutput.accept(block.asItem(), ItemModels.special(baseModelId, new PlayerHeadModelRenderer.Unbaked()));
        } else {
            this.itemModelOutput.accept(block.asItem(), ItemModels.special(baseModelId, new HeadModelRenderer.Unbaked(type)));
        }
    }

    private void registerSkulls() {
        Identifier lv = ModelIds.getMinecraftNamespacedItem("template_skull");
        this.registerSkull(Blocks.CREEPER_HEAD, Blocks.CREEPER_WALL_HEAD, SkullBlock.Type.CREEPER, lv);
        this.registerSkull(Blocks.PLAYER_HEAD, Blocks.PLAYER_WALL_HEAD, SkullBlock.Type.PLAYER, lv);
        this.registerSkull(Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD, SkullBlock.Type.ZOMBIE, lv);
        this.registerSkull(Blocks.SKELETON_SKULL, Blocks.SKELETON_WALL_SKULL, SkullBlock.Type.SKELETON, lv);
        this.registerSkull(Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, SkullBlock.Type.WITHER_SKELETON, lv);
        this.registerSkull(Blocks.PIGLIN_HEAD, Blocks.PIGLIN_WALL_HEAD, SkullBlock.Type.PIGLIN, lv);
        this.registerSkull(Blocks.DRAGON_HEAD, Blocks.DRAGON_WALL_HEAD, SkullBlock.Type.DRAGON, ModelIds.getItemModelId(Items.DRAGON_HEAD));
    }

    private void registerCopperGolemStatues() {
        this.registerCopperGolemStatue(Blocks.COPPER_GOLEM_STATUE, Blocks.COPPER_BLOCK, Oxidizable.OxidationLevel.UNAFFECTED);
        this.registerCopperGolemStatue(Blocks.EXPOSED_COPPER_GOLEM_STATUE, Blocks.EXPOSED_COPPER, Oxidizable.OxidationLevel.EXPOSED);
        this.registerCopperGolemStatue(Blocks.WEATHERED_COPPER_GOLEM_STATUE, Blocks.WEATHERED_COPPER, Oxidizable.OxidationLevel.WEATHERED);
        this.registerCopperGolemStatue(Blocks.OXIDIZED_COPPER_GOLEM_STATUE, Blocks.OXIDIZED_COPPER, Oxidizable.OxidationLevel.OXIDIZED);
        this.registerParented(Blocks.COPPER_GOLEM_STATUE, Blocks.WAXED_COPPER_GOLEM_STATUE);
        this.registerParented(Blocks.EXPOSED_COPPER_GOLEM_STATUE, Blocks.WAXED_EXPOSED_COPPER_GOLEM_STATUE);
        this.registerParented(Blocks.WEATHERED_COPPER_GOLEM_STATUE, Blocks.WAXED_WEATHERED_COPPER_GOLEM_STATUE);
        this.registerParented(Blocks.OXIDIZED_COPPER_GOLEM_STATUE, Blocks.WAXED_OXIDIZED_COPPER_GOLEM_STATUE);
    }

    private void registerCopperGolemStatue(Block block, Block particleBlock, Oxidizable.OxidationLevel oxidationLevel) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(Models.PARTICLE.upload(block, TextureMap.particle(TextureMap.getId(particleBlock)), this.modelCollector));
        Identifier lv2 = ModelIds.getMinecraftNamespacedItem("template_copper_golem_statue");
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(block, lv));
        this.itemModelOutput.accept(block.asItem(), ItemModels.select(CopperGolemStatueBlock.POSE, ItemModels.special(lv2, new CopperGolemStatueModelRenderer.Unbaked(oxidationLevel, CopperGolemStatueBlock.Pose.STANDING)), Map.of(CopperGolemStatueBlock.Pose.SITTING, ItemModels.special(lv2, new CopperGolemStatueModelRenderer.Unbaked(oxidationLevel, CopperGolemStatueBlock.Pose.SITTING)), CopperGolemStatueBlock.Pose.STAR, ItemModels.special(lv2, new CopperGolemStatueModelRenderer.Unbaked(oxidationLevel, CopperGolemStatueBlock.Pose.STAR)), CopperGolemStatueBlock.Pose.RUNNING, ItemModels.special(lv2, new CopperGolemStatueModelRenderer.Unbaked(oxidationLevel, CopperGolemStatueBlock.Pose.RUNNING)))));
    }

    private void registerBanner(Block block, Block wallBlock, DyeColor color) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(ModelIds.getMinecraftNamespacedBlock("banner"));
        Identifier lv2 = ModelIds.getMinecraftNamespacedItem("template_banner");
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(block, lv));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(wallBlock, lv));
        Item lv3 = block.asItem();
        this.itemModelOutput.accept(lv3, ItemModels.special(lv2, new BannerModelRenderer.Unbaked(color)));
    }

    private void registerBanners() {
        this.registerBanner(Blocks.WHITE_BANNER, Blocks.WHITE_WALL_BANNER, DyeColor.WHITE);
        this.registerBanner(Blocks.ORANGE_BANNER, Blocks.ORANGE_WALL_BANNER, DyeColor.ORANGE);
        this.registerBanner(Blocks.MAGENTA_BANNER, Blocks.MAGENTA_WALL_BANNER, DyeColor.MAGENTA);
        this.registerBanner(Blocks.LIGHT_BLUE_BANNER, Blocks.LIGHT_BLUE_WALL_BANNER, DyeColor.LIGHT_BLUE);
        this.registerBanner(Blocks.YELLOW_BANNER, Blocks.YELLOW_WALL_BANNER, DyeColor.YELLOW);
        this.registerBanner(Blocks.LIME_BANNER, Blocks.LIME_WALL_BANNER, DyeColor.LIME);
        this.registerBanner(Blocks.PINK_BANNER, Blocks.PINK_WALL_BANNER, DyeColor.PINK);
        this.registerBanner(Blocks.GRAY_BANNER, Blocks.GRAY_WALL_BANNER, DyeColor.GRAY);
        this.registerBanner(Blocks.LIGHT_GRAY_BANNER, Blocks.LIGHT_GRAY_WALL_BANNER, DyeColor.LIGHT_GRAY);
        this.registerBanner(Blocks.CYAN_BANNER, Blocks.CYAN_WALL_BANNER, DyeColor.CYAN);
        this.registerBanner(Blocks.PURPLE_BANNER, Blocks.PURPLE_WALL_BANNER, DyeColor.PURPLE);
        this.registerBanner(Blocks.BLUE_BANNER, Blocks.BLUE_WALL_BANNER, DyeColor.BLUE);
        this.registerBanner(Blocks.BROWN_BANNER, Blocks.BROWN_WALL_BANNER, DyeColor.BROWN);
        this.registerBanner(Blocks.GREEN_BANNER, Blocks.GREEN_WALL_BANNER, DyeColor.GREEN);
        this.registerBanner(Blocks.RED_BANNER, Blocks.RED_WALL_BANNER, DyeColor.RED);
        this.registerBanner(Blocks.BLACK_BANNER, Blocks.BLACK_WALL_BANNER, DyeColor.BLACK);
    }

    private void registerChest(Block block, Block particleSource, Identifier texture, boolean christmas) {
        this.registerBuiltinWithParticle(block, particleSource);
        Item lv = block.asItem();
        Identifier lv2 = Models.TEMPLATE_CHEST.upload(lv, TextureMap.particle(particleSource), this.modelCollector);
        ItemModel.Unbaked lv3 = ItemModels.special(lv2, new ChestModelRenderer.Unbaked(texture));
        if (christmas) {
            ItemModel.Unbaked lv4 = ItemModels.special(lv2, new ChestModelRenderer.Unbaked(ChestModelRenderer.CHRISTMAS_ID));
            this.itemModelOutput.accept(lv, ItemModels.christmasSelect(lv4, lv3));
        } else {
            this.itemModelOutput.accept(lv, lv3);
        }
    }

    private void registerChests() {
        this.registerChest(Blocks.CHEST, Blocks.OAK_PLANKS, ChestModelRenderer.NORMAL_ID, true);
        this.registerChest(Blocks.TRAPPED_CHEST, Blocks.OAK_PLANKS, ChestModelRenderer.TRAPPED_ID, true);
        this.registerChest(Blocks.ENDER_CHEST, Blocks.OBSIDIAN, ChestModelRenderer.ENDER_ID, false);
    }

    private void registerCopperChests() {
        this.registerChest(Blocks.COPPER_CHEST, Blocks.COPPER_BLOCK, ChestModelRenderer.COPPER_ID, false);
        this.registerChest(Blocks.EXPOSED_COPPER_CHEST, Blocks.EXPOSED_COPPER, ChestModelRenderer.EXPOSED_COPPER_ID, false);
        this.registerChest(Blocks.WEATHERED_COPPER_CHEST, Blocks.WEATHERED_COPPER, ChestModelRenderer.WEATHERED_COPPER_ID, false);
        this.registerChest(Blocks.OXIDIZED_COPPER_CHEST, Blocks.OXIDIZED_COPPER, ChestModelRenderer.OXIDIZED_COPPER_ID, false);
        this.registerParented(Blocks.COPPER_CHEST, Blocks.WAXED_COPPER_CHEST);
        this.registerParented(Blocks.EXPOSED_COPPER_CHEST, Blocks.WAXED_EXPOSED_COPPER_CHEST);
        this.registerParented(Blocks.WEATHERED_COPPER_CHEST, Blocks.WAXED_WEATHERED_COPPER_CHEST);
        this.registerParented(Blocks.OXIDIZED_COPPER_CHEST, Blocks.WAXED_OXIDIZED_COPPER_CHEST);
    }

    private void registerBed(Block block, Block particleSource, DyeColor color) {
        WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(ModelIds.getMinecraftNamespacedBlock("bed"));
        this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(block, lv));
        Item lv2 = block.asItem();
        Identifier lv3 = Models.TEMPLATE_BED.upload(ModelIds.getItemModelId(lv2), TextureMap.particle(particleSource), this.modelCollector);
        this.itemModelOutput.accept(lv2, ItemModels.special(lv3, new BedModelRenderer.Unbaked(color)));
    }

    private void registerBeds() {
        this.registerBed(Blocks.WHITE_BED, Blocks.WHITE_WOOL, DyeColor.WHITE);
        this.registerBed(Blocks.ORANGE_BED, Blocks.ORANGE_WOOL, DyeColor.ORANGE);
        this.registerBed(Blocks.MAGENTA_BED, Blocks.MAGENTA_WOOL, DyeColor.MAGENTA);
        this.registerBed(Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_BLUE_WOOL, DyeColor.LIGHT_BLUE);
        this.registerBed(Blocks.YELLOW_BED, Blocks.YELLOW_WOOL, DyeColor.YELLOW);
        this.registerBed(Blocks.LIME_BED, Blocks.LIME_WOOL, DyeColor.LIME);
        this.registerBed(Blocks.PINK_BED, Blocks.PINK_WOOL, DyeColor.PINK);
        this.registerBed(Blocks.GRAY_BED, Blocks.GRAY_WOOL, DyeColor.GRAY);
        this.registerBed(Blocks.LIGHT_GRAY_BED, Blocks.LIGHT_GRAY_WOOL, DyeColor.LIGHT_GRAY);
        this.registerBed(Blocks.CYAN_BED, Blocks.CYAN_WOOL, DyeColor.CYAN);
        this.registerBed(Blocks.PURPLE_BED, Blocks.PURPLE_WOOL, DyeColor.PURPLE);
        this.registerBed(Blocks.BLUE_BED, Blocks.BLUE_WOOL, DyeColor.BLUE);
        this.registerBed(Blocks.BROWN_BED, Blocks.BROWN_WOOL, DyeColor.BROWN);
        this.registerBed(Blocks.GREEN_BED, Blocks.GREEN_WOOL, DyeColor.GREEN);
        this.registerBed(Blocks.RED_BED, Blocks.RED_WOOL, DyeColor.RED);
        this.registerBed(Blocks.BLACK_BED, Blocks.BLACK_WOOL, DyeColor.BLACK);
    }

    private void registerSpecialItemModel(Block block, SpecialModelRenderer.Unbaked specialModel) {
        Item lv = block.asItem();
        Identifier lv2 = ModelIds.getItemModelId(lv);
        this.itemModelOutput.accept(lv, ItemModels.special(lv2, specialModel));
    }

    public void register() {
        BlockFamilies.getFamilies().filter(BlockFamily::shouldGenerateModels).forEach(family -> this.registerCubeAllModelTexturePool(family.getBaseBlock()).family((BlockFamily)family));
        this.registerCubeAllModelTexturePool(Blocks.CUT_COPPER).family(BlockFamilies.CUT_COPPER).parented(Blocks.CUT_COPPER, Blocks.WAXED_CUT_COPPER).parented(Blocks.CHISELED_COPPER, Blocks.WAXED_CHISELED_COPPER).family(BlockFamilies.WAXED_CUT_COPPER);
        this.registerCubeAllModelTexturePool(Blocks.EXPOSED_CUT_COPPER).family(BlockFamilies.EXPOSED_CUT_COPPER).parented(Blocks.EXPOSED_CUT_COPPER, Blocks.WAXED_EXPOSED_CUT_COPPER).parented(Blocks.EXPOSED_CHISELED_COPPER, Blocks.WAXED_EXPOSED_CHISELED_COPPER).family(BlockFamilies.WAXED_EXPOSED_CUT_COPPER);
        this.registerCubeAllModelTexturePool(Blocks.WEATHERED_CUT_COPPER).family(BlockFamilies.WEATHERED_CUT_COPPER).parented(Blocks.WEATHERED_CUT_COPPER, Blocks.WAXED_WEATHERED_CUT_COPPER).parented(Blocks.WEATHERED_CHISELED_COPPER, Blocks.WAXED_WEATHERED_CHISELED_COPPER).family(BlockFamilies.WAXED_WEATHERED_CUT_COPPER);
        this.registerCubeAllModelTexturePool(Blocks.OXIDIZED_CUT_COPPER).family(BlockFamilies.OXIDIZED_CUT_COPPER).parented(Blocks.OXIDIZED_CUT_COPPER, Blocks.WAXED_OXIDIZED_CUT_COPPER).parented(Blocks.OXIDIZED_CHISELED_COPPER, Blocks.WAXED_OXIDIZED_CHISELED_COPPER).family(BlockFamilies.WAXED_OXIDIZED_CUT_COPPER);
        this.registerCopperBulb(Blocks.COPPER_BULB);
        this.registerCopperBulb(Blocks.EXPOSED_COPPER_BULB);
        this.registerCopperBulb(Blocks.WEATHERED_COPPER_BULB);
        this.registerCopperBulb(Blocks.OXIDIZED_COPPER_BULB);
        this.registerWaxedCopperBulb(Blocks.COPPER_BULB, Blocks.WAXED_COPPER_BULB);
        this.registerWaxedCopperBulb(Blocks.EXPOSED_COPPER_BULB, Blocks.WAXED_EXPOSED_COPPER_BULB);
        this.registerWaxedCopperBulb(Blocks.WEATHERED_COPPER_BULB, Blocks.WAXED_WEATHERED_COPPER_BULB);
        this.registerWaxedCopperBulb(Blocks.OXIDIZED_COPPER_BULB, Blocks.WAXED_OXIDIZED_COPPER_BULB);
        this.registerSimpleState(Blocks.AIR);
        this.registerStateWithModelReference(Blocks.CAVE_AIR, Blocks.AIR);
        this.registerStateWithModelReference(Blocks.VOID_AIR, Blocks.AIR);
        this.registerSimpleState(Blocks.BEACON);
        this.registerSimpleState(Blocks.CACTUS);
        this.registerStateWithModelReference(Blocks.BUBBLE_COLUMN, Blocks.WATER);
        this.registerSimpleState(Blocks.DRAGON_EGG);
        this.registerSimpleState(Blocks.DRIED_KELP_BLOCK);
        this.registerSimpleState(Blocks.ENCHANTING_TABLE);
        this.registerSimpleState(Blocks.FLOWER_POT);
        this.registerItemModel(Items.FLOWER_POT);
        this.registerSimpleState(Blocks.HONEY_BLOCK);
        this.registerSimpleState(Blocks.WATER);
        this.registerSimpleState(Blocks.LAVA);
        this.registerSimpleState(Blocks.SLIME_BLOCK);
        this.registerItemModel(Items.IRON_CHAIN);
        Items.COPPER_CHAINS.getWaxingMap().forEach(this::registerWaxable);
        this.registerCandle(Blocks.WHITE_CANDLE, Blocks.WHITE_CANDLE_CAKE);
        this.registerCandle(Blocks.ORANGE_CANDLE, Blocks.ORANGE_CANDLE_CAKE);
        this.registerCandle(Blocks.MAGENTA_CANDLE, Blocks.MAGENTA_CANDLE_CAKE);
        this.registerCandle(Blocks.LIGHT_BLUE_CANDLE, Blocks.LIGHT_BLUE_CANDLE_CAKE);
        this.registerCandle(Blocks.YELLOW_CANDLE, Blocks.YELLOW_CANDLE_CAKE);
        this.registerCandle(Blocks.LIME_CANDLE, Blocks.LIME_CANDLE_CAKE);
        this.registerCandle(Blocks.PINK_CANDLE, Blocks.PINK_CANDLE_CAKE);
        this.registerCandle(Blocks.GRAY_CANDLE, Blocks.GRAY_CANDLE_CAKE);
        this.registerCandle(Blocks.LIGHT_GRAY_CANDLE, Blocks.LIGHT_GRAY_CANDLE_CAKE);
        this.registerCandle(Blocks.CYAN_CANDLE, Blocks.CYAN_CANDLE_CAKE);
        this.registerCandle(Blocks.PURPLE_CANDLE, Blocks.PURPLE_CANDLE_CAKE);
        this.registerCandle(Blocks.BLUE_CANDLE, Blocks.BLUE_CANDLE_CAKE);
        this.registerCandle(Blocks.BROWN_CANDLE, Blocks.BROWN_CANDLE_CAKE);
        this.registerCandle(Blocks.GREEN_CANDLE, Blocks.GREEN_CANDLE_CAKE);
        this.registerCandle(Blocks.RED_CANDLE, Blocks.RED_CANDLE_CAKE);
        this.registerCandle(Blocks.BLACK_CANDLE, Blocks.BLACK_CANDLE_CAKE);
        this.registerCandle(Blocks.CANDLE, Blocks.CANDLE_CAKE);
        this.registerSimpleState(Blocks.POTTED_BAMBOO);
        this.registerSimpleState(Blocks.POTTED_CACTUS);
        this.registerSimpleState(Blocks.POWDER_SNOW);
        this.registerSimpleState(Blocks.SPORE_BLOSSOM);
        this.registerAzalea(Blocks.AZALEA);
        this.registerAzalea(Blocks.FLOWERING_AZALEA);
        this.registerPottedAzaleaBush(Blocks.POTTED_AZALEA_BUSH);
        this.registerPottedAzaleaBush(Blocks.POTTED_FLOWERING_AZALEA_BUSH);
        this.registerCaveVines();
        this.registerWoolAndCarpet(Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET);
        this.registerPaleMossCarpet(Blocks.PALE_MOSS_CARPET);
        this.registerHangingMoss(Blocks.PALE_HANGING_MOSS);
        this.registerSimpleCubeAll(Blocks.PALE_MOSS_BLOCK);
        this.registerFlowerbed(Blocks.PINK_PETALS);
        this.registerFlowerbed(Blocks.WILDFLOWERS);
        this.registerLeafLitter(Blocks.LEAF_LITTER);
        this.registerTintableCrossBlockState(Blocks.FIREFLY_BUSH, CrossType.EMISSIVE_NOT_TINTED);
        this.registerItemModel(Items.FIREFLY_BUSH);
        this.registerBuiltinWithParticle(Blocks.BARRIER, Items.BARRIER);
        this.registerItemModel(Items.BARRIER);
        this.registerLightBlock();
        this.registerBuiltinWithParticle(Blocks.STRUCTURE_VOID, Items.STRUCTURE_VOID);
        this.registerItemModel(Items.STRUCTURE_VOID);
        this.registerBuiltinWithParticle(Blocks.MOVING_PISTON, TextureMap.getSubId(Blocks.PISTON, "_side"));
        this.registerSimpleCubeAll(Blocks.COAL_ORE);
        this.registerSimpleCubeAll(Blocks.DEEPSLATE_COAL_ORE);
        this.registerSimpleCubeAll(Blocks.COAL_BLOCK);
        this.registerSimpleCubeAll(Blocks.DIAMOND_ORE);
        this.registerSimpleCubeAll(Blocks.DEEPSLATE_DIAMOND_ORE);
        this.registerSimpleCubeAll(Blocks.DIAMOND_BLOCK);
        this.registerSimpleCubeAll(Blocks.EMERALD_ORE);
        this.registerSimpleCubeAll(Blocks.DEEPSLATE_EMERALD_ORE);
        this.registerSimpleCubeAll(Blocks.EMERALD_BLOCK);
        this.registerSimpleCubeAll(Blocks.GOLD_ORE);
        this.registerSimpleCubeAll(Blocks.NETHER_GOLD_ORE);
        this.registerSimpleCubeAll(Blocks.DEEPSLATE_GOLD_ORE);
        this.registerSimpleCubeAll(Blocks.GOLD_BLOCK);
        this.registerSimpleCubeAll(Blocks.IRON_ORE);
        this.registerSimpleCubeAll(Blocks.DEEPSLATE_IRON_ORE);
        this.registerSimpleCubeAll(Blocks.IRON_BLOCK);
        this.registerSingleton(Blocks.ANCIENT_DEBRIS, TexturedModel.CUBE_COLUMN);
        this.registerSimpleCubeAll(Blocks.NETHERITE_BLOCK);
        this.registerSimpleCubeAll(Blocks.LAPIS_ORE);
        this.registerSimpleCubeAll(Blocks.DEEPSLATE_LAPIS_ORE);
        this.registerSimpleCubeAll(Blocks.LAPIS_BLOCK);
        this.registerSimpleCubeAll(Blocks.RESIN_BLOCK);
        this.registerSimpleCubeAll(Blocks.NETHER_QUARTZ_ORE);
        this.registerSimpleCubeAll(Blocks.REDSTONE_ORE);
        this.registerSimpleCubeAll(Blocks.DEEPSLATE_REDSTONE_ORE);
        this.registerSimpleCubeAll(Blocks.REDSTONE_BLOCK);
        this.registerSimpleCubeAll(Blocks.GILDED_BLACKSTONE);
        this.registerSimpleCubeAll(Blocks.BLUE_ICE);
        this.registerSimpleCubeAll(Blocks.CLAY);
        this.registerSimpleCubeAll(Blocks.COARSE_DIRT);
        this.registerSimpleCubeAll(Blocks.CRYING_OBSIDIAN);
        this.registerSimpleCubeAll(Blocks.END_STONE);
        this.registerSimpleCubeAll(Blocks.GLOWSTONE);
        this.registerSimpleCubeAll(Blocks.GRAVEL);
        this.registerSimpleCubeAll(Blocks.HONEYCOMB_BLOCK);
        this.registerSimpleCubeAll(Blocks.ICE);
        this.registerSingleton(Blocks.JUKEBOX, TexturedModel.CUBE_TOP);
        this.registerSingleton(Blocks.LODESTONE, TexturedModel.CUBE_COLUMN);
        this.registerSingleton(Blocks.MELON, TexturedModel.CUBE_COLUMN);
        this.registerSimpleState(Blocks.MANGROVE_ROOTS);
        this.registerSimpleState(Blocks.POTTED_MANGROVE_PROPAGULE);
        this.registerSimpleCubeAll(Blocks.NETHER_WART_BLOCK);
        this.registerSimpleCubeAll(Blocks.NOTE_BLOCK);
        this.registerSimpleCubeAll(Blocks.PACKED_ICE);
        this.registerSimpleCubeAll(Blocks.OBSIDIAN);
        this.registerSimpleCubeAll(Blocks.QUARTZ_BRICKS);
        this.registerSimpleCubeAll(Blocks.SEA_LANTERN);
        this.registerSimpleCubeAll(Blocks.SHROOMLIGHT);
        this.registerSimpleCubeAll(Blocks.SOUL_SAND);
        this.registerSimpleCubeAll(Blocks.SOUL_SOIL);
        this.registerSingleton(Blocks.SPAWNER, TexturedModel.CUBE_ALL_INNER_FACES);
        this.registerCreakingHeart(Blocks.CREAKING_HEART);
        this.registerSimpleCubeAll(Blocks.SPONGE);
        this.registerSingleton(Blocks.SEAGRASS, TexturedModel.TEMPLATE_SEAGRASS);
        this.registerItemModel(Items.SEAGRASS);
        this.registerSingleton(Blocks.TNT, TexturedModel.CUBE_BOTTOM_TOP);
        this.registerSingleton(Blocks.TARGET, TexturedModel.CUBE_COLUMN);
        this.registerSimpleCubeAll(Blocks.WARPED_WART_BLOCK);
        this.registerSimpleCubeAll(Blocks.WET_SPONGE);
        this.registerSimpleCubeAll(Blocks.AMETHYST_BLOCK);
        this.registerSimpleCubeAll(Blocks.BUDDING_AMETHYST);
        this.registerSimpleCubeAll(Blocks.CALCITE);
        this.registerSimpleCubeAll(Blocks.DRIPSTONE_BLOCK);
        this.registerSimpleCubeAll(Blocks.RAW_IRON_BLOCK);
        this.registerSimpleCubeAll(Blocks.RAW_COPPER_BLOCK);
        this.registerSimpleCubeAll(Blocks.RAW_GOLD_BLOCK);
        this.registerMirrorable(Blocks.SCULK);
        this.registerSimpleState(Blocks.HEAVY_CORE);
        this.registerPetrifiedOakSlab();
        this.registerSimpleCubeAll(Blocks.COPPER_ORE);
        this.registerSimpleCubeAll(Blocks.DEEPSLATE_COPPER_ORE);
        this.registerSimpleCubeAll(Blocks.COPPER_BLOCK);
        this.registerSimpleCubeAll(Blocks.EXPOSED_COPPER);
        this.registerSimpleCubeAll(Blocks.WEATHERED_COPPER);
        this.registerSimpleCubeAll(Blocks.OXIDIZED_COPPER);
        this.registerParented(Blocks.COPPER_BLOCK, Blocks.WAXED_COPPER_BLOCK);
        this.registerParented(Blocks.EXPOSED_COPPER, Blocks.WAXED_EXPOSED_COPPER);
        this.registerParented(Blocks.WEATHERED_COPPER, Blocks.WAXED_WEATHERED_COPPER);
        this.registerParented(Blocks.OXIDIZED_COPPER, Blocks.WAXED_OXIDIZED_COPPER);
        this.registerDoor(Blocks.COPPER_DOOR);
        this.registerDoor(Blocks.EXPOSED_COPPER_DOOR);
        this.registerDoor(Blocks.WEATHERED_COPPER_DOOR);
        this.registerDoor(Blocks.OXIDIZED_COPPER_DOOR);
        this.registerParentedDoor(Blocks.COPPER_DOOR, Blocks.WAXED_COPPER_DOOR);
        this.registerParentedDoor(Blocks.EXPOSED_COPPER_DOOR, Blocks.WAXED_EXPOSED_COPPER_DOOR);
        this.registerParentedDoor(Blocks.WEATHERED_COPPER_DOOR, Blocks.WAXED_WEATHERED_COPPER_DOOR);
        this.registerParentedDoor(Blocks.OXIDIZED_COPPER_DOOR, Blocks.WAXED_OXIDIZED_COPPER_DOOR);
        this.registerTrapdoor(Blocks.COPPER_TRAPDOOR);
        this.registerTrapdoor(Blocks.EXPOSED_COPPER_TRAPDOOR);
        this.registerTrapdoor(Blocks.WEATHERED_COPPER_TRAPDOOR);
        this.registerTrapdoor(Blocks.OXIDIZED_COPPER_TRAPDOOR);
        this.registerParentedTrapdoor(Blocks.COPPER_TRAPDOOR, Blocks.WAXED_COPPER_TRAPDOOR);
        this.registerParentedTrapdoor(Blocks.EXPOSED_COPPER_TRAPDOOR, Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR);
        this.registerParentedTrapdoor(Blocks.WEATHERED_COPPER_TRAPDOOR, Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR);
        this.registerParentedTrapdoor(Blocks.OXIDIZED_COPPER_TRAPDOOR, Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR);
        this.registerSimpleCubeAll(Blocks.COPPER_GRATE);
        this.registerSimpleCubeAll(Blocks.EXPOSED_COPPER_GRATE);
        this.registerSimpleCubeAll(Blocks.WEATHERED_COPPER_GRATE);
        this.registerSimpleCubeAll(Blocks.OXIDIZED_COPPER_GRATE);
        this.registerParented(Blocks.COPPER_GRATE, Blocks.WAXED_COPPER_GRATE);
        this.registerParented(Blocks.EXPOSED_COPPER_GRATE, Blocks.WAXED_EXPOSED_COPPER_GRATE);
        this.registerParented(Blocks.WEATHERED_COPPER_GRATE, Blocks.WAXED_WEATHERED_COPPER_GRATE);
        this.registerParented(Blocks.OXIDIZED_COPPER_GRATE, Blocks.WAXED_OXIDIZED_COPPER_GRATE);
        this.registerLightningRod(Blocks.LIGHTNING_ROD, Blocks.WAXED_LIGHTNING_ROD);
        this.registerLightningRod(Blocks.EXPOSED_LIGHTNING_ROD, Blocks.WAXED_EXPOSED_LIGHTNING_ROD);
        this.registerLightningRod(Blocks.WEATHERED_LIGHTNING_ROD, Blocks.WAXED_WEATHERED_LIGHTNING_ROD);
        this.registerLightningRod(Blocks.OXIDIZED_LIGHTNING_ROD, Blocks.WAXED_OXIDIZED_LIGHTNING_ROD);
        this.registerWeightedPressurePlate(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Blocks.GOLD_BLOCK);
        this.registerWeightedPressurePlate(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Blocks.IRON_BLOCK);
        this.registerShelf(Blocks.ACACIA_SHELF, Blocks.STRIPPED_ACACIA_LOG);
        this.registerShelf(Blocks.BAMBOO_SHELF, Blocks.STRIPPED_BAMBOO_BLOCK);
        this.registerShelf(Blocks.BIRCH_SHELF, Blocks.STRIPPED_BIRCH_LOG);
        this.registerShelf(Blocks.CHERRY_SHELF, Blocks.STRIPPED_CHERRY_LOG);
        this.registerShelf(Blocks.CRIMSON_SHELF, Blocks.STRIPPED_CRIMSON_STEM);
        this.registerShelf(Blocks.DARK_OAK_SHELF, Blocks.STRIPPED_DARK_OAK_LOG);
        this.registerShelf(Blocks.JUNGLE_SHELF, Blocks.STRIPPED_JUNGLE_LOG);
        this.registerShelf(Blocks.MANGROVE_SHELF, Blocks.STRIPPED_MANGROVE_LOG);
        this.registerShelf(Blocks.OAK_SHELF, Blocks.STRIPPED_OAK_LOG);
        this.registerShelf(Blocks.PALE_OAK_SHELF, Blocks.STRIPPED_PALE_OAK_LOG);
        this.registerShelf(Blocks.SPRUCE_SHELF, Blocks.STRIPPED_SPRUCE_LOG);
        this.registerShelf(Blocks.WARPED_SHELF, Blocks.STRIPPED_WARPED_STEM);
        this.registerAmethysts();
        this.registerBookshelf();
        this.registerChiseledBookshelf();
        this.registerBrewingStand();
        this.registerCake();
        this.registerCampfire(Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE);
        this.registerCartographyTable();
        this.registerCauldrons();
        this.registerChorusFlower();
        this.registerChorusPlant();
        this.registerComposter();
        this.registerDaylightDetector();
        this.registerEndPortalFrame();
        this.registerRod(Blocks.END_ROD);
        this.registerFarmland();
        this.registerFire();
        this.registerSoulFire();
        this.registerFrostedIce();
        this.registerTopSoils();
        this.registerCocoa();
        this.registerDirtPath();
        this.registerGrindstone();
        this.registerHopper();
        this.registerBars(Blocks.IRON_BARS);
        Blocks.COPPER_BARS.getWaxingMap().forEach(this::registerCopperBars);
        this.registerLever();
        this.registerLilyPad();
        this.registerNetherPortal();
        this.registerNetherrack();
        this.registerObserver();
        this.registerPistons();
        this.registerPistonHead();
        this.registerScaffolding();
        this.registerRedstoneTorch();
        this.registerRedstoneLamp();
        this.registerRepeater();
        this.registerSeaPickle();
        this.registerSmithingTable();
        this.registerSnows();
        this.registerStonecutter();
        this.registerStructureBlock();
        this.registerSweetBerryBush();
        this.registerTestBlock();
        this.registerSimpleCubeAll(Blocks.TEST_INSTANCE_BLOCK);
        this.registerTripwire();
        this.registerTripwireHook();
        this.registerTurtleEgg();
        this.registerSnifferEgg();
        this.registerDriedGhast();
        this.registerVine();
        this.registerMultifaceBlock(Blocks.GLOW_LICHEN);
        this.registerMultifaceBlock(Blocks.SCULK_VEIN);
        this.registerMultifaceBlock(Blocks.RESIN_CLUMP, Items.RESIN_CLUMP);
        this.registerMagmaBlock();
        this.registerJigsaw();
        this.registerSculkSensor();
        this.registerCalibratedSculkSensor();
        this.registerSculkShrieker();
        this.registerFrogspawn();
        this.registerMangrovePropagule();
        this.registerMuddyMangroveRoots();
        this.registerTrialSpawner();
        this.registerVault();
        this.registerNorthDefaultHorizontalRotatable(Blocks.LADDER);
        this.registerItemModel(Blocks.LADDER);
        this.registerNorthDefaultHorizontalRotatable(Blocks.LECTERN);
        this.registerBigDripleaf();
        this.registerNorthDefaultHorizontalRotatable(Blocks.BIG_DRIPLEAF_STEM);
        this.registerTorch(Blocks.TORCH, Blocks.WALL_TORCH);
        this.registerTorch(Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH);
        this.registerTorch(Blocks.COPPER_TORCH, Blocks.COPPER_WALL_TORCH);
        this.registerCubeWithCustomTextures(Blocks.CRAFTING_TABLE, Blocks.OAK_PLANKS, TextureMap::frontSideWithCustomBottom);
        this.registerCubeWithCustomTextures(Blocks.FLETCHING_TABLE, Blocks.BIRCH_PLANKS, TextureMap::frontTopSide);
        this.registerNetherrackBottomCustomTop(Blocks.CRIMSON_NYLIUM);
        this.registerNetherrackBottomCustomTop(Blocks.WARPED_NYLIUM);
        this.registerDispenserLikeOrientable(Blocks.DISPENSER);
        this.registerDispenserLikeOrientable(Blocks.DROPPER);
        this.registerCrafter();
        this.registerLantern(Blocks.LANTERN);
        this.registerLantern(Blocks.SOUL_LANTERN);
        Blocks.COPPER_LANTERNS.getWaxingMap().forEach(this::registerCopperLantern);
        this.registerAxisRotated(Blocks.IRON_CHAIN, BlockStateModelGenerator.createWeightedVariant(TexturedModel.TEMPLATE_CHAIN.upload(Blocks.IRON_CHAIN, this.modelCollector)));
        Blocks.COPPER_CHAINS.getWaxingMap().forEach(this::registerCopperChain);
        this.registerAxisRotated(Blocks.BASALT, TexturedModel.CUBE_COLUMN);
        this.registerAxisRotated(Blocks.POLISHED_BASALT, TexturedModel.CUBE_COLUMN);
        this.registerSimpleCubeAll(Blocks.SMOOTH_BASALT);
        this.registerAxisRotated(Blocks.BONE_BLOCK, TexturedModel.CUBE_COLUMN);
        this.registerRotatable(Blocks.DIRT);
        this.registerRotatable(Blocks.ROOTED_DIRT);
        this.registerRotatable(Blocks.SAND);
        this.registerBrushableBlock(Blocks.SUSPICIOUS_SAND);
        this.registerBrushableBlock(Blocks.SUSPICIOUS_GRAVEL);
        this.registerRotatable(Blocks.RED_SAND);
        this.registerMirrorable(Blocks.BEDROCK);
        this.registerSingleton(Blocks.REINFORCED_DEEPSLATE, TexturedModel.CUBE_BOTTOM_TOP);
        this.registerAxisRotated(Blocks.HAY_BLOCK, TexturedModel.CUBE_COLUMN, TexturedModel.CUBE_COLUMN_HORIZONTAL);
        this.registerAxisRotated(Blocks.PURPUR_PILLAR, TexturedModel.END_FOR_TOP_CUBE_COLUMN, TexturedModel.END_FOR_TOP_CUBE_COLUMN_HORIZONTAL);
        this.registerAxisRotated(Blocks.QUARTZ_PILLAR, TexturedModel.END_FOR_TOP_CUBE_COLUMN, TexturedModel.END_FOR_TOP_CUBE_COLUMN_HORIZONTAL);
        this.registerAxisRotated(Blocks.OCHRE_FROGLIGHT, TexturedModel.CUBE_COLUMN, TexturedModel.CUBE_COLUMN_HORIZONTAL);
        this.registerAxisRotated(Blocks.VERDANT_FROGLIGHT, TexturedModel.CUBE_COLUMN, TexturedModel.CUBE_COLUMN_HORIZONTAL);
        this.registerAxisRotated(Blocks.PEARLESCENT_FROGLIGHT, TexturedModel.CUBE_COLUMN, TexturedModel.CUBE_COLUMN_HORIZONTAL);
        this.registerNorthDefaultHorizontalRotatable(Blocks.LOOM, TexturedModel.ORIENTABLE_WITH_BOTTOM);
        this.registerPumpkins();
        this.registerBeehive(Blocks.BEE_NEST, TextureMap::sideFrontTopBottom);
        this.registerBeehive(Blocks.BEEHIVE, TextureMap::sideFrontEnd);
        this.registerCrop(Blocks.BEETROOTS, Properties.AGE_3, 0, 1, 2, 3);
        this.registerCrop(Blocks.CARROTS, Properties.AGE_7, 0, 0, 1, 1, 2, 2, 2, 3);
        this.registerCrop(Blocks.NETHER_WART, Properties.AGE_3, 0, 1, 1, 2);
        this.registerCrop(Blocks.POTATOES, Properties.AGE_7, 0, 0, 1, 1, 2, 2, 2, 3);
        this.registerCrop(Blocks.WHEAT, Properties.AGE_7, 0, 1, 2, 3, 4, 5, 6, 7);
        this.registerTintableCrossBlockStateWithStages(Blocks.TORCHFLOWER_CROP, CrossType.NOT_TINTED, Properties.AGE_1, 0, 1);
        this.registerPitcherCrop();
        this.registerPitcherPlant();
        this.registerBanners();
        this.registerBeds();
        this.registerSkulls();
        this.registerChests();
        this.registerCopperChests();
        this.registerShulkerBox(Blocks.SHULKER_BOX, null);
        this.registerShulkerBox(Blocks.WHITE_SHULKER_BOX, DyeColor.WHITE);
        this.registerShulkerBox(Blocks.ORANGE_SHULKER_BOX, DyeColor.ORANGE);
        this.registerShulkerBox(Blocks.MAGENTA_SHULKER_BOX, DyeColor.MAGENTA);
        this.registerShulkerBox(Blocks.LIGHT_BLUE_SHULKER_BOX, DyeColor.LIGHT_BLUE);
        this.registerShulkerBox(Blocks.YELLOW_SHULKER_BOX, DyeColor.YELLOW);
        this.registerShulkerBox(Blocks.LIME_SHULKER_BOX, DyeColor.LIME);
        this.registerShulkerBox(Blocks.PINK_SHULKER_BOX, DyeColor.PINK);
        this.registerShulkerBox(Blocks.GRAY_SHULKER_BOX, DyeColor.GRAY);
        this.registerShulkerBox(Blocks.LIGHT_GRAY_SHULKER_BOX, DyeColor.LIGHT_GRAY);
        this.registerShulkerBox(Blocks.CYAN_SHULKER_BOX, DyeColor.CYAN);
        this.registerShulkerBox(Blocks.PURPLE_SHULKER_BOX, DyeColor.PURPLE);
        this.registerShulkerBox(Blocks.BLUE_SHULKER_BOX, DyeColor.BLUE);
        this.registerShulkerBox(Blocks.BROWN_SHULKER_BOX, DyeColor.BROWN);
        this.registerShulkerBox(Blocks.GREEN_SHULKER_BOX, DyeColor.GREEN);
        this.registerShulkerBox(Blocks.RED_SHULKER_BOX, DyeColor.RED);
        this.registerShulkerBox(Blocks.BLACK_SHULKER_BOX, DyeColor.BLACK);
        this.registerCopperGolemStatues();
        this.registerBuiltin(Blocks.CONDUIT);
        this.registerSpecialItemModel(Blocks.CONDUIT, new ConduitModelRenderer.Unbaked());
        this.registerBuiltinWithParticle(Blocks.DECORATED_POT, Blocks.TERRACOTTA);
        this.registerSpecialItemModel(Blocks.DECORATED_POT, new DecoratedPotModelRenderer.Unbaked());
        this.registerBuiltinWithParticle(Blocks.END_PORTAL, Blocks.OBSIDIAN);
        this.registerBuiltinWithParticle(Blocks.END_GATEWAY, Blocks.OBSIDIAN);
        this.registerSimpleCubeAll(Blocks.AZALEA_LEAVES);
        this.registerSimpleCubeAll(Blocks.FLOWERING_AZALEA_LEAVES);
        this.registerSimpleCubeAll(Blocks.WHITE_CONCRETE);
        this.registerSimpleCubeAll(Blocks.ORANGE_CONCRETE);
        this.registerSimpleCubeAll(Blocks.MAGENTA_CONCRETE);
        this.registerSimpleCubeAll(Blocks.LIGHT_BLUE_CONCRETE);
        this.registerSimpleCubeAll(Blocks.YELLOW_CONCRETE);
        this.registerSimpleCubeAll(Blocks.LIME_CONCRETE);
        this.registerSimpleCubeAll(Blocks.PINK_CONCRETE);
        this.registerSimpleCubeAll(Blocks.GRAY_CONCRETE);
        this.registerSimpleCubeAll(Blocks.LIGHT_GRAY_CONCRETE);
        this.registerSimpleCubeAll(Blocks.CYAN_CONCRETE);
        this.registerSimpleCubeAll(Blocks.PURPLE_CONCRETE);
        this.registerSimpleCubeAll(Blocks.BLUE_CONCRETE);
        this.registerSimpleCubeAll(Blocks.BROWN_CONCRETE);
        this.registerSimpleCubeAll(Blocks.GREEN_CONCRETE);
        this.registerSimpleCubeAll(Blocks.RED_CONCRETE);
        this.registerSimpleCubeAll(Blocks.BLACK_CONCRETE);
        this.registerRandomHorizontalRotations(TexturedModel.CUBE_ALL, Blocks.WHITE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE_POWDER, Blocks.LIME_CONCRETE_POWDER, Blocks.PINK_CONCRETE_POWDER, Blocks.GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.CYAN_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE_POWDER, Blocks.BROWN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE_POWDER, Blocks.RED_CONCRETE_POWDER, Blocks.BLACK_CONCRETE_POWDER);
        this.registerSimpleCubeAll(Blocks.TERRACOTTA);
        this.registerSimpleCubeAll(Blocks.WHITE_TERRACOTTA);
        this.registerSimpleCubeAll(Blocks.ORANGE_TERRACOTTA);
        this.registerSimpleCubeAll(Blocks.MAGENTA_TERRACOTTA);
        this.registerSimpleCubeAll(Blocks.LIGHT_BLUE_TERRACOTTA);
        this.registerSimpleCubeAll(Blocks.YELLOW_TERRACOTTA);
        this.registerSimpleCubeAll(Blocks.LIME_TERRACOTTA);
        this.registerSimpleCubeAll(Blocks.PINK_TERRACOTTA);
        this.registerSimpleCubeAll(Blocks.GRAY_TERRACOTTA);
        this.registerSimpleCubeAll(Blocks.LIGHT_GRAY_TERRACOTTA);
        this.registerSimpleCubeAll(Blocks.CYAN_TERRACOTTA);
        this.registerSimpleCubeAll(Blocks.PURPLE_TERRACOTTA);
        this.registerSimpleCubeAll(Blocks.BLUE_TERRACOTTA);
        this.registerSimpleCubeAll(Blocks.BROWN_TERRACOTTA);
        this.registerSimpleCubeAll(Blocks.GREEN_TERRACOTTA);
        this.registerSimpleCubeAll(Blocks.RED_TERRACOTTA);
        this.registerSimpleCubeAll(Blocks.BLACK_TERRACOTTA);
        this.registerSimpleCubeAll(Blocks.TINTED_GLASS);
        this.registerGlassAndPane(Blocks.GLASS, Blocks.GLASS_PANE);
        this.registerGlassAndPane(Blocks.WHITE_STAINED_GLASS, Blocks.WHITE_STAINED_GLASS_PANE);
        this.registerGlassAndPane(Blocks.ORANGE_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS_PANE);
        this.registerGlassAndPane(Blocks.MAGENTA_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS_PANE);
        this.registerGlassAndPane(Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE);
        this.registerGlassAndPane(Blocks.YELLOW_STAINED_GLASS, Blocks.YELLOW_STAINED_GLASS_PANE);
        this.registerGlassAndPane(Blocks.LIME_STAINED_GLASS, Blocks.LIME_STAINED_GLASS_PANE);
        this.registerGlassAndPane(Blocks.PINK_STAINED_GLASS, Blocks.PINK_STAINED_GLASS_PANE);
        this.registerGlassAndPane(Blocks.GRAY_STAINED_GLASS, Blocks.GRAY_STAINED_GLASS_PANE);
        this.registerGlassAndPane(Blocks.LIGHT_GRAY_STAINED_GLASS, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE);
        this.registerGlassAndPane(Blocks.CYAN_STAINED_GLASS, Blocks.CYAN_STAINED_GLASS_PANE);
        this.registerGlassAndPane(Blocks.PURPLE_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS_PANE);
        this.registerGlassAndPane(Blocks.BLUE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS_PANE);
        this.registerGlassAndPane(Blocks.BROWN_STAINED_GLASS, Blocks.BROWN_STAINED_GLASS_PANE);
        this.registerGlassAndPane(Blocks.GREEN_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS_PANE);
        this.registerGlassAndPane(Blocks.RED_STAINED_GLASS, Blocks.RED_STAINED_GLASS_PANE);
        this.registerGlassAndPane(Blocks.BLACK_STAINED_GLASS, Blocks.BLACK_STAINED_GLASS_PANE);
        this.registerSouthDefaultHorizontalFacing(TexturedModel.TEMPLATE_GLAZED_TERRACOTTA, Blocks.WHITE_GLAZED_TERRACOTTA, Blocks.ORANGE_GLAZED_TERRACOTTA, Blocks.MAGENTA_GLAZED_TERRACOTTA, Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA, Blocks.YELLOW_GLAZED_TERRACOTTA, Blocks.LIME_GLAZED_TERRACOTTA, Blocks.PINK_GLAZED_TERRACOTTA, Blocks.GRAY_GLAZED_TERRACOTTA, Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA, Blocks.CYAN_GLAZED_TERRACOTTA, Blocks.PURPLE_GLAZED_TERRACOTTA, Blocks.BLUE_GLAZED_TERRACOTTA, Blocks.BROWN_GLAZED_TERRACOTTA, Blocks.GREEN_GLAZED_TERRACOTTA, Blocks.RED_GLAZED_TERRACOTTA, Blocks.BLACK_GLAZED_TERRACOTTA);
        this.registerWoolAndCarpet(Blocks.WHITE_WOOL, Blocks.WHITE_CARPET);
        this.registerWoolAndCarpet(Blocks.ORANGE_WOOL, Blocks.ORANGE_CARPET);
        this.registerWoolAndCarpet(Blocks.MAGENTA_WOOL, Blocks.MAGENTA_CARPET);
        this.registerWoolAndCarpet(Blocks.LIGHT_BLUE_WOOL, Blocks.LIGHT_BLUE_CARPET);
        this.registerWoolAndCarpet(Blocks.YELLOW_WOOL, Blocks.YELLOW_CARPET);
        this.registerWoolAndCarpet(Blocks.LIME_WOOL, Blocks.LIME_CARPET);
        this.registerWoolAndCarpet(Blocks.PINK_WOOL, Blocks.PINK_CARPET);
        this.registerWoolAndCarpet(Blocks.GRAY_WOOL, Blocks.GRAY_CARPET);
        this.registerWoolAndCarpet(Blocks.LIGHT_GRAY_WOOL, Blocks.LIGHT_GRAY_CARPET);
        this.registerWoolAndCarpet(Blocks.CYAN_WOOL, Blocks.CYAN_CARPET);
        this.registerWoolAndCarpet(Blocks.PURPLE_WOOL, Blocks.PURPLE_CARPET);
        this.registerWoolAndCarpet(Blocks.BLUE_WOOL, Blocks.BLUE_CARPET);
        this.registerWoolAndCarpet(Blocks.BROWN_WOOL, Blocks.BROWN_CARPET);
        this.registerWoolAndCarpet(Blocks.GREEN_WOOL, Blocks.GREEN_CARPET);
        this.registerWoolAndCarpet(Blocks.RED_WOOL, Blocks.RED_CARPET);
        this.registerWoolAndCarpet(Blocks.BLACK_WOOL, Blocks.BLACK_CARPET);
        this.registerSimpleCubeAll(Blocks.MUD);
        this.registerSimpleCubeAll(Blocks.PACKED_MUD);
        this.registerFlowerPotPlant(Blocks.FERN, Blocks.POTTED_FERN, CrossType.TINTED);
        this.registerGrassTinted(Blocks.FERN);
        this.registerFlowerPotPlantAndItem(Blocks.DANDELION, Blocks.POTTED_DANDELION, CrossType.NOT_TINTED);
        this.registerFlowerPotPlantAndItem(Blocks.POPPY, Blocks.POTTED_POPPY, CrossType.NOT_TINTED);
        this.registerFlowerPotPlantAndItem(Blocks.OPEN_EYEBLOSSOM, Blocks.POTTED_OPEN_EYEBLOSSOM, CrossType.EMISSIVE_NOT_TINTED);
        this.registerFlowerPotPlantAndItem(Blocks.CLOSED_EYEBLOSSOM, Blocks.POTTED_CLOSED_EYEBLOSSOM, CrossType.NOT_TINTED);
        this.registerFlowerPotPlantAndItem(Blocks.BLUE_ORCHID, Blocks.POTTED_BLUE_ORCHID, CrossType.NOT_TINTED);
        this.registerFlowerPotPlantAndItem(Blocks.ALLIUM, Blocks.POTTED_ALLIUM, CrossType.NOT_TINTED);
        this.registerFlowerPotPlantAndItem(Blocks.AZURE_BLUET, Blocks.POTTED_AZURE_BLUET, CrossType.NOT_TINTED);
        this.registerFlowerPotPlantAndItem(Blocks.RED_TULIP, Blocks.POTTED_RED_TULIP, CrossType.NOT_TINTED);
        this.registerFlowerPotPlantAndItem(Blocks.ORANGE_TULIP, Blocks.POTTED_ORANGE_TULIP, CrossType.NOT_TINTED);
        this.registerFlowerPotPlantAndItem(Blocks.WHITE_TULIP, Blocks.POTTED_WHITE_TULIP, CrossType.NOT_TINTED);
        this.registerFlowerPotPlantAndItem(Blocks.PINK_TULIP, Blocks.POTTED_PINK_TULIP, CrossType.NOT_TINTED);
        this.registerFlowerPotPlantAndItem(Blocks.OXEYE_DAISY, Blocks.POTTED_OXEYE_DAISY, CrossType.NOT_TINTED);
        this.registerFlowerPotPlantAndItem(Blocks.CORNFLOWER, Blocks.POTTED_CORNFLOWER, CrossType.NOT_TINTED);
        this.registerFlowerPotPlantAndItem(Blocks.LILY_OF_THE_VALLEY, Blocks.POTTED_LILY_OF_THE_VALLEY, CrossType.NOT_TINTED);
        this.registerFlowerPotPlantAndItem(Blocks.WITHER_ROSE, Blocks.POTTED_WITHER_ROSE, CrossType.NOT_TINTED);
        this.registerFlowerPotPlantAndItem(Blocks.RED_MUSHROOM, Blocks.POTTED_RED_MUSHROOM, CrossType.NOT_TINTED);
        this.registerFlowerPotPlantAndItem(Blocks.BROWN_MUSHROOM, Blocks.POTTED_BROWN_MUSHROOM, CrossType.NOT_TINTED);
        this.registerFlowerPotPlantAndItem(Blocks.DEAD_BUSH, Blocks.POTTED_DEAD_BUSH, CrossType.NOT_TINTED);
        this.registerFlowerPotPlantAndItem(Blocks.TORCHFLOWER, Blocks.POTTED_TORCHFLOWER, CrossType.NOT_TINTED);
        this.registerPointedDripstone();
        this.registerMushroomBlock(Blocks.BROWN_MUSHROOM_BLOCK);
        this.registerMushroomBlock(Blocks.RED_MUSHROOM_BLOCK);
        this.registerMushroomBlock(Blocks.MUSHROOM_STEM);
        this.registerTintableCrossBlockState(Blocks.SHORT_GRASS, CrossType.TINTED);
        this.registerGrassTinted(Blocks.SHORT_GRASS);
        this.registerTintableCross(Blocks.SHORT_DRY_GRASS, CrossType.NOT_TINTED);
        this.registerTintableCross(Blocks.TALL_DRY_GRASS, CrossType.NOT_TINTED);
        this.registerTintableCrossBlockState(Blocks.BUSH, CrossType.TINTED);
        this.registerGrassTinted(Blocks.BUSH);
        this.registerTintableCrossBlockState(Blocks.SUGAR_CANE, CrossType.TINTED);
        this.registerItemModel(Items.SUGAR_CANE);
        this.registerPlantPart(Blocks.KELP, Blocks.KELP_PLANT, CrossType.NOT_TINTED);
        this.registerItemModel(Items.KELP);
        this.registerTintableCrossBlockState(Blocks.HANGING_ROOTS, CrossType.NOT_TINTED);
        this.registerPlantPart(Blocks.WEEPING_VINES, Blocks.WEEPING_VINES_PLANT, CrossType.NOT_TINTED);
        this.registerPlantPart(Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, CrossType.NOT_TINTED);
        this.registerItemModel(Blocks.WEEPING_VINES, "_plant");
        this.registerItemModel(Blocks.TWISTING_VINES, "_plant");
        this.registerTintableCross(Blocks.BAMBOO_SAPLING, CrossType.TINTED, TextureMap.cross(TextureMap.getSubId(Blocks.BAMBOO, "_stage0")));
        this.registerBamboo();
        this.registerTintableCross(Blocks.CACTUS_FLOWER, CrossType.NOT_TINTED);
        this.registerTintableCross(Blocks.COBWEB, CrossType.NOT_TINTED);
        this.registerDoubleBlockAndItem(Blocks.LILAC, CrossType.NOT_TINTED);
        this.registerDoubleBlockAndItem(Blocks.ROSE_BUSH, CrossType.NOT_TINTED);
        this.registerDoubleBlockAndItem(Blocks.PEONY, CrossType.NOT_TINTED);
        this.registerGrassTintedDoubleBlockAndItem(Blocks.TALL_GRASS);
        this.registerGrassTintedDoubleBlockAndItem(Blocks.LARGE_FERN);
        this.registerSunflower();
        this.registerTallSeagrass();
        this.registerSmallDripleaf();
        this.registerCoral(Blocks.TUBE_CORAL, Blocks.DEAD_TUBE_CORAL, Blocks.TUBE_CORAL_BLOCK, Blocks.DEAD_TUBE_CORAL_BLOCK, Blocks.TUBE_CORAL_FAN, Blocks.DEAD_TUBE_CORAL_FAN, Blocks.TUBE_CORAL_WALL_FAN, Blocks.DEAD_TUBE_CORAL_WALL_FAN);
        this.registerCoral(Blocks.BRAIN_CORAL, Blocks.DEAD_BRAIN_CORAL, Blocks.BRAIN_CORAL_BLOCK, Blocks.DEAD_BRAIN_CORAL_BLOCK, Blocks.BRAIN_CORAL_FAN, Blocks.DEAD_BRAIN_CORAL_FAN, Blocks.BRAIN_CORAL_WALL_FAN, Blocks.DEAD_BRAIN_CORAL_WALL_FAN);
        this.registerCoral(Blocks.BUBBLE_CORAL, Blocks.DEAD_BUBBLE_CORAL, Blocks.BUBBLE_CORAL_BLOCK, Blocks.DEAD_BUBBLE_CORAL_BLOCK, Blocks.BUBBLE_CORAL_FAN, Blocks.DEAD_BUBBLE_CORAL_FAN, Blocks.BUBBLE_CORAL_WALL_FAN, Blocks.DEAD_BUBBLE_CORAL_WALL_FAN);
        this.registerCoral(Blocks.FIRE_CORAL, Blocks.DEAD_FIRE_CORAL, Blocks.FIRE_CORAL_BLOCK, Blocks.DEAD_FIRE_CORAL_BLOCK, Blocks.FIRE_CORAL_FAN, Blocks.DEAD_FIRE_CORAL_FAN, Blocks.FIRE_CORAL_WALL_FAN, Blocks.DEAD_FIRE_CORAL_WALL_FAN);
        this.registerCoral(Blocks.HORN_CORAL, Blocks.DEAD_HORN_CORAL, Blocks.HORN_CORAL_BLOCK, Blocks.DEAD_HORN_CORAL_BLOCK, Blocks.HORN_CORAL_FAN, Blocks.DEAD_HORN_CORAL_FAN, Blocks.HORN_CORAL_WALL_FAN, Blocks.DEAD_HORN_CORAL_WALL_FAN);
        this.registerGourd(Blocks.MELON_STEM, Blocks.ATTACHED_MELON_STEM);
        this.registerGourd(Blocks.PUMPKIN_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
        this.createLogTexturePool(Blocks.MANGROVE_LOG).log(Blocks.MANGROVE_LOG).wood(Blocks.MANGROVE_WOOD);
        this.createLogTexturePool(Blocks.STRIPPED_MANGROVE_LOG).log(Blocks.STRIPPED_MANGROVE_LOG).wood(Blocks.STRIPPED_MANGROVE_WOOD);
        this.registerHangingSign(Blocks.STRIPPED_MANGROVE_LOG, Blocks.MANGROVE_HANGING_SIGN, Blocks.MANGROVE_WALL_HANGING_SIGN);
        this.registerTintedBlockAndItem(Blocks.MANGROVE_LEAVES, TexturedModel.LEAVES, -7158200);
        this.createLogTexturePool(Blocks.ACACIA_LOG).log(Blocks.ACACIA_LOG).wood(Blocks.ACACIA_WOOD);
        this.createLogTexturePool(Blocks.STRIPPED_ACACIA_LOG).log(Blocks.STRIPPED_ACACIA_LOG).wood(Blocks.STRIPPED_ACACIA_WOOD);
        this.registerHangingSign(Blocks.STRIPPED_ACACIA_LOG, Blocks.ACACIA_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN);
        this.registerFlowerPotPlantAndItem(Blocks.ACACIA_SAPLING, Blocks.POTTED_ACACIA_SAPLING, CrossType.NOT_TINTED);
        this.registerTintedBlockAndItem(Blocks.ACACIA_LEAVES, TexturedModel.LEAVES, -12012264);
        this.createLogTexturePool(Blocks.CHERRY_LOG).uvLockedLog(Blocks.CHERRY_LOG).wood(Blocks.CHERRY_WOOD);
        this.createLogTexturePool(Blocks.STRIPPED_CHERRY_LOG).uvLockedLog(Blocks.STRIPPED_CHERRY_LOG).wood(Blocks.STRIPPED_CHERRY_WOOD);
        this.registerHangingSign(Blocks.STRIPPED_CHERRY_LOG, Blocks.CHERRY_HANGING_SIGN, Blocks.CHERRY_WALL_HANGING_SIGN);
        this.registerFlowerPotPlantAndItem(Blocks.CHERRY_SAPLING, Blocks.POTTED_CHERRY_SAPLING, CrossType.NOT_TINTED);
        this.registerSingleton(Blocks.CHERRY_LEAVES, TexturedModel.LEAVES);
        this.createLogTexturePool(Blocks.BIRCH_LOG).log(Blocks.BIRCH_LOG).wood(Blocks.BIRCH_WOOD);
        this.createLogTexturePool(Blocks.STRIPPED_BIRCH_LOG).log(Blocks.STRIPPED_BIRCH_LOG).wood(Blocks.STRIPPED_BIRCH_WOOD);
        this.registerHangingSign(Blocks.STRIPPED_BIRCH_LOG, Blocks.BIRCH_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN);
        this.registerFlowerPotPlantAndItem(Blocks.BIRCH_SAPLING, Blocks.POTTED_BIRCH_SAPLING, CrossType.NOT_TINTED);
        this.registerTintedBlockAndItem(Blocks.BIRCH_LEAVES, TexturedModel.LEAVES, -8345771);
        this.createLogTexturePool(Blocks.OAK_LOG).log(Blocks.OAK_LOG).wood(Blocks.OAK_WOOD);
        this.createLogTexturePool(Blocks.STRIPPED_OAK_LOG).log(Blocks.STRIPPED_OAK_LOG).wood(Blocks.STRIPPED_OAK_WOOD);
        this.registerHangingSign(Blocks.STRIPPED_OAK_LOG, Blocks.OAK_HANGING_SIGN, Blocks.OAK_WALL_HANGING_SIGN);
        this.registerFlowerPotPlantAndItem(Blocks.OAK_SAPLING, Blocks.POTTED_OAK_SAPLING, CrossType.NOT_TINTED);
        this.registerTintedBlockAndItem(Blocks.OAK_LEAVES, TexturedModel.LEAVES, -12012264);
        this.createLogTexturePool(Blocks.SPRUCE_LOG).log(Blocks.SPRUCE_LOG).wood(Blocks.SPRUCE_WOOD);
        this.createLogTexturePool(Blocks.STRIPPED_SPRUCE_LOG).log(Blocks.STRIPPED_SPRUCE_LOG).wood(Blocks.STRIPPED_SPRUCE_WOOD);
        this.registerHangingSign(Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN);
        this.registerFlowerPotPlantAndItem(Blocks.SPRUCE_SAPLING, Blocks.POTTED_SPRUCE_SAPLING, CrossType.NOT_TINTED);
        this.registerTintedBlockAndItem(Blocks.SPRUCE_LEAVES, TexturedModel.LEAVES, -10380959);
        this.createLogTexturePool(Blocks.DARK_OAK_LOG).log(Blocks.DARK_OAK_LOG).wood(Blocks.DARK_OAK_WOOD);
        this.createLogTexturePool(Blocks.STRIPPED_DARK_OAK_LOG).log(Blocks.STRIPPED_DARK_OAK_LOG).wood(Blocks.STRIPPED_DARK_OAK_WOOD);
        this.registerHangingSign(Blocks.STRIPPED_DARK_OAK_LOG, Blocks.DARK_OAK_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN);
        this.registerFlowerPotPlantAndItem(Blocks.DARK_OAK_SAPLING, Blocks.POTTED_DARK_OAK_SAPLING, CrossType.NOT_TINTED);
        this.registerTintedBlockAndItem(Blocks.DARK_OAK_LEAVES, TexturedModel.LEAVES, -12012264);
        this.createLogTexturePool(Blocks.PALE_OAK_LOG).log(Blocks.PALE_OAK_LOG).wood(Blocks.PALE_OAK_WOOD);
        this.createLogTexturePool(Blocks.STRIPPED_PALE_OAK_LOG).log(Blocks.STRIPPED_PALE_OAK_LOG).wood(Blocks.STRIPPED_PALE_OAK_WOOD);
        this.registerHangingSign(Blocks.STRIPPED_PALE_OAK_LOG, Blocks.PALE_OAK_HANGING_SIGN, Blocks.PALE_OAK_WALL_HANGING_SIGN);
        this.registerFlowerPotPlantAndItem(Blocks.PALE_OAK_SAPLING, Blocks.POTTED_PALE_OAK_SAPLING, CrossType.NOT_TINTED);
        this.registerSingleton(Blocks.PALE_OAK_LEAVES, TexturedModel.LEAVES);
        this.createLogTexturePool(Blocks.JUNGLE_LOG).log(Blocks.JUNGLE_LOG).wood(Blocks.JUNGLE_WOOD);
        this.createLogTexturePool(Blocks.STRIPPED_JUNGLE_LOG).log(Blocks.STRIPPED_JUNGLE_LOG).wood(Blocks.STRIPPED_JUNGLE_WOOD);
        this.registerHangingSign(Blocks.STRIPPED_JUNGLE_LOG, Blocks.JUNGLE_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN);
        this.registerFlowerPotPlantAndItem(Blocks.JUNGLE_SAPLING, Blocks.POTTED_JUNGLE_SAPLING, CrossType.NOT_TINTED);
        this.registerTintedBlockAndItem(Blocks.JUNGLE_LEAVES, TexturedModel.LEAVES, -12012264);
        this.createLogTexturePool(Blocks.CRIMSON_STEM).stem(Blocks.CRIMSON_STEM).wood(Blocks.CRIMSON_HYPHAE);
        this.createLogTexturePool(Blocks.STRIPPED_CRIMSON_STEM).stem(Blocks.STRIPPED_CRIMSON_STEM).wood(Blocks.STRIPPED_CRIMSON_HYPHAE);
        this.registerHangingSign(Blocks.STRIPPED_CRIMSON_STEM, Blocks.CRIMSON_HANGING_SIGN, Blocks.CRIMSON_WALL_HANGING_SIGN);
        this.registerFlowerPotPlantAndItem(Blocks.CRIMSON_FUNGUS, Blocks.POTTED_CRIMSON_FUNGUS, CrossType.NOT_TINTED);
        this.registerRoots(Blocks.CRIMSON_ROOTS, Blocks.POTTED_CRIMSON_ROOTS);
        this.createLogTexturePool(Blocks.WARPED_STEM).stem(Blocks.WARPED_STEM).wood(Blocks.WARPED_HYPHAE);
        this.createLogTexturePool(Blocks.STRIPPED_WARPED_STEM).stem(Blocks.STRIPPED_WARPED_STEM).wood(Blocks.STRIPPED_WARPED_HYPHAE);
        this.registerHangingSign(Blocks.STRIPPED_WARPED_STEM, Blocks.WARPED_HANGING_SIGN, Blocks.WARPED_WALL_HANGING_SIGN);
        this.registerFlowerPotPlantAndItem(Blocks.WARPED_FUNGUS, Blocks.POTTED_WARPED_FUNGUS, CrossType.NOT_TINTED);
        this.registerRoots(Blocks.WARPED_ROOTS, Blocks.POTTED_WARPED_ROOTS);
        this.createLogTexturePool(Blocks.BAMBOO_BLOCK).uvLockedLog(Blocks.BAMBOO_BLOCK);
        this.createLogTexturePool(Blocks.STRIPPED_BAMBOO_BLOCK).uvLockedLog(Blocks.STRIPPED_BAMBOO_BLOCK);
        this.registerHangingSign(Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_HANGING_SIGN, Blocks.BAMBOO_WALL_HANGING_SIGN);
        this.registerTintableCrossBlockState(Blocks.NETHER_SPROUTS, CrossType.NOT_TINTED);
        this.registerItemModel(Items.NETHER_SPROUTS);
        this.registerDoor(Blocks.IRON_DOOR);
        this.registerTrapdoor(Blocks.IRON_TRAPDOOR);
        this.registerSmoothStone();
        this.registerTurnableRail(Blocks.RAIL);
        this.registerStraightRail(Blocks.POWERED_RAIL);
        this.registerStraightRail(Blocks.DETECTOR_RAIL);
        this.registerStraightRail(Blocks.ACTIVATOR_RAIL);
        this.registerComparator();
        this.registerCommandBlock(Blocks.COMMAND_BLOCK);
        this.registerCommandBlock(Blocks.REPEATING_COMMAND_BLOCK);
        this.registerCommandBlock(Blocks.CHAIN_COMMAND_BLOCK);
        this.registerAnvil(Blocks.ANVIL);
        this.registerAnvil(Blocks.CHIPPED_ANVIL);
        this.registerAnvil(Blocks.DAMAGED_ANVIL);
        this.registerBarrel();
        this.registerBell();
        this.registerCooker(Blocks.FURNACE, TexturedModel.ORIENTABLE);
        this.registerCooker(Blocks.BLAST_FURNACE, TexturedModel.ORIENTABLE);
        this.registerCooker(Blocks.SMOKER, TexturedModel.ORIENTABLE_WITH_BOTTOM);
        this.registerRedstone();
        this.registerRespawnAnchor();
        this.registerSculkCatalyst();
        this.registerParented(Blocks.CHISELED_STONE_BRICKS, Blocks.INFESTED_CHISELED_STONE_BRICKS);
        this.registerParented(Blocks.COBBLESTONE, Blocks.INFESTED_COBBLESTONE);
        this.registerParented(Blocks.CRACKED_STONE_BRICKS, Blocks.INFESTED_CRACKED_STONE_BRICKS);
        this.registerParented(Blocks.MOSSY_STONE_BRICKS, Blocks.INFESTED_MOSSY_STONE_BRICKS);
        this.registerInfestedStone();
        this.registerParented(Blocks.STONE_BRICKS, Blocks.INFESTED_STONE_BRICKS);
        this.registerInfestedDeepslate();
    }

    private void registerLightBlock() {
        ItemModel.Unbaked lv = ItemModels.basic(this.uploadItemModel(Items.LIGHT));
        HashMap<Integer, ItemModel.Unbaked> map = new HashMap<Integer, ItemModel.Unbaked>(16);
        BlockStateVariantMap.SingleProperty<WeightedVariant, Integer> lv2 = BlockStateVariantMap.models(Properties.LEVEL_15);
        for (int i = 0; i <= 15; ++i) {
            String string = String.format(Locale.ROOT, "_%02d", i);
            Identifier lv3 = TextureMap.getSubId(Items.LIGHT, string);
            lv2.register(i, BlockStateModelGenerator.createWeightedVariant(Models.PARTICLE.upload(Blocks.LIGHT, string, TextureMap.particle(lv3), this.modelCollector)));
            ItemModel.Unbaked lv4 = ItemModels.basic(Models.GENERATED.upload(ModelIds.getItemSubModelId(Items.LIGHT, string), TextureMap.layer0(lv3), this.modelCollector));
            map.put(i, lv4);
        }
        this.itemModelOutput.accept(Items.LIGHT, ItemModels.select(LightBlock.LEVEL_15, lv, map));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(Blocks.LIGHT).with(lv2));
    }

    private void registerWaxable(Item unwaxed, Item waxed) {
        Identifier lv = this.uploadItemModel(unwaxed);
        this.registerItemModel(unwaxed, lv);
        this.registerItemModel(waxed, lv);
    }

    private void registerCandle(Block candle, Block cake) {
        this.registerItemModel(candle.asItem());
        TextureMap lv = TextureMap.all(TextureMap.getId(candle));
        TextureMap lv2 = TextureMap.all(TextureMap.getSubId(candle, "_lit"));
        WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_CANDLE.upload(candle, "_one_candle", lv, this.modelCollector));
        WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_TWO_CANDLES.upload(candle, "_two_candles", lv, this.modelCollector));
        WeightedVariant lv5 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_THREE_CANDLES.upload(candle, "_three_candles", lv, this.modelCollector));
        WeightedVariant lv6 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_FOUR_CANDLES.upload(candle, "_four_candles", lv, this.modelCollector));
        WeightedVariant lv7 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_CANDLE.upload(candle, "_one_candle_lit", lv2, this.modelCollector));
        WeightedVariant lv8 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_TWO_CANDLES.upload(candle, "_two_candles_lit", lv2, this.modelCollector));
        WeightedVariant lv9 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_THREE_CANDLES.upload(candle, "_three_candles_lit", lv2, this.modelCollector));
        WeightedVariant lv10 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_FOUR_CANDLES.upload(candle, "_four_candles_lit", lv2, this.modelCollector));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(candle).with(BlockStateVariantMap.models(Properties.CANDLES, Properties.LIT).register(1, false, lv3).register(2, false, lv4).register(3, false, lv5).register(4, false, lv6).register(1, true, lv7).register(2, true, lv8).register(3, true, lv9).register(4, true, lv10)));
        WeightedVariant lv11 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_CAKE_WITH_CANDLE.upload(cake, TextureMap.candleCake(candle, false), this.modelCollector));
        WeightedVariant lv12 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_CAKE_WITH_CANDLE.upload(cake, "_lit", TextureMap.candleCake(candle, true), this.modelCollector));
        this.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(cake).with(BlockStateModelGenerator.createBooleanModelMap(Properties.LIT, lv12, lv11)));
    }

    @Environment(value=EnvType.CLIENT)
    class BlockTexturePool {
        private final TextureMap textures;
        private final Map<Model, Identifier> knownModels = new HashMap<Model, Identifier>();
        @Nullable
        private BlockFamily family;
        @Nullable
        private ModelVariant baseModelId;
        private final Set<Block> children = new HashSet<Block>();

        public BlockTexturePool(TextureMap textures) {
            this.textures = textures;
        }

        public BlockTexturePool base(Block block, Model model) {
            this.baseModelId = BlockStateModelGenerator.createModelVariant(model.upload(block, this.textures, BlockStateModelGenerator.this.modelCollector));
            if (BASE_WITH_CUSTOM_GENERATOR.containsKey(block)) {
                BlockStateModelGenerator.this.blockStateCollector.accept(BASE_WITH_CUSTOM_GENERATOR.get(block).create(block, this.baseModelId, this.textures, BlockStateModelGenerator.this.modelCollector));
            } else {
                BlockStateModelGenerator.this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(block, BlockStateModelGenerator.createWeightedVariant(this.baseModelId)));
            }
            return this;
        }

        public BlockTexturePool parented(Block parent, Block child) {
            Identifier lv = ModelIds.getBlockModelId(parent);
            BlockStateModelGenerator.this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(child, BlockStateModelGenerator.createWeightedVariant(lv)));
            BlockStateModelGenerator.this.itemModelOutput.acceptAlias(parent.asItem(), child.asItem());
            this.children.add(child);
            return this;
        }

        public BlockTexturePool button(Block buttonBlock) {
            WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(Models.BUTTON.upload(buttonBlock, this.textures, BlockStateModelGenerator.this.modelCollector));
            WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.BUTTON_PRESSED.upload(buttonBlock, this.textures, BlockStateModelGenerator.this.modelCollector));
            BlockStateModelGenerator.this.blockStateCollector.accept(BlockStateModelGenerator.createButtonBlockState(buttonBlock, lv, lv2));
            Identifier lv3 = Models.BUTTON_INVENTORY.upload(buttonBlock, this.textures, BlockStateModelGenerator.this.modelCollector);
            BlockStateModelGenerator.this.registerParentedItemModel(buttonBlock, lv3);
            return this;
        }

        public BlockTexturePool wall(Block wallBlock) {
            WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_WALL_POST.upload(wallBlock, this.textures, BlockStateModelGenerator.this.modelCollector));
            WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_WALL_SIDE.upload(wallBlock, this.textures, BlockStateModelGenerator.this.modelCollector));
            WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_WALL_SIDE_TALL.upload(wallBlock, this.textures, BlockStateModelGenerator.this.modelCollector));
            BlockStateModelGenerator.this.blockStateCollector.accept(BlockStateModelGenerator.createWallBlockState(wallBlock, lv, lv2, lv3));
            Identifier lv4 = Models.WALL_INVENTORY.upload(wallBlock, this.textures, BlockStateModelGenerator.this.modelCollector);
            BlockStateModelGenerator.this.registerParentedItemModel(wallBlock, lv4);
            return this;
        }

        public BlockTexturePool customFence(Block customFenceBlock) {
            TextureMap lv = TextureMap.textureParticle(customFenceBlock);
            WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.CUSTOM_FENCE_POST.upload(customFenceBlock, lv, BlockStateModelGenerator.this.modelCollector));
            WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(Models.CUSTOM_FENCE_SIDE_NORTH.upload(customFenceBlock, lv, BlockStateModelGenerator.this.modelCollector));
            WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(Models.CUSTOM_FENCE_SIDE_EAST.upload(customFenceBlock, lv, BlockStateModelGenerator.this.modelCollector));
            WeightedVariant lv5 = BlockStateModelGenerator.createWeightedVariant(Models.CUSTOM_FENCE_SIDE_SOUTH.upload(customFenceBlock, lv, BlockStateModelGenerator.this.modelCollector));
            WeightedVariant lv6 = BlockStateModelGenerator.createWeightedVariant(Models.CUSTOM_FENCE_SIDE_WEST.upload(customFenceBlock, lv, BlockStateModelGenerator.this.modelCollector));
            BlockStateModelGenerator.this.blockStateCollector.accept(BlockStateModelGenerator.createCustomFenceBlockState(customFenceBlock, lv2, lv3, lv4, lv5, lv6));
            Identifier lv7 = Models.CUSTOM_FENCE_INVENTORY.upload(customFenceBlock, lv, BlockStateModelGenerator.this.modelCollector);
            BlockStateModelGenerator.this.registerParentedItemModel(customFenceBlock, lv7);
            return this;
        }

        public BlockTexturePool fence(Block fenceBlock) {
            WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(Models.FENCE_POST.upload(fenceBlock, this.textures, BlockStateModelGenerator.this.modelCollector));
            WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.FENCE_SIDE.upload(fenceBlock, this.textures, BlockStateModelGenerator.this.modelCollector));
            BlockStateModelGenerator.this.blockStateCollector.accept(BlockStateModelGenerator.createFenceBlockState(fenceBlock, lv, lv2));
            Identifier lv3 = Models.FENCE_INVENTORY.upload(fenceBlock, this.textures, BlockStateModelGenerator.this.modelCollector);
            BlockStateModelGenerator.this.registerParentedItemModel(fenceBlock, lv3);
            return this;
        }

        public BlockTexturePool customFenceGate(Block customFenceGateBlock) {
            TextureMap lv = TextureMap.textureParticle(customFenceGateBlock);
            WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_CUSTOM_FENCE_GATE_OPEN.upload(customFenceGateBlock, lv, BlockStateModelGenerator.this.modelCollector));
            WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_CUSTOM_FENCE_GATE.upload(customFenceGateBlock, lv, BlockStateModelGenerator.this.modelCollector));
            WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_CUSTOM_FENCE_GATE_WALL_OPEN.upload(customFenceGateBlock, lv, BlockStateModelGenerator.this.modelCollector));
            WeightedVariant lv5 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_CUSTOM_FENCE_GATE_WALL.upload(customFenceGateBlock, lv, BlockStateModelGenerator.this.modelCollector));
            BlockStateModelGenerator.this.blockStateCollector.accept(BlockStateModelGenerator.createFenceGateBlockState(customFenceGateBlock, lv2, lv3, lv4, lv5, false));
            return this;
        }

        public BlockTexturePool fenceGate(Block fenceGateBlock) {
            WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_FENCE_GATE_OPEN.upload(fenceGateBlock, this.textures, BlockStateModelGenerator.this.modelCollector));
            WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_FENCE_GATE.upload(fenceGateBlock, this.textures, BlockStateModelGenerator.this.modelCollector));
            WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_FENCE_GATE_WALL_OPEN.upload(fenceGateBlock, this.textures, BlockStateModelGenerator.this.modelCollector));
            WeightedVariant lv4 = BlockStateModelGenerator.createWeightedVariant(Models.TEMPLATE_FENCE_GATE_WALL.upload(fenceGateBlock, this.textures, BlockStateModelGenerator.this.modelCollector));
            BlockStateModelGenerator.this.blockStateCollector.accept(BlockStateModelGenerator.createFenceGateBlockState(fenceGateBlock, lv, lv2, lv3, lv4, true));
            return this;
        }

        public BlockTexturePool pressurePlate(Block pressurePlateBlock) {
            WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(Models.PRESSURE_PLATE_UP.upload(pressurePlateBlock, this.textures, BlockStateModelGenerator.this.modelCollector));
            WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.PRESSURE_PLATE_DOWN.upload(pressurePlateBlock, this.textures, BlockStateModelGenerator.this.modelCollector));
            BlockStateModelGenerator.this.blockStateCollector.accept(BlockStateModelGenerator.createPressurePlateBlockState(pressurePlateBlock, lv, lv2));
            return this;
        }

        public BlockTexturePool sign(Block signBlock) {
            if (this.family == null) {
                throw new IllegalStateException("Family not defined");
            }
            Block lv = this.family.getVariants().get((Object)BlockFamily.Variant.WALL_SIGN);
            WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.PARTICLE.upload(signBlock, this.textures, BlockStateModelGenerator.this.modelCollector));
            BlockStateModelGenerator.this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(signBlock, lv2));
            BlockStateModelGenerator.this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(lv, lv2));
            BlockStateModelGenerator.this.registerItemModel(signBlock.asItem());
            return this;
        }

        public BlockTexturePool slab(Block block) {
            if (this.baseModelId == null) {
                throw new IllegalStateException("Full block not generated yet");
            }
            Identifier lv = this.ensureModel(Models.SLAB, block);
            WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(this.ensureModel(Models.SLAB_TOP, block));
            BlockStateModelGenerator.this.blockStateCollector.accept(BlockStateModelGenerator.createSlabBlockState(block, BlockStateModelGenerator.createWeightedVariant(lv), lv2, BlockStateModelGenerator.createWeightedVariant(this.baseModelId)));
            BlockStateModelGenerator.this.registerParentedItemModel(block, lv);
            return this;
        }

        public BlockTexturePool stairs(Block block) {
            WeightedVariant lv = BlockStateModelGenerator.createWeightedVariant(this.ensureModel(Models.INNER_STAIRS, block));
            Identifier lv2 = this.ensureModel(Models.STAIRS, block);
            WeightedVariant lv3 = BlockStateModelGenerator.createWeightedVariant(this.ensureModel(Models.OUTER_STAIRS, block));
            BlockStateModelGenerator.this.blockStateCollector.accept(BlockStateModelGenerator.createStairsBlockState(block, lv, BlockStateModelGenerator.createWeightedVariant(lv2), lv3));
            BlockStateModelGenerator.this.registerParentedItemModel(block, lv2);
            return this;
        }

        private BlockTexturePool block(Block block) {
            TexturedModel lv = TEXTURED_MODELS.getOrDefault(block, TexturedModel.CUBE_ALL.get(block));
            WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(lv.upload(block, BlockStateModelGenerator.this.modelCollector));
            BlockStateModelGenerator.this.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(block, lv2));
            return this;
        }

        private BlockTexturePool door(Block block) {
            BlockStateModelGenerator.this.registerDoor(block);
            return this;
        }

        private void registerTrapdoor(Block block) {
            if (UNORIENTABLE_TRAPDOORS.contains(block)) {
                BlockStateModelGenerator.this.registerTrapdoor(block);
            } else {
                BlockStateModelGenerator.this.registerOrientableTrapdoor(block);
            }
        }

        private Identifier ensureModel(Model model, Block block) {
            return this.knownModels.computeIfAbsent(model, newModel -> newModel.upload(block, this.textures, BlockStateModelGenerator.this.modelCollector));
        }

        public BlockTexturePool family(BlockFamily family) {
            this.family = family;
            family.getVariants().forEach((variant, block) -> {
                if (this.children.contains(block)) {
                    return;
                }
                BiConsumer<BlockTexturePool, Block> biConsumer = VARIANT_POOL_FUNCTIONS.get(variant);
                if (biConsumer != null) {
                    biConsumer.accept(this, (Block)block);
                }
            });
            return this;
        }
    }

    @Environment(value=EnvType.CLIENT)
    class LogTexturePool {
        private final TextureMap textures;

        public LogTexturePool(TextureMap textures) {
            this.textures = textures;
        }

        public LogTexturePool wood(Block woodBlock) {
            TextureMap lv = this.textures.copyAndAdd(TextureKey.END, this.textures.getTexture(TextureKey.SIDE));
            Identifier lv2 = Models.CUBE_COLUMN.upload(woodBlock, lv, BlockStateModelGenerator.this.modelCollector);
            BlockStateModelGenerator.this.blockStateCollector.accept(BlockStateModelGenerator.createAxisRotatedBlockState(woodBlock, BlockStateModelGenerator.createWeightedVariant(lv2)));
            BlockStateModelGenerator.this.registerParentedItemModel(woodBlock, lv2);
            return this;
        }

        public LogTexturePool stem(Block stemBlock) {
            Identifier lv = Models.CUBE_COLUMN.upload(stemBlock, this.textures, BlockStateModelGenerator.this.modelCollector);
            BlockStateModelGenerator.this.blockStateCollector.accept(BlockStateModelGenerator.createAxisRotatedBlockState(stemBlock, BlockStateModelGenerator.createWeightedVariant(lv)));
            BlockStateModelGenerator.this.registerParentedItemModel(stemBlock, lv);
            return this;
        }

        public LogTexturePool log(Block logBlock) {
            Identifier lv = Models.CUBE_COLUMN.upload(logBlock, this.textures, BlockStateModelGenerator.this.modelCollector);
            WeightedVariant lv2 = BlockStateModelGenerator.createWeightedVariant(Models.CUBE_COLUMN_HORIZONTAL.upload(logBlock, this.textures, BlockStateModelGenerator.this.modelCollector));
            BlockStateModelGenerator.this.blockStateCollector.accept(BlockStateModelGenerator.createAxisRotatedBlockState(logBlock, BlockStateModelGenerator.createWeightedVariant(lv), lv2));
            BlockStateModelGenerator.this.registerParentedItemModel(logBlock, lv);
            return this;
        }

        public LogTexturePool uvLockedLog(Block logBlock) {
            BlockStateModelGenerator.this.blockStateCollector.accept(BlockStateModelGenerator.createUvLockedColumnBlockState(logBlock, this.textures, BlockStateModelGenerator.this.modelCollector));
            BlockStateModelGenerator.this.registerParentedItemModel(logBlock, Models.CUBE_COLUMN.upload(logBlock, this.textures, BlockStateModelGenerator.this.modelCollector));
            return this;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum CrossType {
        TINTED(Models.TINTED_CROSS, Models.TINTED_FLOWER_POT_CROSS, false),
        NOT_TINTED(Models.CROSS, Models.FLOWER_POT_CROSS, false),
        EMISSIVE_NOT_TINTED(Models.CROSS_EMISSIVE, Models.FLOWER_POT_CROSS_EMISSIVE, true);

        private final Model model;
        private final Model flowerPotModel;
        private final boolean emissive;

        private CrossType(Model model, Model flowerPotModel, boolean emissive) {
            this.model = model;
            this.flowerPotModel = flowerPotModel;
            this.emissive = emissive;
        }

        public Model getCrossModel() {
            return this.model;
        }

        public Model getFlowerPotCrossModel() {
            return this.flowerPotModel;
        }

        public Identifier registerItemModel(BlockStateModelGenerator modelGenerator, Block block) {
            Item lv = block.asItem();
            if (this.emissive) {
                return modelGenerator.uploadTwoLayerBlockItemModel(lv, block, "_emissive");
            }
            return modelGenerator.uploadBlockItemModel(lv, block);
        }

        public TextureMap getTextureMap(Block block) {
            return this.emissive ? TextureMap.crossAndCrossEmissive(block) : TextureMap.cross(block);
        }

        public TextureMap getFlowerPotTextureMap(Block block) {
            return this.emissive ? TextureMap.plantAndCrossEmissive(block) : TextureMap.plant(block);
        }
    }

    @Environment(value=EnvType.CLIENT)
    record ChiseledBookshelfModelCacheKey(Model template, String modelSuffix) {
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    static interface StateFactory {
        public BlockModelDefinitionCreator create(Block var1, ModelVariant var2, TextureMap var3, BiConsumer<Identifier, ModelSupplier> var4);
    }
}

