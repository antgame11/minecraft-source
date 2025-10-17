/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.StructureBoxRendering;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class StructureBlockBlockEntityRenderState
extends BlockEntityRenderState {
    public boolean visible;
    public StructureBoxRendering.RenderMode renderMode;
    public StructureBoxRendering.StructureBox structureBox;
    @Nullable
    public InvisibleRenderType[] invisibleBlocks;
    @Nullable
    public boolean[] field_62682;

    @Environment(value=EnvType.CLIENT)
    public static enum InvisibleRenderType {
        AIR,
        BARRIER,
        LIGHT,
        STRUCUTRE_VOID;

    }
}

