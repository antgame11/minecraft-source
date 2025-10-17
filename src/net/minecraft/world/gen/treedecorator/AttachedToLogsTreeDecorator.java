/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.treedecorator;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;

public class AttachedToLogsTreeDecorator
extends TreeDecorator {
    public static final MapCodec<AttachedToLogsTreeDecorator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("probability")).forGetter(treeDecorator -> Float.valueOf(treeDecorator.probability)), ((MapCodec)BlockStateProvider.TYPE_CODEC.fieldOf("block_provider")).forGetter(treeDecorator -> treeDecorator.blockProvider), ((MapCodec)Codecs.nonEmptyList(Direction.CODEC.listOf()).fieldOf("directions")).forGetter(treeDecorator -> treeDecorator.directions)).apply((Applicative<AttachedToLogsTreeDecorator, ?>)instance, AttachedToLogsTreeDecorator::new));
    private final float probability;
    private final BlockStateProvider blockProvider;
    private final List<Direction> directions;

    public AttachedToLogsTreeDecorator(float probability, BlockStateProvider blockProvider, List<Direction> directions) {
        this.probability = probability;
        this.blockProvider = blockProvider;
        this.directions = directions;
    }

    @Override
    public void generate(TreeDecorator.Generator generator) {
        Random lv = generator.getRandom();
        for (BlockPos lv2 : Util.copyShuffled(generator.getLogPositions(), lv)) {
            Direction lv3 = Util.getRandom(this.directions, lv);
            BlockPos lv4 = lv2.offset(lv3);
            if (!(lv.nextFloat() <= this.probability) || !generator.isAir(lv4)) continue;
            generator.replace(lv4, this.blockProvider.get(lv, lv4));
        }
    }

    @Override
    protected TreeDecoratorType<?> getType() {
        return TreeDecoratorType.ATTACHED_TO_LOGS;
    }
}

