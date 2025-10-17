/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.player;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.AssetInfo;
import org.jetbrains.annotations.Nullable;

public record SkinTextures(AssetInfo.TextureAsset body, @Nullable AssetInfo.TextureAsset cape, @Nullable AssetInfo.TextureAsset elytra, PlayerSkinType model, boolean secure) {
    public static SkinTextures create(AssetInfo.TextureAsset body, @Nullable AssetInfo.TextureAsset cape, @Nullable AssetInfo.TextureAsset elytra, PlayerSkinType model) {
        return new SkinTextures(body, cape, elytra, model, false);
    }

    public SkinTextures withOverride(SkinOverride override) {
        if (override.equals(SkinOverride.EMPTY)) {
            return this;
        }
        return SkinTextures.create(DataFixUtils.orElse(override.body, this.body), DataFixUtils.orElse(override.cape, this.cape), DataFixUtils.orElse(override.elytra, this.elytra), override.model.orElse(this.model));
    }

    @Nullable
    public AssetInfo.TextureAsset cape() {
        return this.cape;
    }

    @Nullable
    public AssetInfo.TextureAsset elytra() {
        return this.elytra;
    }

    public record SkinOverride(Optional<AssetInfo.TextureAssetInfo> body, Optional<AssetInfo.TextureAssetInfo> cape, Optional<AssetInfo.TextureAssetInfo> elytra, Optional<PlayerSkinType> model) {
        public static final SkinOverride EMPTY = new SkinOverride(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        public static final MapCodec<SkinOverride> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(AssetInfo.TextureAssetInfo.CODEC.optionalFieldOf("texture").forGetter(SkinOverride::body), AssetInfo.TextureAssetInfo.CODEC.optionalFieldOf("cape").forGetter(SkinOverride::cape), AssetInfo.TextureAssetInfo.CODEC.optionalFieldOf("elytra").forGetter(SkinOverride::elytra), PlayerSkinType.CODEC.optionalFieldOf("model").forGetter(SkinOverride::model)).apply((Applicative<SkinOverride, ?>)instance, SkinOverride::create));
        public static final PacketCodec<ByteBuf, SkinOverride> PACKET_CODEC = PacketCodec.tuple(AssetInfo.TextureAssetInfo.PACKET_CODEC.collect(PacketCodecs::optional), SkinOverride::body, AssetInfo.TextureAssetInfo.PACKET_CODEC.collect(PacketCodecs::optional), SkinOverride::cape, AssetInfo.TextureAssetInfo.PACKET_CODEC.collect(PacketCodecs::optional), SkinOverride::elytra, PlayerSkinType.PACKET_CODEC.collect(PacketCodecs::optional), SkinOverride::model, SkinOverride::create);

        public static SkinOverride create(Optional<AssetInfo.TextureAssetInfo> texture, Optional<AssetInfo.TextureAssetInfo> cape, Optional<AssetInfo.TextureAssetInfo> elytra, Optional<PlayerSkinType> model) {
            if (texture.isEmpty() && cape.isEmpty() && elytra.isEmpty() && model.isEmpty()) {
                return EMPTY;
            }
            return new SkinOverride(texture, cape, elytra, model);
        }
    }
}

