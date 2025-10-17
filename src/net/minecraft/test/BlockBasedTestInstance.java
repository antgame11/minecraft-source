/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TestBlock;
import net.minecraft.block.entity.TestBlockEntity;
import net.minecraft.block.enums.TestBlockMode;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestData;
import net.minecraft.test.TestEnvironmentDefinition;
import net.minecraft.test.TestInstance;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class BlockBasedTestInstance
extends TestInstance {
    public static final MapCodec<BlockBasedTestInstance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(TestData.CODEC.forGetter(TestInstance::getData)).apply((Applicative<BlockBasedTestInstance, ?>)instance, BlockBasedTestInstance::new));

    public BlockBasedTestInstance(TestData<RegistryEntry<TestEnvironmentDefinition>> arg) {
        super(arg);
    }

    @Override
    public void start(TestContext context) {
        BlockPos lv = this.findStartBlockPos(context);
        TestBlockEntity lv2 = context.getBlockEntity(lv, TestBlockEntity.class);
        lv2.trigger();
        context.forEachRemainingTick(() -> {
            boolean bl;
            List<BlockPos> list = this.findTestBlocks(context, TestBlockMode.ACCEPT);
            if (list.isEmpty()) {
                context.throwGameTestException(Text.translatable("test_block.error.missing", TestBlockMode.ACCEPT.getName()));
            }
            if (bl = list.stream().map(pos -> context.getBlockEntity((BlockPos)pos, TestBlockEntity.class)).anyMatch(TestBlockEntity::hasTriggered)) {
                context.complete();
            } else {
                this.handleTrigger(context, TestBlockMode.FAIL, testBlockEntity -> context.throwGameTestException(Text.literal(testBlockEntity.getMessage())));
                this.handleTrigger(context, TestBlockMode.LOG, TestBlockEntity::trigger);
            }
        });
    }

    private void handleTrigger(TestContext context, TestBlockMode mode, Consumer<TestBlockEntity> callback) {
        List<BlockPos> list = this.findTestBlocks(context, mode);
        for (BlockPos lv : list) {
            TestBlockEntity lv2 = context.getBlockEntity(lv, TestBlockEntity.class);
            if (!lv2.hasTriggered()) continue;
            callback.accept(lv2);
            lv2.reset();
        }
    }

    private BlockPos findStartBlockPos(TestContext context) {
        List<BlockPos> list = this.findTestBlocks(context, TestBlockMode.START);
        if (list.isEmpty()) {
            context.throwGameTestException(Text.translatable("test_block.error.missing", TestBlockMode.START.getName()));
        }
        if (list.size() != 1) {
            context.throwGameTestException(Text.translatable("test_block.error.too_many", TestBlockMode.START.getName()));
        }
        return list.getFirst();
    }

    private List<BlockPos> findTestBlocks(TestContext context, TestBlockMode mode) {
        ArrayList<BlockPos> list = new ArrayList<BlockPos>();
        context.forEachRelativePos(pos -> {
            BlockState lv = context.getBlockState((BlockPos)pos);
            if (lv.isOf(Blocks.TEST_BLOCK) && lv.get(TestBlock.MODE) == mode) {
                list.add(pos.toImmutable());
            }
        });
        return list;
    }

    public MapCodec<BlockBasedTestInstance> getCodec() {
        return CODEC;
    }

    @Override
    protected MutableText getTypeDescription() {
        return Text.translatable("test_instance.type.block_based");
    }
}

