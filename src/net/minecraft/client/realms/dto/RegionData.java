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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.RealmsSerializable;
import net.minecraft.client.realms.ServiceQuality;
import net.minecraft.client.realms.dto.RealmsRegion;

@Environment(value=EnvType.CLIENT)
public record RegionData(@SerializedName(value="regionName") @JsonAdapter(value=RealmsRegion.RegionTypeAdapter.class) RealmsRegion region, @SerializedName(value="serviceQuality") @JsonAdapter(value=ServiceQuality.ServiceQualityTypeAdapter.class) ServiceQuality serviceQuality) implements RealmsSerializable
{
    @SerializedName(value="regionName")
    public RealmsRegion region() {
        return this.region;
    }

    @SerializedName(value="serviceQuality")
    public ServiceQuality serviceQuality() {
        return this.serviceQuality;
    }
}

