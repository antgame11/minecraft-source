/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.dto;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.CheckedGson;
import net.minecraft.client.realms.RealmsSerializable;
import net.minecraft.client.realms.ServiceQuality;
import net.minecraft.client.realms.dto.RealmsRegion;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public record RealmsServerAddress(@Nullable @SerializedName(value="address") String address, @Nullable @SerializedName(value="resourcePackUrl") String resourcePackUrl, @Nullable @SerializedName(value="resourcePackHash") String resourcePackHash, @Nullable @SerializedName(value="sessionRegionData") RegionData regionData) implements RealmsSerializable
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final RealmsServerAddress NULL = new RealmsServerAddress(null, null, null, null);

    public static RealmsServerAddress parse(CheckedGson gson, String json) {
        try {
            RealmsServerAddress lv = gson.fromJson(json, RealmsServerAddress.class);
            if (lv == null) {
                LOGGER.error("Could not parse RealmsServerAddress: {}", (Object)json);
                return NULL;
            }
            return lv;
        } catch (Exception exception) {
            LOGGER.error("Could not parse RealmsServerAddress: {}", (Object)exception.getMessage());
            return NULL;
        }
    }

    @SerializedName(value="address")
    @Nullable
    public String address() {
        return this.address;
    }

    @SerializedName(value="resourcePackUrl")
    @Nullable
    public String resourcePackUrl() {
        return this.resourcePackUrl;
    }

    @SerializedName(value="resourcePackHash")
    @Nullable
    public String resourcePackHash() {
        return this.resourcePackHash;
    }

    @SerializedName(value="sessionRegionData")
    @Nullable
    public RegionData regionData() {
        return this.regionData;
    }

    @Environment(value=EnvType.CLIENT)
    public record RegionData(@Nullable @SerializedName(value="regionName") @JsonAdapter(value=RealmsRegion.RegionTypeAdapter.class) RealmsRegion region, @Nullable @SerializedName(value="serviceQuality") @JsonAdapter(value=ServiceQuality.ServiceQualityTypeAdapter.class) ServiceQuality serviceQuality) implements RealmsSerializable
    {
        @SerializedName(value="regionName")
        @Nullable
        public RealmsRegion region() {
            return this.region;
        }

        @SerializedName(value="serviceQuality")
        @Nullable
        public ServiceQuality serviceQuality() {
            return this.serviceQuality;
        }
    }
}

