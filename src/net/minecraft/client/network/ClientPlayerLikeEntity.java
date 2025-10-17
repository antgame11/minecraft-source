/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerLikeState;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface ClientPlayerLikeEntity {
    public ClientPlayerLikeState getState();

    public SkinTextures getSkin();

    @Nullable
    public Text getMannequinName();

    @Nullable
    public ParrotEntity.Variant getShoulderParrotVariant(boolean var1);

    public boolean hasExtraEars();
}

