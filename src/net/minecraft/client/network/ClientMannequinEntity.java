/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.network;

import com.mojang.logging.LogUtils;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerLikeEntity;
import net.minecraft.client.network.ClientPlayerLikeState;
import net.minecraft.client.texture.PlayerSkinCache;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.MannequinEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ClientMannequinEntity
extends MannequinEntity
implements ClientPlayerLikeEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final SkinTextures DEFAULT_TEXTURES = DefaultSkinHelper.getSkinTextures(MannequinEntity.DEFAULT_INFO.getGameProfile());
    private final ClientPlayerLikeState state = new ClientPlayerLikeState();
    @Nullable
    private CompletableFuture<Optional<SkinTextures>> skinLookup;
    private SkinTextures skin = DEFAULT_TEXTURES;
    private final PlayerSkinCache skinCache;

    public static void setFactory(PlayerSkinCache cache) {
        MannequinEntity.factory = (type, world) -> world instanceof ClientWorld ? new ClientMannequinEntity(world, cache) : new MannequinEntity(type, world);
    }

    public ClientMannequinEntity(World world, PlayerSkinCache skinCache) {
        super(world);
        this.skinCache = skinCache;
    }

    @Override
    public void tick() {
        super.tick();
        this.state.tick(this.getEntityPos(), this.getVelocity());
        if (this.skinLookup != null && this.skinLookup.isDone()) {
            try {
                this.skinLookup.get().ifPresent(this::setSkin);
                this.skinLookup = null;
            } catch (Exception exception) {
                LOGGER.error("Error when trying to look up skin", exception);
            }
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (data.equals(PROFILE)) {
            this.refreshSkin();
        }
    }

    private void refreshSkin() {
        if (this.skinLookup != null) {
            CompletableFuture<Optional<SkinTextures>> completableFuture = this.skinLookup;
            this.skinLookup = null;
            completableFuture.cancel(false);
        }
        this.skinLookup = this.skinCache.getFuture(this.getMannequinProfile()).thenApply(skin -> skin.map(PlayerSkinCache.Entry::getTextures));
    }

    @Override
    public ClientPlayerLikeState getState() {
        return this.state;
    }

    @Override
    public SkinTextures getSkin() {
        return this.skin;
    }

    private void setSkin(SkinTextures skin) {
        this.skin = skin;
    }

    @Override
    @Nullable
    public Text getMannequinName() {
        return this.getDescription();
    }

    @Override
    @Nullable
    public ParrotEntity.Variant getShoulderParrotVariant(boolean leftShoulder) {
        return null;
    }

    @Override
    public boolean hasExtraEars() {
        return false;
    }
}

