/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.resource;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.RawTextureDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.biome.DryFoliageColors;

@Environment(value=EnvType.CLIENT)
public class DryFoliageColormapResourceSupplier
extends SinglePreparationResourceReloader<int[]> {
    private static final Identifier DRY_FOLIAGE_COLORMAP = Identifier.ofVanilla("textures/colormap/dry_foliage.png");

    protected int[] reload(ResourceManager resourceManager, Profiler profiler) {
        try {
            return RawTextureDataLoader.loadRawTextureData(resourceManager, DRY_FOLIAGE_COLORMAP);
        } catch (IOException iOException) {
            throw new IllegalStateException("Failed to load dry foliage color texture", iOException);
        }
    }

    @Override
    protected void apply(int[] is, ResourceManager arg, Profiler arg2) {
        DryFoliageColors.setColorMap(is);
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager manager, Profiler profiler) {
        return this.reload(manager, profiler);
    }
}

