/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import java.lang.runtime.SwitchBootstraps;
import java.util.Collection;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BakedGeometry {
    public static final BakedGeometry EMPTY = new BakedGeometry(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    private final List<BakedQuad> allQuads;
    private final List<BakedQuad> sidelessQuads;
    private final List<BakedQuad> northQuads;
    private final List<BakedQuad> southQuads;
    private final List<BakedQuad> eastQuads;
    private final List<BakedQuad> westQuads;
    private final List<BakedQuad> upQuads;
    private final List<BakedQuad> downQuads;

    BakedGeometry(List<BakedQuad> allQuads, List<BakedQuad> sidelessQuads, List<BakedQuad> northQuads, List<BakedQuad> southQuads, List<BakedQuad> eastQuads, List<BakedQuad> westQuads, List<BakedQuad> upQuads, List<BakedQuad> downQuads) {
        this.allQuads = allQuads;
        this.sidelessQuads = sidelessQuads;
        this.northQuads = northQuads;
        this.southQuads = southQuads;
        this.eastQuads = eastQuads;
        this.westQuads = westQuads;
        this.upQuads = upQuads;
        this.downQuads = downQuads;
    }

    public List<BakedQuad> getQuads(@Nullable Direction side) {
        Direction direction = side;
        int n = 0;
        return switch (SwitchBootstraps.enumSwitch("enumSwitch", new Object[]{"NORTH", "SOUTH", "EAST", "WEST", "UP", "DOWN"}, (Direction)direction, n)) {
            default -> throw new MatchException(null, null);
            case -1 -> this.sidelessQuads;
            case 0 -> this.northQuads;
            case 1 -> this.southQuads;
            case 2 -> this.eastQuads;
            case 3 -> this.westQuads;
            case 4 -> this.upQuads;
            case 5 -> this.downQuads;
        };
    }

    public List<BakedQuad> getAllQuads() {
        return this.allQuads;
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final ImmutableList.Builder<BakedQuad> sidelessQuads = ImmutableList.builder();
        private final Multimap<Direction, BakedQuad> sidedQuads = ArrayListMultimap.create();

        public Builder add(Direction side, BakedQuad quad) {
            this.sidedQuads.put(side, quad);
            return this;
        }

        public Builder add(BakedQuad quad) {
            this.sidelessQuads.add((Object)quad);
            return this;
        }

        private static BakedGeometry buildFromList(List<BakedQuad> quads, int sidelessCount, int northCount, int southCount, int eastCount, int westCount, int upCount, int downCount) {
            int p = 0;
            List<BakedQuad> list2 = quads.subList(p, p += sidelessCount);
            List<BakedQuad> list3 = quads.subList(p, p += northCount);
            List<BakedQuad> list4 = quads.subList(p, p += southCount);
            List<BakedQuad> list5 = quads.subList(p, p += eastCount);
            List<BakedQuad> list6 = quads.subList(p, p += westCount);
            List<BakedQuad> list7 = quads.subList(p, p += upCount);
            List<BakedQuad> list8 = quads.subList(p, p + downCount);
            return new BakedGeometry(quads, list2, list3, list4, list5, list6, list7, list8);
        }

        public BakedGeometry build() {
            ImmutableCollection immutableList = this.sidelessQuads.build();
            if (this.sidedQuads.isEmpty()) {
                if (immutableList.isEmpty()) {
                    return EMPTY;
                }
                return new BakedGeometry((List<BakedQuad>)((Object)immutableList), (List<BakedQuad>)((Object)immutableList), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
            }
            ImmutableList.Builder builder = ImmutableList.builder();
            builder.addAll((Iterable)immutableList);
            Collection<BakedQuad> collection = this.sidedQuads.get(Direction.NORTH);
            builder.addAll(collection);
            Collection<BakedQuad> collection2 = this.sidedQuads.get(Direction.SOUTH);
            builder.addAll(collection2);
            Collection<BakedQuad> collection3 = this.sidedQuads.get(Direction.EAST);
            builder.addAll(collection3);
            Collection<BakedQuad> collection4 = this.sidedQuads.get(Direction.WEST);
            builder.addAll(collection4);
            Collection<BakedQuad> collection5 = this.sidedQuads.get(Direction.UP);
            builder.addAll(collection5);
            Collection<BakedQuad> collection6 = this.sidedQuads.get(Direction.DOWN);
            builder.addAll(collection6);
            return Builder.buildFromList((List<BakedQuad>)((Object)builder.build()), immutableList.size(), collection.size(), collection2.size(), collection3.size(), collection4.size(), collection5.size(), collection6.size());
        }
    }
}

