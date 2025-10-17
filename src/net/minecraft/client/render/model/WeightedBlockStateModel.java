/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.ResolvableModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class WeightedBlockStateModel
implements BlockStateModel {
    private final Pool<BlockStateModel> models;
    private final Sprite particleSprite;

    public WeightedBlockStateModel(Pool<BlockStateModel> models) {
        this.models = models;
        BlockStateModel lv = models.getEntries().getFirst().value();
        this.particleSprite = lv.particleSprite();
    }

    @Override
    public Sprite particleSprite() {
        return this.particleSprite;
    }

    @Override
    public void addParts(Random random, List<BlockModelPart> parts) {
        this.models.get(random).addParts(random, parts);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked(Pool<BlockStateModel.Unbaked> entries) implements BlockStateModel.Unbaked
    {
        @Override
        public BlockStateModel bake(Baker baker) {
            return new WeightedBlockStateModel(this.entries.transform(model -> model.bake(baker)));
        }

        @Override
        public void resolve(ResolvableModel.Resolver resolver) {
            this.entries.getEntries().forEach(entry -> ((BlockStateModel.Unbaked)entry.value()).resolve(resolver));
        }
    }
}

