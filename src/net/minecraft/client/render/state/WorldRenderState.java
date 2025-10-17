/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.state;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.BreakingBlockRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.render.state.OutlineRenderState;
import net.minecraft.client.render.state.SkyRenderState;
import net.minecraft.client.render.state.WeatherRenderState;
import net.minecraft.client.render.state.WorldBorderRenderState;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class WorldRenderState {
    public CameraRenderState cameraRenderState = new CameraRenderState();
    public final List<EntityRenderState> entityRenderStates = new ArrayList<EntityRenderState>();
    public final List<BlockEntityRenderState> blockEntityRenderStates = new ArrayList<BlockEntityRenderState>();
    public boolean hasOutline;
    @Nullable
    public OutlineRenderState outlineRenderState;
    public final List<BreakingBlockRenderState> breakingBlockRenderStates = new ArrayList<BreakingBlockRenderState>();
    public final WeatherRenderState weatherRenderState = new WeatherRenderState();
    public final WorldBorderRenderState worldBorderRenderState = new WorldBorderRenderState();
    public final SkyRenderState skyRenderState = new SkyRenderState();

    public void clear() {
        this.entityRenderStates.clear();
        this.blockEntityRenderStates.clear();
        this.breakingBlockRenderStates.clear();
        this.hasOutline = false;
        this.outlineRenderState = null;
        this.weatherRenderState.clear();
        this.worldBorderRenderState.clear();
        this.skyRenderState.clear();
    }
}

