/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.fluid.Fluid;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.State;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class FluidState
extends State<Fluid, FluidState> {
    public static final Codec<FluidState> CODEC = FluidState.createCodec(Registries.FLUID.getCodec(), Fluid::getDefaultState).stable();
    public static final int MAX_AMOUNT = 9;
    public static final int MAX_FLUID_LEVEL = 8;

    public FluidState(Fluid fluid, Reference2ObjectArrayMap<Property<?>, Comparable<?>> propertyMap, MapCodec<FluidState> codec) {
        super(fluid, propertyMap, codec);
    }

    public Fluid getFluid() {
        return (Fluid)this.owner;
    }

    public boolean isStill() {
        return this.getFluid().isStill(this);
    }

    public boolean isEqualAndStill(Fluid fluid) {
        return this.owner == fluid && ((Fluid)this.owner).isStill(this);
    }

    public boolean isEmpty() {
        return this.getFluid().isEmpty();
    }

    public float getHeight(BlockView world, BlockPos pos) {
        return this.getFluid().getHeight(this, world, pos);
    }

    public float getHeight() {
        return this.getFluid().getHeight(this);
    }

    public int getLevel() {
        return this.getFluid().getLevel(this);
    }

    public boolean canFlowTo(BlockView world, BlockPos pos) {
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                BlockPos lv = pos.add(i, 0, j);
                FluidState lv2 = world.getFluidState(lv);
                if (lv2.getFluid().matchesType(this.getFluid()) || world.getBlockState(lv).isOpaqueFullCube()) continue;
                return true;
            }
        }
        return false;
    }

    public void onScheduledTick(ServerWorld world, BlockPos pos, BlockState state) {
        this.getFluid().onScheduledTick(world, pos, state, this);
    }

    public void randomDisplayTick(World world, BlockPos pos, Random random) {
        this.getFluid().randomDisplayTick(world, pos, this, random);
    }

    public boolean hasRandomTicks() {
        return this.getFluid().hasRandomTicks();
    }

    public void onRandomTick(ServerWorld world, BlockPos pos, Random random) {
        this.getFluid().onRandomTick(world, pos, this, random);
    }

    public Vec3d getVelocity(BlockView world, BlockPos pos) {
        return this.getFluid().getVelocity(world, pos, this);
    }

    public BlockState getBlockState() {
        return this.getFluid().toBlockState(this);
    }

    @Nullable
    public ParticleEffect getParticle() {
        return this.getFluid().getParticle();
    }

    public boolean isIn(TagKey<Fluid> tag) {
        return this.getFluid().getRegistryEntry().isIn(tag);
    }

    public boolean isIn(RegistryEntryList<Fluid> fluids) {
        return fluids.contains(this.getFluid().getRegistryEntry());
    }

    public boolean isOf(Fluid fluid) {
        return this.getFluid() == fluid;
    }

    public float getBlastResistance() {
        return this.getFluid().getBlastResistance();
    }

    public boolean canBeReplacedWith(BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        return this.getFluid().canBeReplacedWith(this, world, pos, fluid, direction);
    }

    public VoxelShape getShape(BlockView world, BlockPos pos) {
        return this.getFluid().getShape(this, world, pos);
    }

    @Nullable
    public Box getCollisionBox(BlockView world, BlockPos pos) {
        return this.getFluid().getCollisionBox(this, world, pos);
    }

    public RegistryEntry<Fluid> getRegistryEntry() {
        return ((Fluid)this.owner).getRegistryEntry();
    }

    public Stream<TagKey<Fluid>> streamTags() {
        return ((Fluid)this.owner).getRegistryEntry().streamTags();
    }

    public void onEntityCollision(World world, BlockPos pos, Entity entity, EntityCollisionHandler handler) {
        this.getFluid().onEntityCollision(world, pos, entity, handler);
    }
}

