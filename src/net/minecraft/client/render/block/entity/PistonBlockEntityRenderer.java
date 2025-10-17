/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.enums.PistonType;
import net.minecraft.client.render.block.MovingBlockRenderState;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.block.entity.state.PistonBlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class PistonBlockEntityRenderer
implements BlockEntityRenderer<PistonBlockEntity, PistonBlockEntityRenderState> {
    @Override
    public PistonBlockEntityRenderState createRenderState() {
        return new PistonBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(PistonBlockEntity arg, PistonBlockEntityRenderState arg2, float f, Vec3d arg3, @Nullable ModelCommandRenderer.CrumblingOverlayCommand arg4) {
        BlockEntityRenderer.super.updateRenderState(arg, arg2, f, arg3, arg4);
        arg2.offsetX = arg.getRenderOffsetX(f);
        arg2.offsetY = arg.getRenderOffsetY(f);
        arg2.offsetZ = arg.getRenderOffsetZ(f);
        arg2.pushedState = null;
        arg2.extendedPistonState = null;
        BlockState lv = arg.getPushedBlock();
        World lv2 = arg.getWorld();
        if (lv2 != null && !lv.isAir()) {
            BlockPos lv3 = arg.getPos().offset(arg.getMovementDirection().getOpposite());
            RegistryEntry<Biome> lv4 = lv2.getBiome(lv3);
            if (lv.isOf(Blocks.PISTON_HEAD) && arg.getProgress(f) <= 4.0f) {
                lv = (BlockState)lv.with(PistonHeadBlock.SHORT, arg.getProgress(f) <= 0.5f);
                arg2.pushedState = PistonBlockEntityRenderer.renderModel(lv3, lv, lv4, lv2);
            } else if (arg.isSource() && !arg.isExtending()) {
                PistonType lv5 = lv.isOf(Blocks.STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT;
                BlockState lv6 = (BlockState)((BlockState)Blocks.PISTON_HEAD.getDefaultState().with(PistonHeadBlock.TYPE, lv5)).with(PistonHeadBlock.FACING, (Direction)lv.get(PistonBlock.FACING));
                lv6 = (BlockState)lv6.with(PistonHeadBlock.SHORT, arg.getProgress(f) >= 0.5f);
                arg2.pushedState = PistonBlockEntityRenderer.renderModel(lv3, lv6, lv4, lv2);
                BlockPos lv7 = lv3.offset(arg.getMovementDirection());
                lv = (BlockState)lv.with(PistonBlock.EXTENDED, true);
                arg2.extendedPistonState = PistonBlockEntityRenderer.renderModel(lv7, lv, lv4, lv2);
            } else {
                arg2.pushedState = PistonBlockEntityRenderer.renderModel(lv3, lv, lv4, lv2);
            }
        }
    }

    @Override
    public void render(PistonBlockEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        if (arg.pushedState == null) {
            return;
        }
        arg2.push();
        arg2.translate(arg.offsetX, arg.offsetY, arg.offsetZ);
        arg3.submitMovingBlock(arg2, arg.pushedState);
        arg2.pop();
        if (arg.extendedPistonState != null) {
            arg3.submitMovingBlock(arg2, arg.extendedPistonState);
        }
    }

    private static MovingBlockRenderState renderModel(BlockPos pos, BlockState state, RegistryEntry<Biome> biome, World world) {
        MovingBlockRenderState lv = new MovingBlockRenderState();
        lv.fallingBlockPos = pos;
        lv.entityBlockPos = pos;
        lv.blockState = state;
        lv.biome = biome;
        lv.world = world;
        return lv;
    }

    @Override
    public int getRenderDistance() {
        return 68;
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

