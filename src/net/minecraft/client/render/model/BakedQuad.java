/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;

@Environment(value=EnvType.CLIENT)
public record BakedQuad(int[] vertexData, int tintIndex, Direction face, Sprite sprite, boolean shade, int lightEmission) {
    public boolean hasTint() {
        return this.tintIndex != -1;
    }
}

