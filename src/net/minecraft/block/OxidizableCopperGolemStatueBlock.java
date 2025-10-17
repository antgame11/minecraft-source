/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CopperGolemStatueBlock;
import net.minecraft.block.Degradable;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CopperGolemStatueBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.CopperGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class OxidizableCopperGolemStatueBlock
extends CopperGolemStatueBlock
implements Oxidizable {
    public static final MapCodec<OxidizableCopperGolemStatueBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Oxidizable.OxidationLevel.CODEC.fieldOf("weathering_state")).forGetter(Degradable::getDegradationLevel), OxidizableCopperGolemStatueBlock.createSettingsCodec()).apply((Applicative<OxidizableCopperGolemStatueBlock, ?>)instance, OxidizableCopperGolemStatueBlock::new));

    public MapCodec<OxidizableCopperGolemStatueBlock> getCodec() {
        return CODEC;
    }

    public OxidizableCopperGolemStatueBlock(Oxidizable.OxidationLevel arg, AbstractBlock.Settings arg2) {
        super(arg, arg2);
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return Oxidizable.getIncreasedOxidationBlock(state.getBlock()).isPresent();
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        this.tickDegradation(state, world, pos, random);
    }

    @Override
    public Oxidizable.OxidationLevel getDegradationLevel() {
        return this.getOxidationLevel();
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof CopperGolemStatueBlockEntity) {
            CopperGolemStatueBlockEntity lv = (CopperGolemStatueBlockEntity)blockEntity;
            if (stack.isIn(ItemTags.AXES)) {
                if (this.getDegradationLevel().equals(Oxidizable.OxidationLevel.UNAFFECTED)) {
                    CopperGolemEntity lv2 = lv.createCopperGolem(state);
                    stack.damage(1, (LivingEntity)player, hand.getEquipmentSlot());
                    if (lv2 != null) {
                        world.spawnEntity(lv2);
                        world.removeBlock(pos, false);
                        return ActionResult.SUCCESS;
                    }
                }
            } else {
                if (stack.isOf(Items.HONEYCOMB)) {
                    return ActionResult.PASS;
                }
                this.changePose(world, state, pos, player);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public /* synthetic */ Enum getDegradationLevel() {
        return this.getDegradationLevel();
    }
}

