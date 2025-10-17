/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.treedecorator;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;

public class PlaceOnGroundTreeDecorator
extends TreeDecorator {
    public static final MapCodec<PlaceOnGroundTreeDecorator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codecs.POSITIVE_INT.fieldOf("tries")).orElse(128).forGetter(treeDecorator -> treeDecorator.tries), ((MapCodec)Codecs.NON_NEGATIVE_INT.fieldOf("radius")).orElse(2).forGetter(treeDecorator -> treeDecorator.radius), ((MapCodec)Codecs.NON_NEGATIVE_INT.fieldOf("height")).orElse(1).forGetter(treeDecorator -> treeDecorator.height), ((MapCodec)BlockStateProvider.TYPE_CODEC.fieldOf("block_state_provider")).forGetter(treeDecorator -> treeDecorator.blockStateProvider)).apply((Applicative<PlaceOnGroundTreeDecorator, ?>)instance, PlaceOnGroundTreeDecorator::new));
    private final int tries;
    private final int radius;
    private final int height;
    private final BlockStateProvider blockStateProvider;

    public PlaceOnGroundTreeDecorator(int tries, int radius, int height, BlockStateProvider blockStateProvider) {
        this.tries = tries;
        this.radius = radius;
        this.height = height;
        this.blockStateProvider = blockStateProvider;
    }

    @Override
    protected TreeDecoratorType<?> getType() {
        return TreeDecoratorType.PLACE_ON_GROUND;
    }

    @Override
    public void generate(TreeDecorator.Generator generator) {
        List<BlockPos> list = TreeFeature.getLeafLitterPositions(generator);
        if (list.isEmpty()) {
            return;
        }
        BlockPos lv = list.getFirst();
        int i = lv.getY();
        int j = lv.getX();
        int k = lv.getX();
        int l = lv.getZ();
        int m = lv.getZ();
        for (BlockPos lv2 : list) {
            if (lv2.getY() != i) continue;
            j = Math.min(j, lv2.getX());
            k = Math.max(k, lv2.getX());
            l = Math.min(l, lv2.getZ());
            m = Math.max(m, lv2.getZ());
        }
        Random lv3 = generator.getRandom();
        BlockBox lv4 = new BlockBox(j, i, l, k, i, m).expand(this.radius, this.height, this.radius);
        BlockPos.Mutable lv5 = new BlockPos.Mutable();
        for (int n = 0; n < this.tries; ++n) {
            lv5.set(lv3.nextBetween(lv4.getMinX(), lv4.getMaxX()), lv3.nextBetween(lv4.getMinY(), lv4.getMaxY()), lv3.nextBetween(lv4.getMinZ(), lv4.getMaxZ()));
            this.generate(generator, lv5);
        }
    }

    private void generate(TreeDecorator.Generator generator, BlockPos pos) {
        BlockPos lv = pos.up();
        if (generator.getWorld().testBlockState(lv, state -> state.isAir() || state.isOf(Blocks.VINE)) && generator.matches(pos, AbstractBlock.AbstractBlockState::isOpaqueFullCube) && generator.getWorld().getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos).getY() <= lv.getY()) {
            generator.replace(lv, this.blockStateProvider.get(generator.getRandom(), lv));
        }
    }
}

