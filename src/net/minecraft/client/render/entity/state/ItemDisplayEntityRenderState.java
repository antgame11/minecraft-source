/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.DisplayEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;

@Environment(value=EnvType.CLIENT)
public class ItemDisplayEntityRenderState
extends DisplayEntityRenderState {
    public final ItemRenderState itemRenderState = new ItemRenderState();

    @Override
    public boolean canRender() {
        return !this.itemRenderState.isEmpty();
    }
}

