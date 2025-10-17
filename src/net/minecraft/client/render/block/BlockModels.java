/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block;

import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.texture.Sprite;

@Environment(value=EnvType.CLIENT)
public class BlockModels {
    private Map<BlockState, BlockStateModel> models = Map.of();
    private final BakedModelManager modelManager;

    public BlockModels(BakedModelManager modelManager) {
        this.modelManager = modelManager;
    }

    public Sprite getModelParticleSprite(BlockState state) {
        return this.getModel(state).particleSprite();
    }

    public BlockStateModel getModel(BlockState state) {
        BlockStateModel lv = this.models.get(state);
        if (lv == null) {
            lv = this.modelManager.getMissingModel();
        }
        return lv;
    }

    public BakedModelManager getModelManager() {
        return this.modelManager;
    }

    public void setModels(Map<BlockState, BlockStateModel> models) {
        this.models = models;
    }
}

