/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure.pool;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.block.enums.Orientation;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.PlacedFeature;

public class FeaturePoolElement
extends StructurePoolElement {
    public static final MapCodec<FeaturePoolElement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)PlacedFeature.REGISTRY_CODEC.fieldOf("feature")).forGetter(pool -> pool.feature), FeaturePoolElement.projectionGetter()).apply((Applicative<FeaturePoolElement, ?>)instance, FeaturePoolElement::new));
    private static final Identifier DEFAULT_NAME = Identifier.ofVanilla("bottom");
    private final RegistryEntry<PlacedFeature> feature;
    private final NbtCompound nbt;

    protected FeaturePoolElement(RegistryEntry<PlacedFeature> feature, StructurePool.Projection projection) {
        super(projection);
        this.feature = feature;
        this.nbt = this.createDefaultJigsawNbt();
    }

    private NbtCompound createDefaultJigsawNbt() {
        NbtCompound lv = new NbtCompound();
        lv.put("name", Identifier.CODEC, DEFAULT_NAME);
        lv.putString("final_state", "minecraft:air");
        lv.put("pool", JigsawBlockEntity.STRUCTURE_POOL_KEY_CODEC, StructurePools.EMPTY);
        lv.put("target", Identifier.CODEC, JigsawBlockEntity.DEFAULT_NAME);
        lv.put("joint", JigsawBlockEntity.Joint.CODEC, JigsawBlockEntity.Joint.ROLLABLE);
        return lv;
    }

    @Override
    public Vec3i getStart(StructureTemplateManager structureTemplateManager, BlockRotation rotation) {
        return Vec3i.ZERO;
    }

    @Override
    public List<StructureTemplate.JigsawBlockInfo> getStructureBlockInfos(StructureTemplateManager structureTemplateManager, BlockPos pos, BlockRotation rotation, Random random) {
        return List.of(StructureTemplate.JigsawBlockInfo.of(new StructureTemplate.StructureBlockInfo(pos, (BlockState)Blocks.JIGSAW.getDefaultState().with(JigsawBlock.ORIENTATION, Orientation.byDirections(Direction.DOWN, Direction.SOUTH)), this.nbt)));
    }

    @Override
    public BlockBox getBoundingBox(StructureTemplateManager structureTemplateManager, BlockPos pos, BlockRotation rotation) {
        Vec3i lv = this.getStart(structureTemplateManager, rotation);
        return new BlockBox(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + lv.getX(), pos.getY() + lv.getY(), pos.getZ() + lv.getZ());
    }

    @Override
    public boolean generate(StructureTemplateManager structureTemplateManager, StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, BlockPos pos, BlockPos pivot, BlockRotation rotation, BlockBox box, Random random, StructureLiquidSettings liquidSettings, boolean keepJigsaws) {
        return this.feature.value().generateUnregistered(world, chunkGenerator, random, pos);
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return StructurePoolElementType.FEATURE_POOL_ELEMENT;
    }

    public String toString() {
        return "Feature[" + String.valueOf(this.feature) + "]";
    }
}

