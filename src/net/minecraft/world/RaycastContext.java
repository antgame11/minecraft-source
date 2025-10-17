/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import java.util.function.Predicate;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;

public class RaycastContext {
    private final Vec3d start;
    private final Vec3d end;
    private final ShapeType shapeType;
    private final FluidHandling fluid;
    private final ShapeContext shapeContext;

    public RaycastContext(Vec3d start, Vec3d end, ShapeType shapeType, FluidHandling fluidHandling, Entity entity) {
        this(start, end, shapeType, fluidHandling, ShapeContext.of(entity));
    }

    public RaycastContext(Vec3d start, Vec3d end, ShapeType shapeType, FluidHandling fluidHandling, ShapeContext shapeContext) {
        this.start = start;
        this.end = end;
        this.shapeType = shapeType;
        this.fluid = fluidHandling;
        this.shapeContext = shapeContext;
    }

    public Vec3d getEnd() {
        return this.end;
    }

    public Vec3d getStart() {
        return this.start;
    }

    public VoxelShape getBlockShape(BlockState state, BlockView world, BlockPos pos) {
        return this.shapeType.get(state, world, pos, this.shapeContext);
    }

    public VoxelShape getFluidShape(FluidState state, BlockView world, BlockPos pos) {
        return this.fluid.handled(state) ? state.getShape(world, pos) : VoxelShapes.empty();
    }

    public static enum ShapeType implements ShapeProvider
    {
        COLLIDER(AbstractBlock.AbstractBlockState::getCollisionShape),
        OUTLINE(AbstractBlock.AbstractBlockState::getOutlineShape),
        VISUAL(AbstractBlock.AbstractBlockState::getCameraCollisionShape),
        FALLDAMAGE_RESETTING((state, world, pos, context) -> {
            EntityShapeContext lv;
            if (state.isIn(BlockTags.FALL_DAMAGE_RESETTING)) {
                return VoxelShapes.fullCube();
            }
            if (context instanceof EntityShapeContext && (lv = (EntityShapeContext)context).getEntity() != null && lv.getEntity().getType() == EntityType.PLAYER) {
                if (state.isOf(Blocks.END_GATEWAY) || state.isOf(Blocks.END_PORTAL)) {
                    return VoxelShapes.fullCube();
                }
                if (world instanceof ServerWorld) {
                    ServerWorld lv2 = (ServerWorld)world;
                    if (state.isOf(Blocks.NETHER_PORTAL) && lv2.getGameRules().getInt(GameRules.PLAYERS_NETHER_PORTAL_DEFAULT_DELAY) == 0) {
                        return VoxelShapes.fullCube();
                    }
                }
            }
            return VoxelShapes.empty();
        });

        private final ShapeProvider provider;

        private ShapeType(ShapeProvider provider) {
            this.provider = provider;
        }

        @Override
        public VoxelShape get(BlockState arg, BlockView arg2, BlockPos arg3, ShapeContext arg4) {
            return this.provider.get(arg, arg2, arg3, arg4);
        }
    }

    public static enum FluidHandling {
        NONE(state -> false),
        SOURCE_ONLY(FluidState::isStill),
        ANY(state -> !state.isEmpty()),
        WATER(state -> state.isIn(FluidTags.WATER));

        private final Predicate<FluidState> predicate;

        private FluidHandling(Predicate<FluidState> predicate) {
            this.predicate = predicate;
        }

        public boolean handled(FluidState state) {
            return this.predicate.test(state);
        }
    }

    public static interface ShapeProvider {
        public VoxelShape get(BlockState var1, BlockView var2, BlockPos var3, ShapeContext var4);
    }
}

