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
import net.minecraft.client.render.entity.state.VillagerDataRenderState;
import net.minecraft.client.render.entity.state.ZombieEntityRenderState;
import net.minecraft.village.VillagerData;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ZombieVillagerRenderState
extends ZombieEntityRenderState
implements VillagerDataRenderState {
    @Nullable
    public VillagerData villagerData;

    @Override
    @Nullable
    public VillagerData getVillagerData() {
        return this.villagerData;
    }
}

