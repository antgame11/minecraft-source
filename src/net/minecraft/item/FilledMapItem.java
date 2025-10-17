/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multisets;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.component.type.MapPostProcessingComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

public class FilledMapItem
extends Item {
    public static final int field_30907 = 128;
    public static final int field_30908 = 128;

    public FilledMapItem(Item.Settings arg) {
        super(arg);
    }

    public static ItemStack createMap(ServerWorld world, int x, int z, byte scale, boolean showIcons, boolean unlimitedTracking) {
        ItemStack lv = new ItemStack(Items.FILLED_MAP);
        MapIdComponent lv2 = FilledMapItem.allocateMapId(world, x, z, scale, showIcons, unlimitedTracking, world.getRegistryKey());
        lv.set(DataComponentTypes.MAP_ID, lv2);
        return lv;
    }

    @Nullable
    public static MapState getMapState(@Nullable MapIdComponent id, World world) {
        return id == null ? null : world.getMapState(id);
    }

    @Nullable
    public static MapState getMapState(ItemStack map, World world) {
        MapIdComponent lv = map.get(DataComponentTypes.MAP_ID);
        return FilledMapItem.getMapState(lv, world);
    }

    private static MapIdComponent allocateMapId(ServerWorld world, int x, int z, int scale, boolean showIcons, boolean unlimitedTracking, RegistryKey<World> dimension) {
        MapState lv = MapState.of(x, z, (byte)scale, showIcons, unlimitedTracking, dimension);
        MapIdComponent lv2 = world.increaseAndGetMapId();
        world.putMapState(lv2, lv);
        return lv2;
    }

    public void updateColors(World world, Entity entity, MapState state) {
        if (world.getRegistryKey() != state.dimension || !(entity instanceof PlayerEntity)) {
            return;
        }
        int i = 1 << state.scale;
        int j = state.centerX;
        int k = state.centerZ;
        int l = MathHelper.floor(entity.getX() - (double)j) / i + 64;
        int m = MathHelper.floor(entity.getZ() - (double)k) / i + 64;
        int n = 128 / i;
        if (world.getDimension().hasCeiling()) {
            n /= 2;
        }
        MapState.PlayerUpdateTracker lv = state.getPlayerSyncData((PlayerEntity)entity);
        ++lv.field_131;
        BlockPos.Mutable lv2 = new BlockPos.Mutable();
        BlockPos.Mutable lv3 = new BlockPos.Mutable();
        boolean bl = false;
        for (int o = l - n + 1; o < l + n; ++o) {
            if ((o & 0xF) != (lv.field_131 & 0xF) && !bl) continue;
            bl = false;
            double d = 0.0;
            for (int p = m - n - 1; p < m + n; ++p) {
                double f;
                if (o < 0 || p < -1 || o >= 128 || p >= 128) continue;
                int q = MathHelper.square(o - l) + MathHelper.square(p - m);
                boolean bl2 = q > (n - 2) * (n - 2);
                int r = (j / i + o - 64) * i;
                int s = (k / i + p - 64) * i;
                LinkedHashMultiset<MapColor> multiset = LinkedHashMultiset.create();
                WorldChunk lv4 = world.getChunk(ChunkSectionPos.getSectionCoord(r), ChunkSectionPos.getSectionCoord(s));
                if (lv4.isEmpty()) continue;
                int t = 0;
                double e = 0.0;
                if (world.getDimension().hasCeiling()) {
                    u = r + s * 231871;
                    if (((u = u * u * 31287121 + u * 11) >> 20 & 1) == 0) {
                        multiset.add(Blocks.DIRT.getDefaultState().getMapColor(world, BlockPos.ORIGIN), 10);
                    } else {
                        multiset.add(Blocks.STONE.getDefaultState().getMapColor(world, BlockPos.ORIGIN), 100);
                    }
                    e = 100.0;
                } else {
                    for (u = 0; u < i; ++u) {
                        for (int v = 0; v < i; ++v) {
                            BlockState lv5;
                            lv2.set(r + u, 0, s + v);
                            int w = lv4.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, lv2.getX(), lv2.getZ()) + 1;
                            if (w > world.getBottomY()) {
                                do {
                                    lv2.setY(--w);
                                } while ((lv5 = lv4.getBlockState(lv2)).getMapColor(world, lv2) == MapColor.CLEAR && w > world.getBottomY());
                                if (w > world.getBottomY() && !lv5.getFluidState().isEmpty()) {
                                    BlockState lv6;
                                    int x = w - 1;
                                    lv3.set(lv2);
                                    do {
                                        lv3.setY(x--);
                                        lv6 = lv4.getBlockState(lv3);
                                        ++t;
                                    } while (x > world.getBottomY() && !lv6.getFluidState().isEmpty());
                                    lv5 = this.getFluidStateIfVisible(world, lv5, lv2);
                                }
                            } else {
                                lv5 = Blocks.BEDROCK.getDefaultState();
                            }
                            state.removeBanner(world, lv2.getX(), lv2.getZ());
                            e += (double)w / (double)(i * i);
                            multiset.add(lv5.getMapColor(world, lv2));
                        }
                    }
                }
                MapColor lv7 = Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MapColor.CLEAR);
                MapColor.Brightness lv8 = lv7 == MapColor.WATER_BLUE ? ((f = (double)(t /= i * i) * 0.1 + (double)(o + p & 1) * 0.2) < 0.5 ? MapColor.Brightness.HIGH : (f > 0.9 ? MapColor.Brightness.LOW : MapColor.Brightness.NORMAL)) : ((f = (e - d) * 4.0 / (double)(i + 4) + ((double)(o + p & 1) - 0.5) * 0.4) > 0.6 ? MapColor.Brightness.HIGH : (f < -0.6 ? MapColor.Brightness.LOW : MapColor.Brightness.NORMAL));
                d = e;
                if (p < 0 || q >= n * n || bl2 && (o + p & 1) == 0) continue;
                bl |= state.putColor(o, p, lv7.getRenderColorByte(lv8));
            }
        }
    }

    private BlockState getFluidStateIfVisible(World world, BlockState state, BlockPos pos) {
        FluidState lv = state.getFluidState();
        if (!lv.isEmpty() && !state.isSideSolidFullSquare(world, pos, Direction.UP)) {
            return lv.getBlockState();
        }
        return state;
    }

    private static boolean isAquaticBiome(boolean[] biomes, int x, int z) {
        return biomes[z * 128 + x];
    }

    public static void fillExplorationMap(ServerWorld world, ItemStack map) {
        int o;
        int n;
        MapState lv = FilledMapItem.getMapState(map, (World)world);
        if (lv == null) {
            return;
        }
        if (world.getRegistryKey() != lv.dimension) {
            return;
        }
        int i = 1 << lv.scale;
        int j = lv.centerX;
        int k = lv.centerZ;
        boolean[] bls = new boolean[16384];
        int l = j / i - 64;
        int m = k / i - 64;
        BlockPos.Mutable lv2 = new BlockPos.Mutable();
        for (n = 0; n < 128; ++n) {
            for (o = 0; o < 128; ++o) {
                RegistryEntry<Biome> lv3 = world.getBiome(lv2.set((l + o) * i, 0, (m + n) * i));
                bls[n * 128 + o] = lv3.isIn(BiomeTags.WATER_ON_MAP_OUTLINES);
            }
        }
        for (n = 1; n < 127; ++n) {
            for (o = 1; o < 127; ++o) {
                int p = 0;
                for (int q = -1; q < 2; ++q) {
                    for (int r = -1; r < 2; ++r) {
                        if (q == 0 && r == 0 || !FilledMapItem.isAquaticBiome(bls, n + q, o + r)) continue;
                        ++p;
                    }
                }
                MapColor.Brightness lv4 = MapColor.Brightness.LOWEST;
                MapColor lv5 = MapColor.CLEAR;
                if (FilledMapItem.isAquaticBiome(bls, n, o)) {
                    lv5 = MapColor.ORANGE;
                    if (p > 7 && o % 2 == 0) {
                        switch ((n + (int)(MathHelper.sin((float)o + 0.0f) * 7.0f)) / 8 % 5) {
                            case 0: 
                            case 4: {
                                lv4 = MapColor.Brightness.LOW;
                                break;
                            }
                            case 1: 
                            case 3: {
                                lv4 = MapColor.Brightness.NORMAL;
                                break;
                            }
                            case 2: {
                                lv4 = MapColor.Brightness.HIGH;
                            }
                        }
                    } else if (p > 7) {
                        lv5 = MapColor.CLEAR;
                    } else if (p > 5) {
                        lv4 = MapColor.Brightness.NORMAL;
                    } else if (p > 3) {
                        lv4 = MapColor.Brightness.LOW;
                    } else if (p > 1) {
                        lv4 = MapColor.Brightness.LOW;
                    }
                } else if (p > 0) {
                    lv5 = MapColor.BROWN;
                    lv4 = p > 3 ? MapColor.Brightness.NORMAL : MapColor.Brightness.LOWEST;
                }
                if (lv5 == MapColor.CLEAR) continue;
                lv.setColor(n, o, lv5.getRenderColorByte(lv4));
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
        MapState lv = FilledMapItem.getMapState(stack, (World)world);
        if (lv == null) {
            return;
        }
        if (entity instanceof PlayerEntity) {
            PlayerEntity lv2 = (PlayerEntity)entity;
            lv.update(lv2, stack);
        }
        if (!lv.locked && slot != null && slot.getType() == EquipmentSlot.Type.HAND) {
            this.updateColors(world, entity, lv);
        }
    }

    @Override
    public void onCraft(ItemStack stack, World world) {
        MapPostProcessingComponent lv = stack.remove(DataComponentTypes.MAP_POST_PROCESSING);
        if (lv == null) {
            return;
        }
        if (world instanceof ServerWorld) {
            ServerWorld lv2 = (ServerWorld)world;
            switch (lv) {
                case LOCK: {
                    FilledMapItem.copyMap(stack, lv2);
                    break;
                }
                case SCALE: {
                    FilledMapItem.scale(stack, lv2);
                }
            }
        }
    }

    private static void scale(ItemStack map, ServerWorld world) {
        MapState lv = FilledMapItem.getMapState(map, (World)world);
        if (lv != null) {
            MapIdComponent lv2 = world.increaseAndGetMapId();
            world.putMapState(lv2, lv.zoomOut());
            map.set(DataComponentTypes.MAP_ID, lv2);
        }
    }

    private static void copyMap(ItemStack stack, ServerWorld world) {
        MapState lv = FilledMapItem.getMapState(stack, (World)world);
        if (lv != null) {
            MapIdComponent lv2 = world.increaseAndGetMapId();
            MapState lv3 = lv.copy();
            world.putMapState(lv2, lv3);
            stack.set(DataComponentTypes.MAP_ID, lv2);
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockState lv = context.getWorld().getBlockState(context.getBlockPos());
        if (lv.isIn(BlockTags.BANNERS)) {
            MapState lv2;
            if (!context.getWorld().isClient() && (lv2 = FilledMapItem.getMapState(context.getStack(), context.getWorld())) != null && !lv2.addBanner(context.getWorld(), context.getBlockPos())) {
                return ActionResult.FAIL;
            }
            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }
}

