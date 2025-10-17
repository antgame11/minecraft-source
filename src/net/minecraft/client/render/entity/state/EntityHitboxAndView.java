/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.state;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.EntityHitbox;

@Environment(value=EnvType.CLIENT)
public record EntityHitboxAndView(double viewX, double viewY, double viewZ, ImmutableList<EntityHitbox> hitboxes) {
}

